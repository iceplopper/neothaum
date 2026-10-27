package dev.neothaum.entity.projectile;

import dev.neothaum.entity.TVEntities;
import dev.neothaum.entity.monster.Pech;
import dev.neothaum.fx.FX;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;

/**
 * EntityPechBlast: a pech mage's bolt, a knot of green and blue wisps that drops gently and bursts on whatever it meets, hurting and poisoning,
 * slowing or weakening everything (but pechs) within two blocks. A nightshade blast does all three.
 */
public class PechBlast extends ThrowableProjectile {
	private int strength;
	private int duration;
	private boolean nightshade;

	public PechBlast(EntityType<? extends PechBlast> type, Level level) {
		super(type, level);
	}

	public PechBlast(Level level, LivingEntity thrower, int strength, int duration, boolean nightshade) {
		super(TVEntities.PECH_BLAST, thrower.getX(), thrower.getEyeY() - 0.1F, thrower.getZ(), level);
		this.setOwner(thrower);
		this.strength = strength;
		this.duration = duration;
		this.nightshade = nightshade;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData) {
	}

	@Override
	protected double getDefaultGravity() {
		return 0.025;
	}

	@Override
	public void tick() {
		Level level = this.level();
		if (level.isClientSide()) {
			RandomSource r = level.getRandom();
			for (int a = 0; a < 3; a++) {
				double x = this.getX() + (r.nextFloat() - r.nextFloat()) * 0.2F;
				double y = this.getY() + (r.nextFloat() - r.nextFloat()) * 0.2F;
				double z = this.getZ() + (r.nextFloat() - r.nextFloat()) * 0.2F;
				FX.wispTo(level, x, y, z, x, y, z, 0.3F, 3, true, 0.02F);
				double x2 = (this.getX() + this.xo) / 2.0 + (r.nextFloat() - r.nextFloat()) * 0.2F;
				double y2 = (this.getY() + this.yo) / 2.0 + (r.nextFloat() - r.nextFloat()) * 0.2F;
				double z2 = (this.getZ() + this.zo) / 2.0 + (r.nextFloat() - r.nextFloat()) * 0.2F;
				FX.wispTo(level, x2, y2, z2, x2, y2, z2, 0.3F, 2, true, 0.02F);
				FX.sparkle(level, this.getX() + (r.nextFloat() - r.nextFloat()) * 0.1F, this.getY() + (r.nextFloat() - r.nextFloat()) * 0.1F,
					this.getZ() + (r.nextFloat() - r.nextFloat()) * 0.1F, 1.5F, 5, 0.0F);
			}
		}
		super.tick();
		if (this.tickCount > 500) {
			this.discard();
		}
	}

	@Override
	protected void onHit(HitResult hit) {
		super.onHit(hit);
		if (!(this.level() instanceof ServerLevel level)) {
			return;
		}
		Entity thrower = this.getOwner();
		for (Entity e : level.getEntities(thrower, this.getBoundingBox().inflate(2.0))) {
			if (e instanceof Pech || !(e instanceof LivingEntity le)) {
				continue;
			}
			le.hurtServer(level, this.damageSources().thrown(this, thrower), this.strength + 2);
			int time = 100 + this.duration * 40;
			if (this.nightshade) {
				le.addEffect(new MobEffectInstance(MobEffects.POISON, time, this.strength));
				le.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, time, this.strength + 1));
				le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, time, this.strength));
			} else {
				switch (this.random.nextInt(3)) {
					case 0 -> le.addEffect(new MobEffectInstance(MobEffects.POISON, time, this.strength));
					case 1 -> le.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, time, this.strength + 1));
					default -> le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, time, this.strength));
				}
			}
		}
		level.broadcastEntityEvent(this, (byte) 3);
		this.discard();
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id != 3) {
			super.handleEntityEvent(id);
			return;
		}
		Level level = this.level();
		RandomSource r = level.getRandom();
		for (int a = 0; a < 9; a++) {
			for (int type : new int[]{3, 2, 0}) {
				float fx = (r.nextFloat() - r.nextFloat()) * 0.3F;
				float fy = (r.nextFloat() - r.nextFloat()) * 0.3F;
				float fz = (r.nextFloat() - r.nextFloat()) * 0.3F;
				FX.wispTo(level, this.getX() + fx, this.getY() + fy, this.getZ() + fz, this.getX() + fx * 8.0F, this.getY() + fy * 8.0F,
					this.getZ() + fz * 8.0F, 0.3F, type, true, 0.02F);
			}
		}
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("Strength", this.strength);
		output.putInt("Duration", this.duration);
		output.putBoolean("Nightshade", this.nightshade);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.strength = input.getIntOr("Strength", 0);
		this.duration = input.getIntOr("Duration", 0);
		this.nightshade = input.getBooleanOr("Nightshade", false);
	}
}
