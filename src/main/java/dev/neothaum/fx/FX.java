package dev.neothaum.fx;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Bridge to Thaumcraft's client particle engine (ParticleEngine / FXWisp / FXSparkle ...).
 * Common code calls these; the client installs a real implementation, the server keeps the no-op.
 */
public final class FX {
	public interface Impl {
		void wisp(Level level, double x, double y, double z, float scale, float r, float g, float b, boolean tinkle, boolean shrink, float gravity);

		void vent(Level level, double x, double y, double z, double mx, double my, double mz, int color, float scale);

		void wispTo(Level level, double x, double y, double z, double tx, double ty, double tz, float scale, int type, boolean shrink, float gravity);

		void sparkle(Level level, double x, double y, double z, float scale, int type, float gravity);

		void blockSparkle(Level level, BlockPos pos, int color);

		void crucibleBubble(Level level, double x, double y, double z, float r, float g, float b);

		void crucibleFroth(Level level, float x, float y, float z);

		void crucibleFrothDown(Level level, float x, float y, float z);

		void crucibleBoil(Level level, BlockPos pos, float height);

		void essentiaTrail(Level level, double x, double y, double z, double tx, double ty, double tz, int count, int color, float scale);

		void visBeam(Level level, double x, double y, double z, double tx, double ty, double tz, int color, float width);

		void infusionItem(Level level, double x, double y, double z, BlockPos target, ItemStack stack, boolean drift);

		void infusionSparkle(Level level, double x, double y, double z, BlockPos target, float r, float g, float b);

		void blockRunes(Level level, double x, double y, double z, float r, float g, float b, int dur, float grav);

		void smokeSpiral(Level level, double x, double y, double z, float rad, int start, int miny, int color);

		void droplet(Level level, double x, double y, double z, float r, float g, float b);

		Object swarm(net.minecraft.world.entity.Entity target, float speed, float turnSpeed, float gravity);

		boolean isAlive(Object particle);

		void kill(Object particle);

		void sploosh(net.minecraft.world.entity.Entity e);

		void slimeJump(net.minecraft.world.entity.Entity e, int size);

		void tentacleArise(net.minecraft.world.entity.Entity e);

		void taintsplosion(net.minecraft.world.entity.Entity e);

		void spark(Level level, double x, double y, double z, float scale, float r, float g, float b, float a);

		void generic(Level level, double x, double y, double z, double mx, double my, double mz, float r, float g, float b, float alpha, boolean loop,
			int start, int num, int inc, int age, int delay, float scale);

		Object beamCont(net.minecraft.world.entity.player.Player p, double tx, double ty, double tz, int type, int color, boolean reverse, float endmod,
			Object input, int impact);

		void shockBolt(net.minecraft.world.entity.player.Player p, double tx, double ty, double tz);

		void excavate(BlockPos pos, net.minecraft.world.entity.player.Player p, int progress);

		void nodeBolt(Level level, double x, double y, double z, double x2, double y2, double z2);

		void burst(Level level, double x, double y, double z, float size);
	}

	private static Impl impl;

	public static void install(Impl i) {
		impl = i;
	}

	/** ClientProxy.drawVentParticles: a puff of steam blown along (mx, my, mz). */
	public static void vent(Level level, double x, double y, double z, double mx, double my, double mz, int color, float scale) {
		if (impl != null && level.isClientSide()) {
			impl.vent(level, x, y, z, mx, my, mz, color, scale);
		}
	}

	public static void wisp(Level level, double x, double y, double z, float scale, float r, float g, float b, boolean tinkle, boolean shrink, float gravity) {
		if (impl != null && level.isClientSide()) {
			impl.wisp(level, x, y, z, scale, r, g, b, tinkle, shrink, gravity);
		}
	}

	public static void wispTo(Level level, double x, double y, double z, double tx, double ty, double tz, float scale, int type, boolean shrink, float gravity) {
		if (impl != null && level.isClientSide()) {
			impl.wispTo(level, x, y, z, tx, ty, tz, scale, type, shrink, gravity);
		}
	}

	public static void sparkle(Level level, double x, double y, double z, float scale, int type, float gravity) {
		if (impl != null && level.isClientSide()) {
			impl.sparkle(level, x, y, z, scale, type, gravity);
		}
	}

	public static void blockSparkle(Level level, BlockPos pos, int color) {
		if (impl != null && level.isClientSide()) {
			impl.blockSparkle(level, pos, color);
		}
	}

	public static void crucibleBubble(Level level, double x, double y, double z, float r, float g, float b) {
		if (impl != null && level.isClientSide()) {
			impl.crucibleBubble(level, x, y, z, r, g, b);
		}
	}

	public static void crucibleFroth(Level level, float x, float y, float z) {
		if (impl != null && level.isClientSide()) {
			impl.crucibleFroth(level, x, y, z);
		}
	}

	public static void crucibleFrothDown(Level level, float x, float y, float z) {
		if (impl != null && level.isClientSide()) {
			impl.crucibleFrothDown(level, x, y, z);
		}
	}

	public static void crucibleBoil(Level level, BlockPos pos, float height) {
		if (impl != null && level.isClientSide()) {
			impl.crucibleBoil(level, pos, height);
		}
	}

	public static void essentiaTrail(Level level, double x, double y, double z, double tx, double ty, double tz, int count, int color, float scale) {
		if (impl != null && level.isClientSide()) {
			impl.essentiaTrail(level, x, y, z, tx, ty, tz, count, color, scale);
		}
	}

	public static void visBeam(Level level, double x, double y, double z, double tx, double ty, double tz, int color, float width) {
		if (impl != null && level.isClientSide()) {
			impl.visBeam(level, x, y, z, tx, ty, tz, color, width);
		}
	}

