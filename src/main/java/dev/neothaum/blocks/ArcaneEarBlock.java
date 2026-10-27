package dev.neothaum.blocks;

import dev.neothaum.tiles.ArcaneEarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Port of BlockWoodenDevice meta 1: Arcane Ear listens for note blocks and emits redstone pulses. */
public class ArcaneEarBlock extends Block implements EntityBlock {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	private static final VoxelShape SHAPE_UP = Block.box(3.0, 0.0, 3.0, 13.0, 4.0, 13.0);
	private static final VoxelShape SHAPE_DOWN = Block.box(3.0, 12.0, 3.0, 13.0, 16.0, 13.0);
	private static final VoxelShape SHAPE_NORTH = Block.box(3.0, 3.0, 12.0, 13.0, 13.0, 16.0);
	private static final VoxelShape SHAPE_SOUTH = Block.box(3.0, 3.0, 0.0, 13.0, 13.0, 4.0);
	private static final VoxelShape SHAPE_WEST = Block.box(12.0, 3.0, 3.0, 16.0, 13.0, 13.0);
	private static final VoxelShape SHAPE_EAST = Block.box(0.0, 3.0, 3.0, 4.0, 13.0, 13.0);

	public ArcaneEarBlock(BlockBehaviour.Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP).setValue(POWERED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, POWERED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getClickedFace());
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(FACING)) {
			case DOWN -> SHAPE_DOWN;
			case UP -> SHAPE_UP;
			case NORTH -> SHAPE_NORTH;
			case SOUTH -> SHAPE_SOUTH;
			case WEST -> SHAPE_WEST;
			case EAST -> SHAPE_EAST;
		};
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof ArcaneEarBlockEntity ear) {
			if (!level.isClientSide()) {
				ear.cycleNote();
				float pitch = (float) Math.pow(2.0, (ear.getNote() - 12) / 12.0);
				level.playSound(null, pos, ear.getInstrument().getSoundEvent().value(), net.minecraft.sounds.SoundSource.RECORDS, 3.0F, pitch);
			}
			return InteractionResult.SUCCESS;
		}
		return super.useWithoutItem(state, level, pos, player, hit);
	}

	@Override
	protected boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return state.getValue(POWERED) ? 15 : 0;
	}

	@Override
	protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return state.getValue(POWERED) && state.getValue(FACING) == direction ? 15 : 0;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ArcaneEarBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide() ? null : (lvl, pos, st, be) -> {
			if (be instanceof ArcaneEarBlockEntity ear) {
				ArcaneEarBlockEntity.serverTick(lvl, pos, st, ear);
			}
		};
	}
}
