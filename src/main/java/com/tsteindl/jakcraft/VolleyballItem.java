package com.tsteindl.jakcraft;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class VolleyballItem extends Item {

  public VolleyballItem(Properties properties) {
    super(properties);
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    Level level = context.getLevel();
    BlockPos placePos = context.getClickedPos().relative(context.getClickedFace());
    VolleyballEntity volleyball = new VolleyballEntity(ModEntities.VOLLEYBALL.get(), level);

    volleyball.setPos(placePos.getX() + 0.5D, placePos.getY() + 0.05D, placePos.getZ() + 0.5D);
    if (!level.noCollision(volleyball)) {
      return InteractionResult.FAIL;
    }

    if (!level.isClientSide) {
      level.addFreshEntity(volleyball);
      Player player = context.getPlayer();
      ItemStack stack = context.getItemInHand();
      if (player == null || !player.getAbilities().instabuild) {
        stack.shrink(1);
      }
    }

    return InteractionResult.sidedSuccess(level.isClientSide);
  }
}
