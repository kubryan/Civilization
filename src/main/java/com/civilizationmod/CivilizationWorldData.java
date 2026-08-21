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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Persistent world-level state for the civilization simulation.
 *
 * <p>The world owns the simulation clock, registered settlements, and
 * server-observed building markers. Detailed calculations stay in pure model
 * classes so the same rules can later be tested independently of a running
 * client.</p>
 */
public final class CivilizationWorldData extends SavedData {
        private static final int CURRENT_SCHEMA_VERSION = 8;

    private static final int BUILDING_BIND_HORIZONTAL_RADIUS = 128;
    private static final int BUILDING_BIND_VERTICAL_RADIUS = 64;

    private static final Codec<CivilizationWorldData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("schema_version").forGetter(CivilizationWorldData::getSchemaVersion),
            Codec.LONG.fieldOf("simulation_steps").forGetter(CivilizationWorldData::getSimulationSteps),
            Codec.INT.fieldOf("settlement_count").forGetter(CivilizationWorldData::getSettlementCount),
                        SettlementAdapter.CODEC.listOf().optionalFieldOf("settlements", List.of()).forGetter(CivilizationWorldData::getSettlements),
            BuildingObservation.CODEC.listOf().optionalFieldOf("buildings", List.of()).forGetter(CivilizationWorldData::getBuildings),
            TownHallCore.CODEC.listOf().optionalFieldOf("town_halls", List.of()).forGetter(CivilizationWorldData::getTownHallCores),
            ResidentRegistry.CODEC.optionalFieldOf("resident_registry", new ResidentRegistry())
                    .forGetter(CivilizationWorldData::getResidentRegistry)

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
    private final List<TownHallCore> townHallCores;
    private final ResidentRegistry residentRegistry;

    public CivilizationWorldData() {
        this(CURRENT_SCHEMA_VERSION, 0L, 0, List.of(), List.of(), List.of(), new ResidentRegistry());
    }

