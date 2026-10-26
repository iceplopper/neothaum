package dev.thaumvibe.blocks;

import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.tiles.VisRelayBlockEntity;
import dev.thaumvibe.tiles.WorkbenchChargerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * BlockMetalDevice 14 and 2: the vis relay, mounted on whichever face it was placed against, and the vis charge relay that sits on an arcane
 * worktable. Both are drawn by their block entity renderers.
 */
public class VisRelayBlock extends BaseEntityBlock {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
	private static final double W5 = 5.0;
	private static final double W11 = 11.0;
	private final boolean charger;

	public VisRelayBlock(boolean charger, BlockBehaviour.Properties props) {
		super(props);
		this.charger = charger;
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return this.defaultBlockState().setValue(FACING, this.charger ? Direction.UP : ctx.getClickedFace());
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		if (this.charger) {
			return box(W5, 8.0, W5, W11, 16.0, W11);
		}
		return switch (state.getValue(FACING).getOpposite()) {
			case UP -> box(W5, 8.0, W5, W11, 16.0, W11);
			case DOWN -> box(W5, 0.0, W5, W11, 8.0, W11);
			case EAST -> box(8.0, W5, W5, 16.0, W11, W11);
			case WEST -> box(0.0, W5, W5, 8.0, W11, W11);
			case SOUTH -> box(W5, W5, 8.0, W11, W11, 16.0);
			case NORTH -> box(W5, W5, 0.0, W11, W11, 8.0);
		};
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return this.charger ? new WorkbenchChargerBlockEntity(pos, state) : new VisRelayBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return this.charger ? createTickerHelper(type, TVBlockEntities.WORKBENCH_CHARGER, WorkbenchChargerBlockEntity::tick)
			: createTickerHelper(type, TVBlockEntities.VIS_RELAY, VisRelayBlockEntity::tick);
	}
}
