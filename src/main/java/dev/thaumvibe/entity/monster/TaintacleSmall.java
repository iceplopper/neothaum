package dev.thaumvibe.entity.monster;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/** EntityTaintacleSmall: a taint tendril that bursts out of the ground under someone, lashes them for ten seconds and withers away. */
public class TaintacleSmall extends Taintacle {
	private int lifetime = 200;

	public TaintacleSmall(EntityType<? extends TaintacleSmall> type, Level level) {
		super(type, level);
		this.xpReward = 0;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Taintacle.createAttributes().add(Attributes.MAX_HEALTH, 8.0).add(Attributes.ATTACK_DAMAGE, 2.0);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level() instanceof ServerLevel level && this.lifetime-- <= 0) {
			this.hurtServer(level, this.damageSources().magic(), 10.0F);
		}
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
	}
}
