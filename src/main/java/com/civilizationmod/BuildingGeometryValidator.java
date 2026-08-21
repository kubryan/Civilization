package com.civilizationmod;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

/**
 * Bounded server-side validator for Civitas marker buildings.
 *
 * <p>Warehouse frame-aware validation is declaration-first: a completed
 * warehouse territory carried by the marker must contain the attached ItemFrame
 * in the current dimension. The older door/floor/roof/wall geometry path remains
 * available for compatibility and pure diagnostic seams, but it is no longer the
 * warehouse activation gate.</p>
 */
public final class BuildingGeometryValidator {
    public static final int DOOR_SEARCH_HORIZONTAL_RADIUS = 4;
    public static final int DOOR_SEARCH_VERTICAL_RADIUS = 3;
    public static final int MARKER_DOOR_HORIZONTAL_RADIUS = 2;
    public static final int MARKER_DOOR_VERTICAL_RADIUS = 2;
    public static final int MAX_HORIZONTAL_RADIUS = 8;
    public static final int MAX_FLOOR_TILES = 256;
    public static final int FLOOR_SEARCH_VERTICAL_RADIUS = 8;
    public static final int MAX_ROOM_SCAN_HEIGHT = 30;
    public static final int MIN_ROOM_CLEARANCE = 2;
    public static final int MIN_INTERIOR_AIR_BLOCKS = 4;
    public static final int MIN_FLOOR_SUPPORT_BLOCKS = 4;
    public static final int MIN_CEILING_BLOCKS = 4;

    private static final Direction[] HORIZONTAL_DIRECTIONS = {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.EAST,
            Direction.WEST
    };

    private BuildingGeometryValidator() {
    }

    public static ValidationResult validate(ServerLevel level, BlockPos markerPosition) {
        return diagnose(level, markerPosition, null).result();
    }

    public static ValidationResult validate(
            ServerLevel level,
            BlockPos markerPosition,
            String functionId
    ) {
        return diagnoseInternal(level, markerPosition, null, functionId).result();
    }

    public static ValidationResult validate(
            ServerLevel level,
            ItemFrame markerFrame,
            String functionId
    ) {
        return diagnose(level, markerFrame, functionId).result();
    }

    public static GeometryDiagnostic diagnose(ServerLevel level, BlockPos markerPosition) {
        return diagnoseInternal(level, markerPosition, null, null);
    }

    public static GeometryDiagnostic diagnose(ServerLevel level, ItemFrame markerFrame) {
        return diagnose(level, markerFrame, null);
    }

    public static GeometryDiagnostic diagnose(
            ServerLevel level,
            ItemFrame markerFrame,
            String functionId
    ) {
        if (markerFrame == null) {
            return diagnoseInternal(level, null, null, functionId);
        }
        return diagnoseInternal(level, markerFrame.blockPosition(), markerFrame, functionId);
    }

    /**
     * Validate a marker and retain transient geometry details for diagnostics.
     * The diagnostic object is not persisted in Saved Data.
     */
    public static GeometryDiagnostic diagnose(
            ServerLevel level,
            BlockPos markerPosition,
            String functionId
    ) {
        return diagnoseInternal(level, markerPosition, null, functionId);
    }

