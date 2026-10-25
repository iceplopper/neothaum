package dev.thaumvibe.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.client.render.TVRenderTypes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Port of FXBeamWand: the triple-twisted beam a channelled focus draws from the caster's hand to where it lands, kept alive while the caster
 * keeps feeding it targets (ClientProxy.beamCont), with a flaring burst at the far end on impact.
 */
public final class BeamFX {
	private static final Identifier[] TEX = {Thaumvibe.id("textures/misc/beam.png"), Thaumvibe.id("textures/misc/beam1.png"),
		Thaumvibe.id("textures/misc/beam2.png"), Thaumvibe.id("textures/misc/beam3.png")};
	private static final Identifier PARTICLES = Thaumvibe.id("textures/misc/particles.png");
	private static final List<BeamFX> BEAMS = new ArrayList<>();

	private final Player player;
	private final double offset;
	private double tX, tY, tZ;
	private float r, g, b;
	private int type;
	private float endMod = 1.0F;
	private boolean reverse;
	private int age;
	private int maxAge;
	private float prevSize;
	private float size;
	private int impact;
	private boolean dead;

	private BeamFX(Player player, double tx, double ty, double tz, float r, float g, float b, int age) {
		this.player = player;
		Minecraft mc = Minecraft.getInstance();
		this.offset = mc.getCameraEntity() != null && player.getId() != mc.getCameraEntity().getId() ? player.getBbHeight() / 2.0F + 0.25 : 0.0;
		this.tX = tx;
		this.tY = ty;
		this.tZ = tz;
		this.r = r;
		this.g = g;
		this.b = b;
		this.maxAge = age;
		if (mc.getCameraEntity() != null && mc.getCameraEntity().position().distanceTo(player.position()) > 50.0) {
			this.maxAge = 0;
		}
	}

