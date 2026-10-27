package dev.neothaum.entity.projectile;

import dev.neothaum.entity.TVEntities;
import dev.neothaum.fx.FX;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** EntityFrostShard: a bouncing shard of ice from the frost focus; its size follows its damage, and frosty shards slow what they hit. */
public class FrostShard extends TCThrowable {
	private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(FrostShard.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> FROSTY = SynchedEntityData.defineId(FrostShard.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> FRAGILE = SynchedEntityData.defineId(FrostShard.class, EntityDataSerializers.BOOLEAN);
	public double bounce = 0.5;
	public int bounceLimit = 3;

	public FrostShard(EntityType<? extends FrostShard> type, Level level) {
		super(type, level);
	}

	public FrostShard(Level level, LivingEntity thrower, float scatter) {
		super(TVEntities.FROST_SHARD, level, thrower, 1.5F, scatter);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(DAMAGE, 0.0F);
		builder.define(FROSTY, 0);
		builder.define(FRAGILE, false);
	}

	public float getDamage() {
		return this.entityData.get(DAMAGE);
	}

	public void setDamage(float d) {
		this.entityData.set(DAMAGE, d);
		this.refreshDimensions();
	}

	public int getFrosty() {
		return this.entityData.get(FROSTY);
	}

	public void setFrosty(int f) {
		this.entityData.set(FROSTY, f);
	}

	public boolean isFragile() {
		return this.entityData.get(FRAGILE);
	}

	public void setFragile(boolean f) {
		this.entityData.set(FRAGILE, f);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		if (DAMAGE.equals(key)) {
			this.refreshDimensions();
		}
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		float s = 0.15F + this.getDamage() * 0.15F;
		return EntityDimensions.scalable(s, s);
	}

	@Override
	protected double getDefaultGravity() {
		return this.isFragile() ? 0.015 : 0.05;
	}

	private void crack(BlockState state, int count) {
		if (this.level() instanceof ServerLevel sl) {
			for (int a = 0; a < count; a++) {
				sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), this.getX(), this.getY(), this.getZ(), 0,
					4.0 * (this.random.nextFloat() - 0.5), 0.5, (this.random.nextFloat() - 0.5) * 4.0, 1.0);
			}
		}
	}

	@Override
	protected void onHit(HitResult hit) {
		Vec3 m = this.getDeltaMovement();
		double mx = m.x, my = m.y, mz = m.z;
		Entity hitEntity = hit instanceof EntityHitResult eh ? eh.getEntity() : null;
		if (hitEntity != null) {
			int ox = Mth.floor(this.getX()) - Mth.floor(hitEntity.getX());
			int oy = Mth.floor(this.getY()) - Mth.floor(hitEntity.getY());
			int oz = Mth.floor(this.getZ()) - Mth.floor(hitEntity.getZ());
			if (oz != 0) {
				mz *= -1.0;
			}
			if (ox != 0) {
				mx *= -1.0;
			}
			if (oy != 0) {
				my *= -0.9;
			}
			mx *= 0.66;
			my *= 0.66;
			mz *= 0.66;
			this.crack(Blocks.ICE.defaultBlockState(), (int) this.getDamage());
		} else if (hit instanceof BlockHitResult bh && hit.getType() == HitResult.Type.BLOCK) {
			Direction dir = bh.getDirection();
			if (dir.getStepZ() != 0) {
				mz *= -1.0;
			}
			if (dir.getStepX() != 0) {
				mx *= -1.0;
			}
			if (dir.getStepY() != 0) {
				my *= -0.9;
			}
			BlockState bs = this.level().getBlockState(bh.getBlockPos());
			this.playSound(bs.getSoundType().getBreakSound(), 0.3F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
			this.crack(bs, (int) this.getDamage());
		}
		mx *= this.bounce;
		my *= this.bounce;
		mz *= this.bounce;
		double len = Math.sqrt(mx * mx + my * my + mz * mz);
		if (len > 0) {
			this.setPos(this.getX() - mx / len * 0.05F, this.getY() - my / len * 0.05F, this.getZ() - mz / len * 0.05F);
		}
		this.setDeltaMovement(mx, my, mz);
		this.needsSync = true;
		if (this.level() instanceof ServerLevel sl && hitEntity != null) {
			Vec3 before = hitEntity.getDeltaMovement();
			hitEntity.hurtServer(sl, this.damageSources().thrown(this, this.getOwner()), this.getDamage());
			if (hitEntity instanceof LivingEntity le && this.getFrosty() > 0) {
				le.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, this.getFrosty() - 1));
			}
			if (this.isFragile()) {
				if (hitEntity instanceof LivingEntity le) {
					le.damageCooldownTime = 0;
				}
				this.discard();
				this.playSound(Blocks.ICE.defaultBlockState().getSoundType().getBreakSound(), 0.3F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
				Vec3 after = hitEntity.getDeltaMovement();
				hitEntity.setDeltaMovement(before.add(after.subtract(before).scale(0.1)));
			}
		}
		if (this.bounceLimit-- <= 0) {
			this.discard();
			this.playSound(Blocks.ICE.defaultBlockState().getSoundType().getBreakSound(), 0.3F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
			this.crack(Blocks.ICE.defaultBlockState(), (int) (8.0F * this.getDamage()));
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide() && this.getFrosty() > 0) {
			float s = this.getDamage() / 10.0F;
			for (int a = 0; a < this.getFrosty(); a++) {
				FX.sparkle(this.level(), this.getX() - s + this.random.nextFloat() * (s * 2.0F), this.getY() - s + this.random.nextFloat() * (s * 2.0F),
					this.getZ() - s + this.random.nextFloat() * (s * 2.0F), 0.4F, 6, 0.005F);
			}
		}
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putFloat("damage", this.getDamage());
		output.putBoolean("fragile", this.isFragile());
		output.putInt("frost", this.getFrosty());
		output.putDouble("bounce", this.bounce);
		output.putInt("bounceLimit", this.bounceLimit);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.setDamage(input.getFloatOr("damage", 0.0F));
		this.setFragile(input.getBooleanOr("fragile", false));
		this.setFrosty(input.getIntOr("frost", 0));
		this.bounce = input.getDoubleOr("bounce", 0.5);
		this.bounceLimit = input.getIntOr("bounceLimit", 3);
	}
}