    private static GeometryDiagnostic diagnoseInternal(
            ServerLevel level,
            BlockPos markerPosition,
            ItemFrame markerFrame,
            String functionId
    ) {
        if (level == null || markerPosition == null) {
            return diagnostic(
                    markerPosition,
                    null,
                    null,
                    "none",
                    0,
                    false,
                    invalid(BuildingObservation.VALIDATION_REASON_NO_CONTEXT),
                    SideScan.empty("none"));
        }

        if (markerFrame != null && BuildingFunction.WAREHOUSE.id().equals(functionId)) {
            WarehouseTerritoryValidator.Validation territory = WarehouseTerritoryValidator.validate(level, markerFrame);
            int legalMarkers = territory.territoryDeclared() ? 1 : 0;
            return diagnostic(
                    markerPosition,
                    null,
                    null,
                    "territory",
                    legalMarkers,
                    territory.markerAttached(),
                    new ValidationResult(
                            territory.status(),
                            territory.reason(),
                            0,
                            0,
                            0),
                    SideScan.empty("territory"));
        }

        if (markerFrame != null && BuildingFunction.RESIDENCE.id().equals(functionId)) {
            int capacity = BuildingMarkerRegistry.capacity(markerFrame.getItem());
            ResidenceValidator.Validation residence = ResidenceValidator.validate(level, markerFrame, capacity);
            return diagnostic(
                    markerPosition,
                    null,
                    null,
                    "residence",
                    residence.bedCount(),
                    residence.isValid(),
                    new ValidationResult(
                            residence.status(),
                            residence.reason(),
                            residence.bedCount(),
                            residence.capacity(),
                            residence.bedCount()),
                    SideScan.empty("residence"));
        }

        DoorCandidate door = findNearestDoor(level, markerPosition);
        if (door == null) {
            return diagnostic(
                    markerPosition,
                    null,
                    null,
                    "none",
                    0,
                    false,
                    invalid(BuildingObservation.VALIDATION_REASON_NO_DOOR),
                    SideScan.empty("none"));
        }

        MarkerContext markerContext = inspectMarkersNearDoor(
                level,
                markerPosition,
                markerFrame,
                door.lowerPosition());
        if (!markerContext.markerAtDoor()) {
            return diagnostic(
                    markerPosition,
                    door.lowerPosition(),
                    door.state().getValue(DoorBlock.FACING),
                    "none",
                    markerContext.legalMarkerCount(),
                    false,
                    invalid(BuildingObservation.VALIDATION_REASON_MARKER_NOT_AT_DOOR),
                    SideScan.empty("none"));
        }
        if (markerContext.legalMarkerCount() != 1) {
            return diagnostic(
                    markerPosition,
                    door.lowerPosition(),
                    door.state().getValue(DoorBlock.FACING),
                    "none",
                    markerContext.legalMarkerCount(),
                    true,
                    invalid(BuildingObservation.VALIDATION_REASON_MARKER_AMBIGUOUS),
                    SideScan.empty("none"));
        }

        Direction facing = door.state().getValue(DoorBlock.FACING);
        SideScan facingSide = scanSide(level, door.lowerPosition(), facing, "facing");
        SideScan oppositeSide = scanSide(
                level,
                door.lowerPosition(),
                facing.getOpposite(),
                "opposite");
        SideScan selected = selectBestSide(facingSide, oppositeSide);
        boolean warehouse = BuildingFunction.WAREHOUSE.id().equals(functionId);
        ValidationResult result;

        if (selected.scanLimitReached) {
            result = invalid(
                    BuildingObservation.VALIDATION_REASON_SCAN_LIMIT,
                    selected.airBlocks,
                    selected.floorSupportBlocks,
                    selected.ceilingBlocks);
        } else {
            result = evaluateStructure(
                    true,
                    selected.complete,
                    selected.airBlocks,
                    selected.floorSupportBlocks,
                    selected.ceilingBlocks,
                    selected.wallsComplete,
                    selected.entryAccessible,
                    selected.containerBlocks,
                    warehouse);
        }

        return diagnostic(
                markerPosition,
                door.lowerPosition(),
                facing,
                selected.sideLabel,
                markerContext.legalMarkerCount(),
                true,
                result,
                facingSide,
                oppositeSide);
    }

    /**
     * Compatibility decision seam retained for pure Java regression tests.
     * It checks only the original geometry counters and assumes wall and entry
     * checks have already passed.
     */
    public static ValidationResult evaluate(
            boolean doorFound,
            boolean scanComplete,
            int interiorAirBlocks,
            int floorSupportBlocks,
            int ceilingBlocks
    ) {
        return evaluateStructure(
                doorFound,
                scanComplete,
                interiorAirBlocks,
                floorSupportBlocks,
                ceilingBlocks,
                true,
                true,
                0,
                false);
    }

