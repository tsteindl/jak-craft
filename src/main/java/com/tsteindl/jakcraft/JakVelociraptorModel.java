package com.tsteindl.jakcraft;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import ru.astemir.astemirlib.client.DeferredEntityRenderer;
import ru.astemir.astemirlib.client.RenderUtils;
import ru.astemir.astemirlib.client.bedrock.animation.Animation;
import ru.astemir.astemirlib.client.bedrock.animation.data.AnimationBlending;
import ru.astemir.astemirlib.client.bedrock.animation.data.Animator;
import ru.astemir.astemirlib.client.bedrock.model.render.BedrockModelPart;
import ru.astemir.astemirlib.client.bedrock.renderer.EntityRenderData;
import ru.astemir.astemirlib.common.math.AVector3f;
import ru.astemir.astemirlib.common.math.EasingType;
import ru.astemir.astemirlib.common.math.InterpolationType;
import ru.astemir.astemirlib.common.math.MathUtils;
import ru.xishnikus.thedawnera.client.render.entity.TDEArmorLayer;
import ru.xishnikus.thedawnera.client.render.entity.TDEEntityModel;
import ru.xishnikus.thedawnera.client.render.entity.TDELivingRenderer;
import ru.xishnikus.thedawnera.client.render.entity.TDESaddleLayer;
import ru.xishnikus.thedawnera.common.entity.entity.ground.EntityCeratosaurus;

public class JakVelociraptorModel extends TDEEntityModel<EntityCeratosaurus> {
    private static final String ID = "jakvelociraptor";
    private static final ResourceLocation TEXTURE_MALE = textureLocation("ceratosaurus", "male.png");
    private static final ResourceLocation TEXTURE_FEMALE = textureLocation("ceratosaurus", "female.png");
    private static final ResourceLocation TEXTURE_BABY = textureLocation("ceratosaurus", "baby.png");

    public JakVelociraptorModel(ResourceLocation model, ResourceLocation animations) {
        super(model, animations);
    }

