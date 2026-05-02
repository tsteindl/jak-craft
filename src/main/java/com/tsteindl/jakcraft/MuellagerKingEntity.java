package com.tsteindl.jakcraft;

import com.yellowbrossproductions.illageandspillage.entities.MagispellerEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class MuellagerKingEntity extends MagispellerEntity {

    public MuellagerKingEntity(EntityType<? extends MagispellerEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
    }
}