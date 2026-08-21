package com.civilizationmod;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;
import java.util.UUID;

/**
 * Persistent logical identity for one Civitas resident.
 *
 * <p>{@code residentId} remains stable for the logical person. {@code entityUuid}
 * identifies the currently represented Minecraft body and may change when a body
 * is rebound in a later implementation. The first migration intentionally uses
 * the legacy villager UUID for both values.</p>
 */
public record ResidentRecord(
        String residentId,
        String entityUuid,
        String colonyId,
        String homeBuildingKey,
        String workBuildingKey,
        String role,
        String bodyType,
        String lifecycle,
        String name,
        long createdAt,
        long lastSeen
) {
    public static final String BODY_TYPE_VANILLA_VILLAGER = "vanilla_villager";
    public static final String LIFECYCLE_ACTIVE = "active";
    public static final String LIFECYCLE_DEAD = "dead";
    public static final String LIFECYCLE_REMOVED = "removed";
    public static final String ROLE_RESIDENT = "resident";
    public static final String ROLE_WAREHOUSE_WORKER = "warehouse_worker";

    public static final Codec<ResidentRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("resident_id").forGetter(ResidentRecord::residentId),
            Codec.STRING.optionalFieldOf("entity_uuid", "").forGetter(ResidentRecord::entityUuid),
            Codec.STRING.optionalFieldOf("colony_id", "").forGetter(ResidentRecord::colonyId),
            Codec.STRING.optionalFieldOf("home_building_key", "").forGetter(ResidentRecord::homeBuildingKey),
            Codec.STRING.optionalFieldOf("work_building_key", "").forGetter(ResidentRecord::workBuildingKey),
            Codec.STRING.optionalFieldOf("role", "").forGetter(ResidentRecord::role),
            Codec.STRING.optionalFieldOf("body_type", BODY_TYPE_VANILLA_VILLAGER).forGetter(ResidentRecord::bodyType),
            Codec.STRING.optionalFieldOf("lifecycle", LIFECYCLE_ACTIVE).forGetter(ResidentRecord::lifecycle),
            Codec.STRING.optionalFieldOf("name", "").forGetter(ResidentRecord::name),
            Codec.LONG.optionalFieldOf("created_at", 0L).forGetter(ResidentRecord::createdAt),
            Codec.LONG.optionalFieldOf("last_seen", 0L).forGetter(ResidentRecord::lastSeen)
    ).apply(instance, ResidentRecord::new));

    public ResidentRecord {
        residentId = residentId == null ? "" : residentId;
        entityUuid = entityUuid == null ? "" : entityUuid;
        colonyId = colonyId == null ? "" : colonyId;
        homeBuildingKey = homeBuildingKey == null ? "" : homeBuildingKey;
        workBuildingKey = workBuildingKey == null ? "" : workBuildingKey;
        role = role == null ? "" : role;
        bodyType = bodyType == null || bodyType.isBlank() ? BODY_TYPE_VANILLA_VILLAGER : bodyType;
        lifecycle = lifecycle == null || lifecycle.isBlank() ? LIFECYCLE_ACTIVE : lifecycle;
        name = name == null ? "" : name;
        createdAt = Math.max(0L, createdAt);
        lastSeen = Math.max(createdAt, lastSeen);
    }

    public Optional<UUID> residentIdValue() {
        return parseUuid(residentId);
    }

    public Optional<UUID> entityUuidValue() {
        return parseUuid(entityUuid);
    }

    public boolean hasEntityBody() {
        return !entityUuid.isBlank();
    }

    public boolean isActive() {
        return LIFECYCLE_ACTIVE.equals(lifecycle);
    }

    public boolean isAssignedTo(BuildingKey key) {
        if (key == null) {
            return false;
        }
        String value = key.serialize();
        return value.equals(homeBuildingKey) || value.equals(workBuildingKey);
    }

    public String assignedBuildingKey() {
        if (!workBuildingKey.isBlank()) {
            return workBuildingKey;
        }
        return homeBuildingKey;
    }

    public ResidentRecord withAssignment(
            BuildingKey building,
            String newColonyId,
            String newRole,
            long observedAt
    ) {
        String key = building == null ? "" : building.serialize();
        boolean residence = ROLE_RESIDENT.equals(newRole);
        return new ResidentRecord(
                residentId,
                entityUuid,
                newColonyId,
                residence ? key : homeBuildingKey,
                residence ? workBuildingKey : key,
                newRole,
                bodyType,
                LIFECYCLE_ACTIVE,
                name,
                createdAt,
                Math.max(lastSeen, observedAt));
    }

    public ResidentRecord withClearedAssignment(long observedAt) {
        return new ResidentRecord(
                residentId,
                entityUuid,
                "",
                "",
                "",
                "",
                bodyType,
                lifecycle,
                name,
                createdAt,
                Math.max(lastSeen, observedAt));
    }

    public ResidentRecord withEntityUuid(String newEntityUuid, long observedAt) {
        return new ResidentRecord(
                residentId,
                newEntityUuid,
                colonyId,
                homeBuildingKey,
                workBuildingKey,
                role,
                bodyType,
                lifecycle,
                name,
                createdAt,
                Math.max(lastSeen, observedAt));
    }

    public ResidentRecord withLifecycle(String newLifecycle, long observedAt) {
        return new ResidentRecord(
                residentId,
                entityUuid,
                colonyId,
                homeBuildingKey,
                workBuildingKey,
                role,
                bodyType,
                newLifecycle,
                name,
                createdAt,
                Math.max(lastSeen, observedAt));
    }

    public ResidentRecord withName(String newName, long observedAt) {
        return new ResidentRecord(
                residentId,
                entityUuid,
                colonyId,
                homeBuildingKey,
                workBuildingKey,
                role,
                bodyType,
                lifecycle,
                newName,
                createdAt,
                Math.max(lastSeen, observedAt));
    }

    public static ResidentRecord fromLegacy(
            BuildingObservation.ResidentAssignment assignment,
            BuildingObservation building
    ) {
        if (assignment == null || building == null || assignment.uuidValue().isEmpty()) {
            return null;
        }
        String role = BuildingFunction.RESIDENCE.id().equals(building.functionId())
                ? ROLE_RESIDENT
                : BuildingFunction.WAREHOUSE.id().equals(building.functionId())
                ? ROLE_WAREHOUSE_WORKER
                : building.functionId();
        String key = BuildingKey.from(building).serialize();
        return new ResidentRecord(
                assignment.uuid(),
                assignment.uuid(),
                building.colonyId(),
                ROLE_RESIDENT.equals(role) ? key : "",
                ROLE_RESIDENT.equals(role) ? "" : key,
                role,
                BODY_TYPE_VANILLA_VILLAGER,
                LIFECYCLE_ACTIVE,
                assignment.name(),
                building.firstSeen(),
                building.lastSeen());
    }

    private static Optional<UUID> parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    /** Immutable, scan-index-independent reference to a building marker. */
    public record BuildingKey(String dimension, int markerX, int markerY, int markerZ) {
        public BuildingKey {
            dimension = dimension == null ? "" : dimension;
        }

        public static BuildingKey from(BuildingObservation building) {
            return new BuildingKey(
                    building.dimension(),
                    building.markerX(),
                    building.markerY(),
                    building.markerZ());
        }

        public String serialize() {
            return dimension + "@" + markerX + "," + markerY + "," + markerZ;
        }

        public static Optional<BuildingKey> parse(String value) {
            if (value == null || value.isBlank()) {
                return Optional.empty();
            }
            int separator = value.indexOf('@');
            if (separator <= 0 || separator == value.length() - 1) {
                return Optional.empty();
            }
            String dimension = value.substring(0, separator);
            String[] coordinates = value.substring(separator + 1).split(",", -1);
            if (coordinates.length != 3) {
                return Optional.empty();
            }
            try {
                return Optional.of(new BuildingKey(
                        dimension,
                        Integer.parseInt(coordinates[0]),
                        Integer.parseInt(coordinates[1]),
                        Integer.parseInt(coordinates[2])));
            } catch (NumberFormatException exception) {
                return Optional.empty();
            }
        }
    }
}
