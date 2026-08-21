package com.civilizationmod.client;

import com.civilizationmod.CivitasVillagerBackpackMenu;
import com.civilizationmod.CivilizationMessages;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;

/** Uses Minecraft 26.2's original three-row container GUI and texture. */
public final class CivitasVillagerBackpackScreen extends ContainerScreen {
    public CivitasVillagerBackpackScreen(
            ChestMenu menu,
            Inventory playerInventory,
            Component title
    ) {
        super(menu, playerInventory, title);
        if (!(menu instanceof CivitasVillagerBackpackMenu)) {
            throw new IllegalArgumentException("Civitas backpack screen received an unexpected menu");
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xff404040);
        graphics.text(
                this.font,
                CivilizationMessages.translatable("civilizationmod.villager.backpack.player_inventory"),
                this.inventoryLabelX,
                this.inventoryLabelY,
                0xff404040);
    }
}
