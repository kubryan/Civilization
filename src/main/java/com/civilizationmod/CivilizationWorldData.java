package com.civilizationmod;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Persistent world-level state for the civilization simulation.
 *
 * <p>The world owns the simulation clock, registered settlements, and
 * server-observed building markers. Detailed calculations stay in pure model
 * classes so the same rules can later be tested independently of a running
 * client.</p>
 */
public final class CivilizationWorldData extends SavedData {
    private static final int CURRENT_SCHEMA_VERSION = 5;
    private static final int BUILDING_BIND_HORIZONTAL_RADIUS = 128;
    private static final int BUILDING_BIND_VERTICAL_RADIUS = 64;

    private static final Codec<CivilizationWorldData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("schema_version").forGetter(CivilizationWorldData::getSchemaVersion),
            Codec.LONG.fieldOf("simulation_steps").forGetter(CivilizationWorldData::getSimulationSteps),
            Codec.INT.fieldOf("settlement_count").forGetter(CivilizationWorldData::getSettlementCount),
            SettlementAdapter.CODEC.listOf().optionalFieldOf("settlements", List.of()).forGetter(CivilizationWorldData::getSettlements),
            BuildingObservation.CODEC.listOf().optionalFieldOf("buildings", List.of()).forGetter(CivilizationWorldData::getBuildings)
    ).apply(instance, CivilizationWorldData::new));

    public static final SavedDataType<CivilizationWorldData> TYPE = new SavedDataType<>(
            CivilizationMod.id("civilization_world"),
            CivilizationWorldData::new,
            CODEC,
            null
    );

    private final int schemaVersion;
    private long simulationSteps;
    private final List<SettlementAdapter> settlements;
    private final List<BuildingObservation> buildings;

    public CivilizationWorldData() {
        this(CURRENT_SCHEMA_VERSION, 0L, 0, List.of(), List.of());
    }

    private CivilizationWorldData(
            int schemaVersion,
            long simulationSteps,
            int ignoredSettlementCount,
            List<SettlementAdapter> settlements,
            List<BuildingObservation> buildings
    ) {
        this.schemaVersion = Math.max(schemaVersion, CURRENT_SCHEMA_VERSION);
        this.simulationSteps = Math.max(0L, simulationSteps);
        this.settlements = new ArrayList<>(settlements);
        this.buildings = new ArrayList<>(buildings);
    }

    public static CivilizationWorldData get(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            CivilizationMod.LOGGER.warn("CivilizationWorldData requested before the Overworld is available; using transient state that will not be saved");
            return new CivilizationWorldData();
        }

        CivilizationWorldData data = overworld.getDataStorage().computeIfAbsent(TYPE);
        data.removeDuplicateSettlements();
        data.removeDuplicateBuildings();
        return data;
    }

    public int getSchemaVersion() {
        return this.schemaVersion;
    }

    public long getSimulationSteps() {
        return this.simulationSteps;
    }

    public int getSettlementCount() {
        return this.settlements.size();
    }

    public List<SettlementAdapter> getSettlements() {
        return List.copyOf(this.settlements);
    }

    public SettlementAdapter getSettlement(int oneBasedIndex) {
        if (oneBasedIndex < 1 || oneBasedIndex > this.settlements.size()) {
            return null;
        }
        return this.settlements.get(oneBasedIndex - 1);
    }

    public SettlementAdapter findSettlement(SettlementAdapter probe) {
        return this.settlements.stream()
                .filter(existing -> existing.isSameSettlement(probe))
                .findFirst()
                .orElse(null);
    }

    public boolean replaceSettlement(SettlementAdapter current, SettlementAdapter replacement) {
        int index = this.settlements.indexOf(current);
        if (index < 0) {
            return false;
        }
        this.settlements.set(index, replacement);
        this.setDirty();
        return true;
    }

    public boolean addSettlement(SettlementAdapter settlement) {
        boolean alreadyKnown = this.settlements.stream().anyMatch(existing -> existing.isSameSettlement(settlement));
        if (alreadyKnown) {
            return false;
        }

        this.settlements.add(settlement);
        this.setDirty();
        return true;
    }

    public List<BuildingObservation> getBuildings() {
        return List.copyOf(this.buildings);
    }

    public int getBuildingCount() {
        return this.buildings.size();
    }

    public BuildingObservation getBuilding(int oneBasedIndex) {
        if (oneBasedIndex < 1 || oneBasedIndex > this.buildings.size()) {
            return null;
        }
        return this.buildings.get(oneBasedIndex - 1);
    }

    public BuildingObservation findBuilding(String dimension, int x, int y, int z) {
        return this.buildings.stream()
                .filter(existing -> existing.isSameMarker(dimension, x, y, z))
                .findFirst()
                .orElse(null);
    }

    public BuildingObservation findBuildingAssignedTo(String residentUuid) {
        if (residentUuid == null || residentUuid.isBlank()) {
            return null;
        }
        return this.buildings.stream()
                .filter(existing -> residentUuid.equals(existing.residentUuid()))
                .findFirst()
                .orElse(null);
    }

    public boolean replaceBuilding(BuildingObservation current, BuildingObservation replacement) {
        int index = this.buildings.indexOf(current);
        if (index < 0) {
            return false;
        }
        this.buildings.set(index, replacement);
        this.setDirty();
        return true;
    }

    public boolean removeBuilding(BuildingObservation building) {
        if (building == null || !this.buildings.remove(building)) {
            return false;
        }
        this.setDirty();
        return true;
    }

    /**
     * Removes observations whose marker ItemFrame is gone, but only when the
     * marker position is loaded so an unloaded chunk cannot erase valid data.
     */
    public int removeMissingBuildings(ServerLevel level) {
        if (level == null) {
            return 0;
        }

        String dimension = level.dimension().identifier().toString();
        int removed = 0;
        for (int index = this.buildings.size() - 1; index >= 0; index--) {
            BuildingObservation building = this.buildings.get(index);
            if (!dimension.equals(building.dimension())) {
                continue;
            }

            BlockPos marker = new BlockPos(building.markerX(), building.markerY(), building.markerZ());
            if (!level.isLoaded(marker)) {
                continue;
            }

            boolean markerPresent = !level.getEntities(
                    EntityTypeTest.forClass(ItemFrame.class),
                    new AABB(marker).inflate(0.5D),
                    frame -> frame.blockPosition().equals(marker)
                            && building.functionId().equals(
                            BuildingMarkerRegistry.functionId(frame.getItem()))
            ).isEmpty();
            if (!markerPresent) {
                this.buildings.remove(index);
                removed++;
            }
        }

        if (removed > 0) {
            this.setDirty();
        }
        return removed;
    }

    public BuildingScanSummary scanBuildingMarkers(ServerLevel level, BlockPos origin, int radius, long observedAt) {
        removeMissingBuildings(level);
        String dimension = level.dimension().identifier().toString();
        List<BuildingMarkerScanner.MarkerCandidate> candidates = BuildingMarkerScanner.scan(level, origin, radius);
        int updated = 0;
        int bound = 0;
        int valid = 0;
        int invalid = 0;
        Set<WarehouseTerritory> claimedTerritories = new HashSet<>();

        for (BuildingMarkerScanner.MarkerCandidate candidate : candidates) {
            BlockPos position = candidate.position();
            SettlementAdapter settlement = findSettlementForBuilding(
                    dimension,
                    position.getX(),
                    position.getY(),
                    position.getZ());
            String status = settlement == null
                    ? BuildingObservation.STATUS_UNBOUND
                    : BuildingObservation.STATUS_BOUND;
            BuildingMarkerRegistry.MarkerDefinition markerDefinition =
                    BuildingMarkerRegistry.definition(candidate.frame().getItem());
            int capacity = markerDefinition == null ? 0 : markerDefinition.capacity();
            int bedCount = 0;
            BuildingGeometryValidator.ValidationResult validation;
            if (BuildingFunction.RESIDENCE.id().equals(candidate.functionId())) {
                ResidenceValidator.Validation residence = ResidenceValidator.validate(
                        level,
                        candidate.frame(),
                        capacity);
                bedCount = residence.bedCount();
                validation = new BuildingGeometryValidator.ValidationResult(
                        residence.status(),
                        residence.reason(),
                        residence.bedCount(),
                        residence.capacity(),
                        residence.bedCount());
            } else {
                validation = BuildingGeometryValidator.validate(
                        level,
                        candidate.frame(),
                        candidate.functionId());
            }
            boolean duplicateTerritory = validation.isValid()
                    && WarehouseTerritory.read(candidate.frame().getItem())
                    .map(territory -> !claimedTerritories.add(territory))
                    .orElse(false);
            if (duplicateTerritory) {
                validation = new BuildingGeometryValidator.ValidationResult(
                        BuildingObservation.VALIDATION_INVALID,
                        BuildingObservation.VALIDATION_REASON_DUPLICATE_TERRITORY,
                        0,
                        0,
                        0);
            }
            BuildingMarkerVisualState.apply(candidate.frame(), validation.isValid());
            if (!validation.isValid()) {
                CivilizationMod.LOGGER.info(
                        "Building marker invalid: dimension={}, marker={}, reason={}, air={}, floor={}, ceiling={}",
                        dimension,
                        position,
                        validation.reason(),
                        validation.interiorAirBlocks(),
                        validation.floorSupportBlocks(),
                        validation.ceilingBlocks());
            }
            String settlementDimension = settlement == null ? "" : settlement.dimension();
            int settlementX = settlement == null ? 0 : settlement.centerX();
            int settlementY = settlement == null ? 0 : settlement.centerY();
            int settlementZ = settlement == null ? 0 : settlement.centerZ();
            BuildingStorageSnapshot storageSnapshot = BuildingStorageSnapshot.unscanned();
            if (validation.isValid() && BuildingFunction.WAREHOUSE.id().equals(candidate.functionId())) {
                storageSnapshot = WarehouseTerritory.read(candidate.frame().getItem())
                        .map(territory -> BuildingStorageProvider.scan(level, territory, observedAt))
                        .orElse(BuildingStorageSnapshot.unscanned());
            }

            BuildingObservation existing = findBuilding(dimension, position.getX(), position.getY(), position.getZ());
            BuildingObservation observation;
            if (existing == null) {
                observation = new BuildingObservation(
                        candidate.functionId(),
                        dimension,
                        position.getX(),
                        position.getY(),
                        position.getZ(),
                        status,
                        validation.status(),
                        validation.reason(),
                        observedAt,
                        observedAt,
                        settlementDimension,
                        settlementX,
                        settlementY,
                        settlementZ,
                        capacity,
                        bedCount,
                        "",
                        "",
                        storageSnapshot.scanned() ? storageSnapshot : BuildingStorageSnapshot.unscanned());
            } else {
                observation = existing.refreshed(
                        candidate.functionId(),
                        observedAt,
                        status,
                        validation.status(),
                        validation.reason(),
                        settlementDimension,
                        settlementX,
                        settlementY,
                        settlementZ,
                        capacity,
                        bedCount,
                        storageSnapshot.scanned()
                                ? storageSnapshot
                                : existing.storageSnapshot());
                if (storageSnapshot.scanned()) {
                    observation = observation.withStorageSnapshot(storageSnapshot);
                }
            }

            if (existing == null || !existing.equals(observation)) {
                if (existing == null) {
                    this.buildings.add(observation);
                } else {
                    this.buildings.set(this.buildings.indexOf(existing), observation);
                }
                updated++;
            }
            if (settlement != null) {
                bound++;
            }
            if (validation.isValid()) {
                valid++;
            } else {
                invalid++;
            }
        }

        if (updated > 0) {
            this.setDirty();
        }
        return new BuildingScanSummary(candidates.size(), updated, bound, valid, invalid);
    }

    SettlementAdapter findSettlementForBuilding(String dimension, int markerX, int markerY, int markerZ) {
        return findSettlementForMarker(dimension, new BlockPos(markerX, markerY, markerZ));
    }

    private SettlementAdapter findSettlementForMarker(String dimension, BlockPos markerPosition) {
        SettlementAdapter nearest = null;
        long nearestDistance = Long.MAX_VALUE;
        for (SettlementAdapter settlement : this.settlements) {
            if (!settlement.dimension().equals(dimension)) {
                continue;
            }
            long deltaX = (long) settlement.centerX() - markerPosition.getX();
            long deltaY = (long) settlement.centerY() - markerPosition.getY();
            long deltaZ = (long) settlement.centerZ() - markerPosition.getZ();
            if (Math.abs(deltaX) > BUILDING_BIND_HORIZONTAL_RADIUS
                    || Math.abs(deltaY) > BUILDING_BIND_VERTICAL_RADIUS
                    || Math.abs(deltaZ) > BUILDING_BIND_HORIZONTAL_RADIUS) {
                continue;
            }
            long distance = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
            if (distance < nearestDistance) {
                nearest = settlement;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private int removeDuplicateSettlements() {
        int removed = 0;
        for (int index = this.settlements.size() - 1; index >= 0; index--) {
            SettlementAdapter candidate = this.settlements.get(index);
            boolean duplicate = false;
            for (int previous = 0; previous < index; previous++) {
                if (this.settlements.get(previous).isSameSettlement(candidate)) {
                    duplicate = true;
                    break;
                }
            }
            if (duplicate) {
                this.settlements.remove(index);
                removed++;
            }
        }
        if (removed > 0) {
            this.setDirty();
        }
        return removed;
    }

    private int removeDuplicateBuildings() {
        int removed = 0;
        for (int index = this.buildings.size() - 1; index >= 0; index--) {
            BuildingObservation candidate = this.buildings.get(index);
            boolean duplicate = false;
            for (int previous = 0; previous < index; previous++) {
                if (this.buildings.get(previous).isSameMarker(
                        candidate.dimension(),
                        candidate.markerX(),
                        candidate.markerY(),
                        candidate.markerZ()
                )) {
                    duplicate = true;
                    break;
                }
            }
            if (duplicate) {
                this.buildings.remove(index);
                removed++;
            }
        }
        if (removed > 0) {
            this.setDirty();
        }
        return removed;
    }

    public FoodSimulationSummary advanceSimulation() {
        int totalPopulation = 0;
        long totalDemand = 0L;
        long totalConsumed = 0L;
        long totalShortage = 0L;
        long totalFoodStock = 0L;
        int totalStabilityDebt = 0;
        int totalStability = 0;
        String aggregatedFoodEvent = FoodDemandModel.EVENT_STABLE;

        for (int index = 0; index < this.settlements.size(); index++) {
            FoodDemandModel.Result result = FoodDemandModel.simulate(this.settlements.get(index));
            this.settlements.set(index, result.settlement());
            totalPopulation += result.settlement().population();
            totalDemand += result.demand();
            totalConsumed += result.consumed();
            totalShortage += result.shortage();
            totalFoodStock += result.settlement().foodStock();
            totalStabilityDebt += result.settlement().stabilityDebt();
            totalStability += result.settlement().stability();
            aggregatedFoodEvent = aggregateFoodEvent(aggregatedFoodEvent, result.settlement().lastFoodEvent());
        }

        this.simulationSteps++;
        this.setDirty();
        return new FoodSimulationSummary(
                this.simulationSteps,
                this.getSettlementCount(),
                totalPopulation,
                totalDemand,
                totalConsumed,
                totalShortage,
                totalFoodStock,
                totalStabilityDebt,
                averageStability(totalStability),
                aggregatedFoodEvent
        );
    }

    public FoodSimulationSummary getFoodSummary() {
        int totalPopulation = 0;
        long totalFoodStock = 0L;
        int totalStabilityDebt = 0;
        int totalStability = 0;
        String aggregatedFoodEvent = FoodDemandModel.EVENT_STABLE;
        for (SettlementAdapter settlement : this.settlements) {
            totalPopulation += settlement.population();
            totalFoodStock += settlement.foodStock();
            totalStabilityDebt += settlement.stabilityDebt();
            totalStability += settlement.stability();
            aggregatedFoodEvent = aggregateFoodEvent(aggregatedFoodEvent, settlement.lastFoodEvent());
        }

        return new FoodSimulationSummary(
                this.simulationSteps,
                this.getSettlementCount(),
                totalPopulation,
                0L,
                0L,
                0L,
                totalFoodStock,
                totalStabilityDebt,
                averageStability(totalStability),
                aggregatedFoodEvent
        );
    }

    private int averageStability(int totalStability) {
        return this.settlements.isEmpty() ? 0 : totalStability / this.settlements.size();
    }

    private static String aggregateFoodEvent(String current, String next) {
        if (FoodDemandModel.EVENT_SHORTAGE.equals(current) || FoodDemandModel.EVENT_SHORTAGE.equals(next)) {
            return FoodDemandModel.EVENT_SHORTAGE;
        }
        if (FoodDemandModel.EVENT_SURPLUS.equals(current) || FoodDemandModel.EVENT_SURPLUS.equals(next)) {
            return FoodDemandModel.EVENT_SURPLUS;
        }
        return FoodDemandModel.EVENT_STABLE;
    }
}

