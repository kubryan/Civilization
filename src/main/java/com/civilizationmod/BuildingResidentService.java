package com.civilizationmod;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

/**
 * Server-side bridge between assigned building observations and vanilla villagers.
 *
 * <p>This first colony slice deliberately does not replace villager goals. It only
 * gives an assigned villager a visible role coat and periodically asks vanilla
 * navigation to approach the building marker.</p>
 */
public final class BuildingResidentService {
    private static final int UPDATE_INTERVAL_TICKS = 20;
    private static final double ARRIVAL_DISTANCE_SQUARED = 64.0D;
    private static final double MOVE_SPEED = 0.6D;
    private static final double LOOK_RANGE = 16.0D;
    private static final double MIN_LOOK_DOT = 0.92D;

    private BuildingResidentService() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(BuildingResidentService::onEndServerTick);
        ServerLivingEntityEvents.AFTER_DEATH.register(BuildingResidentService::onAfterDeath);
    }

    private static void onAfterDeath(net.minecraft.world.entity.LivingEntity entity,
                                     net.minecraft.world.damagesource.DamageSource damageSource) {
        if (!(entity instanceof Villager villager)
                || !(entity.level() instanceof ServerLevel level)) {
            return;
        }
        CivilizationWorldData data = CivilizationWorldData.get(level.getServer());
        if (data.markResidentDead(villager.getUUID(), level.getGameTime())) {
            CivilizationMod.LOGGER.info(
                    "Resident body died: entityUuid={}, name={}; assignment released",
                    villager.getUUID(),
                    villager.getName().getString());
        }
    }

        public static Villager findVillagerByUuid(MinecraftServer server, UUID entityUuid) {
        if (server == null || entityUuid == null) {
            return null;
        }
        for (ResourceKey<Level> key : server.levelKeys()) {
            ServerLevel level = server.getLevel(key);
            if (level == null) {
                continue;
            }
            Entity entity = level.getEntityInAnyDimension(entityUuid);
            if (entity instanceof Villager villager && villager.isAlive()) {
                return villager;
            }
        }
        return null;
    }

    public static Villager findLookedAtVillager(ServerLevel level, ServerPlayer player) {

        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 viewVector = player.getViewVector(1.0F);
        double range = LOOK_RANGE;
        AABB searchBounds = new AABB(
                player.getX() - range,
                player.getY() - range,
                player.getZ() - range,
                player.getX() + range,
                player.getY() + range,
                player.getZ() + range);

        Villager nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Villager candidate : level.getEntities(
                EntityTypeTest.forClass(Villager.class),
                searchBounds,
                Villager::isAlive)) {
            Vec3 towardCandidate = candidate.getBoundingBox().getCenter().subtract(eyePosition);
            double distance = towardCandidate.length();
            if (distance > range || distance < 0.001D) {
                continue;
            }
            double alignment = towardCandidate.normalize().dot(viewVector);
            if (alignment < MIN_LOOK_DOT || !player.hasLineOfSight(candidate)) {
                continue;
            }
            if (distance < nearestDistance) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    public static void applyAssignmentVisual(Villager villager, String functionId) {
        BuildingRoleEquipment.apply(villager, functionId);
    }

    private static void onEndServerTick(MinecraftServer server) {
        if (server.getTickCount() % UPDATE_INTERVAL_TICKS != 0) {
            return;
        }

        CivilizationWorldData data = CivilizationWorldData.get(server);
        for (ResourceKey<Level> key : server.levelKeys()) {
            ServerLevel level = server.getLevel(key);
            if (level != null) {
                data.removeMissingBuildings(level);
            }
        }

                for (ResidentRecord resident : data.getResidents()) {
            BuildingObservation building = data.findBuildingByKey(resident.assignedBuildingKey());
            if (building == null) {
                continue;
            }
            boolean buildingValid = data.isBuildingOperational(building);

            ServerLevel buildingLevel = findLevel(server, building.dimension());
            Optional<UUID> residentUuid = resident.entityUuidValue();
            if (buildingLevel == null || residentUuid.isEmpty()) {
                continue;
            }

            Entity entity = buildingLevel.getEntityInAnyDimension(residentUuid.get());
            if (!(entity instanceof Villager villager)
                    || villager.level() != buildingLevel
                    || !villager.isAlive()) {
                continue;
            }

            applyAssignmentVisual(villager, building.functionId());
            if (!buildingValid) {
                continue;
            }
            BlockPos marker = new BlockPos(building.markerX(), building.markerY(), building.markerZ());
            if (villager.distanceToSqr(marker.getX() + 0.5D, marker.getY(), marker.getZ() + 0.5D)
                    > ARRIVAL_DISTANCE_SQUARED) {
                villager.getNavigation().moveTo(
                        marker.getX() + 0.5D,
                        marker.getY(),
                        marker.getZ() + 0.5D,
                        MOVE_SPEED);
            } else if (BuildingFunction.WAREHOUSE.id().equals(building.functionId())) {
                int buildingIndex = data.getBuildings().indexOf(building) + 1;
                BuildingLogisticsService.tryDeposit(
                        server,
                        data,
                        buildingIndex,
                        building,
                        buildingLevel,
                        villager);
            }
        }

    }

    private static ServerLevel findLevel(MinecraftServer server, String dimensionId) {
        for (ResourceKey<Level> key : server.levelKeys()) {
            if (key.identifier().toString().equals(dimensionId)) {
                return server.getLevel(key);
            }
        }
        return null;
    }
}
