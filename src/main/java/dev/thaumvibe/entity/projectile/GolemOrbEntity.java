package dev.thaumvibe.entity.projectile;

import dev.thaumvibe.entity.TVEntities;
import dev.thaumvibe.registry.TVSounds;
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

		// Homing steering toward target
		if (!this.level().isClientSide() && this.target != null && this.target.isAlive()) {
			Vec3 currentPos = this.position();
			Vec3 targetPos = this.target.position().add(0, this.target.getBbHeight() * 0.5, 0);
			Vec3 dir = targetPos.subtract(currentPos);
			double dist = dir.length();
			if (dist > 0.01) {
				Vec3 norm = dir.scale(1.0 / dist);
				Vec3 motion = this.getDeltaMovement();
				double steer = 0.15;
				Vec3 newMotion = motion.scale(0.85).add(norm.scale(steer * 0.66));
				this.setDeltaMovement(newMotion);
			}
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
			float damage = this.isRed() ? 7.0F : 5.0F;
			entity.hurtServer(sl, this.damageSources().indirectMagic(this, this.getOwner()), damage);
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
