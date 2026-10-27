package dev.neothaum.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.neothaum.NeoThaum;
import dev.neothaum.client.render.TVRenderTypes;
import dev.neothaum.entity.monster.boss.CultistPortalEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * CultistPortalRenderer: 1:1 Port of RenderCultistPortal.
 * Renders pulsating crimson rift with animated swirling vortex frames and glowing energy.
 */
public class CultistPortalRenderer extends EntityRenderer<CultistPortalEntity, CultistPortalRenderer.State> {
	private static final Identifier PORTAL_TEX = NeoThaum.id("textures/misc/cultist_portal.png");

	public static class State extends EntityRenderState {
		public int ticks = 0;
		public int pulse = 0;
		public float healthFraction = 1.0F;
	}

	public CultistPortalRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.5F;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(CultistPortalEntity entity, State state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.ticks = entity.tickCount;
		state.pulse = entity.getPulse();
		state.healthFraction = entity.getHealth() / Math.max(1.0F, entity.getMaxHealth());
	}

	@Override
	public void submit(State state, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		ps.pushPose();
		ps.translate(0.0F, 1.5F, 0.0F);

		// Billboard facing camera
		ps.rotate(camera.orientation);

		float pulseBonus = 0.0F;
		if (state.pulse > 0) {
			pulseBonus = (float) Math.sin(state.pulse * 36.0 * Math.PI / 180.0) * 0.4F;
		}

		float scaleX = (1.8F + pulseBonus) * (0.8F + 0.2F * (float) Math.sin(state.ticks * 0.08F));
		float scaleY = (2.4F + pulseBonus) * (0.8F + 0.2F * (float) Math.cos(state.ticks * 0.08F));
		ps.scale(scaleX, scaleY, 1.0F);

		int frame = 15 - (state.ticks / 2) % 16;
		float u0 = frame / 16.0F;
		float u1 = u0 + (1.0F / 16.0F);
		float v0 = 0.0F;
		float v1 = 1.0F;

		collector.submitCustomGeometry(ps, TVRenderTypes.glow(PORTAL_TEX, false, true), (pose, vc) -> {
			float hx = 0.8F;
			float hy = 0.8F;
			vc.addVertex(pose, -hx, -hy, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u1, v1).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0F, 0.0F, 1.0F);
			vc.addVertex(pose,  hx, -hy, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u0, v1).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0F, 0.0F, 1.0F);
			vc.addVertex(pose,  hx,  hy, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u0, v0).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0F, 0.0F, 1.0F);
			vc.addVertex(pose, -hx,  hy, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u1, v0).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0F, 0.0F, 1.0F);
		});

		ps.popPose();
	}
}
