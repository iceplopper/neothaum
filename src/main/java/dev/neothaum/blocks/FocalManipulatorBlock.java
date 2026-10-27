package dev.neothaum.blocks;

import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.tiles.FocalManipulatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** BlockStoneDevice 13: the focal manipulator, an arcane table with a focus turning above it. The table is its block model. */
public class FocalManipulatorBlock extends BaseEntityBlock {
	public FocalManipulatorBlock(BlockBehaviour.Properties props) {
		super(props);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new FocalManipulatorBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, TVBlockEntities.FOCAL_MANIPULATOR, FocalManipulatorBlockEntity::tick);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof FocalManipulatorBlockEntity be) {
			player.openMenu(be);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof FocalManipulatorBlockEntity be) {
			Containers.dropContents(level, pos, be);
		}
		return super.playerWillDestroy(level, pos, state, player);
	}
}
