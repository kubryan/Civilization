package com.civilizationmod.client.mixin;

import com.civilizationmod.BuildingRoleEquipment;
import com.civilizationmod.client.WarehouseVillagerRenderStateAccess;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(VillagerRenderState.class)
public class VillagerRenderStateMixin implements WarehouseVillagerRenderStateAccess {
    @Unique
    private boolean civilizationmod$warehouseRole;

    @Override
    public boolean civilizationmod$hasWarehouseRole() {
        return civilizationmod$warehouseRole;
    }

    @Override
    public void civilizationmod$setWarehouseRole(boolean warehouseRole) {
        this.civilizationmod$warehouseRole = warehouseRole;
    }
}
