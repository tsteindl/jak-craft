package com.tsteindl.jakcraft;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

public class CarlosDieKlinge extends SwordItem {
    public CarlosDieKlinge() {
        super(Tiers.DIAMOND, 0, 0F, new Item.Properties().tab(CreativeModeTab.TAB_COMBAT));
    }

}
