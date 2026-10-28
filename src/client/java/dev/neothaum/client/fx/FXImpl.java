package dev.neothaum.client.fx;

import dev.neothaum.fx.FX;
import dev.neothaum.tiles.CrucibleBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** Client side of {@link FX}: ClientProxy's particle helpers. */
public final class FXImpl implements FX.Impl {
	private static void add(Particle p) {
		Minecraft.getInstance().particleEngine.add(p);
	}

	@Override
	public void wisp(Level level, double x, double y, double z, float scale, float r, float g, float b, boolean tinkle, boolean shrink, float gravity) {
		TCParticles.Wisp w = new TCParticles.Wisp((ClientLevel) level, x, y, z, scale, r, g, b);
		w.tinkle = tinkle;
		w.shrink = shrink;
		w.setGravityTC(gravity);
		add(w);
	}

	@Override
	public void wispTo(Level level, double x, double y, double z, double tx, double ty, double tz, float scale, int type, boolean shrink, float gravity) {
		TCParticles.Wisp w = new TCParticles.Wisp((ClientLevel) level, x, y, z, tx, ty, tz, scale, type);
		w.shrink = shrink;
		w.setGravityTC(gravity);
		add(w);
	}

	@Override
	public void sparkle(Level level, double x, double y, double z, float scale, int type, float gravity) {
		if (level.getRandom().nextInt(6) < 2) {
			TCParticles.Sparkle s = new TCParticles.Sparkle((ClientLevel) level, x, y, z, scale, type, 6);
			s.setGravityTC(gravity);
			add(s);
		}
	}

	@Override
	public void blockSparkle(Level level, BlockPos pos, int c) {
		var rand = level.getRandom();
		float r = (c >> 16 & 255) / 255.0F;
		float g = (c >> 8 & 255) / 255.0F;
		float b = (c & 255) / 255.0F;
		for (int a = 0; a < 5; a++) {
			if (c == -9999) {
				r = 0.33F + rand.nextFloat() * 0.67F;
				g = 0.33F + rand.nextFloat() * 0.67F;
				b = 0.33F + rand.nextFloat() * 0.67F;
			}
			add(new TCParticles.Generic((ClientLevel) level, pos.getX() - 0.1F + rand.nextFloat() * 1.2F, pos.getY() - 0.1F + rand.nextFloat() * 1.2F,
				pos.getZ() - 0.1F + rand.nextFloat() * 1.2F, 0.0, rand.nextFloat() * 0.02, 0.0)
				.setup(r - 0.2F + rand.nextFloat() * 0.4F, g - 0.2F + rand.nextFloat() * 0.4F, b - 0.2F + rand.nextFloat() * 0.4F, 0.9F, false, 112, 9, 1,
					5 + rand.nextInt(8), rand.nextInt(10), 0.7F + rand.nextFloat() * 0.4F));
		}
	}

	@Override
	public void crucibleBubble(Level level, double x, double y, double z, float r, float g, float b) {
		TCParticles.Bubble fb = new TCParticles.Bubble((ClientLevel) level, x, y, z, 0.0, 0.0, 0.0, 1);
		fb.setColor(r, g, b);
		add(fb);
	}

	@Override
	public void crucibleFroth(Level level, float x, float y, float z) {
		TCParticles.Bubble fb = new TCParticles.Bubble((ClientLevel) level, x, y, z, 0.0, 0.0, 0.0, -4);
		fb.setColor(0.5F, 0.5F, 0.7F);
		fb.setFroth();
		add(fb);
	}

	@Override
	public void crucibleFrothDown(Level level, float x, float y, float z) {
		TCParticles.Bubble fb = new TCParticles.Bubble((ClientLevel) level, x, y, z, 0.0, 0.0, 0.0, -4);
		fb.setColor(0.5F, 0.5F, 0.7F);
		fb.setFroth2();
		add(fb);
	}

