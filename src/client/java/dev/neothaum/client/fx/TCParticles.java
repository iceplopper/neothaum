package dev.neothaum.client.fx;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

/** Ports of FXWisp, FXSparkle, FXBubble and FXGeneric. */
public final class TCParticles {
	private TCParticles() {
	}

	/** EntityFX default particleScale. */
	private static float defaultScale(net.minecraft.util.RandomSource r) {
		return (r.nextFloat() * 0.5F + 0.5F) * 2.0F;
	}

	public static class Wisp extends TCParticle {
		public boolean shrink = false;
		public boolean tinkle = false;
		private final float moteParticleScale;
		private final int moteHalfLife;
		private double tx, ty, tz;
		private boolean hasTarget;

		public Wisp(ClientLevel level, double x, double y, double z, float f, float red, float green, float blue) {
			super(level, x, y, z);
			if (red == 0.0F) {
				red = 1.0F;
			}
			this.rCol = red;
			this.gCol = green;
			this.bCol = blue;
			this.alpha = 0.5F;
			this.xd = this.yd = this.zd = 0.0;
			this.moteParticleScale = defaultScale(this.random) * f;
			this.lifetime = (int) (36.0 / (Math.random() * 0.3 + 0.7));
			this.moteHalfLife = this.lifetime / 2;
			if (tooFar(x, y, z)) {
				this.lifetime = 0;
			}
			setUV(0.0F, 0.875F, 0.125F, 1.0F);
		}

		public Wisp(ClientLevel level, double x, double y, double z, float f, int type) {
			this(level, x, y, z, f, 0.0F, 0.0F, 0.0F);
			var r = level.getRandom();
			switch (type) {
				case 0 -> setColor(0.75F + r.nextFloat() * 0.25F, 0.25F + r.nextFloat() * 0.25F, 0.75F + r.nextFloat() * 0.25F);
				case 1 -> setColor(0.5F + r.nextFloat() * 0.3F, 0.5F + r.nextFloat() * 0.3F, 0.2F);
				case 2 -> setColor(0.2F, 0.2F, 0.7F + r.nextFloat() * 0.3F);
				case 3 -> setColor(0.2F, 0.7F + r.nextFloat() * 0.3F, 0.2F);
				case 4 -> setColor(0.7F + r.nextFloat() * 0.3F, 0.2F, 0.2F);
				case 5 -> {
					this.additive = false;
					setColor(r.nextFloat() * 0.1F, r.nextFloat() * 0.1F, r.nextFloat() * 0.1F);
				}
				case 6 -> setColor(0.8F + r.nextFloat() * 0.2F, 0.8F + r.nextFloat() * 0.2F, 0.8F + r.nextFloat() * 0.2F);
				case 7 -> setColor(0.7F + r.nextFloat() * 0.3F, 0.5F + r.nextFloat() * 0.2F, 0.3F + r.nextFloat() * 0.1F);
				default -> {
				}
			}
		}

		public Wisp(ClientLevel level, double x, double y, double z, double tx, double ty, double tz, float f, int type) {
			this(level, x, y, z, f, type);
			if (this.lifetime > 0) {
				this.xd = (tx - this.x) / this.lifetime;
				this.yd = (ty - this.y) / this.lifetime;
				this.zd = (tz - this.z) / this.lifetime;
			}
		}

		public void setGravityTC(float g) {
			this.gravity = g;
		}

		@Override
		public float getQuadSize(float a) {
			float agescale;
			if (this.shrink) {
				agescale = ((float) this.lifetime - this.age) / Math.max(1, this.lifetime);
			} else {
				agescale = (float) this.age / Math.max(1, this.moteHalfLife);
				if (agescale > 1.0F) {
					agescale = 2.0F - agescale;
				}
			}
			return 0.5F * this.moteParticleScale * agescale;
		}

		@Override
		public void tick() {
			this.xo = this.x;
			this.yo = this.y;
			this.zo = this.z;
			if (this.age++ >= this.lifetime) {
				this.remove();
				return;
			}
			this.yd -= 0.04 * this.gravity;
			this.move(this.xd, this.yd, this.zd);
			this.xd *= 0.98F;
			this.yd *= 0.98F;
			this.zd *= 0.98F;
			if (this.onGround) {
				this.xd *= 0.7F;
				this.zd *= 0.7F;
			}
		}
	}

	public static class Sparkle extends TCParticle {
		public boolean shrink = true;
		public boolean slowdown = true;
		public int particle = 16;
		private final int multiplier;
		private final float particleScale;

