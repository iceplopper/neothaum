package dev.neothaum.blocks;

import dev.neothaum.entity.monster.TaintSpore;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVMobEffects;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.world.TVBiomes;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * BlockTaintFibres: the purple growth of the taint. Type 0 is bare fibres (a film over every solid face around it, now and then glowing), 1 and 2
 * are taint grass, 3 is a spore stalk and 4 a stalk with a spore swelling on top. Fibres spread through the tainted land, crust logs and turn
 * soil to taint, and wither once nothing but taint holds them up or the land is cleansed.
 */
public class TaintFibreBlock extends Block {
	public static final IntegerProperty TYPE = IntegerProperty.create("type", 0, 4);
	/** BlockTaintFibres.getOverlayBlockTexture: which of the glowing taint_over textures (0 for none) this block shows. */
	public static final IntegerProperty OVERLAY = IntegerProperty.create("overlay", 0, 3);
	public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
	public static final BooleanProperty UP = BlockStateProperties.UP;
	public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
	public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
	public static final BooleanProperty WEST = BlockStateProperties.WEST;
	public static final BooleanProperty EAST = BlockStateProperties.EAST;
	private static final BooleanProperty[] FACES = {DOWN, UP, NORTH, SOUTH, WEST, EAST};

	private static final float F = 1.0F;
	private static final VoxelShape[] PLATES = {
		Block.box(0, 0, 0, 16, F, 16), Block.box(0, 16 - F, 0, 16, 16, 16), Block.box(0, 0, 0, 16, 16, F), Block.box(0, 0, 16 - F, 16, 16, 16),
		Block.box(0, 0, 0, F, 16, 16), Block.box(16 - F, 0, 0, 16, 16, 16)};
	private static final VoxelShape PLANT = Block.box(3.2, 0, 3.2, 12.8, 12.8, 12.8);

	public TaintFibreBlock(Properties props) {
		super(props);
		BlockState s = this.stateDefinition.any().setValue(TYPE, 0).setValue(OVERLAY, 0);
		for (BooleanProperty p : FACES) {
			s = s.setValue(p, false);
		}
		this.registerDefaultState(s);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(TYPE, OVERLAY, DOWN, UP, NORTH, SOUTH, WEST, EAST);
	}

	public static int lightLevel(BlockState state) {
		int t = state.getValue(TYPE);
		return t == 2 ? 8 : t == 4 ? 10 : 0;
	}

	// ------------------------------------------------------------------ shape and the faces it clings to

	/** The renderer's test: a solid face that isn't crusted taint, soil or flesh. */
	private static boolean clingsTo(BlockGetter level, BlockPos pos, Direction dir) {
		BlockPos n = pos.relative(dir);
		BlockState ns = level.getBlockState(n);
		return !(ns.getBlock() instanceof TaintBlock) && ns.isFaceSturdy(level, n, dir.getOpposite());
	}

	public BlockState withFaces(BlockGetter level, BlockPos pos, BlockState state) {
		for (int i = 0; i < 6; i++) {
			state = state.setValue(FACES[i], clingsTo(level, pos, Direction.from3DDataValue(i)));
		}
		if (state.getValue(TYPE) == 0) {
			// 95% of faces show nothing over the fibres; the rest glow with one of three patterns
			Random r = new Random(pos.getY() + (long) pos.getX() * pos.getZ());
			state = state.setValue(OVERLAY, r.nextInt(100) < 95 ? 0 : r.nextInt(3) + 1);
		} else {
			state = state.setValue(OVERLAY, 0);
		}
		return state;
	}

	public BlockState ofType(BlockGetter level, BlockPos pos, int type) {
		return this.withFaces(level, pos, this.defaultBlockState().setValue(TYPE, type));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return this.withFaces(ctx.getLevel(), ctx.getClickedPos(), this.defaultBlockState());
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir, BlockPos neighbourPos,
		BlockState neighbour, RandomSource random) {
		return this.withFaces(level, pos, state);
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, net.minecraft.world.level.redstone.Orientation orientation,
		boolean movedByPiston) {
		if (!level.isClientSide() && isOnlyAdjacentToTaint(level, pos)) {
			level.removeBlock(pos, false);
		}
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		if (state.getValue(TYPE) == 0) {
			for (int i = 0; i < 6; i++) {
				if (state.getValue(FACES[i])) {
					return PLATES[i];
				}
			}
			return PLATES[0];
		}
		return PLANT;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return Shapes.empty();
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return level.getBiome(pos).is(TVBiomes.TAINTED_LAND);
	}

