package com.tsteindl.jakcraft;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class VolleyballEntity extends Entity implements ItemSupplier {

  private static final double GRAVITY = 0.04D;
  private static final double BOUNCE = 0.9D;
  private static final double FRICTION = 0.99D;
  private static final double GROUND_FRICTION = 0.7D;

  public VolleyballEntity(EntityType<? extends VolleyballEntity> entityType, Level level) {
    super(entityType, level);
  }

  @Override
  protected void defineSynchedData() {
  }

  @Override
  public void tick() {
    super.tick();

    Vec3 motion = getDeltaMovement();
    if (!isNoGravity()) {
      motion = motion.add(0.0D, -GRAVITY, 0.0D);
    }

    move(MoverType.SELF, motion);

    if (verticalCollision) {
      double bouncedY = Math.abs(motion.y) > 0.02D ? -motion.y * BOUNCE : 0.0D;
      motion = new Vec3(motion.x * GROUND_FRICTION, bouncedY, motion.z * GROUND_FRICTION);
    }

    if (horizontalCollision) {
      motion = new Vec3(-motion.x * BOUNCE, motion.y, -motion.z * BOUNCE);
    }

    motion = motion.multiply(FRICTION, FRICTION, FRICTION);
    if (onGround()) {
      motion = motion.multiply(GROUND_FRICTION, 1.0D, GROUND_FRICTION);
    }

    if (motion.lengthSqr() < 0.00001D) {
      motion = Vec3.ZERO;
    }

    setDeltaMovement(motion);
  }

  @Override
  public boolean hurt(DamageSource source, float amount) {
    if (isInvulnerableTo(source)) {
      return false;
    }

    Entity attacker = source.getEntity();
    if (attacker != null) {
      Vec3 look = attacker.getLookAngle().normalize();
      Vec3 horizontalHit = new Vec3(look.x, 0.0D, look.z).normalize().scale(0.45D);
      Vec3 hit = horizontalHit.add(0.0D, 0.9D, 0.0D);
      setDeltaMovement(hit);
      hasImpulse = true;
      markHurt();
      return true;
    }

    return super.hurt(source, amount);
  }

  @Override
  public boolean isPickable() {
    return true;
  }

  @Override
  public boolean isPushable() {
    return true;
  }

  @Override
  public ItemStack getItem() {
    return new ItemStack(ModItems.VOLLEYBALL.get());
  }

  @Override
  protected void readAdditionalSaveData(CompoundTag tag) {
  }

  @Override
  protected void addAdditionalSaveData(CompoundTag tag) {
  }
}
