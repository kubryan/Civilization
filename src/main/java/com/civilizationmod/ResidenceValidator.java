package com.civilizationmod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

/** Server-side validator for capacity-based residential markers. */
public final class ResidenceValidator {
    private ResidenceValidator() {
    }

    public static Validation validate(ServerLevel level, ItemFrame frame, int capacity) {
        WarehouseTerritoryValidator.Validation territory = WarehouseTerritoryValidator.validate(level, frame);
        if (!territory.isValid()) {
            return new Validation(
                    territory.status(),
                    territory.reason(),
                    Math.max(0, capacity),
                    0);
        }

        int beds = countBeds(level, territory.territory());
        if (beds < capacity) {
            return new Validation(
                    BuildingObservation.VALIDATION_INVALID,
                    BuildingObservation.VALIDATION_REASON_INSUFFICIENT_BEDS,
                    Math.max(0, capacity),
                    beds);
        }
        return new Validation(
                BuildingObservation.VALIDATION_VALID,
                BuildingObservation.VALIDATION_REASON_VALID,
                Math.max(0, capacity),
                beds);
    }

    public static int countBeds(ServerLevel level, WarehouseTerritory territory) {
        if (territory == null) {
            return 0;
        }
        int beds = 0;
        for (int x = territory.minX(); x <= territory.maxX(); x++) {
            for (int y = territory.minY(); y <= territory.maxY(); y++) {
                for (int z = territory.minZ(); z <= territory.maxZ(); z++) {
                    BlockState state = level.getBlockState(new BlockPos(x, y, z));
                    if (state.is(BlockTags.BEDS)
                            && state.getBlock() instanceof BedBlock
                            && state.getValue(BedBlock.PART) == BedPart.FOOT) {
                        beds++;
                    }
                }
            }
        }
        return beds;
    }

    public record Validation(String status, String reason, int capacity, int bedCount) {
        public Validation {
            status = status == null ? BuildingObservation.VALIDATION_INVALID : status;
            reason = reason == null ? BuildingObservation.VALIDATION_REASON_NO_CONTEXT : reason;
            capacity = Math.max(0, capacity);
            bedCount = Math.max(0, bedCount);
        }

        public boolean isValid() {
            return BuildingObservation.VALIDATION_VALID.equals(status);
        }
    }
}

