package com.civilizationmod;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** 27-slot persistent Civitas backpack owned by an assigned vanilla Villager. */
public final class CivitasVillagerBackpackContainer implements Container {
    public static final int SLOT_COUNT = CivitasVillagerBackpackHolder.SLOT_COUNT;

    private final Villager villager;
    private final NonNullList<ItemStack> items;

    public CivitasVillagerBackpackContainer(Villager villager) {
        this.villager = villager;
        if (!(villager instanceof CivitasVillagerBackpackHolder holder)) {
            throw new IllegalStateException("Villager is missing the Civitas backpack holder mixin");
        }
        this.items = holder.civitasBackpackItems();
    }

    public Villager villager() {
        return this.villager;
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return isValidSlot(slot) ? this.items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return isValidSlot(slot) ? ContainerHelperBridge.removeItem(this.items, slot, amount) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return isValidSlot(slot) ? this.items.set(slot, ItemStack.EMPTY) : ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (isValidSlot(slot)) {
            this.items.set(slot, stack);
            setChanged();
        }
    }

    @Override
    public void setChanged() {
        // Entity save hooks persist this holder list; no vanilla inventory mutation is needed.
    }

    @Override
    public boolean stillValid(Player player) {
        return this.villager.isAlive()
                && this.villager.level() == player.level()
                && player.isWithinEntityInteractionRange(this.villager, 4.0D);
    }

    @Override
    public void clearContent() {
        this.items.replaceAll(ignored -> ItemStack.EMPTY);
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return isValidSlot(slot);
    }

    private static boolean isValidSlot(int slot) {
        return slot >= 0 && slot < SLOT_COUNT;
    }

    /** Keeps Container mutation paths explicit without exposing the holder list. */
    private static final class ContainerHelperBridge {
        private ContainerHelperBridge() {
        }

        private static ItemStack removeItem(NonNullList<ItemStack> items, int slot, int amount) {
            ItemStack current = items.get(slot);
            if (current.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack removed = current.split(amount);
            if (current.isEmpty()) {
                items.set(slot, ItemStack.EMPTY);
            }
            return removed;
        }
    }
}
