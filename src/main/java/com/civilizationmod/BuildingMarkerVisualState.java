package com.civilizationmod;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;

/** Synchronizes the visual state of a marker ItemFrame after server validation. */
public final class BuildingMarkerVisualState {
    private BuildingMarkerVisualState() {
    }

    public static void apply(ItemFrame frame, boolean valid) {
        if (frame == null) {
            return;
        }

        ItemStack current = frame.getItem();
        if (current == null || current.isEmpty()) {
            return;
        }

        ItemStack updated = current.copy();
        if (valid) {
            updated.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        } else {
            updated.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        }

        if (!ItemStack.isSameItemSameComponents(current, updated)) {
            frame.setItem(updated, false);
        }
    }
}

