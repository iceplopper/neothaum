package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;

/** AIHurtByTarget: a guard golem turns on whatever hurt it (not its owner, villagers, other golems or bats, nor creative players). */
public class GolemHurtByTargetGoal extends Goal {
	private final GolemEntity golem;
	private int timestamp;

	public GolemHurtByTargetGoal(GolemEntity golem) {
		this.golem = golem;
		this.setFlags(EnumSet.of(Goal.Flag.TARGET));
	}

	static boolean suitable(GolemEntity golem, LivingEntity e) {
		if (e == null || e == golem || !e.isAlive() || e instanceof Villager || e instanceof GolemEntity || e instanceof net.minecraft.world.entity.ambient.Bat) {
			return false;
		}
		if (e instanceof Player p && (p.isCreative() || p.isSpectator() || p.getName().getString().equals(golem.getOwnerName()))) {
			return false;
		}
		return golem.isWithinHomeRange(e.blockPosition());
	}

	@Override
	public boolean canUse() {
		LivingEntity e = this.golem.getLastHurtByMob();
		return this.golem.getLastHurtByMobTimestamp() != this.timestamp && suitable(this.golem, e);
	}

	@Override
	public boolean canContinueToUse() {
		LivingEntity t = this.golem.getTarget();
		return t != null && t.isAlive() && this.golem.distanceToSqr(t) <= 256.0;
	}

	@Override
	public void start() {
		this.golem.setTarget(this.golem.getLastHurtByMob());
		this.timestamp = this.golem.getLastHurtByMobTimestamp();
	}

	@Override
	public void stop() {
		if (this.golem.getTarget() instanceof Player p && p.getAbilities().invulnerable) {
			this.golem.setTarget(null);
		}
	}
}
