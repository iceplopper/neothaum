package dev.neothaum.blocks.eldritch;

import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.tiles.eldritch.EldritchCapBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Eldritch Cap block positioned between the node/portal and obelisk shaft. */
public class EldritchCapBlock extends BaseEntityBlock {

	public EldritchCapBlock(Properties props) {
		super(props);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new EldritchCapBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
}
