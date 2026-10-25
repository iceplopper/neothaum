package dev.thaumvibe.blocks;

import dev.thaumvibe.fx.FX;
import dev.thaumvibe.registry.TVSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** BlockAiry meta 10, the static field: crackling air left by the earth shock orb that zaps and slows whatever walks through it, then fades. */
public class StaticFieldBlock extends Block {
	public StaticFieldBlock(BlockBehaviour.Properties props) {
		super(props);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
		if (level instanceof ServerLevel sl) {
			entity.hurtServer(sl, sl.damageSources().magic(), 1 + level.getRandom().nextInt(2));
			if (level.getRandom().nextInt(100) == 0) {
				level.removeBlock(pos, false);
			}
		}
		entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.8, 1.0, 0.8));
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		level.removeBlock(pos, false);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource r) {
		float h = r.nextFloat() * 0.33F;
		FX.spark(level, pos.getX() + r.nextFloat(), pos.getY() + 0.1515F + h / 2.0F, pos.getZ() + r.nextFloat(), 0.33F + h,
			0.65F + r.nextFloat() * 0.1F, 1.0F, 1.0F, 0.8F);
		if (r.nextInt(50) == 0) {
			level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), TVSounds.JACOBS, SoundSource.BLOCKS, 0.5F,
				1.0F + (r.nextFloat() - r.nextFloat()) * 0.2F, false);
		}
	}
}
