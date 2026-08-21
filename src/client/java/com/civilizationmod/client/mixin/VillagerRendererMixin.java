package com.civilizationmod.client.mixin;

import com.civilizationmod.BuildingRoleEquipment;
import com.civilizationmod.client.WarehouseRoleLayer;
import com.civilizationmod.client.WarehouseVillagerRenderStateAccess;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerRenderer.class)
public abstract class VillagerRendererMixin {
    @Inject(
            method = "<init>(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)V",
            at = @At("RETURN"))
    private void civilizationmod$addWarehouseRoleLayer(
            EntityRendererProvider.Context context,
            CallbackInfo callbackInfo
    ) {
        @SuppressWarnings("unchecked")
        RenderLayerParent<VillagerRenderState, VillagerModel> parent =
                (RenderLayerParent<VillagerRenderState, VillagerModel>) (Object) this;
        ((LivingEntityRendererAccessor) (Object) this).civilizationmod$addLayer(new WarehouseRoleLayer(parent));
    }

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/npc/villager/Villager;Lnet/minecraft/client/renderer/entity/state/VillagerRenderState;F)V",
            at = @At("RETURN"))
    private void civilizationmod$extractWarehouseRole(
            Villager villager,
            VillagerRenderState state,
            float tickProgress,
            CallbackInfo callbackInfo
    ) {
        ((WarehouseVillagerRenderStateAccess) state).civilizationmod$setWarehouseRole(
                BuildingRoleEquipment.isWarehouseRole(villager.getItemBySlot(EquipmentSlot.CHEST)));
    }
}
