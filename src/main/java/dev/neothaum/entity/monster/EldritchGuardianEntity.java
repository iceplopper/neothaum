package dev.neothaum.entity.monster;

import dev.neothaum.registry.TVSounds;
import dev.neothaum.warp.Warp;
import java.util.EnumSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** EntityEldritchGuardian: mysterious, robed outer lands guardian summoned by deep warp hallucinations or ancient portals. */
public class EldritchGuardianEntity extends Monster implements dev.neothaum.api.EldritchMob {
	private static final EntityDataAccessor<Byte> ATTACK_STATE = SynchedEntityData.defineId(EldritchGuardianEntity.class, EntityDataSerializers.BYTE);

	public float armLiftL = 0.0F;
	public float armLiftR = 0.0F;

	public EldritchGuardianEntity(EntityType<? extends EldritchGuardianEntity> type, Level level) {
		super(type, level);
		this.xpReward = 20;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MAX_HEALTH, 50.0)
			.add(Attributes.MOVEMENT_SPEED, 0.28)
			.add(Attributes.ATTACK_DAMAGE, 7.0)
			.add(Attributes.ARMOR, 4.0)
			.add(Attributes.FOLLOW_RANGE, 40.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ATTACK_STATE, (byte) 0);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new GuardianRangedAttackGoal(this));
		this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		// Magic damage resistance: take 50% damage
		if (source.is(DamageTypeTags.WITCH_RESISTANT_TO) || source.is(DamageTypeTags.BYPASSES_ARMOR)) {
			amount *= 0.5F;
		}
		return super.hurtServer(level, source, amount);
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		boolean hit = super.doHurtTarget(level, target);
		if (hit && target instanceof LivingEntity living) {
			living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
			living.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
		}
		return hit;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.level().isClientSide()) {
			if (this.armLiftL > 0.0F) this.armLiftL -= 0.05F;
			if (this.armLiftR > 0.0F) this.armLiftR -= 0.05F;

			// Ambient dark particles around guardian
			if (this.random.nextInt(3) == 0) {
				double px = this.getX() + (this.random.nextDouble() - this.random.nextDouble()) * 0.3;
				double py = this.getY() + this.random.nextDouble() * this.getBbHeight();
				double pz = this.getZ() + (this.random.nextDouble() - this.random.nextDouble()) * 0.3;
				this.level().addParticle(ParticleTypes.PORTAL, px, py, pz,
					(this.random.nextDouble() - 0.5) * 0.2, (this.random.nextDouble() - 0.5) * 0.2, (this.random.nextDouble() - 0.5) * 0.2);
			}
		}
	}

	/** In the Outer Lands a guardian wears a ward of half its base health (EntityEldritchGuardian.onSpawnWithEgg). */
	@Override
	public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level,
		net.minecraft.world.DifficultyInstance difficulty, net.minecraft.world.entity.EntitySpawnReason reason,
		net.minecraft.world.entity.SpawnGroupData data) {
		data = super.finalizeSpawn(level, difficulty, reason, data);
		if (level.getLevel().dimension().equals(dev.neothaum.world.outer.OuterLands.KEY)) {
			float ward = (float) this.getAttributeBaseValue(Attributes.MAX_HEALTH) / 2.0F;
			var max = this.getAttribute(Attributes.MAX_ABSORPTION);
			if (max != null) {
				max.removeModifier(OUTER_WARD);
				max.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(OUTER_WARD, ward,
					net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
			}
			this.setAbsorptionAmount(this.getAbsorptionAmount() + ward);
		}
		return data;
	}

	private static final net.minecraft.resources.Identifier OUTER_WARD = dev.neothaum.NeoThaum.id("outer_ward");

	/** ...and the ward grows back a point every 25 ticks while it isn't being hit (updateAITasks). */
	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		boolean outer = level.dimension().equals(dev.neothaum.world.outer.OuterLands.KEY);
		// EntityEldritchGuardian.onUpdate: away from the Outer Lands, its gaze clouds the eyes of everyone within 16 blocks (24 on Hard)
		if (!outer && (this.tickCount <= 1 || this.tickCount % 100 == 0) && level.getDifficulty() != net.minecraft.world.Difficulty.EASY) {
			double range = level.getDifficulty() == net.minecraft.world.Difficulty.HARD ? 576.0 : 256.0;
			for (var p : level.players()) {
				if (p.isAlive() && p.distanceToSqr(this) < range) {
					net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(p, new dev.neothaum.network.TVNetwork.MiscEvent((short) 2));
				}
			}
		}
		if (outer && this.hurtTime <= 0 && this.tickCount % 25 == 0) {
			int bh = (int) this.getAttributeBaseValue(Attributes.MAX_HEALTH) / 2;
			if (this.getAbsorptionAmount() < bh) {
				this.setAbsorptionAmount(this.getAbsorptionAmount() + 1.0F);
			}
		}
	}

	public void setAttackState(byte state) {
		this.entityData.set(ATTACK_STATE, state);
	}

	public byte getAttackState() {
		return this.entityData.get(ATTACK_STATE);
	}

	@Override
	public void handleEntityEvent(byte eventId) {
		if (eventId == 15) {
			this.armLiftL = 0.5F;
		} else if (eventId == 16) {
			this.armLiftR = 0.5F;
		} else if (eventId == 17) {
			this.armLiftL = 0.9F;
			this.armLiftR = 0.9F;
		} else {
			super.handleEntityEvent(eventId);
		}
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
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
	}

	// Ranged spell attack
	private static class GuardianRangedAttackGoal extends Goal {
		private final EldritchGuardianEntity guardian;
		private int attackCooldown = 30;

		GuardianRangedAttackGoal(EldritchGuardianEntity guardian) {
			this.guardian = guardian;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			LivingEntity target = this.guardian.getTarget();
			if (target == null || !target.isAlive()) return false;
			double distSq = this.guardian.distanceToSqr(target);
			return distSq >= 16.0 && distSq <= 576.0;
		}

		@Override
		public void tick() {
			LivingEntity target = this.guardian.getTarget();
			if (target == null) return;
			this.guardian.getLookControl().setLookAt(target, 30.0F, 30.0F);

			if (--this.attackCooldown <= 0) {
				this.attackCooldown = 35 + this.guardian.getRandom().nextInt(15);
				Level level = this.guardian.level();
				if (level instanceof ServerLevel sl) {
					// 20% chance screech, 80% ranged wither strike
					if (this.guardian.getRandom().nextFloat() < 0.2F) {
						// Screech attack
						level.playSound(null, this.guardian.getX(), this.guardian.getY(), this.guardian.getZ(),
							TVSounds.EGSCREECH, SoundSource.HOSTILE, 2.5F, 1.0F);
						target.addEffect(new MobEffectInstance(MobEffects.WITHER, 160, 0));
						if (target instanceof Player player) {
							Warp.addWarpToPlayer(player, 1 + this.guardian.getRandom().nextInt(3), true);
						}
						sl.sendParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getY() + 1.0, target.getZ(), 1, 0, 0, 0, 0);
					} else {
						// Ranged blast attack
						level.playSound(null, this.guardian.getX(), this.guardian.getY(), this.guardian.getZ(),
							TVSounds.EGATTACK, SoundSource.HOSTILE, 1.8F, 1.0F);
						target.hurtServer(sl, this.guardian.damageSources().mobAttack(this.guardian), 4.5F);
						target.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 0));
						sl.sendParticles(ParticleTypes.WITCH, target.getX(), target.getY() + 1.0, target.getZ(), 15, 0.3, 0.5, 0.3, 0.05);
					}
					level.broadcastEntityEvent(this.guardian, (byte) (this.guardian.getRandom().nextBoolean() ? 15 : 16));
				}
			}
		}
	}
}
