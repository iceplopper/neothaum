package dev.thaumvibe.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * EntityFireBat: a burning bat of the nether that hangs from ceilings until a player comes within four blocks, then flits about and dives at
 * anyone within twelve, setting them alight, biting, or (one time in ten) bursting in a small explosion. Water hurts it, fire cannot. Summoned
 * bats (from a focus) serve their owner, wither away with no target, and come in devil, explosive and vampire kinds.
 */
public class FireBat extends Monster {
	private static final EntityDataAccessor<Byte> FLAGS = SynchedEntityData.defineId(FireBat.class, EntityDataSerializers.BYTE);
	private static final int HANGING = 0, SUMMONED = 1, EXPLOSIVE = 2, DEVIL = 3, VAMPIRE = 4;

	private BlockPos currentFlightTarget;
	public Player owner;
	public int damBonus;
	private int attackTime;

	public FireBat(EntityType<? extends FireBat> type, Level level) {
		super(type, level);
		this.setIsBatHanging(true);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 5.0).add(Attributes.ATTACK_DAMAGE, 1.0).add(Attributes.FOLLOW_RANGE, 12.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData) {
		super.defineSynchedData(entityData);
		entityData.define(FLAGS, (byte) 0);
	}

	private boolean flag(int bit) {
		return (this.entityData.get(FLAGS) & 1 << bit) != 0;
	}

	private void setFlag(int bit, boolean on) {
		byte b = this.entityData.get(FLAGS);
		this.entityData.set(FLAGS, (byte) (on ? b | 1 << bit : b & ~(1 << bit)));
	}

	public boolean getIsBatHanging() {
		return this.flag(HANGING);
	}

	public void setIsBatHanging(boolean hanging) {
		this.setFlag(HANGING, hanging);
	}

	public boolean getIsSummoned() {
		return this.flag(SUMMONED);
	}

	public void setIsSummoned(boolean summoned) {
		this.setFlag(SUMMONED, summoned);
		this.updateDamage();
	}

	public boolean getIsExplosive() {
		return this.flag(EXPLOSIVE);
	}

	public void setIsExplosive(boolean explosive) {
		this.setFlag(EXPLOSIVE, explosive);
	}

	public boolean getIsDevil() {
		return this.flag(DEVIL);
	}

	public void setIsDevil(boolean devil) {
		this.setFlag(DEVIL, devil);
		this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(devil ? 15.0 : 5.0);
		this.updateDamage();
	}

	public boolean getIsVampire() {
		return this.flag(VAMPIRE);
	}

	public void setIsVampire(boolean vampire) {
		this.setFlag(VAMPIRE, vampire);
	}

	private void updateDamage() {
		this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(this.getIsSummoned() ? (this.getIsDevil() ? 3 : 2) + this.damBonus : 1.0);
	}

	@Override
	protected void registerGoals() {
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.NONE;
	}

