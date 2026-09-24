package com.azeem.backweapons.mixin;

import com.azeem.backweapons.BackWeaponAnimationTracker;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void backweapons$applyDrawAnimation(AvatarRenderState state, CallbackInfo ci) {
        BackWeaponAnimationTracker.State tracked = BackWeaponAnimationTracker.get(state.id);
        float visibility = tracked.visibility;

        if (visibility <= 0.0F || visibility >= 1.0F) {
            return;
        }

        float extra = Mth.sin(visibility * (float) Math.PI) * 1.4F;

        PlayerModel self = (PlayerModel) (Object) this;
        self.rightArm.xRot -= extra;
        self.rightArm.yRot -= extra * 0.3F;
    }
}