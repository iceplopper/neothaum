package dev.thaumvibe.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.client.render.TVRenderTypes;
import dev.thaumvibe.entity.monster.Wisp;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * RenderWisp: two camera-facing glows, the wisp's own frame of wisp.png in its aspect's colour (reddened while hurt), and a pulsing white
 * spark from particles.png over it.
 */
public class WispRenderer extends EntityRenderer<Wisp, WispRenderer.State> {
	private static final Identifier WISP = Thaumvibe.id("textures/misc/wisp.png");

	public static class State extends EntityRenderState {
		int color;
		boolean hurt;
		boolean dead;
		int ticks;
		float partial;
	}

	public WispRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.0F;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(Wisp entity, State state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		Aspect a = entity.getAspect();
		state.color = a == null ? 0 : a.getColor();
		state.hurt = entity.hurtTime > 0;
		state.dead = entity.getHealth() <= 0.0F;
		state.ticks = entity.tickCount;
		state.partial = partialTicks;
	}

	@Override
	public void submit(State state, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.dead) {
			return;
		}
		ps.pushPose();
		ps.translate(0.0F, 0.45F, 0.0F);
		ps.rotate(camera.orientation);
		int c = state.color;
		float r = (c >> 16 & 255) / 255.0F, g = (c >> 8 & 255) / 255.0F, b = (c & 255) / 255.0F;
		if (state.hurt) {
			r = 1.0F;
			g = (c >> 8 & 255) / 300.0F;
			b = (c & 255) / 300.0F;
		}
		// wisp.png is a 4x4 sheet of frames
		int i = state.ticks % 16;
		float x0 = (i % 4) / 4.0F, x1 = (i % 4 + 0.99F) / 4.0F, y0 = (i / 4) / 4.0F, y1 = (i / 4 + 0.99F) / 4.0F;
		float fr = r, fg = g, fb = b;
		collector.submitCustomGeometry(ps, TVRenderTypes.glow(WISP, true, true), (pose, vc) -> quad(pose, vc, 1.0F, x0, x1, y0, y1, fr, fg, fb));
		int qq = state.ticks % 16;
		float u0 = qq / 16.0F, u1 = (qq + 1) / 16.0F, v0 = 5.0F / 16.0F, v1 = 6.0F / 16.0F;
		float size = 0.4F + Mth.sin((state.ticks + state.partial) / 10.0F) * 0.1F;
		collector.submitCustomGeometry(ps, TVRenderTypes.glow(TVRenderTypes.PARTICLES, true, true),
			(pose, vc) -> quad(pose, vc, size, u0, u1, v0, v1, 1.0F, 1.0F, 1.0F));
		ps.popPose();
	}

	private static void quad(PoseStack.Pose pose, VertexConsumer vc, float s, float u0, float u1, float v0, float v1, float r, float g, float b) {
		v(pose, vc, -s, -s, u1, v1, r, g, b);
		v(pose, vc, s, -s, u0, v1, r, g, b);
		v(pose, vc, s, s, u0, v0, r, g, b);
		v(pose, vc, -s, s, u1, v0, r, g, b);
	}

	private static void v(PoseStack.Pose pose, VertexConsumer vc, float x, float y, float u, float v, float r, float g, float b) {
		vc.addVertex(pose, x, y, 0.0F).setColor(r, g, b, 1.0F).setUv(u, v).setOverlay(0).setLight(0xF000F0).setNormal(pose, 0.0F, 0.0F, 1.0F);
	}
}
