package com.civilizationmod;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.UUID;

import net.minecraft.core.BlockPos;

/**
 * Lightweight regression checks for the pure Java food model.
 *
 * <p>This deliberately uses a main method instead of a third-party test
 * framework so the first test slice stays independent from additional
 * dependency version choices.</p>
 */
public final class FoodModelRegressionTest {
	private FoodModelRegressionTest() {
	}

	public static void main(String[] args) {
		checkDemandNormalization();
		checkShortageStep();
		checkSurplusStep();
		checkStableThreshold();
		checkClampRules();
		checkDeterministicMultiStepSimulation();
		checkSettlementDeduplication();
		checkProviderInitialization();
		checkPopulationRefresh();
		checkBuildingFunctionContract();
		checkBuildingObservationLifecycle();
			checkBuildingResidentAssignment();
			checkResidenceCapacityRoster();
			checkBuildingSettlementBinding();
			checkBuildingGeometryValidator();
											checkWarehouseTerritoryBounds();
				checkTownHallCoreLifecycle();

				System.out.println("FoodModelRegressionTest: PASS");

	}

	private static void checkDemandNormalization() {
		equalsLong(20L, FoodDemandModel.calculateDemand(20, 1), "normal demand");
		equalsLong(0L, FoodDemandModel.calculateDemand(-1, 1), "negative population clamp");
		equalsLong(20L, FoodDemandModel.calculateDemand(20, 0), "minimum consumption clamp");
	}

	private static void checkShortageStep() {
		FoodDemandModel.Result result = FoodDemandModel.simulate(settlement(10L, 0, 100));
		equalsLong(20L, result.demand(), "shortage demand");
		equalsLong(10L, result.consumed(), "shortage consumed");
		equalsLong(10L, result.shortage(), "shortage amount");
		equalsLong(0L, result.settlement().foodStock(), "shortage remaining stock");
		equalsInt(10, result.settlement().stabilityDebt(), "shortage debt");
		equalsInt(95, result.settlement().stability(), "shortage stability");
		equalsString(FoodDemandModel.EVENT_SHORTAGE, result.settlement().lastFoodEvent(), "shortage event");
	}

	private static void checkSurplusStep() {
		FoodDemandModel.Result result = FoodDemandModel.simulate(settlement(100L, 5, 99));
		equalsLong(80L, result.settlement().foodStock(), "surplus remaining stock");
		equalsInt(4, result.settlement().stabilityDebt(), "surplus debt recovery");
		equalsInt(100, result.settlement().stability(), "surplus stability recovery");
		equalsString(FoodDemandModel.EVENT_SURPLUS, result.settlement().lastFoodEvent(), "surplus event");
	}

	private static void checkStableThreshold() {
		FoodDemandModel.Result result = FoodDemandModel.simulate(settlement(60L, 5, 88));
		equalsLong(40L, result.settlement().foodStock(), "stable threshold remaining stock");
		equalsInt(5, result.settlement().stabilityDebt(), "stable threshold debt");
		equalsInt(88, result.settlement().stability(), "stable threshold stability");
		equalsString(FoodDemandModel.EVENT_STABLE, result.settlement().lastFoodEvent(), "stable event");
	}

	private static void checkClampRules() {
		equalsInt(StabilityDebt.MAX_DEBT, StabilityDebt.addShortage(999_999, 10L), "debt upper clamp");
		equalsInt(0, StabilityDebt.applyShortage(2, 5), "stability lower clamp");
		equalsInt(100, StabilityDebt.recoverWithSurplus(100, 1), "stability upper clamp");
	}

	private static void checkDeterministicMultiStepSimulation() {
		SettlementAdapter current = settlement(100L, 0, 100);
		for (int step = 1; step <= 5; step++) {
			current = FoodDemandModel.simulate(current).settlement();
			equalsLong(100L - step * 20L, current.foodStock(), "deterministic stock step " + step);
			equalsInt(100, current.stability(), "deterministic stability step " + step);
		}

		current = FoodDemandModel.simulate(current).settlement();
		equalsLong(0L, current.foodStock(), "deterministic exhausted stock");
		equalsInt(20, current.stabilityDebt(), "deterministic shortage debt");
		equalsInt(95, current.stability(), "deterministic shortage stability");
		equalsString(FoodDemandModel.EVENT_SHORTAGE, current.lastFoodEvent(), "deterministic shortage event");
	}

