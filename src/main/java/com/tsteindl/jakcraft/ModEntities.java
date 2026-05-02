package com.tsteindl.jakcraft;

import com.yellowbrossproductions.illageandspillage.entities.MagispellerEntity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;

@Mod.EventBusSubscriber(modid = Jakcraft.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {

  public static void register(IEventBus eventBus) {
    ENTITY_TYPES.register(eventBus);
  }

  public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister
      .create(ForgeRegistries.ENTITY_TYPES, Jakcraft.MODID);

  public static final RegistryObject<EntityType<MagispellerEntity>> MUELLAGERKING = ENTITY_TYPES
      .register("muellagerking", () -> EntityType.Builder
          .of(MagispellerEntity::new, MobCategory.MONSTER)
          .sized(0.6F, 2.3F)
          .build(fromNamespaceAndPath(Jakcraft.MODID, "muellagerking").toString()));

  public static final RegistryObject<EntityType<EntityJakVelociraptor>> JAK_VELOCIRAPTOR = ENTITY_TYPES
      .register("jakvelociraptor", () -> EntityType.Builder
          .of(EntityJakVelociraptor::new, MobCategory.MONSTER)
          .sized(0.6F, 2.3F)
          .build(fromNamespaceAndPath(Jakcraft.MODID, "jakvelociraptor").toString()));

  @SubscribeEvent
  public static void onAttribute(EntityAttributeCreationEvent event) {
    event.put(MUELLAGERKING.get(), MagispellerEntity.createAttributes().build());
    event.put(JAK_VELOCIRAPTOR.get(), EntityJakVelociraptor.createLivingAttributes()
        .add(Attributes.FOLLOW_RANGE, 35.0D)
        .add(Attributes.MOVEMENT_SPEED, 0.25D)
        .add(Attributes.ATTACK_DAMAGE, 6.0D)
        .build());
  }
}
