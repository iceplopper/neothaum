package dev.thaumvibe.blocks;

import dev.thaumvibe.api.aspects.EssentiaTransport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class AbstractLampBlock extends BaseEntityBlock {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

	private static final VoxelShape BODY = box(4.0, 2.0, 4.0, 12.0, 14.0, 12.0);
	private static final VoxelShape SHAPE_DOWN = Shapes.or(BODY, box(5.0, 0.0, 5.0, 11.0, 2.0, 11.0));
	private static final VoxelShape SHAPE_UP = Shapes.or(BODY, box(5.0, 14.0, 5.0, 11.0, 16.0, 11.0));
	private static final VoxelShape SHAPE_NORTH = Shapes.or(BODY, box(5.0, 5.0, 0.0, 11.0, 11.0, 2.0));
	private static final VoxelShape SHAPE_SOUTH = Shapes.or(BODY, box(5.0, 5.0, 14.0, 11.0, 11.0, 16.0));
	private static final VoxelShape SHAPE_WEST = Shapes.or(BODY, box(0.0, 5.0, 5.0, 2.0, 11.0, 11.0));
	private static final VoxelShape SHAPE_EAST = Shapes.or(BODY, box(14.0, 5.0, 5.0, 16.0, 11.0, 11.0));

	public AbstractLampBlock(BlockBehaviour.Properties props) {
		super(props);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(FACING)) {
			case DOWN -> SHAPE_DOWN;
			case UP -> SHAPE_UP;
			case NORTH -> SHAPE_NORTH;
			case SOUTH -> SHAPE_SOUTH;
			case WEST -> SHAPE_WEST;
			case EAST -> SHAPE_EAST;
		};
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		Direction facing = state.getValue(FACING);
		BlockPos support = pos.relative(facing);
		BlockState supportState = level.getBlockState(support);
		return !supportState.isAir() && (supportState.isFaceSturdy(level, support, facing.getOpposite())
			|| supportState.isSolid()
			|| level.getBlockEntity(support) instanceof EssentiaTransport);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Direction facing = ctx.getClickedFace().getOpposite();
		BlockState state = this.defaultBlockState().setValue(FACING, facing);
		return state.canSurvive(ctx.getLevel(), ctx.getClickedPos()) ? state : null;
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir, BlockPos neighbourPos,
		BlockState neighbourState, RandomSource random) {
		if (dir == state.getValue(FACING) && !state.canSurvive(level, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, level, ticks, pos, dir, neighbourPos, neighbourState, random);
	}
}
