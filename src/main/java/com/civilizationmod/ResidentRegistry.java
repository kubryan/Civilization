package com.civilizationmod;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * World-persistent registry for Civitas logical residents.
 *
 * <p>The list is the only mutable persistence truth. Lookup maps are rebuilt on
 * demand from that list so an index can never silently become a second source of
 * truth after Codec loading.</p>
 */
public final class ResidentRegistry {
    public static final Codec<ResidentRegistry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResidentRecord.CODEC.listOf().optionalFieldOf("residents", List.of())
                    .forGetter(ResidentRegistry::getResidents)
    ).apply(instance, ResidentRegistry::new));

    private final List<ResidentRecord> residents;

    public ResidentRegistry() {
        this(List.of());
    }

    public ResidentRegistry(List<ResidentRecord> residents) {
        this.residents = new ArrayList<>();
        if (residents != null) {
            for (ResidentRecord resident : residents) {
                upsert(resident);
            }
        }
    }

    public List<ResidentRecord> getResidents() {
        return List.copyOf(residents);
    }

    public int size() {
        return residents.size();
    }

    public ResidentRecord findByResidentId(String residentId) {
        if (residentId == null || residentId.isBlank()) {
            return null;
        }
        return residents.stream()
                .filter(resident -> residentId.equals(resident.residentId()))
                .findFirst()
                .orElse(null);
    }

    public ResidentRecord findByEntityUuid(String entityUuid) {
        if (entityUuid == null || entityUuid.isBlank()) {
            return null;
        }
        return residents.stream()
                .filter(resident -> entityUuid.equals(resident.entityUuid()))
                .findFirst()
                .orElse(null);
    }

    public ResidentRecord findByResidentId(UUID residentId) {
        return residentId == null ? null : findByResidentId(residentId.toString());
    }

    public ResidentRecord findByEntityUuid(UUID entityUuid) {
        return entityUuid == null ? null : findByEntityUuid(entityUuid.toString());
    }

    /**
     * Adds or replaces one canonical record. A body UUID may not identify two
     * logical residents at the same time.
     */
    public boolean upsert(ResidentRecord replacement) {
        if (replacement == null
                || replacement.residentId().isBlank()
                || replacement.residentIdValue().isEmpty()) {
            return false;
        }
        ResidentRecord entityOwner = replacement.entityUuid().isBlank()
                ? null
                : findByEntityUuid(replacement.entityUuid());
        if (entityOwner != null && !entityOwner.residentId().equals(replacement.residentId())) {
            return false;
        }

        for (int index = 0; index < residents.size(); index++) {
            if (residents.get(index).residentId().equals(replacement.residentId())) {
                residents.set(index, replacement);
                return true;
            }
        }
        residents.add(replacement);
        return true;
    }

    public boolean removeByResidentId(String residentId) {
        if (residentId == null || residentId.isBlank()) {
            return false;
        }
        return residents.removeIf(resident -> residentId.equals(resident.residentId()));
    }

    /**
     * Migrates legacy resident assignments. Existing canonical records win, so
     * loading the same old world repeatedly is idempotent and does not reset data.
     */
    public int migrateLegacy(List<BuildingObservation> buildings) {
        int added = 0;
        if (buildings == null) {
            return 0;
        }
        for (BuildingObservation building : buildings) {
            if (building == null) {
                continue;
            }
            for (BuildingObservation.ResidentAssignment assignment : building.residents()) {
                ResidentRecord candidate = ResidentRecord.fromLegacy(assignment, building);
                if (candidate == null || findByResidentId(candidate.residentId()) != null) {
                    continue;
                }
                if (upsert(candidate)) {
                    added++;
                }
            }
        }
        return added;
    }

    public int countActiveAssignedTo(String buildingKey) {
        if (buildingKey == null || buildingKey.isBlank()) {
            return 0;
        }
        int count = 0;
        for (ResidentRecord resident : residents) {
            if (resident.isActive() && buildingKey.equals(resident.assignedBuildingKey())) {
                count++;
            }
        }
        return count;
    }

    public List<ResidentRecord> findActiveAssignedTo(String buildingKey) {
        if (buildingKey == null || buildingKey.isBlank()) {
            return List.of();
        }
        return residents.stream()
                .filter(resident -> resident.isActive() && buildingKey.equals(resident.assignedBuildingKey()))
                .toList();
    }

    public int clearAssignmentsForBuilding(String buildingKey, long observedAt) {
        if (buildingKey == null || buildingKey.isBlank()) {
            return 0;
        }
        int cleared = 0;
        for (int index = 0; index < residents.size(); index++) {
            ResidentRecord resident = residents.get(index);
            if (resident.isActive() && buildingKey.equals(resident.assignedBuildingKey())) {
                ResidentRecord replacement = resident.withClearedAssignment(observedAt);
                if (!replacement.equals(resident)) {
                    residents.set(index, replacement);
                    cleared++;
                }
            }
        }
        return cleared;
    }

    public static ResidentRecord createNew(
            UUID entityUuid,
            BuildingObservation building,
            String role,
            String name,
            long observedAt
    ) {
        if (entityUuid == null || building == null) {
            return null;
        }
        UUID residentId = UUID.randomUUID();
        ResidentRecord base = new ResidentRecord(
                residentId.toString(),
                entityUuid.toString(),
                "",
                "",
                "",
                role,
                ResidentRecord.BODY_TYPE_VANILLA_VILLAGER,
                ResidentRecord.LIFECYCLE_ACTIVE,
                name,
                observedAt,
                observedAt);
        return base.withAssignment(
                ResidentRecord.BuildingKey.from(building),
                building.colonyId(),
                role,
                observedAt);
    }
}