	@Override
	public void crucibleBoil(Level level, BlockPos pos, float strength) {
		if (!(level.getBlockEntity(pos) instanceof CrucibleBlockEntity tile)) {
			return;
		}
		var rand = level.getRandom();
		for (int q = 0; q < 10; q++) {
			TCParticles.Bubble fb = new TCParticles.Bubble((ClientLevel) level, pos.getX() + 0.2F + rand.nextFloat() * 0.6F,
				pos.getY() + 0.1F + tile.getFluidHeight(), pos.getZ() + 0.2F + rand.nextFloat() * 0.6F, 0.0, 0.0, 0.0, 3);
			if (tile.aspects.size() == 0) {
				fb.setColor(1.0F, 1.0F, 1.0F);
			} else {
				int color = tile.aspects.getAspects()[rand.nextInt(tile.aspects.size())].getColor();
				fb.setColor((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F);
			}
			fb.bubblespeed = 0.003 * strength;
			add(fb);
		}
	}

	@Override
	public void essentiaTrail(Level level, double x, double y, double z, double tx, double ty, double tz, int count, int color, float scale) {
		for (int i = 0; i < count; i++) {
			TCParticles.Wisp w = new TCParticles.Wisp((ClientLevel) level, x, y, z, tx, ty, tz, scale, 0);
			w.setColor((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F);
			add(w);
		}
	}

	@Override
	public void visBeam(Level level, double x, double y, double z, double tx, double ty, double tz, int color, float width) {
		essentiaTrail(level, x, y, z, tx, ty, tz, 3, color, width);
	}

	@Override
	public void infusionItem(Level level, double x, double y, double z, BlockPos t, net.minecraft.world.item.ItemStack stack, boolean drift) {
		BoreParticle fb = new BoreParticle((ClientLevel) level, x, y, z, t.getX() + 0.5, t.getY() - 0.5, t.getZ() + 0.5, stack).alpha(0.3F);
		if (drift) {
			var r = level.getRandom();
			fb.motion(r.nextGaussian() * 0.03, r.nextGaussian() * 0.03, r.nextGaussian() * 0.03);
		}
		add(fb);
	}

	@Override
	public void infusionSparkle(Level level, double x, double y, double z, BlockPos t, float r, float g, float b) {
		TCParticles.BoreSparkle fb = new TCParticles.BoreSparkle((ClientLevel) level, x, y, z, t.getX() + 0.5, t.getY() - 0.5, t.getZ() + 0.5);
		fb.setColor(r, g, b);
		add(fb);
	}

	@Override
	public void blockRunes(Level level, double x, double y, double z, float r, float g, float b, int dur, float grav) {
		TCParticles.BlockRunes fb = new TCParticles.BlockRunes((ClientLevel) level, x + 0.5, y + 0.5, z + 0.5, r, g, b, dur);
		fb.setGravityTC(grav);
		add(fb);
	}

	@Override
	public void smokeSpiral(Level level, double x, double y, double z, float rad, int start, int miny, int color) {
		TCParticles.SmokeSpiral fx = new TCParticles.SmokeSpiral((ClientLevel) level, x, y, z, rad, start, miny);
		fx.setColor((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F);
		add(fx);
	}

	@Override
	public void droplet(Level level, double x, double y, double z, float r, float g, float b) {
		add(new TCParticles.Drop((ClientLevel) level, x, y, z, r, g, b));
	}

	@Override
	public Object swarm(net.minecraft.world.entity.Entity target, float speed, float turnSpeed, float gravity) {
		var level = (ClientLevel) target.level();
		var r = level.getRandom();
		TCParticles.Swarm fx = new TCParticles.Swarm(level, target.getX() + (r.nextFloat() - r.nextFloat()) * 2.0F,
			target.getY() + (r.nextFloat() - r.nextFloat()) * 2.0F, target.getZ() + (r.nextFloat() - r.nextFloat()) * 2.0F, target,
			0.8F + r.nextFloat() * 0.2F, r.nextFloat() * 0.4F, 1.0F - r.nextFloat() * 0.2F, speed, turnSpeed, gravity);
		add(fx);
		return fx;
	}

	@Override
	public boolean isAlive(Object particle) {
		return particle instanceof Particle p && p.isAlive();
	}

	@Override
	public void kill(Object particle) {
		if (particle instanceof Particle p) {
			p.remove();
		}
	}

	@Override
	public void sploosh(net.minecraft.world.entity.Entity e) {
		var level = (ClientLevel) e.level();
		var r = level.getRandom();
		float f = r.nextFloat() * (float) Math.PI * 2.0F;
		float f1 = r.nextFloat() * 0.5F + 0.5F;
		float f2 = net.minecraft.util.Mth.sin(f) * 2.0F * 0.5F * f1;
		float f3 = net.minecraft.util.Mth.cos(f) * 2.0F * 0.5F * f1;
		TCParticles.Slime fx = new TCParticles.Slime(level, e.getX() + f2, e.getY() + r.nextFloat() * e.getBbHeight(), e.getZ() + f3);
		if (r.nextBoolean()) {
			fx.tint(0.6F, 0.0F, 0.3F, 0.4F);
		} else {
			fx.tint(0.3F, 0.0F, 0.3F, 0.6F);
		}
		fx.setLifetime((int) (66.0F / (r.nextFloat() * 0.9F + 0.1F)));
		add(fx);
	}

	@Override
	public void slimeJump(net.minecraft.world.entity.Entity e, int i) {
		var level = (ClientLevel) e.level();
		var r = level.getRandom();
		float f = r.nextFloat() * (float) Math.PI * 2.0F;
		float f1 = r.nextFloat() * 0.5F + 0.5F;
		float f2 = net.minecraft.util.Mth.sin(f) * i * 0.5F * f1;
		float f3 = net.minecraft.util.Mth.cos(f) * i * 0.5F * f1;
		TCParticles.Slime fx = new TCParticles.Slime(level, e.getX() + f2, e.getBoundingBox().getCenter().y, e.getZ() + f3);
		fx.tint(0.7F, 0.0F, 1.0F, 0.4F);
		fx.setLifetime((int) (66.0F / (r.nextFloat() * 0.9F + 0.1F)));
		add(fx);
	}

	@Override
	public void tentacleArise(net.minecraft.world.entity.Entity e) {
		var level = (ClientLevel) e.level();
		var r = level.getRandom();
		float h = e.getBbHeight();
		for (int j = 0; j < 2.0F * h; j++) {
			float f = r.nextFloat() * (float) Math.PI * h;
			float f1 = r.nextFloat() * 0.5F + 0.5F;
			float f2 = net.minecraft.util.Mth.sin(f) * h * 0.25F * f1;
			float f3 = net.minecraft.util.Mth.cos(f) * h * 0.25F * f1;
			TCParticles.Slime fx = new TCParticles.Slime(level, e.getX() + f2, e.getY(), e.getZ() + f3);
			fx.tint(0.4F, 0.0F, 0.4F, 0.5F);
			fx.setLifetime((int) (66.0F / (r.nextFloat() * 0.9F + 0.1F)));
			add(fx);
			// and crumbs of the ground it is breaking through
			var below = net.minecraft.core.BlockPos.containing(e.getX(), e.getY() - 1, e.getZ());
			var state = level.getBlockState(below);
			if (!state.isAir()) {
				f = r.nextFloat() * (float) Math.PI * h;
				f1 = r.nextFloat() * 0.5F + 0.5F;
				level.addParticle(new net.minecraft.core.particles.BlockParticleOption(net.minecraft.core.particles.ParticleTypes.BLOCK, state),
					e.getX() + net.minecraft.util.Mth.sin(f) * h * 0.25F * f1, e.getY(), e.getZ() + net.minecraft.util.Mth.cos(f) * h * 0.25F * f1, 0, 0, 0);
			}
		}
	}

	@Override
	public void taintsplosion(net.minecraft.world.entity.Entity e) {
		var level = (ClientLevel) e.level();
		var r = level.getRandom();
		TCParticles.Slime fx = new TCParticles.Slime(level, e.getX(), e.getY() + r.nextFloat() * e.getBbHeight(), e.getZ());
		if (r.nextBoolean()) {
			fx.tint(0.6F, 0.0F, 0.3F, 0.4F);
		} else {
			fx.tint(0.3F, 0.0F, 0.3F, 0.6F);
		}
		double mx = Math.random() * 2.0 - 1.0, my = Math.random() * 2.0 - 1.0, mz = Math.random() * 2.0 - 1.0;
		double f = (Math.random() + Math.random() + 1.0) * 0.15F;
		double len = Math.sqrt(mx * mx + my * my + mz * mz);
		fx.setParticleSpeed(mx / len * f * 0.964, my / len * f * 0.964 + 0.1F, mz / len * f * 0.964);
		fx.setLifetime((int) (66.0F / (r.nextFloat() * 0.9F + 0.1F)));
		add(fx);
	}

	@Override
	public void spark(Level level, double x, double y, double z, float scale, float r, float g, float b, float a) {
		TCParticles.Spark s = new TCParticles.Spark((ClientLevel) level, x, y, z, scale);
		s.setColor(r, g, b);
		s.alpha(a);
		add(s);
	}

	@Override
	public void vent(Level level, double x, double y, double z, double mx, double my, double mz, int color, float scale) {
		TCParticles.Vent v = new TCParticles.Vent((ClientLevel) level, x, y, z, mx, my, mz, color).scaled(scale);
		add(v);
	}

	@Override
	public void generic(Level level, double x, double y, double z, double mx, double my, double mz, float r, float g, float b, float alpha,
		boolean loop, int start, int num, int inc, int age, int delay, float scale) {
		add(new TCParticles.Generic((ClientLevel) level, x, y, z, mx, my, mz).setup(r, g, b, alpha, loop, start, num, inc, age, delay, scale));
	}

	@Override
	public Object beamCont(net.minecraft.world.entity.player.Player p, double tx, double ty, double tz, int type, int color, boolean reverse,
		float endmod, Object input, int impact) {
		return BeamFX.beamCont(p, tx, ty, tz, type, color, reverse, endmod, input, impact);
	}

	@Override
	public void shockBolt(net.minecraft.world.entity.player.Player p, double tx, double ty, double tz) {
		var mc = net.minecraft.client.Minecraft.getInstance();
		double px = p.getX();
		double py = p.getY() + p.getEyeHeight();
		double pz = p.getZ();
		if (mc.getCameraEntity() == null || p.getId() != mc.getCameraEntity().getId()) {
			py = p.getBoundingBox().minY + p.getBbHeight() / 2.0F + 0.25;
		}
		px += -net.minecraft.util.Mth.cos(p.getYRot() / 180.0F * (float) Math.PI) * 0.06F;
		py += -0.06F;
		pz += -net.minecraft.util.Mth.sin(p.getYRot() / 180.0F * (float) Math.PI) * 0.06F;
		var look = p.getViewVector(1.0F);
		px += look.x * 0.3;
		py += look.y * 0.3;
		pz += look.z * 0.3;
		BoltFX.bolt(px, py, pz, tx, ty, tz, 6, 0.5F, 8, 2, 0.125F);
	}

	@Override
	public void excavate(BlockPos pos, net.minecraft.world.entity.player.Player p, int progress) {
		var mc = net.minecraft.client.Minecraft.getInstance();
		if (mc.level != null) {
			mc.level.destroyBlockProgress(p.getId(), pos, Math.min(9, progress));
		}
	}

	@Override
	public void nodeBolt(Level level, double x, double y, double z, double x2, double y2, double z2) {
		BoltFX.bolt(x, y, z, x2, y2, z2, 10, 4.0F, 5, 0, 0.03F);
	}

	@Override
	public void burst(Level level, double x, double y, double z, float size) {
		add(new TCParticles.Burst((ClientLevel) level, x, y, z, size));
	}
}