		public Sparkle(ClientLevel level, double x, double y, double z, float f, float r, float g, float b, int m) {
			super(level, x, y, z);
			if (r == 0.0F) {
				r = 1.0F;
			}
			setColor(r, g, b);
			this.xd = this.yd = this.zd = 0.0;
			this.particleScale = defaultScale(this.random) * f;
			this.lifetime = 3 * m;
			this.multiplier = m;
		}

		public Sparkle(ClientLevel level, double x, double y, double z, float f, int type, int m) {
			this(level, x, y, z, f, 0.0F, 0.0F, 0.0F, m);
			var r = level.getRandom();
			switch (type) {
				case 0 -> setColor(0.75F + r.nextFloat() * 0.25F, 0.25F + r.nextFloat() * 0.25F, 0.75F + r.nextFloat() * 0.25F);
				case 1 -> setColor(0.5F + r.nextFloat() * 0.3F, 0.5F + r.nextFloat() * 0.3F, 0.2F);
				case 2 -> setColor(0.2F, 0.2F, 0.7F + r.nextFloat() * 0.3F);
				case 3 -> setColor(0.2F, 0.7F + r.nextFloat() * 0.3F, 0.2F);
				case 4 -> setColor(0.7F + r.nextFloat() * 0.3F, 0.2F, 0.2F);
				case 5 -> {
					this.additive = false;
					setColor(r.nextFloat() * 0.1F, r.nextFloat() * 0.1F, r.nextFloat() * 0.1F);
				}
				case 6 -> setColor(0.8F + r.nextFloat() * 0.2F, 0.8F + r.nextFloat() * 0.2F, 0.8F + r.nextFloat() * 0.2F);
				case 7 -> setColor(0.2F, 0.5F + r.nextFloat() * 0.3F, 0.6F + r.nextFloat() * 0.3F);
				default -> {
				}
			}
		}

		public void setGravityTC(float g) {
			this.gravity = g;
		}

		@Override
		public float getQuadSize(float a) {
			float s = 0.1F * this.particleScale;
			if (this.shrink) {
				s *= (float) (this.lifetime - this.age + 1) / Math.max(1, this.lifetime);
			}
			return s;
		}

		@Override
		public void tick() {
			this.xo = this.x;
			this.yo = this.y;
			this.zo = this.z;
			if (this.age++ >= this.lifetime) {
				this.remove();
				return;
			}
			int part = this.particle + this.age / Math.max(1, this.multiplier);
			float u = part % 4 / 16.0F;
			setUV(u, 0.25F, u + 0.0624375F, 0.25F + 0.0624375F);
			this.yd -= 0.04 * this.gravity;
			this.x += this.xd;
			this.y += this.yd;
			this.z += this.zd;
			if (this.slowdown) {
				this.xd *= 0.908;
				this.yd *= 0.908;
				this.zd *= 0.908;
			}
		}
	}

	public static class Bubble extends TCParticle {
		public int particle = 16;
		public double bubblespeed = 0.002;
		private float particleScale;

		public Bubble(ClientLevel level, double x, double y, double z, double mx, double my, double mz, int age) {
			super(level, x, y, z);
			setColor(1.0F, 0.0F, 0.5F);
			this.particleScale = defaultScale(this.random) * (this.random.nextFloat() * 0.3F + 0.2F);
			this.xd = mx * 0.2F + (float) (Math.random() * 2.0 - 1.0) * 0.02F;
			this.yd = my * 0.2F + (float) Math.random() * 0.02F;
			this.zd = mz * 0.2F + (float) (Math.random() * 2.0 - 1.0) * 0.02F;
			this.lifetime = (int) (age + 2 + 8.0 / (Math.random() * 0.8 + 0.2));
			if (tooFar(x, y, z)) {
				this.lifetime = 0;
			}
			this.additive = false;
			updateUV();
		}

		public void setFroth() {
			this.particleScale *= 0.75F;
			this.lifetime = 4 + this.random.nextInt(3);
			this.bubblespeed = -0.001;
			this.xd /= 5.0;
			this.yd /= 10.0;
			this.zd /= 5.0;
		}

		public void setFroth2() {
			this.particleScale *= 0.75F;
			this.lifetime = 12 + this.random.nextInt(12);
			this.bubblespeed = -0.005;
			this.xd /= 5.0;
			this.yd /= 10.0;
			this.zd /= 5.0;
		}

		private void updateUV() {
			float u = this.particle % 16 / 16.0F;
			float v = this.particle / 16 / 16.0F;
			setUV(u, v, u + 0.0624375F, v + 0.0624375F);
		}

		@Override
		public float getQuadSize(float a) {
			return 0.1F * this.particleScale;
		}

