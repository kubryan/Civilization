package com.civilizationmod;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Shows the currently held Civitas territory selection to its owner.
 *
 * <p>The preview is intentionally server-authored and sent only to the selecting
 * player. This mirrors Residence's particle visualizer without introducing a
 * client-to-server selection payload or a client-only renderer hook.</p>
 */
public final class CivitasTerritoryPreviewService {
    private static final int UPDATE_INTERVAL_TICKS = 5;
    private static final int MAX_EDGE_INTERVALS = 24;

    private CivitasTerritoryPreviewService() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(CivitasTerritoryPreviewService::onEndServerTick);
    }

    private static void onEndServerTick(MinecraftServer server) {
        if (server.getTickCount() % UPDATE_INTERVAL_TICKS != 0) {
            return;
        }

        for (ResourceKey<Level> key : server.levelKeys()) {
            ServerLevel level = server.getLevel(key);
            if (level == null) {
                continue;
            }
            for (ServerPlayer player : level.players()) {
                showPreview(player);
            }
        }
    }

    private static void showPreview(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (!BuildingMarkerRegistry.isKnownMarker(stack)) {
            return;
        }

        String currentDimension = player.level().dimension().identifier().toString();
        if (WarehouseTerritory.selectedDimension(stack)
                .filter(currentDimension::equals)
                .isEmpty()) {
            return;
        }

        Optional<BlockPos> pointA = WarehouseTerritory.selectedPointA(stack);
        if (pointA.isEmpty()) {
            return;
        }

        Optional<BlockPos> pointB = WarehouseTerritory.selectedPointB(stack);
        if (pointB.isEmpty()) {
            sendPoint(player, pointA.get(), ParticleTypes.HAPPY_VILLAGER);
            return;
        }

        WarehouseTerritory territory = WarehouseTerritory.read(stack).orElse(null);
        if (territory == null) {
            return;
        }

        drawCuboid(player, territory);
        sendPoint(player, pointA.get(), ParticleTypes.HAPPY_VILLAGER);
        sendPoint(player, pointB.get(), ParticleTypes.HAPPY_VILLAGER);
    }

    private static void drawCuboid(ServerPlayer player, WarehouseTerritory territory) {
        double minX = territory.minX();
        double minY = territory.minY();
        double minZ = territory.minZ();
        double maxX = territory.maxX() + 1.0D;
        double maxY = territory.maxY() + 1.0D;
        double maxZ = territory.maxZ() + 1.0D;

        drawEdge(player, minX, minY, minZ, maxX, minY, minZ);
        drawEdge(player, minX, minY, maxZ, maxX, minY, maxZ);
        drawEdge(player, minX, maxY, minZ, maxX, maxY, minZ);
        drawEdge(player, minX, maxY, maxZ, maxX, maxY, maxZ);

        drawEdge(player, minX, minY, minZ, minX, maxY, minZ);
        drawEdge(player, maxX, minY, minZ, maxX, maxY, minZ);
        drawEdge(player, minX, minY, maxZ, minX, maxY, maxZ);
        drawEdge(player, maxX, minY, maxZ, maxX, maxY, maxZ);

        drawEdge(player, minX, minY, minZ, minX, minY, maxZ);
        drawEdge(player, maxX, minY, minZ, maxX, minY, maxZ);
        drawEdge(player, minX, maxY, minZ, minX, maxY, maxZ);
        drawEdge(player, maxX, maxY, minZ, maxX, maxY, maxZ);
    }

    private static void drawEdge(
            ServerPlayer player,
            double startX,
            double startY,
            double startZ,
            double endX,
            double endY,
            double endZ
    ) {
        int intervals = Math.max(1, Math.min(
                MAX_EDGE_INTERVALS,
                (int) Math.ceil(Math.max(
                        Math.abs(endX - startX),
                        Math.max(Math.abs(endY - startY), Math.abs(endZ - startZ))))));

        for (int index = 0; index <= intervals; index++) {
            double progress = (double) index / intervals;
            sendParticle(
                    player,
                    ParticleTypes.END_ROD,
                    startX + (endX - startX) * progress,
                    startY + (endY - startY) * progress,
                    startZ + (endZ - startZ) * progress);
        }
    }

    private static void sendPoint(ServerPlayer player, BlockPos point, ParticleOptions particle) {
        sendParticle(
                player,
                particle,
                point.getX() + 0.5D,
                point.getY() + 0.5D,
                point.getZ() + 0.5D);
    }

    private static void sendParticle(
            ServerPlayer player,
            ParticleOptions particle,
            double x,
            double y,
            double z
    ) {
        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(player, particle, false, true, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }
}
