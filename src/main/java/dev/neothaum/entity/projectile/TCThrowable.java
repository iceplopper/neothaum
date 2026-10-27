package dev.neothaum.entity.projectile;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** The 1.7 EntityThrowable launch: from just below the thrower's eyes, a little to the side, along its look with 1.7's (tighter) scatter. */
public abstract class TCThrowable extends ThrowableProjectile {
	protected TCThrowable(EntityType<? extends TCThrowable> type, Level level) {
		super(type, level);
	}

	protected TCThrowable(EntityType<? extends TCThrowable> type, Level level, LivingEntity thrower, float speed, float inaccuracy) {
		super(type, level);
		this.setOwner(thrower);
		float yaw = thrower.getYRot();
		float pitch = thrower.getXRot();
		double x = thrower.getX() - Mth.cos(yaw / 180.0F * (float) Math.PI) * 0.16F;
		double y = thrower.getEyeY() - 0.1F;
		double z = thrower.getZ() - Mth.sin(yaw / 180.0F * (float) Math.PI) * 0.16F;
		this.setPos(x, y, z);
		this.setYRot(yaw);
		this.setXRot(pitch);
		double mx = -Mth.sin(yaw / 180.0F * (float) Math.PI) * Mth.cos(pitch / 180.0F * (float) Math.PI) * 0.4F;
		double mz = Mth.cos(yaw / 180.0F * (float) Math.PI) * Mth.cos(pitch / 180.0F * (float) Math.PI) * 0.4F;
		double my = -Mth.sin(pitch / 180.0F * (float) Math.PI) * 0.4F;
		this.heading(mx, my, mz, speed, inaccuracy);
	}

	/** EntityThrowable.setThrowableHeading. */
	public void heading(double x, double y, double z, float speed, float inaccuracy) {
		double len = Math.sqrt(x * x + y * y + z * z);
		x /= len;
		y /= len;
		z /= len;
		x += this.random.nextGaussian() * 0.0075F * inaccuracy;
		y += this.random.nextGaussian() * 0.0075F * inaccuracy;
		z += this.random.nextGaussian() * 0.0075F * inaccuracy;
		Vec3 m = new Vec3(x * speed, y * speed, z * speed);
		this.setDeltaMovement(m);
		double h = m.horizontalDistance();
		this.setYRot((float) (Mth.atan2(m.x, m.z) * 180.0 / Math.PI));
		this.setXRot((float) (Mth.atan2(m.y, h) * 180.0 / Math.PI));
		this.yRotO = this.getYRot();
		this.xRotO = this.getXRot();
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}
}
