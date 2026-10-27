package dev.thaumvibe.blocks;

import dev.thaumvibe.fx.FX;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BlockCosmeticSolid meta 2: Paving Stone of Travel.
 * Entities walking on this stone receive Speed II (amplifier 1) and Jump Boost I (amplifier 0) for 40 ticks (2 seconds),
 * accompanied by sparkle effects.
 */
public class PavingStoneOfTravelBlock extends Block {
	public PavingStoneOfTravelBlock(BlockBehaviour.Properties props) {
		super(props);
	}

	@Override
	public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
		if (entity instanceof LivingEntity living) {
			if (!level.isClientSide()) {
				living.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 1));
				living.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 40, 0));
			} else {
				FX.blockSparkle(level, pos, 32768);
			}
		}
		super.stepOn(level, pos, state, entity);
	}
}
