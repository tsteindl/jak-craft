package com.tsteindl.jakcraft;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

public class DerGrossmacher extends SwordItem {
    public DerGrossmacher() {
        super(Tiers.DIAMOND, 1000000, 10F, new Item.Properties());
    }
}
