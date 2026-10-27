package dev.thaumvibe.blocks;

import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.registry.TVItems;
import dev.thaumvibe.tiles.ThaumatoriumBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Port of TC4 Thaumatorium base (BlockMetalDevice metadata 10).
 * Lower half of the Thaumatorium multiblock; automated multi-recipe crucible alchemy machine.
 */
public class ThaumatoriumBlock extends BaseEntityBlock {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

	public ThaumatoriumBlock(BlockBehaviour.Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ThaumatoriumBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return (lvl, pos, st, be) -> {
			if (be instanceof ThaumatoriumBlockEntity t) {
				if (lvl.isClientSide()) {
					ThaumatoriumBlockEntity.clientTick(lvl, pos, st, t);
				} else {
					ThaumatoriumBlockEntity.serverTick(lvl, pos, st, t);
				}
			}
		};
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ThaumatoriumBlockEntity be) {
			player.openMenu(be);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide()) {
			if (level.getBlockEntity(pos) instanceof ThaumatoriumBlockEntity be) {
				ItemStack stack = be.getItem(0);
				if (!stack.isEmpty()) {
					Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
				}
			}
			// Drops Alchemical Construct
			if (!player.isCreative()) {
				popResource(level, pos, new ItemStack(TVBlocks.ALCHEMICAL_CONSTRUCT));
			}
			// Turn top block back into Alchemical Construct if still present
			BlockPos above = pos.above();
			if (level.getBlockState(above).is(TVBlocks.THAUMATORIUM_TOP)) {
				level.setBlock(above, TVBlocks.ALCHEMICAL_CONSTRUCT.defaultBlockState(), Block.UPDATE_ALL);
			}
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess,
		BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, net.minecraft.util.RandomSource random) {
		// If Crucible below is gone or top block is gone, revert to Alchemical Construct
		if (direction == Direction.DOWN && !neighborState.is(TVBlocks.CRUCIBLE)) {
			return TVBlocks.ALCHEMICAL_CONSTRUCT.defaultBlockState();
		}
		if (direction == Direction.UP && !neighborState.is(TVBlocks.THAUMATORIUM_TOP)) {
			return TVBlocks.ALCHEMICAL_CONSTRUCT.defaultBlockState();
		}
		return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
	}
}
