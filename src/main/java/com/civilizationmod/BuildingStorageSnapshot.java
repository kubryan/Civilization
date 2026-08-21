package com.civilizationmod;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/** Persisted summary of the containers observed inside one warehouse territory. */
public record BuildingStorageSnapshot(
        boolean scanned,
        int containerCount,
        int unloadedBlockCount,
        long totalItemCount,
        long lastScannedAt,
        List<BuildingStorageItem> items
) {
    public static final Codec<BuildingStorageSnapshot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("scanned", false).forGetter(BuildingStorageSnapshot::scanned),
            Codec.INT.optionalFieldOf("container_count", 0).forGetter(BuildingStorageSnapshot::containerCount),
            Codec.INT.optionalFieldOf("unloaded_block_count", 0).forGetter(BuildingStorageSnapshot::unloadedBlockCount),
            Codec.LONG.optionalFieldOf("total_item_count", 0L).forGetter(BuildingStorageSnapshot::totalItemCount),
            Codec.LONG.optionalFieldOf("last_scanned_at", 0L).forGetter(BuildingStorageSnapshot::lastScannedAt),
            BuildingStorageItem.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(BuildingStorageSnapshot::items)
    ).apply(instance, BuildingStorageSnapshot::new));

    public BuildingStorageSnapshot {
        containerCount = Math.max(0, containerCount);
        unloadedBlockCount = Math.max(0, unloadedBlockCount);
        totalItemCount = Math.max(0L, totalItemCount);
        lastScannedAt = Math.max(0L, lastScannedAt);
        items = items == null ? List.of() : List.copyOf(items);
    }

    public static BuildingStorageSnapshot unscanned() {
        return new BuildingStorageSnapshot(false, 0, 0, 0L, 0L, List.of());
    }

    public int itemTypeCount() {
        return items.size();
    }
}

