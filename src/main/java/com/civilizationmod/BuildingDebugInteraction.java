package com.civilizationmod;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/** Server-side diagnostic interaction for inspecting registered building markers. */
public final class BuildingDebugInteraction {
    private BuildingDebugInteraction() {
    }

    public static void register() {
        UseEntityCallback.EVENT.register(BuildingDebugInteraction::interact);
    }

    private static InteractionResult interact(
            Player player,
            Level level,
            InteractionHand hand,
            Entity entity,
            EntityHitResult hitResult
    ) {
        if (level.isClientSide()
                || hand != InteractionHand.MAIN_HAND
                || player.isSpectator()
                || !(entity instanceof ItemFrame frame)
                || !isDebugBook(player.getItemInHand(hand))) {
            return InteractionResult.PASS;
        }

        String functionId = BuildingMarkerRegistry.functionId(frame.getItem());
        if (BuildingFunction.fromId(functionId) == null) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        BuildingGeometryValidator.GeometryDiagnostic diagnostic =
                BuildingGeometryValidator.diagnose(serverLevel, frame, functionId);
        BuildingGeometryValidator.ValidationResult validation = diagnostic.result();
        BuildingGeometryValidator.SideDiagnostics selectedSide =
                "opposite".equals(diagnostic.selectedSide())
                        ? diagnostic.oppositeSide()
                        : diagnostic.facingSide();
        player.sendSystemMessage(CivilizationMessages.translatable(
                "civilizationmod.building.debug.result",
                Component.translatable("civilizationmod.building.function." + functionId),
                serverLevel.dimension().identifier().toString(),
                frame.blockPosition().getX(),
                frame.blockPosition().getY(),
                frame.blockPosition().getZ(),
                Component.translatable("civilizationmod.building.validation." + validation.status()),
                Component.translatable("civilizationmod.building.validation.reason." + validation.reason()),
                validation.interiorAirBlocks(),
                validation.floorSupportBlocks(),
                validation.ceilingBlocks(),
                diagnostic.legalMarkerCount(),
                selectedSide.wallBlocks(),
                selectedSide.containerBlocks(),
                diagnostic.markerAtDoor() ? "yes" : "no"));
        player.sendSystemMessage(CivilizationMessages.translatable(
                "civilizationmod.building.debug.geometry",
                formatPosition(diagnostic.doorPosition()),
                formatDirection(diagnostic.doorFacing()),
                diagnostic.selectedSide(),
                diagnostic.legalMarkerCount(),
                validation.interiorAirBlocks(),
                validation.floorSupportBlocks(),
                validation.ceilingBlocks(),
                selectedSide.minFloorY(),
                selectedSide.maxFloorY(),
                selectedSide.minRoomHeight(),
                selectedSide.maxRoomHeight(),
                selectedSide.wallBlocks(),
                selectedSide.wallSegments(),
                selectedSide.entryAccessible() ? "yes" : "no",
                selectedSide.containerBlocks()));
        sendSideDiagnostics(player, diagnostic.facingSide());
        sendSideDiagnostics(player, diagnostic.oppositeSide());
        return InteractionResult.SUCCESS;
    }

    private static void sendSideDiagnostics(Player player, BuildingGeometryValidator.SideDiagnostics side) {
        player.sendSystemMessage(CivilizationMessages.translatable(
                "civilizationmod.building.debug.side",
                side.sideLabel(),
                side.complete() ? "complete" : "incomplete",
                side.scanLimitReached() ? "limit" : "normal",
                side.totalSamples(),
                side.expectedSamples(),
                side.airBlocks(),
                side.floorSupportBlocks(),
                side.ceilingBlocks(),
                side.ceilingNonAirBlocks(),
                side.minFloorY(),
                side.maxFloorY(),
                side.minRoomHeight(),
                side.maxRoomHeight(),
                side.wallBlocks(),
                side.wallSegments(),
                side.wallsComplete() ? "complete" : "incomplete",
                side.entryAccessible() ? "yes" : "no",
                side.containerBlocks(),
                formatPosition(side.sampleStart()),
                formatPosition(side.sampleEnd())));
    }

    private static String formatPosition(net.minecraft.core.BlockPos position) {
        if (position == null) {
            return "none";
        }
        return position.getX() + "," + position.getY() + "," + position.getZ();
    }

    private static String formatDirection(net.minecraft.core.Direction direction) {
        return direction == null ? "none" : direction.toString();
    }

    private static boolean isDebugBook(ItemStack stack) {
        return stack.is(Items.BOOK);
    }
}
