package com.tsteindl.jakcraft.client;

import com.tsteindl.jakcraft.ModEntities;
import com.tsteindl.jakcraft.JakVelociraptorModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "jakcraft", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

  @SubscribeEvent
  public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    // Link your custom Entity Type to your custom Renderer
    event.registerEntityRenderer(ModEntities.JAK_VELOCIRAPTOR.get(), JakVelociraptorModel::createRenderer);
  }
}
