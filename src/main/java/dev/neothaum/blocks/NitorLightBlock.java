package dev.neothaum.blocks;

import dev.neothaum.fx.FX;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Placed nitor (BlockAiry meta 1): a floating flame that lights the area. Rendered purely with particles. */
public class NitorLightBlock extends Block {
	private static final VoxelShape SHAPE = box(5.6, 5.6, 5.6, 10.4, 10.4, 10.4);

	public NitorLightBlock(BlockBehaviour.Properties props) {
		super(props);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		// BlockAiry.randomDisplayTick (md 1): FXSparkle flame ball + small flames
		FX.wisp(level, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, 0.5F, 1.0F, 0.6F, 0.2F, false, true, 0.0F);
		if (random.nextInt(3) == 0) {
			level.addParticle(ParticleTypes.FLAME, pos.getX() + 0.5F + (random.nextFloat() - random.nextFloat()) * 0.1F, pos.getY() + 0.5F,
				pos.getZ() + 0.5F + (random.nextFloat() - random.nextFloat()) * 0.1F, 0.0, 0.01, 0.0);
		}
	}
}
