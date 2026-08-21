package com.civilizationmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CivitasCoreTest {
    private static final UUID RESIDENT_ONE = UUID.fromString("123e4567-e89b-12d3-a456-426614174101");
    private static final UUID RESIDENT_TWO = UUID.fromString("123e4567-e89b-12d3-a456-426614174102");

    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void publicCommandTreeExposesHelpAndRemovesLegacyCommands() {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        CivilizationCommands.register(dispatcher, null, null);

        assertNotNull(dispatcher.getRoot().getChild("civitas"));
        assertNotNull(dispatcher.getRoot().getChild("civilization"));
        assertNotNull(dispatcher.getRoot().getChild("civitas").getChild("help"));
        assertNotNull(dispatcher.getRoot().getChild("civitas").getChild("building"));
        assertNotNull(dispatcher.getRoot().getChild("civitas").getChild("townhall"));
        assertNotNull(dispatcher.getRoot().getChild("civitas").getChild("resident"));
        var unassign = dispatcher.getRoot().getChild("civitas").getChild("unassign");
        assertNotNull(unassign);
        assertNotNull(unassign.getChild("building_index"));
        assertNotNull(unassign.getChild("villager"));
        assertNotNull(unassign.getChild("resident"));
        assertNotNull(unassign.getChild("resident").getChild("resident_index"));
        assertNull(dispatcher.getRoot().getChild("civitas").getChild("simulate"));
        assertNull(dispatcher.getRoot().getChild("civitas").getChild("scan"));
        assertNull(dispatcher.getRoot().getChild("civitas").getChild("settlement"));
        assertNull(dispatcher.getRoot().getChild("civitas").getChild("sc"));
        assertNull(dispatcher.getRoot().getChild("civitas").getChild("help").getChild("text"));
    }

    @Test
    void foodDemandShortageAndSurplusRemainDeterministic() {
        FoodDemandModel.Result shortage = FoodDemandModel.simulate(settlement(0L, 0, 100));
        assertEquals(20L, shortage.demand());
        assertEquals(0L, shortage.consumed());
        assertEquals(0L, shortage.settlement().foodStock());
        assertEquals(20, shortage.settlement().stabilityDebt());
        assertEquals(95, shortage.settlement().stability());
        assertEquals(FoodDemandModel.EVENT_SHORTAGE, shortage.settlement().lastFoodEvent());

        FoodDemandModel.Result surplus = FoodDemandModel.simulate(settlement(100L, 5, 99));
        assertEquals(80L, surplus.settlement().foodStock());
        assertEquals(4, surplus.settlement().stabilityDebt());
        assertEquals(100, surplus.settlement().stability());
        assertEquals(FoodDemandModel.EVENT_SURPLUS, surplus.settlement().lastFoodEvent());
    }

    @Test
    void settlementDeduplicationPreservesExistingSimulationState() {
        CivilizationWorldData data = new CivilizationWorldData();
        SettlementAdapter existing = settlementAt("test:village", 0, 64, 0, 17L, 42, 73);
        assertTrue(data.addSettlement(existing));

        SettlementAdapter driftedScan = settlementAt("test:village", 48, 64, 32, 100L, 0, 100);
        assertFalse(data.addSettlement(driftedScan));
        assertEquals(1, data.getSettlementCount());
        assertEquals(17L, data.getSettlement(1).foodStock());
        assertEquals(42, data.getSettlement(1).stabilityDebt());
        assertEquals(73, data.getSettlement(1).stability());

        assertTrue(data.addSettlement(settlementAt("other:village", 48, 64, 32, 100L, 0, 100)));
        assertTrue(data.addSettlement(settlementAt("test:village", 65, 64, 0, 100L, 0, 100)));
        assertEquals(3, data.getSettlementCount());
    }

    @Test
    void buildingObservationPreservesResidentRosterAcrossRefreshAndCodec() {
        BuildingObservation residence = new BuildingObservation(
                BuildingFunction.RESIDENCE.id(),
                "minecraft:overworld",
                10,
                64,
                20,
                BuildingObservation.STATUS_BOUND,
                100L,
                100L,
                "minecraft:overworld",
                0,
                64,
                0
        ).withResidenceMeasurements(2, 2);

        BuildingObservation assigned = residence
                .withAddedResident(RESIDENT_ONE, "Resident One")
                .withAddedResident(RESIDENT_TWO, "Resident Two")
                .withAddedResident(RESIDENT_TWO, "Duplicate Resident");

        assertEquals(2, assigned.residentCount());
        assertTrue(assigned.hasResident(RESIDENT_ONE));
        assertTrue(assigned.hasResident(RESIDENT_TWO));

        BuildingObservation refreshed = assigned.refreshed(
                BuildingFunction.RESIDENCE.id(),
                200L,
                BuildingObservation.STATUS_BOUND,
                BuildingObservation.VALIDATION_VALID,
                BuildingObservation.VALIDATION_REASON_VALID,
                "minecraft:overworld",
                0,
                64,
                0,
                2,
                2,
                BuildingStorageSnapshot.unscanned());
        assertEquals(2, refreshed.residentCount());
        assertEquals("Resident Two", refreshed.residents().get(1).name());

        BuildingObservation colonyBound = assigned.withColonyBinding(
                BuildingObservation.STATUS_BOUND,
                "minecraft:overworld@townhall",
                BuildingObservation.COLONY_REASON_BOUND);
        assertTrue(colonyBound.isColonyBound());
        assertEquals("minecraft:overworld@townhall", colonyBound.colonyId());

        var encoded = BuildingObservation.CODEC.encodeStart(JsonOps.INSTANCE, colonyBound).getOrThrow();
        BuildingObservation decoded = BuildingObservation.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(2, decoded.residentCount());
        assertEquals("Resident Two", decoded.residents().get(1).name());
        assertEquals("minecraft:overworld@townhall", decoded.colonyId());
        assertEquals(BuildingObservation.COLONY_REASON_BOUND, decoded.colonyBindingReason());
        assertEquals(1, residence.withResident(RESIDENT_ONE, "Legacy Resident").residentCount());
    }

    @Test
    void residentRegistryMigratesLegacyRosterIdempotentlyAndRoundTrips() {
        BuildingObservation residence = new BuildingObservation(
                BuildingFunction.RESIDENCE.id(),
                "minecraft:overworld",
                40,
                70,
                -12,
                BuildingObservation.STATUS_BOUND,
                300L,
                300L,
                "minecraft:overworld",
                0,
                70,
                0)
                .withValidation(BuildingObservation.VALIDATION_VALID, BuildingObservation.VALIDATION_REASON_VALID)
                .withResidenceMeasurements(2, 2)
                .withColonyBinding(
                        BuildingObservation.STATUS_BOUND,
                        "minecraft:overworld@townhall-1",
                        BuildingObservation.COLONY_REASON_BOUND)
                .withResident(RESIDENT_ONE, "Legacy Resident");

        ResidentRegistry registry = new ResidentRegistry();
        assertEquals(1, registry.migrateLegacy(java.util.List.of(residence)));
        assertEquals(0, registry.migrateLegacy(java.util.List.of(residence)));
        assertEquals(1, registry.size());

        ResidentRecord migrated = registry.findByResidentId(RESIDENT_ONE);
        assertNotNull(migrated);
        assertEquals(RESIDENT_ONE.toString(), migrated.entityUuid());
        assertEquals("minecraft:overworld@townhall-1", migrated.colonyId());
        assertEquals(ResidentRecord.ROLE_RESIDENT, migrated.role());
        assertEquals(
                "minecraft:overworld@40,70,-12",
                migrated.homeBuildingKey());

        var encoded = ResidentRegistry.CODEC.encodeStart(JsonOps.INSTANCE, registry).getOrThrow();
        ResidentRegistry decoded = ResidentRegistry.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(registry.getResidents(), decoded.getResidents());

        ResidentRecord replacementBody = ResidentRegistry.createNew(
                RESIDENT_TWO,
                residence,
                ResidentRecord.ROLE_RESIDENT,
                "New Body",
                400L);
        assertNotNull(replacementBody);
        assertNotEquals(replacementBody.residentId(), replacementBody.entityUuid());
        assertTrue(registry.upsert(replacementBody));
        assertSame(replacementBody,
                registry.findByEntityUuid(RESIDENT_TWO));
    }

    @Test
    void newAssignmentWritesRegistryWithoutMutatingLegacyRoster() {
        BuildingObservation residence = new BuildingObservation(
                BuildingFunction.RESIDENCE.id(),
                "minecraft:overworld",
                45,
                70,
                10,
                BuildingObservation.STATUS_BOUND,
                BuildingObservation.VALIDATION_VALID,
                BuildingObservation.VALIDATION_REASON_VALID,
                450L,
                450L,
                "minecraft:overworld",
                0,
                70,
                0)
                .withResidenceMeasurements(2, 2)
                .withColonyBinding(
                        BuildingObservation.STATUS_BOUND,
                        "minecraft:overworld@townhall-1",
                        BuildingObservation.COLONY_REASON_BOUND);

        CivilizationWorldData data = new CivilizationWorldData();
        assertTrue(data.addBuildingObservation(residence));
        BuildingObservation stored = data.getBuilding(1);
        assertNotNull(stored);
        assertEquals(0, stored.residentCount());

        ResidentRecord assigned = data.ensureResidentAssignment(
                stored,
                RESIDENT_ONE,
                "Registry Resident",
                500L);
        assertNotNull(assigned);
        assertEquals(0, data.getBuilding(1).residentCount());
        assertEquals(1, data.countActiveResidents(data.getBuilding(1)));
        assertNotNull(data.findBuildingAssignedTo(RESIDENT_ONE.toString()));

        assertTrue(data.clearResidentAssignment(RESIDENT_ONE, 600L));
        assertEquals(0, data.countActiveResidents(data.getBuilding(1)));
        assertEquals(0, data.getBuilding(1).residentCount());
    }

    @Test
    void repeatedAssignmentToSameBuildingIsIdempotentAndNotReportedAsChanged() {
        BuildingObservation residence = new BuildingObservation(
                BuildingFunction.RESIDENCE.id(),
                "minecraft:overworld",
                45,
                70,
                10,
                BuildingObservation.STATUS_BOUND,
                BuildingObservation.VALIDATION_VALID,
                BuildingObservation.VALIDATION_REASON_VALID,
                450L,
                450L,
                "minecraft:overworld",
                0,
                70,
                0)
                .withResidenceMeasurements(2, 2)
                .withColonyBinding(
                        BuildingObservation.STATUS_BOUND,
                        "minecraft:overworld@townhall-1",
                        BuildingObservation.COLONY_REASON_BOUND);

        CivilizationWorldData data = new CivilizationWorldData();
        assertTrue(data.addBuildingObservation(residence));
        BuildingObservation stored = data.getBuilding(1);

        CivilizationWorldData.ResidentAssignmentResult first =
                data.ensureResidentAssignmentResult(
                        stored,
                        RESIDENT_ONE,
                        "Registry Resident",
                        500L);
        assertEquals(
                CivilizationWorldData.ResidentAssignmentStatus.CREATED,
                first.status());
        assertTrue(first.changed());
        assertEquals(1, data.countActiveResidents(stored));

        CivilizationWorldData.ResidentAssignmentResult repeated =
                data.ensureResidentAssignmentResult(
                        stored,
                        RESIDENT_ONE,
                        "Registry Resident",
                        600L);
        assertEquals(
                CivilizationWorldData.ResidentAssignmentStatus.ALREADY_ASSIGNED_TO_TARGET,
                repeated.status());
        assertFalse(repeated.changed());
        assertEquals(first.resident().residentId(), repeated.resident().residentId());
        assertEquals(1, data.countActiveResidents(stored));
        assertEquals(0, data.getBuilding(1).residentCount());
    }

    @Test
    void unassignByResidentIdUsesRegistryEvenWhenLegacyRosterIsEmpty() {
        BuildingObservation residence = new BuildingObservation(
                BuildingFunction.RESIDENCE.id(),
                "minecraft:overworld",
                55,
                70,
                10,
                BuildingObservation.STATUS_BOUND,
                BuildingObservation.VALIDATION_VALID,
                BuildingObservation.VALIDATION_REASON_VALID,
                450L,
                450L,
                "minecraft:overworld",
                0,
                70,
                0)
                .withResidenceMeasurements(2, 2)
                .withColonyBinding(
                        BuildingObservation.STATUS_BOUND,
                        "minecraft:overworld@townhall-1",
                        BuildingObservation.COLONY_REASON_BOUND);

        CivilizationWorldData data = new CivilizationWorldData();
        assertTrue(data.addBuildingObservation(residence));
        ResidentRecord assigned = data.ensureResidentAssignment(
                data.getBuilding(1),
                RESIDENT_ONE,
                "Registry Resident",
                500L);
        assertNotNull(assigned);
        assertEquals(0, data.getBuilding(1).residentCount());
        assertEquals(1, data.countActiveResidents(data.getBuilding(1)));

        assertTrue(data.clearResidentAssignmentByResidentId(assigned.residentId(), 600L));
        ResidentRecord cleared = data.getResidentRegistry().findByResidentId(assigned.residentId());
        assertNotNull(cleared);
        assertEquals("", cleared.assignedBuildingKey());
        assertEquals(0, data.countActiveResidents(data.getBuilding(1)));
        assertEquals(0, data.getBuilding(1).residentCount());
    }

    @Test
    void residentDeathReleasesHomeCapacityButKeepsHistoricalIdentity() {
        BuildingObservation residence = new BuildingObservation(
                BuildingFunction.RESIDENCE.id(),
                "minecraft:overworld",
                50,
                70,
                10,
                BuildingObservation.STATUS_BOUND,
                500L,
                500L,
                "minecraft:overworld",
                0,
                70,
                0)
                .withValidation(BuildingObservation.VALIDATION_VALID, BuildingObservation.VALIDATION_REASON_VALID)
                .withResidenceMeasurements(2, 2)
                .withColonyBinding(
                        BuildingObservation.STATUS_BOUND,
                        "minecraft:overworld@townhall-1",
                        BuildingObservation.COLONY_REASON_BOUND)
                .withAddedResident(RESIDENT_ONE, "Resident One")
                .withAddedResident(RESIDENT_TWO, "Resident Two");

        CivilizationWorldData data = new CivilizationWorldData();
        assertTrue(data.addBuildingObservation(residence));
        assertEquals(2, data.getBuilding(1).residentCount());
        assertTrue(data.markResidentDead(RESIDENT_ONE, 600L));

        ResidentRecord dead = data.getResidentRegistry().findByEntityUuid(RESIDENT_ONE);
        assertNotNull(dead);
        assertEquals(RESIDENT_ONE.toString(), dead.residentId());
        assertEquals(RESIDENT_ONE.toString(), dead.entityUuid());
        assertEquals(ResidentRecord.LIFECYCLE_DEAD, dead.lifecycle());
        assertEquals("", dead.homeBuildingKey());
        assertEquals(2, data.getBuilding(1).residentCount());
        assertEquals(1, data.countActiveResidents(data.getBuilding(1)));
        assertTrue(data.getBuilding(1).hasResident(RESIDENT_TWO));
        assertNull(data.findBuildingAssignedTo(RESIDENT_ONE.toString()));
        assertNotNull(data.findBuildingAssignedTo(RESIDENT_TWO.toString()));
    }

    @Test
    void buildingIdentityAndSettlementBindingRespectDimensionAndRange() {
        BuildingObservation observation = new BuildingObservation(
                BuildingFunction.WAREHOUSE.id(),
                "minecraft:overworld",
                10,
                64,
                20,
                BuildingObservation.STATUS_UNBOUND,
                100L,
                100L,
                "",
                0,
                0,
                0);
        assertTrue(observation.isSameMarker("minecraft:overworld", 10, 64, 20));
        assertFalse(observation.isSameMarker("minecraft:the_nether", 10, 64, 20));

        CivilizationWorldData data = new CivilizationWorldData();
        assertTrue(data.addSettlement(settlementAt("test:village", 0, 64, 0, 55L, 0, 100)));
        assertNotNull(data.findSettlementForBuilding("minecraft:overworld", 64, 64, 64));
        assertNull(data.findSettlementForBuilding("minecraft:overworld", 129, 64, 0));
        assertNull(data.findSettlementForBuilding("minecraft:the_nether", 0, 64, 0));
    }

    @Test
    void noTownHallAllowsValidBuildingTransitionButExistingRangeRemainsStrict() {
        BuildingObservation transitionBuilding = new BuildingObservation(
                BuildingFunction.WAREHOUSE.id(),
                "minecraft:overworld",
                10,
                64,
                20,
                BuildingObservation.STATUS_UNBOUND,
                BuildingObservation.VALIDATION_VALID,
                BuildingObservation.VALIDATION_REASON_VALID,
                100L,
                100L,
                "",
                0,
                0,
                0);
        BuildingObservation outsideBuilding = new BuildingObservation(
                BuildingFunction.WAREHOUSE.id(),
                "minecraft:overworld",
                500,
                64,
                20,
                BuildingObservation.STATUS_UNBOUND,
                BuildingObservation.VALIDATION_VALID,
                BuildingObservation.VALIDATION_REASON_VALID,
                100L,
                100L,
                "",
                0,
                0,
                0);
        BuildingObservation otherDimensionBuilding = new BuildingObservation(
                BuildingFunction.WAREHOUSE.id(),
                "minecraft:the_nether",
                500,
                64,
                20,
                BuildingObservation.STATUS_UNBOUND,
                BuildingObservation.VALIDATION_VALID,
                BuildingObservation.VALIDATION_REASON_VALID,
                100L,
                100L,
                "",
                0,
                0,
                0);

        CivilizationWorldData data = new CivilizationWorldData();
        assertTrue(data.addBuildingObservation(transitionBuilding));
        assertTrue(data.addBuildingObservation(outsideBuilding));
        assertTrue(data.addBuildingObservation(otherDimensionBuilding));

        assertSame(CivilizationWorldData.TownHallBindingStatus.NO_TOWN_HALL,
                data.findTownHallBinding("minecraft:overworld", new BlockPos(10, 64, 20)).status());
        assertTrue(data.isTownHallTransitionAllowed(transitionBuilding));
        assertTrue(data.isBuildingOperational(transitionBuilding));
        assertTrue(data.isTownHallTransitionAllowed(otherDimensionBuilding));

        assertSame(CivilizationWorldData.TownHallRegistrationStatus.REGISTERED,
                data.registerTownHall("minecraft:overworld", new BlockPos(0, 64, 0), 200L).status());
        assertFalse(data.isTownHallTransitionAllowed(transitionBuilding));
        assertTrue(data.isBuildingOperational(data.getBuilding(1)));
        assertSame(CivilizationWorldData.TownHallBindingStatus.OUTSIDE,
                data.findTownHallBinding("minecraft:overworld", new BlockPos(500, 64, 20)).status());
        assertFalse(data.isTownHallTransitionAllowed(data.getBuilding(2)));
        assertFalse(data.isBuildingOperational(data.getBuilding(2)));
        assertTrue(data.isTownHallTransitionAllowed(data.getBuilding(3)));
        assertTrue(data.isBuildingOperational(data.getBuilding(3)));
    }

    @Test
    void territoryBoundsAreInclusiveAndComparable() {
        WarehouseTerritory territory = new WarehouseTerritory(
                1,
                "minecraft:overworld",
                4,
                64,
                30,
                10,
                70,
                36);
        assertEquals(4, territory.minX());
        assertEquals(10, territory.maxX());
        assertEquals(64, territory.minY());
        assertEquals(70, territory.maxY());
        assertTrue(territory.contains(new BlockPos(7, 67, 33)));
        assertFalse(territory.contains(new BlockPos(3, 67, 33)));
        assertEquals(territory, new WarehouseTerritory(
                1,
                "minecraft:overworld",
                4,
                64,
                30,
                10,
                70,
                36));
    }

    @Test
    void townHallRangesCanBeConfiguredWithoutOverlapAndRemainCodecCompatible() {
        CivilizationWorldData data = new CivilizationWorldData();
        BlockPos firstMarker = new BlockPos(100, 70, -40);

        CivilizationWorldData.TownHallRegistration registered = data.registerTownHall(
                "minecraft:overworld", firstMarker, 1234L);
        assertSame(CivilizationWorldData.TownHallRegistrationStatus.REGISTERED, registered.status());

        TownHallCore core = data.getTownHallCore("minecraft:overworld");
        assertNotNull(core);
        assertTrue(core.isSameMarker("minecraft:overworld", firstMarker));
        assertEquals(1234L, core.createdAt());
        assertEquals(TownHallCore.DEFAULT_RADIUS, core.radius());
        assertTrue(core.contains(firstMarker.offset(TownHallCore.DEFAULT_RADIUS, TownHallCore.DEFAULT_RADIUS, TownHallCore.DEFAULT_RADIUS)));
        assertFalse(core.contains(firstMarker.offset(TownHallCore.DEFAULT_RADIUS + 1, 0, 0)));

        CivilizationWorldData.TownHallRegistration same = data.registerTownHall(
                "minecraft:overworld", firstMarker, 9999L);
        assertSame(CivilizationWorldData.TownHallRegistrationStatus.EXISTING, same.status());
        assertSame(CivilizationWorldData.TownHallRegistrationStatus.DUPLICATE, data.registerTownHall(
                "minecraft:overworld", firstMarker.offset(10, 0, 10), 9999L).status());
        assertSame(CivilizationWorldData.TownHallRegistrationStatus.REGISTERED, data.registerTownHall(
                "minecraft:overworld", firstMarker.offset(180, 0, 0), 2000L).status());
        assertSame(CivilizationWorldData.TownHallRegistrationStatus.REGISTERED, data.registerTownHall(
                "minecraft:the_nether", firstMarker, 2222L).status());

        TownHallCore separatedA = TownHallCore.create("minecraft:overworld", new BlockPos(0, 64, 0), 1L);
        TownHallCore separatedB = TownHallCore.create("minecraft:overworld", new BlockPos(129, 64, 0), 2L);
        TownHallCore touching = TownHallCore.create("minecraft:overworld", new BlockPos(128, 64, 0), 3L);
        assertFalse(separatedA.overlaps(separatedB));
        assertTrue(separatedA.overlaps(touching));
        assertTrue(separatedA.contains(new BlockPos(64, 64, 64)));
        assertFalse(separatedA.contains(new BlockPos(65, 64, 64)));
        assertTrue(touching.contains(new BlockPos(64, 64, 0)));

        CivilizationWorldData touchingData = new CivilizationWorldData();
        assertSame(CivilizationWorldData.TownHallRegistrationStatus.REGISTERED,
                touchingData.registerTownHall("minecraft:overworld", new BlockPos(0, 64, 0), 1L).status());
        assertSame(CivilizationWorldData.TownHallRegistrationStatus.DUPLICATE,
                touchingData.registerTownHall("minecraft:overworld", new BlockPos(128, 64, 0), 2L).status());
        assertSame(CivilizationWorldData.TownHallBindingStatus.BOUND,
                touchingData.findTownHallBinding("minecraft:overworld", new BlockPos(64, 64, 0)).status());
        assertEquals(3, data.getTownHallCoreCount());
        assertSame(CivilizationWorldData.TownHallBindingStatus.BOUND,
                data.findTownHallBinding("minecraft:overworld", firstMarker.offset(20, 0, 0)).status());
        assertSame(CivilizationWorldData.TownHallBindingStatus.OUTSIDE,
                data.findTownHallBinding("minecraft:overworld", firstMarker.offset(500, 0, 0)).status());
        assertSame(CivilizationWorldData.TownHallRadiusUpdateStatus.OVERLAPPING,
                data.updateTownHallRadius(1, 128).status());
        assertSame(CivilizationWorldData.TownHallRadiusUpdateStatus.UPDATED,
                data.updateTownHallRadius(1, 96).status());
        assertEquals(96, data.getTownHallCore(1).radius());

        var encoded = TownHallCore.CODEC.encodeStart(JsonOps.INSTANCE, core).getOrThrow();
        TownHallCore decoded = TownHallCore.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(core, decoded);
    }

    private static SettlementAdapter settlement(long foodStock, int stabilityDebt, int stability) {
        return settlementAt("test:village", 0, 64, 0, foodStock, stabilityDebt, stability);
    }

    private static SettlementAdapter settlementAt(
            String source,
            int centerX,
            int centerY,
            int centerZ,
            long foodStock,
            int stabilityDebt,
            int stability
    ) {
        return new SettlementAdapter(
                source,
                "minecraft:overworld",
                centerX,
                centerY,
                centerZ,
                0L,
                "test",
                20,
                foodStock,
                1,
                stabilityDebt,
                stability,
                FoodDemandModel.EVENT_STABLE);
    }
}
