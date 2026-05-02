package com.tsteindl.jakcraft;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import ru.xishnikus.thedawnera.common.entity.entity.ground.EntityCeratosaurus;

public class EntityJakVelociraptor extends EntityCeratosaurus {

  public EntityJakVelociraptor(EntityType<? extends Animal> type, Level level) {
    super(type, level);
  }

}
