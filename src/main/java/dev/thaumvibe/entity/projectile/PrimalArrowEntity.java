package dev.thaumvibe.entity.projectile;

import dev.thaumvibe.entity.TVEntities;
import dev.thaumvibe.registry.TVItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Port of EntityPrimalArrow:
 * Arrows infused with the six primal aspects.
 * Air: penetrates armor.
 * Fire: ignites target for 5s.
 * Water: inflicts Slowness IV for 10s.
 * Earth: 1.5x damage.
 * Order: inflicts Weakness IV for 10s.
 * Entropy: inflicts Wither for 5s.
 */
public class PrimalArrowEntity extends AbstractArrow {
	private static final EntityDataAccessor<Integer> PRIMAL_TYPE = SynchedEntityData.defineId(PrimalArrowEntity.class, EntityDataSerializers.INT);

	public PrimalArrowEntity(EntityType<? extends PrimalArrowEntity> type, Level level) {
		super(type, level);
		this.pickup = Pickup.DISALLOWED;
	}

	public PrimalArrowEntity(Level level, LivingEntity shooter, ItemStack pickup, ItemStack weapon, int primalType) {
		super(TVEntities.PRIMAL_ARROW, shooter, level, pickup, weapon);
		this.pickup = Pickup.DISALLOWED;
		this.setPrimalType(primalType);
	}

	public PrimalArrowEntity(Level level, double x, double y, double z, ItemStack pickup, ItemStack weapon, int primalType) {
		super(TVEntities.PRIMAL_ARROW, x, y, z, level, pickup, weapon);
		this.pickup = Pickup.DISALLOWED;
		this.setPrimalType(primalType);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(PRIMAL_TYPE, 0);
	}

	public int getPrimalType() {
		return this.entityData.get(PRIMAL_TYPE);
	}

	public void setPrimalType(int type) {
		this.entityData.set(PRIMAL_TYPE, type);
		this.applyDamageScaling();
	}

	private void applyDamageScaling() {
		switch (this.getPrimalType()) {
			case 3 -> this.setBaseDamage(3.15); // Earth: 1.5x damage
			case 4, 5 -> this.setBaseDamage(1.68); // Order, Entropy: 0.8x damage
			default -> this.setBaseDamage(2.1); // Air, Fire, Water: 2.1
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (this.isInGround() && this.inGroundTime >= 100) {
			this.discard();
		}
		if (this.level().isClientSide() && !this.isInGround()) {
			this.spawnFlightParticles();
		}
	}

	private void spawnFlightParticles() {
		double px = this.getX();
		double py = this.getY();
		double pz = this.getZ();
		switch (this.getPrimalType()) {
			case 0 -> this.level().addParticle(ParticleTypes.CLOUD, px, py, pz, 0, 0, 0);
			case 1 -> this.level().addParticle(ParticleTypes.FLAME, px, py, pz, 0, 0, 0);
			case 2 -> this.level().addParticle(ParticleTypes.SPLASH, px, py, pz, 0, 0, 0);
			case 4 -> this.level().addParticle(ParticleTypes.CRIT, px, py, pz, 0, 0, 0);
			case 5 -> this.level().addParticle(ParticleTypes.SMOKE, px, py, pz, 0, 0, 0);
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		Entity target = result.getEntity();
		if (target instanceof LivingEntity living) {
			switch (this.getPrimalType()) {
				case 1 -> living.igniteForSeconds(5);
				case 2 -> living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 3));
				case 4 -> living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 3));
				case 5 -> living.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0));
			}
		}
		super.onHitEntity(result);
	}

	@Override
	protected ItemStack getDefaultPickupItem() {
		return switch (this.getPrimalType()) {
			case 0 -> new ItemStack(TVItems.PRIMAL_ARROW_AIR);
			case 1 -> new ItemStack(TVItems.PRIMAL_ARROW_FIRE);
			case 2 -> new ItemStack(TVItems.PRIMAL_ARROW_WATER);
			case 3 -> new ItemStack(TVItems.PRIMAL_ARROW_EARTH);
			case 4 -> new ItemStack(TVItems.PRIMAL_ARROW_ORDER);
			default -> new ItemStack(TVItems.PRIMAL_ARROW_ENTROPY);
		};
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("primalType", this.getPrimalType());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.setPrimalType(input.getIntOr("primalType", 0));
	}
}
