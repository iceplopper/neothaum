package dev.neothaum.entity.projectile;

import dev.neothaum.entity.TVEntities;
import dev.neothaum.fx.FX;
import dev.neothaum.registry.TVItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * Port of EntityAlumentum:
 * Thrown explosive alchemical fuel. Flies with velocity 0.75, emits wisp and sparkle particles,
 * and causes an explosion with strength 1.66F on impact respecting mobGriefing.
 */
public class AlumentumEntity extends ThrowableItemProjectile {
	public AlumentumEntity(EntityType<? extends AlumentumEntity> type, Level level) {
		super(type, level);
	}

	public AlumentumEntity(Level level, LivingEntity thrower, ItemStack stack) {
		super(TVEntities.ALUMENTUM, thrower, level, stack);
	}

	public AlumentumEntity(Level level, double x, double y, double z, ItemStack stack) {
		super(TVEntities.ALUMENTUM, x, y, z, level, stack);
	}

	@Override
	protected Item getDefaultItem() {
		return TVItems.ALUMENTUM;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide()) {
			for (int a = 0; a < 3; a++) {
				double rx = (this.random.nextFloat() - this.random.nextFloat()) * 0.3;
				double ry = (this.random.nextFloat() - this.random.nextFloat()) * 0.3;
				double rz = (this.random.nextFloat() - this.random.nextFloat()) * 0.3;
				FX.wisp(this.level(), this.getX() + rx, this.getY() + ry, this.getZ() + rz, 0.3F, 1.0F, 0.5F, 0.1F, true, true, 0.02F);
				double x2 = (this.getX() + this.xo) / 2.0 + (this.random.nextFloat() - this.random.nextFloat()) * 0.3;
				double y2 = (this.getY() + this.yo) / 2.0 + (this.random.nextFloat() - this.random.nextFloat()) * 0.3;
				double z2 = (this.getZ() + this.zo) / 2.0 + (this.random.nextFloat() - this.random.nextFloat()) * 0.3;
				FX.wisp(this.level(), x2, y2, z2, 0.3F, 1.0F, 0.5F, 0.1F, true, true, 0.02F);
				FX.sparkle(this.level(), this.getX() + (this.random.nextFloat() - this.random.nextFloat()) * 0.1,
					this.getY() + (this.random.nextFloat() - this.random.nextFloat()) * 0.1,
					this.getZ() + (this.random.nextFloat() - this.random.nextFloat()) * 0.1,
					1.0F, 6, 0.0F);
			}
			this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
		}
	}

	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);
		if (this.level() instanceof ServerLevel sl) {
			boolean grief = Boolean.TRUE.equals(sl.getGameRules().get(GameRules.MOB_GRIEFING));
			this.level().explode(this, this.getX(), this.getY(), this.getZ(), 1.66F, grief ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
			this.discard();
		}
	}
}