	// ------------------------------------------------------------------ spreading

	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return true;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		int md = state.getValue(TYPE);
		taintBiomeSpread(level, pos, random);
		if (md == 0 && isOnlyAdjacentToTaint(level, pos) || !TVBiomes.is(level, pos, TVBiomes.TAINTED_LAND)) {
			level.removeBlock(pos, false);
			return;
		}
		BlockPos p2 = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
		if (!TVBiomes.is(level, p2, TVBiomes.TAINTED_LAND) || spreadFibres(level, p2)) {
			return;
		}
		int adjacentTaint = getAdjacentTaint(level, p2);
		BlockState bs = level.getBlockState(p2);
		if (adjacentTaint >= 2 && (bs.is(BlockTags.LOGS) || bs.is(Blocks.PUMPKIN) || bs.is(Blocks.MELON) || bs.is(Blocks.CACTUS))) {
			level.setBlock(p2, TVBlocks.CRUSTED_TAINT.defaultBlockState(), Block.UPDATE_ALL);
			level.blockEvent(p2, TVBlocks.CRUSTED_TAINT, 1, 0);
		}
		if (adjacentTaint >= 3 && !bs.isAir() && (bs.is(BlockTags.SAND) || bs.is(BlockTags.DIRT) || bs.is(Blocks.CLAY) || bs.is(Blocks.GRAVEL))) {
			level.setBlock(p2, TVBlocks.TAINTED_SOIL.defaultBlockState(), Block.UPDATE_ALL);
			level.blockEvent(p2, TVBlocks.TAINTED_SOIL, 1, 0);
		}
		if (md == 3 && random.nextInt(10) == 0 && level.isEmptyBlock(pos.above())) {
			level.setBlock(pos, state.setValue(TYPE, 4), Block.UPDATE_ALL);
			TaintSpore.spawnOnStalk(level, pos);
		} else if (md == 4 && !TaintSpore.hasSporeOn(level, pos)) {
			level.setBlock(pos, state.setValue(TYPE, 3), Block.UPDATE_ALL);
		}
	}

	/** BlockTaintFibres.spreadFibres: taint takes root here, as bare fibres or (on open ground) as grass or a spore stalk. */
	public static boolean spreadFibres(Level level, BlockPos pos) {
		BlockState bs = level.getBlockState(pos);
		if (isAdjacentToSolidBlock(level, pos) && !isOnlyAdjacentToTaint(level, pos) && bs.getFluidState().isEmpty()
			&& (bs.isAir() || bs.canBeReplaced() || bs.is(BlockTags.FLOWERS) || bs.is(BlockTags.LEAVES))) {
			RandomSource rand = level.getRandom();
			TaintFibreBlock fibres = (TaintFibreBlock) TVBlocks.TAINT_FIBRES;
			int type;
			if (rand.nextInt(10) != 0 || !level.isEmptyBlock(pos.above()) || !level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
				type = 0;
			} else if (rand.nextInt(10) < 9) {
				type = 1;
			} else if (rand.nextInt(12) < 10) {
				type = 2;
			} else {
				type = 3;
			}
			level.setBlock(pos, fibres.ofType(level, pos, type), Block.UPDATE_ALL);
			level.blockEvent(pos, fibres, 1, 0);
			return true;
		}
		return false;
	}

	/** BlockTaintFibres.taintBiomeSpread: now and then the tainted land itself creeps a block further. */
	public static void taintBiomeSpread(ServerLevel level, BlockPos pos, RandomSource rand) {
		int taintSpreadRate = 200;
		int xx = rand.nextInt(3) - 1;
		int zz = rand.nextInt(3) - 1;
		BlockPos p = pos.offset(xx, 0, zz);
		if (!TVBiomes.is(level, p, TVBiomes.TAINTED_LAND) && rand.nextInt(taintSpreadRate * 5) == 0 && getAdjacentTaint(level, pos) >= 2) {
			TVBiomes.setBiomeAt(level, p.getX(), p.getZ(), TVBiomes.TAINTED_LAND);
			level.blockEvent(pos, level.getBlockState(pos).getBlock(), 1, 0);
		}
	}

	public static int getAdjacentTaint(BlockGetter level, BlockPos pos) {
		int count = 0;
		for (Direction d : Direction.values()) {
			if (level.getBlockState(pos.relative(d)).is(TaintBlock.TAINT)) {
				count++;
			}
		}
		return count;
	}

	public static boolean isOnlyAdjacentToTaint(BlockGetter level, BlockPos pos) {
		for (Direction d : Direction.values()) {
			BlockState bs = level.getBlockState(pos.relative(d));
			if (!bs.isAir() && !bs.is(TaintBlock.TAINT)) {
				return false;
			}
		}
		return true;
	}

	/** BlockUtils.isAdjacentToSolidBlock */
	public static boolean isAdjacentToSolidBlock(BlockGetter level, BlockPos pos) {
		for (Direction d : Direction.values()) {
			BlockPos n = pos.relative(d);
			if (level.getBlockState(n).isFaceSturdy(level, n, d.getOpposite())) {
				return true;
			}
		}
		return false;
	}

	// ------------------------------------------------------------------ the rest

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean isPrecise) {
		if (!level.isClientSide() && entity instanceof LivingEntity le && !le.isInvertedHealAndHarm()) {
			if (entity instanceof Player && level.getRandom().nextInt(1000) == 0) {
				le.addEffect(new MobEffectInstance(TVMobEffects.FLUX_TAINT, 80, 0));
			} else if (!(entity instanceof Player) && level.getRandom().nextInt(500) == 0) {
				le.addEffect(new MobEffectInstance(TVMobEffects.FLUX_TAINT, 160, 0));
			}
		}
	}

	@Override
	protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
		if (id == 1) {
			if (level.isClientSide()) {
				level.playLocalSound(pos, TVSounds.ROOTS, SoundSource.BLOCKS, 0.1F, 0.9F + level.getRandom().nextFloat() * 0.2F, false);
			}
			return true;
		}
		return super.triggerEvent(state, level, pos, id, param);
	}
}
