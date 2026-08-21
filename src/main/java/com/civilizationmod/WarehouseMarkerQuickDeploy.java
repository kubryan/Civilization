package com.civilizationmod;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;

import java.util.Optional;

/** Server-validated crouch-right-click transfer into an empty ItemFrame. */
public final class WarehouseMarkerQuickDeploy {
    private WarehouseMarkerQuickDeploy() {
    }

    public static void register() {
        UseEntityCallback.EVENT.register(WarehouseMarkerQuickDeploy::interact);
    }

    private static InteractionResult interact(
            Player player,
            Level level,
            InteractionHand hand,
            net.minecraft.world.entity.Entity entity,
            EntityHitResult hitResult
    ) {
        if (hand != InteractionHand.MAIN_HAND
                || !player.isShiftKeyDown()
                || player.isSpectator()
                || !(entity instanceof ItemFrame frame)
                || frame.getItem() == null
                || !frame.getItem().isEmpty()
                || !BuildingMarkerRegistry.isTerritoryMarker(player.getItemInHand(hand))) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }
        return deployToFrame(serverLevel, player, player.getItemInHand(hand), frame);
    }

    private static InteractionResult deployToFrame(
            ServerLevel serverLevel,
            Player player,
            ItemStack handStack,
            ItemFrame frame
    ) {
        ItemStack territorySource = handStack;
        Optional<WarehouseTerritory> territory = WarehouseTerritory.read(handStack);
        if (territory.isEmpty()) {
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                ItemStack candidate = player.getInventory().getItem(slot);
                if (candidate.getItem() == handStack.getItem()) {
                    Optional<WarehouseTerritory> candidateTerritory = WarehouseTerritory.read(candidate);
                    if (candidateTerritory.isPresent()) {
                        territorySource = candidate;
                        territory = candidateTerritory;
                        break;
                    }
                }
            }
        }
        if (territory.isEmpty()) {
            player.sendSystemMessage(CivilizationMessages.translatable(
                    "civilizationmod.building.deploy.incomplete"));
            return InteractionResult.FAIL;
        }

        WarehouseTerritory value = territory.get();
        if (!value.dimension().equals(serverLevel.dimension().identifier().toString())) {
            player.sendSystemMessage(CivilizationMessages.translatable(
                    "civilizationmod.building.deploy.wrong_dimension"));
            return InteractionResult.FAIL;
        }
        if (!value.contains(frame.blockPosition())) {
            player.sendSystemMessage(CivilizationMessages.translatable(
                    "civilizationmod.building.deploy.outside_territory"));
            return InteractionResult.FAIL;
        }
        if (!BuildingGeometryValidator.isAttachedToWall(serverLevel, frame)) {
            player.sendSystemMessage(CivilizationMessages.translatable(
                    "civilizationmod.building.deploy.not_attached"));
            return InteractionResult.FAIL;
        }
        if (hasOtherMarkerInTerritory(serverLevel, frame, value)) {
            player.sendSystemMessage(CivilizationMessages.translatable(
                    "civilizationmod.building.deploy.duplicate"));
            return InteractionResult.FAIL;
        }

        ItemStack transferred = handStack.copyWithCount(1);
        WarehouseTerritory.copyCustomData(territorySource, transferred);
        frame.setItem(transferred, false);
        territorySource.shrink(1);

        BuildingMarkerVisualState.apply(frame, true);
        player.sendSystemMessage(CivilizationMessages.translatable(
                "civilizationmod.building.deploy.success"));
        return InteractionResult.SUCCESS;
    }

    private static boolean hasOtherMarkerInTerritory(
            ServerLevel level,
            ItemFrame target,
            WarehouseTerritory territory
    ) {
        for (ItemFrame frame : level.getEntities(
                EntityTypeTest.forClass(ItemFrame.class),
                territoryBounds(territory),
                candidate -> candidate != target
                        && WarehouseTerritory.read(candidate.getItem())
                        .map(territory::equals)
                        .orElse(false))) {
            return true;
        }
        return false;
    }

    private static AABB territoryBounds(WarehouseTerritory territory) {
        return new AABB(
                territory.minX(),
                territory.minY(),
                territory.minZ(),
                territory.maxX() + 1.0D,
                territory.maxY() + 1.0D,
                territory.maxZ() + 1.0D);
    }
}
