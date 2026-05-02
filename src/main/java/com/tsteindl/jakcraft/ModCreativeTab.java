package com.tsteindl.jakcraft;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTab {
  public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
      .create(Registries.CREATIVE_MODE_TAB, "jakcraft");

  public static final RegistryObject<CreativeModeTab> JAKCRAFT_TAB = CREATIVE_MODE_TABS.register("jakcraft_tab",
      () -> CreativeModeTab.builder()
          .icon(() -> new ItemStack(ModItems.JAK_VELOCIRAPTOR_SPAWN_EGG.get()))
          .title(Component.translatable("creativetab.jakcraft_tab"))
          .displayItems((parameters, output) -> {
            output.accept(ModItems.JAK_VELOCIRAPTOR_SPAWN_EGG.get());
            output.accept(ModItems.MUELLAGERKING_SPAWN_EGG.get());
            output.accept(ModItems.CARLOS_DIE_KLINGE.get());
            output.accept(ModItems.DER_GROSSMACHER.get());
          })
          .build());

  public static void register(IEventBus eventBus) {
    CREATIVE_MODE_TABS.register(eventBus);
  }
}
