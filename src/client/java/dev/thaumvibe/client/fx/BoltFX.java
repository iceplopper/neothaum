package dev.thaumvibe.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.client.render.TVRenderTypes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * FXLightningBolt and FXLightningBoltCommon: a fractal bolt that grows from its source to its target and fades, drawn twice (a wide soft pass
 * from p_large.png, then a bright core from p_small.png) as camera-facing ribbons.
 */
public final class BoltFX {
	private static final Identifier LARGE = Thaumvibe.id("textures/misc/p_large.png");
	private static final Identifier SMALL = Thaumvibe.id("textures/misc/p_small.png");
	private static final List<BoltFX> BOLTS = new ArrayList<>();
	private static double cx, cy, cz;

	// ------------------------------------------------------------------ WRVector3

	private static final class V {
		float x, y, z;

		V(double x, double y, double z) {
			this.x = (float) x;
			this.y = (float) y;
			this.z = (float) z;
		}

		V add(V v) {
			x += v.x;
			y += v.y;
			z += v.z;
			return this;
		}

		V sub(V v) {
			x -= v.x;
			y -= v.y;
			z -= v.z;
			return this;
		}

		V scale(float s) {
			x *= s;
			y *= s;
			z *= s;
			return this;
		}

		float length() {
			return (float) Math.sqrt(x * x + y * y + z * z);
		}

		V normalize() {
			float l = length();
			x /= l;
			y /= l;
			z /= l;
			return this;
		}

		V copy() {
			return new V(x, y, z);
		}

		static V cross(V a, V b) {
			return new V(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
		}

		static V xCross(V v) {
			return new V(0.0, v.z, -v.y);
		}

		static V zCross(V v) {
			return new V(-v.y, v.x, 0.0);
		}

		static float dot(V a, V b) {
			return a.x * b.x + a.y * b.y + a.z * b.z;
		}

		static float anglePreNorm(V a, V b) {
			return (float) Math.acos(dot(a, b));
		}

		static V perpendicular(V v) {
			return v.z == 0.0F ? zCross(v) : xCross(v);
		}

		/** WRMat4.rotationMat(angle, axis).translate(this) */
		V rotate(float angle, V axis) {
			V a = axis.copy().normalize();
			double r = angle * 0.0174532925;
			float cos = (float) Math.cos(r), ocos = 1.0F - cos, sin = (float) Math.sin(r);
			float m0 = a.x * a.x * ocos + cos, m1 = a.y * a.x * ocos + a.z * sin, m2 = a.x * a.z * ocos - a.y * sin;
			float m4 = a.x * a.y * ocos - a.z * sin, m5 = a.y * a.y * ocos + cos, m6 = a.y * a.z * ocos + a.x * sin;
			float m8 = a.x * a.z * ocos + a.y * sin, m9 = a.y * a.z * ocos - a.x * sin, m10 = a.z * a.z * ocos + cos;
			float nx = x * m0 + y * m1 + z * m2, ny = x * m4 + y * m5 + z * m6, nz = x * m8 + y * m9 + z * m10;
			x = nx;
			y = ny;
			z = nz;
			return this;
		}
	}

	private static final class BoltPoint {
		final V point, basepoint, offsetvec;

		BoltPoint(V basepoint, V offsetvec) {
			this.point = basepoint.copy().add(offsetvec);
			this.basepoint = basepoint;
			this.offsetvec = offsetvec;
		}
	}

	private static final class Segment {
		final BoltPoint startpoint, endpoint;
		V diff, nextdiff, prevdiff;
		Segment prev, next;
		float sinprev, sinnext;
		final float light;
		final int segmentno, splitno;

		Segment(BoltPoint start, BoltPoint end, float light, int segmentno, int splitno) {
			this.startpoint = start;
			this.endpoint = end;
			this.light = light;
			this.segmentno = segmentno;
			this.splitno = splitno;
			this.diff = end.point.copy().sub(start.point);
		}

		void calcEndDiffs() {
			if (this.prev != null) {
				V p = this.prev.diff.copy().normalize();
				V t = this.diff.copy().normalize();
				this.prevdiff = t.copy().add(p).normalize();
				this.sinprev = (float) Math.sin(V.anglePreNorm(t, p.scale(-1.0F)) / 2.0F);
			} else {
				this.prevdiff = this.diff.copy().normalize();
				this.sinprev = 1.0F;
			}
			if (this.next != null) {
				V n = this.next.diff.copy().normalize();
				V t = this.diff.copy().normalize();
				this.nextdiff = t.copy().add(n).normalize();
				this.sinnext = (float) Math.sin(V.anglePreNorm(t, n.scale(-1.0F)) / 2.0F);
			} else {
				this.nextdiff = this.diff.copy().normalize();
				this.sinnext = 1.0F;
			}
		}
	}

