package com.tsteindl.jakcraft;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;

// White "sneaker" boots that, worn alone, are absurdly tanky: maxed armor (30) and toughness (20),
// plus full knockback resistance so the wearer barely flinches.
public class VolleyballShoesItem extends ArmorItem {

  private static final String ARMOR_TEXTURE = Jakcraft.MODID + ":textures/models/armor/volleyball_shoes_layer_1.png";
  private static final double ARMOR = 30.0D;              // Attributes.ARMOR is capped at 30
  private static final double TOUGHNESS = 20.0D;          // Attributes.ARMOR_TOUGHNESS is capped at 20
  private static final double KNOCKBACK_RESIST = 1.0D;    // 1.0 = immune to knockback
  private static final UUID ARMOR_UUID = UUID.fromString("6b9d0e2a-7c41-4f7e-9b3a-1d2c3e4f5a6b");
  private static final UUID TOUGHNESS_UUID = UUID.fromString("2f8c1a5d-4e63-4a90-8c17-9a0b1c2d3e4f");
  private static final UUID KNOCKBACK_UUID = UUID.fromString("8e1a2b3c-4d5e-6f70-8192-a3b4c5d6e7f8");

  private final Multimap<Attribute, AttributeModifier> bootsModifiers;

  public VolleyballShoesItem(Properties properties) {
    super(ArmorMaterials.DIAMOND, Type.BOOTS, properties);
    this.bootsModifiers = ImmutableMultimap.<Attribute, AttributeModifier>builder()
        .put(Attributes.ARMOR, new AttributeModifier(
            ARMOR_UUID, "Volleyball shoe armor", ARMOR, AttributeModifier.Operation.ADDITION))
        .put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(
            TOUGHNESS_UUID, "Volleyball shoe toughness", TOUGHNESS, AttributeModifier.Operation.ADDITION))
        .put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(
            KNOCKBACK_UUID, "Volleyball shoe stability", KNOCKBACK_RESIST, AttributeModifier.Operation.ADDITION))
        .build();
  }

  @Override
  public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
    return slot == EquipmentSlot.FEET ? this.bootsModifiers : super.getDefaultAttributeModifiers(slot);
  }

  @Override
  public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, @Nullable String type) {
    return ARMOR_TEXTURE;
  }
}
