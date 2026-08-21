package com.civilizationmod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Server-side, bounded observation of chest storage inside a warehouse territory. */
public final class BuildingStorageProvider {
    private BuildingStorageProvider() {
    }

    /**
     * Scans only already-loaded normal chest blocks in the declared territory.
     * Double chests are counted once, using their left half as the primary block.
     */
    public static BuildingStorageSnapshot scan(
            ServerLevel level,
            WarehouseTerritory territory,
            long observedAt
    ) {
        if (level == null || territory == null
                || !territory.dimension().equals(level.dimension().identifier().toString())) {
            return BuildingStorageSnapshot.unscanned();
        }

        Map<String, Long> itemCounts = new TreeMap<>();
        int containerCount = 0;
        int unloadedBlockCount = 0;

        for (int x = territory.minX(); x <= territory.maxX(); x++) {
            for (int y = territory.minY(); y <= territory.maxY(); y++) {
                for (int z = territory.minZ(); z <= territory.maxZ(); z++) {
                    BlockPos position = new BlockPos(x, y, z);
                    if (!level.isLoaded(position)) {
                        unloadedBlockCount++;
                        continue;
                    }

                    BlockState state = level.getBlockState(position);
                    Container container = primaryChestContainer(level, position, state);
                    if (container == null) {
                        continue;
                    }

                    containerCount++;
                    collectItems(container, itemCounts);
                }
            }
        }

        List<BuildingStorageItem> items = new ArrayList<>(itemCounts.size());
        for (Map.Entry<String, Long> entry : itemCounts.entrySet()) {
            items.add(new BuildingStorageItem(entry.getKey(), entry.getValue()));
        }

        long totalItemCount = items.stream()
                .mapToLong(BuildingStorageItem::count)
                .sum();
        return new BuildingStorageSnapshot(
                true,
                containerCount,
                unloadedBlockCount,
                totalItemCount,
                Math.max(0L, observedAt),
                items);
    }

    static Container primaryChestContainer(ServerLevel level, BlockPos position, BlockState state) {
        if (state.getBlock() != Blocks.CHEST) {
            return null;
        }

        ChestType chestType = state.getValue(ChestBlock.TYPE);
        if (chestType != ChestType.SINGLE && chestType != ChestType.LEFT) {
            return null;
        }

        return ChestBlock.getContainer(
                (ChestBlock) state.getBlock(),
                state,
                level,
                position,
                false);
    }

    private static void collectItems(Container container, Map<String, Long> itemCounts) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            var itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (itemId != null) {
                itemCounts.merge(itemId.toString(), (long) stack.getCount(), Long::sum);
            }
        }
    }
}

