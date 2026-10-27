package dev.neothaum.blocks;

import dev.neothaum.registry.TVBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Port of TC4 Mnemonic Matrix (TileBrainbox / BlockMetalDevice metadata 12).
 * Attaches to the sides of a Thaumatorium or Thaumatorium Top block, increasing recipe capacity by +2 per matrix.
 */
public class MnemonicMatrixBlock extends Block {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

	private static final VoxelShape CORE = Block.box(3, 3, 3, 13, 13, 13);
	private static final VoxelShape SHAPE_NORTH = Shapes.or(CORE, Block.box(6, 6, 0, 10, 10, 3));
	private static final VoxelShape SHAPE_SOUTH = Shapes.or(CORE, Block.box(6, 6, 13, 10, 10, 16));
	private static final VoxelShape SHAPE_WEST = Shapes.or(CORE, Block.box(0, 6, 6, 3, 10, 10));
	private static final VoxelShape SHAPE_EAST = Shapes.or(CORE, Block.box(13, 6, 6, 16, 10, 10));

	public MnemonicMatrixBlock(BlockBehaviour.Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		Direction f = state.getValue(FACING);
		return switch (f) {
			case NORTH -> SHAPE_NORTH;
			case SOUTH -> SHAPE_SOUTH;
			case WEST -> SHAPE_WEST;
			case EAST -> SHAPE_EAST;
			default -> CORE;
		};
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Direction clickedFace = ctx.getClickedFace();
		if (!clickedFace.getAxis().isHorizontal()) {
			return null;
		}
		// Facing points towards the clicked Thaumatorium block
		Direction targetDir = clickedFace.getOpposite();
		BlockPos targetPos = ctx.getClickedPos().relative(targetDir);
		BlockState targetState = ctx.getLevel().getBlockState(targetPos);
		if (!isThaumatorium(targetState)) {
			return null;
		}
		return this.defaultBlockState().setValue(FACING, targetDir);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		Direction dir = state.getValue(FACING);
		BlockState targetState = level.getBlockState(pos.relative(dir));
		return isThaumatorium(targetState);
	}

	@Override
	public BlockState updateShape(BlockState state, LevelReader level, net.minecraft.world.level.ScheduledTickAccess tickAccess,
		BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, net.minecraft.util.RandomSource random) {
		if (direction == state.getValue(FACING) && !canSurvive(state, level, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
	}

	private static boolean isThaumatorium(BlockState state) {
		return state.is(TVBlocks.THAUMATORIUM) || state.is(TVBlocks.THAUMATORIUM_TOP);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}
}
