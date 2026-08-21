package com.civilizationmod;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.function.Function;

/** Common-side item registry for civilization building markers. */
public final class CivilizationItems {
    public static final ResourceKey<Item> WAREHOUSE_MARKER_KEY = itemKey("warehouse_marker");
    public static final Item WAREHOUSE_MARKER = register(
            WAREHOUSE_MARKER_KEY,
            WarehouseMarkerItem::new,
            new Item.Properties().stacksTo(1)
    );

    public static final ResourceKey<Item> RESIDENTIAL_MARKER_1_KEY = itemKey("residential_marker_1");
    public static final Item RESIDENTIAL_MARKER_1 = register(
            RESIDENTIAL_MARKER_1_KEY,
            ResidentialMarkerItem::new,
            new Item.Properties().stacksTo(1)
    );

    public static final ResourceKey<Item> RESIDENTIAL_MARKER_2_KEY = itemKey("residential_marker_2");
    public static final Item RESIDENTIAL_MARKER_2 = register(
            RESIDENTIAL_MARKER_2_KEY,
            ResidentialMarkerItem::new,
            new Item.Properties().stacksTo(1)
    );

    public static final ResourceKey<Item> RESIDENTIAL_MARKER_4_KEY = itemKey("residential_marker_4");
    public static final Item RESIDENTIAL_MARKER_4 = register(
            RESIDENTIAL_MARKER_4_KEY,
            ResidentialMarkerItem::new,
            new Item.Properties().stacksTo(1)
    );

    public static final ResourceKey<Item> RESIDENTIAL_MARKER_6_KEY = itemKey("residential_marker_6");
    public static final Item RESIDENTIAL_MARKER_6 = register(
            RESIDENTIAL_MARKER_6_KEY,
            ResidentialMarkerItem::new,
            new Item.Properties().stacksTo(1)
    );

    private CivilizationItems() {
    }

    public static void initialize() {
        BuildingMarkerRegistry.registerDefaults();
        CivitasBuildingMarkerItem.registerSelectionCallback();
    }

    private static ResourceKey<Item> itemKey(String name) {
        return ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(CivilizationMod.MOD_ID, name)
        );
    }

    private static Item register(
            ResourceKey<Item> itemKey,
            Function<Item.Properties, Item> itemFactory,
            Item.Properties properties
    ) {
        Item item = itemFactory.apply(properties.setId(itemKey));
        return Registry.register(BuiltInRegistries.ITEM, itemKey, item);
    }
}

