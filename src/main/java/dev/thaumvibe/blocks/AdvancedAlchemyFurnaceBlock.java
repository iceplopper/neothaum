package dev.thaumvibe.blocks;

import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.tiles.AdvancedAlchemyFurnaceBlockEntity;
import dev.thaumvibe.tiles.AdvancedAlchemyFurnaceNozzleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Port of TC4 BlockAlchemyFurnace (metadata 0..4):
 * The 3x3x2 multiblock Advanced Alchemical Furnace.
 * Center bottom (CORE) contains the furnace controller and liquid reservoir (500 max vis).
 * Four bottom sides (NOZZLE) are essentia transport nozzles connecting to tubes.
 * Remaining blocks are the structural walls and corners forming the item funnel.
 */
public class AdvancedAlchemyFurnaceBlock extends BaseEntityBlock {
	public enum FurnacePart implements StringRepresentable {
		CORE("core"),
		NOZZLE("nozzle"),
		BOTTOM_CORNER("bottom_corner"),
		TOP_SIDE("top_side"),
		TOP_CORNER("top_corner");

		private final String name;

		FurnacePart(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}

	public static final EnumProperty<FurnacePart> PART = EnumProperty.create("part", FurnacePart.class);
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

	private static final VoxelShape CORE_SHAPE = box(0.0, 0.0, 0.0, 16.0, 11.2, 16.0);

	public AdvancedAlchemyFurnaceBlock(BlockBehaviour.Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(PART, FurnacePart.CORE).setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(PART, FACING);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		if (state.getValue(PART) == FurnacePart.CORE) {
			return CORE_SHAPE;
		}
		return Shapes.block();
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		FurnacePart part = state.getValue(PART);
		if (part == FurnacePart.CORE) {
			return new AdvancedAlchemyFurnaceBlockEntity(pos, state);
		} else if (part == FurnacePart.NOZZLE) {
			return new AdvancedAlchemyFurnaceNozzleBlockEntity(pos, state);
		}
		return null;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide()) {
			return null;
		}
		FurnacePart part = state.getValue(PART);
		if (part == FurnacePart.CORE) {
			return createTickerHelper(type, TVBlockEntities.ADVANCED_ALCHEMY_FURNACE, AdvancedAlchemyFurnaceBlockEntity::serverTick);
		} else if (part == FurnacePart.NOZZLE) {
			return createTickerHelper(type, TVBlockEntities.ADVANCED_ALCHEMY_FURNACE_NOZZLE, AdvancedAlchemyFurnaceNozzleBlockEntity::serverTick);
		}
		return null;
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
		if (state.getValue(PART) != FurnacePart.CORE) {
			return;
		}
		if (entity instanceof ItemEntity itemEntity) {
			double dx = pos.getX() + 0.5 - entity.getX();
			double dz = pos.getZ() + 0.5 - entity.getZ();
			entity.setDeltaMovement(entity.getDeltaMovement().add(dx * 0.05, 0.0, dz * 0.05));

			if (!level.isClientSide() && (itemEntity.getY() <= pos.getY() + 0.8 || itemEntity.onGround())) {
				if (level.getBlockEntity(pos) instanceof AdvancedAlchemyFurnaceBlockEntity furnace) {
					ItemStack stack = itemEntity.getItem();
					if (furnace.process(stack)) {
						level.playSound(null, pos, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.BLOCKS, 0.2F,
							1.0F + level.getRandom().nextFloat() * 0.4F);
						if (stack.isEmpty()) {
							itemEntity.discard();
						} else {
							itemEntity.setItem(stack);
						}
					}
				}
			}
		}
	}

