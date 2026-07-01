package com.tsteindl.jakcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tsteindl.jakcraft.Jakcraft;
import com.tsteindl.jakcraft.PopeVillagerEntity;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;

public class PopeVillagerRenderer extends MobRenderer<PopeVillagerEntity, VillagerModel<PopeVillagerEntity>> {

  private static final ResourceLocation TEXTURE = fromNamespaceAndPath(
      Jakcraft.MODID,
      "textures/entity/pope_villager.png");

  public PopeVillagerRenderer(EntityRendererProvider.Context context) {
    super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
  }

  @Override
  public ResourceLocation getTextureLocation(PopeVillagerEntity entity) {
    return TEXTURE;
  }

  @Override
  protected void scale(PopeVillagerEntity entity, PoseStack poseStack, float partialTickTime) {
    float scale = 0.9375F;
    if (entity.isBaby()) {
      scale *= 0.5F;
      shadowRadius = 0.25F;
    } else {
      shadowRadius = 0.5F;
    }

    poseStack.scale(scale, scale, scale);
  }
}
