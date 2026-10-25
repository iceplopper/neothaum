package dev.thaumvibe.entity.monster;

import dev.thaumvibe.api.TaintedMob;
import dev.thaumvibe.fx.FX;
import dev.thaumvibe.registry.TVItems;
import dev.thaumvibe.registry.TVSounds;
import dev.thaumvibe.world.TVBiomes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * EntityTaintSwarm: a cloud of biting motes with no body of its own. It drifts about the tainted land, hunts any player within twelve blocks and
 * leaves them weakened. A summoned swarm (from a focus) dies off quickly once it has nothing to chase.
 */
public class TaintSwarm extends Monster implements TaintedMob {
	private static final EntityDataAccessor<Byte> FLAGS = SynchedEntityData.defineId(TaintSwarm.class, EntityDataSerializers.BYTE);

	private BlockPos currentFlightTarget;
	public int damBonus;
	public final List<Object> swarm = new ArrayList<>();
	private int attackTime;

	public TaintSwarm(EntityType<? extends TaintSwarm> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.ATTACK_DAMAGE, 2.0).add(Attributes.FOLLOW_RANGE, 12.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData) {
		super.defineSynchedData(entityData);
		entityData.define(FLAGS, (byte) 0);
	}

	public boolean getIsSummoned() {
		return (this.entityData.get(FLAGS) & 2) != 0;
	}

	public void setIsSummoned(boolean summoned) {
		byte b = this.entityData.get(FLAGS);
		this.entityData.set(FLAGS, summoned ? (byte) (b | 2) : (byte) (b & -4));
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
	public void tick() {
		super.tick();
		this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
		if (this.level().isClientSide()) {
			this.swarm.removeIf(p -> !FX.isAlive(p));
			if (this.swarm.size() < 25) {
				Object fx = FX.swarm(this, 0.22F, 15.0F, 0.08F);
				if (fx != null) {
					this.swarm.add(fx);
				}
			}
		}
	}

	@Override
	public void travel(Vec3 input) {
		this.travelFlying(input, 0.02F);
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		LivingEntity target = this.getTarget();
		if (target instanceof Player p && (p.isCreative() || p.isSpectator() || !p.isAlive()) || target != null && !target.isAlive()) {
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
				this.hurtServer(level, this.damageSources().generic(), 5.0F);
			}
			if (this.currentFlightTarget != null && (!level.isEmptyBlock(this.currentFlightTarget) || this.currentFlightTarget.getY() < level.getMinY() + 1
				|| this.currentFlightTarget.getY() > level.getHeight(Heightmap.Types.MOTION_BLOCKING, this.currentFlightTarget.getX(),
					this.currentFlightTarget.getZ()) + 8
				|| !TVBiomes.is(level, this.currentFlightTarget, TVBiomes.TAINTED_LAND))) {
				this.currentFlightTarget = null;
			}
			if (this.currentFlightTarget == null || this.random.nextInt(30) == 0
				|| this.currentFlightTarget.distSqr(BlockPos.containing(this.getX(), this.getY(), this.getZ())) < 4.0) {
				this.currentFlightTarget = BlockPos.containing(this.getX() + this.random.nextInt(7) - this.random.nextInt(7),
					this.getY() + this.random.nextInt(6) - 2, this.getZ() + this.random.nextInt(7) - this.random.nextInt(7));
			}
			double var1 = this.currentFlightTarget.getX() + 0.5 - this.getX();
			double var3 = this.currentFlightTarget.getY() + 0.1 - this.getY();
			double var5 = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
			mx += (Math.signum(var1) * 0.5 - mx) * 0.015;
			my += (Math.signum(var3) * 0.7F - my) * 0.1F;
			mz += (Math.signum(var5) * 0.5 - mz) * 0.015;
		} else {
			double var1 = target.getX() - this.getX();
			double var3 = target.getY() + target.getEyeHeight() - this.getY();
			double var5 = target.getZ() - this.getZ();
			mx += (Math.signum(var1) * 0.5 - mx) * 0.025;
			my += (Math.signum(var3) * 0.7F - my) * 0.1F;
			mz += (Math.signum(var5) * 0.5 - mz) * 0.025;
			this.attackEntity(level, target, this.distanceTo(target));
		}
		this.setDeltaMovement(mx, my, mz);
		float var7 = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0F;
		this.setYRot(this.getYRot() + Mth.wrapDegrees(var7 - this.getYRot()));
		this.zza = 0.1F;
		if (this.attackTime > 0) {
			this.attackTime--;
		}
	}

	private void attackEntity(ServerLevel level, LivingEntity target, float dist) {
		if (this.attackTime <= 0 && dist < 3.0F && target.getBoundingBox().maxY > this.getBoundingBox().minY
			&& target.getBoundingBox().minY < this.getBoundingBox().maxY) {
			if (this.getIsSummoned()) {
				target.setLastHurtByMob(this);
			}
			this.attackTime = 10 + this.random.nextInt(5);
			Vec3 motion = target.getDeltaMovement();
			if (this.doHurtTarget(level, target)) {
				target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
			}
			target.setDeltaMovement(motion);
			target.needsSync = true;
			level.playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.SWARMATTACK, SoundSource.HOSTILE, 0.3F,
				0.9F + level.getRandom().nextFloat() * 0.2F);
		}
	}

	@Override
	protected float getSoundVolume() {
		return 0.1F;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return TVSounds.SWARMATTACK;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return TVSounds.SWARMATTACK;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putByte("Flags", this.entityData.get(FLAGS));
		output.putByte("damBonus", (byte) this.damBonus);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.entityData.set(FLAGS, input.getByteOr("Flags", (byte) 0));
		this.damBonus = input.getByteOr("damBonus", (byte) 0);
		this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(2 + this.damBonus);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		if (this.random.nextBoolean()) {
			this.spawnAtLocation(level, new ItemStack(TVItems.TAINTED_GOO), this.getBbHeight() / 2.0F);
		}
	}
}