	@Override
	protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
	}

	@Override
	public boolean isIgnoringBlockTriggers() {
		return true;
	}

	@Override
	public void travel(Vec3 input) {
		this.travelFlying(input, 0.02F);
	}

	@Override
	public void aiStep() {
		if (this.isInWaterOrRain() && this.level() instanceof ServerLevel sl) {
			this.hurtServer(sl, this.damageSources().drown(), 1.0F);
		}
		super.aiStep();
	}

	@Override
	public void tick() {
		super.tick();
		Level level = this.level();
		RandomSource r = this.random;
		if (level.isClientSide() && this.getIsExplosive()) {
			level.addParticle(ParticleTypes.SMALL_FLAME, this.xo + (r.nextFloat() - r.nextFloat()) * 0.1F,
				this.yo + this.getBbHeight() / 2.0F + (r.nextFloat() - r.nextFloat()) * 0.1F, this.zo + (r.nextFloat() - r.nextFloat()) * 0.1F, 0, 0, 0);
		}
		if (this.getIsBatHanging()) {
			this.setDeltaMovement(Vec3.ZERO);
			this.setPos(this.getX(), Mth.floor(this.getY()) + 1.0 - this.getBbHeight(), this.getZ());
		} else {
			this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
		}
		if (level.isClientSide() && !this.getIsVampire()) {
			level.addParticle(ParticleTypes.SMOKE, this.xo + (r.nextFloat() - r.nextFloat()) * 0.2F,
				this.yo + this.getBbHeight() / 2.0F + (r.nextFloat() - r.nextFloat()) * 0.2F, this.zo + (r.nextFloat() - r.nextFloat()) * 0.2F, 0, 0, 0);
			level.addParticle(ParticleTypes.FLAME, this.xo + (r.nextFloat() - r.nextFloat()) * 0.2F,
				this.yo + this.getBbHeight() / 2.0F + (r.nextFloat() - r.nextFloat()) * 0.2F, this.zo + (r.nextFloat() - r.nextFloat()) * 0.2F, 0, 0, 0);
		}
	}

	private boolean ceilingAbove() {
		BlockPos up = BlockPos.containing(this.getX(), (int) this.getY() + 1, this.getZ());
		return this.level().getBlockState(up).isRedstoneConductor(this.level(), up);
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		if (this.attackTime > 0) {
			this.attackTime--;
		}
		if (this.getIsBatHanging()) {
			if (!this.ceilingAbove()) {
				this.setIsBatHanging(false);
				level.levelEvent(null, 1025, this.blockPosition(), 0);
			} else {
				if (this.random.nextInt(200) == 0) {
					this.yHeadRot = this.random.nextInt(360);
				}
				if (level.getNearestPlayer(this, 4.0) != null) {
					this.setIsBatHanging(false);
					level.levelEvent(null, 1025, this.blockPosition(), 0);
				}
			}
			return;
		}
		LivingEntity target = this.getTarget();
		if (target instanceof Player p && (p.isCreative() || p.isSpectator()) || target != null && !target.isAlive()) {
			this.setTarget(null);
			target = null;
		}
		if (target == null && !this.getIsSummoned()) {
			target = level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 12.0,
				e -> e instanceof Player p && !p.isCreative() && !p.isSpectator() && p.isAlive());
			this.setTarget(target);
		}
		Vec3 m = this.getDeltaMovement();
		double mx = m.x, my = m.y, mz = m.z;
		if (target == null) {
			if (this.getIsSummoned()) {
				this.hurtServer(level, this.damageSources().generic(), 2.0F);
			}
			if (this.currentFlightTarget != null && (!level.isEmptyBlock(this.currentFlightTarget) || this.currentFlightTarget.getY() < level.getMinY() + 1)) {
				this.currentFlightTarget = null;
			}
			if (this.currentFlightTarget == null || this.random.nextInt(30) == 0
				|| this.currentFlightTarget.distSqr(BlockPos.containing(this.getX(), this.getY(), this.getZ())) < 4.0) {
				this.currentFlightTarget = BlockPos.containing(this.getX() + this.random.nextInt(7) - this.random.nextInt(7),
					this.getY() + this.random.nextInt(6) - 2, this.getZ() + this.random.nextInt(7) - this.random.nextInt(7));
			}
			double dx = this.currentFlightTarget.getX() + 0.5 - this.getX();
			double dy = this.currentFlightTarget.getY() + 0.1 - this.getY();
			double dz = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
			mx += (Math.signum(dx) * 0.5 - mx) * 0.1F;
			my += (Math.signum(dy) * 0.7F - my) * 0.1F;
			mz += (Math.signum(dz) * 0.5 - mz) * 0.1F;
			if (this.random.nextInt(100) == 0 && this.ceilingAbove()) {
				this.setIsBatHanging(true);
			}
		} else {
			double dx = target.getX() - this.getX();
			double dy = target.getY() + target.getEyeHeight() * 0.66F - this.getY();
			double dz = target.getZ() - this.getZ();
			mx += (Math.signum(dx) * 0.5 - mx) * 0.1F;
			my += (Math.signum(dy) * 0.7F - my) * 0.1F;
			mz += (Math.signum(dz) * 0.5 - mz) * 0.1F;
			this.attackEntity(level, target, this.distanceTo(target));
		}
		if (this.isRemoved()) {
			return;
		}
		this.setDeltaMovement(mx, my, mz);
		float yaw = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0F;
		this.setYRot(this.getYRot() + Mth.wrapDegrees(yaw - this.getYRot()));
		this.zza = 0.5F;
	}

	private void attackEntity(ServerLevel level, LivingEntity target, float dist) {
		if (this.attackTime > 0 || dist >= Math.max(2.5F, target.getBbWidth() * 1.1F) || target.getBoundingBox().maxY <= this.getBoundingBox().minY
			|| target.getBoundingBox().minY >= this.getBoundingBox().maxY) {
			return;
		}
		if (this.getIsSummoned()) {
			target.setLastHurtByMob(this);
		}
		if (this.getIsVampire()) {
			if (this.owner != null && !this.owner.hasEffect(MobEffects.REGENERATION)) {
				this.owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 26, 1));
			}
			this.heal(1.0F);
		}
		this.attackTime = 20;
		if ((this.getIsExplosive() || level.getRandom().nextInt(10) == 0) && !this.getIsDevil()) {
			target.damageCooldownTime = 0;
			level.explode(this, this.getX(), this.getY(), this.getZ(), 1.5F + (this.getIsExplosive() ? this.damBonus * 0.33F : 0.0F),
				Level.ExplosionInteraction.NONE);
			this.discard();
		} else if (!this.getIsVampire() && !level.getRandom().nextBoolean()) {
			target.igniteForSeconds(this.getIsSummoned() ? 4 : 2);
		} else {
			Vec3 motion = target.getDeltaMovement();
			this.doHurtTarget(level, target);
			target.setDeltaMovement(motion);
		}
		level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BAT_HURT, SoundSource.HOSTILE, 0.5F,
			0.9F + level.getRandom().nextFloat() * 0.2F);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_EXPLOSION)) {
			return false;
		}
		if (this.getIsBatHanging()) {
			this.setIsBatHanging(false);
		}
		return super.hurtServer(level, source, damage);
	}

	@Override
	protected float getSoundVolume() {
		return 0.1F;
	}

	@Override
	public float getVoicePitch() {
		return super.getVoicePitch() * 0.95F;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return this.getIsBatHanging() && this.random.nextInt(4) != 0 ? null : SoundEvents.BAT_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.BAT_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.BAT_DEATH;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		if (!this.getIsSummoned()) {
			int looting = dev.thaumvibe.entity.TVEntities.looting(level, source);
			// EntityLiving.dropFewItems: up to two, more with looting
			int n = this.random.nextInt(3) + (looting > 0 ? this.random.nextInt(looting + 1) : 0);
			for (int i = 0; i < n; i++) {
				this.spawnAtLocation(level, Items.GUNPOWDER);
			}
		}
	}

	/** EntityFireBat.getCanSpawnHere: only where block light is low. */
	public static boolean checkFireBatSpawnRules(EntityType<FireBat> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
		RandomSource random) {
		if (level.getDifficulty() == Difficulty.PEACEFUL) {
			return false;
		}
		return EntitySpawnReason.isSpawner(reason) || level.getBrightness(LightLayer.BLOCK, pos) <= random.nextInt(7);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putByte("BatFlags", this.entityData.get(FLAGS));
		output.putByte("damBonus", (byte) this.damBonus);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.entityData.set(FLAGS, input.getByteOr("BatFlags", (byte) 0));
		this.damBonus = input.getByteOr("damBonus", (byte) 0);
		this.updateDamage();
	}
}
