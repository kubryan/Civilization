package com.civilizationmod;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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
        BuildingStorageSnapshot storageSnapshot
) {
    public static final String STATUS_BOUND = "bound";
    public static final String STATUS_UNBOUND = "unbound";

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
    public static final String VALIDATION_REASON_MARKER_NOT_AT_DOOR = "marker_not_at_door";
    public static final String VALIDATION_REASON_MARKER_AMBIGUOUS = "marker_ambiguous";
    public static final String VALIDATION_REASON_SCAN_LIMIT = "scan_limit";
    public static final String VALIDATION_REASON_TERRITORY_MISSING = "territory_missing";
    public static final String VALIDATION_REASON_TERRITORY_TOO_LARGE = "territory_too_large";
    public static final String VALIDATION_REASON_TERRITORY_WRONG_DIMENSION = "territory_wrong_dimension";
    public static final String VALIDATION_REASON_MARKER_OUTSIDE_TERRITORY = "marker_outside_territory";
    public static final String VALIDATION_REASON_MARKER_NOT_ATTACHED = "marker_not_attached";
    public static final String VALIDATION_REASON_DUPLICATE_TERRITORY = "duplicate_territory";

    private record CodecTail(
            int capacity,
            int bedCount,
            String residentName,
            BuildingStorageSnapshot storageSnapshot
    ) {
    }

    private static final MapCodec<CodecTail> CODEC_TAIL = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("capacity", 0).forGetter(CodecTail::capacity),
            Codec.INT.optionalFieldOf("bed_count", 0).forGetter(CodecTail::bedCount),
            Codec.STRING.optionalFieldOf("resident_name", "").forGetter(CodecTail::residentName),
            BuildingStorageSnapshot.CODEC.optionalFieldOf("storage", BuildingStorageSnapshot.unscanned())
                    .forGetter(CodecTail::storageSnapshot)
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
                    observation.storageSnapshot()))
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
            tail.storageSnapshot())));

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
                BuildingStorageSnapshot.unscanned());
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
                BuildingStorageSnapshot.unscanned());
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
        storageSnapshot = storageSnapshot == null
                ? BuildingStorageSnapshot.unscanned()
                : storageSnapshot;
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
                newStorageSnapshot);
    }

    public boolean hasResident() {
        return residentUuidValue().isPresent();
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

    public BuildingObservation withResident(UUID uuid, String name) {
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
                uuid == null ? "" : uuid.toString(),
                name == null ? "" : name,
                this.storageSnapshot);
    }

    public BuildingObservation withoutResident() {
        return withResident(null, "");
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
                snapshot);
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