	private static void checkProviderInitialization() {
		SettlementAdapter empty = emptySettlementAt("test:village", 0, 64, 0);
		SettlementAdapter observed = FoodStockProvider.initialize(
				null,
			empty,
				(level, settlement) -> OptionalInt.of(7),
				(level, settlement) -> OptionalLong.of(55L)
		);
		equalsInt(7, observed.population(), "provider population");
		equalsLong(55L, observed.foodStock(), "provider food stock");

		SettlementAdapter preserved = FoodStockProvider.initialize(
				null,
				observed,
				(level, settlement) -> OptionalInt.of(99),
				(level, settlement) -> OptionalLong.of(999L)
		);
		equalsInt(7, preserved.population(), "provider preserves population");
		equalsLong(55L, preserved.foodStock(), "provider preserves food stock");

		SettlementAdapter bootstrap = FoodStockProvider.initialize(empty);
		equalsInt(FoodStockProvider.DEFAULT_POPULATION, bootstrap.population(), "bootstrap population");
		equalsLong(FoodStockProvider.DEFAULT_FOOD_STOCK, bootstrap.foodStock(), "bootstrap food stock");
	}

	private static void checkPopulationRefresh() {
		SettlementAdapter current = new SettlementAdapter(
				"test:village",
				"minecraft:overworld",
				0,
				64,
				0,
				0L,
				"test",
				20,
				42L,
				1,
				9,
				87,
				FoodDemandModel.EVENT_SHORTAGE
		);
		SettlementAdapter refreshed = FoodStockProvider.refreshPopulation(
				null,
				current,
				(level, settlement) -> OptionalInt.of(7)
		);
		equalsInt(7, refreshed.population(), "refresh population");
		equalsLong(42L, refreshed.foodStock(), "refresh preserves food");
		equalsInt(9, refreshed.stabilityDebt(), "refresh preserves debt");
		equalsInt(87, refreshed.stability(), "refresh preserves stability");
		equalsString(FoodDemandModel.EVENT_SHORTAGE, refreshed.lastFoodEvent(), "refresh preserves event");

		SettlementAdapter unchanged = FoodStockProvider.refreshPopulation(
				null,
				refreshed,
				(level, settlement) -> OptionalInt.empty()
		);
		equalsInt(7, unchanged.population(), "empty refresh preserves observed population");
	}

	private static void checkSettlementDeduplication() {
		CivilizationWorldData data = new CivilizationWorldData();
		SettlementAdapter existing = settlementAt("test:village", 0, 64, 0, 17L, 42, 73);
		if (!data.addSettlement(existing)) {
			throw new AssertionError("initial settlement should be added");
		}

		SettlementAdapter sameVillageWithDrift = settlementAt("test:village", 48, 64, 32, 100L, 0, 100);
		if (data.addSettlement(sameVillageWithDrift)) {
			throw new AssertionError("nearby scan of the same settlement should be rejected");
		}
		equalsInt(1, data.getSettlementCount(), "duplicate settlement count");
		equalsLong(17L, data.getSettlement(1).foodStock(), "duplicate preserves food");
		equalsInt(42, data.getSettlement(1).stabilityDebt(), "duplicate preserves debt");
		equalsInt(73, data.getSettlement(1).stability(), "duplicate preserves stability");

		SettlementAdapter differentSource = settlementAt("other:village", 48, 64, 32, 100L, 0, 100);
		if (!data.addSettlement(differentSource)) {
			throw new AssertionError("different settlement source should remain distinct");
		}
		SettlementAdapter farSettlement = settlementAt("test:village", 65, 64, 0, 100L, 0, 100);
		if (!data.addSettlement(farSettlement)) {
			throw new AssertionError("far settlement should remain distinct");
		}
		equalsInt(3, data.getSettlementCount(), "distinct settlement count");
	}

