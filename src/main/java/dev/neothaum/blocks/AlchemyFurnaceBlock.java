package dev.neothaum.blocks;

import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.tiles.AlchemyFurnaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/** BlockStoneDevice metadata 0: the alchemical furnace. Its fronts glow while it burns and its top fills while it holds essentia. */
public class AlchemyFurnaceBlock extends BaseEntityBlock {
	public static final BooleanProperty LIT = BlockStateProperties.LIT;
	public static final BooleanProperty FILLED = BooleanProperty.create("filled");

	public AlchemyFurnaceBlock(BlockBehaviour.Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false).setValue(FILLED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIT, FILLED);
	}

	public static void updateState(Level level, BlockPos pos, boolean lit, boolean filled) {
		BlockState s = level.getBlockState(pos);
		if (s.getBlock() instanceof AlchemyFurnaceBlock && (s.getValue(LIT) != lit || s.getValue(FILLED) != filled)) {
			level.setBlock(pos, s.setValue(LIT, lit).setValue(FILLED, filled), Block.UPDATE_ALL);
		}
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new AlchemyFurnaceBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, TVBlockEntities.ALCHEMY_FURNACE, AlchemyFurnaceBlockEntity::tick);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof AlchemyFurnaceBlockEntity be) {
			player.openMenu(be);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof AlchemyFurnaceBlockEntity be) {
			be.dropContents();
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	/** BlockStoneDevice.randomDisplayTick: flames and smoke from the sides while it burns. */
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource r) {
		if (!state.getValue(LIT)) {
			return;
		}
		float x = pos.getX() + 0.5F;
		float y = pos.getY() + 0.2F + r.nextFloat() * 5.0F / 16.0F;
		float z = pos.getZ() + 0.5F;
		float o = 0.52F;
		float d = r.nextFloat() * 0.5F - 0.25F;
		level.addParticle(ParticleTypes.SMOKE, x - o, y, z + d, 0, 0, 0);
		level.addParticle(ParticleTypes.FLAME, x - o, y, z + d, 0, 0, 0);
		level.addParticle(ParticleTypes.SMOKE, x + o, y, z + d, 0, 0, 0);
		level.addParticle(ParticleTypes.FLAME, x + o, y, z + d, 0, 0, 0);
		level.addParticle(ParticleTypes.SMOKE, x + d, y, z - o, 0, 0, 0);
		level.addParticle(ParticleTypes.FLAME, x + d, y, z - o, 0, 0, 0);
		level.addParticle(ParticleTypes.SMOKE, x + d, y, z + o, 0, 0, 0);
		level.addParticle(ParticleTypes.FLAME, x + d, y, z + o, 0, 0, 0);
	}
}
