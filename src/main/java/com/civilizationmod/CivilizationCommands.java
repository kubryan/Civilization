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

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.npc.villager.Villager;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Server commands used to inspect and exercise the civilization world state. */
public final class CivilizationCommands {

			private static final int DEFAULT_BUILDING_SCAN_RADIUS = 32;
		private static final int MIN_BUILDING_SCAN_RADIUS = 8;
		private static final int MAX_BUILDING_SCAN_RADIUS = 256;
			private static final int[] BUILDING_SCAN_RADIUS_SUGGESTIONS = {16, 32, 64, 128};


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
										.then(helpCommand())
					.then(Commands.literal("status").executes(context -> status(context.getSource())))
					.then(buildingCommand())
					.then(townHallCommand())
					.then(assignCommand())
					.then(unassignCommand())
					.then(residentCommand());

		}

        private static LiteralArgumentBuilder<CommandSourceStack> helpCommand() {
                return Commands.literal("help")
                                .executes(context -> help(context.getSource()));
        }

        private static int help(CommandSourceStack source) {
                String[] helpKeys = {
                        "civilizationmod.command.help.title",
                        "civilizationmod.command.help.status",
                        "civilizationmod.command.help.building",
                        "civilizationmod.command.help.assign",
                        "civilizationmod.command.help.unassign",
                        "civilizationmod.command.help.resident",
                        "civilizationmod.command.help.townhall",
                        "civilizationmod.command.help.scan",
                        "civilizationmod.command.help.alias"
                };
                for (String helpKey : helpKeys) {
                        final String translationKey = helpKey;
                        source.sendSuccess(() -> CivilizationMessages.translatable(translationKey), false);
                }
                return helpKeys.length;
        }

        private static LiteralArgumentBuilder<CommandSourceStack> townHallCommand() {
                return Commands.literal("townhall")
                                .executes(context -> townHallStatus(context.getSource()))
                                .then(Commands.literal("radius")
                                                .then(Commands.argument("index", IntegerArgumentType.integer(1))
                                                                .suggests(CivilizationCommands::suggestTownHallIndex)
                                                                .then(Commands.argument(
                                                                                "radius",
                                                                                IntegerArgumentType.integer(1, TownHallCore.MAX_RADIUS))
                                                                                .suggests(CivilizationCommands::suggestTownHallRadius)
                                                                                .executes(context -> setTownHallRadius(
                                                                                                context.getSource(),
                                                                                                IntegerArgumentType.getInteger(context, "index"),
                                                                                                IntegerArgumentType.getInteger(context, "radius"))))));
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

                private static LiteralArgumentBuilder<CommandSourceStack> unassignCommand() {
                return Commands.literal("unassign")
                        .then(Commands.argument("building_index", IntegerArgumentType.integer(1))
                                .suggests(CivilizationCommands::suggestBuildingIndex)
                                .executes(context -> unassignResident(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "building_index"),
                                        null))
                                .then(Commands.argument("villager", EntityArgument.entity())
                                        .executes(context -> unassignResident(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "building_index"),
                                                EntityArgument.getEntity(context, "villager")))));
        }

        private static LiteralArgumentBuilder<CommandSourceStack> residentCommand() {
                return Commands.literal("resident")
                        .then(Commands.literal("list")
                                .executes(context -> residentList(context.getSource())));
        }

        private static int residentList(CommandSourceStack source) {
                CivilizationWorldData data = CivilizationWorldData.get(source.getServer());
                if (data.getResidents().isEmpty()) {
                        source.sendSuccess(() -> CivilizationMessages.translatable(
                                "civilizationmod.command.resident.none"), false);
                        return 0;
                }
                for (int index = 0; index < data.getResidents().size(); index++) {
                        final int residentIndex = index + 1;
                        ResidentRecord resident = data.getResidents().get(index);
                        source.sendSuccess(() -> CivilizationMessages.translatable(
                                "civilizationmod.command.resident.entry",
                                residentIndex,
                                resident.residentId(),
                                resident.entityUuid(),
                                resident.colonyId().isBlank() ? "-" : resident.colonyId(),
                                resident.homeBuildingKey().isBlank() ? "-" : resident.homeBuildingKey(),
                                resident.workBuildingKey().isBlank() ? "-" : resident.workBuildingKey(),
                                resident.role().isBlank() ? "-" : resident.role(),
                                resident.bodyType(),
                                resident.lifecycle(),
                                resident.name().isBlank() ? "-" : resident.name()), false);
                }
                return data.getResidents().size();
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

				return Commands.literal("building")

					.executes(context -> buildingList(context.getSource()))
					.then(scan)
					.then(Commands.literal("list").executes(context -> buildingList(context.getSource())))
										.then(inspect);

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

			private static int townHallStatus(CommandSourceStack source) {
			CivilizationWorldData data = CivilizationWorldData.get(source.getServer());
			if (data.getTownHallCoreCount() == 0) {
				source.sendSuccess(() -> CivilizationMessages.translatable(
						"civilizationmod.command.town_hall.none"), false);
				return 0;
			}

			                                for (int index = 1; index <= data.getTownHallCoreCount(); index++) {
                                        final int coreIndex = index;
                                        TownHallCore core = data.getTownHallCore(index);
                                        source.sendSuccess(() -> CivilizationMessages.translatable(
                                                        "civilizationmod.command.town_hall.entry",
                                                        coreIndex,
                                                        core.colonyId(),
                                                        core.dimension(),
                                                        core.markerX(),
                                                        core.markerY(),
                                                        core.markerZ(),
                                                        core.createdAt(),
                                                        core.radius()), false);
                                }

			return data.getTownHallCoreCount();
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

		        private static int setTownHallRadius(CommandSourceStack source, int index, int radius) {
                CivilizationWorldData data = CivilizationWorldData.get(source.getServer());
                CivilizationWorldData.TownHallRadiusUpdate update = data.updateTownHallRadius(index, radius);
                switch (update.status()) {
                        case UPDATED -> source.sendSuccess(() -> CivilizationMessages.translatable(
                                        "civilizationmod.command.town_hall.radius.updated",
                                        index,
                                        update.core().colonyId(),
                                        update.core().radius()), false);
                        case UNCHANGED -> source.sendSuccess(() -> CivilizationMessages.translatable(
                                        "civilizationmod.command.town_hall.radius.unchanged",
                                        index,
                                        update.core().radius()), false);
                        case OVERLAPPING -> source.sendSuccess(() -> CivilizationMessages.translatable(
                                        "civilizationmod.command.town_hall.radius.overlapping",
                                        index,
                                        update.core().colonyId()), false);
                        case INVALID -> source.sendSuccess(() -> CivilizationMessages.translatable(
                                        "civilizationmod.command.town_hall.radius.invalid",
                                        index,
                                        data.getTownHallCoreCount()), false);
                }
                return update.status() == CivilizationWorldData.TownHallRadiusUpdateStatus.UPDATED ? 1 : 0;
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
						summary.invalid(),
						summary.townHallsRegistered(),
						summary.townHallConflicts()), false);

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
                                                buildingResident(data, building),
                        buildingResidence(data, building),

                        building.firstSeen(),
                        building.lastSeen(),
                        buildingStorage(building));
		}

        private static Component buildingResidence(CivilizationWorldData data, BuildingObservation building) {
                if (!BuildingFunction.RESIDENCE.id().equals(building.functionId())) {
                        return Component.translatable("civilizationmod.building.residence.not_applicable");
                }
                return Component.translatable(
                        "civilizationmod.building.residence.summary",
                        building.bedCount(),
                        building.capacity(),
                        data.countActiveResidents(building),
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
				if (BuildingFunction.TOWN_HALL.id().equals(functionId)) {
					return Component.translatable("civilizationmod.building.function.town_hall");
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

        private static Component buildingResident(CivilizationWorldData data, BuildingObservation building) {
                List<ResidentRecord> residents = data.findActiveResidents(building);
                if (residents.isEmpty()) {
                        return Component.translatable("civilizationmod.building.resident.unassigned");
                }
                if (BuildingFunction.RESIDENCE.id().equals(building.functionId())) {
                        MutableComponent names = Component.empty();
                        for (int residentIndex = 0; residentIndex < residents.size(); residentIndex++) {
                                if (residentIndex > 0) {
                                        names.append(Component.translatable("civilizationmod.building.resident.separator"));
                                }
                                ResidentRecord resident = residents.get(residentIndex);
                                names.append(Component.literal(
                                        resident.name().isBlank() ? resident.entityUuid() : resident.name()));
                        }
                        return Component.translatable(
                                "civilizationmod.building.resident.capacity",
                                residents.size(),
                                building.capacity(),
                                names);
                }
                ResidentRecord resident = residents.get(0);
                return Component.translatable(
                        "civilizationmod.building.resident.assigned",
                        resident.name().isBlank() ? resident.entityUuid() : resident.name(),
                        resident.entityUuid());
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
                if (BuildingObservation.VALIDATION_REASON_RESIDENTS_OVER_CAPACITY.equals(reason)) {
                    return Component.translatable("civilizationmod.building.validation.reason.residents_over_capacity");
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
                if (BuildingObservation.VALIDATION_REASON_DUPLICATE_TOWN_HALL.equals(reason)) {
                    return Component.translatable("civilizationmod.building.validation.reason.duplicate_town_hall");
                }

                return Component.translatable("civilizationmod.building.validation.reason.unknown");
		}

		        private static Component buildingColonyBindingReason(BuildingObservation building) {
                return Component.translatable(
                        "civilizationmod.building.colony.reason." + building.colonyBindingReason());
        }

                private static Component buildingBinding(CivilizationWorldData data, BuildingObservation building) {
                if (building.isColonyBound()) {
                        return Component.translatable(
                                        "civilizationmod.building.binding.colony",
                                        building.colonyId());
                }
                if (data.isTownHallTransitionAllowed(building)) {
                        return Component.translatable(
                                "civilizationmod.building.binding.transition",
                                buildingColonyBindingReason(building));
                }
                return Component.translatable(
                                "civilizationmod.building.binding.unbound_reason",
                                buildingColonyBindingReason(building));
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
                        String currentValidationReason = building.validationReason();
                        source.sendSuccess(() -> CivilizationMessages.translatable(
                                "civilizationmod.command.assign.building_not_valid",
                                index,
                                buildingValidationReason(currentValidationReason)), false);
                        return 0;
                }
                if (!data.isBuildingOperational(building)) {
                        Component bindingReason = buildingColonyBindingReason(building);
                        source.sendSuccess(() -> CivilizationMessages.translatable(
                                "civilizationmod.command.assign.building_not_in_colony",
                                bindingReason), false);
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
                boolean alreadyAssignedToTarget = existingAssignment != null
                        && existingAssignment.isSameMarker(
                        building.dimension(),
                        building.markerX(),
                        building.markerY(),
                        building.markerZ());
                if (existingAssignment != null && !alreadyAssignedToTarget) {
                        source.sendSuccess(() -> CivilizationMessages.translatable(
                                "civilizationmod.command.assign.already_assigned",
                                existingAssignment.markerX(),
                                existingAssignment.markerY(),
                                existingAssignment.markerZ()), false);
                        return 0;
                }

                if (BuildingFunction.RESIDENCE.id().equals(building.functionId())) {
                        ItemFrame markerFrame = data.findBuildingMarker(level, building);
                        WarehouseTerritory territory = markerFrame == null
                                ? null
                                : WarehouseTerritory.read(markerFrame.getItem()).orElse(null);
                        int liveBedCount = ResidenceValidator.countBeds(level, territory);
                        int targetCapacity = building.capacity();
                        boolean missingContext = markerFrame == null || territory == null;
                        boolean insufficientBeds = !missingContext && liveBedCount < targetCapacity;
                        if (missingContext || insufficientBeds) {
                                String residenceValidationReason = insufficientBeds
                                        ? "civilizationmod.building.validation.reason.insufficient_beds"
                                        : "civilizationmod.building.validation.reason.no_context";
                                source.sendSuccess(() -> CivilizationMessages.translatable(
                                        "civilizationmod.command.assign.building_not_valid",
                                        index,
                                        Component.translatable(residenceValidationReason)), false);
                                return 0;
                        }
                        if (liveBedCount != building.bedCount()) {
                                BuildingObservation measured = building.withResidenceMeasurements(
                                        building.capacity(),
                                        liveBedCount);
                                if (!data.replaceBuilding(building, measured)) {
                                        source.sendSuccess(() -> CivilizationMessages.translatable(
                                                "civilizationmod.command.assign.save_failed"), false);
                                        return 0;
                                }
                                building = measured;
                        }
                        if (!alreadyAssignedToTarget && data.countActiveResidents(building) >= building.capacity()) {
                                int currentResidentCount = data.countActiveResidents(building);
                                int currentCapacity = building.capacity();
                                source.sendSuccess(() -> CivilizationMessages.translatable(
                                        "civilizationmod.command.assign.residence_full",
                                        index,
                                        currentResidentCount,
                                        currentCapacity), false);
                                return 0;
                                                        }
                }

                ResidentRecord resident = data.ensureResidentAssignment(
                        building,

                        villager.getUUID(),
                        villager.getName().getString(),
                        source.getServer().getTickCount());
                if (resident == null) {
                        source.sendSuccess(() -> CivilizationMessages.translatable(
                                "civilizationmod.command.assign.save_failed"), false);
                        return 0;
                }
                BuildingResidentService.applyAssignmentVisual(villager, building.functionId());
                                String successKey = data.isTownHallTransitionAllowed(building)
                        ? "civilizationmod.command.assign.success.transition"
                        : "civilizationmod.command.assign.success";
                source.sendSuccess(() -> CivilizationMessages.translatable(
                        successKey,
                        index,
                        villager.getName(),
                        villager.getStringUUID()), false);

                return 1;
        }

        private static int unassignResident(CommandSourceStack source, int index, Entity explicitTarget)
                throws CommandSyntaxException {
                CivilizationWorldData data = CivilizationWorldData.get(source.getServer());
                BuildingObservation building = data.getBuilding(index);
                if (building == null) {
                        source.sendSuccess(() -> CivilizationMessages.translatable(
                                "civilizationmod.command.unassign.invalid_building",
                                index,
                                data.getBuildingCount()), false);
                        return 0;
                }

                ServerLevel level = source.getLevel();
                Villager villager;
                if (explicitTarget != null) {
                        if (!(explicitTarget instanceof Villager candidate)) {
                                source.sendSuccess(() -> CivilizationMessages.translatable(
                                        "civilizationmod.command.unassign.target_not_villager"), false);
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

                BuildingObservation assignedBuilding = data.findBuildingAssignedTo(villager.getStringUUID());
                if (assignedBuilding == null || !assignedBuilding.isSameMarker(
                        building.dimension(),
                        building.markerX(),
                        building.markerY(),
                        building.markerZ())) {
                        source.sendSuccess(() -> CivilizationMessages.translatable(
                                "civilizationmod.command.unassign.not_assigned",
                                villager.getName()), false);
                        return 0;
                }

                if (!data.clearResidentAssignment(villager.getUUID(), source.getServer().getTickCount())) {
                        source.sendSuccess(() -> CivilizationMessages.translatable(
                                "civilizationmod.command.assign.save_failed"), false);
                        return 0;
                }
                if (data.findBuildingAssignedTo(villager.getStringUUID()) == null) {
                        BuildingRoleEquipment.clearIfCivitasRole(villager);
                }
                source.sendSuccess(() -> CivilizationMessages.translatable(
                        "civilizationmod.command.unassign.success",
                        villager.getName(),
                        index), false);
                return 1;
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

		                private static CompletableFuture<Suggestions> suggestTownHallIndex(
                                CommandContext<CommandSourceStack> context,
                                SuggestionsBuilder builder
                ) {
                        CivilizationWorldData data = CivilizationWorldData.get(context.getSource().getServer());
                        for (int index = 1; index <= data.getTownHallCoreCount(); index++) {
                                builder.suggest(index);
                        }
                        return builder.buildFuture();
                }

                private static CompletableFuture<Suggestions> suggestTownHallRadius(
                                CommandContext<CommandSourceStack> context,
                                SuggestionsBuilder builder
                ) {
                        for (int radius : new int[]{32, 64, 96, 128, 256, 512}) {
                                builder.suggest(radius);
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

	

	private static Component foodEvent(String event) {
		return switch (event) {
			case FoodDemandModel.EVENT_SHORTAGE -> Component.translatable("civilizationmod.food.event.shortage");
			case FoodDemandModel.EVENT_SURPLUS -> Component.translatable("civilizationmod.food.event.surplus");
			default -> Component.translatable("civilizationmod.food.event.stable");
		};
	}

	

}
