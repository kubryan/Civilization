package com.civilizationmod;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

/** Persistent server-side identity of one registered Town Hall core. */
public record TownHallCore(
        String colonyId,
        String dimension,
        int markerX,
        int markerY,
        int markerZ,
        long createdAt,
        int radius
) {
    public static final int DEFAULT_RADIUS = 64;
    public static final int MAX_RADIUS = 512;

    public static final Codec<TownHallCore> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("colony_id", "").forGetter(TownHallCore::colonyId),
            Codec.STRING.optionalFieldOf("dimension", "").forGetter(TownHallCore::dimension),
            Codec.INT.optionalFieldOf("marker_x", 0).forGetter(TownHallCore::markerX),
            Codec.INT.optionalFieldOf("marker_y", 0).forGetter(TownHallCore::markerY),
            Codec.INT.optionalFieldOf("marker_z", 0).forGetter(TownHallCore::markerZ),
            Codec.LONG.optionalFieldOf("created_at", 0L).forGetter(TownHallCore::createdAt),
            Codec.INT.optionalFieldOf("radius", DEFAULT_RADIUS).forGetter(TownHallCore::radius)
    ).apply(instance, TownHallCore::new));

    public TownHallCore {
        dimension = dimension == null ? "" : dimension;
        colonyId = colonyId == null || colonyId.isBlank()
                ? createColonyId(dimension, markerX, markerY, markerZ)
                : colonyId;
        createdAt = Math.max(0L, createdAt);
        radius = Math.max(1, Math.min(MAX_RADIUS, radius));
    }

    public static TownHallCore create(String dimension, BlockPos markerPosition, long createdAt) {
        return new TownHallCore(
                createColonyId(dimension, markerPosition.getX(), markerPosition.getY(), markerPosition.getZ()),
                dimension,
                markerPosition.getX(),
                markerPosition.getY(),
                markerPosition.getZ(),
                createdAt,
                DEFAULT_RADIUS);
    }

    public boolean isSameMarker(String otherDimension, BlockPos position) {
        return dimension.equals(otherDimension)
                && markerX == position.getX()
                && markerY == position.getY()
                && markerZ == position.getZ();
    }

    public boolean contains(BlockPos position) {
        return position != null
                && Math.abs((long) position.getX() - markerX) <= radius
                && Math.abs((long) position.getY() - markerY) <= radius
                && Math.abs((long) position.getZ() - markerZ) <= radius;
    }

    /** Returns whether the two same-dimension cubic colony ranges overlap. */
    public boolean overlaps(TownHallCore other) {
        if (other == null || !dimension.equals(other.dimension)) {
            return false;
        }
        long combinedRadius = (long) radius + other.radius;
        return Math.abs((long) markerX - other.markerX) <= combinedRadius
                && Math.abs((long) markerY - other.markerY) <= combinedRadius
                && Math.abs((long) markerZ - other.markerZ) <= combinedRadius;
    }

    public TownHallCore withRadius(int newRadius) {
        return new TownHallCore(
                colonyId,
                dimension,
                markerX,
                markerY,
                markerZ,
                createdAt,
                newRadius);
    }

    private static String createColonyId(String dimension, int x, int y, int z) {
        String safeDimension = dimension == null ? "" : dimension;
        return safeDimension + "@" + x + "," + y + "," + z;
    }
}
