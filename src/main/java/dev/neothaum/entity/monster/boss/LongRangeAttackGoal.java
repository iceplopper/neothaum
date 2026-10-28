package dev.neothaum.entity.monster.boss;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;

/** AILongRangeAttack: the ranged attack, but only at targets further off than a minimum distance (closer ones are left to melee). */
public class LongRangeAttackGoal extends RangedAttackGoal {
	private final Mob wielder;
	private final double minDistance;

	public LongRangeAttackGoal(RangedAttackMob mob, double min, double speed, int minInterval, int maxInterval, float range) {
		super(mob, speed, minInterval, maxInterval, range);
		this.wielder = (Mob) mob;
		this.minDistance = min;
	}

	@Override
	public boolean canUse() {
		if (!super.canUse()) {
			return false;
		}
		LivingEntity t = this.wielder.getTarget();
		if (t == null) {
			return false;
		}
		if (!t.isAlive()) {
			this.wielder.setTarget(null);
			return false;
		}
		return this.wielder.distanceToSqr(t.getX(), t.getBoundingBox().minY, t.getZ()) >= this.minDistance * this.minDistance;
	}
}
