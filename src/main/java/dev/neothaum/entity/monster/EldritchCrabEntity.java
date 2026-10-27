package dev.neothaum.entity.monster;

import dev.neothaum.registry.TVSounds;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * EldritchCrabEntity: skittering eldritch arthropod that leaps onto heads and wears a cultist helmet as armor.
 */
public class EldritchCrabEntity extends Monster {
	private static final EntityDataAccessor<Boolean> HELM = SynchedEntityData.defineId(EldritchCrabEntity.class, EntityDataSerializers.BOOLEAN);

	private int rideAttackCooldown = 0;

	public EldritchCrabEntity(EntityType<? extends EldritchCrabEntity> type, Level level) {
		super(type, level);
		this.xpReward = 6;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MAX_HEALTH, 20.0)
			.add(Attributes.ATTACK_DAMAGE, 4.0)
			.add(Attributes.MOVEMENT_SPEED, 0.275);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(HELM, false);
	}

	public boolean hasHelm() {
		return this.entityData.get(HELM);
	}

	public void setHelm(boolean helm) {
		this.entityData.set(HELM, helm);
		var speedAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speedAttr != null) {
			speedAttr.setBaseValue(helm ? 0.275 : 0.3);
		}
		var armorAttr = this.getAttribute(Attributes.ARMOR);
		if (armorAttr != null) {
			armorAttr.setBaseValue(helm ? 5.0 : 0.0);
		}
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.63F));
		this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
		this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, CultistEntity.class, true));
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, net.minecraft.world.entity.EntitySpawnReason spawnReason,
		SpawnGroupData spawnGroupData) {
		if (difficulty.getDifficulty() == Difficulty.HARD) {
			this.setHelm(true);
		} else {
			this.setHelm(level.getRandom().nextFloat() < 0.33F);
		}
		return super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
	}

	@Override
	public boolean canBeAffected(MobEffectInstance effect) {
		if (effect.getEffect() == MobEffects.POISON) {
			return false;
		}
		return super.canBeAffected(effect);
	}

	@Override
	public boolean canAttack(LivingEntity target) {
		if (target instanceof EldritchCrabEntity) {
			return false;
		}
		return super.canAttack(target);
	}

	@Override
	public void tick() {
		super.tick();

		// Face-hugger latch on target
		LivingEntity target = this.getTarget();
		if (!this.isPassenger() && target != null && !this.hasHelm() && !this.onGround() && !target.isPassenger()) {
			if (this.getY() - target.getY() >= target.getBbHeight() * 0.5 && this.distanceToSqr(target) < 4.0) {
				this.startRiding(target);
			}
		}

		// While riding a target entity, periodically attack it
		if (!this.level().isClientSide() && this.getVehicle() instanceof LivingEntity host) {
			if (this.rideAttackCooldown <= 0) {
				this.rideAttackCooldown = 10 + this.random.nextInt(10);
				this.doHurtTarget(this.level() instanceof ServerLevel sl ? sl : null, host);
				this.level().playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.CRABCLAW, SoundSource.HOSTILE, 1.0F,
					0.9F + this.random.nextFloat() * 0.2F);
				if (this.random.nextFloat() < 0.2F) {
					host.push((this.random.nextFloat() - 0.5) * 0.2, 0.1, (this.random.nextFloat() - 0.5) * 0.2);
				}
			} else {
				this.rideAttackCooldown--;
			}
		}
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		boolean damaged = super.hurtServer(level, source, amount);
		if (this.hasHelm() && this.getHealth() / this.getMaxHealth() <= 0.5F) {
			this.setHelm(false);
			level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ITEM_BREAK, SoundSource.HOSTILE, 1.0F, 1.0F);
			level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.IRON_HELMET),
				this.getX(), this.getY(0.5), this.getZ(), 10, 0.2, 0.2, 0.2, 0.1);
		}
		return damaged;
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		boolean hit = super.doHurtTarget(level, target);
		if (hit) {
			this.level().playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.CRABCLAW, SoundSource.HOSTILE, 1.0F,
				0.9F + this.random.nextFloat() * 0.2F);
		}
		return hit;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return TVSounds.CRABTALK;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.HOSTILE_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return TVSounds.CRABDEATH;
	}

	@Override
	public int getAmbientSoundInterval() {
		return 160;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
		super.dropCustomDeathLoot(level, source, recentlyHit);
		if (recentlyHit && (this.random.nextInt(3) == 0 || this.random.nextFloat() < 0.4F)) {
			this.spawnAtLocation(level, new ItemStack(Items.SPIDER_EYE));
		}
	}
}