		@Override
		public void tick() {
			this.xo = this.x;
			this.yo = this.y;
			this.zo = this.z;
			this.yd += this.bubblespeed;
			if (this.bubblespeed > 0.0) {
				this.xd += (this.random.nextFloat() - this.random.nextFloat()) * 0.01F;
				this.zd += (this.random.nextFloat() - this.random.nextFloat()) * 0.01F;
			}
			this.x += this.xd;
			this.y += this.yd;
			this.z += this.zd;
			this.xd *= 0.85F;
			this.yd *= 0.85F;
			this.zd *= 0.85F;
			if (this.lifetime-- <= 0) {
				this.remove();
			} else if (this.lifetime <= 2) {
				this.particle++;
				updateUV();
			}
		}
	}

	/** FXGeneric: animates through a run of particles.png cells. */
	/**
	 * FXVent: a puff of steam from a crab vent. It starts small, swells by 15% a tick to its full size (frames 1-5 of particles.png as it
	 * grows), fades as it does, and drifts slowly up. Drawn with normal blending and the world's light.
	 */
	public static class Vent extends TCParticle {
		private float particleScale;
		private float psm = 1.0F;

		public Vent(ClientLevel level, double x, double y, double z, double mx, double my, double mz, int color) {
			super(level, x, y, z);
			this.additive = false;
			this.particleScale = this.random.nextFloat() * 0.1F + 0.05F;
			this.setColor((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F);
			// setHeading(motion, 0.125, 5): the given direction, wobbled a little, at a fixed speed
			double len = Math.sqrt(mx * mx + my * my + mz * mz);
			mx /= len;
			my /= len;
			mz /= len;
			mx += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * 0.0075 * 5.0;
			my += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * 0.0075 * 5.0;
			mz += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * 0.0075 * 5.0;
			this.xd = mx * 0.125;
			this.yd = my * 0.125;
			this.zd = mz * 0.125;
			this.lifetime = 200;
			if (tooFar(x, y, z)) {
				this.lifetime = 0;
			}
		}

		public Vent scaled(float f) {
			this.particleScale *= f;
			this.psm *= f;
			return this;
		}

		@Override
		public float getQuadSize(float a) {
			return 0.3F * this.particleScale;
		}

		@Override
		protected int getLightCoords(float a) {
			var pos = net.minecraft.core.BlockPos.containing(this.x, this.y, this.z);
			return this.level.hasChunkAt(pos) ? net.minecraft.util.LightCoordsUtil.getLightCoords(this.level, pos) : 0;
		}

		@Override
		public void tick() {
			this.xo = this.x;
			this.yo = this.y;
			this.zo = this.z;
			if (this.age++ >= this.lifetime || this.particleScale > this.psm) {
				this.remove();
				return;
			}
			this.yd += 0.0025;
			this.x += this.xd;
			this.y += this.yd;
			this.z += this.zd;
			this.xd *= 0.85;
			this.yd *= 0.85;
			this.zd *= 0.85;
			if (this.particleScale < this.psm) {
				this.particleScale *= 1.15F;
			}
			int part = (int) (1.0F + this.particleScale / this.psm * 4.0F);
			float u = part % 16 / 16.0F;
			float v = part / 16 / 16.0F;
			this.setUV(u, v, u + 0.0624375F, v + 0.0624375F);
			this.alpha = Math.max(0.0F, 0.4F * ((this.psm - this.particleScale) / this.psm));
		}
	}

	public static class Generic extends TCParticle {
		boolean loop;
		int delay;
		int startParticle;
		int numParticles = 1;
		int particleInc = 1;
		float particleScale;
		float baseAlpha;

		public Generic(ClientLevel level, double x, double y, double z, double xx, double yy, double zz) {
			super(level, x, y, z);
			this.xd = xx;
			this.yd = yy;
			this.zd = zz;
		}

		public Generic setup(float r, float g, float b, float alpha, boolean loop, int start, int num, int inc, int age, int delay, float scale) {
			setColor(r, g, b);
			this.baseAlpha = alpha;
			this.loop = loop;
			this.startParticle = start;
			this.numParticles = num;
			this.particleInc = inc;
			this.lifetime = age + delay;
			this.delay = delay;
			this.particleScale = scale;
			setIndex(start);
			return this;
		}

		private void setIndex(int idx) {
			float u = idx % 16 / 16.0F;
			float v = idx / 16 / 16.0F;
			setUV(u, v, u + 0.0624375F, v + 0.0624375F);
		}

		@Override
		public float getQuadSize(float a) {
			return this.age >= this.delay ? 0.1F * this.particleScale : 0.0F;
		}

