package dev.neothaum.blocks.eldritch;

import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.tiles.eldritch.EldritchObeliskBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Monolith shaft block forming the soaring Eldritch Obelisks. */
public class EldritchObeliskBlock extends BaseEntityBlock {

	public EldritchObeliskBlock(Properties props) {
		super(props);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new EldritchObeliskBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
		return createTickerHelper(type, TVBlockEntities.ELDRITCH_OBELISK, EldritchObeliskBlockEntity::tick);
	}
}
