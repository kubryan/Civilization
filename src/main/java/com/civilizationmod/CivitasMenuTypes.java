package com.civilizationmod;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.core.Registry;

/** Common-side menu type registry for Civitas screens. */
public final class CivitasMenuTypes {
    public static final MenuType<CivitasVillagerBackpackMenu> VILLAGER_BACKPACK =
            new MenuType<>(CivitasVillagerBackpackMenu::new, FeatureFlags.DEFAULT_FLAGS);

    private CivitasMenuTypes() {
    }

    public static void initialize() {
        Registry.register(
                BuiltInRegistries.MENU,
                CivilizationMod.id("villager_backpack"),
                VILLAGER_BACKPACK);
    }
}

