package com.tsteindl.jakcraft;

import com.yellowbrossproductions.illageandspillage.entities.MagispellerEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ExampleMod.MODID);

    public static final RegistryObject<EntityType<MagispellerEntity>> MUELLAGERKING =
            ENTITY_TYPES.register("muellagerking", () ->
                    EntityType.Builder
                            .of(MagispellerEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 2.3F)
                            .build(fromNamespaceAndPath(ExampleMod.MODID, "muellagerking").toString())
            );

    @SubscribeEvent
    public static void onAttribute(EntityAttributeCreationEvent event) {
        event.put(MUELLAGERKING.get(), MagispellerEntity.createAttributes().build());
    }
}