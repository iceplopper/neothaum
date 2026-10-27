package dev.neothaum.entity.monster;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * EntityTaintacleGiant: a towering taintacle guarding the tainted land, with a boss bar. Huge blows are capped at 35 and the first one enrages
 * it, healing and strengthening it for ten seconds. (TC4 made it a champion and dropped a primordial pearl when the last of them died; those
 * come with the champion system and the eldritch items.)
 */
public class TaintacleGiant extends Taintacle {
	private static final EntityDataAccessor<Integer> ANGER = SynchedEntityData.defineId(TaintacleGiant.class, EntityDataSerializers.INT);
	private final ServerBossEvent bossEvent = new ServerBossEvent(this.getUUID(), this.getDisplayName(), BossEvent.BossBarColor.PURPLE,
		BossEvent.BossBarOverlay.PROGRESS);

	public TaintacleGiant(EntityType<? extends TaintacleGiant> type, Level level) {
		super(type, level);
		this.xpReward = 20;
		this.setPersistenceRequired();
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Taintacle.createAttributes().add(Attributes.MAX_HEALTH, 125.0).add(Attributes.ATTACK_DAMAGE, 9.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData) {
		super.defineSynchedData(entityData);
		entityData.define(ANGER, 0);
	}

	public int getAnger() {
		return this.entityData.get(ANGER);
	}

	public void setAnger(int anger) {
		this.entityData.set(ANGER, anger);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.getAnger() > 0) {
			this.setAnger(this.getAnger() - 1);
		}
		if (this.level().isClientSide()) {
			if (this.random.nextInt(15) == 0 && this.getAnger() > 0) {
				this.level().addParticle(ParticleTypes.ANGRY_VILLAGER, this.getX() + this.random.nextFloat() * this.getBbWidth() - this.getBbWidth() / 2.0,
					this.getBoundingBox().minY + this.getBbHeight() + this.random.nextFloat() * 0.5,
					this.getZ() + this.random.nextFloat() * this.getBbWidth() - this.getBbWidth() / 2.0, this.random.nextGaussian() * 0.02,
					this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02);
			}
		} else {
			if (this.tickCount % 30 == 0) {
				this.heal(1.0F);
			}
			this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
		}
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (damage > 35.0F) {
			if (this.getAnger() == 0) {
				this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, (int) (damage / 15.0F)));
				this.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 200, (int) (damage / 40.0F)));
				this.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, (int) (damage / 40.0F)));
				this.setAnger(200);
				if (source.getEntity() instanceof Player p) {
					p.sendSystemMessage(Component.empty().append(this.getDisplayName()).append(" ").append(Component.translatable("tc.boss.enrage")));
				}
			}
			damage = 35.0F;
		}
		return super.hurtServer(level, source, damage);
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return false;
	}

	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		this.bossEvent.addPlayer(player);
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		this.bossEvent.removePlayer(player);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
	}
}
