package dev.thaumvibe.golem.ai;

import dev.thaumvibe.golem.GolemCoreType;
import dev.thaumvibe.golem.GolemEntity;
import dev.thaumvibe.golem.GolemUpgradeType;
import dev.thaumvibe.registry.TVSounds;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

public class GolemAttackGoal extends Goal {
	private final GolemEntity golem;
	private LivingEntity target;
	private int attackCooldown = 0;
	private int searchCooldown = 0;

	public GolemAttackGoal(GolemEntity golem) {
		this.golem = golem;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		int core = this.golem.getCore();
		if (this.golem.isInactive() || (core != GolemCoreType.GUARD.id && core != GolemCoreType.BUTCHER.id)) {
			return false;
		}

		if (this.searchCooldown-- > 0) return false;
		this.searchCooldown = 10;

		return this.findTarget();
	}

	private boolean findTarget() {
		int core = this.golem.getCore();
		double range = 16.0 + this.golem.getUpgradeCount(GolemUpgradeType.AIR) * 8.0;
		AABB box = this.golem.getBoundingBox().inflate(range);
		List<LivingEntity> nearby = this.golem.level().getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != this.golem);

		double closest = Double.MAX_VALUE;
		LivingEntity best = null;

		for (LivingEntity e : nearby) {
			if (this.isValidTarget(e, core)) {
				double dist = this.golem.distanceToSqr(e);
				if (dist < closest) {
					closest = dist;
					best = e;
				}
			}
		}

		if (best != null) {
			this.target = best;
			this.golem.setTarget(best);
			return true;
		}
		return false;
	}

	private boolean isValidTarget(LivingEntity e, int core) {
		if (core == GolemCoreType.BUTCHER.id) {
			if (e instanceof Animal animal && !animal.isBaby()) {
				// Count adults of same species in range
				List<Animal> species = this.golem.level().getEntitiesOfClass(Animal.class, animal.getBoundingBox().inflate(8.0), a -> a.getClass() == animal.getClass() && a.isAlive() && !a.isBaby());
				return species.size() > 2; // Keep at least 2 adults for breeding
			}
			return false;
		}

		if (core == GolemCoreType.GUARD.id) {
			if (e instanceof Creeper) return this.golem.getTargetFlag(3);
			if (e instanceof Enemy) return this.golem.getTargetFlag(0);
			if (e instanceof Animal) return this.golem.getTargetFlag(1);
			if (e instanceof Player) return this.golem.getTargetFlag(2);
		}

		return false;
	}

	@Override
	public boolean canContinueToUse() {
		return this.target != null && this.target.isAlive() && this.golem.distanceToSqr(this.target) < 400.0;
	}

	@Override
	public void tick() {
		if (this.target == null) return;

		this.golem.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
		double distSq = this.golem.distanceToSqr(this.target);

		boolean hasDart = this.golem.getDecoration().contains("R");

		if (hasDart && distSq > 16.0) {
			// Ranged dart attack
			this.golem.getNavigation().moveTo(this.target, 1.0);
			if (this.attackCooldown <= 0 && this.golem.hasLineOfSight(this.target)) {
				if (this.golem.level() instanceof ServerLevel sl) {
					Arrow arrow = new Arrow(sl, this.golem, new ItemStack(Items.ARROW), null);
					double dx = this.target.getX() - this.golem.getX();
					double dy = this.target.getY(0.33333) - arrow.getY();
					double dz = this.target.getZ() - this.golem.getZ();
					double dist = Math.sqrt(dx * dx + dz * dz);
					arrow.shoot(dx, dy + dist * 0.2, dz, 1.6F, 2.0F);
					sl.addFreshEntity(arrow);
					sl.playSound(null, this.golem.getX(), this.golem.getY(), this.golem.getZ(),
						SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 0.5F, 1.2F);
				}
				this.attackCooldown = Math.max(15, 30 - this.golem.getUpgradeCount(GolemUpgradeType.FIRE) * 6);
			}
		} else {
			// Melee attack
			this.golem.getNavigation().moveTo(this.target, 1.25);
			if (distSq <= 4.0 && this.attackCooldown <= 0) {
				boolean hasMace = this.golem.getDecoration().contains("M");
				float damage = (float) this.golem.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
				if (hasMace) damage += 2.0F;

				if (this.golem.level() instanceof ServerLevel sl) {
					this.target.hurtServer(sl, this.golem.damageSources().mobAttack(this.golem), damage);
					if (hasMace) {
						this.target.knockback(1.0, this.golem.getX() - this.target.getX(), this.golem.getZ() - this.target.getZ(),
							this.golem.damageSources().mobAttack(this.golem), 1.0F);
					}
					sl.playSound(null, this.golem.getX(), this.golem.getY(), this.golem.getZ(),
						TVSounds.ZAP, SoundSource.NEUTRAL, 0.4F, 1.4F);
				}
				this.attackCooldown = 20;
			}
		}

		if (this.attackCooldown > 0) {
			this.attackCooldown--;
		}
	}

	@Override
	public void stop() {
		this.target = null;
		this.golem.setTarget(null);
		this.golem.getNavigation().stop();
	}
}
