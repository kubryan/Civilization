package com.civilizationmod;

import net.minecraft.server.level.ServerLevel;

import java.util.OptionalLong;

/** Deterministic first-version food source used when no world inventory adapter is configured. */
public final class BootstrapFoodStockProvider implements SettlementFoodProvider {
	@Override
	public OptionalLong findFoodStock(ServerLevel level, SettlementAdapter settlement) {
		return OptionalLong.of(FoodStockProvider.DEFAULT_FOOD_STOCK);
	}
}
