package com.civilizationmod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;

/**
 * Versioned, server-authored warehouse territory data carried by a marker stack.
 *
 * <p>The stack stores either an incomplete point-A selection or a completed,
 * normalized inclusive cuboid. A completed territory remains on the stack when
 * the player changes hotbar slots; only an incomplete point-A selection is
 * cleared by {@link WarehouseMarkerItem#inventoryTick}.</p>
 */
public record WarehouseTerritory(
        int version,
        String dimension,
        int minX,
        int minY,
        int minZ,
        int maxX,
        int maxY,
        int maxZ
) {
    public static final int CURRENT_VERSION = 1;
    public static final int MAX_AXIS_LENGTH = 64;
    public static final long MAX_VOLUME = 262_144L;

    private static final String DATA_KEY = "civitas_warehouse";
    private static final String STATE_KEY = "state";
    private static final String STATE_PENDING = "pending";
    private static final String STATE_TERRITORY = "territory";
    private static final String VERSION_KEY = "version";
    private static final String DIMENSION_KEY = "dimension";
    private static final String POINT_X_KEY = "point_x";
    private static final String POINT_Y_KEY = "point_y";
    private static final String POINT_Z_KEY = "point_z";
    private static final String POINT_A_X_KEY = "point_a_x";
    private static final String POINT_A_Y_KEY = "point_a_y";
    private static final String POINT_A_Z_KEY = "point_a_z";
    private static final String POINT_B_X_KEY = "point_b_x";
    private static final String POINT_B_Y_KEY = "point_b_y";
    private static final String POINT_B_Z_KEY = "point_b_z";
    private static final String MIN_X_KEY = "min_x";
    private static final String MIN_Y_KEY = "min_y";
    private static final String MIN_Z_KEY = "min_z";
    private static final String MAX_X_KEY = "max_x";
    private static final String MAX_Y_KEY = "max_y";
    private static final String MAX_Z_KEY = "max_z";

    public WarehouseTerritory {
        version = Math.max(1, version);
        dimension = dimension == null ? "" : dimension;
        if (minX > maxX || minY > maxY || minZ > maxZ) {
            throw new IllegalArgumentException("Warehouse territory bounds must be normalized");
        }
    }

    public static Optional<WarehouseTerritory> read(ItemStack stack) {
        CompoundTag payload = payload(stack);
        if (!STATE_TERRITORY.equals(payload.getStringOr(STATE_KEY, ""))) {
            return Optional.empty();
        }

        String dimension = payload.getStringOr(DIMENSION_KEY, "");
        if (dimension.isBlank()
                || !containsAll(payload, MIN_X_KEY, MIN_Y_KEY, MIN_Z_KEY, MAX_X_KEY, MAX_Y_KEY, MAX_Z_KEY)) {
            return Optional.empty();
        }

        int minX = payload.getIntOr(MIN_X_KEY, 0);
        int minY = payload.getIntOr(MIN_Y_KEY, 0);
        int minZ = payload.getIntOr(MIN_Z_KEY, 0);
        int maxX = payload.getIntOr(MAX_X_KEY, 0);
        int maxY = payload.getIntOr(MAX_Y_KEY, 0);
        int maxZ = payload.getIntOr(MAX_Z_KEY, 0);
        if (!isNormalizedAndWithinLimits(minX, minY, minZ, maxX, maxY, maxZ)) {
            return Optional.empty();
        }

        return Optional.of(new WarehouseTerritory(
                payload.getIntOr(VERSION_KEY, CURRENT_VERSION),
                dimension,
                minX,
                minY,
                minZ,
                maxX,
                maxY,
                maxZ));
    }

    public static boolean hasPendingSelection(ItemStack stack) {
        CompoundTag payload = payload(stack);
        return STATE_PENDING.equals(payload.getStringOr(STATE_KEY, ""))
                && !payload.getStringOr(DIMENSION_KEY, "").isBlank()
                && containsAll(payload, POINT_X_KEY, POINT_Y_KEY, POINT_Z_KEY);
    }

    public static void setPointA(ItemStack stack, String dimension, BlockPos position) {
        CompoundTag payload = new CompoundTag();
        payload.putInt(VERSION_KEY, CURRENT_VERSION);
        payload.putString(STATE_KEY, STATE_PENDING);
        payload.putString(DIMENSION_KEY, dimension);
        payload.putInt(POINT_X_KEY, position.getX());
        payload.putInt(POINT_Y_KEY, position.getY());
        payload.putInt(POINT_Z_KEY, position.getZ());
        setPayload(stack, payload);
    }

    public static CompletionResult complete(ItemStack stack, String dimension, BlockPos pointB) {
        CompoundTag pending = payload(stack);
        if (!STATE_PENDING.equals(pending.getStringOr(STATE_KEY, ""))
                || !containsAll(pending, POINT_X_KEY, POINT_Y_KEY, POINT_Z_KEY)) {
            return CompletionResult.NO_FIRST_POINT;
        }

        String firstDimension = pending.getStringOr(DIMENSION_KEY, "");
        if (!firstDimension.equals(dimension)) {
            clearPendingSelection(stack);
            return CompletionResult.DIFFERENT_DIMENSION;
        }

        int pointAX = pending.getIntOr(POINT_A_X_KEY, pending.getIntOr(POINT_X_KEY, 0));
        int pointAY = pending.getIntOr(POINT_A_Y_KEY, pending.getIntOr(POINT_Y_KEY, 0));
        int pointAZ = pending.getIntOr(POINT_A_Z_KEY, pending.getIntOr(POINT_Z_KEY, 0));
        int minX = Math.min(pointAX, pointB.getX());
        int minY = Math.min(pointAY, pointB.getY());
        int minZ = Math.min(pointAZ, pointB.getZ());
        int maxX = Math.max(pointAX, pointB.getX());
        int maxY = Math.max(pointAY, pointB.getY());
        int maxZ = Math.max(pointAZ, pointB.getZ());
        if (!isNormalizedAndWithinLimits(minX, minY, minZ, maxX, maxY, maxZ)) {
            return CompletionResult.TOO_LARGE;
        }

        WarehouseTerritory territory = new WarehouseTerritory(
                CURRENT_VERSION,
                dimension,
                minX,
                minY,
                minZ,
                maxX,
                maxY,
                maxZ);
        CompoundTag payload = new CompoundTag();
        payload.putInt(VERSION_KEY, territory.version());
        payload.putString(STATE_KEY, STATE_TERRITORY);
        payload.putString(DIMENSION_KEY, territory.dimension());
        payload.putInt(POINT_A_X_KEY, pointAX);
        payload.putInt(POINT_A_Y_KEY, pointAY);
        payload.putInt(POINT_A_Z_KEY, pointAZ);
        payload.putInt(POINT_B_X_KEY, pointB.getX());
        payload.putInt(POINT_B_Y_KEY, pointB.getY());
        payload.putInt(POINT_B_Z_KEY, pointB.getZ());
        payload.putInt(MIN_X_KEY, territory.minX());
        payload.putInt(MIN_Y_KEY, territory.minY());
        payload.putInt(MIN_Z_KEY, territory.minZ());
        payload.putInt(MAX_X_KEY, territory.maxX());
        payload.putInt(MAX_Y_KEY, territory.maxY());
        payload.putInt(MAX_Z_KEY, territory.maxZ());
        setPayload(stack, payload);
        return CompletionResult.COMPLETE;
    }

    public static Optional<BlockPos> selectedPointA(ItemStack stack) {
        CompoundTag payload = payload(stack);
        if (STATE_PENDING.equals(payload.getStringOr(STATE_KEY, ""))) {
            return readPoint(payload, POINT_A_X_KEY, POINT_A_Y_KEY, POINT_A_Z_KEY, POINT_X_KEY, POINT_Y_KEY, POINT_Z_KEY);
        }
        if (STATE_TERRITORY.equals(payload.getStringOr(STATE_KEY, ""))) {
            return readPoint(payload, POINT_A_X_KEY, POINT_A_Y_KEY, POINT_A_Z_KEY, MIN_X_KEY, MIN_Y_KEY, MIN_Z_KEY);
        }
        return Optional.empty();
    }

    public static Optional<BlockPos> selectedPointB(ItemStack stack) {
        CompoundTag payload = payload(stack);
        if (!STATE_TERRITORY.equals(payload.getStringOr(STATE_KEY, ""))) {
            return Optional.empty();
        }
        return readPoint(payload, POINT_B_X_KEY, POINT_B_Y_KEY, POINT_B_Z_KEY, MAX_X_KEY, MAX_Y_KEY, MAX_Z_KEY);
    }

    public static boolean isCompleted(ItemStack stack) {
        return read(stack).isPresent();
    }

    public static void clearPendingSelection(ItemStack stack) {
        CompoundTag current = customData(stack).copyTag();
        current.remove(DATA_KEY);
        if (current.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(current));
        }
    }

    public boolean contains(BlockPos position) {
        return position.getX() >= minX && position.getX() <= maxX
                && position.getY() >= minY && position.getY() <= maxY
                && position.getZ() >= minZ && position.getZ() <= maxZ;
    }

    public int sizeX() {
        return maxX - minX + 1;
    }

    public int sizeY() {
        return maxY - minY + 1;
    }

    public int sizeZ() {
        return maxZ - minZ + 1;
    }

    public long volume() {
        return (long) sizeX() * sizeY() * sizeZ();
    }

    private static CompoundTag payload(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return new CompoundTag();
        }
        return customData(stack).copyTag().getCompoundOrEmpty(DATA_KEY);
    }

    private static CustomData customData(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
    }

    private static void setPayload(ItemStack stack, CompoundTag payload) {
        CompoundTag outer = customData(stack).copyTag();
        outer.put(DATA_KEY, payload);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(outer));
    }

    private static Optional<BlockPos> readPoint(
            CompoundTag tag,
            String primaryX,
            String primaryY,
            String primaryZ,
            String fallbackX,
            String fallbackY,
            String fallbackZ
    ) {
        if (!containsAll(tag, primaryX, primaryY, primaryZ)
                && !containsAll(tag, fallbackX, fallbackY, fallbackZ)) {
            return Optional.empty();
        }
        int x = tag.contains(primaryX) ? tag.getIntOr(primaryX, 0) : tag.getIntOr(fallbackX, 0);
        int y = tag.contains(primaryY) ? tag.getIntOr(primaryY, 0) : tag.getIntOr(fallbackY, 0);
        int z = tag.contains(primaryZ) ? tag.getIntOr(primaryZ, 0) : tag.getIntOr(fallbackZ, 0);
        return Optional.of(new BlockPos(x, y, z));
    }

    private static boolean containsAll(CompoundTag tag, String... keys) {
        for (String key : keys) {
            if (!tag.contains(key)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isNormalizedAndWithinLimits(
            int minX,
            int minY,
            int minZ,
            int maxX,
            int maxY,
            int maxZ
    ) {
        if (minX > maxX || minY > maxY || minZ > maxZ) {
            return false;
        }
        long sizeX = (long) maxX - minX + 1L;
        long sizeY = (long) maxY - minY + 1L;
        long sizeZ = (long) maxZ - minZ + 1L;
        return sizeX <= MAX_AXIS_LENGTH
                && sizeY <= MAX_AXIS_LENGTH
                && sizeZ <= MAX_AXIS_LENGTH
                && sizeX * sizeY * sizeZ <= MAX_VOLUME;
    }

    public enum CompletionResult {
        COMPLETE,
        NO_FIRST_POINT,
        DIFFERENT_DIMENSION,
        TOO_LARGE
    }
}