    public void animate(Animator animator, EntityCeratosaurus entity, EntityRenderData entityRenderData) {
        Animation animation = animator.getAnimation("animation.model.idle");
        if (this.isMoving(entity, -0.1F, 0.1F, entityRenderData.partialTick)) {
            animation = animator.getAnimation("animation.model.walk");
        }

        if (this.isMoving(entity, -0.75F, 0.75F, entityRenderData.partialTick)) {
            animation = animator.getAnimation("animation.model.run");
        }

        if (entity.isInWater()) {
            animation = animator.getAnimation("animation.model.swim");
        }

        if (entity.actionController.is(entity.actionEat)) {
            animation = animator.getAnimation("animation.model.eat");
        } else if (entity.actionController.is(entity.actionRoar)) {
            animation = animator.getAnimation("animation.model.roar");
            if (entity.getHunger() == 0.0F) {
                entity.clientNeckRot.setTo((double) MathUtils.sin(entityRenderData.ageInTicks / 2.0F) * 300.0);
            } else {
                entity.clientNeckRot.setTo((double)MathUtils.sin(entityRenderData.ageInTicks / 8.0F) * 90.0);
            }
        } else {
            entity.clientNeckRot.setTo(0.0);
        }

        if (entity.actionController.is(entity.actionAttack)) {
            animation = animation.merge(animator.getAnimation("animation.model.attack"));
        }

        if (entity.actionController.is(entity.actionDown)) {
            animation = animator.getAnimation("animation.model.down");
        }

        if (entity.stateController.is(entity.actionRest)) {
            animation = animator.getAnimation("animation.model.rest");
        }

        if (entity.actionController.is(entity.actionStartSleeping)) {
            animation = animator.getAnimation("animation.model.fall_asleep");
        }

        if (entity.stateController.is(entity.actionSleep)) {
            animation = animator.getAnimation("animation.model.sleep");
        }

        if (entity.actionController.is(entity.actionWakeUp)) {
            animation = animator.getAnimation("animation.model.wake_up");
        }

        if (entity.actionController.is(entity.actionUp)) {
            animation = animator.getAnimation("animation.model.up");
        }

        float bodyRotDeviation = ((Double)entity.bodyRotDeviation.get(entityRenderData.partialTick)).floatValue();
        float neckYaw = bodyRotDeviation * 2.0F + entityRenderData.entityYaw + ((Double)entity.clientNeckRot.get(entityRenderData.partialTick)).floatValue();
        animator.getTransform("neck").setLookAt(this.clamp(neckYaw, -60.0F, 60.0F), this.clamp(entityRenderData.entityPitch, -20.0F, 20.0F));
        animator.getTransform("tail1").setLookAt(-bodyRotDeviation * 2.0F, 0.0F);
        animator.getTransform("tail2").setLookAt(-bodyRotDeviation, 0.0F);
        if (!entity.isBaby()) {
            BedrockModelPart reins = this.getPart("reins");
            if (entity.getControllingPassenger() != null) {
                reins.visible = true;
                float xRot = MathUtils.clamp(entity.getControllingPassenger().getXRot() - 20.0F, -5.0F, 10.0F);
                animator.getTransform("reins").setLookAt(0.0F, xRot / 2.0F);
                animator.getTransform("reins1").setCustomRotation(0.0F, 0.0F, xRot * 2.0F);
                animator.getTransform("reins2").setCustomRotation(0.0F, 0.0F, -xRot * 2.0F);
            } else {
                reins.visible = false;
            }
        }

        animator.setAnimation(animation, AnimationBlending.create(InterpolationType.CATMULLROM, EasingType.NONE, 0.15000000596046448), 1.0, 1);
        if (entity.actionController.is(entity.actionRoar) && !entity.isBaby() && entity.actionController.getTicks() > 5 && entity.actionController.getTicks() < 40) {
            this.renderRoarParticles(entity, this, entityRenderData, "head", 0.5F, 0.5F, 2);
        }

        if (entity.actionController.is(entity.actionEat) && entity.actionController.getTicks() > 30 && entity.actionController.getTicks() < 50) {
            this.renderFoodParticles(entity, this, entityRenderData, new AVector3f(0.0F, -0.3F, -0.2F), new AVector3f(0.4F, 0.5F, 0.4F), 5, "head");
        }

    }

    public void renderPassenger(EntityCeratosaurus entity, LivingEntity passenger) {
        PoseStack poseStack = RenderUtils.fromPose(this.getPart("body").getLastPose());
        DeferredEntityRenderer.renderDeferred(passenger.getUUID(), poseStack, new AVector3f(0.075F, 0.2F, 0.0F), 0.0F);
    }

    public ResourceLocation getTexture(EntityCeratosaurus entity) {
        if (entity.isBaby()) {
            return TEXTURE_BABY;
        } else {
            switch (entity.getGender()) {
                case MALE -> {
                    return TEXTURE_MALE;
                }
                case FEMALE -> {
                    return TEXTURE_FEMALE;
                }
                default -> {
                    return TEXTURE_MALE;
                }
            }
        }
    }

    public static TDELivingRenderer<EntityCeratosaurus> createRenderer(EntityRendererProvider.Context context) {
        ru.xishnikus.thedawnera.client.render.entity.mobs.CeratosaurusModel model = new ru.xishnikus.thedawnera.client.render.entity.mobs.CeratosaurusModel(modelLocation("ceratosaurus", "geo.json"), animationsLocation("ceratosaurus", "animation.json"));
        model.addLayer(new TDEArmorLayer(model, textureLocation("ceratosaurus", "armor_0.png")));
        model.addLayer(new TDESaddleLayer(model, textureLocation("ceratosaurus", "saddle_0.png")));
        ru.xishnikus.thedawnera.client.render.entity.mobs.CeratosaurusModel babyModel = new ru.xishnikus.thedawnera.client.render.entity.mobs.CeratosaurusModel(modelLocation("ceratosaurus", "geo_baby.json"), animationsLocation("ceratosaurus", "animation_baby.json"));
        return TDELivingRenderer.createRendererGendered(context, (mob) -> {
            return model;
        }, (mob) -> {
            return babyModel;
        }, 1.0F, 1.0F);
    }
}