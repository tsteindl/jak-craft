package com.tsteindl.jakcraft;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Jakcraft.MODID)
public class VolleyballShoesEffects {

  private static final int JUMP_BOOST_AMPLIFIER = 0;
  private static final int EFFECT_DURATION_TICKS = 220;
  private static final int REFRESH_THRESHOLD_TICKS = 40;
  private static final boolean AMBIENT = false;
  private static final boolean SHOW_PARTICLES = false;
  private static final boolean SHOW_ICON = false;

  @SubscribeEvent
  public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
    if (event.phase != TickEvent.Phase.END || event.side.isClient()) {
      return;
    }

    Player player = event.player;
    if (isWearingVolleyballShoes(player)) {
      applyJumpBoostIfNeeded(player);
    } else {
      removeManagedJumpBoost(player);
    }
  }

  private static boolean isWearingVolleyballShoes(Player player) {
    ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
    return boots.is(ModItems.VOLLEYBALL_SHOES.get());
  }

  private static void applyJumpBoostIfNeeded(Player player) {
    MobEffectInstance currentEffect = player.getEffect(MobEffects.JUMP);
    if (hasSufficientJumpBoost(currentEffect)) {
      return;
    }

    player.addEffect(new MobEffectInstance(
        MobEffects.JUMP,
        EFFECT_DURATION_TICKS,
        JUMP_BOOST_AMPLIFIER,
        AMBIENT,
        SHOW_PARTICLES,
        SHOW_ICON));
  }

  private static boolean hasSufficientJumpBoost(MobEffectInstance effect) {
    return effect != null
        && effect.getAmplifier() >= JUMP_BOOST_AMPLIFIER
        && effect.getDuration() > REFRESH_THRESHOLD_TICKS;
  }

  private static void removeManagedJumpBoost(Player player) {
    MobEffectInstance currentEffect = player.getEffect(MobEffects.JUMP);
    if (isManagedJumpBoost(currentEffect)) {
      player.removeEffect(MobEffects.JUMP);
    }
  }

  private static boolean isManagedJumpBoost(MobEffectInstance effect) {
    return effect != null
        && effect.getAmplifier() == JUMP_BOOST_AMPLIFIER
        && effect.getDuration() <= EFFECT_DURATION_TICKS
        && !effect.isVisible()
        && !effect.showIcon();
  }
}
