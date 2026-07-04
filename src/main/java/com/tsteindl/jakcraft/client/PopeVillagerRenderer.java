package com.tsteindl.jakcraft.client;

import com.tsteindl.jakcraft.Jakcraft;
import com.tsteindl.jakcraft.PopeVillagerEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;

// The Papst and Helfer use standard 64x64 player-skin textures, so they must be drawn with a
// player model (their skins are NOT villager-UV textures - drawing them on a VillagerModel is what
// looked bugged). The texture is chosen by role, so the two NPCs can have different skins.
public class PopeVillagerRenderer extends MobRenderer<PopeVillagerEntity, PlayerModel<PopeVillagerEntity>> {

  private static final ResourceLocation POPE_TEXTURE =
      fromNamespaceAndPath(Jakcraft.MODID, "textures/entity/pope_villager.png");
  private static final ResourceLocation HELPER_TEXTURE =
      fromNamespaceAndPath(Jakcraft.MODID, "textures/entity/helper_villager.png");

  public PopeVillagerRenderer(EntityRendererProvider.Context context) {
    // false = classic (4px) arms, matching a standard player skin layout.
    super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
  }

  @Override
  public ResourceLocation getTextureLocation(PopeVillagerEntity entity) {
    return entity.getRole() == PopeVillagerEntity.Role.HELPER ? HELPER_TEXTURE : POPE_TEXTURE;
  }
}
