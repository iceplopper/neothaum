package dev.thaumvibe.entity.monster;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** EntityTaintChicken: a quick, leaping chicken of the taint that flutters down slowly and pecks at anything alive. */
public class TaintChicken extends TaintedBeast {
	public float flap;
	public float flapSpeed;
	public float oFlapSpeed;
	public float oFlap;
	private float flapping = 1.0F;

	public TaintChicken(EntityType<? extends TaintChicken> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 8.0).add(Attributes.ATTACK_DAMAGE, 3.0).add(Attributes.MOVEMENT_SPEED, 0.4)
			.add(Attributes.ARMOR, 2.0);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.3F));
	}

	@Override
	public void aiStep() {
		super.aiStep();
		this.oFlap = this.flap;
		this.oFlapSpeed = this.flapSpeed;
		this.flapSpeed += (this.onGround() ? -1.0F : 4.0F) * 0.3F;
		this.flapSpeed = Math.max(0.0F, Math.min(1.0F, this.flapSpeed));
		if (!this.onGround() && this.flapping < 1.0F) {
			this.flapping = 1.0F;
		}
		this.flapping *= 0.9F;
		Vec3 m = this.getDeltaMovement();
		if (!this.onGround() && m.y < 0.0) {
			this.setDeltaMovement(m.multiply(1.0, 0.9, 1.0));
		}
		this.flap += this.flapping * 2.0F;
	}

	@Override
	public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
		return false;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return net.minecraft.sounds.SoundEvents.CHICKEN_SOUNDS.get(net.minecraft.world.entity.animal.chicken.ChickenSoundVariants.SoundSet.CLASSIC).adultSounds().ambientSound().value();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return net.minecraft.sounds.SoundEvents.CHICKEN_SOUNDS.get(net.minecraft.world.entity.animal.chicken.ChickenSoundVariants.SoundSet.CLASSIC).adultSounds().hurtSound().value();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return net.minecraft.sounds.SoundEvents.CHICKEN_SOUNDS.get(net.minecraft.world.entity.animal.chicken.ChickenSoundVariants.SoundSet.CLASSIC).adultSounds().hurtSound().value();
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		this.spawnAtLocation(level, new ItemStack(this.random.nextInt(4) == 0 ? dev.thaumvibe.registry.TVItems.TAINTED_GOO
			: dev.thaumvibe.registry.TVItems.TAINT_TENDRIL), this.getBbHeight() / 2.0F);
	}
}