	// ------------------------------------------------------------------ FXLightningBoltCommon

	private List<Segment> segments = new ArrayList<>();
	private final V start;
	private final Map<Integer, Integer> splitparents = new HashMap<>();
	private float multiplier = 1.0F;
	private final float length;
	private int numsegments0 = 1;
	private int increment = 1;
	private int type;
	private float width = 0.03F;
	private int numsplits;
	private boolean finalized;
	private final Random rand;
	private int particleAge;
	private int particleMaxAge;

	private BoltFX(V start, V end, long seed) {
		this.start = start;
		this.rand = new Random(seed);
		this.length = end.copy().sub(start).length();
		this.particleMaxAge = 3 + this.rand.nextInt(3) - 1;
		this.particleAge = -((int) (this.length * 3.0F));
		this.segments.add(new Segment(new BoltPoint(start, new V(0, 0, 0)), new BoltPoint(end, new V(0, 0, 0)), 1.0F, 0, 0));
	}

	private void fractal(int splits, float amount, float splitchance, float splitlength, float splitangle) {
		if (this.finalized) {
			return;
		}
		List<Segment> old = this.segments;
		this.segments = new ArrayList<>();
		Segment prev;
		for (Segment segment : old) {
			prev = segment.prev;
			V subsegment = segment.diff.copy().scale(1.0F / splits);
			BoltPoint[] newpoints = new BoltPoint[splits + 1];
			V startpoint = segment.startpoint.point;
			newpoints[0] = segment.startpoint;
			newpoints[splits] = segment.endpoint;
			for (int i = 1; i < splits; i++) {
				V randoff = V.perpendicular(segment.diff).rotate(this.rand.nextFloat() * 360.0F, segment.diff);
				randoff.scale((this.rand.nextFloat() - 0.5F) * amount);
				V basepoint = startpoint.copy().add(subsegment.copy().scale(i));
				newpoints[i] = new BoltPoint(basepoint, randoff);
			}
			for (int i = 0; i < splits; i++) {
				Segment next = new Segment(newpoints[i], newpoints[i + 1], segment.light, segment.segmentno * splits + i, segment.splitno);
				next.prev = prev;
				if (prev != null) {
					prev.next = next;
				}
				if (i != 0 && this.rand.nextFloat() < splitchance) {
					V splitrot = V.xCross(next.diff).rotate(this.rand.nextFloat() * 360.0F, next.diff);
					V diff = next.diff.copy().rotate((this.rand.nextFloat() * 0.66F + 0.33F) * splitangle, splitrot).scale(splitlength);
					this.numsplits++;
					this.splitparents.put(this.numsplits, next.splitno);
					Segment split = new Segment(newpoints[i], new BoltPoint(newpoints[i + 1].basepoint, newpoints[i + 1].offsetvec.copy().add(diff)),
						segment.light / 2.0F, next.segmentno, this.numsplits);
					split.prev = prev;
					this.segments.add(split);
				}
				prev = next;
				this.segments.add(next);
			}
			if (segment.next != null) {
				segment.next.prev = prev;
			}
		}
		this.numsegments0 *= splits;
	}

	private void defaultFractal() {
		float l = this.length * this.multiplier;
		fractal(2, l / 8.0F, 0.7F, 0.1F, 45.0F);
		fractal(2, l / 12.0F, 0.5F, 0.1F, 50.0F);
		fractal(2, l / 17.0F, 0.5F, 0.1F, 55.0F);
		fractal(2, l / 23.0F, 0.5F, 0.1F, 60.0F);
		fractal(2, l / 30.0F, 0.0F, 0.0F, 0.0F);
		fractal(2, l / 34.0F, 0.0F, 0.0F, 0.0F);
		fractal(2, l / 40.0F, 0.0F, 0.0F, 0.0F);
	}

