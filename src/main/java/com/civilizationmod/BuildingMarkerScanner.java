package com.civilizationmod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Bounded, server-side scanner for registered building marker ItemFrames.
 *
 * <p>The first slice deliberately scans only loaded entities around the command
 * origin. It does not force chunk loading, inspect arbitrary blocks, or treat
 * an unknown item as a civilization building.</p>
 */
public final class BuildingMarkerScanner {
    public static final int SEARCH_HORIZONTAL_RADIUS = 32;
    public static final int SEARCH_VERTICAL_RADIUS = 16;

    private BuildingMarkerScanner() {
    }

    public static List<MarkerCandidate> scan(ServerLevel level, BlockPos origin, int radius) {
        int horizontalRadius = Math.max(1, radius);
        int verticalRadius = Math.max(1, radius);
        AABB bounds = new AABB(
                origin.getX() - horizontalRadius,
                origin.getY() - verticalRadius,
                origin.getZ() - horizontalRadius,
                origin.getX() + horizontalRadius + 1.0D,
                origin.getY() + verticalRadius + 1.0D,
                origin.getZ() + horizontalRadius + 1.0D
        );

        return level.getEntities(
                        EntityTypeTest.forClass(ItemFrame.class),
                        bounds,
                        frame -> !BuildingMarkerRegistry.FUNCTION_UNKNOWN.equals(
                                BuildingMarkerRegistry.functionId(frame.getItem())
                        )
                )
                .stream()
                .map(frame -> new MarkerCandidate(
                        BuildingMarkerRegistry.functionId(frame.getItem()),
                        frame.blockPosition(),
                        frame
                ))
                .toList();
    }

    public record MarkerCandidate(String functionId, BlockPos position, ItemFrame frame) {
    }
}

