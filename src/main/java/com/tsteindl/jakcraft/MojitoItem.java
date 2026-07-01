package com.tsteindl.jakcraft;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class MojitoItem extends Item {

  private static final int DRINK_DURATION_TICKS = 32;
  private static final int NAUSEA_DURATION_TICKS = 20 * 15;
  private static final int NAUSEA_AMPLIFIER = 0;

  public MojitoItem(Properties properties) {
    super(properties);
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
    Player player = entity instanceof Player ? (Player) entity : null;
    if (player instanceof ServerPlayer serverPlayer) {
      CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
    }

    if (!level.isClientSide) {
      entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, NAUSEA_DURATION_TICKS, NAUSEA_AMPLIFIER));
    }

    if (player != null) {
      player.awardStat(Stats.ITEM_USED.get(this));
      if (!player.getAbilities().instabuild) {
        stack.shrink(1);
      }
    }

    if (player == null || !player.getAbilities().instabuild) {
      if (stack.isEmpty()) {
        return new ItemStack(Items.GLASS_BOTTLE);
      }

      if (player != null) {
        ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
        if (!player.getInventory().add(bottle)) {
          player.drop(bottle, false);
        }
      }
    }

    entity.gameEvent(GameEvent.DRINK);
    return stack;
  }

  @Override
  public int getUseDuration(ItemStack stack) {
    return DRINK_DURATION_TICKS;
  }

  @Override
  public UseAnim getUseAnimation(ItemStack stack) {
    return UseAnim.DRINK;
  }

  @Override
  public SoundEvent getDrinkingSound() {
    return SoundEvents.GENERIC_DRINK;
  }

  @Override
  public SoundEvent getEatingSound() {
    return SoundEvents.GENERIC_DRINK;
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    return ItemUtils.startUsingInstantly(level, player, hand);
  }
}
