package com.azeem.backweapons;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.EntityTypes;

public class BackWeaponsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BackWeapons.LOGGER.info("BackWeapons client initialized!");

        LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, renderer, registrationHelper, context) -> {
            if (entityType == EntityTypes.PLAYER) {
                @SuppressWarnings("unchecked")
                RenderLayerParent<AvatarRenderState, EntityModel<AvatarRenderState>> avatarRenderer =
                        (RenderLayerParent<AvatarRenderState, EntityModel<AvatarRenderState>>) (RenderLayerParent<?, ?>) renderer;

                registrationHelper.register(new BackWeaponFeatureRenderer(avatarRenderer, context.getItemModelResolver()));
            }
        });
    }
}