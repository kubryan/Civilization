package com.civilizationmod;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/** Server-authoritative two-step binding workflow for every Civitas building marker. */
public final class ResidenceBindingDeviceInteraction {
    private ResidenceBindingDeviceInteraction() {
    }

    public static void register() {
        UseEntityCallback.EVENT.register(ResidenceBindingDeviceInteraction::interact);
    }

    private static InteractionResult interact(
            Player player,
            Level level,
            InteractionHand hand,
            Entity entity,
            EntityHitResult hitResult
    ) {
        if (hand != InteractionHand.MAIN_HAND
                || player.isSpectator()
                || !(player.getItemInHand(hand).getItem() instanceof CivitasBindingDeviceItem)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }

        ItemStack device = player.getItemInHand(hand);
        if (entity instanceof ItemFrame frame) {
            return selectBuilding(serverLevel, player, device, frame);
        }
        if (entity instanceof Villager villager) {
            return bindResident(serverLevel, player, device, villager);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult selectBuilding(
            ServerLevel level,
            Player player,
            ItemStack device,
            ItemFrame frame
    ) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        ItemStack markerStack = frame.getItem();
        String functionId = BuildingMarkerRegistry.functionId(markerStack);
        if (BuildingMarkerRegistry.FUNCTION_UNKNOWN.equals(functionId)) {
            player.sendSystemMessage(CivilizationMessages.translatable(
                    "civilizationmod.binding.select.not_marker"));
            return InteractionResult.FAIL;
        }

        CivilizationWorldData data = CivilizationWorldData.get(level.getServer());
        BuildingObservation building = data.findBuilding(
                level.dimension().identifier().toString(),
                frame.blockPosition().getX(),
                frame.blockPosition().getY(),
                frame.blockPosition().getZ());
        if (building == null
                || !BuildingObservation.VALIDATION_VALID.equals(building.validationStatus())
                || data.findBuildingMarker(level, building) != frame) {
            player.sendSystemMessage(CivilizationMessages.translatable(
                    "civilizationmod.binding.select.invalid"));
            return InteractionResult.FAIL;
        }

        ResidenceBindingDeviceData.write(
                device,
                level.dimension().identifier().toString(),
                frame.blockPosition());
        player.sendSystemMessage(CivilizationMessages.translatable(
                "civilizationmod.binding.select.success",
                functionName(functionId),
                frame.blockPosition().getX(),
                frame.blockPosition().getY(),
                frame.blockPosition().getZ()));
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult bindResident(
            ServerLevel level,
            Player player,
            ItemStack device,
            Villager villager
    ) {
        if (!villager.isAlive() || villager.level() != level) {
            player.sendSystemMessage(CivilizationMessages.translatable(
                    "civilizationmod.binding.bind.unavailable"));
            return InteractionResult.FAIL;
        }
        ResidenceBindingDeviceData selected = ResidenceBindingDeviceData.read(device).orElse(null);
        if (selected == null) {
            player.sendSystemMessage(CivilizationMessages.translatable(
                    "civilizationmod.binding.bind.no_selection"));
            return InteractionResult.FAIL;
        }
        if (!selected.dimension().equals(level.dimension().identifier().toString())) {
            player.sendSystemMessage(CivilizationMessages.translatable(
                    "civilizationmod.binding.bind.wrong_dimension"));
            return InteractionResult.FAIL;
        }

        CivilizationWorldData data = CivilizationWorldData.get(level.getServer());
        BuildingObservation building = data.findBuilding(
                selected.dimension(),
                selected.markerX(),
                selected.markerY(),
                selected.markerZ());
        if (building == null
                || !BuildingObservation.VALIDATION_VALID.equals(building.validationStatus())) {
            player.sendSystemMessage(CivilizationMessages.translatable(
                    "civilizationmod.binding.bind.invalid_building"));
            return InteractionResult.FAIL;
        }

        ItemFrame markerFrame = data.findBuildingMarker(level, building);
        if (markerFrame == null
                || !building.functionId().equals(BuildingMarkerRegistry.functionId(markerFrame.getItem()))) {
            player.sendSystemMessage(CivilizationMessages.translatable(
                    "civilizationmod.binding.bind.invalid_building"));
            return InteractionResult.FAIL;
        }

        if (BuildingFunction.RESIDENCE.id().equals(building.functionId())) {
            WarehouseTerritory territory = WarehouseTerritory.read(markerFrame.getItem()).orElse(null);
            ResidenceValidator.Validation liveValidation = ResidenceValidator.validate(
                    level,
                    markerFrame,
                    building.capacity());
            if (territory == null || !liveValidation.isValid()) {
                player.sendSystemMessage(CivilizationMessages.translatable(
                        "civilizationmod.binding.bind.invalid_building"));
                return InteractionResult.FAIL;
            }

            if (liveValidation.bedCount() != building.bedCount()) {
                BuildingObservation measured = building.withResidenceMeasurements(
                        building.capacity(),
                        liveValidation.bedCount());
                if (!data.replaceBuilding(building, measured)) {
                    player.sendSystemMessage(CivilizationMessages.translatable(
                            "civilizationmod.command.assign.save_failed"));
                    return InteractionResult.FAIL;
                }
                building = measured;
            }
        }

        BuildingObservation existingAssignment = data.findBuildingAssignedTo(villager.getStringUUID());
        boolean alreadyAssignedToTarget = existingAssignment != null
                && existingAssignment.isSameMarker(
                building.dimension(),
                building.markerX(),
                building.markerY(),
                building.markerZ());
        if (existingAssignment != null && !alreadyAssignedToTarget) {
            player.sendSystemMessage(CivilizationMessages.translatable(
                    "civilizationmod.command.assign.already_assigned",
                    existingAssignment.markerX(),
                    existingAssignment.markerY(),
                    existingAssignment.markerZ()));
            return InteractionResult.FAIL;
        }

        if (BuildingFunction.RESIDENCE.id().equals(building.functionId())
                && !alreadyAssignedToTarget
                && building.residentCount() >= building.capacity()) {
            player.sendSystemMessage(CivilizationMessages.translatable(
                    "civilizationmod.command.assign.residence_full",
                    0,
                    building.residentCount(),
                    building.capacity()));
            return InteractionResult.FAIL;
        }

        BuildingObservation replacement;
        if (BuildingFunction.RESIDENCE.id().equals(building.functionId())) {
            replacement = alreadyAssignedToTarget
                    ? building
                    : building.withAddedResident(villager.getUUID(), villager.getName().getString());
        } else {
            replacement = building.withResident(villager.getUUID(), villager.getName().getString());
        }

        if (!data.replaceBuilding(building, replacement)) {
            player.sendSystemMessage(CivilizationMessages.translatable(
                    "civilizationmod.command.assign.save_failed"));
            return InteractionResult.FAIL;
        }
        BuildingResidentService.applyAssignmentVisual(villager, replacement.functionId());
        player.sendSystemMessage(CivilizationMessages.translatable(
                "civilizationmod.binding.bind.success",
                functionName(replacement.functionId()),
                villager.getName(),
                replacement.residentCount()));
        return InteractionResult.SUCCESS;
    }

    private static Component functionName(String functionId) {
        return Component.translatable("civilizationmod.building.function." + functionId);
    }
}
