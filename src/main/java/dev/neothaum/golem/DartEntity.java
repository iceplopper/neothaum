package dev.neothaum.golem;

import dev.neothaum.entity.TVEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/** EntityDart: the little arrow a golem's dart launcher fires, trailing a puff of smoke; it can't be picked up. */
public class DartEntity extends AbstractArrow {
	private boolean first = true;

	public DartEntity(EntityType<? extends DartEntity> type, Level level) {
		super(type, level);
		this.pickup = Pickup.DISALLOWED;
	}

	/** Aimed at the target's eyes, a little high for the drop (as a skeleton aims). */
	public DartEntity(Level level, LivingEntity shooter, LivingEntity target, float speed, float inaccuracy) {
		super(TVEntities.DART, shooter, level, new ItemStack(Items.ARROW), null);
		this.pickup = Pickup.DISALLOWED;
		this.setPos(shooter.getX(), shooter.getEyeY() - 0.1F, shooter.getZ());
		double dx = target.getX() - shooter.getX();
		double dy = target.getEyeY() - 0.7F - this.getY();
		double dz = target.getZ() - shooter.getZ();
		double d = Math.sqrt(dx * dx + dz * dz);
		if (d >= 1.0E-7) {
			this.setPos(shooter.getX() + dx / d / 5.0, this.getY(), shooter.getZ() + dz / d / 5.0);
			this.shoot(dx, dy + d * 0.2F, dz, speed, inaccuracy);
		}
	}

	@Override
	public void tick() {
		if (this.first && this.level().isClientSide()) {
			this.first = false;
			var v = this.getDeltaMovement();
			for (int a = 0; a < 5; a++) {
				this.level().addParticle(ParticleTypes.SMOKE, this.getX() - v.x / 1.5, this.getY() - v.y / 1.5, this.getZ() - v.z / 1.5,
					v.x / 9.0 + this.random.nextGaussian() * 0.01, v.y / 9.0 + this.random.nextGaussian() * 0.01, v.z / 9.0 + this.random.nextGaussian() * 0.01);
			}
		}
		super.tick();
	}

	@Override
	protected ItemStack getDefaultPickupItem() {
		return new ItemStack(Items.ARROW);
	}
}
