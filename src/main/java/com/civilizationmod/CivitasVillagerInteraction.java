package com.civilizationmod;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

import java.util.OptionalInt;

/** Replaces vanilla villager trading only for valid assigned Civitas residents. */
public final class CivitasVillagerInteraction {
    private CivitasVillagerInteraction() {
    }

    public static void register() {
        UseEntityCallback.EVENT.register(CivitasVillagerInteraction::interact);
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
                || !(entity instanceof Villager villager)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return BuildingRoleEquipment.isCivitasRole(
                    villager.getItemBySlot(EquipmentSlot.CHEST))
                    ? InteractionResult.SUCCESS
                    : InteractionResult.PASS;
        }

        if (!(level instanceof ServerLevel serverLevel)
                || !(player instanceof ServerPlayer serverPlayer)
                || villager.level() != serverLevel
                || !villager.isAlive()) {
            return InteractionResult.PASS;
        }

        BuildingObservation building = CivilizationWorldData.get(serverLevel.getServer())
                .findBuildingAssignedTo(villager.getUUID().toString());
        if (building == null
                || !BuildingObservation.VALIDATION_VALID.equals(building.validationStatus())) {
            return InteractionResult.PASS;
        }

        SimpleMenuProvider provider = new SimpleMenuProvider(
                (syncId, playerInventory, ignoredPlayer) ->
                        new CivitasVillagerBackpackMenu(syncId, playerInventory, villager),
                CivilizationMessages.translatable("civilizationmod.villager.backpack.title"));
        OptionalInt opened = serverPlayer.openMenu(provider);
        return opened.isPresent() ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }
}

