package dev.thaumvibe.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** EntityTaintCow: a huge, slow, hard-hitting cow of the taint. */
public class TaintCow extends TaintedBeast {
	public TaintCow(EntityType<? extends TaintCow> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 40.0).add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.MOVEMENT_SPEED, 0.27);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return net.minecraft.sounds.SoundEvents.COW_SOUNDS.get(net.minecraft.world.entity.animal.cow.CowSoundVariants.SoundSet.CLASSIC).ambientSound().value();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return net.minecraft.sounds.SoundEvents.COW_SOUNDS.get(net.minecraft.world.entity.animal.cow.CowSoundVariants.SoundSet.CLASSIC).hurtSound().value();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return net.minecraft.sounds.SoundEvents.COW_SOUNDS.get(net.minecraft.world.entity.animal.cow.CowSoundVariants.SoundSet.CLASSIC).hurtSound().value();
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		this.playSound(net.minecraft.sounds.SoundEvents.COW_SOUNDS.get(net.minecraft.world.entity.animal.cow.CowSoundVariants.SoundSet.CLASSIC).stepSound().value(), 0.15F, 1.0F);
	}

	@Override
	protected float getSoundVolume() {
		return 0.4F;
	}
}
