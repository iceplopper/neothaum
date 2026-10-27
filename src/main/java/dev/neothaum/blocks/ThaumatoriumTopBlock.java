package dev.neothaum.blocks;

import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVItems;
import dev.neothaum.tiles.ThaumatoriumBlockEntity;
import dev.neothaum.tiles.ThaumatoriumTopBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Port of TC4 Thaumatorium top (BlockMetalDevice metadata 11).
 * Upper half of the Thaumatorium multiblock; proxies tubes and inventory down to base block.
 */
public class ThaumatoriumTopBlock extends BaseEntityBlock {
	public ThaumatoriumTopBlock(BlockBehaviour.Properties props) {
		super(props);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ThaumatoriumTopBlockEntity(pos, state);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide() && level.getBlockEntity(pos.below()) instanceof ThaumatoriumBlockEntity be) {
			player.openMenu(be);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide()) {
			// Drops Alchemical Construct
			if (!player.isCreative()) {
				popResource(level, pos, new ItemStack(TVBlocks.ALCHEMICAL_CONSTRUCT));
			}
			// Revert base block if still present
			BlockPos below = pos.below();
			if (level.getBlockState(below).is(TVBlocks.THAUMATORIUM)) {
				level.setBlock(below, TVBlocks.ALCHEMICAL_CONSTRUCT.defaultBlockState(), Block.UPDATE_ALL);
			}
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess,
		BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, net.minecraft.util.RandomSource random) {
		// If base block below is gone, revert to Alchemical Construct
		if (direction == Direction.DOWN && !neighborState.is(TVBlocks.THAUMATORIUM)) {
			return TVBlocks.ALCHEMICAL_CONSTRUCT.defaultBlockState();
		}
		return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
	}
}
