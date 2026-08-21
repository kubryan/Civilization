package com.civilizationmod;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistent server-side observation of a building marker held by an item frame.
 *
 * <p>Binding status, geometry validation, and function-specific measurements are
 * separate concerns. Residence observations use capacity and bedCount; warehouse
 * observations keep both values at zero.</p>
 */
public record BuildingObservation(
        String functionId,
        String dimension,
        int markerX,
        int markerY,
        int markerZ,
        String status,
        String validationStatus,
        String validationReason,
        long firstSeen,
        long lastSeen,
        String settlementDimension,
        int settlementX,
        int settlementY,
        int settlementZ,
        int capacity,
        int bedCount,
        String residentUuid,
        String residentName,
        BuildingStorageSnapshot storageSnapshot,
        List<ResidentAssignment> residents,
        String colonyId,
        String colonyBindingReason
) {
    public static final String STATUS_BOUND = "bound";
    public static final String STATUS_UNBOUND = "unbound";

    public static final String COLONY_REASON_BOUND = "bound";
    public static final String COLONY_REASON_NO_TOWN_HALL = "no_town_hall";
    public static final String COLONY_REASON_OUTSIDE_TOWN_HALL = "outside_town_hall";
    public static final String COLONY_REASON_OVERLAPPING_TOWN_HALL = "overlapping_town_hall";
    public static final String COLONY_REASON_TOWN_HALL_CONFLICT = "town_hall_conflict";
    public static final String COLONY_REASON_BUILDING_INVALID = "building_invalid";

    public static final String VALIDATION_DETECTED = "detected";
    public static final String VALIDATION_VALID = "valid";
    public static final String VALIDATION_INVALID = "invalid";

    public static final String VALIDATION_REASON_UNKNOWN = "unknown";
    public static final String VALIDATION_REASON_VALID = "valid";
    public static final String VALIDATION_REASON_NO_CONTEXT = "no_context";
    public static final String VALIDATION_REASON_NO_DOOR = "no_door";
    public static final String VALIDATION_REASON_UNLOADED = "unloaded";
    public static final String VALIDATION_REASON_NO_ROOM = "no_room";
    public static final String VALIDATION_REASON_NO_FLOOR = "no_floor";
    public static final String VALIDATION_REASON_NO_CEILING = "no_ceiling";
    public static final String VALIDATION_REASON_NO_WALLS = "no_walls";
    public static final String VALIDATION_REASON_NO_ENTRY = "no_entry";
    public static final String VALIDATION_REASON_NO_CONTAINER = "no_container";
    public static final String VALIDATION_REASON_INSUFFICIENT_BEDS = "insufficient_beds";
    public static final String VALIDATION_REASON_RESIDENTS_OVER_CAPACITY = "residents_over_capacity";
    public static final String VALIDATION_REASON_MARKER_NOT_AT_DOOR = "marker_not_at_door";
    public static final String VALIDATION_REASON_MARKER_AMBIGUOUS = "marker_ambiguous";
    public static final String VALIDATION_REASON_SCAN_LIMIT = "scan_limit";
    public static final String VALIDATION_REASON_TERRITORY_MISSING = "territory_missing";
    public static final String VALIDATION_REASON_TERRITORY_TOO_LARGE = "territory_too_large";
    public static final String VALIDATION_REASON_TERRITORY_WRONG_DIMENSION = "territory_wrong_dimension";
    public static final String VALIDATION_REASON_MARKER_OUTSIDE_TERRITORY = "marker_outside_territory";
    public static final String VALIDATION_REASON_MARKER_NOT_ATTACHED = "marker_not_attached";
    public static final String VALIDATION_REASON_DUPLICATE_TERRITORY = "duplicate_territory";
    public static final String VALIDATION_REASON_DUPLICATE_TOWN_HALL = "duplicate_town_hall";

    /** Persisted resident identity; UUID is authoritative and name is diagnostic. */
    public record ResidentAssignment(String uuid, String name) {
        public static final Codec<ResidentAssignment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("uuid").forGetter(ResidentAssignment::uuid),
                Codec.STRING.optionalFieldOf("name", "").forGetter(ResidentAssignment::name)
        ).apply(instance, ResidentAssignment::new));

        public ResidentAssignment {
            uuid = uuid == null ? "" : uuid;
            name = name == null ? "" : name;
        }

        public Optional<UUID> uuidValue() {
            if (uuid.isBlank()) {
                return Optional.empty();
            }
            try {
                return Optional.of(UUID.fromString(uuid));
            } catch (IllegalArgumentException exception) {
                return Optional.empty();
            }
        }

        public String displayName() {
            return name.isBlank() ? uuid : name;
        }
    }

    private record CodecTail(
            int capacity,
            int bedCount,
            String residentName,
            BuildingStorageSnapshot storageSnapshot,
            List<ResidentAssignment> residents,
            String colonyId,
            String colonyBindingReason
    ) {
    }

    private static final MapCodec<CodecTail> CODEC_TAIL = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("capacity", 0).forGetter(CodecTail::capacity),
            Codec.INT.optionalFieldOf("bed_count", 0).forGetter(CodecTail::bedCount),
            Codec.STRING.optionalFieldOf("resident_name", "").forGetter(CodecTail::residentName),
            BuildingStorageSnapshot.CODEC.optionalFieldOf("storage", BuildingStorageSnapshot.unscanned())
                    .forGetter(CodecTail::storageSnapshot),
            ResidentAssignment.CODEC.listOf().optionalFieldOf("residents", List.of())
                    .forGetter(CodecTail::residents),
            Codec.STRING.optionalFieldOf("colony_id", "").forGetter(CodecTail::colonyId),
            Codec.STRING.optionalFieldOf("colony_binding_reason", COLONY_REASON_NO_TOWN_HALL)
                    .forGetter(CodecTail::colonyBindingReason)
    ).apply(instance, CodecTail::new));

    public static final Codec<BuildingObservation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("function_id").forGetter(BuildingObservation::functionId),
            Codec.STRING.fieldOf("dimension").forGetter(BuildingObservation::dimension),
            Codec.INT.fieldOf("marker_x").forGetter(BuildingObservation::markerX),
            Codec.INT.fieldOf("marker_y").forGetter(BuildingObservation::markerY),
            Codec.INT.fieldOf("marker_z").forGetter(BuildingObservation::markerZ),
            Codec.STRING.optionalFieldOf("status", STATUS_UNBOUND).forGetter(BuildingObservation::status),
            Codec.STRING.optionalFieldOf("validation_status", VALIDATION_DETECTED).forGetter(BuildingObservation::validationStatus),
            Codec.STRING.optionalFieldOf("validation_reason", VALIDATION_REASON_UNKNOWN).forGetter(BuildingObservation::validationReason),
            Codec.LONG.optionalFieldOf("first_seen", 0L).forGetter(BuildingObservation::firstSeen),
            Codec.LONG.optionalFieldOf("last_seen", 0L).forGetter(BuildingObservation::lastSeen),
            Codec.STRING.optionalFieldOf("settlement_dimension", "").forGetter(BuildingObservation::settlementDimension),
            Codec.INT.optionalFieldOf("settlement_x", 0).forGetter(BuildingObservation::settlementX),
            Codec.INT.optionalFieldOf("settlement_y", 0).forGetter(BuildingObservation::settlementY),
            Codec.INT.optionalFieldOf("settlement_z", 0).forGetter(BuildingObservation::settlementZ),
            Codec.STRING.optionalFieldOf("resident_uuid", "").forGetter(BuildingObservation::residentUuid),
            CODEC_TAIL.forGetter(observation -> new CodecTail(
                    observation.capacity(),
                    observation.bedCount(),
                    observation.residentName(),
                    observation.storageSnapshot(),
                    observation.residents(),
                    observation.colonyId(),
                    observation.colonyBindingReason()))
    ).apply(instance, (functionId, dimension, markerX, markerY, markerZ, status,
                       validationStatus, validationReason, firstSeen, lastSeen,
                       settlementDimension, settlementX, settlementY, settlementZ,
                       residentUuid, tail) -> new BuildingObservation(
            functionId,
            dimension,
            markerX,
            markerY,
            markerZ,
            status,
            validationStatus,
            validationReason,
            firstSeen,
            lastSeen,
            settlementDimension,
            settlementX,
            settlementY,
            settlementZ,
            tail.capacity(),
            tail.bedCount(),
            residentUuid,
            tail.residentName(),
            tail.storageSnapshot(),
            tail.residents(),
            tail.colonyId(),
            tail.colonyBindingReason())));

    /** Backward-compatible constructor for observations created before geometry validation. */
    public BuildingObservation(
            String functionId,
            String dimension,
            int markerX,
            int markerY,
            int markerZ,
            String status,
            String validationStatus,
            String validationReason,
            long firstSeen,
            long lastSeen,
            String settlementDimension,
            int settlementX,
            int settlementY,
            int settlementZ
    ) {
        this(
                functionId,
                dimension,
                markerX,
                markerY,
                markerZ,
                status,
                validationStatus,
                validationReason,
                firstSeen,
                lastSeen,
                settlementDimension,
                settlementX,
                settlementY,
                settlementZ,
                0,
                0,
                "",
                "",
                BuildingStorageSnapshot.unscanned(),
                List.of(),
                "",
                COLONY_REASON_NO_TOWN_HALL);
    }

    public BuildingObservation(
            String functionId,
            String dimension,
            int markerX,
            int markerY,
            int markerZ,
            String status,
            long firstSeen,
            long lastSeen,
            String settlementDimension,
            int settlementX,
            int settlementY,
            int settlementZ
    ) {
        this(
                functionId,
                dimension,
                markerX,
                markerY,
                markerZ,
                status,
                VALIDATION_DETECTED,
                VALIDATION_REASON_UNKNOWN,
                firstSeen,
                lastSeen,
                settlementDimension,
                settlementX,
                settlementY,
                settlementZ,
                0,
                0,
                "",
                "",
                BuildingStorageSnapshot.unscanned(),
                List.of(),
                "",
                COLONY_REASON_NO_TOWN_HALL);
    }

    public BuildingObservation {
        functionId = functionId == null || functionId.isBlank()
                ? BuildingMarkerRegistry.FUNCTION_UNKNOWN
                : functionId;
        dimension = dimension == null ? "" : dimension;
        status = STATUS_BOUND.equals(status) ? STATUS_BOUND : STATUS_UNBOUND;
        validationStatus = normalizeValidationStatus(validationStatus);
        validationReason = validationReason == null || validationReason.isBlank()
                ? VALIDATION_REASON_UNKNOWN
                : validationReason;
        firstSeen = Math.max(0L, firstSeen);
        lastSeen = Math.max(firstSeen, lastSeen);
        settlementDimension = settlementDimension == null ? "" : settlementDimension;
        capacity = Math.max(0, capacity);
        bedCount = Math.max(0, bedCount);
        residentUuid = residentUuid == null ? "" : residentUuid;
        residentName = residentName == null ? "" : residentName;
        colonyId = colonyId == null ? "" : colonyId;
        colonyBindingReason = colonyBindingReason == null || colonyBindingReason.isBlank()
                ? COLONY_REASON_NO_TOWN_HALL
                : colonyBindingReason;
        storageSnapshot = storageSnapshot == null
                ? BuildingStorageSnapshot.unscanned()
                : storageSnapshot;
        residents = normalizeResidents(residents, residentUuid, residentName);
        if (!residents.isEmpty()) {
            residentUuid = residents.get(0).uuid();
            residentName = residents.get(0).name();
        }
    }

    public boolean isColonyBound() {
        return STATUS_BOUND.equals(status) && !colonyId.isBlank();
    }

    public BuildingObservation withColonyBinding(String newStatus, String newColonyId, String newReason) {
        return new BuildingObservation(
                this.functionId,
                this.dimension,
                this.markerX,
                this.markerY,
                this.markerZ,
                newStatus,
                this.validationStatus,
                this.validationReason,
                this.firstSeen,
                this.lastSeen,
                this.settlementDimension,
                this.settlementX,
                this.settlementY,
                this.settlementZ,
                this.capacity,
                this.bedCount,
                this.residentUuid,
                this.residentName,
                this.storageSnapshot,
                this.residents,
                newColonyId,
                newReason);
    }

    public boolean isSameMarker(String otherDimension, int x, int y, int z) {
        return this.dimension.equals(otherDimension)
                && this.markerX == x
                && this.markerY == y
                && this.markerZ == z;
    }

    public BuildingObservation refreshed(
            String newFunctionId,
            long observedAt,
            String newStatus,
            String newSettlementDimension,
            int newSettlementX,
            int newSettlementY,
            int newSettlementZ
    ) {
        return refreshed(
                newFunctionId,
                observedAt,
                newStatus,
                this.validationStatus,
                this.validationReason,
                newSettlementDimension,
                newSettlementX,
                newSettlementY,
                newSettlementZ,
                this.capacity,
                this.bedCount,
                this.storageSnapshot);
    }

    public BuildingObservation refreshed(
            String newFunctionId,
            long observedAt,
            String newStatus,
            String newValidationStatus,
            String newValidationReason,
            String newSettlementDimension,
            int newSettlementX,
            int newSettlementY,
            int newSettlementZ
    ) {
        return refreshed(
                newFunctionId,
                observedAt,
                newStatus,
                newValidationStatus,
                newValidationReason,
                newSettlementDimension,
                newSettlementX,
                newSettlementY,
                newSettlementZ,
                this.capacity,
                this.bedCount,
                this.storageSnapshot);
    }

    public BuildingObservation refreshed(
            String newFunctionId,
            long observedAt,
            String newStatus,
            String newValidationStatus,
            String newValidationReason,
            String newSettlementDimension,
            int newSettlementX,
            int newSettlementY,
            int newSettlementZ,
            int newCapacity,
            int newBedCount,
            BuildingStorageSnapshot newStorageSnapshot
    ) {
        return new BuildingObservation(
                newFunctionId,
                this.dimension,
                this.markerX,
                this.markerY,
                this.markerZ,
                newStatus,
                newValidationStatus,
                newValidationReason,
                this.firstSeen,
                observedAt,
                newSettlementDimension,
                newSettlementX,
                newSettlementY,
                newSettlementZ,
                newCapacity,
                newBedCount,
                this.residentUuid,
                this.residentName,
                newStorageSnapshot,
                this.residents,
                this.colonyId,
                this.colonyBindingReason);
    }

    public boolean hasResident() {
        return !residents.isEmpty();
    }

    public Optional<UUID> residentUuidValue() {
        if (residentUuid.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(residentUuid));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    public int residentCount() {
        return residents.size();
    }

    public boolean hasResident(UUID uuid) {
        return uuid != null && hasResidentUuid(uuid.toString());
    }

    public boolean hasResidentUuid(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            return false;
        }
        return residents.stream().anyMatch(resident -> uuid.equals(resident.uuid()));
    }

    public BuildingObservation withResident(UUID uuid, String name) {
        return withResidents(uuid == null
                ? List.of()
                : List.of(new ResidentAssignment(uuid.toString(), name)));
    }

    public BuildingObservation withAddedResident(UUID uuid, String name) {
        if (uuid == null || hasResident(uuid)) {
            return this;
        }
        List<ResidentAssignment> updated = new ArrayList<>(this.residents);
        updated.add(new ResidentAssignment(uuid.toString(), name));
        return withResidents(updated);
    }

    public BuildingObservation withValidation(String newValidationStatus, String newValidationReason) {
        return new BuildingObservation(
                this.functionId,
                this.dimension,
                this.markerX,
                this.markerY,
                this.markerZ,
                this.status,
                newValidationStatus,
                newValidationReason,
                this.firstSeen,
                this.lastSeen,
                this.settlementDimension,
                this.settlementX,
                this.settlementY,
                this.settlementZ,
                this.capacity,
                this.bedCount,
                this.residentUuid,
                this.residentName,
                this.storageSnapshot,
                this.residents,
                this.colonyId,
                this.colonyBindingReason);
    }

    public BuildingObservation withResidenceMeasurements(int newCapacity, int newBedCount) {
        return new BuildingObservation(
                this.functionId,
                this.dimension,
                this.markerX,
                this.markerY,
                this.markerZ,
                this.status,
                this.validationStatus,
                this.validationReason,
                this.firstSeen,
                this.lastSeen,
                this.settlementDimension,
                this.settlementX,
                this.settlementY,
                this.settlementZ,
                newCapacity,
                newBedCount,
                this.residentUuid,
                this.residentName,
                this.storageSnapshot,
                this.residents,
                this.colonyId,
                this.colonyBindingReason);
    }

    private BuildingObservation withResidents(List<ResidentAssignment> updatedResidents) {
        String primaryUuid = updatedResidents == null || updatedResidents.isEmpty()
                ? ""
                : updatedResidents.get(0).uuid();
        String primaryName = updatedResidents == null || updatedResidents.isEmpty()
                ? ""
                : updatedResidents.get(0).name();
        return new BuildingObservation(
                this.functionId,
                this.dimension,
                this.markerX,
                this.markerY,
                this.markerZ,
                this.status,
                this.validationStatus,
                this.validationReason,
                this.firstSeen,
                this.lastSeen,
                this.settlementDimension,
                this.settlementX,
                this.settlementY,
                this.settlementZ,
                this.capacity,
                this.bedCount,
                primaryUuid,
                primaryName,
                this.storageSnapshot,
                updatedResidents == null ? List.of() : updatedResidents,
                this.colonyId,
                this.colonyBindingReason);
    }

    public BuildingObservation withoutResident() {
        return withResidents(List.of());
    }

    public BuildingObservation withoutResident(UUID uuid) {
        if (uuid == null) {
            return this;
        }
        String targetUuid = uuid.toString();
        List<ResidentAssignment> remaining = this.residents.stream()
                .filter(resident -> !targetUuid.equals(resident.uuid()))
                .toList();
        return remaining.size() == this.residents.size()
                ? this
                : withResidents(remaining);
    }

    public BuildingObservation withStorageSnapshot(BuildingStorageSnapshot snapshot) {
        return new BuildingObservation(
                this.functionId,
                this.dimension,
                this.markerX,
                this.markerY,
                this.markerZ,
                this.status,
                this.validationStatus,
                this.validationReason,
                this.firstSeen,
                this.lastSeen,
                this.settlementDimension,
                this.settlementX,
                this.settlementY,
                this.settlementZ,
                this.capacity,
                this.bedCount,
                this.residentUuid,
                this.residentName,
                snapshot,
                this.residents,
                this.colonyId,
                this.colonyBindingReason);
    }

    private static List<ResidentAssignment> normalizeResidents(
            List<ResidentAssignment> values,
            String legacyUuid,
            String legacyName
    ) {
        List<ResidentAssignment> normalized = new ArrayList<>();
        addResidentIfValid(normalized, new ResidentAssignment(legacyUuid, legacyName));
        if (values != null) {
            for (ResidentAssignment value : values) {
                addResidentIfValid(normalized, value);
            }
        }
        return List.copyOf(normalized);
    }

    private static void addResidentIfValid(List<ResidentAssignment> residents, ResidentAssignment candidate) {
        if (candidate == null || candidate.uuidValue().isEmpty()) {
            return;
        }
        if (residents.stream().noneMatch(existing -> existing.uuid().equals(candidate.uuid()))) {
            residents.add(candidate);
        }
    }

    private static String normalizeValidationStatus(String value) {
        if (VALIDATION_VALID.equals(value)) {
            return VALIDATION_VALID;
        }
        if (VALIDATION_INVALID.equals(value)) {
            return VALIDATION_INVALID;
        }
        return VALIDATION_DETECTED;
    }
}
