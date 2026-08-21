package com.civilizationmod;

import net.minecraft.server.level.ServerLevel;

import java.util.OptionalLong;

/** Supplies an observed aggregate food stock for a settlement. */
@FunctionalInterface
public interface SettlementFoodProvider {
	OptionalLong findFoodStock(ServerLevel level, SettlementAdapter settlement);
}
