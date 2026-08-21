package com.civilizationmod;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;

/** Server-authoritative three-row Civitas villager backpack menu. */
public final class CivitasVillagerBackpackMenu extends ChestMenu {
    public static final int BACKPACK_ROWS = 3;
    public static final int BACKPACK_SLOT_COUNT = BACKPACK_ROWS * 9;

    private final Container backpack;
    private final Villager villager;

    /** Client-side constructor used by MenuType when the server opening packet arrives. */
    public CivitasVillagerBackpackMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, null);
    }

    /** Server-side constructor bound to the actual assigned villager. */
    public CivitasVillagerBackpackMenu(int syncId, Inventory playerInventory, Villager villager) {
        super(
                CivitasMenuTypes.VILLAGER_BACKPACK,
                syncId,
                playerInventory,
                createContainer(villager),
                BACKPACK_ROWS);
        this.villager = villager;
        this.backpack = getContainer();
    }

    public Villager villager() {
        return this.villager;
    }

    public Container backpack() {
        return this.backpack;
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.villager == null) {
            return true;
        }
        if (!(player.level() instanceof ServerLevel serverLevel)
                || !this.villager.isAlive()
                || this.villager.level() != player.level()
                || !player.isWithinEntityInteractionRange(this.villager, 4.0D)) {
            return false;
        }

        BuildingObservation building = CivilizationWorldData.get(serverLevel.getServer())
                .findBuildingAssignedTo(this.villager.getUUID().toString());
        return building != null
                && BuildingObservation.VALIDATION_VALID.equals(building.validationStatus());
    }

    @Override
    public void removed(Player player) {
        this.backpack.setChanged();
        super.removed(player);
    }

    private static Container createContainer(Villager villager) {
        return villager == null
                ? new SimpleContainer(BACKPACK_SLOT_COUNT)
                : new CivitasVillagerBackpackContainer(villager);
    }
}
