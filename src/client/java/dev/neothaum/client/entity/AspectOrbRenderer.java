package dev.neothaum.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.client.render.TVRenderTypes;
import dev.neothaum.entity.AspectOrbEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * AspectOrbRenderer: renders a shimmering billboarded aspect orb with aspect color.
 */
public class AspectOrbRenderer extends EntityRenderer<AspectOrbEntity, AspectOrbRenderer.State> {
	public static class State extends EntityRenderState {
		public Aspect aspect = Aspect.AIR;
		public int ticks = 0;
	}

	public AspectOrbRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.1F;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(AspectOrbEntity entity, State state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.aspect = entity.getAspect();
		state.ticks = entity.tickCount;
	}

	@Override
	public void submit(State state, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		ps.pushPose();
		ps.translate(0.0F, 0.1F, 0.0F);

		// Billboard toward camera
		ps.rotate(camera.orientation);
		ps.rotateDegrees(Axis.ZP, (state.ticks % 360) * 4.0F);

		float scale = 0.25F + 0.05F * (float) Math.sin(state.ticks * 0.2F);
		ps.scale(scale, scale, scale);

		int col = state.aspect != null ? state.aspect.getColor() : 0xFFFFFF;
		float r = (col >> 16 & 255) / 255.0F;
		float g = (col >> 8 & 255) / 255.0F;
		float b = (col & 255) / 255.0F;

		collector.submitCustomGeometry(ps, TVRenderTypes.glow(TVRenderTypes.WHITE, true, true), (pose, vc) -> {
			float h = 0.5F;
			// Quad 1
			vc.addVertex(pose, -h, -h, 0.0F).setColor(r, g, b, 0.85F).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
			vc.addVertex(pose, h, -h, 0.0F).setColor(r, g, b, 0.85F).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
			vc.addVertex(pose, h, h, 0.0F).setColor(r, g, b, 0.85F).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
			vc.addVertex(pose, -h, h, 0.0F).setColor(r, g, b, 0.85F).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);

			// Inner brighter white core
			float ch = 0.25F;
			vc.addVertex(pose, -ch, -ch, 0.01F).setColor(1.0F, 1.0F, 1.0F, 0.95F).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
			vc.addVertex(pose, ch, -ch, 0.01F).setColor(1.0F, 1.0F, 1.0F, 0.95F).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
			vc.addVertex(pose, ch, ch, 0.01F).setColor(1.0F, 1.0F, 1.0F, 0.95F).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
			vc.addVertex(pose, -ch, ch, 0.01F).setColor(1.0F, 1.0F, 1.0F, 0.95F).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
		});

		ps.popPose();
	}
}
