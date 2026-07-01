package com.tsteindl.jakcraft;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
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

  public static final RegistryObject<Item> POPE_VILLAGER_SPAWN_EGG = ITEMS.register("pope_villager_spawn_egg",
      () -> new ForgeSpawnEggItem(
          ModEntities.POPE_VILLAGER,
          0xf2f0dc,
          0xd2bc5a,
          new Item.Properties()));

  // Base Carlos die Klinge - diamond-sword damage (7). All variants are drinkable (heal 8 hearts).
  public static final RegistryObject<Item> CARLOS_DIE_KLINGE = ITEMS.register(
          "carlos_die_klinge",
          CarlosDieKlinge::new
  );

  // Test variants with increasing melee damage (displayed attack damage = modifier + 4).
  public static final RegistryObject<Item> CARLOS_DIE_KLINGE_II = ITEMS.register(
          "carlos_die_klinge_ii", () -> new CarlosDieKlinge(11));   // 15 damage
  public static final RegistryObject<Item> CARLOS_DIE_KLINGE_III = ITEMS.register(
          "carlos_die_klinge_iii", () -> new CarlosDieKlinge(26));  // 30 damage
  public static final RegistryObject<Item> CARLOS_DIE_KLINGE_IV = ITEMS.register(
          "carlos_die_klinge_iv", () -> new CarlosDieKlinge(56));   // 60 damage
  public static final RegistryObject<Item> CARLOS_DIE_KLINGE_V = ITEMS.register(
          "carlos_die_klinge_v", () -> new CarlosDieKlinge(116));   // 120 damage

  public static final RegistryObject<Item> DER_GROSSMACHER = ITEMS.register(
          "der_grossmacher",
          DerGrossmacher::new
  );

  public static final RegistryObject<Item> VOLLEYBALL = ITEMS.register(
      "volleyball",
      () -> new VolleyballItem(new Item.Properties().stacksTo(16))
  );

  public static final RegistryObject<Item> VOLLEYBALL_SHOES = ITEMS.register(
      "volleyball_shoes",
      () -> new VolleyballShoesItem(new Item.Properties())
  );

  public static final RegistryObject<Item> MOJITO = ITEMS.register(
      "mojito",
      () -> new MojitoItem(new Item.Properties().stacksTo(16))
  );

  // Scroll-like trophy dropped by the Müllagerking - Jakob finally earns his doctorate.
  public static final RegistryObject<Item> PHD = ITEMS.register(
      "phd",
      () -> new Item(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1))
  );

  // 2. Register the Spawn Egg (Primary color: Green, Secondary color: Red -
  // change as desired)
  public static final RegistryObject<Item> JAK_VELOCIRAPTOR_SPAWN_EGG = ITEMS.register("jak_velociraptor_spawn_egg",
      () -> new ForgeSpawnEggItem(ModEntities.JAK_VELOCIRAPTOR, 0x55aa55, 0xaa2222,
          new Item.Properties().stacksTo(64)));

  public static void register(IEventBus modBus) {
    ITEMS.register(modBus);
  }

}
