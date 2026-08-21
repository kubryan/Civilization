package com.civilizationmod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

/**
 * Generates a deterministic warehouse fixture for in-game geometry validation.
 *
 * <p>This is a test utility, not world generation. It places one standard house
 * near the command source and deliberately does not run building scan, so the
 * caller can verify scan and marker visual state as separate steps.</p>
 */
public final class BuildingTestStructureGenerator {
    private static final int ROOM_WIDTH = 4;
    private static final int ROOM_DEPTH = 4;
    private static final int ROOM_HEIGHT = 2;
    private static final int GENERATION_OFFSET_X = 6;
    private static final int SET_BLOCK_FLAGS = 3;

    private BuildingTestStructureGenerator() {
    }

    public static GenerationResult generate(ServerLevel level, BlockPos sourcePosition) {
        if (level == null || sourcePosition == null) {
            return GenerationResult.failure("invalid_context");
        }

        BlockPos doorPosition = sourcePosition.offset(GENERATION_OFFSET_X, 0, 0);
        BlockPos floorReference = doorPosition.below();
        int minX = doorPosition.getX() - 1;
        int maxX = minX + ROOM_WIDTH + 1;
        int northZ = doorPosition.getZ();
        int southZ = northZ + ROOM_DEPTH + 1;
        int doorY = doorPosition.getY();
        int topY = doorY + ROOM_HEIGHT;
        BlockPos markerPosition = new BlockPos(
                doorPosition.getX(),
                topY,
                northZ - 1);

        if (!level.isLoaded(floorReference)
                || !level.isLoaded(markerPosition)
                || !isAreaLoaded(level, minX, maxX, northZ - 2, southZ + 1, doorY - 1, topY)) {
            return GenerationResult.failure("unloaded");
        }

        AABB entityBounds = new AABB(
                minX - 2,
                doorY - 2,
                northZ - 2,
                maxX + 3.0D,
                topY + 2.0D,
                southZ + 2.0D);
        boolean existingFrame = !level.getEntities(
                EntityTypeTest.forClass(ItemFrame.class),
                entityBounds,
                frame -> true).isEmpty();
        if (existingFrame) {
            return GenerationResult.failure("existing_item_frame");
        }

        clearVolume(level, minX, maxX, northZ, southZ, doorY, topY);
        placeFloor(level, doorPosition, northZ);
        placePorch(level, doorPosition, northZ);
        placeWalls(level, minX, maxX, northZ, southZ, doorY, topY, doorPosition);
        placeRoof(level, doorPosition, northZ, topY);
        placeDoor(level, doorPosition);
        placeChest(level, doorPosition, northZ);

        ItemStack markerStack = new ItemStack(CivilizationItems.WAREHOUSE_MARKER);
        WarehouseTerritory.setPointA(
                markerStack,
                level.dimension().identifier().toString(),
                new BlockPos(minX, doorY - 1, northZ - 1));
        if (WarehouseTerritory.complete(
                markerStack,
                level.dimension().identifier().toString(),
                new BlockPos(maxX, topY, southZ)) != WarehouseTerritory.CompletionResult.COMPLETE) {
            return GenerationResult.failure("territory_too_large");
        }

        ItemFrame markerFrame = new ItemFrame(level, markerPosition, Direction.NORTH);
        markerFrame.setItem(markerStack, false);
        if (!markerFrame.survives()) {
            return GenerationResult.failure("frame_invalid");
        }
        if (!level.addFreshEntity(markerFrame)) {
            return GenerationResult.failure("entity_spawn_failed");
        }

        return GenerationResult.success(doorPosition, markerPosition, floorReference);
    }

    private static boolean isAreaLoaded(
            ServerLevel level,
            int minX,
            int maxX,
            int minZ,
            int maxZ,
            int minY,
            int maxY
    ) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    if (!level.isLoaded(new BlockPos(x, y, z))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static void clearVolume(
            ServerLevel level,
            int minX,
            int maxX,
            int minZ,
            int maxZ,
            int minY,
            int maxY
    ) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    set(level, new BlockPos(x, y, z), Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private static void placeFloor(ServerLevel level, BlockPos doorPosition, int northZ) {
        int floorY = doorPosition.getY() - 1;
        set(level, new BlockPos(doorPosition.getX(), floorY, northZ), Blocks.OAK_PLANKS.defaultBlockState());
        for (int x = doorPosition.getX(); x <= doorPosition.getX() + ROOM_WIDTH - 1; x++) {
            for (int z = northZ + 1; z <= northZ + ROOM_DEPTH; z++) {
                set(level, new BlockPos(x, floorY, z), Blocks.OAK_PLANKS.defaultBlockState());
            }
        }
    }

    private static void placePorch(ServerLevel level, BlockPos doorPosition, int northZ) {
        int floorY = doorPosition.getY() - 1;
        for (int x = doorPosition.getX() - 1; x <= doorPosition.getX() + 1; x++) {
            for (int z = northZ - 2; z <= northZ - 1; z++) {
                set(level, new BlockPos(x, floorY, z), Blocks.OAK_PLANKS.defaultBlockState());
            }
        }
    }

    private static void placeWalls(
            ServerLevel level,
            int minX,
            int maxX,
            int northZ,
            int southZ,
            int doorY,
            int topY,
            BlockPos doorPosition
    ) {
        BlockState wall = Blocks.OAK_PLANKS.defaultBlockState();
        for (int y = doorY; y <= topY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (!(x == doorPosition.getX() && y < topY)) {
                    set(level, new BlockPos(x, y, northZ), wall);
                }
                set(level, new BlockPos(x, y, southZ), wall);
            }
            for (int z = northZ + 1; z < southZ; z++) {
                set(level, new BlockPos(minX, y, z), wall);
                set(level, new BlockPos(maxX, y, z), wall);
            }
        }
    }

    private static void placeRoof(
            ServerLevel level,
            BlockPos doorPosition,
            int northZ,
            int roofY
    ) {
        BlockState roof = Blocks.OAK_PLANKS.defaultBlockState();
        for (int x = doorPosition.getX(); x <= doorPosition.getX() + ROOM_WIDTH - 1; x++) {
            for (int z = northZ + 1; z <= northZ + ROOM_DEPTH; z++) {
                set(level, new BlockPos(x, roofY, z), roof);
            }
        }
    }

    private static void placeDoor(ServerLevel level, BlockPos doorPosition) {
        BlockState lower = Blocks.OAK_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, Direction.SOUTH)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
        BlockState upper = lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
        level.setBlock(doorPosition, lower, 2);
        level.setBlock(doorPosition.above(), upper, 2);
    }

    private static void placeChest(ServerLevel level, BlockPos doorPosition, int northZ) {
        BlockPos chestPosition = new BlockPos(
                doorPosition.getX() + 1,
                doorPosition.getY(),
                northZ + 2);
        set(level, chestPosition, Blocks.CHEST.defaultBlockState());
    }

    private static void set(ServerLevel level, BlockPos position, BlockState state) {
        level.setBlock(position, state, SET_BLOCK_FLAGS);
    }

    public record GenerationResult(
            boolean generated,
            String reason,
            BlockPos doorPosition,
            BlockPos markerPosition,
            BlockPos floorPosition
    ) {
        private static GenerationResult success(
                BlockPos doorPosition,
                BlockPos markerPosition,
                BlockPos floorPosition
        ) {
            return new GenerationResult(true, "generated", doorPosition, markerPosition, floorPosition);
        }

        private static GenerationResult failure(String reason) {
            return new GenerationResult(false, reason, null, null, null);
        }
    }
}
