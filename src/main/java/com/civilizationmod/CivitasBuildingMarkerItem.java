package com.civilizationmod;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;


import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.function.Consumer;

/** Common territory-selection item behavior shared by all Civitas building markers. */
public class CivitasBuildingMarkerItem extends Item {
    public CivitasBuildingMarkerItem(Properties properties) {
        super(properties);
    }

    /** Registers the left-click hook used to select the first territory point. */
    public static void registerSelectionCallback() {
        AttackBlockCallback.EVENT.register(CivitasBuildingMarkerItem::handleAttackBlock);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        BuildingMarkerRegistry.MarkerDefinition definition = BuildingMarkerRegistry.definition(stack);
        if (definition != null && definition.capacity() > 0) {
            tooltip.accept(Component.translatable(
                    "civilizationmod.item.residential_marker.capacity",
                    definition.capacity()));
        }
        appendPoint(tooltip, "civilizationmod.item.marker.point_a", WarehouseTerritory.selectedPointA(stack));
        Optional<BlockPos> pointB = WarehouseTerritory.selectedPointB(stack);
        if (pointB.isPresent()) {
            appendPoint(tooltip, "civilizationmod.item.marker.point_b", pointB);
            tooltip.accept(Component.translatable("civilizationmod.item.marker.territory.configured"));
        } else {
            tooltip.accept(Component.translatable("civilizationmod.item.marker.point_b.pending"));
            tooltip.accept(Component.translatable("civilizationmod.item.marker.territory.unconfigured"));
        }
    }

    private static void appendPoint(
            Consumer<Component> tooltip,
            String translationKey,
            Optional<BlockPos> point
    ) {
        if (point.isPresent()) {
            BlockPos position = point.get();
            tooltip.accept(Component.translatable(
                    translationKey,
                    position.getX(),
                    position.getY(),
                    position.getZ()));
        } else {
            tooltip.accept(Component.translatable(translationKey + ".unset"));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null
                || context.getHand() != InteractionHand.MAIN_HAND
                || player.isSpectator()
                || !BuildingMarkerRegistry.isKnownMarker(context.getItemInHand())) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getItemInHand();
        String dimension = serverLevel.dimension().identifier().toString();
        WarehouseTerritory.CompletionResult result = WarehouseTerritory.complete(
                stack,
                dimension,
                context.getClickedPos());
        switch (result) {
            case COMPLETE -> {
                WarehouseTerritory territory = WarehouseTerritory.read(stack).orElse(null);
                if (territory == null) {
                    return InteractionResult.FAIL;
                }
                player.sendSystemMessage(CivilizationMessages.translatable(
                        "civilizationmod.building.selection.complete",
                        territory.minX(),
                        territory.minY(),
                        territory.minZ(),
                        territory.maxX(),
                        territory.maxY(),
                        territory.maxZ()));
                return InteractionResult.SUCCESS;
            }
            case NO_FIRST_POINT -> player.sendSystemMessage(CivilizationMessages.translatable(
                    "civilizationmod.building.selection.no_first_point"));
            case DIFFERENT_DIMENSION -> player.sendSystemMessage(CivilizationMessages.translatable(
                    "civilizationmod.building.selection.different_dimension"));
            case TOO_LARGE -> player.sendSystemMessage(CivilizationMessages.translatable(
                    "civilizationmod.building.selection.too_large"));
        }
        return InteractionResult.FAIL;
    }

    

    private static InteractionResult handleAttackBlock(
            Player player,
            Level level,
            InteractionHand hand,
            BlockPos position,
            Direction direction
    ) {
        if (hand != InteractionHand.MAIN_HAND
                || player.isSpectator()
                || !BuildingMarkerRegistry.isKnownMarker(player.getItemInHand(hand))) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }

        ItemStack stack = player.getItemInHand(hand);
        WarehouseTerritory.setPointA(
                stack,
                serverLevel.dimension().identifier().toString(),
                position);
        player.sendSystemMessage(CivilizationMessages.translatable(
                "civilizationmod.building.selection.point_a",
                position.getX(),
                position.getY(),
                position.getZ()));
        return InteractionResult.FAIL;
    }
}