	private void finalizeBolt() {
		if (this.finalized) {
			return;
		}
		this.finalized = true;
		Map<Integer, Integer> lastactivesegment = new HashMap<>();
		this.segments.sort(Comparator.<Segment>comparingInt(s -> s.splitno).thenComparingInt(s -> s.segmentno));
		int lastsplitcalc = 0;
		int lastactiveseg = 0;
		for (Segment segment : this.segments) {
			if (segment.splitno > lastsplitcalc) {
				lastactivesegment.put(lastsplitcalc, lastactiveseg);
				lastsplitcalc = segment.splitno;
				lastactiveseg = lastactivesegment.get(this.splitparents.get(segment.splitno));
			}
			lastactiveseg = segment.segmentno;
		}
		lastactivesegment.put(lastsplitcalc, lastactiveseg);
		lastsplitcalc = 0;
		lastactiveseg = lastactivesegment.get(0);
		for (Iterator<Segment> it = this.segments.iterator(); it.hasNext(); ) {
			Segment segment = it.next();
			if (lastsplitcalc != segment.splitno) {
				lastsplitcalc = segment.splitno;
				lastactiveseg = lastactivesegment.get(segment.splitno);
			}
			if (segment.segmentno > lastactiveseg) {
				it.remove();
			}
			segment.calcEndDiffs();
		}
		this.segments.sort((a, b) -> Float.compare(b.light, a.light));
		BOLTS.add(this);
	}

	// ------------------------------------------------------------------ ClientProxy.bolt / nodeBolt

	/** ClientProxy.bolt: a wisp's zap, from the source to the target's chest. */
	public static void bolt(Entity source, Entity target) {
		BoltFX b = new BoltFX(new V(source.getX(), source.getY(), source.getZ()),
			new V(target.getX(), target.getY() + target.getEyeHeight() - 0.7F, target.getZ()), source.level().getRandom().nextLong());
		b.increment = 4;
		b.multiplier = 0.4F;
		b.defaultFractal();
		b.type = 0;
		b.finalizeBolt();
	}

	/** FXLightningBolt(world, x1, y1, z1, x, y, z, seed, duration, multi, speed) */
	public static void bolt(double x1, double y1, double z1, double x2, double y2, double z2, int duration, float multi, int speed, int type,
		float width) {
		Random r = new Random();
		BoltFX b = new BoltFX(new V(x1, y1, z1), new V(x2, y2, z2), r.nextLong());
		b.particleMaxAge = duration + b.rand.nextInt(duration) - duration / 2;
		b.multiplier = multi;
		b.increment = speed;
		b.width = width;
		b.defaultFractal();
		b.type = type;
		b.finalizeBolt();
	}

	public static void tick(Minecraft mc) {
		for (Iterator<BoltFX> it = BOLTS.iterator(); it.hasNext(); ) {
			BoltFX b = it.next();
			b.particleAge += b.increment;
			if (b.particleAge > b.particleMaxAge) {
				b.particleAge = b.particleMaxAge;
			}
			if (b.particleAge >= b.particleMaxAge) {
				it.remove();
			}
		}
	}

	public static void render(LevelRenderContext ctx) {
		var mc = Minecraft.getInstance();
		if (BOLTS.isEmpty() || mc.level == null || mc.player == null) {
			return;
		}
		float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		var camera = mc.gameRenderer.mainCamera();
		Vec3 cam = camera.position();
		Vec3 look = Vec3.directionFromRotation(camera.xRot(), camera.yRot());
		V playervec = new V(look.x, look.y, look.z);
		PoseStack ps = ctx.poseStack();
		for (BoltFX b : BOLTS) {
			if (mc.player.position().distanceTo(new Vec3(b.start.x, b.start.y, b.start.z)) > 100.0) {
				continue;
			}
			float[] c0 = switch (b.type) {
				case 1 -> new float[]{0.6F, 0.6F, 0.1F};
				case 2 -> new float[]{0.1F, 0.1F, 0.6F};
				case 3 -> new float[]{0.1F, 1.0F, 0.1F};
				case 4 -> new float[]{0.6F, 0.1F, 0.1F};
				case 5 -> new float[]{0.6F, 0.2F, 0.6F};
				case 6 -> new float[]{0.75F, 1.0F, 1.0F};
				default -> new float[]{0.6F, 0.3F, 0.6F};
			};
			float[] c1 = switch (b.type) {
				case 1 -> new float[]{1.0F, 1.0F, 0.1F};
				case 2 -> new float[]{0.1F, 0.1F, 1.0F};
				case 3 -> new float[]{0.1F, 0.6F, 0.1F};
				case 4 -> new float[]{1.0F, 0.1F, 0.1F};
				case 5 -> new float[]{0.0F, 0.0F, 0.0F};
				case 6 -> new float[]{0.75F, 1.0F, 1.0F};
				default -> new float[]{1.0F, 0.6F, 1.0F};
			};
			boolean additive = b.type < 5;
			float ma = b.type == 6 ? 0.2F : 1.0F;
			ps.pushPose();
			cx = cam.x;
			cy = cam.y;
			cz = cam.z;
			ctx.submitNodeCollector().submitCustomGeometry(ps, TVRenderTypes.glow(LARGE, additive, true),
				(pose, vc) -> b.renderBolt(pose, vc, pt, playervec, cam, 0, ma, c0));
			ctx.submitNodeCollector().submitCustomGeometry(ps, TVRenderTypes.glow(SMALL, additive, true),
				(pose, vc) -> b.renderBolt(pose, vc, pt, playervec, cam, 1, ma, c1));
			ps.popPose();
		}
	}

