package dev.neothaum.entity.monster;

import dev.neothaum.entity.projectile.GolemOrbEntity;
import dev.neothaum.registry.TVItems;
import dev.neothaum.registry.TVSounds;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
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

/**
 * CrimsonLeaderEntity: Crimson Praetor boss leading the cultists.
 * Heavy fortress armor, radiates Strength II to cultists, fires homing orbs, caps damage at 35, and drops Rare Loot Bag.
 */
public class CrimsonLeaderEntity extends Monster implements RangedAttackMob, dev.neothaum.api.EldritchMob {
	public static final String[] TITLES = new String[]{
		"Alberic", "Anselm", "Bastian", "Beturian", "Chabier", "Chorache",
		"Chuse", "Dodorol", "Ebardo", "Ferrando", "Fertus", "Guillen",
		"Larpe", "Obano", "Zelipe"
	};

	private static final EntityDataAccessor<Integer> TITLE = SynchedEntityData.defineId(CrimsonLeaderEntity.class, EntityDataSerializers.INT);

	private final ServerBossEvent bossEvent = new ServerBossEvent(UUID.randomUUID(), Component.literal("Crimson Praetor"),
		BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
	private int enrageTimer = 0;

	public CrimsonLeaderEntity(EntityType<? extends CrimsonLeaderEntity> type, Level level) {
		super(type, level);
		this.xpReward = 40;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MAX_HEALTH, 125.0)
			.add(Attributes.MOVEMENT_SPEED, 0.32)
			.add(Attributes.ATTACK_DAMAGE, 5.0)
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
		Component name = Component.literal(TITLES[clamped] + " the Crimson Praetor");
		this.setCustomName(name);
		this.bossEvent.setName(name);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 30, 40, 24.0F));
		this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, false));
		this.goalSelector.addGoal(4, new OpenDoorGoal(this, true));
		this.goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.8));
		this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

		this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(CultistEntity.class));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
		this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TVItems.CULTIST_LEADER_HELM));
		this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(TVItems.CULTIST_LEADER_CHEST));
		this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(TVItems.CULTIST_LEADER_LEGS));
		this.setItemSlot(EquipmentSlot.FEET, new ItemStack(TVItems.CULTIST_BOOTS));

		if (difficulty.getDifficulty() == Difficulty.EASY) {
			this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TVItems.VOID_SWORD));
		} else {
			this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TVItems.CRIMSON_BLADE));
		}
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, net.minecraft.world.entity.EntitySpawnReason spawnReason,
		SpawnGroupData spawnGroupData) {
		this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
		this.setTitleIndex(level.getRandom().nextInt(TITLES.length));
		return super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
	}

	@Override
	public boolean canAttack(LivingEntity target) {
		if (target instanceof CultistEntity || target instanceof CrimsonLeaderEntity) {
			return false;
		}
		return super.canAttack(target);
	}

	@Override
	public void tick() {
		super.tick();

		if (!this.level().isClientSide()) {
			this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

			if (this.enrageTimer > 0) {
				this.enrageTimer--;
			}

			// Aura: Strength II to nearby cultists every 20 ticks
			if (this.tickCount % 20 == 0) {
				AABB box = this.getBoundingBox().inflate(8.0);
				for (CultistEntity cultist : this.level().getEntitiesOfClass(CultistEntity.class, box)) {
					cultist.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 60, 1));
				}
			}
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
	public boolean hurtServer(net.minecraft.server.level.ServerLevel level, DamageSource source, float amount) {
		if (amount > 35.0F) {
			if (this.enrageTimer == 0) {
				this.enrageTimer = 200;
				this.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 200, 1));
				this.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 200, 2));
				this.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 1));
				level.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY(1.0), this.getZ(), 5, 0.3, 0.3, 0.3, 0.1);
			}
			amount = 35.0F;
		}
		return super.hurtServer(level, source, amount);
	}

	@Override
	public void performRangedAttack(LivingEntity target, float distanceFactor) {
		double dx = target.getX() - this.getX();
		double dy = target.getY(0.5) - this.getY(0.5);
		double dz = target.getZ() - this.getZ();

		GolemOrbEntity orb = new GolemOrbEntity(this.level(), this, target, true);
		orb.setPos(this.getX(), this.getY(0.5) + 0.8, this.getZ());
		orb.heading(dx, dy + 2.0, dz, 0.66F, 3.0F);
		this.level().playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.EGATTACK, SoundSource.HOSTILE, 1.0F,
			1.0F + this.random.nextFloat() * 0.1F);
		this.level().addFreshEntity(orb);
	}

	@Override
	protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel level, DamageSource source, boolean recentlyHit) {
		super.dropCustomDeathLoot(level, source, recentlyHit);
		this.spawnAtLocation(level, new ItemStack(TVItems.LOOT_BAG_RARE));
	}
}
