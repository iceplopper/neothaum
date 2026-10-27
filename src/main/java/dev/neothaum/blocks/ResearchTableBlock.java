package dev.neothaum.blocks;

import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVItems;
import dev.neothaum.tiles.ResearchTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * BlockTable research table: a two-block piece. The MAIN half (where the scribing tools were used) holds the tile,
 * FACING points at the other half.
 */
public class ResearchTableBlock extends BaseEntityBlock {
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty MAIN = BooleanProperty.create("main");
	public static final BooleanProperty INK = BooleanProperty.create("ink");
	public static final BooleanProperty NOTES = BooleanProperty.create("notes");

	public ResearchTableBlock(BlockBehaviour.Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(MAIN, true).setValue(INK, false).setValue(NOTES, false));
	}


	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, MAIN, INK, NOTES);
	}

	public static void place(Level level, BlockPos main, Direction toOther) {
		level.setBlock(main, TVBlocks.RESEARCH_TABLE.defaultBlockState().setValue(FACING, toOther).setValue(MAIN, true), 3);
		level.setBlock(main.relative(toOther), TVBlocks.RESEARCH_TABLE.defaultBlockState().setValue(FACING, toOther.getOpposite()).setValue(MAIN, false), 3);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(MAIN) ? new ResearchTableBlockEntity(pos, state) : null;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, TVBlockEntities.RESEARCH_TABLE, ResearchTableBlockEntity::tick);
	}

	private static BlockPos mainPos(BlockState state, BlockPos pos) {
		return state.getValue(MAIN) ? pos : pos.relative(state.getValue(FACING));
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (player.isShiftKeyDown()) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide() && level.getBlockEntity(mainPos(state, pos)) instanceof ResearchTableBlockEntity be) {
			player.openMenu(be);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir, BlockPos neighbourPos,
		BlockState neighbourState, RandomSource random) {
		if (dir == state.getValue(FACING) && !neighbourState.is(this)) {
			// other half gone: revert to a plain table, like breaking one half of the original
			return TVBlocks.TABLE.defaultBlockState();
		}
		return state;
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && state.getValue(MAIN) && level.getBlockEntity(pos) instanceof ResearchTableBlockEntity be) {
			net.minecraft.world.Containers.dropContents(level, pos, be);
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, boolean movedByPiston) {
		super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
	}

	public static ItemStack drop() {
		return new ItemStack(TVBlocks.TABLE);
	}
}
