package com.civilizationmod.mixin;

import com.civilizationmod.CivitasVillagerBackpackHolder;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds a separately persisted Civitas backpack only to vanilla Villagers. */
@Mixin(AbstractVillager.class)
public abstract class CivitasVillagerBackpackMixin implements CivitasVillagerBackpackHolder {
    @Unique
    private final NonNullList<ItemStack> civilizationmod$civitasBackpackItems =
            NonNullList.withSize(CivitasVillagerBackpackHolder.SLOT_COUNT, ItemStack.EMPTY);

    @Override
    public NonNullList<ItemStack> civitasBackpackItems() {
        return this.civilizationmod$civitasBackpackItems;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void civilizationmod$saveBackpack(ValueOutput output, CallbackInfo callbackInfo) {
        if ((Object) this instanceof Villager) {
            ContainerHelper.saveAllItems(
                    output.child("CivitasBackpack"),
                    this.civilizationmod$civitasBackpackItems);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void civilizationmod$loadBackpack(ValueInput input, CallbackInfo callbackInfo) {
        if ((Object) this instanceof Villager) {
            ContainerHelper.loadAllItems(
                    input.childOrEmpty("CivitasBackpack"),
                    this.civilizationmod$civitasBackpackItems);
        }
    }
}
