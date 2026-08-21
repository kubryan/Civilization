package com.civilizationmod;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

/** Common-side access to the persistent 27-slot Civitas villager backpack. */
public interface CivitasVillagerBackpackHolder {
    int SLOT_COUNT = 27;

    NonNullList<ItemStack> civitasBackpackItems();
}
