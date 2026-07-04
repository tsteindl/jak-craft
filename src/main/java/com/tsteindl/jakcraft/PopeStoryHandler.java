package com.tsteindl.jakcraft;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Drives the Papst storyline on the server:
//   1. Keeps the Helfer (plaza) and Papst (altar) quest NPCs present at their configured
//      coordinates - a testing convenience. Only spawns when a player is nearby (chunk loaded)
//      and that NPC isn't already there, so it never duplicates. The Papst is not respawned while
//      the Müllagerking is alive.
//   2. After the king is defeated, teleports the participating players to the birthday platform
//      once the configured delay has elapsed (time to grab the dropped PhD scroll).
@Mod.EventBusSubscriber(modid = Jakcraft.MODID)
public class PopeStoryHandler {

  private static final int CHECK_INTERVAL_TICKS = 40; // ~2s between spawn checks
  private static final double AREA_RADIUS = 48.0;

  // A player waiting for the victory teleport. They are moved once they've picked up a NEW PhD
  // scroll (more than they held at the king's death - so a scroll from a previous run doesn't
  // count), plus a short beat; or after a fallback timeout as a safety net.
  private static final class VictoryWatch {
    final long fallbackFireTime;          // absolute game time to teleport no matter what
    final int phdBaseline;                // PhD scrolls the player already had when the king died
    long armedFireTime = Long.MAX_VALUE;  // set once the player picks up a new PhD scroll

    VictoryWatch(long fallbackFireTime, int phdBaseline) {
      this.fallbackFireTime = fallbackFireTime;
      this.phdBaseline = phdBaseline;
    }
  }

  // Keyed by player UUID so a logged-out/rejoined player is resolved fresh at fire time.
  private static final Map<UUID, VictoryWatch> VICTORY_WATCHES = new HashMap<>();

  // True while a Müllagerking is alive in the overworld. Updated every overworld tick and read by
  // the block-protection handlers below so the boss fight can't destroy the world.
  private static volatile boolean muellagerActive = false;

  // Called by the Müllagerking when it dies; starts watching nearby players for the PhD pickup.
  public static void scheduleVictoryTeleport(ServerLevel level, List<ServerPlayer> players) {
    if (players.isEmpty()) {
      return;
    }
    long fallbackTicks = Math.max(0L, (long) Config.VICTORY_FALLBACK_SECONDS.get()) * 20L;
    long fallbackFireTime = level.getGameTime() + fallbackTicks;
    for (ServerPlayer player : players) {
      VICTORY_WATCHES.put(player.getUUID(), new VictoryWatch(fallbackFireTime, countPhdScrolls(player)));
    }
  }

  @SubscribeEvent
  public static void onLevelTick(TickEvent.LevelTickEvent event) {
    if (event.phase != TickEvent.Phase.END) {
      return;
    }
    if (!(event.level instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) {
      return;
    }

    muellagerActive = !level.getEntities(ModEntities.MUELLAGERKING.get(), MuellagerKingEntity::isAlive).isEmpty();

    processPendingTeleports(level);

    if (level.getGameTime() % CHECK_INTERVAL_TICKS != 0 || !Config.STORY_AUTO_SPAWN.get()) {
      return;
    }
    ensureNpcsExist(level);
  }

  // --- Block protection: the Müllagerking fight must never destroy the world ---
  // While the king is alive, no mob may grief (this covers the "big creeper"/Kaboomer and any
  // fireball/bomb that respects mob-griefing), explosions can't break blocks, and no mob can
  // directly destroy a block. Player actions (e.g. their own TNT) are left untouched.

  private static boolean inFight(Level level) {
    return muellagerActive && level.dimension() == Level.OVERWORLD;
  }

  @SubscribeEvent
  public static void onMobGriefing(EntityMobGriefingEvent event) {
    if (event.getEntity() != null && inFight(event.getEntity().level())) {
      event.setResult(Event.Result.DENY);
    }
  }

  @SubscribeEvent
  public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
    if (!inFight(event.getLevel())) {
      return;
    }
    // Keep player-caused explosions (their own TNT) working; only neuter mob/boss explosions.
    LivingEntity source = event.getExplosion().getIndirectSourceEntity();
    if (source instanceof Player) {
      return;
    }
    event.getAffectedBlocks().clear(); // entities still take damage; no blocks are broken
  }

  @SubscribeEvent
  public static void onLivingDestroyBlock(LivingDestroyBlockEvent event) {
    if (!(event.getEntity() instanceof Player) && inFight(event.getEntity().level())) {
      event.setCanceled(true);
    }
  }