    private CivilizationWorldData(
            int schemaVersion,
            long simulationSteps,
                        int ignoredSettlementCount,
            List<SettlementAdapter> settlements,
            List<BuildingObservation> buildings,
            List<TownHallCore> townHallCores,
            ResidentRegistry residentRegistry
    ) {

        this.schemaVersion = Math.max(schemaVersion, CURRENT_SCHEMA_VERSION);
        this.simulationSteps = Math.max(0L, simulationSteps);
        this.settlements = new ArrayList<>(settlements);
        this.buildings = new ArrayList<>(buildings);
        this.townHallCores = new ArrayList<>(townHallCores);
        this.residentRegistry = residentRegistry == null ? new ResidentRegistry() : residentRegistry;
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
        data.removeDuplicateTownHallCores();
        data.migrateLegacyResidents();
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

    public boolean addBuildingObservation(BuildingObservation observation) {
        if (observation == null || findBuilding(
                observation.dimension(),
                observation.markerX(),
                observation.markerY(),
                observation.markerZ()) != null) {
            return false;
        }
        this.buildings.add(observation);
        this.residentRegistry.migrateLegacy(List.of(observation));
        this.setDirty();
        return true;
    }

        public BuildingObservation getBuilding(int oneBasedIndex) {

        if (oneBasedIndex < 1 || oneBasedIndex > this.buildings.size()) {
            return null;
        }
        return this.buildings.get(oneBasedIndex - 1);
    }

    public List<TownHallCore> getTownHallCores() {
        return List.copyOf(this.townHallCores);
    }

    public int getTownHallCoreCount() {
        return this.townHallCores.size();
    }

    public ResidentRegistry getResidentRegistry() {
        return this.residentRegistry;
    }

    public List<ResidentRecord> getResidents() {
        return this.residentRegistry.getResidents();
    }

    private int migrateLegacyResidents() {
        int added = this.residentRegistry.migrateLegacy(this.buildings);
        if (added > 0) {
            this.setDirty();
        }
        return added;
    }

    public TownHallCore getTownHallCore(String dimension) {
        if (dimension == null || dimension.isBlank()) {
            return null;
        }
        return this.townHallCores.stream()
                .filter(core -> dimension.equals(core.dimension()))
                .findFirst()
                .orElse(null);
    }

    public TownHallCore getTownHallCore(int oneBasedIndex) {
        if (oneBasedIndex < 1 || oneBasedIndex > this.townHallCores.size()) {
            return null;
        }
        return this.townHallCores.get(oneBasedIndex - 1);
    }

    public List<TownHallCore> getTownHallCores(String dimension) {
        if (dimension == null || dimension.isBlank()) {
            return List.of();
        }
        return this.townHallCores.stream()
                .filter(core -> dimension.equals(core.dimension()))
                .toList();
    }

    public TownHallRegistration registerTownHall(
            String dimension,
            BlockPos markerPosition,
            long observedAt
    ) {
        return registerTownHall(dimension, markerPosition, observedAt, TownHallCore.DEFAULT_RADIUS);
    }

    public TownHallRegistration registerTownHall(
            String dimension,
            BlockPos markerPosition,
            long observedAt,
            int radius
    ) {
        if (dimension == null || dimension.isBlank() || markerPosition == null) {
            return new TownHallRegistration(TownHallRegistrationStatus.INVALID, null);
        }

        TownHallCore existingAtMarker = this.townHallCores.stream()
                .filter(core -> core.isSameMarker(dimension, markerPosition))
                .findFirst()
                .orElse(null);
        if (existingAtMarker != null) {
            return new TownHallRegistration(TownHallRegistrationStatus.EXISTING, existingAtMarker);
        }

        TownHallCore candidate = TownHallCore.create(dimension, markerPosition, observedAt)
                .withRadius(radius);
        for (TownHallCore existing : this.townHallCores) {
            if (candidate.overlaps(existing)) {
                return new TownHallRegistration(TownHallRegistrationStatus.DUPLICATE, existing);
            }
        }

        this.townHallCores.add(candidate);
        this.refreshColonyBindings();
        this.setDirty();
        return new TownHallRegistration(TownHallRegistrationStatus.REGISTERED, candidate);
    }

    public TownHallRadiusUpdate updateTownHallRadius(int oneBasedIndex, int radius) {
        TownHallCore current = getTownHallCore(oneBasedIndex);
        if (current == null || radius < 1 || radius > TownHallCore.MAX_RADIUS) {
            return new TownHallRadiusUpdate(TownHallRadiusUpdateStatus.INVALID, current);
        }
        TownHallCore candidate = current.withRadius(radius);
        if (candidate.equals(current)) {
            return new TownHallRadiusUpdate(TownHallRadiusUpdateStatus.UNCHANGED, current);
        }
        for (TownHallCore other : this.townHallCores) {
            if (other != current && candidate.overlaps(other)) {
                return new TownHallRadiusUpdate(TownHallRadiusUpdateStatus.OVERLAPPING, other);
            }
        }
        this.townHallCores.set(oneBasedIndex - 1, candidate);
        this.refreshColonyBindings();
        this.setDirty();
        return new TownHallRadiusUpdate(TownHallRadiusUpdateStatus.UPDATED, candidate);
    }

    private int refreshColonyBindings() {
        int changed = 0;
        for (int index = 0; index < this.buildings.size(); index++) {
            BuildingObservation building = this.buildings.get(index);
            TownHallBinding binding = findTownHallBinding(
                    building.dimension(),
                    new BlockPos(building.markerX(), building.markerY(), building.markerZ()));
            TownHallCore core = binding.core();
            String reason;
            if (!BuildingObservation.VALIDATION_VALID.equals(building.validationStatus())) {
                core = null;
                reason = BuildingObservation.COLONY_REASON_BUILDING_INVALID;
            } else {
                reason = switch (binding.status()) {
                    case BOUND -> BuildingObservation.COLONY_REASON_BOUND;
                    case OUTSIDE -> BuildingObservation.COLONY_REASON_OUTSIDE_TOWN_HALL;
                    case OVERLAPPING -> BuildingObservation.COLONY_REASON_OVERLAPPING_TOWN_HALL;
                    case NO_TOWN_HALL -> BuildingObservation.COLONY_REASON_NO_TOWN_HALL;
                };
            }
            String status = core == null
                    ? BuildingObservation.STATUS_UNBOUND
                    : BuildingObservation.STATUS_BOUND;
            String colonyId = core == null ? "" : core.colonyId();
            BuildingObservation replacement = building.withColonyBinding(status, colonyId, reason);
            if (!building.equals(replacement)) {
                this.buildings.set(index, replacement);
                changed++;
            }
        }
        if (changed > 0) {
            this.setDirty();
        }
        return changed;
    }

    public TownHallBinding findTownHallBinding(String dimension, BlockPos position) {
        if (dimension == null || dimension.isBlank() || position == null) {
            return new TownHallBinding(TownHallBindingStatus.NO_TOWN_HALL, null);
        }
        List<TownHallCore> containing = this.townHallCores.stream()
                .filter(core -> dimension.equals(core.dimension()) && core.contains(position))
                .toList();
        if (containing.size() > 1) {
            return new TownHallBinding(TownHallBindingStatus.OVERLAPPING, null);
        }
        if (containing.size() == 1) {
            return new TownHallBinding(TownHallBindingStatus.BOUND, containing.get(0));
        }
        boolean hasSameDimension = this.townHallCores.stream()
                .anyMatch(core -> dimension.equals(core.dimension()));
        return new TownHallBinding(
                hasSameDimension ? TownHallBindingStatus.OUTSIDE : TownHallBindingStatus.NO_TOWN_HALL,
                null);
    }

    public record TownHallRegistration(TownHallRegistrationStatus status, TownHallCore core) {
    }

    public enum TownHallRegistrationStatus {
        REGISTERED,
        EXISTING,
        DUPLICATE,
        INVALID
    }

    public record TownHallRadiusUpdate(TownHallRadiusUpdateStatus status, TownHallCore core) {
    }

    public enum TownHallRadiusUpdateStatus {
        UPDATED,
        UNCHANGED,
        OVERLAPPING,
        INVALID
    }

    public record TownHallBinding(TownHallBindingStatus status, TownHallCore core) {
        public boolean isBound() {
            return status == TownHallBindingStatus.BOUND && core != null;
        }
    }

    public enum TownHallBindingStatus {
        BOUND,
        NO_TOWN_HALL,
        OUTSIDE,
        OVERLAPPING
    }



        public BuildingObservation findBuilding(String dimension, int x, int y, int z) {
        return this.buildings.stream()
                .filter(existing -> existing.isSameMarker(dimension, x, y, z))
                .findFirst()
                .orElse(null);
    }

    /** Finds the loaded ItemFrame that currently backs an observed building. */
    public ItemFrame findBuildingMarker(ServerLevel level, BuildingObservation building) {
        if (level == null || building == null
                || !level.dimension().identifier().toString().equals(building.dimension())) {
            return null;
        }
        BlockPos marker = new BlockPos(building.markerX(), building.markerY(), building.markerZ());
        if (!level.isLoaded(marker)) {
            return null;
        }
        return level.getEntities(
                        EntityTypeTest.forClass(ItemFrame.class),
                        new AABB(marker).inflate(0.5D),
                        frame -> frame.blockPosition().equals(marker)
                                && building.functionId().equals(
                                BuildingMarkerRegistry.functionId(frame.getItem())))
                .stream()
                .findFirst()
                .orElse(null);
    }

    public BuildingObservation findBuildingAssignedTo(String residentUuid) {
        if (residentUuid == null || residentUuid.isBlank()) {
            return null;
        }
        ResidentRecord resident = this.residentRegistry.findByEntityUuid(residentUuid);
        if (resident == null || !resident.isActive()) {
            return null;
        }
        return findBuildingByKey(resident.assignedBuildingKey());
    }

    public BuildingObservation findBuildingAssignedToResidentId(String residentId) {
        ResidentRecord resident = this.residentRegistry.findByResidentId(residentId);
        return resident == null || !resident.isActive()
                ? null
                : findBuildingByKey(resident.assignedBuildingKey());
    }

    public BuildingObservation findBuildingByKey(String serializedKey) {
        return ResidentRecord.BuildingKey.parse(serializedKey)
                .map(key -> findBuilding(
                        key.dimension(),
                        key.markerX(),
                        key.markerY(),
                        key.markerZ()))
                .orElse(null);
    }

    public ResidentRecord ensureResidentAssignment(
            BuildingObservation building,
            java.util.UUID entityUuid,
            String name,
            long observedAt
    ) {
        if (building == null || entityUuid == null) {
            return null;
        }
        String role = BuildingFunction.RESIDENCE.id().equals(building.functionId())
                ? ResidentRecord.ROLE_RESIDENT
                : ResidentRecord.ROLE_WAREHOUSE_WORKER;
        ResidentRecord.BuildingKey targetKey = ResidentRecord.BuildingKey.from(building);
        ResidentRecord current = this.residentRegistry.findByEntityUuid(entityUuid);
        if (current != null
                && current.isActive()
                && !current.assignedBuildingKey().isBlank()
                && !current.assignedBuildingKey().equals(targetKey.serialize())) {
            return null;
        }
        ResidentRecord replacement = current == null
                ? ResidentRegistry.createNew(entityUuid, building, role, name, observedAt)
                : current.withAssignment(
                        targetKey,
                        building.colonyId(),
                        role,
                        observedAt);
        if (replacement == null || !this.residentRegistry.upsert(replacement)) {
            return null;
        }
        this.setDirty();
        return replacement;
    }

    public int countActiveResidents(BuildingObservation building) {
        if (building == null) {
            return 0;
        }
        return this.residentRegistry.countActiveAssignedTo(
                ResidentRecord.BuildingKey.from(building).serialize());
    }

    public List<ResidentRecord> findActiveResidents(BuildingObservation building) {
        if (building == null) {
            return List.of();
        }
        return this.residentRegistry.findActiveAssignedTo(
                ResidentRecord.BuildingKey.from(building).serialize());
    }

    public boolean markResidentDead(UUID entityUuid, long observedAt) {
        if (entityUuid == null) {
            return false;
        }
        ResidentRecord current = this.residentRegistry.findByEntityUuid(entityUuid);
        if (current == null || !current.isActive()) {
            return false;
        }

        ResidentRecord dead = current.withDeath(observedAt);
        if (!this.residentRegistry.upsert(dead)) {
            return false;
        }

        this.setDirty();
        return !dead.equals(current);
    }

    public boolean clearResidentAssignment(java.util.UUID entityUuid, long observedAt) {
        if (entityUuid == null) {
            return false;
        }
        ResidentRecord current = this.residentRegistry.findByEntityUuid(entityUuid);
        if (current == null) {
            return false;
        }
        ResidentRecord replacement = current.withClearedAssignment(observedAt);
        if (!this.residentRegistry.upsert(replacement)) {
            return false;
        }
        this.setDirty();
        return true;
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
        this.residentRegistry.clearAssignmentsForBuilding(
                ResidentRecord.BuildingKey.from(building).serialize(),
                building.lastSeen());
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
                this.residentRegistry.clearAssignmentsForBuilding(
                        ResidentRecord.BuildingKey.from(building).serialize(),
                        building.lastSeen());
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
        removeMissingTownHallCores(level);

        String dimension = level.dimension().identifier().toString();
        List<BuildingMarkerScanner.MarkerCandidate> candidates = BuildingMarkerScanner.scan(level, origin, radius);
        int updated = 0;
        int bound = 0;
        int valid = 0;
        int invalid = 0;
                int townHallsRegistered = 0;
        int townHallConflicts = 0;
        Set<WarehouseTerritory> claimedTerritories = new HashSet<>();
        Map<String, TownHallRegistration> townHallRegistrations = new HashMap<>();

        // Register all valid Town Halls first so buildings found earlier in the
        // scanner order can still bind to a core found later in the same scan.
        for (BuildingMarkerScanner.MarkerCandidate candidate : candidates) {
            if (!BuildingFunction.TOWN_HALL.id().equals(candidate.functionId())) {
                continue;
            }
            BuildingGeometryValidator.ValidationResult townHallValidation = BuildingGeometryValidator.validate(
                    level,
                    candidate.frame(),
                    candidate.functionId());
            if (!townHallValidation.isValid()) {
                continue;
            }
            BlockPos position = candidate.position();
            TownHallRegistration registration = registerTownHall(dimension, position, observedAt);
            townHallRegistrations.put(buildingKey(dimension, position), registration);
            if (registration.status() == TownHallRegistrationStatus.REGISTERED) {
                townHallsRegistered++;
            } else if (registration.status() == TownHallRegistrationStatus.DUPLICATE) {
                townHallConflicts++;
            }
        }

        for (BuildingMarkerScanner.MarkerCandidate candidate : candidates) {

            BlockPos position = candidate.position();
                        SettlementAdapter settlement = findSettlementForBuilding(
                    dimension,
                    position.getX(),
                    position.getY(),
                    position.getZ());
            TownHallBinding townHallBinding = findTownHallBinding(dimension, position);
            TownHallRegistration townHallRegistration = townHallRegistrations.get(buildingKey(dimension, position));
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

            boolean townHallConflict = BuildingFunction.TOWN_HALL.id().equals(candidate.functionId())
                    && townHallRegistration != null
                    && townHallRegistration.status() == TownHallRegistrationStatus.DUPLICATE;
            if (townHallConflict) {
                validation = new BuildingGeometryValidator.ValidationResult(
                        BuildingObservation.VALIDATION_INVALID,
                        BuildingObservation.VALIDATION_REASON_DUPLICATE_TOWN_HALL,
                        0,
                        0,
                        0);
            }

            TownHallCore colonyCore = null;
            String colonyReason;
            if (townHallConflict) {
                colonyReason = BuildingObservation.COLONY_REASON_TOWN_HALL_CONFLICT;
            } else if (!validation.isValid()) {
                colonyReason = BuildingObservation.COLONY_REASON_BUILDING_INVALID;
            } else if (BuildingFunction.TOWN_HALL.id().equals(candidate.functionId())) {
                colonyCore = townHallRegistration == null ? null : townHallRegistration.core();
                colonyReason = colonyCore == null
                        ? BuildingObservation.COLONY_REASON_TOWN_HALL_CONFLICT
                        : BuildingObservation.COLONY_REASON_BOUND;
            } else {
                colonyCore = townHallBinding.core();
                colonyReason = switch (townHallBinding.status()) {
                    case BOUND -> BuildingObservation.COLONY_REASON_BOUND;
                    case OUTSIDE -> BuildingObservation.COLONY_REASON_OUTSIDE_TOWN_HALL;
                    case OVERLAPPING -> BuildingObservation.COLONY_REASON_OVERLAPPING_TOWN_HALL;
                    case NO_TOWN_HALL -> BuildingObservation.COLONY_REASON_NO_TOWN_HALL;
                };
            }
            String status = colonyCore == null
                    ? BuildingObservation.STATUS_UNBOUND
                    : BuildingObservation.STATUS_BOUND;
            String colonyId = colonyCore == null ? "" : colonyCore.colonyId();
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
                        storageSnapshot.scanned() ? storageSnapshot : BuildingStorageSnapshot.unscanned(),
                        List.of(),
                        colonyId,
                        colonyReason);

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

            observation = observation.withColonyBinding(status, colonyId, colonyReason);

            if (BuildingFunction.RESIDENCE.id().equals(candidate.functionId())
                    && countActiveResidents(observation) > capacity
                    && BuildingObservation.VALIDATION_VALID.equals(observation.validationStatus())) {
                observation = observation.withValidation(
                        BuildingObservation.VALIDATION_INVALID,
                        BuildingObservation.VALIDATION_REASON_RESIDENTS_OVER_CAPACITY)
                        .withColonyBinding(
                                BuildingObservation.STATUS_UNBOUND,
                                "",
                                BuildingObservation.COLONY_REASON_BUILDING_INVALID);
            }
            BuildingMarkerVisualState.apply(
                    candidate.frame(),
                    BuildingObservation.VALIDATION_VALID.equals(observation.validationStatus()));
            if (!BuildingObservation.VALIDATION_VALID.equals(observation.validationStatus())) {
                CivilizationMod.LOGGER.info(
                        "Building marker invalid: dimension={}, marker={}, reason={}, air={}, floor={}, ceiling={}",
                        dimension,
                        position,
                        observation.validationReason(),
                        validation.interiorAirBlocks(),
                        validation.floorSupportBlocks(),
                        validation.ceilingBlocks());
            }

            if (existing == null || !existing.equals(observation)) {

                if (existing == null) {
                    this.buildings.add(observation);
                } else {
                    this.buildings.set(this.buildings.indexOf(existing), observation);
                }
                updated++;
            }
                        if (observation.isColonyBound()) {
                bound++;
            }

            if (BuildingObservation.VALIDATION_VALID.equals(observation.validationStatus())) {
                valid++;
            } else {
                invalid++;
            }
        }

        if (updated > 0) {
            this.setDirty();
        }
                return new BuildingScanSummary(
                candidates.size(),
                updated,
                bound,
                valid,
                invalid,
                townHallsRegistered,
                townHallConflicts);

    }

