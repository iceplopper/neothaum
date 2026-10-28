package dev.neothaum.entity.monster;

import dev.neothaum.blocks.LootBlock;
import dev.neothaum.entity.monster.boss.LongRangeAttackGoal;
import dev.neothaum.entity.monster.boss.ThaumcraftBoss;
import dev.neothaum.entity.projectile.GolemOrbEntity;
import dev.neothaum.fx.FX;
import dev.neothaum.registry.TVSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
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
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * EntityEldritchGolem: a hulking construct, fire-proof and all but immovable, that tramples soft blocks and smashes urns and crates as it
 * walks. Its blows hurl their target into the air. The blow that would kill it instead blows its head off: it rises again over five seconds,
 * and from then on, sparking from the neck, it hurls golem orbs whenever its charge allows, recharging for seven and a half seconds whenever
 * it runs dry, and knocks back what it strikes.
 */
public class EldritchGolemEntity extends ThaumcraftBoss implements RangedAttackMob, dev.neothaum.api.EldritchMob,
	dev.neothaum.entity.champion.ChampionBoss {
	private static final EntityDataAccessor<Boolean> HEADLESS = SynchedEntityData.defineId(EldritchGolemEntity.class, EntityDataSerializers.BOOLEAN);

	private int beamCharge;
	private boolean chargingBeam;
	private int attackTimer;
	public int arcing;
	public BlockPos arcTo = BlockPos.ZERO;
	private boolean rangedAdded;

	public EldritchGolemEntity(EntityType<? extends EldritchGolemEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return ThaumcraftBoss.createBossAttributes().add(Attributes.MAX_HEALTH, 250.0).add(Attributes.MOVEMENT_SPEED, 0.3)
			.add(Attributes.ATTACK_DAMAGE, 10.0).add(Attributes.ARMOR, 6.0);
	}

	/** entity.Thaumcraft.EldritchGolem.name: "<modifier> Eldritch Construct". */
	@Override
	public void nameAsChampion(Component modifier) {
		this.setCustomName(modifier.copy().append(" Eldritch Construct"));
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
		this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, false));
		this.goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.8));
		this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.8));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	/** makeHeadless: from now on it also shoots. */
	private void makeHeadless() {
		if (!this.rangedAdded) {
			this.rangedAdded = true;
			this.goalSelector.addGoal(2, new LongRangeAttackGoal(this, 3.0, 1.0, 5, 5, 24.0F));
		}
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
		this.spawnTimer = 100;
		return super.finalizeSpawn(level, difficulty, reason, data);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.attackTimer > 0) {
			this.attackTimer--;
		}
		Level level = this.level();
		Vec3 m = this.getDeltaMovement();
		if (m.x * m.x + m.z * m.z > 2.5000003E-7F && this.random.nextInt(5) == 0) {
			BlockPos below = BlockPos.containing(this.getX(), this.getY() - 0.2, this.getZ());
			BlockState bs = level.getBlockState(below);
			if (!bs.isAir()) {
				level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, bs), this.getX() + (this.random.nextFloat() - 0.5) * this.getBbWidth(),
					this.getBoundingBox().minY + 0.1, this.getZ() + (this.random.nextFloat() - 0.5) * this.getBbWidth(), 4.0 * (this.random.nextFloat() - 0.5),
					0.5, (this.random.nextFloat() - 0.5) * 4.0);
			}
			if (!level.isClientSide() && bs.getBlock() instanceof LootBlock) {
				level.destroyBlock(below, true);
			}
		}
		if (!level.isClientSide()) {
			BlockPos ahead = BlockPos.containing(this.getX() + m.x, this.getBoundingBox().minY, this.getZ() + m.z);
			float h = level.getBlockState(ahead).getDestroySpeed(level, ahead);
			if (h >= 0.0F && h <= 0.15F && !level.getBlockState(ahead).isAir()) {
				level.destroyBlock(ahead, true);
			}
		}
	}

	/** The killing blow takes its head off instead. */
	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (damage > this.getHealth() && !this.isHeadless() && !this.isInvulnerable()) {
			this.setHeadless(true);
			this.spawnTimer = 100;
			double xx = Mth.cos(this.getYRot() % 360.0F / 180.0F * (float) Math.PI) * 0.75F;
			double zz = Mth.sin(this.getYRot() % 360.0F / 180.0F * (float) Math.PI) * 0.75F;
			level.explode(this, this.getX() + xx, this.getY() + this.getEyeHeight(), this.getZ() + zz, 2.0F, Level.ExplosionInteraction.NONE);
			this.makeHeadless();
			return false;
		}
		return super.hurtServer(level, source, damage);
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		if (this.attackTimer > 0) {
			return false;
		}
		this.attackTimer = 10;
		level.broadcastEntityEvent(this, (byte) 4);
		boolean hit = target.hurtServer(level, this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.75F);
		if (hit) {
			target.setDeltaMovement(target.getDeltaMovement().add(0.0, 0.2, 0.0));
			if (this.isHeadless()) {
				target.push(-Mth.sin(this.getYRot() * (float) Math.PI / 180.0F) * 1.5F, 0.1, Mth.cos(this.getYRot() * (float) Math.PI / 180.0F) * 1.5F);
			}
		}
		return hit;
	}

	@Override
	public void performRangedAttack(LivingEntity target, float distanceFactor) {
		if (!this.hasLineOfSight(target) || this.chargingBeam || this.beamCharge <= 0) {
			return;
		}
		this.beamCharge -= 15 + this.random.nextInt(5);
		this.getLookControl().setLookAt(target.getX(), target.getBoundingBox().minY + target.getBbHeight() / 2.0F, target.getZ(), 30.0F, 30.0F);
		Vec3 v = this.getViewVector(1.0F);
		GolemOrbEntity blast = new GolemOrbEntity(this.level(), this, target, false);
		blast.setPos(blast.getX() + v.x, blast.getY(), blast.getZ() + v.z);
		var tm = target.getDeltaMovement();
		blast.heading(target.getX() + tm.x - this.getX(), target.getY() - this.getY() - target.getBbHeight() / 2.0F, target.getZ() + tm.z - this.getZ(),
			0.66F, 5.0F);
		this.playSound(TVSounds.EGATTACK, 1.0F, 1.0F + this.random.nextFloat() * 0.1F);
		this.level().addFreshEntity(blast);
	}

	@Override
	public void tick() {
		if (this.spawnTimer == 150 && !this.level().isClientSide()) {
			this.level().broadcastEntityEvent(this, (byte) 18);
		}
		if (this.spawnTimer > 0) {
			this.heal(2.0F);
		}
		super.tick();
		if (this.level().isClientSide()) {
			if (this.isHeadless()) {
				float yaw = -this.yBodyRot * Mth.DEG_TO_RAD - (float) Math.PI;
				Vec3 v = new Vec3(Mth.sin(yaw) * -1.0F, 0.0, Mth.cos(yaw) * -1.0F);
				double ey = this.getY() + this.getEyeHeight();
				if (this.random.nextInt(20) == 0) {
					float a = (this.random.nextFloat() - this.random.nextFloat()) / 2.0F;
					float b = (this.random.nextFloat() - this.random.nextFloat()) / 2.0F;
					FX.spark(this.level(), this.getX() + v.x + a, ey - 0.25F, this.getZ() + v.z + b, 0.3F, 0.65F + this.random.nextFloat() * 0.1F, 1.0F, 1.0F,
						0.8F);
				}
				FX.vent(this.level(), this.getX() + v.x * 0.66, ey - 0.75F, this.getZ() + v.z * 0.66, 0.0, 0.001, 0.0, 5592405, 4.0F);
				if (this.arcing > 0) {
					FX.arc(this.level(), this.getX(), this.getY() + this.getBbHeight() / 2.0F, this.getZ(), this.arcTo.getX() + 0.5, this.arcTo.getY() + 1,
						this.arcTo.getZ() + 0.5, 0.65F + this.random.nextFloat() * 0.1F, 1.0F, 1.0F, 1.0F - this.arcing / 10.0F);
					this.arcing--;
				}
			}
		} else {
			if (this.isHeadless() && this.beamCharge <= 0) {
				this.chargingBeam = true;
			}
			if (this.isHeadless() && this.chargingBeam) {
				this.beamCharge++;
				this.level().broadcastEntityEvent(this, (byte) 19);
				if (this.beamCharge == 150) {
					this.chargingBeam = false;
				}
			}
		}
	}

	public int getAttackTimer() {
		return this.attackTimer;
	}

	@Override
	public void handleEntityEvent(byte id) {
		switch (id) {
			case 4 -> {
				this.attackTimer = 10;
				this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
			}
			case 18 -> this.spawnTimer = 150;
			case 19 -> {
				if (this.arcing == 0) {
					float radius = 2.0F + this.random.nextFloat() * 2.0F;
					double radians = Math.toRadians(this.random.nextInt(360));
					int bx = Mth.floor(this.getX() + radius * Math.cos(radians));
					int by = Mth.floor(this.getY());
					int bz = Mth.floor(this.getZ() + radius * Math.sin(radians));
					for (int c = 0; c < 5 && this.level().getBlockState(new BlockPos(bx, by, bz)).isAir(); by--) {
						c++;
					}
					BlockPos p = new BlockPos(bx, by, bz);
					if (this.level().getBlockState(p.above()).isAir() && !this.level().getBlockState(p).isAir()) {
						this.arcTo = p;
						this.arcing = 8 + this.random.nextInt(5);
						this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), TVSounds.JACOBS, SoundSource.HOSTILE, 0.8F,
							1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.05F, false);
					}
				}
			}
			default -> super.handleEntityEvent(id);
		}
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
	protected void playStepSound(BlockPos pos, BlockState state) {
		this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 1.0F);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putBoolean("headless", this.isHeadless());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.setHeadless(input.getBooleanOr("headless", false));
		if (this.isHeadless()) {
			this.makeHeadless();
		}
	}
}
