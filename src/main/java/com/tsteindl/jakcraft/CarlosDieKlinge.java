package com.tsteindl.jakcraft;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

// A sword that is also drinkable: right-click to drink (heals 8 hearts) without ever
// being consumed. The melee damage is configurable so several test variants can exist.
public class CarlosDieKlinge extends SwordItem {

    private static final int DRINK_DURATION_TICKS = 32;
    private static final float HEAL_AMOUNT = 16.0F; // 8 hearts

    public CarlosDieKlinge() {
        this(3); // default: diamond-sword-level damage (7 attack damage)
    }

    // attackDamageModifier is added to the Diamond tier bonus (3) and the player's base (1),
    // so the displayed attack damage is (attackDamageModifier + 4).
    public CarlosDieKlinge(int attackDamageModifier) {
        super(Tiers.DIAMOND, attackDamageModifier, 0.0F, new Item.Properties());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            entity.heal(HEAL_AMOUNT);
        }
        // Also fill hunger and saturation.
        if (entity instanceof Player player) {
            player.getFoodData().setFoodLevel(20);
            player.getFoodData().setSaturation(20.0F);
        }
        entity.gameEvent(GameEvent.DRINK);
        // Never consumed - return the stack unchanged so it does not shrink.
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
}
