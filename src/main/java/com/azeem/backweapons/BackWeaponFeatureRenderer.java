package com.azeem.backweapons;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class BackWeaponFeatureRenderer extends RenderLayer<AvatarRenderState, EntityModel<AvatarRenderState>> {

    private final ItemModelResolver itemModelResolver;
    private final ItemStackRenderState backItemState = new ItemStackRenderState();
    private ItemStack rememberedWeapon = ItemStack.EMPTY;

    public BackWeaponFeatureRenderer(RenderLayerParent<AvatarRenderState, EntityModel<AvatarRenderState>> parent,
                                      ItemModelResolver itemModelResolver) {
        super(parent);
        this.itemModelResolver = itemModelResolver;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
        if (!state.rightHandItemStack.isEmpty()) {
            this.rememberedWeapon = state.rightHandItemStack;
            return;
        }

        if (this.rememberedWeapon.isEmpty()) {
            return;
        }

        this.itemModelResolver.updateForTopItem(
                this.backItemState,
                this.rememberedWeapon,
                ItemDisplayContext.FIXED,
                null,
                null,
                0
        );

        if (this.backItemState.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        poseStack.translate(0.0F, 0.3F, 0.15F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(65.0F));
        poseStack.scale(0.7F, 0.7F, 0.7F);

        this.backItemState.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);

        poseStack.popPose();
    }
}