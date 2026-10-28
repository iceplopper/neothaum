package dev.neothaum.client.mixin;

import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds the warp effects' screen shaders (WarpVisuals) to the post effects the game renderer applies this frame. */
@Mixin(GameRenderer.class)
public abstract class GameRendererPostEffectsMixin {
	@Shadow
	@Final
	private List<Identifier> requestedPostEffects;

	@Inject(method = "update", at = @At("TAIL"))
	private void neothaum$warpShaders(DeltaTracker deltaTracker, CallbackInfo ci) {
		dev.neothaum.client.hud.WarpVisuals.addPostEffects(this.requestedPostEffects);
	}
}
