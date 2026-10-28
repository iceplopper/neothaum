package dev.neothaum.entity.monster;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.phys.AABB;

/** AICultistHurtByTarget: turn on whoever hurt it, and call every idle cultist nearby (but not clerics at a ritual) to join in. */
public class CultistHurtByTargetGoal extends HurtByTargetGoal {
	public CultistHurtByTargetGoal(PathfinderMob mob) {
		super(mob);
		this.setAlertOthers();
	}

	@Override
	protected void alertOthers() {
		LivingEntity attacker = this.mob.getLastHurtByMob();
		if (attacker == null) {
			return;
		}
		double d = this.getFollowDistance();
		AABB box = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(d, 10.0, d);
		for (CultistEntity c : this.mob.level().getEntitiesOfClass(CultistEntity.class, box)) {
			if (c != this.mob && c.getTarget() == null && !c.isAlliedTo(attacker)
				&& !(c instanceof CrimsonClericEntity cleric && cleric.isRitualist())) {
				c.setTarget(attacker);
			}
		}
	}
}
