package dev.neothaum.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.neothaum.NeoThaum;
import dev.neothaum.client.render.ObjModel;
import dev.neothaum.client.render.TVRenderTypes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * FXArc and FXSonic. An arc is a one-tick crackling lob of lightning from an entity down to a block (the Eldritch Warden's sapping field, the
 * cultist portal), drawn as two crossed ribbons of beamh along a noisy parabola, with a sparkle where it lands. A sonic is the warden's
 * screech: a ripple hemisphere flung out in front of its face, burning through fifteen frames.
 */
public final class ArcSonicFX {
	private static final Identifier BEAM = NeoThaum.id("textures/misc/beamh.png");
	private static final Identifier HEMIS = NeoThaum.id("textures/models/hemis.obj");
	private static ObjModel model;

	private record Arc(double x, double y, double z, List<Vec3> points, float length, float r, float g, float b, int[] age) {
	}

	private static final class Sonic {
		final int entity;
		final float yaw;
		final float pitch;
		final int maxAge;
		int age;

		Sonic(int entity, float yaw, float pitch, int maxAge) {
			this.entity = entity;
			this.yaw = yaw;
			this.pitch = pitch;
			this.maxAge = maxAge;
		}
	}

	private static final List<Arc> ARCS = new ArrayList<>();
	private static final List<Sonic> SONICS = new ArrayList<>();

	private ArcSonicFX() {
	}

	/** ClientProxy.arcLightning: from an entity's middle to the top of a block. */
	public static void arcLightning(double x, double y, double z, double tx, double ty, double tz, float r, float g, float b, double hg) {
		var level = Minecraft.getInstance().level;
		if (level == null) {
			return;
		}
		dev.neothaum.fx.FX.sparkle(level, tx, ty, tz, 3.0F, 6, 0.0F);
		Random rand = new Random();
		double gravity = 0.115;
		double noise = 0.25;
		Vec3 vs = Vec3.ZERO;
		Vec3 ve = new Vec3(tx - x, ty - y, tz - z);
		Vec3 vc = Vec3.ZERO;
		Vec3 vv = velocity(vs, ve, hg, gravity);
		double l = vv.lengthSqr();
		List<Vec3> points = new ArrayList<>();
		points.add(vs);
		for (int c = 0; ve.distanceToSqr(vc) > l && c < 50; c++) {
			vc = vc.add(vv);
			points.add(vc.add((rand.nextDouble() - rand.nextDouble()) * noise, (rand.nextDouble() - rand.nextDouble()) * noise,
				(rand.nextDouble() - rand.nextDouble()) * noise));
			vv = vv.add(0.0, -gravity / 1.9, 0.0);
		}
		points.add(ve);
		ARCS.add(new Arc(x, y, z, points, (float) ve.length(), r, g, b, new int[]{0}));
	}

	/** Utils.calculateVelocity: the launch that lobs from one point to another over a hump of the given height. */
	private static Vec3 velocity(Vec3 from, Vec3 to, double heightGain, double gravity) {
		double endGain = to.y - from.y;
		double horizDist = Math.sqrt((to.x - from.x) * (to.x - from.x) + (to.z - from.z) * (to.z - from.z));
		double maxGain = Math.max(heightGain, endGain + heightGain);
		double a = -horizDist * horizDist / (4.0 * maxGain);
		double c = -endGain;
		double slope = -horizDist / (2.0 * a) - Math.sqrt(horizDist * horizDist - 4.0 * a * c) / (2.0 * a);
		double vy = Math.sqrt(maxGain * gravity);
		double vh = vy / slope;
		double dx = to.x - from.x;
		double dz = to.z - from.z;
		double mag = Math.sqrt(dx * dx + dz * dz);
		return new Vec3(vh * dx / mag, vy, vh * dz / mag);
	}

	/** PacketFXSonic. */
	public static void sonic(Entity e) {
		SONICS.add(new Sonic(e.getId(), e.getYHeadRot(), e.getXRot(), 10 + e.getRandom().nextInt(5)));
	}

