package com.civilizationmod;

import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.npc.villager.Villager;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Gameplay-level ResidentRecord checks. These tests deliberately use a real
 * GameTest server and Villager body instead of only constructing records in JUnit.
 */
public final class ResidentRecordGameTest implements CustomTestMethodInvoker {
    private static final String TEST_RESIDENT_NAME = "Civitas GameTest Resident";

    @GameTest(maxTicks = 80, setupTicks = 1)
    public void residentBodyLookupAndDeathRelease(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        CivilizationWorldData data = CivilizationWorldData.get(level.getServer());
        String dimension = level.dimension().identifier().toString();
        BlockPos marker = helper.absolutePos(new BlockPos(2, 1, 2));

        BuildingObservation fixture = new BuildingObservation(
                BuildingFunction.RESIDENCE.id(),
                dimension,
                marker.getX(),
                marker.getY(),
                marker.getZ(),
                BuildingObservation.STATUS_UNBOUND,
                BuildingObservation.VALIDATION_VALID,
                BuildingObservation.VALIDATION_REASON_VALID,
                level.getGameTime(),
                level.getGameTime(),
                "",
                0,
                0,
                0,
                1,
                1,
                "",
                "",
                BuildingStorageSnapshot.unscanned(),
                List.of(),
                "",
                BuildingObservation.COLONY_REASON_NO_TOWN_HALL);

        BuildingObservation building = data.findBuilding(
                dimension,
                marker.getX(),
                marker.getY(),
                marker.getZ());
        if (building == null) {
            if (!data.addBuildingObservation(fixture)) {
                helper.fail("ResidentRecord GameTest could not add the building fixture");
                return;
            }
            building = fixture;
        }
        final BuildingObservation assignedBuilding = building;

        Villager villager = helper.spawnWithNoFreeWill(
                EntityTypes.VILLAGER,
                new BlockPos(2, 2, 2));
        ResidentRecord assigned = data.ensureResidentAssignment(
                assignedBuilding,
                villager.getUUID(),
                TEST_RESIDENT_NAME,
                level.getGameTime());
        if (assigned == null) {
            helper.fail("ResidentRecord assignment was not created in a real server world");
            return;
        }

        helper.assertTrue(
                level.getEntityInAnyDimension(villager.getUUID()) == villager,
                "loaded Villager body must be discoverable by entity UUID");
        helper.assertTrue(
                data.findBuildingAssignedTo(villager.getUUID().toString()) != null,
                "active ResidentRecord must resolve to its assigned building");
        helper.assertValueEqual(
                1,
                data.countActiveResidents(assignedBuilding),
                "active residential capacity must include the assigned body");

        ResidentRecord savedRecord = data.getResidentRegistry().findByResidentId(assigned.residentId());
        helper.assertTrue(savedRecord != null, "ResidentRecord must be present in world SavedData object");
        helper.assertValueEqual(
                assigned.entityUuid(),
                savedRecord.entityUuid(),
                "SavedData resident record must retain the body UUID");

        helper.kill(villager);
        helper.runAtTickTime(2, () -> {
            ResidentRecord dead = data.getResidentRegistry().findByResidentId(assigned.residentId());
            helper.assertTrue(dead != null, "dead resident history must remain in the registry");
            helper.assertValueEqual(
                    ResidentRecord.LIFECYCLE_DEAD,
                    dead.lifecycle(),
                    "server death callback must mark the ResidentRecord dead");
            helper.assertValueEqual(
                    0,
                    data.countActiveResidents(assignedBuilding),
                    "dead resident must no longer consume residential capacity");
            helper.assertTrue(
                    data.findBuildingAssignedTo(villager.getUUID().toString()) == null,
                    "dead body UUID must no longer resolve to an active building assignment");
            helper.succeed();
        });
    }

    @Override
    public void invokeTestMethod(GameTestHelper helper, Method method)
            throws ReflectiveOperationException {
        method.invoke(this, helper);
    }
}