	private void renderBolt(PoseStack.Pose pose, VertexConsumer vc, float partialframe, V playervec, Vec3 cam, int pass, float mainalpha, float[] c) {
		float boltage = this.particleAge >= 0 ? (float) this.particleAge / this.particleMaxAge : 0.0F;
		if (pass == 0) {
			mainalpha = (1.0F - boltage) * 0.4F;
		} else {
			mainalpha = 1.0F - boltage * 0.5F;
		}
		int l3 = Math.max(1, (int) (this.length * 3.0F));
		int renderlength = (int) ((this.particleAge + partialframe + (int) (this.length * 3.0F)) / l3 * this.numsegments0);
		for (Segment seg : this.segments) {
			if (seg.segmentno > renderlength) {
				continue;
			}
			V rel = new V(cam.x - seg.startpoint.point.x, cam.y - seg.startpoint.point.y, cam.z - seg.startpoint.point.z);
			float w = this.width * (rel.length() / 5.0F + 1.0F) * (1.0F + seg.light) * 0.5F;
			V diff1 = V.cross(playervec, seg.prevdiff).scale(w / seg.sinprev);
			V diff2 = V.cross(playervec, seg.nextdiff).scale(w / seg.sinnext);
			V s = seg.startpoint.point;
			V e = seg.endpoint.point;
			float a = mainalpha * seg.light;
			vert(pose, vc, e.x - diff2.x, e.y - diff2.y, e.z - diff2.z, 0.5F, 0.0F, c, a);
			vert(pose, vc, s.x - diff1.x, s.y - diff1.y, s.z - diff1.z, 0.5F, 0.0F, c, a);
			vert(pose, vc, s.x + diff1.x, s.y + diff1.y, s.z + diff1.z, 0.5F, 1.0F, c, a);
			vert(pose, vc, e.x + diff2.x, e.y + diff2.y, e.z + diff2.z, 0.5F, 1.0F, c, a);
			if (seg.next == null) {
				V r = e.copy().add(seg.diff.copy().normalize().scale(w));
				vert(pose, vc, r.x - diff2.x, r.y - diff2.y, r.z - diff2.z, 0.0F, 0.0F, c, a);
				vert(pose, vc, e.x - diff2.x, e.y - diff2.y, e.z - diff2.z, 0.5F, 0.0F, c, a);
				vert(pose, vc, e.x + diff2.x, e.y + diff2.y, e.z + diff2.z, 0.5F, 1.0F, c, a);
				vert(pose, vc, r.x + diff2.x, r.y + diff2.y, r.z + diff2.z, 0.0F, 1.0F, c, a);
			}
			if (seg.prev == null) {
				V r = s.copy().sub(seg.diff.copy().normalize().scale(w));
				vert(pose, vc, s.x - diff1.x, s.y - diff1.y, s.z - diff1.z, 0.5F, 0.0F, c, a);
				vert(pose, vc, r.x - diff1.x, r.y - diff1.y, r.z - diff1.z, 0.0F, 0.0F, c, a);
				vert(pose, vc, r.x + diff1.x, r.y + diff1.y, r.z + diff1.z, 0.0F, 1.0F, c, a);
				vert(pose, vc, s.x + diff1.x, s.y + diff1.y, s.z + diff1.z, 0.5F, 1.0F, c, a);
			}
		}
	}

	private static void vert(PoseStack.Pose pose, VertexConsumer vc, float x, float y, float z, float u, float v, float[] c, float a) {
		vc.addVertex(pose, (float) (x - cx), (float) (y - cy), (float) (z - cz)).setColor(c[0], c[1], c[2], Math.max(0.0F, Math.min(1.0F, a))).setUv(u, v).setOverlay(0).setLight(0xF000F0)
			.setNormal(pose, 0.0F, 1.0F, 0.0F);
	}

	private BoltFX() {
		throw new UnsupportedOperationException();
	}
}
