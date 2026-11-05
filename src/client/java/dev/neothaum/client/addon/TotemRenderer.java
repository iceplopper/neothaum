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

/**
 * A ring of glowing runes turning slowly above a totem, rising and falling a little. Its colour is for whoever looks at it: violet over a
 * Traveller's Totem they have attuned to (amber over their own Home Totem), sea-green over one someone else has attuned to (or claimed) but
 * they haven't, and a faint grey over one nobody has.
 */
public class TotemRenderer implements BlockEntityRenderer<TotemBlockEntity, TotemRenderer.State> {
	private static final Identifier RING = NeoThaum.id("textures/misc/totem_ring.png");
	private static final int SEGMENTS = 20;

	public static class State extends BlockEntityRenderState {
		float time;
		float r;
		float g;
		float b;
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
		float[] c = colour(be, net.minecraft.client.Minecraft.getInstance().player);
		s.r = c[0];
		s.g = c[1];
		s.b = c[2];
	}

	/** The ring's colour for this player: theirs, someone else's, or nobody's yet. */
	public static float[] colour(TotemBlockEntity be, net.minecraft.world.entity.player.Player viewer) {
		boolean mine = false;
		if (viewer != null && be.getLevel() != null) {
			var gp = be.globalPos();
			var known = viewer.getAttachedOrCreate(dev.neothaum.addon.totems.TravellersTotems.KNOWN);
			mine = be.isHome() ? known.home().filter(gp::equals).isPresent() : known.learned().contains(gp);
		}
		return mine ? (be.isHome() ? AMBER : VIOLET) : be.attuned ? OTHERS : NOBODY;
	}

	public static final float[] VIOLET = {0.69F, 0.47F, 1.0F};
	public static final float[] AMBER = {1.0F, 0.75F, 0.31F};
	/** Attuned by someone, but not by the one looking. */
	public static final float[] OTHERS = {0.35F, 0.9F, 0.75F};
	/** Attuned by nobody yet. */
	public static final float[] NOBODY = {0.62F, 0.62F, 0.66F};

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		float r = s.r;
		float g = s.g;
		float b = s.b;
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
