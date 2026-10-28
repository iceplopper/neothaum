package dev.neothaum.entity.projectile;

import dev.neothaum.entity.TVEntities;
import dev.neothaum.registry.TVItems;
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
 * Air: magic damage that ignores armour.
 * Fire: ignites target for 5s.
 * Water: inflicts Slowness V for 10s.
 * Earth: 1.5x damage.
 * Order: magic damage that ignores armour, and Weakness V for 10s.
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

	/** EntityPrimalArrow.getDamage: 2.1, Earth 1.5x, Order and Entropy 0.8x. */
	private double primalDamage = 2.1;

	private void applyDamageScaling() {
		this.primalDamage = switch (this.getPrimalType()) {
			case 3 -> 3.15;
			case 4, 5 -> 1.68;
			default -> 2.1;
		};
		this.setBaseDamage(this.primalDamage);
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

	private static final net.minecraft.resources.ResourceKey<net.minecraft.world.damagesource.DamageType> AIR_ARROW = net.minecraft.resources.ResourceKey
		.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE, dev.neothaum.NeoThaum.id("airarrow"));
	private static final net.minecraft.resources.ResourceKey<net.minecraft.world.damagesource.DamageType> ORDER_ARROW = net.minecraft.resources.ResourceKey
		.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE, dev.neothaum.NeoThaum.id("orderarrow"));

	@Override
	protected void onHitEntity(EntityHitResult result) {
		Entity target = result.getEntity();
		int type = this.getPrimalType();
		if (target instanceof LivingEntity living) {
			switch (type) {
				case 1 -> living.igniteForSeconds(5);
				case 2 -> living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 4));
				case 4 -> living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 4));
				case 5 -> living.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0));
			}
		}
		if ((type == 0 || type == 4) && this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
			// "airarrow" / "orderarrow": magic damage that ignores armour, but otherwise an arrow's hit
			Entity owner = this.getOwner();
			var damageType = sl.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
				.getOrThrow(type == 0 ? AIR_ARROW : ORDER_ARROW);
			var source = new net.minecraft.world.damagesource.DamageSource(damageType, this, owner == null ? this : owner);
			int damage = net.minecraft.util.Mth.ceil(net.minecraft.util.Mth.clamp(this.getDeltaMovement().length() * this.primalDamage, 0.0, 2.147483647E9));
			if (this.isCritArrow()) {
				damage = (int) Math.min((long) this.random.nextInt(damage / 2 + 2) + damage, Integer.MAX_VALUE);
			}
			target.hurtServer(sl, source, damage);
			this.playSound(net.minecraft.sounds.SoundEvents.ARROW_HIT, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
			this.discard();
			return;
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
