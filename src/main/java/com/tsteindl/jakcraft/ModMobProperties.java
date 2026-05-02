package com.tsteindl.jakcraft;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import ru.xishnikus.thedawnera.common.entity.entity.base.BaseRideableAnimal;
import ru.xishnikus.thedawnera.common.entity.properties.MobProperties;

import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;

public class ModMobProperties {

  public static final DeferredRegister<MobProperties> MOB_PROPERTIES = DeferredRegister.create(
      fromNamespaceAndPath("dawnera", "mob_properties"), Jakcraft.MODID);

  public static final RegistryObject<MobProperties> JAK_VELOCIRAPTOR = MOB_PROPERTIES.register("jakvelociraptor",
      () -> MobProperties.build(BaseRideableAnimal.Properties.class, fromNamespaceAndPath("dawnera", "mobs/ceratosaurus.json")).get());

  public static void register(IEventBus eventBus) {
    MOB_PROPERTIES.register(eventBus);
  }
}
