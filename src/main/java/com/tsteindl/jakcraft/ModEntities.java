package com.tsteindl.jakcraft;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
  // Replace "jakcraft" with your actual Mod ID
  public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES,
      "jakcraft");

  public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "jakcraft");

  // 1. Register the Entity Type
  public static final RegistryObject<EntityType<EntityJakVelociraptor>> JAK_VELOCIRAPTOR = ENTITIES
      .register("jak_velociraptor", () -> EntityType.Builder.of(EntityJakVelociraptor::new, MobCategory.CREATURE)
          .sized(1.0f, 0.8f) // Width and Height (adjust these to fit a raptor!)
          .build("jak_velociraptor"));

  // 2. Register the Spawn Egg (Primary color: Green, Secondary color: Red -
  // change as desired)
  public static final RegistryObject<Item> JAK_VELOCIRAPTOR_SPAWN_EGG = ITEMS.register("jak_velociraptor_spawn_egg",
      () -> new ForgeSpawnEggItem(JAK_VELOCIRAPTOR, 0x55aa55, 0xaa2222, new Item.Properties().stacksTo(64)));

  @SubscribeEvent
  public static void onAttributeCreate(EntityAttributeCreationEvent event) {
    event.put(
        ModEntities.JAK_VELOCIRAPTOR.get(),
        Mob.createMobAttributes() // <-- this includes FOLLOW_RANGE by default
            .add(Attributes.MAX_HEALTH, 40.0)
            .add(Attributes.MOVEMENT_SPEED, 0.25)
            .add(Attributes.ATTACK_DAMAGE, 8.0)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.5)
            .build());
  }

  public static void register(IEventBus eventBus) {
    ENTITIES.register(eventBus);
    ITEMS.register(eventBus);
  }
}