		@Override
		public void tick() {
			this.xo = this.x;
			this.yo = this.y;
			this.zo = this.z;
			if (this.age++ >= this.lifetime) {
				this.remove();
				return;
			}
			this.x += this.xd;
			this.y += this.yd;
			this.z += this.zd;
			this.xd *= 0.98F;
			this.yd *= 0.98F;
			this.zd *= 0.98F;
			if (this.loop) {
				setIndex(this.startParticle + this.age / this.particleInc % this.numParticles);
			} else {
				float fs = (float) this.age / this.lifetime;
				setIndex((int) (this.startParticle + Math.min(this.numParticles * fs, (float) (this.numParticles - 1))));
			}
			this.alpha = this.age <= 1 || this.age >= this.lifetime - 1 ? this.baseAlpha / 2.0F : this.baseAlpha;
		}
	}

	static float clampF(float v) {
		return Mth.clamp(v, 0.0F, 1.0F);
	}
	/** FXBoreSparkle: a flickering spark that homes in on a target block (infusion sources). */
	public static class BoreSparkle extends TCParticle {
		private final double targetX, targetY, targetZ;
		private float particleScale;

		public BoreSparkle(ClientLevel level, double x, double y, double z, double tx, double ty, double tz) {
			super(level, x, y, z);
			this.particleScale = this.random.nextFloat() * 0.5F + 0.5F;
			this.targetX = tx;
			this.targetY = ty;
			this.targetZ = tz;
			double dx = tx - x, dy = ty - y, dz = tz - z;
			int base = Math.max(1, (int) (Math.sqrt(dx * dx + dy * dy + dz * dz) * 3.0));
			this.lifetime = base / 2 + this.random.nextInt(base);
			this.xd = this.random.nextGaussian() * 0.01;
			this.yd = this.random.nextGaussian() * 0.01;
			this.zd = this.random.nextGaussian() * 0.01;
			setColor(0.2F, 0.6F + this.random.nextFloat() * 0.3F, 0.2F);
			this.alpha = 0.75F;
			if (tooFar(x, y, z)) {
				this.lifetime = 0;
			}
			setUV(0.0F, 0.25F, 0.0624375F, 0.3124375F);
		}

		@Override
		public float getQuadSize(float a) {
			float bob = Mth.sin(this.age / 3.0F) * 0.5F + 1.0F;
			return 0.1F * this.particleScale * bob;
		}

		@Override
		public void tick() {
			this.xo = this.x;
			this.yo = this.y;
			this.zo = this.z;
			if (this.age++ >= this.lifetime || Mth.floor(this.x) == Mth.floor(this.targetX) && Mth.floor(this.y) == Mth.floor(this.targetY)
				&& Mth.floor(this.z) == Mth.floor(this.targetZ)) {
				this.remove();
				return;
			}
			float u = this.age % 4 / 16.0F;
			setUV(u, 0.25F, u + 0.0624375F, 0.3124375F);
			this.x += this.xd;
			this.y += this.yd;
			this.z += this.zd;
			this.xd *= 0.985;
			this.yd *= 0.985;
			this.zd *= 0.985;
			double dx = this.targetX - this.x, dy = this.targetY - this.y, dz = this.targetZ - this.z;
			double pull = 0.3;
			double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
			if (d < 4.0) {
				this.particleScale *= 0.9F;
				pull = 0.6;
			}
			this.xd = Mth.clamp(this.xd + dx / d * pull, -0.35, 0.35);
			this.yd = Mth.clamp(this.yd + dy / d * pull, -0.35, 0.35);
			this.zd = Mth.clamp(this.zd + dz / d * pull, -0.35, 0.35);
		}
	}

	/** FXBlockRunes: a rune glyph (particles.png row 14) that fades in and out flat against a face of a block. */
	public static class BlockRunes extends TCParticle {
		private final org.joml.Quaternionf orient;
		private final org.joml.Vector3f offset;
		private final float particleScale;

		public BlockRunes(ClientLevel level, double x, double y, double z, float r, float g, float b, int m) {
			super(level, x, y, z);
			setColor(r == 0.0F ? 1.0F : r, g, b);
			this.lifetime = 3 * m;
			int rune = (int) (Math.random() * 16.0 + 224.0);
			float u = rune % 16 / 16.0F;
			setUV(u, 0.375F, u + 0.0624375F, 0.4374375F);
			this.particleScale = (float) (1.0 + this.random.nextGaussian() * 0.1F);
			float rotation = this.random.nextInt(4) * 90;
			// glRotatef(rotation, Y) then glRotatef(90, Z), then out to the face
			this.orient = new org.joml.Quaternionf().rotateY((float) Math.toRadians(rotation)).rotateZ((float) Math.toRadians(90.0));
			this.offset = this.orient.transform(new org.joml.Vector3f(this.random.nextFloat() * 0.2F, -0.3F + this.random.nextFloat() * 0.6F, -0.51F));
			this.alpha = 0.0F;
		}

