package com.tsteindl.jakcraft;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;

public class VolleyballShoesItem extends ArmorItem {

  private static final String ARMOR_TEXTURE = Jakcraft.MODID + ":textures/models/armor/volleyball_shoes_layer_1.png";

  public VolleyballShoesItem(Properties properties) {
    super(ArmorMaterials.DIAMOND, Type.BOOTS, properties);
  }

  @Override
  public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, @Nullable String type) {
    return ARMOR_TEXTURE;
  }
}
