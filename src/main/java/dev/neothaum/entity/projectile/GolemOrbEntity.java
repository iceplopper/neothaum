package dev.neothaum.entity.projectile;

import net.minecraft.util.Mth;

import dev.neothaum.entity.TVEntities;
import dev.neothaum.registry.TVSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * EntityGolemOrb: homing magic orb projectile fired by Cultist Clerics, Cultist Leaders, and Eldritch Golems.
 * Explodes with a shock sound, burst particles, and magic damage.
 */
public class GolemOrbEntity extends TCThrowable {
	private static final EntityDataAccessor<Boolean> RED = SynchedEntityData.defineId(GolemOrbEntity.class, EntityDataSerializers.BOOLEAN);

	private LivingEntity target;
	private int maxAge = 160;

	public GolemOrbEntity(EntityType<? extends GolemOrbEntity> type, Level level) {
		super(type, level);
	}

	public GolemOrbEntity(Level level, LivingEntity owner, LivingEntity target, boolean red) {
		super(TVEntities.GOLEM_ORB, level, owner, 0.66F, 3.0F);
		this.target = target;
		this.setIsRed(red);
		this.maxAge = red ? 240 : 160;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(RED, false);
	}

	public boolean isRed() {
		return this.entityData.get(RED);
	}

	public void setIsRed(boolean red) {
		this.entityData.set(RED, red);
		this.maxAge = red ? 240 : 160;
	}

	public void setTarget(LivingEntity target) {
		this.target = target;
	}

	@Override
	protected double getDefaultGravity() {
		return 0.0;
	}

	@Override
	public void tick() {
		super.tick();

		if (this.tickCount > this.maxAge) {
			this.discard();
			return;
		}

		// steer towards the target, at most a quarter block a tick each way
		if (this.target != null && this.target.isAlive()) {
			double d = this.distanceToSqr(this.target);
			double dx = (this.target.getX() - this.getX()) / d;
			double dy = (this.target.getBoundingBox().minY + this.target.getBbHeight() * 0.6 - this.getY()) / d;
			double dz = (this.target.getZ() - this.getZ()) / d;
			Vec3 m = this.getDeltaMovement();
			this.setDeltaMovement(Mth.clamp(m.x + dx * 0.2, -0.25, 0.25), Mth.clamp(m.y + dy * 0.2, -0.25, 0.25), Mth.clamp(m.z + dz * 0.2, -0.25, 0.25));
		}

		// Particle trail
		if (this.level() instanceof ServerLevel sl) {
			if (this.isRed()) {
				sl.sendParticles(ParticleTypes.CRIMSON_SPORE, this.getX(), this.getY(), this.getZ(), 2, 0.05, 0.05, 0.05, 0.01);
			} else {
				sl.sendParticles(ParticleTypes.WITCH, this.getX(), this.getY(), this.getZ(), 2, 0.05, 0.05, 0.05, 0.01);
			}
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult hit) {
		super.onHitEntity(hit);
		Entity entity = hit.getEntity();
		if (!this.level().isClientSide() && this.level() instanceof ServerLevel sl) {
			if (this.getOwner() instanceof LivingEntity owner) {
				float damage = (float) owner.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) * (this.isRed() ? 1.0F : 0.6F);
				entity.hurtServer(sl, this.damageSources().indirectMagic(this, owner), damage);
			}
		}
	}

	@Override
	protected void onHit(HitResult hit) {
		super.onHit(hit);
		if (!this.level().isClientSide() && this.level() instanceof ServerLevel sl) {
			sl.playSound(null, this.getX(), this.getY(), this.getZ(), TVSounds.SHOCK, SoundSource.HOSTILE, 1.0F,
				1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
			sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
			sl.sendParticles(this.isRed() ? ParticleTypes.FLAME : ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(), this.getZ(), 12, 0.3, 0.3, 0.3, 0.05);
		}
		this.discard();
	}
}