    public static ValidationResult evaluateStructure(
            boolean doorFound,
            boolean scanComplete,
            int interiorAirBlocks,
            int floorSupportBlocks,
            int ceilingBlocks,
            boolean wallsComplete,
            boolean entryAccessible,
            int containerBlocks,
            boolean warehouse
    ) {
        if (!doorFound) {
            return invalid(BuildingObservation.VALIDATION_REASON_NO_DOOR, 0, 0, 0);
        }
        if (!scanComplete) {
            return invalid(
                    BuildingObservation.VALIDATION_REASON_UNLOADED,
                    interiorAirBlocks,
                    floorSupportBlocks,
                    ceilingBlocks);
        }
        if (interiorAirBlocks < MIN_INTERIOR_AIR_BLOCKS) {
            return invalid(
                    BuildingObservation.VALIDATION_REASON_NO_ROOM,
                    interiorAirBlocks,
                    floorSupportBlocks,
                    ceilingBlocks);
        }
        if (floorSupportBlocks < MIN_FLOOR_SUPPORT_BLOCKS) {
            return invalid(
                    BuildingObservation.VALIDATION_REASON_NO_FLOOR,
                    interiorAirBlocks,
                    floorSupportBlocks,
                    ceilingBlocks);
        }
        if (ceilingBlocks < MIN_CEILING_BLOCKS || ceilingBlocks < floorSupportBlocks) {
            return invalid(
                    BuildingObservation.VALIDATION_REASON_NO_CEILING,
                    interiorAirBlocks,
                    floorSupportBlocks,
                    ceilingBlocks);
        }
        if (!wallsComplete) {
            return invalid(
                    BuildingObservation.VALIDATION_REASON_NO_WALLS,
                    interiorAirBlocks,
                    floorSupportBlocks,
                    ceilingBlocks);
        }
        if (!entryAccessible) {
            return invalid(
                    BuildingObservation.VALIDATION_REASON_NO_ENTRY,
                    interiorAirBlocks,
                    floorSupportBlocks,
                    ceilingBlocks);
        }
        if (warehouse && containerBlocks < 1) {
            return invalid(
                    BuildingObservation.VALIDATION_REASON_NO_CONTAINER,
                    interiorAirBlocks,
                    floorSupportBlocks,
                    ceilingBlocks);
        }
        return new ValidationResult(
                BuildingObservation.VALIDATION_VALID,
                BuildingObservation.VALIDATION_REASON_VALID,
                Math.max(0, interiorAirBlocks),
                Math.max(0, floorSupportBlocks),
                Math.max(0, ceilingBlocks));
    }

    public static BlockPos findDoorPosition(ServerLevel level, BlockPos markerPosition) {
        DoorCandidate door = findNearestDoor(level, markerPosition);
        return door == null ? null : door.lowerPosition();
    }

    private static GeometryDiagnostic diagnostic(
            BlockPos markerPosition,
            BlockPos doorPosition,
            Direction doorFacing,
            String selectedSide,
            int legalMarkerCount,
            boolean markerAtDoor,
            ValidationResult result,
            SideScan selected
    ) {
        return diagnostic(
                markerPosition,
                doorPosition,
                doorFacing,
                selectedSide,
                legalMarkerCount,
                markerAtDoor,
                result,
                selected,
                selected);
    }

    private static GeometryDiagnostic diagnostic(
            BlockPos markerPosition,
            BlockPos doorPosition,
            Direction doorFacing,
            String selectedSide,
            int legalMarkerCount,
            boolean markerAtDoor,
            ValidationResult result,
            SideScan facingSide,
            SideScan oppositeSide
    ) {
        return new GeometryDiagnostic(
                markerPosition,
                doorPosition,
                doorFacing,
                selectedSide,
                legalMarkerCount,
                markerAtDoor,
                result,
                facingSide.toDiagnostics(),
                oppositeSide.toDiagnostics());
    }

