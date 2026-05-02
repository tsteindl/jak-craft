package com.tsteindl.jakcraft;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;

import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(ExampleMod.MODID)
public class ExampleMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "jakcraft";
    // Directly reference a slf4j logger

    public static ArrayList<Item> itemsToRegister = new ArrayList<>();

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> MUELLAGERKING_SPAWN_EGG =
            ITEMS.register("muellagerking_spawn_egg", () ->
                    new ForgeSpawnEggItem(
                            ModEntityTypes.MUELLAGERKING,
                            0x3407872,
                            0x9804699,
                            new Item.Properties()
                    )
            );


    public ExampleMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        ITEMS.register(modEventBus);
        ModEntityTypes.ENTITY_TYPES.register(modEventBus);

        CarlosDieKlinge carlosDieKlinge = new CarlosDieKlinge();
        registerItem(carlosDieKlinge, "carlos_die_klinge");
    }

    private void registerItem(CarlosDieKlinge mySword, String mySword1) {
        mySword.setRegistryName(fromNamespaceAndPath(MODID, mySword1));

        itemsToRegister.add(mySword);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        doItemRegistry(event);
    }

    private static void doItemRegistry(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(itemsToRegister.toArray(new Item[itemsToRegister.size()]));
    }
}
