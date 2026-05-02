package com.tsteindl.jakcraft;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import ru.astemir.astemirlib.client.SmoothValue;
import ru.astemir.astemirlib.common.action.ActionState;
import ru.astemir.astemirlib.common.entity.EntityUtils;
import ru.astemir.astemirlib.common.math.InterpolationType;
import ru.xishnikus.thedawnera.common.entity.ai.controller.GiantDinoBodyControl;
import ru.xishnikus.thedawnera.common.entity.entity.base.BaseRideableAnimal;
import ru.xishnikus.thedawnera.common.entity.input.InputKey;
import ru.xishnikus.thedawnera.common.entity.input.KeyInputMob;
import ru.xishnikus.thedawnera.common.entity.misc.MobStepHandler;
import ru.xishnikus.thedawnera.common.entity.misc.RandomTickAction;
import ru.xishnikus.thedawnera.common.misc.TDESoundEvents;
import ru.xishnikus.thedawnera.common.utils.TDEUtils;

public class EntityJakVelociraptor extends BaseRideableAnimal<BaseRideableAnimal.Properties> implements KeyInputMob {
  private MobStepHandler stepHandler = (new MobStepHandler())
      .addListener(0.7F, 0.8F,
          (livingEntity, stepTick) -> (stepTick == 6 || stepTick == 14) && !this.isSleepingOrResting())
      .addListener(0.1F, 1.92F,
          (livingEntity, stepTick) -> (stepTick == 11 || stepTick == 30) && !this.isSleepingOrResting());
  public SmoothValue<Double> clientNeckRot;
  private RandomTickAction randomRoar;
  public ActionState actionAttack;
  public ActionState actionRoar;
  public ActionState actionEat;

  public EntityJakVelociraptor(EntityType<? extends Animal> type, Level level) {
    super(type, level);
    this.clientNeckRot = SmoothValue.valDouble(InterpolationType.LINEAR, (double) 0.0F);
    this.randomRoar = new RandomTickAction("roar", 200, 10.0F,
        (mob) -> this.getTarget() == null && !this.isSleepingOrResting() && !this.isInWater());
    this.actionAttack = this.actionController.getActionByName("attack");
    this.actionRoar = this.actionController.getActionByName("roar");
    this.actionEat = this.actionController.getActionByName("eat");
  }

  public void tick() {
    super.tick();
    if (!this.isSleepingOrResting() && !this.hasControllingPassenger()) {
      this.randomRoar.tick(this);
    }

    this.clientNeckRot.update(0.05F);
    this.stepHandler.tick(this);
  }

  public void onActionBegin(ActionState state) {
    if (state == this.actionEat) {
      this.playSound((SoundEvent) TDESoundEvents.EAT_MEAT.get(), 1.0F, this.getVoicePitch() * 0.8F);
    }

    if (state == this.actionAttack) {
      this.playSound((SoundEvent) TDESoundEvents.CERATOSAURUS_ATTACK.get(), 1.0F, this.getVoicePitch());
    }

  }

  public void onActionTick(ActionState state, int ticks) {
    if (this.actionController.isActionAt(this.actionDown, 15)) {
      TDEUtils.shakeScreen(this, 14, 20, (double) 2.0F);
    }

    if (this.actionController.isActionAt(this.actionAttack, 10)) {
      TDEUtils.attackFrontEntities(this, 6.0F);
      if (this.getLastAttackTarget() != null && this.isWithinMeleeAttackRange(this.getLastAttackTarget())) {
        EntityUtils.damageEntity(this, this.getLastAttackTarget(), 1.0F);
      }
    }

    if (this.actionController.isActionAt(this.actionRoar, 10)) {
      this.wakeUpOthers(14);
      if (this.hasControllingPassenger()) {
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 2));
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 1));
      }

      if (this.getHunger() == 0.0F) {
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 2));
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 1));
        TDEUtils.shakeScreen(this, 14, 300, (double) 7.0F);
        this.playSound((SoundEvent) TDESoundEvents.CERATOSAURUS_ROAR_STRONG.get(), 3.0F, this.getVoicePitch());
      } else {
        TDEUtils.shakeScreen(this, 14, 300, (double) 5.0F);
        this.playSound((SoundEvent) TDESoundEvents.CERATOSAURUS_ROAR.get(), 3.0F, this.getVoicePitch());
      }
    }

  }

  public void travel(Vec3 motion) {
    if (this.actionController.is(new ActionState[] { this.actionRoar, this.actionEat })) {
      motion = motion.multiply((double) 0.0F, (double) 1.0F, (double) 0.0F);
    }

    super.travel(motion);
  }

  public void onInputHandle(InputKey inputKey) {
    if (this.tickControlled >= 20 && this.actionController.isNoAction() && !this.isSleepingOrResting()) {
      if (inputKey.is("attack") && this.consumeEnergy(20.0F)) {
        this.actionController.playAction(this.actionAttack);
      }

      if (inputKey.is("roar") && this.consumeEnergy(50.0F)) {
        this.actionController.playAction(this.actionRoar);
      }
    }

  }

  public void onServerMobStep(float stepPower) {
    if (!this.isBaby()) {
      MobStepHandler.playStepSound(this, (SoundEvent) TDESoundEvents.CERATOSAURUS_STEP.get());
      TDEUtils.shakeScreen(this, 5, 20, (double) 2.0F);
    }

  }

  public float getPassengerEyeScale() {
    if (this.isInWater()) {
      return 0.4F;
    } else if (this.stateController.is(this.actionDown)) {
      float p = (float) this.actionController.getTicks() / (float) this.actionDown.getLength();
      return Math.max(0.5F, 1.0F * p);
    } else if (this.actionController.is(this.actionUp)) {
      float p = (float) this.actionController.getTicks() / (float) this.actionUp.getLength();
      return Math.max(0.5F, 1.0F * (1.0F - p));
    } else {
      return !this.stateController.is(this.actionRest) && !this.stateController.is(this.actionSleep) ? 1.0F : 0.5F;
    }
  }

  protected @Nullable SoundEvent getAmbientSound() {
    return this.isSleeping() ? (SoundEvent) TDESoundEvents.CERATOSAURUS_SLEEP.get()
        : (SoundEvent) TDESoundEvents.CERATOSAURUS_IDLE.get();
  }

  public int getAmbientSoundInterval() {
    return 200;
  }

  protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
    return (SoundEvent) TDESoundEvents.CERATOSAURUS_HURT.get();
  }

  protected @Nullable SoundEvent getDeathSound() {
    return (SoundEvent) TDESoundEvents.CERATOSAURUS_DEATH.get();
  }

  public int getMaxHeadYRot() {
    return 60;
  }

  public int getHeadRotSpeed() {
    return 5;
  }

  protected BodyRotationControl createBodyControl() {
    return new GiantDinoBodyControl(this);
  }
}
