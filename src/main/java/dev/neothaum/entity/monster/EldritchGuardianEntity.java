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
public class EldritchGuardianEntity extends Monster implements dev.neothaum.api.EldritchMob, net.minecraft.world.entity.monster.RangedAttackMob {
	private static final EntityDataAccessor<Byte> ATTACK_STATE = SynchedEntityData.defineId(EldritchGuardianEntity.class, EntityDataSerializers.BYTE);

	public float armLiftL = 0.0F;
	public float armLiftR = 0.0F;

	public EldritchGuardianEntity(EntityType<? extends EldritchGuardianEntity> type, Level level) {
		super(type, level);
		this.xpReward = 20;
	}

	/** getCanSpawnHere: never within 32 blocks (16 up and down) of another guardian, and otherwise as any monster. */
	public static boolean checkGuardianSpawnRules(EntityType<EldritchGuardianEntity> type, net.minecraft.world.level.ServerLevelAccessor level,
		net.minecraft.world.entity.EntitySpawnReason reason, net.minecraft.core.BlockPos pos, net.minecraft.util.RandomSource random) {
		return level.getEntitiesOfClass(EldritchGuardianEntity.class, new net.minecraft.world.phys.AABB(pos).inflate(32.0, 16.0, 32.0)).isEmpty()
			&& Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
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
		this.goalSelector.addGoal(2, new dev.neothaum.entity.monster.boss.LongRangeAttackGoal(this, 8.0, 1.0, 20, 40, 24.0F));
		this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
		this.goalSelector.addGoal(5, new net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal(this, 0.8));
		this.goalSelector.addGoal(7, new net.minecraft.world.entity.ai.goal.RandomStrollGoal(this, 1.0));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, CultistEntity.class, true));
	}

	/** Projectiles do half damage. */
	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (source.is(DamageTypeTags.IS_PROJECTILE)) {
			amount /= 2.0F;
		}
		return super.hurtServer(level, source, amount);
	}

	/** Like a zombie: a burning guardian may set what it hits alight. */
	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		boolean hit = super.doHurtTarget(level, target);
		if (hit) {
			int i = level.getDifficulty().getId();
			if (this.getMainHandItem().isEmpty() && this.isOnFire() && this.random.nextFloat() < i * 0.3F) {
				target.igniteForSeconds(2 * i);
			}
		}
		return hit;
	}

	/** Nine times in ten an eldritch orb from alternating hands; otherwise, if it can see its target, a screech. */
	@Override
	public void performRangedAttack(LivingEntity target, float distanceFactor) {
		if (this.random.nextFloat() > 0.1F) {
			var blast = new dev.neothaum.entity.projectile.EldritchOrbEntity(this.level(), this);
			boolean right = this.random.nextBoolean();
			this.level().broadcastEntityEvent(this, (byte) (right ? 16 : 15));
			int rr = right ? 90 : 180;
			double xx = net.minecraft.util.Mth.cos((this.getYRot() + rr) % 360.0F / 180.0F * (float) Math.PI) * 0.5F;
			double zz = net.minecraft.util.Mth.sin((this.getYRot() + rr) % 360.0F / 180.0F * (float) Math.PI) * 0.5F;
			blast.setPos(blast.getX() - xx, blast.getY() - 0.057777777 * this.getBbHeight(), blast.getZ() - zz);
			var tm = target.getDeltaMovement();
			blast.heading(target.getX() + tm.x - this.getX(), target.getY() - this.getY() - target.getBbHeight() / 2.0F, target.getZ() + tm.z - this.getZ(),
				1.0F, 2.0F);
			this.playSound(TVSounds.EGATTACK, 2.0F, 1.0F + this.random.nextFloat() * 0.1F);
			this.level().addFreshEntity(blast);
		} else if (this.hasLineOfSight(target)) {
			dev.neothaum.network.TVNetwork.sendEntityFx(this, 5, 0, 0);
			target.addEffect(new MobEffectInstance(MobEffects.WITHER, 400, 0));
			if (target instanceof Player player) {
				Warp.addWarpToPlayer(player, 1 + this.random.nextInt(3), true);
			}
			this.playSound(TVSounds.EGSCREECH, 3.0F, 1.0F + this.random.nextFloat() * 0.1F);
		}
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

	/** EntityEldritchGuardian.onUpdate: away from the Outer Lands, its gaze clouds the eyes of everyone within 16 blocks (24 on Hard). */
	@Override
	public void tick() {
		super.tick();
		if (this.level() instanceof ServerLevel level) {
			boolean outer = level.dimension().equals(dev.neothaum.world.outer.OuterLands.KEY);
			if (!outer && (this.tickCount <= 1 || this.tickCount % 100 == 0) && level.getDifficulty() != net.minecraft.world.Difficulty.EASY) {
				double range = level.getDifficulty() == net.minecraft.world.Difficulty.HARD ? 576.0 : 256.0;
				for (var p : level.players()) {
					if (p.isAlive() && p.distanceToSqr(this) < range) {
						net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(p, new dev.neothaum.network.TVNetwork.MiscEvent((short) 2));
					}
				}
			}
	}
	}

	/** ...and the ward grows back a point every 25 ticks while it isn't being hit (updateAITasks). */
	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		boolean outer = level.dimension().equals(dev.neothaum.world.outer.OuterLands.KEY);
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
}
