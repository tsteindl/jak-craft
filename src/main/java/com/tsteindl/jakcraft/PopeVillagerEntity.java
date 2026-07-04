package com.tsteindl.jakcraft;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;
import java.util.List;

// A story quest NPC. It stands still at a fixed spot (place it with its spawn egg), and each
// right-click reveals the next line of dialogue (no trade screen opens). The role is decided by
// which entity type it is:
//   HELPER (helper_villager) - the pope's assistant on the plaza; turns you away, then sends you
//                              inside. Never transforms.
//   POPE   (pope_villager)   - the pope at the altar; after his last line he bursts into effects
//                              and becomes the Müllagerking, who immediately targets you.
public class PopeVillagerEntity extends Villager {

  public enum Role { HELPER, POPE }

  // How close a player must right-click again before it counts as a new dialogue advance,
  // so a rapid double-click doesn't skip a line.
  private static final int INTERACT_DEBOUNCE_TICKS = 8;
  // Radius in which dialogue lines are shown to players (so co-op friends read along).
  private static final double DIALOGUE_RANGE = 48.0;

  private int dialogueStep = 0;
  // Small negative sentinel (not Long.MIN_VALUE) so the first "now - lastInteractTick" can't overflow.
  private long lastInteractTick = -1000L;

  public PopeVillagerEntity(EntityType<? extends Villager> entityType, Level level) {
    super(entityType, level);
  }

  // Role is intrinsic to the entity type, so it survives saving/loading with no extra data.
  public Role getRole() {
    return this.getType() == ModEntities.HELPER_VILLAGER.get() ? Role.HELPER : Role.POPE;
  }

  // The quest NPC can never be killed or damaged in normal play; it only leaves play by
  // transforming (the Papst) via dialogue. This is stronger/earlier than setInvulnerable so it
  // works even if setup didn't run. /kill and the void still work so it can be reset.
  @Override
  public boolean hurt(DamageSource source, float amount) {
    if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
      return super.hurt(source, amount);
    }
    return false;
  }

  // Runs on every spawn (spawn egg, story handler, /summon): make it a stationary, protected NPC.
  @Override
  @Nullable
  public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
      MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
    SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    this.setNoAi(true);              // no wandering, panicking, sleeping or trading behaviour
    this.setNoGravity(true);         // stays exactly where it was placed
    this.setInvulnerable(true);      // can't be killed/knocked around during the dialogue
    this.setPersistenceRequired();   // never despawns
    this.setCustomName(Component.literal(getRole() == Role.HELPER ? "Helfer" : "Papst"));
    this.setCustomNameVisible(true);
    return result;
  }

  @Override
  public InteractionResult mobInteract(Player player, InteractionHand hand) {
    // Never open the villager trade screen; this NPC only talks.
    if (this.level().isClientSide) {
      return InteractionResult.SUCCESS;
    }

    long now = this.level().getGameTime();
    if (now - this.lastInteractTick < INTERACT_DEBOUNCE_TICKS) {
      return InteractionResult.CONSUME;
    }
    this.lastInteractTick = now;

    advanceDialogue(player);
    return InteractionResult.CONSUME;
  }

  private List<? extends String> dialogueLines() {
    return getRole() == Role.HELPER ? Config.HELPER_DIALOGUE.get() : Config.POPE_DIALOGUE.get();
  }

  private void advanceDialogue(Player player) {
    List<? extends String> lines = dialogueLines();
    if (lines.isEmpty()) {
      if (getRole() == Role.POPE) {
        transformIntoMueller(player);
      }
      return;
    }

    if (this.dialogueStep < lines.size()) {
      showLine(lines.get(this.dialogueStep));
      this.dialogueStep++;
      if (this.dialogueStep >= lines.size() && getRole() == Role.POPE) {
        transformIntoMueller(player); // the Papst's final line triggers the transformation
      }
    } else {
      // Dialogue finished (HELPER only - the POPE is gone by now): repeat the closing hint.
      showLine(lines.get(lines.size() - 1));
    }
  }

  // Renders one "Sprecher|Text" line into nearby players' chat, coloured by speaker.
  private void showLine(String raw) {
    String speaker = "Papst";
    String text = raw;
    int sep = raw.indexOf('|');
    if (sep >= 0) {
      speaker = raw.substring(0, sep).trim();
      text = raw.substring(sep + 1).trim();
    }

    Component message;
    if (isProtagonist(speaker)) {
      // Emulate a plain player chat message ("<TheRealMikeJohn> ...") so it looks like that
      // player typed it. The name is configurable (Config.PROTAGONIST_NAME).
      message = Component.literal("<" + Config.PROTAGONIST_NAME.get() + "> " + text);
    } else {
      ChatFormatting color = speaker.toLowerCase().matches("helfer|wache|diener|gehilfe")
          ? ChatFormatting.GRAY
          : ChatFormatting.GOLD; // Papst and any other NPC speaker
      message = Component.empty()
          .append(Component.literal("<" + speaker + "> ").withStyle(color, ChatFormatting.BOLD))
          .append(Component.literal(text).withStyle(color));
    }
    broadcastNearby(message);
  }

  // Speaker tokens that stand for the player character (rendered as protagonist_name).
  private static boolean isProtagonist(String speaker) {
    return switch (speaker.toLowerCase()) {
      case "jakob", "spieler", "player", "protagonist" -> true;
      default -> false;
    };
  }

  private void broadcastNearby(Component message) {
    if (!(this.level() instanceof ServerLevel serverLevel)) {
      return;
    }
    for (ServerPlayer player : serverLevel.getEntitiesOfClass(
        ServerPlayer.class, this.getBoundingBox().inflate(DIALOGUE_RANGE))) {
      player.sendSystemMessage(message);
    }
  }

  // Replaces the Papst with the Müllagerking boss at the same spot and starts the fight.
  private void transformIntoMueller(Player player) {
    if (!(this.level() instanceof ServerLevel serverLevel)) {
      return;
    }

    BlockPos pos = this.blockPosition();
    serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
        this.getX(), this.getY() + 1.0, this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
    serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
        this.getX(), this.getY() + 1.0, this.getZ(), 60, 0.5, 1.0, 0.5, 0.05);
    serverLevel.playSound(null, pos, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.2F, 1.1F);
    serverLevel.playSound(null, pos, SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 2.0F, 0.7F);

    MuellagerKingEntity king = ModEntities.MUELLAGERKING.get().create(serverLevel);
    if (king != null) {
      king.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
      king.setYHeadRot(this.getYRot());
      king.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(pos),
          MobSpawnType.CONVERSION, null, null);
      king.setPersistenceRequired();
      king.setTarget(player); // triggers the boss's fight-start voice line
      serverLevel.addFreshEntity(king);
    }

    this.discard();
  }

  @Override
  public void addAdditionalSaveData(CompoundTag tag) {
    super.addAdditionalSaveData(tag);
    tag.putInt("DialogueStep", this.dialogueStep);
  }

  @Override
  public void readAdditionalSaveData(CompoundTag tag) {
    super.readAdditionalSaveData(tag);
    this.dialogueStep = tag.getInt("DialogueStep");
  }
}