	private static void checkBuildingFunctionContract() {
		equalsString("warehouse", BuildingFunction.WAREHOUSE.id(), "warehouse function id");
	if (BuildingFunction.fromId("warehouse") != BuildingFunction.WAREHOUSE) {
			throw new AssertionError("warehouse function id should resolve to WAREHOUSE");
		}
		if (BuildingFunction.fromId("unknown") != null) {
			throw new AssertionError("unknown function id should not resolve to a registered function");
		}
	}

	private static void checkBuildingObservationLifecycle() {
		BuildingObservation first = new BuildingObservation(
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
		BuildingObservation refreshed = first.refreshed(
				BuildingFunction.WAREHOUSE.id(),
				200L,
				BuildingObservation.STATUS_BOUND,
				"minecraft:overworld",
				0,
				64,
				0);
		equalsLong(100L, refreshed.firstSeen(), "building first seen is preserved");
		equalsLong(200L, refreshed.lastSeen(), "building last seen is refreshed");
		equalsString(BuildingObservation.STATUS_BOUND, refreshed.status(), "building status is refreshed");
		if (!refreshed.isSameMarker("minecraft:overworld", 10, 64, 20)) {
			throw new AssertionError("building marker identity should use dimension and exact marker position");
		}
		if (refreshed.isSameMarker("minecraft:the_nether", 10, 64, 20)) {
			throw new AssertionError("building marker identity must not cross dimensions");
		}
	}

			private static void checkBuildingGeometryValidator() {
				BuildingGeometryValidator.ValidationResult valid = BuildingGeometryValidator.evaluate(true, true, 4, 4, 4);
			equalsString(BuildingObservation.VALIDATION_VALID, valid.status(), "valid geometry status");
			equalsString(BuildingObservation.VALIDATION_REASON_VALID, valid.reason(), "valid geometry reason");

			BuildingGeometryValidator.ValidationResult noDoor = BuildingGeometryValidator.evaluate(false, true, 12, 3, 3);
			equalsString(BuildingObservation.VALIDATION_INVALID, noDoor.status(), "no door status");
			equalsString(BuildingObservation.VALIDATION_REASON_NO_DOOR, noDoor.reason(), "no door reason");

				BuildingGeometryValidator.ValidationResult noRoom = BuildingGeometryValidator.evaluate(true, true, 1, 4, 4);
				equalsString(BuildingObservation.VALIDATION_REASON_NO_ROOM, noRoom.reason(), "no room reason");
				equalsInt(1, noRoom.interiorAirBlocks(), "no room air diagnostics");
					equalsInt(4, noRoom.floorSupportBlocks(), "no room floor diagnostics");
					equalsInt(4, noRoom.ceilingBlocks(), "no room ceiling diagnostics");

				BuildingGeometryValidator.ValidationResult noFloor = BuildingGeometryValidator.evaluate(true, true, 12, 0, 4);
				equalsString(BuildingObservation.VALIDATION_REASON_NO_FLOOR, noFloor.reason(), "no floor reason");
				equalsInt(12, noFloor.interiorAirBlocks(), "no floor air diagnostics");
				equalsInt(0, noFloor.floorSupportBlocks(), "no floor floor diagnostics");
					equalsInt(4, noFloor.ceilingBlocks(), "no floor ceiling diagnostics");

				BuildingGeometryValidator.ValidationResult noCeiling = BuildingGeometryValidator.evaluate(true, true, 12, 4, 0);
				equalsString(BuildingObservation.VALIDATION_REASON_NO_CEILING, noCeiling.reason(), "no ceiling reason");
				equalsInt(12, noCeiling.interiorAirBlocks(), "no ceiling air diagnostics");
					equalsInt(4, noCeiling.floorSupportBlocks(), "no ceiling floor diagnostics");
									equalsInt(0, noCeiling.ceilingBlocks(), "no ceiling ceiling diagnostics");

				BuildingGeometryValidator.ValidationResult noWalls = BuildingGeometryValidator.evaluateStructure(
						true, true, 4, 4, 4, false, true, 1, true);
				equalsString(BuildingObservation.VALIDATION_REASON_NO_WALLS, noWalls.reason(), "no walls reason");

				BuildingGeometryValidator.ValidationResult noEntry = BuildingGeometryValidator.evaluateStructure(
						true, true, 4, 4, 4, true, false, 1, true);
				equalsString(BuildingObservation.VALIDATION_REASON_NO_ENTRY, noEntry.reason(), "no entry reason");

				BuildingGeometryValidator.ValidationResult noContainer = BuildingGeometryValidator.evaluateStructure(
						true, true, 4, 4, 4, true, true, 0, true);
				equalsString(BuildingObservation.VALIDATION_REASON_NO_CONTAINER, noContainer.reason(), "no container reason");

				BuildingGeometryValidator.ValidationResult unloaded = BuildingGeometryValidator.evaluate(true, false, 12, 4, 4);
			equalsString(BuildingObservation.VALIDATION_REASON_UNLOADED, unloaded.reason(), "unloaded reason");
		}

		private static void checkBuildingResidentAssignment() {
			BuildingObservation first = new BuildingObservation(
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
			UUID resident = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
			BuildingObservation assigned = first.withResident(resident, "Test Villager");
			if (!assigned.hasResident() || !resident.equals(assigned.residentUuidValue().orElse(null))) {
				throw new AssertionError("resident UUID should be stored and readable");
			}
			equalsString("Test Villager", assigned.residentName(), "resident name");

			BuildingObservation refreshed = assigned.refreshed(
					BuildingFunction.WAREHOUSE.id(),
					200L,
					BuildingObservation.STATUS_BOUND,
					"minecraft:overworld",
					0,
					64,
					0);
			if (!resident.toString().equals(refreshed.residentUuid())) {
				throw new AssertionError("building refresh must preserve resident UUID");
			}
			equalsString("Test Villager", refreshed.residentName(), "building refresh preserves resident name");

			if (refreshed.withoutResident().hasResident()) {
				throw new AssertionError("withoutResident should clear the assignment");
			}
		}

		private static void checkResidenceCapacityRoster() {
			BuildingObservation base = new BuildingObservation(
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
					0).withResidenceMeasurements(2, 2);
			UUID first = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
			UUID second = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");
			BuildingObservation assigned = base.withAddedResident(first, "Resident One")
					.withAddedResident(second, "Resident Two")
					.withAddedResident(second, "Resident Two Again");
			equalsInt(2, assigned.residentCount(), "residence roster capacity test count");
			if (!assigned.hasResident(first) || !assigned.hasResident(second)) {
				throw new AssertionError("residence roster should contain both assigned villagers");
			}
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
			equalsInt(2, refreshed.residentCount(), "residence roster survives rescan");

			JsonElement encoded = BuildingObservation.CODEC.encodeStart(JsonOps.INSTANCE, assigned).getOrThrow();
			BuildingObservation decoded = BuildingObservation.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
			equalsInt(2, decoded.residentCount(), "residence roster codec count");
			equalsString("Resident Two", decoded.residents().get(1).name(), "residence roster codec name");
			equalsInt(1, base.withResident(first, "Legacy Resident").residentCount(), "legacy single resident compatibility");
		}

		private static void checkBuildingSettlementBinding() {

		CivilizationWorldData data = new CivilizationWorldData();
		SettlementAdapter settlement = settlementAt("test:village", 0, 64, 0, 55L, 0, 100);
		if (!data.addSettlement(settlement)) {
			throw new AssertionError("building binding test settlement should be added");
		}

		SettlementAdapter nearby = data.findSettlementForBuilding("minecraft:overworld", 129, 64, 0);
		if (nearby != null) {
			throw new AssertionError("marker outside binding range should remain unbound");
		}
		SettlementAdapter bound = data.findSettlementForBuilding("minecraft:overworld", 64, 64, 64);
		if (bound == null) {
			throw new AssertionError("marker within binding range should bind to settlement");
		}
		equalsString("test:village", bound.source(), "building bound settlement source");
		if (data.findSettlementForBuilding("minecraft:the_nether", 0, 64, 0) != null) {
			throw new AssertionError("building binding must not cross dimensions");
		}
	}

		private static void checkWarehouseTerritoryBounds() {
			WarehouseTerritory territory = new WarehouseTerritory(
					1,
					"minecraft:overworld",
					4,
					64,
					30,
					10,
					70,
					36);
			equalsInt(4, territory.minX(), "territory min X");
			equalsInt(10, territory.maxX(), "territory max X");
			equalsInt(64, territory.minY(), "territory min Y");
			equalsInt(70, territory.maxY(), "territory max Y");
			if (!territory.contains(new BlockPos(7, 67, 33)) || territory.contains(new BlockPos(3, 67, 33))) {
				throw new AssertionError("warehouse territory inclusive bounds are incorrect");
			}
			WarehouseTerritory sameTerritory = new WarehouseTerritory(
					1,
					"minecraft:overworld",
					4,
					64,
					30,
					10,
					70,
					36);
			if (!territory.equals(sameTerritory)) {
				throw new AssertionError("equal warehouse territory bounds should deduplicate");
			}
		}

				

		private static void checkTownHallCoreLifecycle() {
			CivilizationWorldData data = new CivilizationWorldData();
			BlockPos first = new BlockPos(100, 70, -40);
			CivilizationWorldData.TownHallRegistration registered = data.registerTownHall(
					"minecraft:overworld", first, 1234L);
			if (registered.status() != CivilizationWorldData.TownHallRegistrationStatus.REGISTERED) {
				throw new AssertionError("first Town Hall should register");
			}
			TownHallCore core = data.getTownHallCore("minecraft:overworld");
			if (core == null || !core.isSameMarker("minecraft:overworld", first)) {
				throw new AssertionError("registered Town Hall core should be queryable");
			}
			equalsLong(1234L, core.createdAt(), "Town Hall created tick");
			equalsInt(TownHallCore.DEFAULT_RADIUS, core.radius(), "Town Hall default radius");

			CivilizationWorldData.TownHallRegistration same = data.registerTownHall(
					"minecraft:overworld", first, 9999L);
			if (same.status() != CivilizationWorldData.TownHallRegistrationStatus.EXISTING) {
				throw new AssertionError("same Town Hall marker should be idempotent");
			}

			CivilizationWorldData.TownHallRegistration duplicate = data.registerTownHall(
					"minecraft:overworld", first.offset(10, 0, 10), 9999L);
			if (duplicate.status() != CivilizationWorldData.TownHallRegistrationStatus.DUPLICATE) {
				throw new AssertionError("second Town Hall in one dimension should be rejected");
			}

			CivilizationWorldData.TownHallRegistration otherDimension = data.registerTownHall(
					"minecraft:the_nether", first, 2222L);
			if (otherDimension.status() != CivilizationWorldData.TownHallRegistrationStatus.REGISTERED
					|| data.getTownHallCoreCount() != 2) {
				throw new AssertionError("one Town Hall per dimension should be allowed");
			}

			JsonElement encoded = TownHallCore.CODEC.encodeStart(JsonOps.INSTANCE, core).getOrThrow();
			TownHallCore decoded = TownHallCore.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
			if (!core.equals(decoded)) {
				throw new AssertionError("Town Hall core Codec round-trip should preserve data");
			}
		}

		private static SettlementAdapter settlement(long foodStock, int stabilityDebt, int stability) {

		return settlementAt("test:village", 0, 64, 0, foodStock, stabilityDebt, stability);
	}

	private static SettlementAdapter emptySettlementAt(String source, int centerX, int centerY, int centerZ) {
		return new SettlementAdapter(
				source,
				"minecraft:overworld",
				centerX,
				centerY,
				centerZ,
				0L,
				"test",
				0,
				0L,
				1,
				0,
				100,
				FoodDemandModel.EVENT_STABLE
		);
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
				FoodDemandModel.EVENT_STABLE
		);
	}

	private static void equalsLong(long expected, long actual, String label) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + ", got " + actual);
		}
	}

	private static void equalsInt(int expected, int actual, String label) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + ", got " + actual);
		}
	}

	private static void equalsString(String expected, String actual, String label) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + ", got " + actual);
		}
	}
}
