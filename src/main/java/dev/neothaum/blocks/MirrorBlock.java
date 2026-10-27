package dev.neothaum.blocks;

import dev.neothaum.registry.TVComponents;
import dev.neothaum.tiles.MagicMirrorBlockEntity;
import dev.neothaum.tiles.EssentiaMirrorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class MirrorBlock extends BaseEntityBlock {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
	public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

	private static final VoxelShape SHAPE_DOWN = Block.box(0, 15, 0, 16, 16, 16);
	private static final VoxelShape SHAPE_UP = Block.box(0, 0, 0, 16, 1, 16);
	private static final VoxelShape SHAPE_NORTH = Block.box(0, 0, 15, 16, 16, 16);
	private static final VoxelShape SHAPE_SOUTH = Block.box(0, 0, 0, 16, 16, 1);
	private static final VoxelShape SHAPE_WEST = Block.box(15, 0, 0, 16, 16, 16);
	private static final VoxelShape SHAPE_EAST = Block.box(0, 0, 0, 1, 16, 16);

	private final boolean essentia;

	public MirrorBlock(boolean essentia, Properties props) {
		super(props);
		this.essentia = essentia;
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP).setValue(OPEN, false));
	}

	public boolean isEssentia() {
		return this.essentia;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, OPEN);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
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
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return Shapes.empty();
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Direction face = ctx.getClickedFace();
		Level level = ctx.getLevel();
		BlockPos supportingPos = ctx.getClickedPos().relative(face.getOpposite());
		if (level.getBlockState(supportingPos).isFaceSturdy(level, supportingPos, face)) {
			return this.defaultBlockState().setValue(FACING, face).setValue(OPEN, false);
		}
		return null;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		Direction face = state.getValue(FACING);
		BlockPos supporting = pos.relative(face.getOpposite());
		return level.getBlockState(supporting).isFaceSturdy(level, supporting, face);
	}

	@Override
	public BlockState updateShape(BlockState state, LevelReader level, net.minecraft.world.level.ScheduledTickAccess ticks, BlockPos pos, Direction dir, BlockPos neighborPos, BlockState neighborState, net.minecraft.util.RandomSource random) {
		if (dir == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, level, ticks, pos, dir, neighborPos, neighborState, random);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (!level.isClientSide()) {
			TVComponents.MirrorLink link = stack.get(TVComponents.MIRROR_LINK);
			if (link != null) {
				if (!this.essentia && level.getBlockEntity(pos) instanceof MagicMirrorBlockEntity mm) {
					mm.setLink(link.dimension(), link.pos());
					mm.restoreLink();
				} else if (this.essentia && level.getBlockEntity(pos) instanceof EssentiaMirrorBlockEntity em) {
					em.setLink(link.dimension(), link.pos());
					em.restoreLink();
				}
			}
		}
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide()) {
			ItemStack drop = new ItemStack(this);
			if (!this.essentia) {
				if (level.getBlockEntity(pos) instanceof MagicMirrorBlockEntity mm) {
					if (mm.isLinkValid()) {
						drop.set(TVComponents.MIRROR_LINK, new TVComponents.MirrorLink(mm.getLinkDim(), mm.getLinkPos()));
					}
					mm.invalidateLink();
				}
			} else {
				if (level.getBlockEntity(pos) instanceof EssentiaMirrorBlockEntity em) {
					if (em.isLinkValid()) {
						drop.set(TVComponents.MIRROR_LINK, new TVComponents.MirrorLink(em.getLinkDim(), em.getLinkPos()));
					}
					em.invalidateLink();
				}
			}
			popResource(level, pos, drop);
		}
		return super.playerWillDestroy(level, pos, state, player);
	}
}
