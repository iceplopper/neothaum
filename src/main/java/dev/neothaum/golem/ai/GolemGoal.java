package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

/** The common ground of the golem AIs: TC4's tasks run every tick, check for work every golemDelay ticks and hold MOVE and LOOK (mutex 3). */
public abstract class GolemGoal extends Goal {
	protected final GolemEntity golem;
	private int lastDelay = Integer.MIN_VALUE / 2;

	protected GolemGoal(GolemEntity golem) {
		this(golem, EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	protected GolemGoal(GolemEntity golem, EnumSet<Goal.Flag> flags) {
		this.golem = golem;
		this.setFlags(flags);
	}

	/** theGolem.ticksExisted % Config.golemDelay <= 0. */
	protected boolean delayTick() {
		if (this.golem.tickCount - this.lastDelay < dev.neothaum.config.NeoThaumConfig.current.golemDelay) {
			return false;
		}
		this.lastDelay = this.golem.tickCount;
		return true;
	}

	protected boolean navDone() {
		return this.golem.getNavigation().isDone();
	}

	/** Within five (squared) of home, where the home tasks work. */
	protected boolean atHome() {
		return this.golem.distToHomeSqr() <= 5.0;
	}

	protected void moveTo(double x, double y, double z) {
		this.golem.getNavigation().moveTo(x, y, z, 1.0);
	}

	protected void moveTo(BlockPos p) {
		this.moveTo(p.getX(), p.getY(), p.getZ());
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}
}
