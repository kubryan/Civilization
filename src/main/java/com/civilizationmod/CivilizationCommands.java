package com.civilizationmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;

import java.util.concurrent.CompletableFuture;

/** Server commands used to inspect and exercise the civilization world state. */
public final class CivilizationCommands {
		private static final int DEFAULT_SCAN_RADIUS = 128;
		private static final int MIN_SCAN_RADIUS = 16;
		private static final int MAX_SCAN_RADIUS = 512;
		private static final int[] SCAN_RADIUS_SUGGESTIONS = {32, 64, 128, 256};
		private static final int DEFAULT_BUILDING_SCAN_RADIUS = 32;
		private static final int MIN_BUILDING_SCAN_RADIUS = 8;
		private static final int MAX_BUILDING_SCAN_RADIUS = 256;
		private static final int[] BUILDING_SCAN_RADIUS_SUGGESTIONS = {16, 32, 64, 128};
	private static final int DEFAULT_SIMULATION_STEPS = 1;
	private static final int MIN_SIMULATION_STEPS = 1;
	private static final int MAX_SIMULATION_STEPS = 100;
	private static final int[] SIMULATION_STEPS_SUGGESTIONS = {1, 5, 10, 25, 50, 100};
	private static final SettlementPopulationProvider POPULATION_PROVIDER = new VanillaVillagerPopulationProvider();
	private static final SettlementFoodProvider FOOD_PROVIDER = new BootstrapFoodStockProvider();

	private CivilizationCommands() {
	}

	public static void register(
			CommandDispatcher<CommandSourceStack> dispatcher,
			CommandBuildContext registryAccess,
			Commands.CommandSelection environment
	) {
		dispatcher.register(rootCommand("civitas"));
		dispatcher.register(rootCommand("civilization"));
	}

		private static LiteralArgumentBuilder<CommandSourceStack> rootCommand(String literal) {
			return Commands.literal(literal)
					.then(Commands.literal("status").executes(context -> status(context.getSource())))
					.then(settlementCommand())
                        .then(buildingCommand())
                        .then(assignCommand())
                        .then(simulateCommand())
					.then(scanCommand("scan"))
					.then(scanCommand("sc"));
		}

	private static LiteralArgumentBuilder<CommandSourceStack> settlementCommand() {
		return Commands.literal("settlement")
				.executes(context -> settlementStatus(context.getSource()))
				.then(Commands.argument("index", IntegerArgumentType.integer(1))
						.suggests(CivilizationCommands::suggestSettlementIndex)
						.executes(context -> settlementStatus(
								context.getSource(),
								IntegerArgumentType.getInteger(context, "index")
						)));
	}

