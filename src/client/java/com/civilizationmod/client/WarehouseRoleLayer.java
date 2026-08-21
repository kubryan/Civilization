package com.civilizationmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Client-only overlay because vanilla VillagerRenderer has no HumanoidArmorLayer. */
public final class WarehouseRoleLayer extends RenderLayer<VillagerRenderState, VillagerModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            "civilizationmod",
            "textures/entity/villager/warehouse_role.png");

    public WarehouseRoleLayer(RenderLayerParent<VillagerRenderState, VillagerModel> parent) {
        super(parent);
    }

    @Override
    public void submit(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight,
            VillagerRenderState state,
            float yRot,
            float xRot
    ) {
        if (state.isInvisible
                || !((WarehouseVillagerRenderStateAccess) state).civilizationmod$hasWarehouseRole()) {
            return;
        }

        VillagerModel model = this.getParentModel();
        var renderType = RenderTypes.entityCutout(TEXTURE);
        int overlayCoords = LivingEntityRenderer.getOverlayCoords(state, 0.0F);

        // VillagerModel.root() contains head, body, arms and legs. Submit only
        // body (including its jacket child) and arms so the role texture cannot
        // bleed onto the villager's head, hat or nose.
        collector.order(1).submitModelPart(
                model.root().getChild("body"),
                poseStack,
                renderType,
                packedLight,
                overlayCoords,
                null);
        collector.order(1).submitModelPart(
                model.root().getChild("arms"),
                poseStack,
                renderType,
                packedLight,
                overlayCoords,
                null);
    }
}
