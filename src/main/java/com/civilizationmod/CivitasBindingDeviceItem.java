package com.civilizationmod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/** Generic Civitas device that selects any valid building marker and binds a villager. */
public class CivitasBindingDeviceItem extends Item {
    public CivitasBindingDeviceItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable("civilizationmod.item.civitas_binding_device.usage"));
        ResidenceBindingDeviceData.read(stack)
                .ifPresentOrElse(
                        selected -> tooltip.accept(Component.translatable(
                                "civilizationmod.item.civitas_binding_device.selected",
                                selected.markerX(),
                                selected.markerY(),
                                selected.markerZ())),
                        () -> tooltip.accept(Component.translatable(
                                "civilizationmod.item.civitas_binding_device.unselected")));
    }
}
