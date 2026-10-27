package dev.neothaum.blocks;

import dev.neothaum.tiles.WardingStoneBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** BlockCosmeticSolid meta 3: Paving Stone of Warding prevents mobs from crossing. */
public class PavingStoneOfWardingBlock extends Block implements EntityBlock {
	public PavingStoneOfWardingBlock(BlockBehaviour.Properties props) {
		super(props);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new WardingStoneBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide() ? null : (lvl, pos, st, be) -> {
			if (be instanceof WardingStoneBlockEntity w) {
				WardingStoneBlockEntity.serverTick(lvl, pos, st, w);
			}
		};
	}
}
