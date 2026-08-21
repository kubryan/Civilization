package com.civilizationmod.client;

import com.civilizationmod.CivitasMenuTypes;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public class CivilizationModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(CivitasMenuTypes.VILLAGER_BACKPACK, CivitasVillagerBackpackScreen::new);
    }
}
