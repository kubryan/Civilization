package com.civilizationmod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Server-side first logistics slice for assigned warehouse residents.
 *
 * <p>Only item IDs already observed in a successful warehouse scan are allowed.
 * The service never overwrites a different item stack and moves at most one
 * villager inventory slot per invocation.</p>
 */
public final class BuildingLogisticsService {
    private BuildingLogisticsService() {
    }

    public static boolean tryDeposit(
            MinecraftServer server,
            CivilizationWorldData data,
            int buildingIndex,
            BuildingObservation building,
            ServerLevel level,
            Villager villager
    ) {
        if (server == null
                || data == null
                || building == null
                || level == null
                || villager == null
                || !villager.isAlive()
                || villager.isTrading()
                                || !BuildingFunction.WAREHOUSE.id().equals(building.functionId())
                || !data.isBuildingOperational(building)) {

            return false;
        }

        BuildingStorageSnapshot snapshot = building.storageSnapshot();
        if (!snapshot.scanned() || snapshot.items().isEmpty()) {
            return false;
        }

        BlockPos marker = new BlockPos(building.markerX(), building.markerY(), building.markerZ());
        if (villager.distanceToSqr(marker.getX() + 0.5D, marker.getY(), marker.getZ() + 0.5D) > 64.0D) {
            return false;
        }

        ItemFrame markerFrame = findMarkerFrame(level, marker);
        if (markerFrame == null) {
            return false;
        }

        WarehouseTerritory territory = WarehouseTerritory.read(markerFrame.getItem()).orElse(null);
        if (territory == null
                || !territory.dimension().equals(level.dimension().identifier().toString())) {
            return false;
        }

        List<Container> containers = findContainers(level, territory);
        if (containers.isEmpty()) {
            return false;
        }

        if (!(villager instanceof CivitasVillagerBackpackHolder)) {
            return false;
        }
        Container inventory = new CivitasVillagerBackpackContainer(villager);
        for (int sourceSlot = 0; sourceSlot < inventory.getContainerSize(); sourceSlot++) {
            ItemStack source = inventory.getItem(sourceSlot);
            if (source.isEmpty() || !isAllowed(snapshot, source)) {
                continue;
            }

            int moved = transferIntoContainers(containers, source);
            if (moved <= 0) {
                continue;
            }

            inventory.setChanged();
            BuildingStorageSnapshot refreshed = BuildingStorageProvider.scan(
                    level,
                    territory,
                    server.getTickCount());
            if (refreshed.scanned()) {
                data.replaceBuilding(building, building.withStorageSnapshot(refreshed));
            }
            return true;
        }
        return false;
    }

    private static boolean isAllowed(BuildingStorageSnapshot snapshot, ItemStack source) {
        var itemId = BuiltInRegistries.ITEM.getKey(source.getItem());
        if (itemId == null) {
            return false;
        }
        String sourceId = itemId.toString();
        return snapshot.items().stream().anyMatch(item -> item.itemId().equals(sourceId));
    }

    private static int transferIntoContainers(List<Container> containers, ItemStack source) {
        int originalCount = source.getCount();
        for (Container container : containers) {
            for (int targetSlot = 0; targetSlot < container.getContainerSize(); targetSlot++) {
                if (!container.canPlaceItem(targetSlot, source)) {
                    continue;
                }

                ItemStack target = container.getItem(targetSlot);
                int targetLimit = Math.min(
                        container.getMaxStackSize(source),
                        source.getItem().getDefaultMaxStackSize());
                int moved;
                if (target.isEmpty()) {
                    moved = Math.min(source.getCount(), targetLimit);
                    if (moved > 0) {
                        container.setItem(targetSlot, source.copyWithCount(moved));
                        source.shrink(moved);
                        container.setChanged();
                    }
                } else if (ItemStack.isSameItemSameComponents(source, target)) {
                    int room = Math.max(0, targetLimit - target.getCount());
                    moved = Math.min(source.getCount(), room);
                    if (moved > 0) {
                        target.grow(moved);
                        container.setItem(targetSlot, target);
                        source.shrink(moved);
                        container.setChanged();
                    }
                }

                if (source.isEmpty()) {
                    return originalCount;
                }
            }
        }
        return originalCount - source.getCount();
    }

    private static ItemFrame findMarkerFrame(ServerLevel level, BlockPos marker) {
        AABB bounds = new AABB(marker).inflate(0.5D);
        return level.getEntities(
                        EntityTypeTest.forClass(ItemFrame.class),
                        bounds,
                        frame -> frame.blockPosition().equals(marker)
                                && BuildingFunction.WAREHOUSE.id().equals(
                                BuildingMarkerRegistry.functionId(frame.getItem())))
                .stream()
                .findFirst()
                .orElse(null);
    }

    private static List<Container> findContainers(ServerLevel level, WarehouseTerritory territory) {
        List<Container> containers = new ArrayList<>();
        for (int x = territory.minX(); x <= territory.maxX(); x++) {
            for (int y = territory.minY(); y <= territory.maxY(); y++) {
                for (int z = territory.minZ(); z <= territory.maxZ(); z++) {
                    BlockPos position = new BlockPos(x, y, z);
                    if (!level.isLoaded(position)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(position);
                    Container container = BuildingStorageProvider.primaryChestContainer(level, position, state);
                    if (container != null) {
                        containers.add(container);
                    }
                }
            }
        }
        return containers;
    }
}

