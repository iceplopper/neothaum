package dev.neothaum.client.mixin;

import dev.neothaum.client.hud.FogFiddle;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** RenderEventHandler.fogDensityEvent: while the fog is fiddled, draw it in close. */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
	@Inject(method = "setupFog", at = @At("RETURN"))
	private void neothaum$fiddleFog(Camera camera, int renderDistance, DeltaTracker delta, float darken, ClientLevel level,
		CallbackInfoReturnable<FogData> cir) {
		float end = FogFiddle.visibleDistance();
		if (end == Float.MAX_VALUE) {
			return;
		}
		FogData fog = cir.getReturnValue();
		fog.environmentalStart = Math.min(fog.environmentalStart, 0.0F);
		fog.environmentalEnd = Math.min(fog.environmentalEnd, end);
		fog.renderDistanceStart = Math.min(fog.renderDistanceStart, 0.0F);
		fog.renderDistanceEnd = Math.min(fog.renderDistanceEnd, end);
		fog.skyEnd = Math.min(fog.skyEnd, end);
		fog.cloudEnd = Math.min(fog.cloudEnd, end);
	}
}
