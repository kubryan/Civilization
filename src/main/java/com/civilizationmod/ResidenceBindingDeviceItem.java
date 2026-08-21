package com.civilizationmod;

import net.minecraft.world.item.Item;

/** Backward-compatible item class for the legacy residence binding device ID. */
@Deprecated
public final class ResidenceBindingDeviceItem extends CivitasBindingDeviceItem {
    public ResidenceBindingDeviceItem(Item.Properties properties) {
        super(properties);
    }
}
