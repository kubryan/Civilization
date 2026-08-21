package com.civilizationmod;

/** Result summary returned after one or more aggregate simulation steps. */
public record FoodSimulationSummary(
		long simulationSteps,
		int settlementCount,
		int population,
		long foodDemand,
		long foodConsumed,
		long foodShortage,
		long foodStock,
		int stabilityDebt,
		int stability,
		String lastFoodEvent
) {
	public static FoodSimulationSummary empty(long simulationSteps, int settlementCount) {
		return new FoodSimulationSummary(
				simulationSteps,
				settlementCount,
				0,
				0L,
				0L,
				0L,
				0L,
				0,
				settlementCount == 0 ? StabilityDebt.MAX_STABILITY : 0,
				FoodDemandModel.EVENT_STABLE
		);
	}
}
