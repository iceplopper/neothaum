package dev.neothaum.client.addon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.neothaum.NeoThaum;
import dev.neothaum.addon.totems.TotemBlockEntity;
import dev.neothaum.client.render.TVRenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** A ring of glowing runes turning slowly above a totem, rising and falling a little: violet over a Traveller's Totem, amber over a Home Totem. */
public class TotemRenderer implements BlockEntityRenderer<TotemBlockEntity, TotemRenderer.State> {
	private static final Identifier RING = NeoThaum.id("textures/misc/totem_ring.png");
	private static final int SEGMENTS = 20;

	public static class State extends BlockEntityRenderState {
		float time;
		boolean home;
	}

	public TotemRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(TotemBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		s.time = (be.getLevel() == null ? 0 : be.getLevel().getGameTime() % 24000L) + pt + (be.getBlockPos().hashCode() & 255);
		s.home = be.isHome();
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		float r = s.home ? 1.0F : 0.69F;
		float g = s.home ? 0.75F : 0.47F;
		float b = s.home ? 0.31F : 1.0F;
		float bob = Mth.sin(s.time / 20.0F) * 0.03F;
		ps.pushPose();
		ps.translate(0.5F, 1.12F + bob, 0.5F);
		ps.rotateDegrees(com.mojang.math.Axis.YP, s.time * 1.2F);
		collector.submitCustomGeometry(ps, TVRenderTypes.glow(RING, false, true), (pose, vc) -> {
			float radius = 0.42F;
			float half = 0.06F;
			for (int i = 0; i < SEGMENTS; i++) {
				float a0 = (float) (i * Math.PI * 2 / SEGMENTS);
				float a1 = (float) ((i + 1) * Math.PI * 2 / SEGMENTS);
				float x0 = Mth.cos(a0) * radius, z0 = Mth.sin(a0) * radius;
				float x1 = Mth.cos(a1) * radius, z1 = Mth.sin(a1) * radius;
				float u0 = (float) i / SEGMENTS, u1 = (float) (i + 1) / SEGMENTS;
				// both faces, so it shows from inside and out
				quad(vc, pose, x0, z0, x1, z1, -half, half, u0, u1, r, g, b);
				quad(vc, pose, x1, z1, x0, z0, -half, half, u1, u0, r, g, b);
			}
		});
		ps.popPose();
	}

	private static void quad(VertexConsumer vc, PoseStack.Pose pose, float x0, float z0, float x1, float z1, float y0, float y1, float u0, float u1,
		float r, float g, float b) {
		vertex(vc, pose, x0, y0, z0, u0, 1, r, g, b);
		vertex(vc, pose, x1, y0, z1, u1, 1, r, g, b);
		vertex(vc, pose, x1, y1, z1, u1, 0, r, g, b);
		vertex(vc, pose, x0, y1, z0, u0, 0, r, g, b);
	}

	private static void vertex(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float z, float u, float v, float r, float g, float b) {
		vc.addVertex(pose, x, y, z).setColor(r, g, b, 0.9F).setUv(u, v).setOverlay(0).setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
	}
}