	public static void tick(Minecraft mc) {
		for (Iterator<Arc> it = ARCS.iterator(); it.hasNext(); ) {
			Arc a = it.next();
			if (a.age[0]++ >= 1) {
				it.remove();
			}
		}
		for (Sonic s : SONICS) {
			s.age++;
		}
		SONICS.removeIf(s -> s.age >= s.maxAge);
	}

	public static void render(LevelRenderContext ctx) {
		var mc = Minecraft.getInstance();
		if (mc.level == null || ARCS.isEmpty() && SONICS.isEmpty()) {
			return;
		}
		Vec3 cam = mc.gameRenderer.mainCamera().position();
		PoseStack ps = ctx.poseStack();
		float size = 0.25F;
		for (Arc a : ARCS) {
			ps.pushPose();
			ps.translate(a.x - cam.x, a.y - cam.y, a.z - cam.z);
			ctx.submitNodeCollector().submitCustomGeometry(ps, TVRenderTypes.glow(BEAM, true, true), (pose, vc) -> {
				for (int pass = 0; pass < 2; pass++) {
					for (int c = 0; c + 1 < a.points.size(); c++) {
						Vec3 p0 = a.points.get(c);
						Vec3 p1 = a.points.get(c + 1);
						float u0 = c / a.length;
						float u1 = (c + 1) / a.length;
						float ox = pass == 0 ? 0.0F : size;
						float oy = pass == 0 ? size : 0.0F;
						quad(pose, vc, p0, p1, ox, oy, u0, u1, a.r, a.g, a.b);
					}
				}
			});
			ps.popPose();
		}
		if (SONICS.isEmpty()) {
			return;
		}
		if (model == null) {
			model = ObjModel.load(HEMIS);
		}
		float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		for (Sonic s : SONICS) {
			Entity target = mc.level.getEntity(s.entity);
			if (target == null) {
				continue;
			}
			float fade = (s.age + pt) / s.maxAge;
			int frame = Math.min(15, (int) (14.0F * fade) + 1);
			var tex = NeoThaum.id("textures/models/ripple" + frame + ".png");
			Vec3 pos = target.getPosition(pt);
			ps.pushPose();
			ps.translate(pos.x - cam.x, pos.y + target.getEyeHeight() - cam.y, pos.z - cam.z);
			ps.rotateDegrees(Axis.YP, -s.yaw);
			ps.rotateDegrees(Axis.XP, s.pitch);
			ps.translate(0.0, 0.0, 2.0F * target.getBbHeight() + target.getBbWidth() / 2.0F);
			ps.scale(0.25F * target.getBbHeight(), 0.25F * target.getBbHeight(), -1.0F * target.getBbHeight());
			int grey = (int) (0.5F * 255);
			int argb = 0xFF000000 | grey << 16 | grey << 8 | grey;
			ctx.submitNodeCollector().submitCustomGeometry(ps, TVRenderTypes.glow(tex, true, true),
				(pose, vc) -> model.render(pose, vc, 0xDC00DC, OverlayTexture.NO_OVERLAY, argb));
			ps.popPose();
		}
	}

	private static void quad(PoseStack.Pose pose, com.mojang.blaze3d.vertex.VertexConsumer vc, Vec3 p0, Vec3 p1, float ox, float oy, float u0, float u1,
		float r, float g, float b) {
		v(pose, vc, p0.x - ox, p0.y - oy, p0.z - ox, u0, 1.0F, r, g, b);
		v(pose, vc, p0.x + ox, p0.y + oy, p0.z + ox, u0, 0.0F, r, g, b);
		v(pose, vc, p1.x + ox, p1.y + oy, p1.z + ox, u1, 0.0F, r, g, b);
		v(pose, vc, p1.x - ox, p1.y - oy, p1.z - ox, u1, 1.0F, r, g, b);
	}

	private static void v(PoseStack.Pose pose, com.mojang.blaze3d.vertex.VertexConsumer vc, double x, double y, double z, float u, float v, float r,
		float g, float b) {
		vc.addVertex(pose, (float) x, (float) y, (float) z).setColor(r, g, b, 0.8F).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
			.setLight(Mth.clamp(200, 0, 240) | 200 << 16).setNormal(pose, 0.0F, 1.0F, 0.0F);
	}
}