		public void setGravityTC(float g) {
			this.gravity = g;
		}

		@Override
		public float getQuadSize(float a) {
			return 0.15F * this.particleScale;
		}

		@Override
		public void extract(net.minecraft.client.renderer.state.level.QuadParticleRenderState state, net.minecraft.client.Camera camera, float a) {
			var cam = camera.position();
			float px = (float) (Mth.lerp(a, this.xo, this.x) - cam.x() + this.offset.x());
			float py = (float) (Mth.lerp(a, this.yo, this.y) - cam.y() + this.offset.y());
			float pz = (float) (Mth.lerp(a, this.zo, this.z) - cam.z() + this.offset.z());
			this.extractRotatedQuad(state, this.orient, px, py, pz, a);
		}

		@Override
		public void tick() {
			this.xo = this.x;
			this.yo = this.y;
			this.zo = this.z;
			float threshold = this.lifetime / 5.0F;
			this.alpha = (this.age <= threshold ? this.age / threshold : (float) (this.lifetime - this.age) / this.lifetime) / 2.0F;
			if (this.age++ >= this.lifetime) {
				this.remove();
				return;
			}
			this.yd -= 0.04 * this.gravity;
			this.x += this.xd;
			this.y += this.yd;
			this.z += this.zd;
		}
	}

	/** FXSmokeSpiral: a puff of smoke wound twice round a sphere of the given radius, never sinking below miny. */
	public static class SmokeSpiral extends TCParticle {
		private final double cx, cy, cz;
		private final float radius;
		private final int start;
		private final int miny;

		public SmokeSpiral(ClientLevel level, double x, double y, double z, float radius, int start, int miny) {
			super(level, x, y, z);
			this.cx = x;
			this.cy = y;
			this.cz = z;
			this.radius = radius;
			this.start = start;
			this.miny = miny;
			this.lifetime = 20 + level.getRandom().nextInt(10);
			this.additive = false;
			this.place(0.0F);
		}

		private void place(float pt) {
			float f = (this.age + pt) / this.lifetime;
			float r1 = this.start + 720.0F * f;
			float r2 = 90.0F - 180.0F * f;
			float mX = -Mth.sin(r1 / 180.0F * Mth.PI) * Mth.cos(r2 / 180.0F * Mth.PI) * this.radius;
			float mZ = Mth.cos(r1 / 180.0F * Mth.PI) * Mth.cos(r2 / 180.0F * Mth.PI) * this.radius;
			float mY = -Mth.sin(r2 / 180.0F * Mth.PI) * this.radius;
			this.setPos(this.cx + mX, Math.max(this.cy + mY, this.miny + 0.1F), this.cz + mZ);
			int particle = (int) (1.0F + (float) this.age / this.lifetime * 4.0F);
			float u = particle % 16 / 16.0F;
			float v = particle / 16 / 16.0F;
			setUV(u, v, u + 0.0624375F, v + 0.0624375F);
		}

		@Override
		public float getQuadSize(float a) {
			return 0.15F;
		}

		@Override
		public void tick() {
			this.xo = this.x;
			this.yo = this.y;
			this.zo = this.z;
			this.alpha = 0.66F * (this.lifetime - this.age) / (float) this.lifetime;
			if (this.age++ >= this.lifetime) {
				this.remove();
				return;
			}
			this.place(0.0F);
		}
	}

	/** FXBurst: a flash from the last row of nodes.png, played when a wisp appears or dies. */
	public static class Burst extends TCParticle {
		private static final Layer LAYER = new Layer(true, dev.neothaum.client.render.TVRenderTypes.NODES,
			dev.neothaum.client.render.TVRenderTypes.PARTICLE_ADDITIVE);
		private final float scale;

		public Burst(ClientLevel level, double x, double y, double z, float f) {
			super(level, x, y, z);
			this.xd = this.yd = this.zd = 0.0;
			this.scale = defaultScale(this.random) * f;
			this.lifetime = 31;
			this.alpha = 0.75F;
			this.setSize(0.01F, 0.01F);
			setFrame();
		}

		private void setFrame() {
			float u = this.age % 32 / 32.0F;
			setUV(u, 0.96875F, u + 0.03125F, 1.0F);
		}

		@Override
		protected Layer getLayer() {
			return LAYER;
		}

