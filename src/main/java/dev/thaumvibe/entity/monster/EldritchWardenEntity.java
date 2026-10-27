package dev.thaumvibe.entity.monster;

import dev.thaumvibe.entity.projectile.GolemOrbEntity;
import dev.thaumvibe.registry.TVItems;
import dev.thaumvibe.registry.TVSounds;
import dev.thaumvibe.warp.Warp;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
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
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * EldritchWardenEntity: massive eldritch guardian boss of the Outer Lands.
 * Runic shielding, sonic screech with temporary warp, wither blasts, and primordial pearl drop.
 */
public class EldritchWardenEntity extends Monster implements RangedAttackMob {
	public static final String[] TITLES = new String[]{
		"Aphoom-Zhah", "Basatan", "Chaugnar Faugn", "Mnomquah", "Nyogtha",
		"Oorn", "Shaikorth", "Rhan-Tegoth", "Rhogog", "Shudde M'ell",
		"Vulthoom", "Yag-Kosha", "Yibb-Tstll", "Zathog", "Zushakon"
	};

	private static final EntityDataAccessor<Integer> TITLE = SynchedEntityData.defineId(EldritchWardenEntity.class, EntityDataSerializers.INT);

	private final ServerBossEvent bossEvent = new ServerBossEvent(UUID.randomUUID(), Component.literal("Eldritch Warden"),
		BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
	private int screechCooldown = 100;
	private int frenzyTicks = 0;

	public EldritchWardenEntity(EntityType<? extends EldritchWardenEntity> type, Level level) {
		super(type, level);
		this.xpReward = 50;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MAX_HEALTH, 200.0)
			.add(Attributes.MOVEMENT_SPEED, 0.33)
			.add(Attributes.ATTACK_DAMAGE, 10.0)
			.add(Attributes.ARMOR, 8.0)
			.add(Attributes.KNOCKBACK_RESISTANCE, 0.95)
			.add(Attributes.FOLLOW_RANGE, 40.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(TITLE, 0);
	}

	public int getTitleIndex() {
		return this.entityData.get(TITLE);
	}

	public void setTitleIndex(int idx) {
		int clamped = Math.max(0, Math.min(TITLES.length - 1, idx));
		this.entityData.set(TITLE, clamped);
		Component name = Component.literal(TITLES[clamped] + " the Eldritch Warden");
		this.setCustomName(name);
		this.bossEvent.setName(name);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 20, 40, 24.0F));
		this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, false));
		this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 0.8));
		this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, CultistEntity.class, true));
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, net.minecraft.world.entity.EntitySpawnReason spawnReason,
		SpawnGroupData spawnGroupData) {
		this.setTitleIndex(level.getRandom().nextInt(TITLES.length));
		return super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
	}

	@Override
	public void tick() {
		super.tick();

		if (!this.level().isClientSide()) {
			this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

			// Passive shield regeneration: 1 HP every 25 ticks
			if (this.tickCount % 25 == 0 && this.getHealth() < this.getMaxHealth()) {
				this.heal(1.0F);
			}

			// Screech cooldown
			if (this.screechCooldown > 0) {
				this.screechCooldown--;
			} else {
				LivingEntity target = this.getTarget();
				if (target != null && this.distanceToSqr(target) < 100.0) {
					this.performScreech(target);
					this.screechCooldown = 200 + this.random.nextInt(100);
				}
			}

			// Frenzy lightning arcing
			if (this.frenzyTicks > 0) {
				this.frenzyTicks--;
				if (this.frenzyTicks % 10 == 0 && this.level() instanceof ServerLevel sl) {
					sl.playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.ZAP, SoundSource.HOSTILE, 1.0F,
						0.9F + this.random.nextFloat() * 0.1F);
					sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(0.5), this.getZ(), 20, 2.0, 1.0, 2.0, 0.1);
				}
			}
		}
	}

	private void performScreech(LivingEntity target) {
		if (this.level() instanceof ServerLevel sl) {
			sl.playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.EGSCREECH, SoundSource.HOSTILE, 4.0F,
				1.0F + this.random.nextFloat() * 0.1F);
			sl.sendParticles(ParticleTypes.SONIC_BOOM, this.getX(), this.getY() + 1.8, this.getZ(), 1, 0, 0, 0, 0);

			AABB box = this.getBoundingBox().inflate(10.0);
			for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, box)) {
				if (e != this && !this.isAlliedTo(e)) {
					e.addEffect(new MobEffectInstance(MobEffects.WITHER, 400, 0));
					e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 0));
					Vec3 knockback = e.position().subtract(this.position()).normalize().scale(1.5);
					e.push(knockback.x, 0.2, knockback.z);

					if (e instanceof ServerPlayer sp) {
						Warp.addWarpToPlayer(sp, 3 + this.random.nextInt(3), true);
					}
				}
			}
		}
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		// 50% magic damage reduction
		if (source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR) || source.is(net.minecraft.tags.DamageTypeTags.WITCH_RESISTANT_TO)) {
			amount *= 0.5F;
		}

		if (amount > 20.0F && this.frenzyTicks == 0) {
			this.frenzyTicks = 60;
		}

		return super.hurtServer(level, source, amount);
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		boolean hit = super.doHurtTarget(level, target);
		if (hit && target instanceof LivingEntity living) {
			living.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0));
			living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
		}
		return hit;
	}

	@Override
	public void performRangedAttack(LivingEntity target, float distanceFactor) {
		double dx = target.getX() - this.getX();
		double dy = target.getY(0.5) - this.getY(0.5);
		double dz = target.getZ() - this.getZ();

		GolemOrbEntity orb = new GolemOrbEntity(this.level(), this, target, false);
		orb.setPos(this.getX(), this.getY(0.7), this.getZ());
		orb.heading(dx, dy + 2.0, dz, 0.66F, 3.0F);
		this.level().playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.EGATTACK, SoundSource.HOSTILE, 1.0F,
			1.0F + this.random.nextFloat() * 0.1F);
		this.level().addFreshEntity(orb);
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
	protected SoundEvent getAmbientSound() {
		return TVSounds.EGIDLE;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return TVSounds.EGDEATH;
	}

	@Override
	public int getAmbientSoundInterval() {
		return 500;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
		super.dropCustomDeathLoot(level, source, recentlyHit);
		this.spawnAtLocation(level, new ItemStack(TVItems.PRIMORDIAL_PEARL));
		this.spawnAtLocation(level, new ItemStack(TVItems.LOOT_BAG_RARE));
	}
}
