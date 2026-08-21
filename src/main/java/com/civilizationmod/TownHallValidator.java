package com.civilizationmod;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ItemFrame;

/** Minimal server-side validation for a Town Hall marker prototype. */
public final class TownHallValidator {
    private TownHallValidator() {
    }

    public static Validation validate(ServerLevel level, ItemFrame frame) {
        boolean attached = level != null
                && frame != null
                && BuildingGeometryValidator.isAttachedToWall(level, frame);
        return new Validation(
                attached ? BuildingObservation.VALIDATION_VALID : BuildingObservation.VALIDATION_INVALID,
                attached
                        ? BuildingObservation.VALIDATION_REASON_VALID
                        : BuildingObservation.VALIDATION_REASON_MARKER_NOT_ATTACHED,
                attached);
    }

    public record Validation(String status, String reason, boolean markerAttached) {
        public boolean isValid() {
            return BuildingObservation.VALIDATION_VALID.equals(status);
        }
    }
}
