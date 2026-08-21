package com.civilizationmod;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Common-side registry mapping marker items to stable building functions and
 * marker-specific metadata.
 */
public final class BuildingMarkerRegistry {
    public static final String FUNCTION_WAREHOUSE = BuildingFunction.WAREHOUSE.id();
    public static final String FUNCTION_RESIDENCE = BuildingFunction.RESIDENCE.id();
    public static final String FUNCTION_UNKNOWN = "unknown";

    private static final Map<Item, MarkerDefinition> DEFINITIONS = new IdentityHashMap<>();

    private BuildingMarkerRegistry() {
    }

    public static void registerDefaults() {
        DEFINITIONS.put(CivilizationItems.WAREHOUSE_MARKER,
                new MarkerDefinition(BuildingFunction.WAREHOUSE, 0));
        DEFINITIONS.put(CivilizationItems.RESIDENTIAL_MARKER_1,
                new MarkerDefinition(BuildingFunction.RESIDENCE, 1));
        DEFINITIONS.put(CivilizationItems.RESIDENTIAL_MARKER_2,
                new MarkerDefinition(BuildingFunction.RESIDENCE, 2));
        DEFINITIONS.put(CivilizationItems.RESIDENTIAL_MARKER_4,
                new MarkerDefinition(BuildingFunction.RESIDENCE, 4));
        DEFINITIONS.put(CivilizationItems.RESIDENTIAL_MARKER_6,
                new MarkerDefinition(BuildingFunction.RESIDENCE, 6));
    }

    public static MarkerDefinition definition(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        return DEFINITIONS.get(stack.getItem());
    }

    public static boolean isKnownMarker(ItemStack stack) {
        return definition(stack) != null;
    }

    public static String functionId(ItemStack stack) {
        MarkerDefinition definition = definition(stack);
        return definition == null ? FUNCTION_UNKNOWN : definition.function().id();
    }

    public static int capacity(ItemStack stack) {
        MarkerDefinition definition = definition(stack);
        return definition == null ? 0 : definition.capacity();
    }

    public record MarkerDefinition(BuildingFunction function, int capacity) {
        public MarkerDefinition {
            function = function == null ? BuildingFunction.RESIDENCE : function;
            capacity = Math.max(0, capacity);
        }
    }
}

