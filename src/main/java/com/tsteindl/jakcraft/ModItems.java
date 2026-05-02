package com.tsteindl.jakcraft;

import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

  public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "jakcraft");

  public static final RegistryObject<Item> MUELLAGERKING_SPAWN_EGG = ITEMS.register("muellagerking_spawn_egg",
      () -> new ForgeSpawnEggItem(
          ModEntities.MUELLAGERKING,
          0x3407872,
          0x9804699,
          new Item.Properties()));

  // 2. Register the Spawn Egg (Primary color: Green, Secondary color: Red -
  // change as desired)
  public static final RegistryObject<Item> JAK_VELOCIRAPTOR_SPAWN_EGG = ITEMS.register("jak_velociraptor_spawn_egg",
      () -> new ForgeSpawnEggItem(ModEntities.JAK_VELOCIRAPTOR, 0x55aa55, 0xaa2222,
          new Item.Properties().stacksTo(64)));

  public static void register(IEventBus modBus) {
    ITEMS.register(modBus);
  }

}
