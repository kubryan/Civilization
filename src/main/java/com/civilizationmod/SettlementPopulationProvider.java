package com.civilizationmod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.OptionalInt;

/** Supplies an observed aggregate population for a settlement. */
@FunctionalInterface
public interface SettlementPopulationProvider {
	OptionalInt findPopulation(ServerLevel level, SettlementAdapter settlement);

	default OptionalInt findPopulation(ServerLevel level, SettlementAdapter settlement, BlockPos observationOrigin) {
		return findPopulation(level, settlement);
	}
}
