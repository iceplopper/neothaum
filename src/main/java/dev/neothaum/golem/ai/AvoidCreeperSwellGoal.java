package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/** AIAvoidCreeperSwell: hop away from a creeper about to blow up. */
public class AvoidCreeperSwellGoal extends Goal {
	private final GolemEntity golem;
	private Creeper creeper;
	private Path path;
	private Vec3 targetBlock;

	public AvoidCreeperSwellGoal(GolemEntity golem) {
		this.golem = golem;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		List<Creeper> list = this.golem.level().getEntitiesOfClass(Creeper.class, this.golem.getBoundingBox().inflate(5.0, 3.0, 5.0));
		if (list.isEmpty() || list.get(0).getSwellDir() != 1) {
			return false;
		}
		this.creeper = list.get(0);
		if (!this.golem.getSensing().hasLineOfSight(this.creeper)) {
			return false;
		}
		Vec3 v = DefaultRandomPos.getPosAway(this.golem, 16, 7, this.creeper.position());
		if (v == null || this.creeper.distanceToSqr(v) < this.creeper.distanceToSqr(this.golem)) {
			return false;
		}
		this.path = this.golem.getNavigation().createPath(v.x, v.y, v.z, 0);
		this.targetBlock = v;
		return this.path != null;
	}

	@Override
	public boolean canContinueToUse() {
		return !this.golem.getNavigation().isDone();
	}

	@Override
	public void start() {
		double dx = this.targetBlock.x + 0.5 - this.golem.getX();
		double dz = this.targetBlock.z + 0.5 - this.golem.getZ();
		double d = Math.sqrt(dx * dx + dz * dz);
		Vec3 m = this.golem.getDeltaMovement();
		this.golem.setDeltaMovement(m.x + (dx / d * 0.8F + m.x * 0.2F), 0.3, m.z + (dz / d * 0.8F + m.z * 0.2F));
		this.golem.getNavigation().moveTo(this.path, 1.25);
	}

	@Override
	public void stop() {
		this.creeper = null;
	}

	@Override
	public void tick() {
		this.golem.getNavigation().setSpeedModifier(this.golem.distanceToSqr(this.creeper) < 49.0 ? 1.25 : 1.125);
	}
}
