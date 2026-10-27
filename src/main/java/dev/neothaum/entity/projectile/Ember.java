package dev.neothaum.entity.projectile;

import dev.neothaum.entity.TVEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** EntityEmber: a weightless mote of the fire focus's spray that slows and dies, setting what it touches alight. */
public class Ember extends TCThrowable {
	private static final EntityDataAccessor<Integer> DURATION = SynchedEntityData.defineId(Ember.class, EntityDataSerializers.INT);
	public int firey;
	public float damage = 1.0F;

	public Ember(EntityType<? extends Ember> type, Level level) {
		super(type, level);
	}

	public Ember(Level level, LivingEntity thrower, float scatter) {
		super(TVEntities.EMBER, level, thrower, 1.0F, scatter);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(DURATION, 20);
	}

	public int getDuration() {
		return this.entityData.get(DURATION);
	}

	public void setDuration(int d) {
		this.entityData.set(DURATION, d);
	}

	@Override
	protected double getDefaultGravity() {
		return 0.0;
	}

	@Override
	public void tick() {
		if (this.tickCount > this.getDuration()) {
			this.discard();
		}
		double damp = this.getDuration() <= 20 ? 0.95 : 0.975;
		this.setDeltaMovement(this.getDeltaMovement().scale(damp));
		if (this.onGround()) {
			this.setDeltaMovement(this.getDeltaMovement().scale(0.66));
		}
		super.tick();
	}

	@Override
	protected void onHit(HitResult hit) {
		if (this.level() instanceof ServerLevel level) {
			if (hit instanceof EntityHitResult eh) {
				var e = eh.getEntity();
				DamageSource src = this.damageSources().source(DamageTypes.FIREBALL, this, this.getOwner());
				if (!e.fireImmune() && e.hurtServer(level, src, this.damage)) {
					e.igniteForSeconds(3 + this.firey);
				}
			} else if (hit instanceof BlockHitResult bh && this.random.nextFloat() < 0.025F * this.firey) {
				BlockPos p = bh.getBlockPos().relative(bh.getDirection());
				if (level.isEmptyBlock(p)) {
					level.setBlockAndUpdate(p, Blocks.FIRE.defaultBlockState());
				}
			}
		}
		this.discard();
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putFloat("damage", this.damage);
		output.putInt("firey", this.firey);
		output.putInt("duration", this.getDuration());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.damage = input.getFloatOr("damage", 1.0F);
		this.firey = input.getIntOr("firey", 0);
		this.setDuration(input.getIntOr("duration", 20));
	}
}
