package dev.neothaum.blocks;

import dev.neothaum.tiles.InfusionPillarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * BlockStoneDevice metadata 3 and 4: an infusion pillar, made by wanding the arcane stone corners of an altar. The lower half carries the
 * model; break either half and both crumble back into arcane stone.
 */
public class InfusionPillarBlock extends BaseEntityBlock {
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
	public static final IntegerProperty CORNER = IntegerProperty.create("corner", 2, 5);
	// setBlockBoundsBasedOnState: the lower half's outline is its bottom half; the upper half points down into it
	private static final VoxelShape LOWER = box(0, 0, 0, 16, 8, 16);
	private static final VoxelShape UPPER = box(0, -16, 0, 16, -8, 16);

	public InfusionPillarBlock(BlockBehaviour.Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER).setValue(CORNER, 2));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HALF, CORNER);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? LOWER : UPPER;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.block();
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new InfusionPillarBlockEntity(pos, state) : null;
	}

	/** onNeighborBlockChange: each half needs the other. */
	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir, BlockPos neighborPos,
		BlockState neighbor, RandomSource random) {
		boolean lower = state.getValue(HALF) == DoubleBlockHalf.LOWER;
		if (lower && dir == Direction.UP || !lower && dir == Direction.DOWN) {
			if (!neighbor.is(this) || neighbor.getValue(HALF) == state.getValue(HALF)) {
				return Blocks.AIR.defaultBlockState();
			}
		}
		return state;
	}

	/** The other half drops its own stone as it crumbles. */
	@Override
	public BlockState playerWillDestroy(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.player.Player player) {
		BlockPos other = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
		if (!level.isClientSide() && level.getBlockState(other).is(this)) {
			level.destroyBlock(other, !player.isCreative());
		}
		return super.playerWillDestroy(level, pos, state, player);
	}
}
