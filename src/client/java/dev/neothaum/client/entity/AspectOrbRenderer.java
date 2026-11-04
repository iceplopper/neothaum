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
		state.ticks = entity.getAge();
	}

	/**
	 * RenderAspectOrb: a frame of the animated orb from the particle sheet, facing the camera, in the aspect's colour at half strength and
	 * blended as the aspect blends; it shrinks from 0.4 to 0.1 over the orb's 150-tick life.
	 */
	@Override
	public void submit(State state, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		ps.pushPose();
		ps.rotate(camera.orientation);
		float life = Math.max(0.0F, (AspectOrbEntity.MAX_AGE - state.ticks) / (float) AspectOrbEntity.MAX_AGE);
		float scale = 0.1F + 0.3F * life;
		ps.scale(scale, scale, scale);
		int frame = (int) (System.nanoTime() / 25000000L % 16L);
		float u0 = frame / 16.0F;
		float u1 = (frame + 1) / 16.0F;
		float v0 = 0.5F;
		float v1 = 0.5625F;
		int col = state.aspect != null ? state.aspect.getColor() : 0xFFFFFF;
		float r = (col >> 16 & 255) / 255.0F;
		float g = (col >> 8 & 255) / 255.0F;
		float b = (col & 255) / 255.0F;
		float a = 128 / 255.0F;
		boolean additive = state.aspect == null || state.aspect.getBlend() == 1;
		collector.submitCustomGeometry(ps, TVRenderTypes.glow(TVRenderTypes.PARTICLES, additive, true), (pose, vc) -> {
			vc.addVertex(pose, -0.5F, -0.25F, 0.0F).setColor(r, g, b, a).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
			vc.addVertex(pose, 0.5F, -0.25F, 0.0F).setColor(r, g, b, a).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
			vc.addVertex(pose, 0.5F, 0.75F, 0.0F).setColor(r, g, b, a).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
			vc.addVertex(pose, -0.5F, 0.75F, 0.0F).setColor(r, g, b, a).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
		});
		ps.popPose();
	}
}
