package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import net.minecraft.world.entity.LivingEntity;

/** AIDartAttack: a golem with a dart launcher shoots darts at targets more than three blocks off, faster with air upgrades. */
public class DartAttackGoal extends GolemGoal {
	private LivingEntity attackTarget;
	private int rangedAttackTime;
	private final int maxRangedAttackTime;

	public DartAttackGoal(GolemEntity golem) {
		super(golem);
		this.maxRangedAttackTime = 30 - golem.getUpgradeAmount(0) * 8;
		this.rangedAttackTime = this.maxRangedAttackTime / 2;
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
		if (this.golem.distanceToSqr(t.getX(), t.getBoundingBox().minY, t.getZ()) < 9.0) {
			return false;
		}
		this.attackTarget = t;
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		return this.canUse() && !this.navDone();
	}

	@Override
	public void stop() {
		this.attackTarget = null;
		this.rangedAttackTime = this.maxRangedAttackTime / 2;
	}

	@Override
	public void tick() {
		double d = this.golem.distanceToSqr(this.attackTarget.getX(), this.attackTarget.getBoundingBox().minY, this.attackTarget.getZ());
		boolean sees = this.golem.getSensing().hasLineOfSight(this.attackTarget);
		this.golem.getNavigation().moveTo(this.attackTarget, 1.0);
		if (sees) {
			this.golem.getLookControl().setLookAt(this.attackTarget, 30.0F, 30.0F);
			this.rangedAttackTime = Math.max(this.rangedAttackTime - 1, 0);
			if (this.rangedAttackTime <= 0) {
				float r = this.golem.getRange() * 0.8F;
				if (d <= r * r) {
					this.golem.attackEntityWithRangedAttack(this.attackTarget);
					this.rangedAttackTime = this.maxRangedAttackTime;
				}
			}
		}
	}
}
