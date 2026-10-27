package dev.thaumvibe.entity.monster;

import dev.thaumvibe.entity.projectile.GolemOrbEntity;
import dev.thaumvibe.registry.TVItems;
import dev.thaumvibe.registry.TVSounds;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * EldritchGolemEntity: colossal construct of the Outer Lands.
 * Knockback immune, flings targets with melee blows, loses its head upon near-death,
 * and unleashes electric neck-beam barrages in phase 2. Drops Primordial Pearl and Rare Loot Bag.
 */
public class EldritchGolemEntity extends Monster implements RangedAttackMob {
	private static final EntityDataAccessor<Boolean> HEADLESS = SynchedEntityData.defineId(EldritchGolemEntity.class, EntityDataSerializers.BOOLEAN);

	private final ServerBossEvent bossEvent = new ServerBossEvent(UUID.randomUUID(), Component.literal("Eldritch Golem"),
		BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
	private int beamCharge = 0;
	private int attackTimer = 0;

	public EldritchGolemEntity(EntityType<? extends EldritchGolemEntity> type, Level level) {
		super(type, level);
		this.xpReward = 50;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MAX_HEALTH, 250.0)
			.add(Attributes.MOVEMENT_SPEED, 0.3)
			.add(Attributes.ATTACK_DAMAGE, 10.0)
			.add(Attributes.ARMOR, 6.0)
			.add(Attributes.KNOCKBACK_RESISTANCE, 0.95)
			.add(Attributes.FOLLOW_RANGE, 40.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(HEADLESS, false);
	}

	public boolean isHeadless() {
		return this.entityData.get(HEADLESS);
	}

	public void setHeadless(boolean headless) {
		this.entityData.set(HEADLESS, headless);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 15, 25, 24.0F));
		this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, false));
		this.goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.8));
		this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	public void tick() {
		super.tick();

		if (this.attackTimer > 0) {
			this.attackTimer--;
		}

		if (!this.level().isClientSide()) {
			this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

			// Headless phase charging
			if (this.isHeadless()) {
				this.beamCharge++;
				if (this.beamCharge % 40 == 0 && this.level() instanceof ServerLevel sl) {
					sl.playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.JACOBS, SoundSource.HOSTILE, 1.0F,
						0.9F + this.random.nextFloat() * 0.1F);
					sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 3.0, this.getZ(), 15, 0.3, 0.3, 0.3, 0.05);
					sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX(), this.getY() + 3.0, this.getZ(), 3, 0.1, 0.2, 0.1, 0.01);
				}
			}
		}
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (amount >= this.getHealth() && !this.isHeadless()) {
			this.setHeadless(true);
			this.setHealth(this.getMaxHealth() * 0.5F);
			level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.0F, 1.0F);
			level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 3.2, this.getZ(), 1, 0, 0, 0, 0);
			level.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY() + 3.0, this.getZ(), 20, 0.4, 0.4, 0.4, 0.1);
			return false;
		}

		return super.hurtServer(level, source, amount);
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		boolean hit = super.doHurtTarget(level, target);
		if (hit) {
			this.attackTimer = 10;
			this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.IRON_GOLEM_ATTACK, SoundSource.HOSTILE, 1.0F, 1.0F);
			target.push(0, 0.4, 0);
			if (this.isHeadless()) {
				Vec3 knock = target.position().subtract(this.position()).normalize().scale(1.2);
				target.push(knock.x, 0.2, knock.z);
			}
		}
		return hit;
	}

	@Override
	public void performRangedAttack(LivingEntity target, float distanceFactor) {
		if (this.isHeadless()) {
			// Rapid barrage of 2-3 homing orbs
			int count = 2 + this.random.nextInt(2);
			for (int i = 0; i < count; i++) {
				double spread = (this.random.nextDouble() - 0.5) * 1.5;
				GolemOrbEntity orb = new GolemOrbEntity(this.level(), this, target, false);
				orb.setPos(this.getX(), this.getY() + 3.0, this.getZ());
				double dx = target.getX() + spread - this.getX();
				double dy = target.getY(0.5) - (this.getY() + 3.0);
				double dz = target.getZ() + spread - this.getZ();
				orb.heading(dx, dy, dz, 0.66F, 4.0F);
				this.level().addFreshEntity(orb);
			}
			this.level().playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.EGATTACK, SoundSource.HOSTILE, 1.0F,
				1.0F + this.random.nextFloat() * 0.1F);
		}
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
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.IRON_GOLEM_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.IRON_GOLEM_DEATH;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
		super.dropCustomDeathLoot(level, source, recentlyHit);
		this.spawnAtLocation(level, new ItemStack(TVItems.PRIMORDIAL_PEARL));
		this.spawnAtLocation(level, new ItemStack(TVItems.LOOT_BAG_RARE));
	}
}