        private static LiteralArgumentBuilder<CommandSourceStack> assignCommand() {
                return Commands.literal("assign")
                        .then(Commands.argument("building_index", IntegerArgumentType.integer(1))
                                .suggests(CivilizationCommands::suggestBuildingIndex)
                                .executes(context -> assignResident(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "building_index"),
                                        null))
                                .then(Commands.argument("villager", EntityArgument.entity())
                                        .executes(context -> assignResident(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "building_index"),
                                                EntityArgument.getEntity(context, "villager")))));
        }

        private static LiteralArgumentBuilder<CommandSourceStack> simulateCommand() {
		return Commands.literal("simulate")
				.executes(context -> simulate(context.getSource(), DEFAULT_SIMULATION_STEPS))
				.then(Commands.argument("steps", IntegerArgumentType.integer(MIN_SIMULATION_STEPS, MAX_SIMULATION_STEPS))
						.suggests(CivilizationCommands::suggestSimulationSteps)
						.executes(context -> simulate(
								context.getSource(),
								IntegerArgumentType.getInteger(context, "steps")
						)));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> scanCommand(String literal) {
		return Commands.literal(literal)
				.executes(context -> scan(context.getSource(), DEFAULT_SCAN_RADIUS))
				.then(Commands.argument("radius", IntegerArgumentType.integer(MIN_SCAN_RADIUS, MAX_SCAN_RADIUS))
						.suggests(CivilizationCommands::suggestScanRadius)
						.executes(context -> scan(
								context.getSource(),
								IntegerArgumentType.getInteger(context, "radius")
						)));
	}

		private static LiteralArgumentBuilder<CommandSourceStack> buildingCommand() {
			LiteralArgumentBuilder<CommandSourceStack> scan = Commands.literal("scan")
					.executes(context -> scanBuildings(context.getSource(), DEFAULT_BUILDING_SCAN_RADIUS))
					.then(Commands.argument(
							"radius",
							IntegerArgumentType.integer(MIN_BUILDING_SCAN_RADIUS, MAX_BUILDING_SCAN_RADIUS))
							.suggests(CivilizationCommands::suggestBuildingScanRadius)
							.executes(context -> scanBuildings(
									context.getSource(),
									IntegerArgumentType.getInteger(context, "radius"))));

			LiteralArgumentBuilder<CommandSourceStack> inspect = Commands.literal("inspect")
					.then(Commands.argument("index", IntegerArgumentType.integer(1))
							.suggests(CivilizationCommands::suggestBuildingIndex)
							.executes(context -> inspectBuilding(
									context.getSource(),
									IntegerArgumentType.getInteger(context, "index"))));

				LiteralArgumentBuilder<CommandSourceStack> generate = Commands.literal("generate")
						.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
						.executes(context -> generateTestBuilding(context.getSource()));

			return Commands.literal("building")
					.executes(context -> buildingList(context.getSource()))
					.then(scan)
					.then(Commands.literal("list").executes(context -> buildingList(context.getSource())))
					.then(inspect)
					.then(generate);
		}

		private static int status(CommandSourceStack source) {
		MinecraftServer server = source.getServer();
		CivilizationWorldData data = CivilizationWorldData.get(server);
		FoodSimulationSummary food = data.getFoodSummary();

		source.sendSuccess(() -> CivilizationMessages.translatable(
				"civilizationmod.command.status",
				data.getSchemaVersion(),
				data.getSimulationSteps(),
				data.getSettlementCount(),
				food.population(),
				food.foodStock(),
				food.stabilityDebt(),
				food.stability(),
				foodEvent(food.lastFoodEvent())), false);

		for (int index = 1; index <= data.getSettlementCount(); index++) {
			final int settlementIndex = index;
			final SettlementAdapter settlement = data.getSettlement(settlementIndex);
			source.sendSuccess(() -> settlementEntry(settlementIndex, settlement), false);
		}
		return 1;
	}

	private static int settlementStatus(CommandSourceStack source) {
		MinecraftServer server = source.getServer();
		CivilizationWorldData data = CivilizationWorldData.get(server);
		if (data.getSettlementCount() == 0) {
			source.sendSuccess(() -> CivilizationMessages.translatable(
					"civilizationmod.command.settlement.none"), false);
			return 0;
		}

		for (int index = 1; index <= data.getSettlementCount(); index++) {
			final int settlementIndex = index;
			SettlementAdapter settlement = data.getSettlement(settlementIndex);
			source.sendSuccess(() -> settlementEntry(settlementIndex, settlement), false);
		}
		return data.getSettlementCount();
	}

	private static int settlementStatus(CommandSourceStack source, int index) {
		CivilizationWorldData data = CivilizationWorldData.get(source.getServer());
		SettlementAdapter settlement = data.getSettlement(index);
		if (settlement == null) {
			source.sendSuccess(() -> CivilizationMessages.translatable(
					"civilizationmod.command.settlement.invalid", index, data.getSettlementCount()), false);
			return 0;
		}

		source.sendSuccess(() -> settlementEntry(index, settlement), false);
		return 1;
	}

	private static Component settlementEntry(int index, SettlementAdapter settlement) {
		return CivilizationMessages.translatable(
				"civilizationmod.command.settlement.entry",
				index,
				settlement.source(),
				settlement.dimension(),
				settlement.centerX(),
				settlement.centerY(),
				settlement.centerZ(),
				settlement.population(),
				FoodDemandModel.calculateDemand(settlement.population(), settlement.foodConsumption()),
				settlement.foodStock(),
				settlement.foodConsumption(),
				settlement.stabilityDebt(),
				settlement.stability(),
				foodEvent(settlement.lastFoodEvent()));
	}

		private static int generateTestBuilding(CommandSourceStack source) {
			ServerLevel level = source.getLevel();
			BlockPos origin = BlockPos.containing(source.getPosition());
			BuildingTestStructureGenerator.GenerationResult result =
					BuildingTestStructureGenerator.generate(level, origin);
			if (!result.generated()) {
				source.sendSuccess(() -> CivilizationMessages.translatable(
						"civilizationmod.command.building.generate.failed",
						generationFailureReason(result.reason())), false);
				return 0;
			}

			source.sendSuccess(() -> CivilizationMessages.translatable(
					"civilizationmod.command.building.generate.result",
					result.doorPosition().getX(),
					result.doorPosition().getY(),
					result.doorPosition().getZ(),
					result.markerPosition().getX(),
					result.markerPosition().getY(),
					result.markerPosition().getZ()), false);
			return 1;
		}

		private static Component generationFailureReason(String reason) {
			return switch (reason) {
				case "existing_item_frame" -> Component.translatable(
						"civilizationmod.command.building.generate.failure.existing_item_frame");
				case "unloaded" -> Component.translatable(
						"civilizationmod.command.building.generate.failure.unloaded");
				case "frame_invalid" -> Component.translatable(
						"civilizationmod.command.building.generate.failure.frame_invalid");
				case "entity_spawn_failed" -> Component.translatable(
						"civilizationmod.command.building.generate.failure.entity_spawn_failed");
				default -> Component.translatable(
						"civilizationmod.command.building.generate.failure.invalid_context");
			};
		}

		private static int scanBuildings(CommandSourceStack source, int radius) {
			ServerLevel level = source.getLevel();
			BlockPos origin = BlockPos.containing(source.getPosition());
			CivilizationWorldData data = CivilizationWorldData.get(source.getServer());
			BuildingScanSummary summary = data.scanBuildingMarkers(
					level,
					origin,
				radius,
				source.getServer().getTickCount());
			source.sendSuccess(() -> CivilizationMessages.translatable(
					"civilizationmod.command.building.scan.result",
					radius,
					summary.detected(),
					summary.updated(),
					summary.bound(),
					summary.valid(),
					summary.invalid()), false);
			return summary.updated();
		}

		private static int buildingList(CommandSourceStack source) {
			CivilizationWorldData data = CivilizationWorldData.get(source.getServer());
			if (data.getBuildingCount() == 0) {
				source.sendSuccess(() -> CivilizationMessages.translatable(
						"civilizationmod.command.building.none"), false);
				return 0;
			}

			for (int index = 1; index <= data.getBuildingCount(); index++) {
				final int buildingIndex = index;
				BuildingObservation building = data.getBuilding(buildingIndex);
				source.sendSuccess(() -> buildingEntry(data, buildingIndex, building), false);
			}
			return data.getBuildingCount();
		}

		private static int inspectBuilding(CommandSourceStack source, int index) {
			CivilizationWorldData data = CivilizationWorldData.get(source.getServer());
			BuildingObservation building = data.getBuilding(index);
			if (building == null) {
				source.sendSuccess(() -> CivilizationMessages.translatable(
						"civilizationmod.command.building.invalid", index, data.getBuildingCount()), false);
				return 0;
			}

			source.sendSuccess(() -> buildingEntry(data, index, building), false);
			return 1;
		}

		private static Component buildingEntry(CivilizationWorldData data, int index, BuildingObservation building) {
			return CivilizationMessages.translatable(
					"civilizationmod.command.building.entry",
					index,
					buildingFunction(building.functionId()),
					building.dimension(),
					building.markerX(),
					building.markerY(),
					building.markerZ(),
					buildingBinding(data, building),
					buildingStatus(building.status()),
                        buildingValidationStatus(building.validationStatus()),
                        buildingValidationReason(building.validationReason()),
                        buildingResident(building),
                        buildingResidence(building),
                        building.firstSeen(),
                        building.lastSeen(),
                        buildingStorage(building));
		}

        private static Component buildingResidence(BuildingObservation building) {
                if (!BuildingFunction.RESIDENCE.id().equals(building.functionId())) {
                        return Component.translatable("civilizationmod.building.residence.not_applicable");
                }
                return Component.translatable(
                        "civilizationmod.building.residence.summary",
                        building.bedCount(),
                        building.capacity());
        }

        private static Component buildingStorage(BuildingObservation building) {
                BuildingStorageSnapshot storage = building.storageSnapshot();
                if (!storage.scanned()) {
                        return Component.translatable("civilizationmod.building.storage.unscanned");
                }

                MutableComponent items = Component.empty();
                int shown = 0;
                for (BuildingStorageItem item : storage.items()) {
                        if (shown > 0) {
                                items.append(Component.literal(", "));
                        }
                        items.append(Component.translatable(
                                "civilizationmod.building.storage.item",
                                item.itemId(),
                                item.count()));
                        shown++;
                }
                if (shown == 0) {
                        items.append(Component.translatable("civilizationmod.building.storage.empty"));
                }

                return Component.translatable(
                        "civilizationmod.building.storage.summary",
                        storage.containerCount(),
                        storage.itemTypeCount(),
                        storage.totalItemCount(),
                        storage.unloadedBlockCount(),
                        storage.lastScannedAt(),
                        items);
        }

		private static Component buildingFunction(String functionId) {
                if (BuildingFunction.WAREHOUSE.id().equals(functionId)) {
                    return Component.translatable("civilizationmod.building.function.warehouse");
                }
                if (BuildingFunction.RESIDENCE.id().equals(functionId)) {
                    return Component.translatable("civilizationmod.building.function.residence");
                }
                return Component.translatable("civilizationmod.building.function.unknown");
		}

		private static Component buildingStatus(String status) {
			return BuildingObservation.STATUS_BOUND.equals(status)
					? Component.translatable("civilizationmod.building.status.bound")
					: Component.translatable("civilizationmod.building.status.unbound");
		}

		private static Component buildingValidationStatus(String status) {
			if (BuildingObservation.VALIDATION_VALID.equals(status)) {
				return Component.translatable("civilizationmod.building.validation.valid");
			}
			if (BuildingObservation.VALIDATION_INVALID.equals(status)) {
				return Component.translatable("civilizationmod.building.validation.invalid");
			}
			return Component.translatable("civilizationmod.building.validation.detected");
		}

		        private static Component buildingResident(BuildingObservation building) {
                if (!building.hasResident()) {
                        return Component.translatable("civilizationmod.building.resident.unassigned");
                }
                return Component.translatable(
                        "civilizationmod.building.resident.assigned",
                        building.residentName().isBlank() ? building.residentUuid() : building.residentName(),
                        building.residentUuid());
        }

        private static Component buildingValidationReason(String reason) {

			if (BuildingObservation.VALIDATION_REASON_VALID.equals(reason)) {
				return Component.translatable("civilizationmod.building.validation.reason.valid");
			}
			if (BuildingObservation.VALIDATION_REASON_NO_DOOR.equals(reason)) {
				return Component.translatable("civilizationmod.building.validation.reason.no_door");
			}
			if (BuildingObservation.VALIDATION_REASON_UNLOADED.equals(reason)) {
				return Component.translatable("civilizationmod.building.validation.reason.unloaded");
			}
			if (BuildingObservation.VALIDATION_REASON_NO_ROOM.equals(reason)) {
				return Component.translatable("civilizationmod.building.validation.reason.no_room");
			}
			if (BuildingObservation.VALIDATION_REASON_NO_FLOOR.equals(reason)) {
				return Component.translatable("civilizationmod.building.validation.reason.no_floor");
			}
			if (BuildingObservation.VALIDATION_REASON_NO_CEILING.equals(reason)) {
				return Component.translatable("civilizationmod.building.validation.reason.no_ceiling");
			}
			if (BuildingObservation.VALIDATION_REASON_NO_WALLS.equals(reason)) {
				return Component.translatable("civilizationmod.building.validation.reason.no_walls");
			}
			if (BuildingObservation.VALIDATION_REASON_NO_ENTRY.equals(reason)) {
				return Component.translatable("civilizationmod.building.validation.reason.no_entry");
			}
                if (BuildingObservation.VALIDATION_REASON_NO_CONTAINER.equals(reason)) {
                    return Component.translatable("civilizationmod.building.validation.reason.no_container");
                }
                if (BuildingObservation.VALIDATION_REASON_INSUFFICIENT_BEDS.equals(reason)) {
                    return Component.translatable("civilizationmod.building.validation.reason.insufficient_beds");
                }
			if (BuildingObservation.VALIDATION_REASON_MARKER_NOT_AT_DOOR.equals(reason)) {
				return Component.translatable("civilizationmod.building.validation.reason.marker_not_at_door");
			}
			if (BuildingObservation.VALIDATION_REASON_MARKER_AMBIGUOUS.equals(reason)) {
				return Component.translatable("civilizationmod.building.validation.reason.marker_ambiguous");
			}
			if (BuildingObservation.VALIDATION_REASON_SCAN_LIMIT.equals(reason)) {
				return Component.translatable("civilizationmod.building.validation.reason.scan_limit");
			}
                if (BuildingObservation.VALIDATION_REASON_NO_CONTEXT.equals(reason)) {
                    return Component.translatable("civilizationmod.building.validation.reason.no_context");
                }
                if (BuildingObservation.VALIDATION_REASON_TERRITORY_MISSING.equals(reason)) {
                    return Component.translatable("civilizationmod.building.validation.reason.territory_missing");
                }
                if (BuildingObservation.VALIDATION_REASON_TERRITORY_TOO_LARGE.equals(reason)) {
                    return Component.translatable("civilizationmod.building.validation.reason.territory_too_large");
                }
                if (BuildingObservation.VALIDATION_REASON_TERRITORY_WRONG_DIMENSION.equals(reason)) {
                    return Component.translatable("civilizationmod.building.validation.reason.territory_wrong_dimension");
                }
                if (BuildingObservation.VALIDATION_REASON_MARKER_OUTSIDE_TERRITORY.equals(reason)) {
                    return Component.translatable("civilizationmod.building.validation.reason.marker_outside_territory");
                }
                if (BuildingObservation.VALIDATION_REASON_MARKER_NOT_ATTACHED.equals(reason)) {
                    return Component.translatable("civilizationmod.building.validation.reason.marker_not_attached");
                }
                if (BuildingObservation.VALIDATION_REASON_DUPLICATE_TERRITORY.equals(reason)) {
                    return Component.translatable("civilizationmod.building.validation.reason.duplicate_territory");
                }
                return Component.translatable("civilizationmod.building.validation.reason.unknown");
		}

		private static Component buildingBinding(CivilizationWorldData data, BuildingObservation building) {
			for (int index = 1; index <= data.getSettlementCount(); index++) {
				SettlementAdapter settlement = data.getSettlement(index);
				if (settlement.dimension().equals(building.settlementDimension())
						&& settlement.centerX() == building.settlementX()
						&& settlement.centerY() == building.settlementY()
						&& settlement.centerZ() == building.settlementZ()) {
					return Component.translatable("civilizationmod.building.binding.index", index);
				}
			}
			return Component.translatable("civilizationmod.building.binding.unbound");
		}

		        private static int assignResident(CommandSourceStack source, int index, Entity explicitTarget)
                throws CommandSyntaxException {
                CivilizationWorldData data = CivilizationWorldData.get(source.getServer());
                BuildingObservation building = data.getBuilding(index);
                if (building == null) {
                        source.sendSuccess(() -> CivilizationMessages.translatable(
                                "civilizationmod.command.assign.invalid_building",
                                index,
                                data.getBuildingCount()), false);
                        return 0;
                }
                if (!BuildingObservation.VALIDATION_VALID.equals(building.validationStatus())) {
                        source.sendSuccess(() -> CivilizationMessages.translatable(
                                "civilizationmod.command.assign.building_not_valid",
                                index,
                                buildingValidationReason(building.validationReason())), false);
                        return 0;
                }

                ServerLevel level = source.getLevel();
                Villager villager;
                if (explicitTarget != null) {
                        if (!(explicitTarget instanceof Villager candidate)) {
                                source.sendSuccess(() -> CivilizationMessages.translatable(
                                        "civilizationmod.command.assign.target_not_villager"), false);
                                return 0;
                        }
                        if (candidate.level() != level) {
                                source.sendSuccess(() -> CivilizationMessages.translatable(
                                        "civilizationmod.command.assign.target_wrong_dimension"), false);
                                return 0;
                        }
                        villager = candidate;
                } else {
                        ServerPlayer player = source.getPlayer();
                        if (player == null) {
                                source.sendSuccess(() -> CivilizationMessages.translatable(
                                        "civilizationmod.command.assign.requires_player"), false);
                                return 0;
                        }
                        villager = BuildingResidentService.findLookedAtVillager(level, player);
                        if (villager == null) {
                                source.sendSuccess(() -> CivilizationMessages.translatable(
                                        "civilizationmod.command.assign.villager_not_found"), false);
                                return 0;
                        }
                }

                if (!villager.isAlive()) {
                        source.sendSuccess(() -> CivilizationMessages.translatable(
                                "civilizationmod.command.assign.target_unavailable"), false);
                        return 0;
                }
                BuildingObservation existingAssignment = data.findBuildingAssignedTo(villager.getStringUUID());
                if (existingAssignment != null && existingAssignment != building) {
                        source.sendSuccess(() -> CivilizationMessages.translatable(
                                "civilizationmod.command.assign.already_assigned",
                                existingAssignment.markerX(),
                                existingAssignment.markerY(),
                                existingAssignment.markerZ()), false);
                        return 0;
                }

                BuildingObservation replacement = building.withResident(
                        villager.getUUID(),
                        villager.getName().getString());
                if (!data.replaceBuilding(building, replacement)) {
                        source.sendSuccess(() -> CivilizationMessages.translatable(
                                "civilizationmod.command.assign.save_failed"), false);
                        return 0;
                }
                BuildingResidentService.applyAssignmentVisual(villager, replacement.functionId());
                source.sendSuccess(() -> CivilizationMessages.translatable(
                        "civilizationmod.command.assign.success",
                        index,
                        villager.getName(),
                        villager.getStringUUID()), false);
                return 1;
        }

        private static int scan(CommandSourceStack source, int radius) {

		ServerLevel level = source.getLevel();
		BlockPos origin = BlockPos.containing(source.getPosition());
		BlockPos villageCenter = level.findNearestMapStructure(
				StructureTags.VILLAGE,
				origin,
				radius,
				false
		);

		if (villageCenter == null) {
			source.sendSuccess(() -> CivilizationMessages.translatable(
					"civilizationmod.command.scan.none", radius), false);
			return 0;
		}

		MinecraftServer server = source.getServer();
		CivilizationWorldData data = CivilizationWorldData.get(server);
		SettlementAdapter probe = new SettlementAdapter(
				"minecraft:village",
				level.dimension().identifier().toString(),
				villageCenter.getX(),
				villageCenter.getY(),
				villageCenter.getZ(),
				server.getTickCount(),
				"discovered",
				0,
				0L,
				FoodDemandModel.DEFAULT_FOOD_CONSUMPTION,
				0,
				StabilityDebt.MAX_STABILITY,
				FoodDemandModel.EVENT_STABLE
		);

		SettlementAdapter existing = data.findSettlement(probe);
		if (existing != null) {
			SettlementAdapter refreshed = FoodStockProvider.refreshPopulation(level, existing, POPULATION_PROVIDER, origin);
			data.replaceSettlement(existing, refreshed);
			source.sendSuccess(() -> CivilizationMessages.translatable(
					"civilizationmod.command.scan.refreshed",
					villageCenter.getX(),
					villageCenter.getY(),
					villageCenter.getZ(),
					refreshed.dimension(),
					refreshed.population()), false);
			return 0;
		}

		SettlementAdapter settlement = FoodStockProvider.initialize(
				level,
				probe,
				POPULATION_PROVIDER,
				FOOD_PROVIDER,
				origin
		);
		boolean added = data.addSettlement(settlement);

		source.sendSuccess(() -> CivilizationMessages.translatable(
				"civilizationmod.command.scan.registered",
				villageCenter.getX(),
				villageCenter.getY(),
				villageCenter.getZ(),
				settlement.dimension()), false);
		return added ? 1 : 0;
	}

	private static CompletableFuture<Suggestions> suggestScanRadius(
			CommandContext<CommandSourceStack> context,
			SuggestionsBuilder builder
	) {
		for (int radius : SCAN_RADIUS_SUGGESTIONS) {
			builder.suggest(radius);
		}
		return builder.buildFuture();
	}

		private static CompletableFuture<Suggestions> suggestBuildingScanRadius(
				CommandContext<CommandSourceStack> context,
				SuggestionsBuilder builder
		) {
			for (int radius : BUILDING_SCAN_RADIUS_SUGGESTIONS) {
				builder.suggest(radius);
			}
			return builder.buildFuture();
		}

		private static CompletableFuture<Suggestions> suggestBuildingIndex(
				CommandContext<CommandSourceStack> context,
				SuggestionsBuilder builder
		) {
			CivilizationWorldData data = CivilizationWorldData.get(context.getSource().getServer());
			for (int index = 1; index <= data.getBuildingCount(); index++) {
				builder.suggest(index);
			}
			return builder.buildFuture();
		}

		private static CompletableFuture<Suggestions> suggestSettlementIndex(
			CommandContext<CommandSourceStack> context,
			SuggestionsBuilder builder
	) {
		CivilizationWorldData data = CivilizationWorldData.get(context.getSource().getServer());
		for (int index = 1; index <= data.getSettlementCount(); index++) {
			builder.suggest(index);
		}
		return builder.buildFuture();
	}

	private static CompletableFuture<Suggestions> suggestSimulationSteps(
			CommandContext<CommandSourceStack> context,
			SuggestionsBuilder builder
	) {
		for (int steps : SIMULATION_STEPS_SUGGESTIONS) {
			builder.suggest(steps);
		}
		return builder.buildFuture();
	}

	private static Component foodEvent(String event) {
		return switch (event) {
			case FoodDemandModel.EVENT_SHORTAGE -> Component.translatable("civilizationmod.food.event.shortage");
			case FoodDemandModel.EVENT_SURPLUS -> Component.translatable("civilizationmod.food.event.surplus");
			default -> Component.translatable("civilizationmod.food.event.stable");
		};
	}

	private static int simulate(CommandSourceStack source, int steps) {
		MinecraftServer server = source.getServer();
		CivilizationWorldData data = CivilizationWorldData.get(server);
		FoodSimulationSummary summary = null;
		for (int step = 0; step < steps; step++) {
			summary = data.advanceSimulation();
		}
		if (summary == null) {
			return 0;
		}

		FoodSimulationSummary result = summary;
		source.sendSuccess(() -> CivilizationMessages.translatable(
				"civilizationmod.command.simulate",
				result.simulationSteps(),
				result.population(),
				result.foodDemand(),
				result.foodConsumed(),
				result.foodShortage(),
				result.foodStock(),
				result.stabilityDebt(),
				result.stability(),
				foodEvent(result.lastFoodEvent())), true);
		return steps;
	}
}