	/** ClientProxy.drawInfusionParticles1/2: a fleck of the pedestal's item flying into the matrix above target. */
	public static void infusionItem(Level level, double x, double y, double z, BlockPos target, ItemStack stack, boolean drift) {
		if (impl != null && level.isClientSide()) {
			impl.infusionItem(level, x, y, z, target, stack, drift);
		}
	}

	/** drawInfusionParticles3/4: FXBoreSparkle. */
	public static void infusionSparkle(Level level, double x, double y, double z, BlockPos target, float r, float g, float b) {
		if (impl != null && level.isClientSide()) {
			impl.infusionSparkle(level, x, y, z, target, r, g, b);
		}
	}

	/** ClientProxy.blockRunes: FXBlockRunes rising off a block. */
	public static void blockRunes(Level level, double x, double y, double z, float r, float g, float b, int dur, float grav) {
		if (impl != null && level.isClientSide()) {
			impl.blockRunes(level, x, y, z, r, g, b, dur, grav);
		}
	}

	/** ClientProxy.swarmParticleFX: an FXSwarm mote buzzing round the target; returns the particle so its owner can count and kill them. */
	public static Object swarm(net.minecraft.world.entity.Entity target, float speed, float turnSpeed, float gravity) {
		return impl != null && target.level().isClientSide() ? impl.swarm(target, speed, turnSpeed, gravity) : null;
	}

	public static boolean isAlive(Object particle) {
		return particle != null && impl != null && impl.isAlive(particle);
	}

	public static void kill(Object particle) {
		if (particle != null && impl != null) {
			impl.kill(particle);
		}
	}

	/** ClientProxy.splooshFX: a gobbet of purple slime flung from the entity. */
	public static void sploosh(net.minecraft.world.entity.Entity e) {
		if (impl != null && e.level().isClientSide()) {
			impl.sploosh(e);
		}
	}

	/** ClientProxy.slimeJumpFX: purple gobbets thrown up round a thaumic slime as it lands or is launched. */
	public static void slimeJump(net.minecraft.world.entity.Entity e, int size) {
		if (impl != null && e.level().isClientSide()) {
			impl.slimeJump(e, size);
		}
	}

	/** ClientProxy.tentacleAriseFX: purple gobbets thrown up round a taintacle as it rises out of the ground. */
	public static void tentacleArise(net.minecraft.world.entity.Entity e) {
		if (impl != null && e.level().isClientSide()) {
			impl.tentacleArise(e);
		}
	}

	/** ClientProxy.taintsplosionFX: a gobbet of purple slime blasted out of a tainted creeper. */
	public static void taintsplosion(net.minecraft.world.entity.Entity e) {
		if (impl != null && e.level().isClientSide()) {
			impl.taintsplosion(e);
		}
	}

	/** ClientProxy.dropletFX: FXDrop. */
	public static void droplet(Level level, double x, double y, double z, float r, float g, float b) {
		if (impl != null && level.isClientSide()) {
			impl.droplet(level, x, y, z, r, g, b);
		}
	}

	/** ClientProxy.smokeSpiral: FXSmokeSpiral. */
	public static void smokeSpiral(Level level, double x, double y, double z, float rad, int start, int miny, int color) {
		if (impl != null && level.isClientSide()) {
			impl.smokeSpiral(level, x, y, z, rad, start, miny, color);
		}
	}

	private FX() {
	}

	/** FXSpark: a crackle of static (energized air, shock impacts). */
	public static void spark(Level level, double x, double y, double z, float scale, float r, float g, float b, float a) {
		if (impl != null && level.isClientSide()) {
			impl.spark(level, x, y, z, scale, r, g, b, a);
		}
	}

	/** ClientProxy.drawGenericParticles: a frame run from particles.png. */
	public static void generic(Level level, double x, double y, double z, double mx, double my, double mz, float r, float g, float b, float alpha,
		boolean loop, int start, int num, int inc, int age, int delay, float scale) {
		if (impl != null && level.isClientSide()) {
			impl.generic(level, x, y, z, mx, my, mz, r, g, b, alpha, loop, start, num, inc, age, delay, scale);
		}
	}

	/** ClientProxy.beamCont: keeps a channelled focus's beam alive; pass back what it returned last tick. */
	public static Object beamCont(net.minecraft.world.entity.player.Player p, double tx, double ty, double tz, int type, int color, boolean reverse,
		float endmod, Object input, int impact) {
		return impl != null && p.level().isClientSide() ? impl.beamCont(p, tx, ty, tz, type, color, reverse, endmod, input, impact) : null;
	}

	/** ItemFocusShock.shootLightning: a crackling bolt from the caster's hand. */
	public static void shockBolt(net.minecraft.world.entity.player.Player p, double tx, double ty, double tz) {
		if (impl != null && p.level().isClientSide()) {
			impl.shockBolt(p, tx, ty, tz);
		}
	}

	/** ClientProxy.excavateFX: the cracking overlay on the block an excavation beam is boring into. */
	public static void excavate(Level level, BlockPos pos, net.minecraft.world.entity.player.Player p, int progress) {
		if (impl != null && level.isClientSide()) {
			impl.excavate(pos, p, progress);
		}
	}

	/** ClientProxy.nodeBolt: a short crackle of lightning (node transducers, the infusion matrix). */
	public static void nodeBolt(Level level, double x, double y, double z, double x2, double y2, double z2) {
		if (impl != null && level.isClientSide()) {
			impl.nodeBolt(level, x, y, z, x2, y2, z2);
		}
	}

	/** ClientProxy.burst: a quick flash of light. */
	public static void burst(Level level, double x, double y, double z, float size) {
		if (impl != null && level.isClientSide()) {
			impl.burst(level, x, y, z, size);
		}
	}
}
