package com.civilizationmod.client;

/** Client-only render-state contract for the warehouse role overlay. */
public interface WarehouseVillagerRenderStateAccess {
    boolean civilizationmod$hasWarehouseRole();

    void civilizationmod$setWarehouseRole(boolean warehouseRole);
}