		@Override
		public float getQuadSize(float a) {
			return this.scale;
		}

		@Override
		public void tick() {
			this.xo = this.x;
			this.yo = this.y;
			this.zo = this.z;
			if (this.age++ >= this.lifetime) {
				this.remove();
				return;
			}
			setFrame();
		}
	}

	/** FXDrop: a coloured drip (vanilla's drip sprites) that hangs, falls and splashes. */
	public static class Drop extends net.minecraft.client.particle.SingleQuadParticle {
		private int bobTimer = 40;

		private static net.minecraft.client.renderer.texture.TextureAtlasSprite sprite(String name) {
			return net.minecraft.client.Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(net.minecraft.data.AtlasIds.PARTICLES)
				.getSprite(net.minecraft.resources.Identifier.withDefaultNamespace(name));
		}

		public Drop(ClientLevel level, double x, double y, double z, float r, float g, float b) {
			super(level, x, y, z, sprite("drip_hang"));
			this.xd = this.yd = this.zd = 0.0;
			this.rCol = r;
			this.gCol = g;
			this.bCol = b;
			this.gravity = 0.06F;
			this.lifetime = (int) (64.0 / (Math.random() * 0.8 + 0.2));
		}

		@Override
		protected Layer getLayer() {
			return Layer.OPAQUE;
		}

		@Override
		protected int getLightCoords(float a) {
			return 257;
		}

		@Override
		public void tick() {
			this.xo = this.x;
			this.yo = this.y;
			this.zo = this.z;
			this.yd -= this.gravity;
			if (this.bobTimer-- > 0) {
				this.xd *= 0.02;
				this.yd *= 0.02;
				this.zd *= 0.02;
			} else if (this.bobTimer == -1) {
				this.setSprite(sprite("drip_fall"));
			}
			this.move(this.xd, this.yd, this.zd);
			this.xd *= 0.98F;
			this.yd *= 0.98F;
			this.zd *= 0.98F;
			if (this.lifetime-- <= 0) {
				this.remove();
			}
			if (this.onGround) {
				this.setSprite(sprite("drip_land"));
				this.xd *= 0.7F;
				this.zd *= 0.7F;
			}
			var pos = net.minecraft.core.BlockPos.containing(this.x, this.y, this.z);
			var state = this.level.getBlockState(pos);
			if (!state.getFluidState().isEmpty() && this.y < pos.getY() + state.getFluidState().getHeight(this.level, pos)) {
				this.remove();
			}
		}
	}

	/** FXSwarm: a little biting mote from row five of particles.png that wheels about its swarm, scattering when it is hurt. */
	public static class Swarm extends TCParticle {
		private static final java.util.List<Long> BUZZCOUNT = new java.util.ArrayList<>();
		private final net.minecraft.world.entity.Entity target;
		private final float turnSpeed;
		private final float speed;
		private final float scale;
		private final float baseG, baseB;
		private int deathtimer;
		private float yaw, pitch;

		public Swarm(ClientLevel level, double x, double y, double z, net.minecraft.world.entity.Entity target, float r, float g, float b, float speed,
			float turnSpeed, float gravity) {
			super(level, x, y, z);
			this.additive = false;
			this.hasPhysics = true;
			this.rCol = r;
			this.gCol = g;
			this.bCol = b;
			this.baseG = g;
			this.baseB = b;
			this.scale = this.random.nextFloat() * 0.5F + 1.0F;
			this.target = target;
			float f3 = 0.2F;
			this.xd = (this.random.nextFloat() - this.random.nextFloat()) * f3;
			this.yd = (this.random.nextFloat() - this.random.nextFloat()) * f3;
			this.zd = (this.random.nextFloat() - this.random.nextFloat()) * f3;
			this.speed = speed;
			this.turnSpeed = turnSpeed;
			this.gravity = gravity;
			this.lifetime = Integer.MAX_VALUE;
			this.alpha = 0.75F;
			if (tooFar(x, y, z)) {
				this.lifetime = 0;
			}
			setFrame();
		}

		private void setFrame() {
			int part = 7 + this.age % 8;
			float u = part / 16.0F;
			setUV(u, 0.25F, u + 0.0624375F, 0.25F + 0.0624375F);
		}

		@Override
		public float getQuadSize(float a) {
			float bob = Mth.sin(this.age / 3.0F) * 0.25F + 1.0F;
			return 0.1F * this.scale * bob;
		}

		private boolean targetHurt() {
			return this.target instanceof net.minecraft.world.entity.LivingEntity le && le.hurtTime > 0;
		}

