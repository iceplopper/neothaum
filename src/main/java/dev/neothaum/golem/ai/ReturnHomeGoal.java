package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

/** AIReturnHome: with nothing else to do, walk back home; if it cannot find a way, wait a little longer each time before trying again. */
public class ReturnHomeGoal extends GolemGoal {
	private int pathingDelay;
	private int pathingDelayInc = 5;
	private int count;
	private BlockPos prev = BlockPos.ZERO;

	public ReturnHomeGoal(GolemEntity golem) {
		super(golem);
	}

	@Override
	public boolean canUse() {
		if (this.pathingDelay > 0) {
			this.pathingDelay--;
		}
		return this.pathingDelay <= 0 && this.golem.distToHomeSqr() >= 3.0;
	}

	@Override
	public boolean canContinueToUse() {
		return this.pathingDelay <= 0 && this.count > 0 && !this.navDone() && this.golem.distToHomeSqr() >= 3.0;
	}

	private void path(double x, double y, double z) {
		if (!this.golem.getNavigation().moveTo(x, y, z, 1.0)) {
			this.pathingDelay = this.pathingDelayInc;
			if (this.pathingDelayInc < 50) {
				this.pathingDelayInc += 5;
			}
		} else {
			this.pathingDelayInc = 5;
		}
	}

	@Override
	public void tick() {
		this.count--;
		if (this.count == 0 && this.prev.equals(this.golem.blockPosition())) {
			Vec3 v = DefaultRandomPos.getPos(this.golem, 2, 1);
			if (v != null) {
				this.count = 20;
				this.path(v.x + 0.5, v.y + 0.5, v.z + 0.5);
			}
		}
	}

	@Override
	public void start() {
		this.count = 20;
		this.prev = this.golem.blockPosition();
		BlockPos h = this.golem.getHomePos();
		this.path(h.getX() + 0.5, h.getY() + 0.5, h.getZ() + 0.5);
	}
}
