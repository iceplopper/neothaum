package dev.neothaum.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Port of BlockCandle: a coloured tallow candle (one block per colour) that always burns and needs a solid top surface below it. */
public class TallowCandleBlock extends Block implements dev.neothaum.api.InfusionStabiliser {
	/** Utils.colors, by dye colour index. */
	public static final int[] COLORS = {15790320, 15435844, 12801229, 6719955, 14602026, 4312372, 14188952, 4408131, 10526880, 2651799, 8073150,
		2437522, 5320730, 3887386, 11743532, 1973019};
	private static final VoxelShape SHAPE = box(6.0, 0.0, 6.0, 10.0, 8.0, 10.0);
	private final int colorIndex;

	public TallowCandleBlock(int color, BlockBehaviour.Properties props) {
		super(props);
		this.colorIndex = color;
	}

	/** BlockCandle.colorMultiplier: the candle's wax colour. */
	public int color() {
		return COLORS[this.colorIndex];
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return canSupportCenter(level, pos.below(), Direction.UP);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir, BlockPos neighbourPos,
		BlockState neighbourState, RandomSource random) {
		return dir == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : state;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		double x = pos.getX() + 0.5, y = pos.getY() + 0.7, z = pos.getZ() + 0.5;
		level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
		level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
	}

	@Override
	public boolean canStabaliseInfusion(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
		return true;
	}
}
