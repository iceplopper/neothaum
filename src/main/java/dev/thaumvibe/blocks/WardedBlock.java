package dev.thaumvibe.blocks;

import dev.thaumvibe.tiles.WardedBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * BlockWarded: a solid block sealed by the warding focus. It looks and lights like the block it holds, but no tool, explosion or mob can break
 * it; only its owner's warding focus lets it go.
 */
public class WardedBlock extends BaseEntityBlock {
	public static final IntegerProperty LIGHT = IntegerProperty.create("light", 0, 15);

	public WardedBlock(BlockBehaviour.Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(LIGHT, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIGHT);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new WardedBlockEntity(pos, state);
	}
}
