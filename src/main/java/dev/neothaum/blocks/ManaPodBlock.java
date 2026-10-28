package dev.neothaum.blocks;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.items.ManaBeanItem;
import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.tiles.ManaPodBlockEntity;
import dev.neothaum.world.TVBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Port of TC4 BlockManaPod:
 * Hanging plant block that grows on the underside of logs in magical biomes.
 * Stages 0-7, drops mana beans infused with primal or compound aspects.
 */
public class ManaPodBlock extends BaseEntityBlock {
	public static final IntegerProperty AGE = BlockStateProperties.AGE_7;

	private static final VoxelShape[] SHAPES = new VoxelShape[]{
		box(4.0, 12.0, 4.0, 12.0, 16.0, 12.0),
		box(4.0, 10.0, 4.0, 12.0, 16.0, 12.0),
		box(4.0, 8.0, 4.0, 12.0, 16.0, 12.0),
		box(4.0, 6.0, 4.0, 12.0, 16.0, 12.0),
		box(4.0, 5.0, 4.0, 12.0, 16.0, 12.0),
		box(4.0, 4.0, 4.0, 12.0, 16.0, 12.0),
		box(4.0, 3.0, 4.0, 12.0, 16.0, 12.0),
		box(4.0, 2.0, 4.0, 12.0, 16.0, 12.0)
	};

	public ManaPodBlock(BlockBehaviour.Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES[state.getValue(AGE)];
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	public static boolean isMagicalBiome(LevelReader level, BlockPos pos) {
		return level.getBiome(pos).is(TVBiomes.MAGICAL_FOREST) || level.getBiome(pos).is(TVBiomes.EERIE);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos above = pos.above();
		BlockState aboveState = level.getBlockState(above);
		return aboveState.is(BlockTags.LOGS) && isMagicalBiome(level, pos);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
		if (dir == Direction.UP && !this.canSurvive(state, level, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, level, ticks, pos, dir, neighbourPos, neighbourState, random);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!this.canSurvive(state, level, pos)) {
			level.destroyBlock(pos, true);
			return;
		}
		if (random.nextInt(30) == 0) {
			int age = state.getValue(AGE);
			if (age < 7) {
				age++;
				level.setBlock(pos, state.setValue(AGE, age), 3);
			}
			if (level.getBlockEntity(pos) instanceof ManaPodBlockEntity be) {
				be.checkGrowth(age);
			}
		}
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && !player.isCreative() && state.getValue(AGE) >= 2) {
			int count = (state.getValue(AGE) == 7 && level.getRandom().nextFloat() > 0.33F) ? 2 : 1;
			Aspect aspect = (level.getBlockEntity(pos) instanceof ManaPodBlockEntity be && be.getAspect() != null)
				? be.getAspect() : Aspect.PLANT;
			ItemStack drop = ManaBeanItem.of(aspect);
			drop.setCount(count);
			popResource(level, pos, drop);
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ManaPodBlockEntity(pos, state);
	}
}
