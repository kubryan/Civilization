package com.civilizationmod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;

/** Server-authored residential building reference carried by the binding device. */
public record ResidenceBindingDeviceData(
        int version,
        String dimension,
        int markerX,
        int markerY,
        int markerZ
) {
    public static final int CURRENT_VERSION = 1;
    private static final String DATA_KEY = "civitas_residence_binding";
    private static final String VERSION_KEY = "version";
    private static final String DIMENSION_KEY = "dimension";
    private static final String MARKER_X_KEY = "marker_x";
    private static final String MARKER_Y_KEY = "marker_y";
    private static final String MARKER_Z_KEY = "marker_z";

    public ResidenceBindingDeviceData {
        version = Math.max(CURRENT_VERSION, version);
        dimension = dimension == null ? "" : dimension;
    }

    public static Optional<ResidenceBindingDeviceData> read(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        CompoundTag payload = customData(stack).copyTag().getCompoundOrEmpty(DATA_KEY);
        String dimension = payload.getStringOr(DIMENSION_KEY, "");
        if (dimension.isBlank()
                || !payload.contains(MARKER_X_KEY)
                || !payload.contains(MARKER_Y_KEY)
                || !payload.contains(MARKER_Z_KEY)) {
            return Optional.empty();
        }
        return Optional.of(new ResidenceBindingDeviceData(
                payload.getIntOr(VERSION_KEY, CURRENT_VERSION),
                dimension,
                payload.getIntOr(MARKER_X_KEY, 0),
                payload.getIntOr(MARKER_Y_KEY, 0),
                payload.getIntOr(MARKER_Z_KEY, 0)));
    }

    public static void write(ItemStack stack, String dimension, BlockPos marker) {
        CompoundTag payload = new CompoundTag();
        payload.putInt(VERSION_KEY, CURRENT_VERSION);
        payload.putString(DIMENSION_KEY, dimension);
        payload.putInt(MARKER_X_KEY, marker.getX());
        payload.putInt(MARKER_Y_KEY, marker.getY());
        payload.putInt(MARKER_Z_KEY, marker.getZ());

        CompoundTag outer = customData(stack).copyTag();
        outer.put(DATA_KEY, payload);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(outer));
    }

    public BlockPos marker() {
        return new BlockPos(markerX, markerY, markerZ);
    }

    private static CustomData customData(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
    }
}
