package dev.neothaum.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

/**
 * BlockCosmeticSolid metadata 9 and 10, the golem fetter: given a redstone signal it lights up (metadata 10), and a golem standing on a lit
 * fetter stops working until the signal goes (EntityGolemBase.onLivingUpdate).
 */
public class GolemFetterBlock extends Block {
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public GolemFetterBlock(Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(POWERED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return this.defaultBlockState().setValue(POWERED, ctx.getLevel().hasNeighborSignal(ctx.getClickedPos()));
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
		boolean powered = level.hasNeighborSignal(pos);
		if (powered != state.getValue(POWERED)) {
			level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_ALL);
		}
	}

	/** Whether a golem standing at {@code feet} is held still. */
	public static boolean holds(Level level, BlockPos feet) {
		BlockState below = level.getBlockState(feet.below());
		return below.getBlock() instanceof GolemFetterBlock && below.getValue(POWERED);
	}
}
