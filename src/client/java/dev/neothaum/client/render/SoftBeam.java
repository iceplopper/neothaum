package dev.neothaum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.neothaum.NeoThaum;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * A soft, wavering stream of light between two points, for vis drawn from a node and essentia drawn into an infusion. It turns its face to
 * the camera all along its length (so it has no hard edge from the side), moves on a smooth clock, wavers most in the middle, fades in at its
 * ends and flows along its length. Points are in the pose stack's space; {@code eye} is the camera there.
 */
public final class SoftBeam {
	public static final Identifier TEXTURE = NeoThaum.id("textures/misc/beam_soft.png");

	/**
	 * @param wobble how far the middle wanders, in blocks
	 * @param phase  sets this stream's wavering apart from others drawn between the same points
	 * @param flow   how fast the texture flows from {@code from} toward {@code to}
	 */
	public static void draw(PoseStack ps, SubmitNodeCollector collector, Vec3 eye, Vec3 from, Vec3 to, int rgb, float alpha, float width,
		float wobble, float phase, float flow, float progress) {
		SubmitNodeCollector.CustomGeometryRenderer g = geometry(eye, from, to, rgb, alpha, width, wobble, phase, flow, progress);
		if (g != null) {
			collector.submitCustomGeometry(ps, type(), g);
		}
	}

	public static net.minecraft.client.renderer.rendertype.RenderType type() {
		return TVRenderTypes.glow(TEXTURE, true, true);
	}

	/** The stream's geometry, for drawing it in a phase of the caller's choosing; null when there is nothing to draw. */
	public static SubmitNodeCollector.CustomGeometryRenderer geometry(Vec3 eye, Vec3 from, Vec3 to, int rgb, float alpha, float width,
		float wobble, float phase, float flow, float progress) {
		Vec3 line = to.subtract(from);
		double dist = line.length();
		if (dist < 0.05 || progress <= 0.0F) {
			return null;
		}
		double time = System.nanoTime() / 1.0E9;
		int segments = Math.max(8, (int) (dist * 12));
		int shown = Math.max(1, (int) (segments * Math.min(1.0F, progress)));
		List<Vec3> pts = new ArrayList<>();
		for (int i = 0; i <= shown; i++) {
			float t = (float) i / segments;
			float env = Mth.sin(t * Mth.PI);
			double w = (time * 2.0 + phase) - t * dist * 1.3;
			Vec3 p = from.add(line.scale(t)).add(Math.sin(w * 1.7 + phase) * wobble * env, Math.sin(w * 2.3 + phase * 1.3) * wobble * env,
				Math.sin(w * 1.1 + phase * 0.7) * wobble * env);
			pts.add(p);
		}
		float r = (rgb >> 16 & 255) / 255.0F;
		float g = (rgb >> 8 & 255) / 255.0F;
		float b = (rgb & 255) / 255.0F;
		float scroll = (float) (time * flow);
		return (pose, vc) -> {
			for (int i = 0; i < pts.size() - 1; i++) {
				Vec3 p0 = pts.get(i);
				Vec3 p1 = pts.get(i + 1);
				Vec3 side0 = side(pts, i, eye, width);
				Vec3 side1 = side(pts, i + 1, eye, width);
				float t0 = (float) i / segments;
				float t1 = (float) (i + 1) / segments;
				float a0 = alpha * fade(t0);
				float a1 = alpha * fade(t1);
				float u0 = (float) (t0 * dist / 2.0) - scroll;
				float u1 = (float) (t1 * dist / 2.0) - scroll;
				vertex(vc, pose, p0.subtract(side0), u0, 1.0F, r, g, b, a0);
				vertex(vc, pose, p0.add(side0), u0, 0.0F, r, g, b, a0);
				vertex(vc, pose, p1.add(side1), u1, 0.0F, r, g, b, a1);
				vertex(vc, pose, p1.subtract(side1), u1, 1.0F, r, g, b, a1);
			}
		};
	}

	/** Half the stream's width, across its length and across the line of sight, at point i. */
	private static Vec3 side(List<Vec3> pts, int i, Vec3 eye, float width) {
		Vec3 along = pts.get(Math.min(i + 1, pts.size() - 1)).subtract(pts.get(Math.max(i - 1, 0)));
		Vec3 across = along.cross(eye.subtract(pts.get(i)));
		double len = across.length();
		return len < 1.0E-6 ? new Vec3(0, width / 2.0, 0) : across.scale(width / 2.0 / len);
	}

	/** Fades in over the first and last tenth of the stream. */
	private static float fade(float t) {
		return Mth.clamp(Math.min(t, 1.0F - t) * 10.0F, 0.0F, 1.0F);
	}

	private static void vertex(VertexConsumer vc, PoseStack.Pose pose, Vec3 p, float u, float v, float r, float g, float b, float a) {
		vc.addVertex(pose, (float) p.x, (float) p.y, (float) p.z).setColor(r, g, b, a).setUv(u, v).setOverlay(0).setLight(0xF000F0)
			.setNormal(pose, 0.0F, 1.0F, 0.0F);
	}

	private SoftBeam() {
	}
}
