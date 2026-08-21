package com.civilizationmod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ItemFrame;

import java.util.Optional;

/** Server-side declaration validator for a warehouse marker's territory. */
public final class WarehouseTerritoryValidator {
    private WarehouseTerritoryValidator() {
    }

    public static Validation validate(ServerLevel level, ItemFrame frame) {
        if (level == null || frame == null) {
            return new Validation(
                    BuildingObservation.VALIDATION_INVALID,
                    BuildingObservation.VALIDATION_REASON_NO_CONTEXT,
                    false,
                    false,
                    null);
        }

        Optional<WarehouseTerritory> territory = WarehouseTerritory.read(frame.getItem());
        if (territory.isEmpty()) {
            return new Validation(
                    BuildingObservation.VALIDATION_INVALID,
                    BuildingObservation.VALIDATION_REASON_TERRITORY_MISSING,
                    false,
                    false,
                    null);
        }

        WarehouseTerritory value = territory.get();
        String dimension = level.dimension().identifier().toString();
        if (!dimension.equals(value.dimension())) {
            return new Validation(
                    BuildingObservation.VALIDATION_INVALID,
                    BuildingObservation.VALIDATION_REASON_TERRITORY_WRONG_DIMENSION,
                    false,
                    false,
                    value);
        }

        BlockPos framePosition = frame.blockPosition();
        if (!value.contains(framePosition)) {
            return new Validation(
                    BuildingObservation.VALIDATION_INVALID,
                    BuildingObservation.VALIDATION_REASON_MARKER_OUTSIDE_TERRITORY,
                    false,
                    false,
                    value);
        }

        boolean attached = BuildingGeometryValidator.isAttachedToWall(level, frame);
        if (!attached) {
            return new Validation(
                    BuildingObservation.VALIDATION_INVALID,
                    BuildingObservation.VALIDATION_REASON_MARKER_NOT_ATTACHED,
                    false,
                    false,
                    value);
        }

        return new Validation(
                BuildingObservation.VALIDATION_VALID,
                BuildingObservation.VALIDATION_REASON_VALID,
                true,
                true,
                value);
    }

    public record Validation(
            String status,
            String reason,
            boolean territoryDeclared,
            boolean markerAttached,
            WarehouseTerritory territory
    ) {
        public boolean isValid() {
            return BuildingObservation.VALIDATION_VALID.equals(status);
        }
    }
}