	/** ClientProxy.beamCont: refresh the beam handed back last tick, or start a new one. */
	public static Object beamCont(Player p, double tx, double ty, double tz, int type, int color, boolean reverse, float endmod, Object input,
		int impact) {
		if (input instanceof BeamFX beam && !beam.dead) {
			beam.tX = tx;
			beam.tY = ty;
			beam.tZ = tz;
			while (beam.maxAge - beam.age < 4) {
				beam.maxAge++;
			}
			beam.endMod = endmod;
			beam.impact = impact;
			return beam;
		}
		BeamFX beam = new BeamFX(p, tx, ty, tz, (color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, 8);
		beam.type = type;
		beam.endMod = endmod;
		beam.reverse = reverse;
		BEAMS.add(beam);
		return beam;
	}

	public static void tick(Minecraft mc) {
		for (Iterator<BeamFX> it = BEAMS.iterator(); it.hasNext(); ) {
			BeamFX bm = it.next();
			if (bm.impact > 0) {
				bm.impact--;
			}
			if (bm.age++ >= bm.maxAge || !bm.player.isAlive()) {
				bm.dead = true;
				it.remove();
			}
		}
	}

	private Vec3 hand(float pt) {
		double px = Mth.lerp(pt, this.player.xo, this.player.getX());
		double py = Mth.lerp(pt, this.player.yo, this.player.getY()) + this.offset + (this.offset == 0.0 ? this.player.getEyeHeight() : 0.0);
		double pz = Mth.lerp(pt, this.player.zo, this.player.getZ());
		float yaw = Mth.lerp(pt, this.player.yRotO, this.player.getYRot());
		px -= Mth.cos(yaw / 180.0F * (float) Math.PI) * 0.066F;
		py -= 0.06;
		pz -= Mth.sin(yaw / 180.0F * (float) Math.PI) * 0.04F;
		Vec3 look = this.player.getViewVector(pt);
		return new Vec3(px + look.x * 0.3, py + look.y * 0.3, pz + look.z * 0.3);
	}

	public static void render(LevelRenderContext ctx) {
		Minecraft mc = Minecraft.getInstance();
		if (BEAMS.isEmpty() || mc.level == null || mc.player == null) {
			return;
		}
		float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		Vec3 cam = mc.gameRenderer.mainCamera().position();
		PoseStack ps = ctx.poseStack();
		for (BeamFX bm : BEAMS) {
			Vec3 h = bm.hand(pt);
			double xd = h.x - bm.tX;
			double yd = h.y - bm.tY;
			double zd = h.z - bm.tZ;
			float length = (float) Math.sqrt(xd * xd + yd * yd + zd * zd);
			float yaw = (float) (Math.atan2(xd, zd) * 180.0 / Math.PI);
			float pitch = (float) (Math.atan2(yd, Math.sqrt(xd * xd + zd * zd)) * 180.0 / Math.PI);
			float size = Math.min((bm.age + pt) / 4.0F, 1.0F);
			float op = 0.4F;
			if (bm.maxAge - bm.age <= 4) {
				op = Math.max(0.0F, 0.4F - (4 - (bm.maxAge - bm.age)) * 0.1F);
			}
			float slide = mc.player.tickCount + pt;
			if (bm.reverse) {
				slide = -slide;
			}
			float scroll = -slide * 0.2F - Mth.floor(-slide * 0.1F);
			float rot = (float) (mc.level.getGameTime() % (360 / 5) * 5) + 5 * pt;
			ps.pushPose();
			ps.translate(h.x - cam.x, h.y - cam.y, h.z - cam.z);
			ps.rotateDegrees(Axis.XP, 90.0F);
			ps.rotateDegrees(Axis.ZN, 180.0F + yaw);
			ps.rotateDegrees(Axis.XP, pitch);
			ps.rotateDegrees(Axis.YP, rot);
			final float fop = op;
			final float fsize = size;
			for (int t = 0; t < 3; t++) {
				// the collector keeps a reference to the pose, so each ribbon gets its own
				ps.pushPose();
				ps.rotateDegrees(Axis.YP, 60.0F * (t + 1));
				final int ft = t;
				ctx.submitNodeCollector().submitCustomGeometry(ps, TVRenderTypes.glow(TEX[Math.min(3, Math.max(0, bm.type))], true, true),
					(pose, vc) -> bm.quad(pose, vc, length * fsize, fsize, -1.0F + scroll + ft / 3.0F, fop));
				ps.popPose();
			}
			ps.popPose();
			if (bm.impact > 0) {
				bm.renderImpact(ctx, ps, cam, pt);
			}
		}
	}

	/** One twisted ribbon; its V scrolls along the beam, split at whole numbers since the beam textures are clamped, not tiled. */
	private void quad(PoseStack.Pose pose, VertexConsumer vc, float len, float size, float v0, float op) {
		float w0 = 0.15F * size;
		float w1 = 0.15F * size * this.endMod;
		float vEnd = v0 + len;
		float start = v0;
		while (start < vEnd - 1.0E-4F) {
			float next = Math.min(vEnd, Mth.floor(start) + 1.0F);
			float f0 = (start - v0) / len;
			float f1 = (next - v0) / len;
			float a0 = Mth.lerp(f0, w0, w1);
			float a1 = Mth.lerp(f1, w0, w1);
			float base = Mth.floor(start);
			float uv0 = start - base;
			float uv1 = next - base;
			v(pose, vc, -a1, len * f1, 1.0F, uv1, op);
			v(pose, vc, -a0, len * f0, 1.0F, uv0, op);
			v(pose, vc, a0, len * f0, 0.0F, uv0, op);
			v(pose, vc, a1, len * f1, 0.0F, uv1, op);
			start = next;
		}
	}

	private void v(PoseStack.Pose pose, VertexConsumer vc, float x, float y, float u, float vv, float a) {
		vc.addVertex(pose, x, y, 0.0F).setColor(this.r, this.g, this.b, a).setUv(u, vv).setOverlay(0).setLight(0xF000F0).setNormal(pose, 0.0F, 0.0F, 1.0F);
	}

	/** FXBeamWand.renderImpact: a camera-facing flare from particles.png at the far end. */
	private void renderImpact(LevelRenderContext ctx, PoseStack ps, Vec3 cam, float pt) {
		Minecraft mc = Minecraft.getInstance();
		int part = mc.player.tickCount % 16;
		float u0 = part / 16.0F;
		float u1 = u0 + 0.0624375F;
		float v0 = 0.3125F;
		float v1 = v0 + 0.0624375F;
		float scale = this.endMod / 2.0F / (6 - this.impact);
		ps.pushPose();
		ps.translate(this.tX - cam.x, this.tY - cam.y, this.tZ - cam.z);
		ps.rotate(mc.gameRenderer.mainCamera().rotation());
		ctx.submitNodeCollector().submitCustomGeometry(ps, TVRenderTypes.glow(PARTICLES, true, true), (pose, vc) -> {
			v(pose, vc, -scale, -scale, u1, v1, 0.66F);
			v(pose, vc, -scale, scale, u1, v0, 0.66F);
			v(pose, vc, scale, scale, u0, v0, 0.66F);
			v(pose, vc, scale, -scale, u0, v1, 0.66F);
		});
		ps.popPose();
	}

	private BeamFX() {
		throw new UnsupportedOperationException();
	}
}
