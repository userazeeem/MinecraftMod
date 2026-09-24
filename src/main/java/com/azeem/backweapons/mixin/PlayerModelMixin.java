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

        // Bell curve: 0 at both ends, peak in the middle of the transition.
        float reach = Mth.sin(visibility * (float) Math.PI);

        PlayerModel self = (PlayerModel) (Object) this;

        // Raise the arm up and back, as if reaching over the shoulder.
        self.rightArm.xRot -= reach * 2.1F;
        // Sweep it inward across the body toward the opposite shoulder blade.
        self.rightArm.yRot -= reach * 0.9F;
        // Slight roll for a more natural wrist/shoulder twist.
        self.rightArm.zRot -= reach * 0.35F;
    }
}