  // The boss's strong attacks (Crashager, the big creeper, ...) hit far too hard. While the king is
  // alive, scale down all damage the player takes and make sure no single hit can be lethal - the
  // player should be able to enjoy the fight without dying.
  private static final float FIGHT_DAMAGE_SCALE = 0.10F; // strong attacks land at ~10%
  private static final float FIGHT_MIN_HEALTH = 6.0F;    // never drop below 3 hearts during the fight

  @SubscribeEvent
  public static void onPlayerHurtDuringFight(LivingHurtEvent event) {
    if (!(event.getEntity() instanceof Player player) || !inFight(player.level())) {
      return;
    }
    // Leave out-of-world / command kills (void, /kill) working so the player can still be reset.
    if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
      return;
    }
    float reduced = event.getAmount() * FIGHT_DAMAGE_SCALE;
    float maxNonLethal = Math.max(0.0F, player.getHealth() - FIGHT_MIN_HEALTH);
    event.setAmount(Math.min(reduced, maxNonLethal));
  }

  private static void processPendingTeleports(ServerLevel level) {
    if (VICTORY_WATCHES.isEmpty()) {
      return;
    }
    long now = level.getGameTime();
    long postPickupDelay = Math.max(0L, (long) Config.VICTORY_TELEPORT_DELAY_SECONDS.get()) * 20L;
    double x = Config.VICTORY_PLATFORM_X.get() + 0.5;
    double y = Config.VICTORY_PLATFORM_Y.get();
    double z = Config.VICTORY_PLATFORM_Z.get() + 0.5;

    Iterator<Map.Entry<UUID, VictoryWatch>> it = VICTORY_WATCHES.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<UUID, VictoryWatch> entry = it.next();
      VictoryWatch watch = entry.getValue();
      ServerPlayer player = level.getServer().getPlayerList().getPlayer(entry.getKey());
      if (player == null) {
        if (now >= watch.fallbackFireTime) {
          it.remove(); // gave up on a player who never came back
        }
        continue;
      }
      // Arm the short countdown the moment the player picks up a NEW PhD scroll (count goes above
      // what they already had at the king's death). A scroll from a previous run does NOT count.
      if (watch.armedFireTime == Long.MAX_VALUE && countPhdScrolls(player) > watch.phdBaseline) {
        watch.armedFireTime = now + postPickupDelay;
      }
      if (now >= watch.armedFireTime || now >= watch.fallbackFireTime) {
        player.teleportTo(level, x, y, z, player.getYRot(), player.getXRot());
        it.remove();
      }
    }
  }

  private static int countPhdScrolls(ServerPlayer player) {
    var phd = ModItems.PHD.get();
    var inventory = player.getInventory();
    int count = 0;
    for (int i = 0; i < inventory.getContainerSize(); i++) {
      var stack = inventory.getItem(i);
      if (stack.is(phd)) {
        count += stack.getCount();
      }
    }
    return count;
  }

  private static void ensureNpcsExist(ServerLevel level) {
    ensureNpc(level, ModEntities.HELPER_VILLAGER.get(), new BlockPos(
        Config.HELPER_SPAWN_X.get(), Config.HELPER_SPAWN_Y.get(), Config.HELPER_SPAWN_Z.get()));

    // Don't respawn the Papst while the boss (his transformed self) is still alive anywhere.
    boolean kingAlive = !level.getEntities(ModEntities.MUELLAGERKING.get(), king -> true).isEmpty();
    if (!kingAlive) {
      ensureNpc(level, ModEntities.POPE_VILLAGER.get(), new BlockPos(
          Config.POPE_SPAWN_X.get(), Config.POPE_SPAWN_Y.get(), Config.POPE_SPAWN_Z.get()));
    }
  }

  private static void ensureNpc(ServerLevel level, EntityType<PopeVillagerEntity> type, BlockPos pos) {
    // Only act when the target chunk is actually loaded (i.e. a player is nearby).
    if (!level.isLoaded(pos)) {
      return;
    }

    AABB area = new AABB(pos).inflate(AREA_RADIUS);
    for (PopeVillagerEntity existing : level.getEntitiesOfClass(PopeVillagerEntity.class, area)) {
      if (existing.getType() == type) {
        return; // this NPC is already present here
      }
    }

    PopeVillagerEntity npc = type.create(level);
    if (npc == null) {
      return;
    }
    npc.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
    // finalizeSpawn configures the NPC (NoAI, name, invulnerable, ...).
    npc.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
    level.addFreshEntity(npc);
  }
}
