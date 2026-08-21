package com.civilizationmod;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** A persisted item-count entry observed inside a warehouse container. */
public record BuildingStorageItem(String itemId, long count) {
    public static final Codec<BuildingStorageItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("item_id").forGetter(BuildingStorageItem::itemId),
            Codec.LONG.optionalFieldOf("count", 0L).forGetter(BuildingStorageItem::count)
    ).apply(instance, BuildingStorageItem::new));

    public BuildingStorageItem {
        itemId = itemId == null ? "" : itemId;
        count = Math.max(0L, count);
    }
}

