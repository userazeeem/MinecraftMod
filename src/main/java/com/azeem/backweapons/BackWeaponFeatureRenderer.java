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
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BackWeaponFeatureRenderer extends RenderLayer<AvatarRenderState, EntityModel<AvatarRenderState>> {

    private static final float TRANSITION_DURATION_TICKS = 6.0F;
    private static final int DEBOUNCE_TICKS = 3;
    private static final float TARGET_TRANSLATE_Y = 0.3F;
    private static final float TARGET_TRANSLATE_Z = 0.28F;
    private static final float TARGET_ROTATION_DEG = -65.0F;
    private static final float TARGET_SCALE = 0.7F;

    private static final float AXE_TRANSLATE_Y = 0.6F;
    private static final float AXE_TRANSLATE_Z = 0.38F;
    private static final float AXE_ROTATION_DEG = 45.0F;
    private static final float AXE_SCALE = 0.7F;

    private final ItemModelResolver itemModelResolver;
    private final ItemStackRenderState backItemState = new ItemStackRenderState();
    private ItemStack rememberedWeapon = ItemStack.EMPTY;

    private boolean wasHolding = false;
    private float transitionStartTick = 0.0F;

    private Boolean pendingHolding = null;
    private int pendingTicks = 0;

    private final ItemStackRenderState axeRenderState = new ItemStackRenderState();
    private ItemStack rememberedAxe = ItemStack.EMPTY;

    private boolean wasHoldingAxe = false;
    private float axeTransitionStartTick = 0.0F;

    private Boolean axePendingHolding = null;
    private int axePendingTicks = 0;

    public BackWeaponFeatureRenderer(RenderLayerParent<AvatarRenderState, EntityModel<AvatarRenderState>> parent,
                                     ItemModelResolver itemModelResolver) {
        super(parent);
        this.itemModelResolver = itemModelResolver;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
        boolean holdingNow = state.rightHandItemStack.is(ItemTags.SWORDS);

        if (holdingNow) {
            this.rememberedWeapon = state.rightHandItemStack;
        } else if (!this.rememberedWeapon.isEmpty() && !this.playerStillHas(state.id, this.rememberedWeapon)) {
            this.rememberedWeapon = ItemStack.EMPTY;
        }

        if (holdingNow == this.wasHolding) {
            this.pendingHolding = null;
            this.pendingTicks = 0;
        } else {
            if (this.pendingHolding != null && this.pendingHolding == holdingNow) {
                this.pendingTicks++;
            } else {
                this.pendingHolding = holdingNow;
                this.pendingTicks = 1;
            }

            if (this.pendingTicks >= DEBOUNCE_TICKS) {
                this.wasHolding = holdingNow;
                this.transitionStartTick = state.ageInTicks;
                this.pendingHolding = null;
                this.pendingTicks = 0;
            }
        }

        float progress = Mth.clamp((state.ageInTicks - this.transitionStartTick) / TRANSITION_DURATION_TICKS, 0.0F, 1.0F);
        float visibility = this.wasHolding ? (1.0F - progress) : progress;
        if (this.rememberedWeapon.isEmpty()) {
            visibility = 0.0F;
        }
        visibility = Mth.clamp(visibility, 0.0F, 1.0F);

        // --- Axe tracking (separate from the sword/main weapon above) ---
        boolean holdingAxeNow = state.rightHandItemStack.is(ItemTags.AXES);

        if (holdingAxeNow) {
            this.rememberedAxe = state.rightHandItemStack;
        } else if (!this.rememberedAxe.isEmpty() && !this.playerStillHas(state.id, this.rememberedAxe)) {
            this.rememberedAxe = ItemStack.EMPTY;
        }

        if (holdingAxeNow == this.wasHoldingAxe) {
            this.axePendingHolding = null;
            this.axePendingTicks = 0;
        } else {
            if (this.axePendingHolding != null && this.axePendingHolding == holdingAxeNow) {
                this.axePendingTicks++;
            } else {
                this.axePendingHolding = holdingAxeNow;
                this.axePendingTicks = 1;
            }

            if (this.axePendingTicks >= DEBOUNCE_TICKS) {
                this.wasHoldingAxe = holdingAxeNow;
                this.axeTransitionStartTick = state.ageInTicks;
                this.axePendingHolding = null;
                this.axePendingTicks = 0;
            }
        }

        float axeProgress = Mth.clamp((state.ageInTicks - this.axeTransitionStartTick) / TRANSITION_DURATION_TICKS, 0.0F, 1.0F);
        float axeVisibility = this.wasHoldingAxe ? (1.0F - axeProgress) : axeProgress;
        if (this.rememberedAxe.isEmpty()) {
            axeVisibility = 0.0F;
        }
        axeVisibility = Mth.clamp(axeVisibility, 0.0F, 1.0F);

        BackWeaponAnimationTracker.State tracked = BackWeaponAnimationTracker.get(state.id);
        tracked.holdingNow = this.wasHolding || this.wasHoldingAxe;
        tracked.visibility = Math.max(visibility, axeVisibility);

        boolean shouldShowOnBack = !this.wasHolding && !this.rememberedWeapon.isEmpty();
        if (shouldShowOnBack) {
            this.itemModelResolver.updateForTopItem(
                    this.backItemState,
                    this.rememberedWeapon,
                    ItemDisplayContext.FIXED,
                    null,
                    null,
                    0
            );

            if (!this.backItemState.isEmpty()) {
                poseStack.pushPose();
                poseStack.translate(0.0F, TARGET_TRANSLATE_Y, TARGET_TRANSLATE_Z);
                poseStack.mulPose(Axis.ZP.rotationDegrees(TARGET_ROTATION_DEG));
                poseStack.scale(TARGET_SCALE, TARGET_SCALE, TARGET_SCALE);
                this.backItemState.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
                poseStack.popPose();
            }
        }

        boolean shouldShowAxeOnBack = !this.wasHoldingAxe && !this.rememberedAxe.isEmpty();
        if (shouldShowAxeOnBack) {
            this.itemModelResolver.updateForTopItem(
                    this.axeRenderState,
                    this.rememberedAxe,
                    ItemDisplayContext.FIXED,
                    null,
                    null,
                    0
            );

            if (!this.axeRenderState.isEmpty()) {
                poseStack.pushPose();
                poseStack.translate(0.0F, AXE_TRANSLATE_Y, AXE_TRANSLATE_Z);
                poseStack.mulPose(Axis.ZP.rotationDegrees(AXE_ROTATION_DEG));
                poseStack.scale(AXE_SCALE, AXE_SCALE, AXE_SCALE);
                this.axeRenderState.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
                poseStack.popPose();
            }
        }
    }

    private boolean playerStillHas(int entityId, ItemStack stack) {
        if (Minecraft.getInstance().level == null) {
            return true;
        }
        Entity entity = Minecraft.getInstance().level.getEntity(entityId);
        if (!(entity instanceof Player player)) {
            return true;
        }
        return player.getInventory().contains(stack);
    }
}