		@Override
		public void tick() {
			this.xo = this.x;
			this.yo = this.y;
			this.zo = this.z;
			this.age++;
			if (this.age > this.lifetime) {
				this.remove();
				return;
			}
			boolean gone = this.target == null || !this.target.isAlive()
				|| this.target instanceof net.minecraft.world.entity.LivingEntity le && le.deathTime > 0;
			if (gone) {
				this.deathtimer++;
				this.yd -= this.gravity / 2.0F;
				if (this.deathtimer > 50) {
					this.remove();
					return;
				}
			} else {
				this.yd += this.gravity;
			}
			this.alpha = (50.0F - this.deathtimer) / 50.0F * 0.75F;
			boolean hurt = targetHurt();
			this.gCol = hurt ? this.baseG / 2.0F : this.baseG;
			this.bCol = hurt ? this.baseB / 2.0F : this.baseB;
			this.move(this.xd, this.yd, this.zd);
			this.xd *= 0.985;
			this.yd *= 0.985;
			this.zd *= 0.985;
			if (!gone) {
				double dx = this.target.getX() - this.x;
				double dz = this.target.getZ() - this.z;
				double dy = this.target.getBoundingBox().getCenter().y - this.y;
				double d3 = Math.sqrt(dx * dx + dz * dz);
				float f2 = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
				float f3 = (float) -(Math.atan2(dy, d3) * 180.0 / Math.PI);
				int half = Math.max(1, (int) (this.turnSpeed / 2.0F));
				float step = this.turnSpeed / 2.0F + this.random.nextInt(half);
				double distSq = dx * dx + dy * dy + dz * dz;
				if (distSq > this.target.getBbWidth() && !hurt) {
					this.pitch = rotate(this.pitch, f3, step);
					this.yaw = rotate(this.yaw, f2, step);
				} else {
					this.pitch = rotate(this.pitch, f3, -step);
					this.yaw = rotate(this.yaw, f2, -step);
				}
				double mx = -Mth.sin(this.yaw / 180.0F * (float) Math.PI) * Mth.cos(this.pitch / 180.0F * (float) Math.PI);
				double mz = Mth.cos(this.yaw / 180.0F * (float) Math.PI) * Mth.cos(this.pitch / 180.0F * (float) Math.PI);
				double my = -Mth.sin(this.pitch / 180.0F * (float) Math.PI);
				double len = Math.sqrt(mx * mx + my * my + mz * mz);
				mx = mx / len + this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * 0.0075F * 15.0F;
				my = my / len + this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * 0.0075F * 15.0F;
				mz = mz / len + this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * 0.0075F * 15.0F;
				this.xd = mx * this.speed;
				this.yd = my * this.speed;
				this.zd = mz * this.speed;
			}
			setFrame();
			var mc = net.minecraft.client.Minecraft.getInstance();
			if (BUZZCOUNT.size() < 3 && this.random.nextInt(50) == 0 && mc.player != null && mc.player.distanceToSqr(this.x, this.y, this.z) < 64.0) {
				this.level.playLocalSound(this.x, this.y, this.z, dev.neothaum.registry.TVSounds.FLY, net.minecraft.sounds.SoundSource.HOSTILE, 0.03F,
					0.5F + this.random.nextFloat() * 0.4F, false);
				BUZZCOUNT.add(System.nanoTime() + 1500000L);
			}
			if (BUZZCOUNT.size() >= 3 && BUZZCOUNT.get(0) < System.nanoTime()) {
				BUZZCOUNT.remove(0);
			}
		}

		private static float rotate(float from, float to, float max) {
			float f3 = Mth.wrapDegrees(to - from);
			if (f3 > max) {
				f3 = max;
			}
			if (f3 < -max) {
				f3 = -max;
			}
			return from + f3;
		}
	}

	/** FXBreaking with a slime ball: a tinted, translucent gobbet that falls and fades. */
	public static class Slime extends net.minecraft.client.particle.SingleQuadParticle {
		private final float uo, vo;

		public Slime(ClientLevel level, double x, double y, double z) {
			super(level, x, y, z, 0.0, 0.0, 0.0, net.minecraft.client.Minecraft.getInstance().getAtlasManager()
				.getAtlasOrThrow(net.minecraft.data.AtlasIds.ITEMS).getSprite(net.minecraft.resources.Identifier.withDefaultNamespace("item/slime_ball")));
			this.gravity = 1.0F;
			this.quadSize /= 2.0F;
			this.uo = this.random.nextFloat() * 3.0F;
			this.vo = this.random.nextFloat() * 3.0F;
		}

		public void tint(float r, float g, float b, float a) {
			this.rCol = r;
			this.gCol = g;
			this.bCol = b;
			this.alpha = a;
		}

