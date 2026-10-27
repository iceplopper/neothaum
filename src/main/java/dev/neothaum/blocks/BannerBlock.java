package dev.neothaum.blocks;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVComponents;
import dev.neothaum.registry.TVItems;
import dev.neothaum.tiles.BannerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Port of TC4 BlockWoodenDevice metadata 8 (Thaumcraft and Cultist Banners).
 * Mounts on floor or wall, carries custom dye color and essentia aspect insignia.
 */
public class BannerBlock extends BaseEntityBlock {
	public static final BooleanProperty WALL = BooleanProperty.create("wall");
	public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

	private static final VoxelShape FLOOR_SHAPE = box(4.0, 0.0, 4.0, 12.0, 32.0, 12.0);
	private static final VoxelShape NORTH_SHAPE = box(0.0, 0.0, 12.0, 16.0, 32.0, 16.0);
	private static final VoxelShape SOUTH_SHAPE = box(0.0, 0.0, 0.0, 16.0, 32.0, 4.0);
	private static final VoxelShape WEST_SHAPE = box(12.0, 0.0, 0.0, 16.0, 32.0, 16.0);
	private static final VoxelShape EAST_SHAPE = box(0.0, 0.0, 0.0, 4.0, 32.0, 16.0);

	public BannerBlock(BlockBehaviour.Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(WALL, false)
			.setValue(ROTATION, 0)
			.setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(WALL, ROTATION, FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Direction face = ctx.getClickedFace();
		if (face.getAxis().isHorizontal()) {
			return this.defaultBlockState().setValue(WALL, true).setValue(FACING, face);
		}
		int rot = Mth.floor((double) ((180.0F + ctx.getRotation()) * 16.0F / 360.0F) + 0.5D) & 15;
		return this.defaultBlockState().setValue(WALL, false).setValue(ROTATION, rot);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		if (state.getValue(WALL)) {
			Direction attachDir = state.getValue(FACING).getOpposite();
			return !level.getBlockState(pos.relative(attachDir)).isAir();
		}
		return !level.getBlockState(pos.below()).isAir();
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
		if (!this.canSurvive(state, level, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, level, ticks, pos, dir, neighbourPos, neighbourState, random);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (level.getBlockEntity(pos) instanceof BannerBlockEntity be) {
			be.setOnWall(state.getValue(WALL));
			if (state.getValue(WALL)) {
				be.setFacing(state.getValue(FACING));
			} else {
				be.setRotation(state.getValue(ROTATION));
			}
			int color = stack.getOrDefault(TVComponents.BANNER_COLOR, -1);
			be.setColor(color);
			Aspect aspect = stack.get(TVComponents.ASPECT_FILTER);
			if (aspect != null) {
				be.setAspect(aspect);
			}
		}
	}

	@Override
	public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof BannerBlockEntity be && be.getColor() >= 0) {
			if (player.isShiftKeyDown()) {
				if (!level.isClientSide()) {
					be.setAspect(null);
					level.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
				}
				return InteractionResult.SUCCESS;
			}
			if (stack.is(TVItems.PHIAL_OF_ESSENTIA)) {
				AspectList al = stack.get(TVComponents.ASPECTS);
				if (al != null && al.size() > 0) {
					if (!level.isClientSide()) {
						be.setAspect(al.getAspects()[0]);
						if (!player.isCreative()) {
							stack.shrink(1);
							player.getInventory().add(new ItemStack(TVItems.GLASS_PHIAL));
						}
						level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
					}
					return InteractionResult.SUCCESS;
				}
			}
		}
		return super.useItemOn(stack, state, level, pos, player, hand, hit);
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && !player.isCreative()) {
			if (level.getBlockEntity(pos) instanceof BannerBlockEntity be) {
				ItemStack drop = new ItemStack(be.getColor() == -1 ? TVItems.CULTIST_BANNER : TVItems.BANNER);
				if (be.getColor() >= 0) {
					drop.set(TVComponents.BANNER_COLOR, be.getColor());
				}
				if (be.getAspect() != null) {
					drop.set(TVComponents.ASPECT_FILTER, be.getAspect());
				}
				popResource(level, pos, drop);
			}
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		if (state.getValue(WALL)) {
			return switch (state.getValue(FACING)) {
				case NORTH -> NORTH_SHAPE;
				case SOUTH -> SOUTH_SHAPE;
				case WEST -> WEST_SHAPE;
				case EAST -> EAST_SHAPE;
				default -> FLOOR_SHAPE;
			};
		}
		return FLOOR_SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BannerBlockEntity(pos, state);
	}
}
