package com.civilizationmod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * Initializes aggregate resource state for a newly discovered settlement.
 *
 * <p>The provider interfaces keep world observation separate from the pure
 * simulation model. A future chest, farm, or external-mod adapter can replace
 * the food provider without changing FoodDemandModel.</p>
 */
public final class FoodStockProvider {
	public static final int DEFAULT_POPULATION = 20;
	public static final long DEFAULT_FOOD_STOCK = 100L;

	private FoodStockProvider() {
	}

	public static SettlementAdapter initialize(
			ServerLevel level,
			SettlementAdapter settlement,
			SettlementPopulationProvider populationProvider,
			SettlementFoodProvider foodProvider
	) {
		return initialize(level, settlement, populationProvider, foodProvider, null);
	}

	public static SettlementAdapter initialize(
			ServerLevel level,
			SettlementAdapter settlement,
			SettlementPopulationProvider populationProvider,
			SettlementFoodProvider foodProvider,
			BlockPos observationOrigin
	) {
		if (hasExistingState(settlement)) {
			return settlement;
		}

		OptionalInt observedPopulation = populationProvider.findPopulation(level, settlement, observationOrigin);
		int population = observedPopulation.isPresent()
				? Math.max(0, observedPopulation.getAsInt())
				: DEFAULT_POPULATION;
		OptionalLong observedFoodStock = foodProvider.findFoodStock(level, settlement);
		long foodStock = observedFoodStock.isPresent()
				? Math.max(0L, observedFoodStock.getAsLong())
				: DEFAULT_FOOD_STOCK;

		return new SettlementAdapter(
				settlement.source(),
				settlement.dimension(),
				settlement.centerX(),
				settlement.centerY(),
				settlement.centerZ(),
				settlement.discoveredAt(),
				settlement.status(),
				population,
				foodStock,
				settlement.foodConsumption(),
				settlement.stabilityDebt(),
				settlement.stability(),
				FoodDemandModel.EVENT_STABLE
		);
	}

	public static SettlementAdapter refreshPopulation(
			ServerLevel level,
			SettlementAdapter settlement,
			SettlementPopulationProvider populationProvider
	) {
		return refreshPopulation(level, settlement, populationProvider, null);
	}

	public static SettlementAdapter refreshPopulation(
			ServerLevel level,
			SettlementAdapter settlement,
			SettlementPopulationProvider populationProvider,
			BlockPos observationOrigin
	) {
		OptionalInt observedPopulation = populationProvider.findPopulation(level, settlement, observationOrigin);
		return observedPopulation.isPresent()
				? settlement.withPopulation(Math.max(0, observedPopulation.getAsInt()))
				: settlement;
	}

	/** Compatibility bootstrap for pure Java callers that do not have a level. */
	public static SettlementAdapter initialize(SettlementAdapter settlement) {
		if (hasExistingState(settlement)) {
			return settlement;
		}
		return new SettlementAdapter(
				settlement.source(),
				settlement.dimension(),
				settlement.centerX(),
				settlement.centerY(),
				settlement.centerZ(),
				settlement.discoveredAt(),
				settlement.status(),
				DEFAULT_POPULATION,
				DEFAULT_FOOD_STOCK,
				settlement.foodConsumption(),
				settlement.stabilityDebt(),
				settlement.stability(),
				FoodDemandModel.EVENT_STABLE
		);
	}

	private static boolean hasExistingState(SettlementAdapter settlement) {
		return settlement.population() > 0
				|| settlement.foodStock() > 0L
				|| settlement.stabilityDebt() > 0
				|| settlement.stability() < StabilityDebt.MAX_STABILITY
				|| !FoodDemandModel.EVENT_STABLE.equals(settlement.lastFoodEvent());
	}
}
