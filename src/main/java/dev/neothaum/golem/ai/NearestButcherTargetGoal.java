package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import java.util.Comparator;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;

/** AINearestButcherTarget: the oldest grown animal in range, if more than two of its kind are about. */
public class NearestButcherTargetGoal extends NearestAttackableTargetGoal {
	public NearestButcherTargetGoal(GolemEntity golem) {
		super(golem);
	}

	@Override
	public boolean canUse() {
		List<LivingEntity> list = this.candidates();
		list.sort(Comparator.comparingInt((LivingEntity e) -> e.tickCount).reversed());
		for (LivingEntity e : list) {
			if (this.golem.isValidTarget(e)) {
				int count = 0;
				for (LivingEntity o : list) {
					if (o.getClass() == e.getClass() && this.golem.isValidTarget(o)) {
						count++;
					}
				}
				if (count > 2) {
					this.target = e;
					return true;
				}
			}
		}
		return false;
	}
}
