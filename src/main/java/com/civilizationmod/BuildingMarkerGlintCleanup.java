package com.civilizationmod;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/** Removes Civitas marker glint before an ItemFrame is broken and drops its item. */
public final class BuildingMarkerGlintCleanup {
    private BuildingMarkerGlintCleanup() {
    }

    public static void register() {
        AttackEntityCallback.EVENT.register(BuildingMarkerGlintCleanup::interact);
    }

    private static InteractionResult interact(
            Player player,
            Level level,
            InteractionHand hand,
            Entity entity,
            EntityHitResult hitResult
    ) {
        if (player == null
                || player.isSpectator()
                || !(entity instanceof ItemFrame frame)
                || level.isClientSide()
                || !(level instanceof ServerLevel)) {
            return InteractionResult.PASS;
        }

        ItemStack current = frame.getItem();
        if (!BuildingMarkerRegistry.isKnownMarker(current)) {
            return InteractionResult.PASS;
        }

        ItemStack updated = current.copy();
        if (updated.remove(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE) == null) {
            return InteractionResult.PASS;
        }

        frame.setItem(updated, false);
        return InteractionResult.PASS;
    }
}
