package dev.neothaum.blocks;

import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.tiles.DeconstructionTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Port of BlockTable metadata 14: Deconstruction Table.
 * Allows breaking items down to primal research aspect points.
 */
public class DeconstructionTableBlock extends Block implements EntityBlock {
	public DeconstructionTableBlock(BlockBehaviour.Properties props) {
		super(props);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new DeconstructionTableBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide()) return null;
		return type == TVBlockEntities.DECONSTRUCTION_TABLE
			? (lvl, pos, st, be) -> DeconstructionTableBlockEntity.tick(lvl, pos, st, (DeconstructionTableBlockEntity) be)
			: null;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (player.isShiftKeyDown()) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof DeconstructionTableBlockEntity be) {
			player.openMenu(be);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof DeconstructionTableBlockEntity be) {
			Containers.dropContents(level, pos, be);
		}
		return super.playerWillDestroy(level, pos, state, player);
	}
}
