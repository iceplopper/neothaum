package dev.neothaum.golem;

import dev.neothaum.entity.TVEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** EntityGolemBobber: the float a fishing golem casts, bobbing on the water until a bite or until it is reeled in; gone after 4000 ticks. */
public class GolemBobberEntity extends Entity {
	private static final EntityDataAccessor<Integer> FISHER = SynchedEntityData.defineId(GolemBobberEntity.class, EntityDataSerializers.INT);

	public GolemBobberEntity(EntityType<? extends GolemBobberEntity> type, Level level) {
		super(type, level);
	}

	/** Cast from the golem towards the water at x, y, z. */
	public GolemBobberEntity(Level level, GolemEntity fisher, BlockPos at) {
		this(TVEntities.GOLEM_BOBBER, level);
		this.entityData.set(FISHER, fisher.getId());
		double d1 = at.getX() + 0.5 - fisher.getX();
		double d3 = at.getY() + 1 - fisher.getY();
		double d5 = at.getZ() + 0.5 - fisher.getZ();
		double d7 = Math.sqrt(d1 * d1 + d3 * d3 + d5 * d5);
		this.setDeltaMovement(d1 * 0.1, d3 * 0.1 + Math.sqrt(d7) * 0.08, d5 * 0.1);
		this.setPos(fisher.getX(), fisher.getY(), fisher.getZ());
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(FISHER, -1);
	}

	public @Nullable GolemEntity getFisher() {
		return this.level().getEntity(this.entityData.get(FISHER)) instanceof GolemEntity g ? g : null;
	}

	@Override
	public void tick() {
		super.tick();
		GolemEntity fisher = this.getFisher();
		if (this.level() instanceof ServerLevel sl) {
			if (fisher == null || !fisher.isAlive()) {
				this.discard();
				return;
			}
			if (this.random.nextFloat() < 0.02F) {
				sl.sendParticles(ParticleTypes.SPLASH, this.getX() + this.random.nextFloat() - this.random.nextFloat(), this.getY() + this.random.nextFloat(),
					this.getZ() + this.random.nextFloat() - this.random.nextFloat(), 2 + this.random.nextInt(2), 0.1, 0.0, 0.1, 0.0);
			}
		}
		if (this.tickCount > 4000) {
			this.discard();
			return;
		}
		Vec3 v = this.getDeltaMovement();
		boolean inWater = this.level().getFluidState(this.blockPosition()).is(FluidTags.WATER);
		this.move(MoverType.SELF, v);
		if (!this.level().isClientSide() && (this.horizontalCollision || this.onGround()) && !inWater) {
			this.discard();
			return;
		}
		// float on the water like a fishing hook
		double d10 = 0.0;
		for (int j = 0; j < 5; j++) {
			double y = this.getBoundingBox().minY + (this.getBoundingBox().maxY - this.getBoundingBox().minY) * j / 5.0 - 0.125 + 0.125;
			if (this.level().getFluidState(BlockPos.containing(this.getX(), y, this.getZ())).is(FluidTags.WATER)) {
				d10 += 1.0 / 5.0;
			}
		}
		double vy = v.y;
		vy += 0.04 * (d10 * 2.0 - 1.0);
		float f = d10 > 0.0 ? 0.92F : 0.92F;
		if (d10 > 0.0) {
			vy *= 0.8;
		}
		this.setDeltaMovement(v.x * f, vy * 0.92, v.z * f);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		return false;
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
	}

	@Override
	public boolean shouldBeSaved() {
		return false;
	}
}
