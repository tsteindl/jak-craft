package com.tsteindl.jakcraft;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;

public class PopeVillagerEntity extends Villager {

  public PopeVillagerEntity(EntityType<? extends Villager> entityType, Level level) {
    super(entityType, level);
  }
}