		@Override
		protected Layer getLayer() {
			return Layer.TRANSLUCENT_ITEMS;
		}

		@Override
		protected float getU0() {
			return this.sprite.getU((this.uo + 1.0F) / 4.0F);
		}

		@Override
		protected float getU1() {
			return this.sprite.getU(this.uo / 4.0F);
		}

		@Override
		protected float getV0() {
			return this.sprite.getV(this.vo / 4.0F);
		}

		@Override
		protected float getV1() {
			return this.sprite.getV((this.vo + 1.0F) / 4.0F);
		}
	}

	/** FXSpark: a seven-frame electric crackle from particles2.png, layer 2 (additive). */
	public static class Spark extends TCParticle {
		private final int particle;
		private final boolean flip;
		private final float scale;

		public Spark(ClientLevel level, double x, double y, double z, float scale) {
			super(level, x, y, z);
			this.secondSheet = true;
			this.scale = scale;
			this.xd = this.yd = this.zd = 0.0;
			this.lifetime = 5 + level.getRandom().nextInt(5);
			this.particle = level.getRandom().nextInt(3) * 8;
			this.flip = level.getRandom().nextBoolean();
			frame();
		}

		public void alpha(float a) {
			this.setAlpha(a);
		}

		private void frame() {
			int part = this.particle + (int) ((float) this.age / this.lifetime * 7.0F);
			float u0 = part % 8 / 8.0F;
			float u1 = u0 + 0.125F;
			float v0 = part / 8 / 8.0F;
			float v1 = v0 + 0.125F;
			if (this.flip) {
				setUV(u1, v0, u0, v1);
			} else {
				setUV(u0, v0, u1, v1);
			}
		}

		@Override
		public float getQuadSize(float a) {
			return this.scale;
		}

		@Override
		public void tick() {
			this.xo = this.x;
			this.yo = this.y;
			this.zo = this.z;
			if (this.age++ >= this.lifetime) {
				this.remove();
				return;
			}
			frame();
		}
	}

	/** FXVisSparkle: a mote that grows, drifts and homes in on its target, cycling through the sparkle frames of particles.png. */
	public static class VisSparkle extends TCParticle {
		private final double tx;
		private final double ty;
		private final double tz;
		private final float sizeMod;
		private float scale;

		public VisSparkle(ClientLevel level, double x, double y, double z, double tx, double ty, double tz) {
			super(level, x, y, z);
			this.tx = tx;
			this.ty = ty;
			this.tz = tz;
			this.lifetime = 1000;
			var r = level.getRandom();
			this.xd = r.nextGaussian() * 0.01F;
			this.yd = r.nextGaussian() * 0.01F;
			this.zd = r.nextGaussian() * 0.01F;
			this.sizeMod = 45 + r.nextInt(15);
			setColor(0.2F, 0.6F + r.nextFloat() * 0.3F, 0.2F);
			this.alpha = 0.5F;
			if (tooFar(x, y, z)) {
				this.lifetime = 0;
			}
		}

		public void tint(float r, float g, float b) {
			setColor(r, g, b);
		}

		@Override
		public float getQuadSize(float a) {
			float bob = Mth.sin(this.age / 3.0F) * 0.3F + 6.0F;
			return 0.1F * this.scale * bob;
		}

		@Override
		public void tick() {
			this.xo = this.x;
			this.yo = this.y;
			this.zo = this.z;
			if (this.age++ >= this.lifetime) {
				this.remove();
				return;
			}
			this.x += this.xd;
			this.y += this.yd;
			this.z += this.zd;
			this.xd *= 0.985;
			this.yd *= 0.985;
			this.zd *= 0.985;
			double dx = this.tx - this.x;
			double dy = this.ty - this.y;
			double dz = this.tz - this.z;
			double d11 = Math.sqrt(dx * dx + dy * dy + dz * dz);
			if (d11 < 2.0) {
				this.scale *= 0.95F;
			}
			if (d11 < 0.2) {
				this.lifetime = this.age;
			}
			if (this.age < 10) {
				this.scale = this.age / this.sizeMod;
			}
			this.xd = Mth.clamp(this.xd + dx / d11 * 0.1, -0.1, 0.1);
			this.yd = Mth.clamp(this.yd + dy / d11 * 0.1, -0.1, 0.1);
			this.zd = Mth.clamp(this.zd + dz / d11 * 0.1, -0.1, 0.1);
			float u0 = this.age % 16 / 16.0F;
			setUV(u0, 0.5F, u0 + 0.0624375F, 0.5624375F);
		}
	}
}
