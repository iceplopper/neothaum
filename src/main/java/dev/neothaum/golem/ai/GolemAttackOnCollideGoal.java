package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.pathfinder.Path;

/** AIGolemAttackOnCollide: chase the target and strike it once per attack cooldown when in reach. */
public class GolemAttackOnCollideGoal extends GolemGoal {
	private LivingEntity entityTarget;
	private int attackTick;
	private Path path;
	private int counter;

	public GolemAttackOnCollideGoal(GolemEntity golem) {
		super(golem);
	}

	@Override
	public boolean canUse() {
		LivingEntity t = this.golem.getTarget();
		if (t == null) {
			return false;
		}
		if (!this.golem.isValidTarget(t)) {
			this.golem.setTarget(null);
			return false;
		}
		this.entityTarget = t;
		this.path = this.golem.getNavigation().createPath(t, 0);
		return this.path != null;
	}

	@Override
	public boolean canContinueToUse() {
		return this.canUse() && !this.navDone();
	}

	@Override
	public void start() {
		this.golem.getNavigation().moveTo(this.path, 1.0);
		this.counter = 0;
	}

	@Override
	public void stop() {
		this.entityTarget = null;
		this.golem.getNavigation().stop();
	}

	@Override
	public void tick() {
		this.golem.getLookControl().setLookAt(this.entityTarget, 30.0F, 30.0F);
		if (this.golem.getSensing().hasLineOfSight(this.entityTarget) && --this.counter <= 0) {
			this.counter = 4 + this.golem.getRandom().nextInt(7);
			this.golem.getNavigation().moveTo(this.entityTarget, 1.0);
		}
		this.attackTick = Math.max(this.attackTick - 1, 0);
		double w = this.entityTarget.getBbWidth() * 2.0F;
		double attackRange = w * w + 1.0;
		if (this.golem.distanceToSqr(this.entityTarget.getX(), this.entityTarget.getBoundingBox().minY, this.entityTarget.getZ()) <= attackRange
			&& this.attackTick <= 0) {
			this.attackTick = this.golem.getAttackSpeed();
			this.golem.startActionTimer();
			this.golem.doHurtTarget((ServerLevel) this.golem.level(), this.entityTarget);
		}
	}
}