        private static String buildingKey(String dimension, BlockPos position) {
        return dimension + "@" + position.getX() + "," + position.getY() + "," + position.getZ();
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

        private int removeMissingTownHallCores(ServerLevel level) {
        if (level == null) {
            return 0;
        }

        String dimension = level.dimension().identifier().toString();
        int removed = 0;
        for (int index = this.townHallCores.size() - 1; index >= 0; index--) {
            TownHallCore core = this.townHallCores.get(index);
            if (!dimension.equals(core.dimension())) {
                continue;
            }

            BlockPos marker = new BlockPos(core.markerX(), core.markerY(), core.markerZ());
            if (!level.isLoaded(marker)) {
                continue;
            }

            boolean markerPresent = !level.getEntities(
                    EntityTypeTest.forClass(ItemFrame.class),
                    new AABB(marker).inflate(0.5D),
                    frame -> frame.blockPosition().equals(marker)
                            && BuildingFunction.TOWN_HALL.id().equals(
                            BuildingMarkerRegistry.functionId(frame.getItem()))
            ).isEmpty();
            if (!markerPresent) {
                this.townHallCores.remove(index);
                removed++;
            }
        }

        if (removed > 0) {
            this.refreshColonyBindings();
            this.setDirty();
        }
        return removed;
    }

    private int removeDuplicateTownHallCores() {
        Set<String> markers = new HashSet<>();
        int removed = 0;
        for (int index = this.townHallCores.size() - 1; index >= 0; index--) {
            TownHallCore core = this.townHallCores.get(index);
            String markerKey = core.dimension() + "@" + core.markerX() + "," + core.markerY() + "," + core.markerZ();
            if (!markers.add(markerKey)) {
                this.townHallCores.remove(index);
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

