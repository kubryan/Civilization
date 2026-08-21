package com.civilizationmod;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.DyedItemColor;

/** Common/server role-equipment contract shared by server assignment and client rendering. */
public final class BuildingRoleEquipment {
    public static final String ROLE_DATA_KEY = "civitas_role";
    public static final String WAREHOUSE_ROLE = BuildingFunction.WAREHOUSE.id();

    private BuildingRoleEquipment() {
    }

    public static void apply(Villager villager, String functionId) {
        DyeColor color = WAREHOUSE_ROLE.equals(functionId) ? DyeColor.YELLOW : DyeColor.GREEN;
        ItemStack roleCoat = new ItemStack(Items.LEATHER_CHESTPLATE);
        roleCoat.set(DataComponents.DYED_COLOR, new DyedItemColor(color.getTextureDiffuseColor()));

        CompoundTag roleData = new CompoundTag();
        roleData.putString(ROLE_DATA_KEY, functionId == null ? "" : functionId);
        roleCoat.set(DataComponents.CUSTOM_DATA, CustomData.of(roleData));
        villager.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, roleCoat);
    }

    public static boolean isWarehouseRole(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return WAREHOUSE_ROLE.equals(data.getStringOr(ROLE_DATA_KEY, ""));
    }

    public static boolean isCivitasRole(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        String role = data.getStringOr(ROLE_DATA_KEY, "");
        return !role.isBlank();
    }
}
