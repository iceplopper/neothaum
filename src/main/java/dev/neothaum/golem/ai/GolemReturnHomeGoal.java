package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

public class GolemReturnHomeGoal extends Goal {
	private final GolemEntity golem;
	private int cooldown = 0;

	public GolemReturnHomeGoal(GolemEntity golem) {
		this.golem = golem;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (this.golem.isInactive() || this.golem.getCore() < 0) return false;
		if (this.cooldown > 0) {
			this.cooldown--;
			return false;
		}
		BlockPos home = this.golem.getHomePos();
		if (home.equals(BlockPos.ZERO)) return false;
		return this.golem.distanceToSqr(home.getX() + 0.5, home.getY() + 0.5, home.getZ() + 0.5) > 9.0;
	}

	@Override
	public boolean canContinueToUse() {
		BlockPos home = this.golem.getHomePos();
		return !this.golem.getNavigation().isDone()
			&& this.golem.distanceToSqr(home.getX() + 0.5, home.getY() + 0.5, home.getZ() + 0.5) > 4.0;
	}

	@Override
	public void start() {
		BlockPos home = this.golem.getHomePos();
		this.golem.getNavigation().moveTo(home.getX() + 0.5, home.getY() + 0.5, home.getZ() + 0.5, 1.0);
	}

	@Override
	public void stop() {
		this.cooldown = 40;
		this.golem.getNavigation().stop();
	}
}
