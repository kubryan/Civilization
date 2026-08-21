package com.civilizationmod;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

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

    public static final ResourceKey<Item> TOWN_HALL_MARKER_KEY = itemKey("town_hall_marker");
    public static final Item TOWN_HALL_MARKER = register(
            TOWN_HALL_MARKER_KEY,
            TownHallMarkerItem::new,
            new Item.Properties().stacksTo(1)
    );

    /** The current generic binding device for every Civitas building marker. */
    public static final ResourceKey<Item> CIVITAS_BINDING_DEVICE_KEY = itemKey("civitas_binding_device");
    public static final Item CIVITAS_BINDING_DEVICE = register(
            CIVITAS_BINDING_DEVICE_KEY,
            CivitasBindingDeviceItem::new,
            new Item.Properties().stacksTo(1)
    );

    /** Legacy ID retained so existing residence binding devices keep working. */
    @Deprecated
    public static final ResourceKey<Item> RESIDENCE_BINDING_DEVICE_KEY = itemKey("residence_binding_device");
    @Deprecated
    public static final Item RESIDENCE_BINDING_DEVICE = register(
            RESIDENCE_BINDING_DEVICE_KEY,
            ResidenceBindingDeviceItem::new,
            new Item.Properties().stacksTo(1)
    );

    public static final ResourceKey<CreativeModeTab> CIVITAS_CREATIVE_TAB_KEY = ResourceKey.create(
            BuiltInRegistries.CREATIVE_MODE_TAB.key(),
            Identifier.fromNamespaceAndPath(CivilizationMod.MOD_ID, "civitas")
    );

    public static final CreativeModeTab CIVITAS_CREATIVE_TAB = FabricCreativeModeTab.builder()
            .icon(() -> new ItemStack(TOWN_HALL_MARKER))
            .title(Component.translatable("itemGroup.civilizationmod.civitas"))
            .displayItems((parameters, output) -> {
                output.accept(WAREHOUSE_MARKER);
                output.accept(RESIDENTIAL_MARKER_1);
                output.accept(RESIDENTIAL_MARKER_2);
                output.accept(RESIDENTIAL_MARKER_4);
                output.accept(RESIDENTIAL_MARKER_6);
                output.accept(TOWN_HALL_MARKER);
                output.accept(CIVITAS_BINDING_DEVICE);
                output.accept(RESIDENCE_BINDING_DEVICE);
            })
            .build();

    private CivilizationItems() {

    }

        public static void initialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CIVITAS_CREATIVE_TAB_KEY, CIVITAS_CREATIVE_TAB);
        BuildingMarkerRegistry.registerDefaults();

        CivitasBuildingMarkerItem.registerSelectionCallback();
        ResidenceBindingDeviceInteraction.register();

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

