package dev.neothaum.blocks;

import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.tiles.ArcaneFurnaceBlockEntity;
import dev.neothaum.tiles.ArcaneFurnaceNozzleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Containers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
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

public class ArcaneFurnaceBlock extends BaseEntityBlock {
	public enum FurnacePart implements StringRepresentable {
		CORE("core"),
		MOUTH("mouth"),
		NOZZLE("nozzle"),
		CASING("casing");

		private final String name;
		FurnacePart(String name) { this.name = name; }
		@Override public String getSerializedName() { return this.name; }
	}

	public static final EnumProperty<FurnacePart> PART = EnumProperty.create("part", FurnacePart.class);
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

	private static final VoxelShape CORE_SHAPE = box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0);

	public ArcaneFurnaceBlock(BlockBehaviour.Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(PART, FurnacePart.CASING).setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(PART, FACING);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(PART) == FurnacePart.CORE ? CORE_SHAPE : Shapes.block();
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(PART) == FurnacePart.CORE ? CORE_SHAPE : Shapes.block();
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		FurnacePart part = state.getValue(PART);
		if (part == FurnacePart.CORE) {
			return new ArcaneFurnaceBlockEntity(pos, state);
		} else if (part == FurnacePart.NOZZLE) {
			return new ArcaneFurnaceNozzleBlockEntity(pos, state);
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
			return createTickerHelper(type, TVBlockEntities.ARCANE_FURNACE, ArcaneFurnaceBlockEntity::serverTick);
		} else if (part == FurnacePart.NOZZLE) {
			return createTickerHelper(type, TVBlockEntities.ARCANE_FURNACE_NOZZLE, ArcaneFurnaceNozzleBlockEntity::serverTick);
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

			if (itemEntity.getY() <= pos.getY() + 0.4 || itemEntity.onGround()) {
				if (level.getBlockEntity(pos) instanceof ArcaneFurnaceBlockEntity furnace) {
					if (furnace.addItemsToInventory(itemEntity.getItem())) {
						itemEntity.discard();
					}
				}
			}
		} else if (entity instanceof LivingEntity living && !living.fireImmune()) {
			living.hurt(level.damageSources().lava(), 3.0F);
			living.setRemainingFireTicks(200);
		}
	}

	public static BlockPos findCore(LevelReader level, BlockPos pos) {
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				for (int dz = -1; dz <= 1; dz++) {
					BlockPos p = pos.offset(dx, dy, dz);
					BlockState s = level.getBlockState(p);
					if (s.is(TVBlocks.ARCANE_FURNACE) && s.getValue(PART) == FurnacePart.CORE) {
						return p;
					}
				}
			}
		}
		return null;
	}

	public static void restoreBlocks(Level level, BlockPos corePos, Direction facing) {
		if (level.isClientSide() || corePos == null) {
			return;
		}
		BlockPos min = corePos.offset(-1, -1, -1);
		BlockPos mouthPos = corePos.relative(facing);

		for (int yy = 0; yy < 3; yy++) {
			for (int xx = 0; xx < 3; xx++) {
				for (int zz = 0; zz < 3; zz++) {
					BlockPos p = min.offset(xx, yy, zz);
					BlockState s = level.getBlockState(p);
					if (!s.is(TVBlocks.ARCANE_FURNACE)) {
						continue;
					}
					boolean corner = (xx != 1 && zz != 1);
					boolean center = (xx == 1 && zz == 1);

					if (yy == 0) {
						level.setBlock(p, corner ? Blocks.NETHER_BRICKS.defaultBlockState() : Blocks.OBSIDIAN.defaultBlockState(), 3);
					} else if (yy == 1) {
						if (center) {
							level.setBlock(p, Blocks.LAVA.defaultBlockState(), 3);
						} else if (p.equals(mouthPos)) {
							level.setBlock(p, Blocks.IRON_BARS.defaultBlockState(), 3);
						} else if (corner) {
							level.setBlock(p, Blocks.NETHER_BRICKS.defaultBlockState(), 3);
						} else {
							level.setBlock(p, Blocks.OBSIDIAN.defaultBlockState(), 3);
						}
					} else {
						if (center) {
							level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
						} else if (corner) {
							level.setBlock(p, Blocks.NETHER_BRICKS.defaultBlockState(), 3);
						} else {
							level.setBlock(p, Blocks.OBSIDIAN.defaultBlockState(), 3);
						}
					}
					level.sendBlockUpdated(p, s, level.getBlockState(p), 3);
				}
			}
		}
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		BlockPos corePos = state.getValue(PART) == FurnacePart.CORE ? pos : findCore(level, pos);
		if (corePos != null && !level.isClientSide()) {
			Direction facing = state.getValue(FACING);
			if (state.getValue(PART) == FurnacePart.CORE) {
				if (level.getBlockEntity(pos) instanceof ArcaneFurnaceBlockEntity furnace) {
					Containers.dropContents(level, pos, furnace);
				}
				Blaze blaze = EntityTypes.BLAZE.create(level, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
				if (blaze != null) {
					blaze.setPos(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
					blaze.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 6000, 2));
					blaze.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 12000, 0));
					level.addFreshEntity(blaze);
				}
			}
			restoreBlocks(level, corePos, facing);
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (state.getValue(PART) == FurnacePart.CORE) {
			for (int a = 0; a < 3; a++) {
				double x = pos.getX() + random.nextFloat();
				double y = pos.getY() + 1.8 + random.nextFloat() * 0.4;
				double z = pos.getZ() + random.nextFloat();
				level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0.0, 0.05, 0.0);
			}
			if (random.nextInt(4) == 0) {
				level.playLocalSound(pos, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.5F, 1.0F, false);
			}
		}
	}
}
