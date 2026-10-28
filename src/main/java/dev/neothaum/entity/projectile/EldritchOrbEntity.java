package dev.neothaum.entity.projectile;

import dev.neothaum.entity.TVEntities;
import dev.neothaum.fx.FX;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * EntityEldritchOrb: the Eldritch Warden's (and guardian's) blast. It flies straight, and on hitting anything bursts over two blocks around,
 * dealing two thirds of the thrower's attack as magic to every living thing but the undead, and weakening them for eight seconds.
 */
public class EldritchOrbEntity extends TCThrowable {
	public EldritchOrbEntity(EntityType<? extends EldritchOrbEntity> type, Level level) {
		super(type, level);
	}

	public EldritchOrbEntity(Level level, LivingEntity thrower) {
		super(TVEntities.ELDRITCH_ORB, level, thrower, 1.5F, 1.0F);
	}

	@Override
	protected double getDefaultGravity() {
		return 0.0;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.tickCount > 100) {
			this.discard();
		}
	}

	@Override
	protected void onHit(HitResult hit) {
		if (this.level() instanceof ServerLevel sl && this.getOwner() instanceof LivingEntity thrower) {
			float damage = (float) thrower.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.666F;
			for (Entity e : sl.getEntities(thrower, this.getBoundingBox().inflate(2.0))) {
				if (e instanceof LivingEntity le && !le.isInvertedHealAndHarm()) {
					le.hurtServer(sl, this.damageSources().indirectMagic(this, thrower), damage);
					le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 0));
				}
			}
			this.playSound(SoundEvents.FIRE_EXTINGUISH, 0.5F, 2.6F + (this.random.nextFloat() - this.random.nextFloat()) * 0.8F);
			this.level().broadcastEntityEvent(this, (byte) 16);
			this.discard();
		}
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id == 16) {
			for (int a = 0; a < 30; a++) {
				float fx = (this.random.nextFloat() - this.random.nextFloat()) * 0.3F;
				float fy = (this.random.nextFloat() - this.random.nextFloat()) * 0.3F;
				float fz = (this.random.nextFloat() - this.random.nextFloat()) * 0.3F;
				FX.wispTo(this.level(), this.getX() + fx, this.getY() + fy, this.getZ() + fz, this.getX() + fx * 8.0F, this.getY() + fy * 8.0F,
					this.getZ() + fz * 8.0F, 0.3F, 5, true, 0.02F);
			}
		} else {
			super.handleEntityEvent(id);
		}
	}
}
