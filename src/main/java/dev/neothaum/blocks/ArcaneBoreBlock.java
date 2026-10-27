package dev.neothaum.blocks;

import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.tiles.ArcaneBoreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Port of TC4 BlockWoodenDevice metadata 5 (Arcane Bore).
 * Turret machine that must be mounted above or below an Arcane Bore Base.
 */
public class ArcaneBoreBlock extends BaseEntityBlock {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
	public static final EnumProperty<Direction> BASE_DIR = EnumProperty.create("base_dir", Direction.class, Direction.DOWN, Direction.UP);

	private static final VoxelShape SHAPE = box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

	public ArcaneBoreBlock(BlockBehaviour.Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(BASE_DIR, Direction.DOWN));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, BASE_DIR);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockState below = level.getBlockState(pos.below());
		BlockState above = level.getBlockState(pos.above());
		return below.is(TVBlocks.ARCANE_BORE_BASE) || above.is(TVBlocks.ARCANE_BORE_BASE);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockState below = level.getBlockState(pos.below());
		BlockState above = level.getBlockState(pos.above());

		Direction baseDir = Direction.DOWN;
		if (below.is(TVBlocks.ARCANE_BORE_BASE)) {
			baseDir = Direction.DOWN;
		} else if (above.is(TVBlocks.ARCANE_BORE_BASE)) {
			baseDir = Direction.UP;
		} else {
			return null;
		}

		Direction facing = ctx.getHorizontalDirection().getOpposite();
		return this.defaultBlockState().setValue(FACING, facing).setValue(BASE_DIR, baseDir);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		ArcaneBoreBlockEntity bore = new ArcaneBoreBlockEntity(pos, state);
		bore.orientation = state.getValue(FACING);
		bore.baseOrientation = state.getValue(BASE_DIR);
		return bore;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide() ? null : createTickerHelper(type, TVBlockEntities.ARCANE_BORE, ArcaneBoreBlockEntity::serverTick);
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide()) {
			if (level.getBlockEntity(pos) instanceof ArcaneBoreBlockEntity bore) {
				player.openMenu(bore);
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ArcaneBoreBlockEntity bore) {
			Containers.dropContents(level, pos, bore);
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	public void rotateFacing(Level level, BlockPos pos, Direction clickedSide) {
		Direction newFacing = clickedSide;
		BlockState state = level.getBlockState(pos);
		level.setBlock(pos, state.setValue(FACING, newFacing), 3);
		if (level.getBlockEntity(pos) instanceof ArcaneBoreBlockEntity bore) {
			bore.setOrientation(newFacing, false);
		}
	}
}
