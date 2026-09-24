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
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class BackWeaponFeatureRenderer extends RenderLayer<AvatarRenderState, EntityModel<AvatarRenderState>> {

    private static final float TRANSITION_DURATION_TICKS = 6.0F;
    private static final float TARGET_TRANSLATE_Y = 0.3F;
    private static final float TARGET_TRANSLATE_Z = 0.28F;
    private static final float TARGET_ROTATION_DEG = 65.0F;
    private static final float TARGET_SCALE = 0.7F;

    private final ItemModelResolver itemModelResolver;
    private final ItemStackRenderState backItemState = new ItemStackRenderState();
    private ItemStack rememberedWeapon = ItemStack.EMPTY;

    private boolean wasHolding = false;
    private float transitionStartTick = 0.0F;

    public BackWeaponFeatureRenderer(RenderLayerParent<AvatarRenderState, EntityModel<AvatarRenderState>> parent,
                                      ItemModelResolver itemModelResolver) {
        super(parent);
        this.itemModelResolver = itemModelResolver;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
        boolean holdingNow = !state.rightHandItemStack.isEmpty();

        if (holdingNow) {
            this.rememberedWeapon = state.rightHandItemStack;
        }

        if (holdingNow != this.wasHolding) {
            this.transitionStartTick = state.ageInTicks;
            this.wasHolding = holdingNow;
        }

        float progress = Mth.clamp((state.ageInTicks - this.transitionStartTick) / TRANSITION_DURATION_TICKS, 0.0F, 1.0F);
        float visibility = holdingNow ? (1.0F - progress) : progress;
        if (this.rememberedWeapon.isEmpty()) {
            visibility = 0.0F;
        }

        BackWeaponAnimationTracker.State tracked = BackWeaponAnimationTracker.get(state.id);
        tracked.holdingNow = holdingNow;
        tracked.visibility = visibility;

        if (visibility <= 0.0F) {
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

        float translateY = Mth.lerp(visibility, 0.0F, TARGET_TRANSLATE_Y);
        float translateZ = Mth.lerp(visibility, 0.0F, TARGET_TRANSLATE_Z);
        float rotationDeg = Mth.lerp(visibility, 0.0F, TARGET_ROTATION_DEG);
        float scale = Mth.lerp(visibility, 0.0F, TARGET_SCALE);

        poseStack.pushPose();

        poseStack.translate(0.0F, translateY, translateZ);
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotationDeg));
        poseStack.scale(scale, scale, scale);

        this.backItemState.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);

        poseStack.popPose();
    }
}