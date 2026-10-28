package dev.neothaum.golem.ai;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.EssentiaTransport;
import dev.neothaum.golem.GolemEntity;
import dev.neothaum.tiles.AlembicBlockEntity;
import dev.neothaum.tiles.JarBlockEntity;
import dev.neothaum.tiles.ReservoirBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * AIEssentiaGather: at home, draw essentia from the home block (a jar, reservoir or anything that gives essentia out on that side) or from the
 * fullest alembic stacked above it, up to what it can carry, one kind at a time.
 */
public class EssentiaGatherGoal extends GolemGoal {
	private long delay;
	private int start;

	public EssentiaGatherGoal(GolemEntity golem) {
		super(golem);
	}

	private boolean accepts(Aspect a) {
		return this.golem.essentiaAmount == 0 || (this.golem.essentia == null || this.golem.essentia.equals(a)) && this.golem.essentiaAmount < this.golem.getCarryLimit();
	}

	@Override
	public boolean canUse() {
		if (!this.navDone() || this.delay > System.currentTimeMillis()) {
			return false;
		}
		BlockPos c = this.golem.getHomeContainerPos();
		if (this.golem.distanceToSqr(c.getX() + 0.5, c.getY() + 0.5, c.getZ() + 0.5) > 6.0) {
			return false;
		}
		Direction facing = this.golem.getHomeFacing();
		this.start = 0;
		BlockEntity te = this.golem.level().getBlockEntity(c);
		if (te instanceof EssentiaTransport et) {
			if ((te instanceof JarBlockEntity || te instanceof ReservoirBlockEntity || et.canOutputTo(facing)) && et.getEssentiaAmount(facing) > 0
				&& (this.golem.essentiaAmount == 0 || (this.golem.essentia == null || this.golem.essentia.equals(et.getEssentiaType(facing))
					|| this.golem.essentia.equals(et.getEssentiaType(null))) && this.golem.essentiaAmount < this.golem.getCarryLimit())) {
				this.delay = System.currentTimeMillis() + 1000L;
				return true;
			}
		} else {
			this.start = -1;
			int prevTot = -1;
			for (int a = 5; a >= 0; a--) {
				if (this.golem.level().getBlockEntity(c.above(a)) instanceof AlembicBlockEntity ta && this.accepts(ta.aspect) && ta.amount > prevTot) {
					this.delay = System.currentTimeMillis() + 1000L;
					this.start = a;
					prevTot = ta.amount;
				}
			}
			return this.start >= 0;
		}
		return false;
	}

	@Override
	public boolean canContinueToUse() {
		return false;
	}

	@Override
	public void start() {
		BlockPos c = this.golem.getHomeContainerPos().above(this.start);
		Direction facing = this.golem.getHomeFacing();
		BlockEntity te = this.golem.level().getBlockEntity(c);
		if (!(te instanceof EssentiaTransport ta)) {
			return;
		}
		if (te instanceof AlembicBlockEntity || te instanceof JarBlockEntity) {
			facing = Direction.UP;
		}
		if (te instanceof ReservoirBlockEntity r) {
			facing = r.facing;
		}
		if (ta.getEssentiaAmount(facing) == 0) {
			return;
		}
		Aspect type = ta.getEssentiaType(facing);
		if (type == null) {
			type = ta.getEssentiaType(null);
		}
		if (!ta.canOutputTo(facing) || !this.accepts(type)) {
			return;
		}
		int qq = te instanceof ReservoirBlockEntity r ? r.containerContains(type) : ta.getEssentiaAmount(facing);
		int am = Math.min(qq, this.golem.getCarryLimit() - this.golem.essentiaAmount);
		this.golem.essentia = type;
		int taken = ta.takeEssentia(type, am, facing);
		if (taken > 0) {
			this.golem.essentiaAmount += taken;
			this.golem.playSound(SoundEvents.GENERIC_SWIM, 0.05F, 1.0F + (this.golem.getRandom().nextFloat() - this.golem.getRandom().nextFloat()) * 0.3F);
			this.golem.updateCarried();
		} else if (this.golem.essentiaAmount == 0) {
			this.golem.essentia = null;
		}
		this.delay = System.currentTimeMillis() + 100L;
	}
}
