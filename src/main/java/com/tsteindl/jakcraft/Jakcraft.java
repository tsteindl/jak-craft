package com.tsteindl.jakcraft;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("jakcraft")
public class Jakcraft {

  public static final String MODID = "jakcraft";

  public Jakcraft(FMLJavaModLoadingContext context) {
    var modBus = context.getModEventBus();

    ModEntities.register(modBus);
    ModItems.register(modBus);
    ModCreativeTab.register(modBus);
    MinecraftForge.EVENT_BUS.register(this);
  }
}
