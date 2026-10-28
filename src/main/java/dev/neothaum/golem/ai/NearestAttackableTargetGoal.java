package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * AINearestAttackableTarget: a guard golem picks the nearest valid target within its range. The butcher's AINearestButcherTarget picks the
 * oldest animal instead, and only from a herd of more than two of its kind.
 */
public class NearestAttackableTargetGoal extends Goal {
	protected final GolemEntity golem;
	protected LivingEntity target;

	public NearestAttackableTargetGoal(GolemEntity golem) {
		this.golem = golem;
		this.setFlags(EnumSet.of(Goal.Flag.TARGET));
	}

	protected List<LivingEntity> candidates() {
		float d = this.golem.getRange();
		return this.golem.level().getEntitiesOfClass(LivingEntity.class, this.golem.getBoundingBox().inflate(d, 4.0, d), e -> e != this.golem);
	}

	@Override
	public boolean canUse() {
		List<LivingEntity> list = this.candidates();
		list.sort(Comparator.comparingDouble(this.golem::distanceToSqr));
		for (LivingEntity e : list) {
			if (this.golem.isValidTarget(e)) {
				this.target = e;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canContinueToUse() {
		LivingEntity t = this.golem.getTarget();
		float d = this.golem.getRange();
		return t != null && t.isAlive() && this.golem.distanceToSqr(t) <= d * d;
	}

	@Override
	public void start() {
		this.golem.setTarget(this.target);
	}

	@Override
	public void stop() {
		this.golem.setTarget(null);
	}
}
