package com.civilizationmod;

/**
 * Low-frequency, settlement-level food simulation.
 *
 * <p>This first slice intentionally uses aggregate values. It does not inspect
 * individual villagers, inventories, chests, or external-mod internals.</p>
 */
public final class FoodDemandModel {
	public static final int DEFAULT_FOOD_CONSUMPTION = 1;
	public static final String EVENT_STABLE = "stable";
	public static final String EVENT_SURPLUS = "surplus";
	public static final String EVENT_SHORTAGE = "shortage";

	private FoodDemandModel() {
	}

	public static Result simulate(SettlementAdapter settlement) {
		long demand = calculateDemand(settlement.population(), settlement.foodConsumption());
		long consumed = Math.min(settlement.foodStock(), demand);
		long shortage = demand - consumed;
		long remainingStock = settlement.foodStock() - consumed;

		int nextDebt;
		int nextStability;
		String event;
		if (shortage > 0) {
			nextDebt = StabilityDebt.addShortage(settlement.stabilityDebt(), shortage);
			nextStability = StabilityDebt.applyShortage(
					settlement.stability(),
					StabilityDebt.STABILITY_LOSS_ON_SHORTAGE);
			event = EVENT_SHORTAGE;
		} else if (remainingStock > demand * 2) {
			nextDebt = StabilityDebt.reduceWithSurplus(settlement.stabilityDebt(), 1);
			nextStability = StabilityDebt.recoverWithSurplus(
					settlement.stability(),
					StabilityDebt.STABILITY_GAIN_ON_SURPLUS);
			event = EVENT_SURPLUS;
		} else {
			nextDebt = settlement.stabilityDebt();
			nextStability = settlement.stability();
			event = EVENT_STABLE;
		}

		SettlementAdapter updated = settlement.withFoodState(remainingStock, nextDebt, nextStability, event);
		return new Result(updated, demand, consumed, shortage);
	}

	public static long calculateDemand(int population, int foodConsumption) {
		return (long) Math.max(0, population) * Math.max(1, foodConsumption);
	}

	public record Result(
			SettlementAdapter settlement,
			long demand,
			long consumed,
			long shortage
	) {
	}
}