    private static DoorCandidate findNearestDoor(ServerLevel level, BlockPos markerPosition) {
        BlockPos min = markerPosition.offset(
                -DOOR_SEARCH_HORIZONTAL_RADIUS,
                -DOOR_SEARCH_VERTICAL_RADIUS,
                -DOOR_SEARCH_HORIZONTAL_RADIUS);
        BlockPos max = markerPosition.offset(
                DOOR_SEARCH_HORIZONTAL_RADIUS,
                DOOR_SEARCH_VERTICAL_RADIUS,
                DOOR_SEARCH_HORIZONTAL_RADIUS);

        DoorCandidate nearest = null;
        long nearestDistance = Long.MAX_VALUE;
        for (BlockPos candidate : BlockPos.betweenClosed(min, max)) {
            if (!level.isLoaded(candidate)) {
                continue;
            }
            BlockState state = level.getBlockState(candidate);
            if (!(state.getBlock() instanceof DoorBlock)) {
                continue;
            }

            BlockPos lowerPosition = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER
                    ? candidate.below()
                    : candidate;
            if (!level.isLoaded(lowerPosition)) {
                continue;
            }
            BlockState lowerState = level.getBlockState(lowerPosition);
            if (!(lowerState.getBlock() instanceof DoorBlock)) {
                continue;
            }

            long dx = (long) lowerPosition.getX() - markerPosition.getX();
            long dy = (long) lowerPosition.getY() - markerPosition.getY();
            long dz = (long) lowerPosition.getZ() - markerPosition.getZ();
            long distance = dx * dx + dy * dy + dz * dz;
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = new DoorCandidate(lowerPosition, lowerState);
            }
        }
        return nearest;
    }

    private static MarkerContext inspectMarkersNearDoor(
            ServerLevel level,
            BlockPos markerPosition,
            ItemFrame markerFrame,
            BlockPos doorPosition
    ) {
        AABB bounds = new AABB(
                doorPosition.getX() - MARKER_DOOR_HORIZONTAL_RADIUS,
                doorPosition.getY() - MARKER_DOOR_VERTICAL_RADIUS,
                doorPosition.getZ() - MARKER_DOOR_HORIZONTAL_RADIUS,
                doorPosition.getX() + MARKER_DOOR_HORIZONTAL_RADIUS + 1.0D,
                doorPosition.getY() + MARKER_DOOR_VERTICAL_RADIUS + 1.0D,
                doorPosition.getZ() + MARKER_DOOR_HORIZONTAL_RADIUS + 1.0D);
        List<ItemFrame> frames = level.getEntities(
                EntityTypeTest.forClass(ItemFrame.class),
                bounds,
                frame -> !BuildingMarkerRegistry.FUNCTION_UNKNOWN.equals(
                        BuildingMarkerRegistry.functionId(frame.getItem())));
        List<ItemFrame> attachedFrames = frames.stream()
                .filter(frame -> isAttachedToWall(level, frame))
                .toList();
        boolean markerAtDoor = markerFrame != null
                ? attachedFrames.stream().anyMatch(frame -> frame == markerFrame)
                : attachedFrames.stream().anyMatch(frame -> frame.blockPosition().equals(markerPosition));
        return new MarkerContext(attachedFrames.size(), markerAtDoor);
    }

    /**
     * ItemFrame.blockPosition() is the frame position. Minecraft checks the
     * backing wall at frame position relative to the opposite of getDirection().
     */
    static boolean isAttachedToWall(ServerLevel level, ItemFrame frame) {
        if (frame == null || frame.getDirection() == null
                || !frame.getDirection().getAxis().isHorizontal()) {
            return false;
        }
        BlockPos framePosition = frame.blockPosition();
        BlockPos supportPosition = framePosition.relative(frame.getDirection().getOpposite());
        return level.isLoaded(framePosition)
                && level.isLoaded(supportPosition)
                && isBoundaryBlock(level.getBlockState(supportPosition));
    }

    private static SideScan scanSide(
            ServerLevel level,
            BlockPos doorPosition,
            Direction roomSide,
            String sideLabel
    ) {
        BlockPos start = doorPosition.relative(roomSide, 1);
        SideScan scan = new SideScan(sideLabel, roomSide, start);
        Deque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            BlockPos roomAnchor = queue.removeFirst();
            if (!withinHorizontalBounds(start, roomAnchor) || !visited.add(roomAnchor)) {
                continue;
            }
            if (scan.totalSamples >= MAX_FLOOR_TILES) {
                scan.scanLimitReached = true;
                return scan;
            }

            scan.totalSamples++;
            scan.bounds.include(roomAnchor);
            FloorSearchResult floorSearch = findFloorColumn(level, roomAnchor, doorPosition);
            if (!floorSearch.loaded()) {
                scan.complete = false;
                return scan;
            }
            if (floorSearch.column() == null) {
                continue;
            }

            FloorColumn floorColumn = floorSearch.column();
            BlockPos floorPosition = floorColumn.supportPosition();
            BlockPos roomPosition = floorColumn.roomPosition();
            if (scan.floorY != Integer.MIN_VALUE && floorPosition.getY() != scan.floorY) {
                continue;
            }
            if (scan.floorY == Integer.MIN_VALUE) {
                scan.floorY = floorPosition.getY();
            }
            scan.bounds.include(floorPosition);
            scan.bounds.include(roomPosition);
            scan.floorSupportBlocks++;
            scan.floorPositions.add(roomPosition);
            scan.minFloorY = Math.min(scan.minFloorY, floorPosition.getY());
            scan.maxFloorY = Math.max(scan.maxFloorY, floorPosition.getY());
            if (scan.entryRoomPosition == null && !floorColumn.container()) {
                scan.entryRoomPosition = roomPosition;
            }
            if (floorColumn.container()) {
                scan.containerBlocks++;
            }

            ColumnScanResult column = scanRoomColumn(level, roomPosition, scan.bounds);
            if (!column.loaded()) {
                scan.complete = false;
                return scan;
            }
            scan.airBlocks += column.airBlocks();
            if (column.foundRoof()) {
                scan.ceilingNonAirBlocks++;
            }
            if (column.roofAccepted()) {
                scan.ceilingBlocks++;
                scan.minRoomHeight = Math.min(scan.minRoomHeight, column.roofHeight());
                scan.maxRoomHeight = Math.max(scan.maxRoomHeight, column.roofHeight());
                enqueueNeighbors(queue, start, roomPosition);
            }
        }

        scan.complete = true;
        WallScanResult walls = scanWalls(level, scan, roomSide);
        scan.wallBlocks = walls.wallBlocks();
        scan.wallSegments = walls.wallSegments();
        scan.wallsComplete = walls.complete();
        scan.entryAccessible = scan.entryRoomPosition != null
                && canEnter(level, doorPosition, roomSide, scan.entryRoomPosition);
        return scan;
    }

    private static FloorSearchResult findFloorColumn(
            ServerLevel level,
            BlockPos roomAnchor,
            BlockPos doorPosition
    ) {
        int expectedSupportY = doorPosition.getY() - 1;
        for (int distance = 0; distance <= FLOOR_SEARCH_VERTICAL_RADIUS; distance++) {
            int[] offsets = distance == 0 ? new int[]{0} : new int[]{-distance, distance};
            for (int offset : offsets) {
                BlockPos supportPosition = new BlockPos(
                        roomAnchor.getX(),
                        expectedSupportY + offset,
                        roomAnchor.getZ());
                BlockPos roomPosition = supportPosition.above();
                if (!level.isLoaded(supportPosition) || !level.isLoaded(roomPosition)) {
                    return new FloorSearchResult(false, null);
                }

                BlockState supportState = level.getBlockState(supportPosition);
                BlockState roomState = level.getBlockState(roomPosition);
                if (!isRoomPassable(level, supportPosition, supportState)
                        && (isRoomPassable(level, roomPosition, roomState)
                        || isContainerBlock(roomState))) {
                    return new FloorSearchResult(
                            true,
                            new FloorColumn(
                                    supportPosition,
                                    roomPosition,
                                    isContainerBlock(roomState)));
                }
            }
        }
        return new FloorSearchResult(true, null);
    }

    private static ColumnScanResult scanRoomColumn(
            ServerLevel level,
            BlockPos roomPosition,
            BoundsTracker bounds
    ) {
        int airBlocks = 0;
        for (int height = 0; height < MAX_ROOM_SCAN_HEIGHT; height++) {
            BlockPos sample = roomPosition.above(height);
            bounds.include(sample);
            if (!level.isLoaded(sample)) {
                return new ColumnScanResult(false, airBlocks, false, false, 0);
            }

            BlockState state = level.getBlockState(sample);
            if (isRoomPassable(level, sample, state)) {
                airBlocks++;
                continue;
            }
            if (height == 0 && isContainerBlock(state)) {
                continue;
            }

            return new ColumnScanResult(
                    true,
                    airBlocks,
                    true,
                    height >= MIN_ROOM_CLEARANCE,
                    height);
        }
        return new ColumnScanResult(true, airBlocks, false, false, 0);
    }

    private static WallScanResult scanWalls(
            ServerLevel level,
            SideScan scan,
            Direction roomSide
    ) {
        if (scan.floorPositions.isEmpty() || scan.maxRoomHeight == 0) {
            return new WallScanResult(0, 0, false);
        }

        Direction doorWall = roomSide.getOpposite();
        int wallBlocks = 0;
        int wallSegments = 0;
        for (BlockPos roomPosition : scan.floorPositions) {
            for (Direction direction : HORIZONTAL_DIRECTIONS) {
                if (direction == doorWall) {
                    continue;
                }
                BlockPos boundary = roomPosition.relative(direction);
                if (scan.floorPositions.contains(boundary)) {
                    continue;
                }
                for (int height = 0; height < scan.maxRoomHeight; height++) {
                    BlockPos wallPosition = boundary.above(height);
                    scan.bounds.include(wallPosition);
                    if (!level.isLoaded(wallPosition)) {
                        return new WallScanResult(wallBlocks, wallSegments, false);
                    }
                    wallSegments++;
                    if (isBoundaryBlock(level.getBlockState(wallPosition))) {
                        wallBlocks++;
                    }
                }
            }
        }
        return new WallScanResult(
                wallBlocks,
                wallSegments,
                wallSegments > 0 && wallBlocks == wallSegments);
    }

    private static boolean canEnter(
            ServerLevel level,
            BlockPos doorPosition,
            Direction roomSide,
            BlockPos entryRoomPosition
    ) {
        if (!canStandAt(level, entryRoomPosition)) {
            return false;
        }
        Direction outsideSide = roomSide.getOpposite();
        BlockPos outsideAnchor = doorPosition.relative(outsideSide, 1);
        for (int offset = -2; offset <= 2; offset++) {
            if (canStandAt(level, outsideAnchor.offset(0, offset, 0))) {
                return true;
            }
        }
        return false;
    }

    private static boolean canStandAt(ServerLevel level, BlockPos bodyPosition) {
        BlockPos headPosition = bodyPosition.above();
        BlockPos supportPosition = bodyPosition.below();
        if (!level.isLoaded(supportPosition)
                || !level.isLoaded(bodyPosition)
                || !level.isLoaded(headPosition)) {
            return false;
        }
        return !isRoomPassable(level, supportPosition, level.getBlockState(supportPosition))
                && isRoomPassable(level, bodyPosition, level.getBlockState(bodyPosition))
                && isRoomPassable(level, headPosition, level.getBlockState(headPosition));
    }

    private static void enqueueNeighbors(
            Deque<BlockPos> queue,
            BlockPos start,
            BlockPos roomPosition
    ) {
        addIfWithin(queue, start, roomPosition.north());
        addIfWithin(queue, start, roomPosition.south());
        addIfWithin(queue, start, roomPosition.east());
        addIfWithin(queue, start, roomPosition.west());
    }

    private static void addIfWithin(Deque<BlockPos> queue, BlockPos start, BlockPos position) {
        if (withinHorizontalBounds(start, position)) {
            queue.addLast(position);
        }
    }

    private static boolean withinHorizontalBounds(BlockPos start, BlockPos position) {
        return Math.abs(position.getX() - start.getX()) <= MAX_HORIZONTAL_RADIUS
                && Math.abs(position.getZ() - start.getZ()) <= MAX_HORIZONTAL_RADIUS;
    }

    private static boolean isRoomPassable(ServerLevel level, BlockPos position, BlockState state) {
        return !state.liquid() && state.getCollisionShape(level, position).isEmpty();
    }

    private static boolean isBoundaryBlock(BlockState state) {
        return !state.isAir() && !state.liquid();
    }

    private static boolean isContainerBlock(BlockState state) {
        return state.getBlock() == Blocks.CHEST;
    }

    private static SideScan selectBestSide(SideScan first, SideScan second) {
        if (first.isStructureCandidate() != second.isStructureCandidate()) {
            return first.isStructureCandidate() ? first : second;
        }
        if (first.complete != second.complete) {
            return first.complete ? first : second;
        }
        if (first.scanLimitReached != second.scanLimitReached) {
            return first.scanLimitReached ? second : first;
        }

        int firstScore = first.floorSupportBlocks * 100
                + first.ceilingBlocks * 40
                + first.wallBlocks * 4
                + first.airBlocks;
        int secondScore = second.floorSupportBlocks * 100
                + second.ceilingBlocks * 40
                + second.wallBlocks * 4
                + second.airBlocks;
        return firstScore >= secondScore ? first : second;
    }

    private static ValidationResult invalid(String reason) {
        return invalid(reason, 0, 0, 0);
    }

    private static ValidationResult invalid(
            String reason,
            int interiorAirBlocks,
            int floorSupportBlocks,
            int ceilingBlocks
    ) {
        return new ValidationResult(
                BuildingObservation.VALIDATION_INVALID,
                reason,
                interiorAirBlocks,
                floorSupportBlocks,
                ceilingBlocks);
    }

    private record DoorCandidate(BlockPos lowerPosition, BlockState state) {
    }

    private record MarkerContext(int legalMarkerCount, boolean markerAtDoor) {
    }

    private record FloorColumn(
            BlockPos supportPosition,
            BlockPos roomPosition,
            boolean container
    ) {
    }

    private record FloorSearchResult(boolean loaded, FloorColumn column) {
    }

    private record ColumnScanResult(
            boolean loaded,
            int airBlocks,
            boolean foundRoof,
            boolean roofAccepted,
            int roofHeight
    ) {
    }

    private record WallScanResult(int wallBlocks, int wallSegments, boolean complete) {
    }

    public record GeometryDiagnostic(
            BlockPos markerPosition,
            BlockPos doorPosition,
            Direction doorFacing,
            String selectedSide,
            int legalMarkerCount,
            boolean markerAtDoor,
            ValidationResult result,
            SideDiagnostics facingSide,
            SideDiagnostics oppositeSide
    ) {
    }

    public record SideDiagnostics(
            String sideLabel,
            boolean complete,
            boolean scanLimitReached,
            int totalSamples,
            int expectedSamples,
            int airBlocks,
            int floorSupportBlocks,
            int ceilingBlocks,
            int ceilingNonAirBlocks,
            int minFloorY,
            int maxFloorY,
            int minRoomHeight,
            int maxRoomHeight,
            int wallBlocks,
            int wallSegments,
            boolean wallsComplete,
            boolean entryAccessible,
            int containerBlocks,
            BlockPos sampleStart,
            BlockPos sampleEnd
    ) {
    }

    public record ValidationResult(
            String status,
            String reason,
            int interiorAirBlocks,
            int floorSupportBlocks,
            int ceilingBlocks
    ) {
        public ValidationResult {
            status = status == null ? BuildingObservation.VALIDATION_INVALID : status;
            reason = reason == null ? BuildingObservation.VALIDATION_REASON_NO_CONTEXT : reason;
            interiorAirBlocks = Math.max(0, interiorAirBlocks);
            floorSupportBlocks = Math.max(0, floorSupportBlocks);
            ceilingBlocks = Math.max(0, ceilingBlocks);
        }

        public boolean isValid() {
            return BuildingObservation.VALIDATION_VALID.equals(status);
        }
    }

    private static final class SideScan {
        private final String sideLabel;
        private final Direction side;
        private final int expectedSamples = MAX_FLOOR_TILES;
        private final BoundsTracker bounds;
        private final Set<BlockPos> floorPositions = new HashSet<>();
        private BlockPos entryRoomPosition;
        private boolean complete;
        private boolean scanLimitReached;
        private int totalSamples;
        private int airBlocks;
        private int floorSupportBlocks;
        private int ceilingBlocks;
        private int ceilingNonAirBlocks;
        private int floorY = Integer.MIN_VALUE;
        private int minFloorY = Integer.MAX_VALUE;
        private int maxFloorY = Integer.MIN_VALUE;
        private int minRoomHeight = Integer.MAX_VALUE;
        private int maxRoomHeight;
        private int wallBlocks;
        private int wallSegments;
        private boolean wallsComplete;
        private boolean entryAccessible;
        private int containerBlocks;

        private SideScan(String sideLabel, Direction side, BlockPos start) {
            this.sideLabel = sideLabel;
            this.side = side;
            this.bounds = new BoundsTracker(start);
        }

        private static SideScan empty(String sideLabel) {
            SideScan scan = new SideScan(sideLabel, null, new BlockPos(0, 0, 0));
            scan.complete = false;
            return scan;
        }

        private boolean isStructureCandidate() {
            return complete
                    && floorSupportBlocks >= MIN_FLOOR_SUPPORT_BLOCKS
                    && ceilingBlocks >= floorSupportBlocks
                    && wallsComplete
                    && entryAccessible;
        }

        private String sideLabel() {
            return sideLabel;
        }

        private SideDiagnostics toDiagnostics() {
            return new SideDiagnostics(
                    sideLabel,
                    complete,
                    scanLimitReached,
                    totalSamples,
                    expectedSamples,
                    airBlocks,
                    floorSupportBlocks,
                    ceilingBlocks,
                    ceilingNonAirBlocks,
                    floorYOrZero(minFloorY),
                    floorYOrZero(maxFloorY),
                    roomHeightOrZero(minRoomHeight),
                    maxRoomHeight,
                    wallBlocks,
                    wallSegments,
                    wallsComplete,
                    entryAccessible,
                    containerBlocks,
                    bounds.start(),
                    bounds.end());
        }
    }

    private static int floorYOrZero(int value) {
        return value == Integer.MAX_VALUE || value == Integer.MIN_VALUE ? 0 : value;
    }

    private static int roomHeightOrZero(int value) {
        return value == Integer.MAX_VALUE ? 0 : value;
    }

    private static final class BoundsTracker {
        private int minX;
        private int minY;
        private int minZ;
        private int maxX;
        private int maxY;
        private int maxZ;

        private BoundsTracker(BlockPos initial) {
            this.minX = initial.getX();
            this.minY = initial.getY();
            this.minZ = initial.getZ();
            this.maxX = initial.getX();
            this.maxY = initial.getY();
            this.maxZ = initial.getZ();
        }

        private void include(BlockPos position) {
            minX = Math.min(minX, position.getX());
            minY = Math.min(minY, position.getY());
            minZ = Math.min(minZ, position.getZ());
            maxX = Math.max(maxX, position.getX());
            maxY = Math.max(maxY, position.getY());
            maxZ = Math.max(maxZ, position.getZ());
        }

        private BlockPos start() {
            return new BlockPos(minX, minY, minZ);
        }

        private BlockPos end() {
            return new BlockPos(maxX, maxY, maxZ);
        }
    }
}
