package com.civilizationmod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.OptionalInt;

/**
 * Counts loaded vanilla villagers in a bounded area around a settlement.
 *
 * <p>This is intentionally an observation provider. It does not force chunk
 * loading and it does not treat an unloaded village as having zero population;
 * the caller can apply a configured fallback when no villagers are observed.</p>
 */
public final class VanillaVillagerPopulationProvider implements SettlementPopulationProvider {
	public static final int SEARCH_HORIZONTAL_RADIUS = 64;
	public static final int SEARCH_VERTICAL_RADIUS = 32;

	@Override
	public OptionalInt findPopulation(ServerLevel level, SettlementAdapter settlement) {
		return findPopulation(level, settlement, null);
	}

	@Override
	public OptionalInt findPopulation(ServerLevel level, SettlementAdapter settlement, BlockPos observationOrigin) {
		int originX = observationOrigin == null ? settlement.centerX() : observationOrigin.getX();
		int originY = observationOrigin == null ? settlement.centerY() : observationOrigin.getY();
		int originZ = observationOrigin == null ? settlement.centerZ() : observationOrigin.getZ();
		AABB bounds = new AABB(
				originX - SEARCH_HORIZONTAL_RADIUS,
				originY - SEARCH_VERTICAL_RADIUS,
				originZ - SEARCH_HORIZONTAL_RADIUS,
				originX + SEARCH_HORIZONTAL_RADIUS + 1.0D,
				originY + SEARCH_VERTICAL_RADIUS + 1.0D,
				originZ + SEARCH_HORIZONTAL_RADIUS + 1.0D
		);
		List<Villager> villagers = level.getEntities(
				EntityTypeTest.forClass(Villager.class),
				bounds,
				villager -> true
		);
		return villagers.isEmpty() ? OptionalInt.empty() : OptionalInt.of(villagers.size());
	}
}