	public static BlockPos findCore(LevelReader level, BlockPos pos) {
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 0; dy++) {
				for (int dz = -1; dz <= 1; dz++) {
					BlockPos p = pos.offset(dx, dy, dz);
					BlockState s = level.getBlockState(p);
					if (s.is(TVBlocks.ADVANCED_ALCHEMICAL_FURNACE) && s.getValue(PART) == FurnacePart.CORE) {
						return p;
					}
				}
			}
		}
		return null;
	}

	public static void restoreBlocks(Level level, BlockPos corePos) {
		if (level.isClientSide() || corePos == null) {
			return;
		}
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				// Layer 0 (bottom)
				BlockPos p0 = corePos.offset(dx, 0, dz);
				BlockState s0 = level.getBlockState(p0);
				if (s0.is(TVBlocks.ADVANCED_ALCHEMICAL_FURNACE)) {
					if (dx == 0 && dz == 0) {
						level.setBlock(p0, TVBlocks.ALCHEMICAL_FURNACE.defaultBlockState(), 3);
					} else {
						level.setBlock(p0, TVBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.defaultBlockState(), 3);
					}
					level.sendBlockUpdated(p0, s0, level.getBlockState(p0), 3);
				}

				// Layer 1 (top)
				BlockPos p1 = corePos.offset(dx, 1, dz);
				BlockState s1 = level.getBlockState(p1);
				if (s1.is(TVBlocks.ADVANCED_ALCHEMICAL_FURNACE)) {
					if (dx == 0 && dz == 0) {
						level.setBlock(p1, Blocks.AIR.defaultBlockState(), 3);
					} else if (dx == 0 || dz == 0) {
						level.setBlock(p1, TVBlocks.ALCHEMICAL_CONSTRUCT.defaultBlockState(), 3);
					} else {
						level.setBlock(p1, TVBlocks.ARCANE_ALEMBIC.defaultBlockState(), 3);
					}
					level.sendBlockUpdated(p1, s1, level.getBlockState(p1), 3);
				}
			}
		}
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		BlockPos corePos = state.getValue(PART) == FurnacePart.CORE ? pos : findCore(level, pos);
		if (corePos != null && !level.isClientSide()) {
			if (level.getBlockEntity(corePos) instanceof AdvancedAlchemyFurnaceBlockEntity core) {
				core.destroy = true;
			}
			restoreBlocks(level, corePos);
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return state.getValue(PART) == FurnacePart.NOZZLE;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
		if (state.getValue(PART) == FurnacePart.NOZZLE) {
			if (level.getBlockEntity(pos) instanceof AdvancedAlchemyFurnaceNozzleBlockEntity nozzle) {
				AdvancedAlchemyFurnaceBlockEntity furnace = nozzle.getFurnace();
				if (furnace != null && furnace.vis > 0) {
					float r = (float) furnace.vis / (float) furnace.maxVis;
					return Mth.floor(r * 14.0F) + 1;
				}
			}
		}
		return 0;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (state.getValue(PART) == FurnacePart.CORE) {
			if (level.getBlockEntity(pos) instanceof AdvancedAlchemyFurnaceBlockEntity furnace) {
				if (furnace.vis > 0) {
					double x = pos.getX() + random.nextFloat();
					double y = pos.getY() + 1.0;
					double z = pos.getZ() + random.nextFloat();
					level.addParticle(ParticleTypes.WITCH, x, y, z, 0.0, 0.02, 0.0);

					if (random.nextInt(50) == 0) {
						level.playLocalSound(pos, SoundEvents.LAVA_POP, SoundSource.BLOCKS,
							0.1F + random.nextFloat() * 0.1F, 0.9F + random.nextFloat() * 0.15F, false);
					}

					int q = random.nextInt(2);
					int w = random.nextInt(2);
					double ex = pos.getX() - 0.6 + random.nextFloat() * 0.2 + q * 2.0;
					double ey = pos.getY() + 2.0;
					double ez = pos.getZ() - 0.6 + random.nextFloat() * 0.2 + w * 2.0;
					level.addParticle(ParticleTypes.PORTAL, ex, ey, ez, (random.nextDouble() - 0.5) * 0.2, 0.05, (random.nextDouble() - 0.5) * 0.2);
				}
				if (furnace.heat > 100 && random.nextInt(4) == 0) {
					level.playLocalSound(pos, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.5F, 1.0F, false);
				}
			}
		}
	}
}
