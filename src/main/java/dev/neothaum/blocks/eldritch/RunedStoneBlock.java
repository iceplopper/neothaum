package dev.neothaum.blocks.eldritch;

import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.tiles.eldritch.RunedStoneBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** BlockEldritch 10, Runed Stone: an Outer Lands trap that zaps players who come within 3 blocks. Drops nothing but 1-4 experience. */
public class RunedStoneBlock extends BaseEntityBlock {

	public RunedStoneBlock(Properties props) {
		super(props);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new RunedStoneBlockEntity(pos, state);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide() ? null : createTickerHelper(type, TVBlockEntities.RUNED_STONE, RunedStoneBlockEntity::tick);
	}

	@Override
	protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool, boolean dropExperience) {
		super.spawnAfterBreak(state, level, pos, tool, dropExperience);
		if (dropExperience) {
			this.tryDropExperience(level, pos, tool, UniformInt.of(1, 4));
		}
	}
}
