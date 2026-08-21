package com.civilizationmod;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Persistent adapter record for a settlement discovered in the world.
 *
 * <p>The record deliberately stores source metadata rather than taking a hard
 * dependency on the mod that created the settlement. The resource fields are
 * aggregate simulation state, not a per-NPC inventory.</p>
 */
public record SettlementAdapter(
		String source,
		String dimension,
		int centerX,
		int centerY,
		int centerZ,
		long discoveredAt,
		String status,
		int population,
		long foodStock,
		int foodConsumption,
		int stabilityDebt,
		int stability,
		String lastFoodEvent
) {
	public static final int DEDUP_HORIZONTAL_RADIUS = 64;
	public static final int DEDUP_VERTICAL_RADIUS = 32;

	public static final Codec<SettlementAdapter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("source").forGetter(SettlementAdapter::source),
			Codec.STRING.fieldOf("dimension").forGetter(SettlementAdapter::dimension),
			Codec.INT.fieldOf("center_x").forGetter(SettlementAdapter::centerX),
			Codec.INT.fieldOf("center_y").forGetter(SettlementAdapter::centerY),
			Codec.INT.fieldOf("center_z").forGetter(SettlementAdapter::centerZ),
			Codec.LONG.fieldOf("discovered_at").forGetter(SettlementAdapter::discoveredAt),
			Codec.STRING.fieldOf("status").forGetter(SettlementAdapter::status),
			Codec.INT.optionalFieldOf("population", 0).forGetter(SettlementAdapter::population),
			Codec.LONG.optionalFieldOf("food_stock", 0L).forGetter(SettlementAdapter::foodStock),
			Codec.INT.optionalFieldOf("food_consumption", FoodDemandModel.DEFAULT_FOOD_CONSUMPTION).forGetter(SettlementAdapter::foodConsumption),
			Codec.INT.optionalFieldOf("stability_debt", 0).forGetter(SettlementAdapter::stabilityDebt),
			Codec.INT.optionalFieldOf("stability", StabilityDebt.MAX_STABILITY).forGetter(SettlementAdapter::stability),
			Codec.STRING.optionalFieldOf("last_food_event", FoodDemandModel.EVENT_STABLE).forGetter(SettlementAdapter::lastFoodEvent)
	).apply(instance, SettlementAdapter::new));

	public SettlementAdapter {
		population = Math.max(0, population);
		foodStock = Math.max(0L, foodStock);
		foodConsumption = Math.max(1, foodConsumption);
		stabilityDebt = Math.max(0, stabilityDebt);
		stability = StabilityDebt.clampStability(stability);
		lastFoodEvent = lastFoodEvent == null || lastFoodEvent.isBlank()
				? FoodDemandModel.EVENT_STABLE
				: lastFoodEvent;
	}

	public boolean isAt(String dimension, int x, int y, int z) {
		return this.dimension.equals(dimension)
				&& this.centerX == x
				&& this.centerY == y
				&& this.centerZ == z;
	}

	public boolean isSameSettlement(SettlementAdapter other) {
		long deltaX = (long) this.centerX - other.centerX;
		long deltaY = (long) this.centerY - other.centerY;
		long deltaZ = (long) this.centerZ - other.centerZ;
		return this.source.equals(other.source)
				&& this.dimension.equals(other.dimension)
				&& Math.abs(deltaX) <= DEDUP_HORIZONTAL_RADIUS
				&& Math.abs(deltaZ) <= DEDUP_HORIZONTAL_RADIUS
				&& Math.abs(deltaY) <= DEDUP_VERTICAL_RADIUS;
	}

	public SettlementAdapter withPopulation(int population) {
		return new SettlementAdapter(
				this.source,
				this.dimension,
				this.centerX,
				this.centerY,
				this.centerZ,
				this.discoveredAt,
				this.status,
				population,
				this.foodStock,
				this.foodConsumption,
				this.stabilityDebt,
				this.stability,
				this.lastFoodEvent
		);
	}

	public SettlementAdapter withFoodState(long foodStock, int stabilityDebt, int stability, String lastFoodEvent) {
		return new SettlementAdapter(
				this.source,
				this.dimension,
				this.centerX,
				this.centerY,
				this.centerZ,
				this.discoveredAt,
				this.status,
				this.population,
				foodStock,
				this.foodConsumption,
				stabilityDebt,
				stability,
				lastFoodEvent
		);
	}
}
