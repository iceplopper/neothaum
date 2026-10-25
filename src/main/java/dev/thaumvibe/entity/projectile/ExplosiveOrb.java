package dev.thaumvibe.entity.projectile;

import dev.thaumvibe.entity.TVEntities;
import dev.thaumvibe.fx.FX;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** EntityExplosiveOrb: the fireball upgrade's slow-arcing, smoking orb; it explodes on contact and can be batted away. */
public class ExplosiveOrb extends TCThrowable {
	public float strength = 1.0F;
	public boolean onFire;

	public ExplosiveOrb(EntityType<? extends ExplosiveOrb> type, Level level) {
		super(type, level);
	}

	public ExplosiveOrb(Level level, LivingEntity thrower) {
		super(TVEntities.EXPLOSIVE_ORB, level, thrower, 1.5F, 1.0F);
	}

	@Override
	protected double getDefaultGravity() {
		return 0.01;
	}

	@Override
	public void tick() {
		super.tick();
		Level level = this.level();
		if (level.isClientSide()) {
			var r = this.random;
			FX.generic(level, this.xo + (r.nextFloat() - r.nextFloat()) * 0.3F, this.yo + (r.nextFloat() - r.nextFloat()) * 0.3F,
				this.zo + (r.nextFloat() - r.nextFloat()) * 0.3F, 0.0, 0.0, 0.0, 1.0F, 1.0F, 1.0F, 0.8F, false, 151, 9, 1, 7 + r.nextInt(5), 0,
				2.0F + r.nextFloat());
		}
		if (this.tickCount > 500) {
			this.discard();
		}
	}

	@Override
	protected void onHit(HitResult hit) {
		if (this.level() instanceof ServerLevel level) {
			if (hit instanceof EntityHitResult eh) {
				DamageSource src = this.getOwner() == null ? this.damageSources().source(DamageTypes.ON_FIRE, this)
					: this.damageSources().source(DamageTypes.FIREBALL, this, this.getOwner());
				eh.getEntity().hurtServer(level, src, this.strength * 1.5F);
			}
			level.explode(null, this.getX(), this.getY(), this.getZ(), this.strength, this.onFire, Level.ExplosionInteraction.NONE);
		}
		this.discard();
	}

	@Override
	public boolean isPickable() {
		return true;
	}

	/** Struck, it flies off the way the striker was looking. */
	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (source.getEntity() != null) {
			Vec3 look = source.getEntity().getLookAngle();
			this.setDeltaMovement(look.scale(0.9));
			this.needsSync = true;
			return true;
		}
		return false;
	}
}
