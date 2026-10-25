package dev.thaumvibe.blocks;

import dev.thaumvibe.tiles.CrystalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;

/** Port of BlockCrystal (metadata 0-6): a cluster of crystals drawn by TileCrystalRenderer, pointing away from the face it was placed on. */
public class CrystalClusterBlock extends BaseEntityBlock implements dev.thaumvibe.api.InfusionStabiliser {
	/** TileCrystal.orientation: the direction the crystals point. */
	public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
	public static final int[] COLORS = {0xFFFF7E, 0xFF3C01, 0x0090FF, 0x00A000, 0xEECCFF, 0x555577, 0xFFFFFF};
	public final int type;

	public CrystalClusterBlock(int type, BlockBehaviour.Properties props) {
		super(props);
		this.type = type;
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockState state = this.defaultBlockState().setValue(FACING, ctx.getClickedFace());
		return state.canSurvive(ctx.getLevel(), ctx.getClickedPos()) ? state : null;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		Direction facing = state.getValue(FACING);
		BlockPos support = pos.relative(facing.getOpposite());
		return level.getBlockState(support).isFaceSturdy(level, support, facing);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir, BlockPos neighbourPos,
		BlockState neighbourState, RandomSource random) {
		return !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : state;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CrystalBlockEntity(pos, state);
	}

	@Override
	public boolean canStabaliseInfusion(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
		return true;
	}
}
