package dev.neothaum.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** BlockTable metas 0/1: a plain wooden table. Wand it into an arcane worktable; use scribing tools on two for a research table. */
public class TableBlock extends Block {
	/** BlockTable.onBlockPlacedBy: meta 0 or 1, the way the table's foot runs, across the direction its placer faced. */
	public static final net.minecraft.world.level.block.state.properties.EnumProperty<net.minecraft.core.Direction.Axis> AXIS =
		net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_AXIS;

	public TableBlock(BlockBehaviour.Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, net.minecraft.core.Direction.Axis.X));
	}

	@Override
	protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AXIS);
	}

	@Override
	public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext ctx) {
		// facing north or south: the foot runs east-west (the model as drawn); facing east or west: north-south
		return this.defaultBlockState().setValue(AXIS, ctx.getHorizontalDirection().getAxis() == net.minecraft.core.Direction.Axis.Z
			? net.minecraft.core.Direction.Axis.X : net.minecraft.core.Direction.Axis.Z);
	}

	@Override
	protected BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rotation) {
		return rotation == net.minecraft.world.level.block.Rotation.CLOCKWISE_90 || rotation == net.minecraft.world.level.block.Rotation.COUNTERCLOCKWISE_90
			? state.setValue(AXIS, state.getValue(AXIS) == net.minecraft.core.Direction.Axis.X ? net.minecraft.core.Direction.Axis.Z : net.minecraft.core.Direction.Axis.X)
			: state;
	}
}
