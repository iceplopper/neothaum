package dev.thaumvibe.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** EntityTaintPig: a tainted pig that runs at players, villagers and other animals. */
public class TaintPig extends TaintedBeast {
	public TaintPig(EntityType<? extends TaintPig> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.ATTACK_DAMAGE, 4.0).add(Attributes.MOVEMENT_SPEED, 0.275)
			.add(Attributes.ARMOR, 2.0);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return net.minecraft.sounds.SoundEvents.PIG_SOUNDS.get(net.minecraft.world.entity.animal.pig.PigSoundVariants.SoundSet.CLASSIC).adultSounds().ambientSound().value();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return net.minecraft.sounds.SoundEvents.PIG_SOUNDS.get(net.minecraft.world.entity.animal.pig.PigSoundVariants.SoundSet.CLASSIC).adultSounds().ambientSound().value();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return net.minecraft.sounds.SoundEvents.PIG_SOUNDS.get(net.minecraft.world.entity.animal.pig.PigSoundVariants.SoundSet.CLASSIC).adultSounds().deathSound().value();
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		this.playSound(net.minecraft.sounds.SoundEvents.PIG_SOUNDS.get(net.minecraft.world.entity.animal.pig.PigSoundVariants.SoundSet.CLASSIC).adultSounds().stepSound().value(), 0.15F, 1.0F);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		this.dropTaint(level, 3);
	}
}
