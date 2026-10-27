package dev.thaumvibe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.tiles.eldritch.EldritchPortalBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/**
 * 1:1 Port of TileEldritchPortalRenderer.
 * Swirling void tear with opening transition and multi-frame animated vortex.
 */
public class EldritchPortalRenderer implements BlockEntityRenderer<EldritchPortalBlockEntity, EldritchPortalRenderer.State> {
	private static final Identifier PORTAL_TEX = Thaumvibe.id("textures/misc/eldritch_portal.png");

	public static class State extends BlockEntityRenderState {
		int ticks;
		int openCount;
	}

	public EldritchPortalRenderer(BlockEntityRendererProvider.Context ctx) {}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(EldritchPortalBlockEntity portal, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(portal, s, pt, cam, crumbling);
		var viewer = Minecraft.getInstance().getCameraEntity();
		s.ticks = viewer == null ? 0 : viewer.tickCount;
		s.openCount = portal.getOpenCount();
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		ps.pushPose();
		ps.translate(0.5F, 0.5F, 0.5F);

		// Billboard facing camera
		ps.rotate(camera.orientation);

		float c = Math.min(30.0F, s.openCount);
		float e = Math.min(5.0F, s.openCount);
		float scale = (e / 5.0F) * 1.5F;
		float scaley = (c / 30.0F) * 1.5F;
		ps.scale(scale, scaley, 1.0F);

		int frame = (s.ticks / 2) % 16;
		float u0 = frame / 16.0F;
		float u1 = u0 + (1.0F / 16.0F);
		float v0 = 0.0F;
		float v1 = 1.0F;

		collector.submitCustomGeometry(ps, TVRenderTypes.glow(PORTAL_TEX, false, false), (pose, vc) -> {
			float h = 0.65F;
			vc.addVertex(pose, -h, -h, 0.0F).setColor(1.0F, 0.5F, 1.0F, 1.0F).setUv(u0, v1).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0F, 0.0F, 1.0F);
			vc.addVertex(pose,  h, -h, 0.0F).setColor(1.0F, 0.5F, 1.0F, 1.0F).setUv(u1, v1).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0F, 0.0F, 1.0F);
			vc.addVertex(pose,  h,  h, 0.0F).setColor(1.0F, 0.5F, 1.0F, 1.0F).setUv(u1, v0).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0F, 0.0F, 1.0F);
			vc.addVertex(pose, -h,  h, 0.0F).setColor(1.0F, 0.5F, 1.0F, 1.0F).setUv(u0, v0).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0F, 0.0F, 1.0F);
		});

		ps.popPose();
	}
}
