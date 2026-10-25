package dev.thaumvibe.blocks;

import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.tiles.BellowsBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * BlockWoodenDevice metadata 0: the arcane bellows. Its nozzle points into the block it was placed against (BlockWoodenDeviceItem); the
 * whole thing is drawn by BellowsRenderer.
 */
public class BellowsBlock extends BaseEntityBlock {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

	public BellowsBlock(BlockBehaviour.Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.DOWN));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return this.defaultBlockState().setValue(FACING, ctx.getClickedFace().getOpposite());
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		BellowsBlockEntity b = new BellowsBlockEntity(pos, state);
		b.orientation = state.getValue(FACING);
		return b;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.LivingEntity placer,
		net.minecraft.world.item.ItemStack stack) {
		if (level.getBlockEntity(pos) instanceof BellowsBlockEntity b) {
			b.orientation = state.getValue(FACING);
			BlockState target = level.getBlockState(pos.relative(b.orientation));
			b.onVanillaFurnace = target.is(Blocks.FURNACE);
			b.setChanged();
		}
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, TVBlockEntities.BELLOWS, BellowsBlockEntity::tick);
	}
}
