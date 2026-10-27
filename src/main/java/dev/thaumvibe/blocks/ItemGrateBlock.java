package dev.thaumvibe.blocks;

import dev.thaumvibe.tiles.ItemGrateBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Port of BlockMetalDevice metadata 5 & 6: Item Grate.
 * Can be opened/closed by player right-click or redstone signal.
 * When OPEN: players and mobs walk across the top, but dropped items fall straight through!
 * When CLOSED: items collide and rest on top of the grate.
 */
public class ItemGrateBlock extends Block implements EntityBlock {
	public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	private static final VoxelShape SHAPE = Block.box(0.0, 13.0, 0.0, 16.0, 16.0, 16.0);

	public ItemGrateBlock(BlockBehaviour.Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(OPEN, true).setValue(POWERED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(OPEN, POWERED);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		if (state.getValue(OPEN)) {
			if (context instanceof EntityCollisionContext ecc && ecc.getEntity() instanceof ItemEntity) {
				return Shapes.empty();
			}
		}
		return SHAPE;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		state = state.cycle(OPEN);
		level.setBlock(pos, state, 2);
		playToggleSound(level, pos, state.getValue(OPEN));
		wakeItemsAbove(level, pos, state.getValue(OPEN));
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, net.minecraft.world.level.redstone.Orientation orientation, boolean moved) {
		super.neighborChanged(state, level, pos, block, orientation, moved);
		if (!level.isClientSide()) {
			boolean powered = level.hasNeighborSignal(pos);
			if (powered != state.getValue(POWERED)) {
				// In TC4, redstone power closes the grate (open when unpowered)
				boolean newOpen = !powered;
				level.setBlock(pos, state.setValue(POWERED, powered).setValue(OPEN, newOpen), 2);
				playToggleSound(level, pos, newOpen);
				wakeItemsAbove(level, pos, newOpen);
			}
		}
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, net.minecraft.world.entity.InsideBlockEffectApplier effectApplier, boolean isPrecise) {
		if (state.getValue(OPEN) && entity instanceof ItemEntity item) {
			if (item.getDeltaMovement().y >= 0.0) {
				item.setDeltaMovement(item.getDeltaMovement().x, -0.05, item.getDeltaMovement().z);
			}
		}
	}

	private static void playToggleSound(Level level, BlockPos pos, boolean open) {
		level.playSound(null, pos, open ? SoundEvents.IRON_TRAPDOOR_OPEN : SoundEvents.IRON_TRAPDOOR_CLOSE,
			SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
	}

	private static void wakeItemsAbove(Level level, BlockPos pos, boolean open) {
		if (open && !level.isClientSide()) {
			AABB box = new AABB(pos.getX(), pos.getY() + 0.75, pos.getZ(), pos.getX() + 1.0, pos.getY() + 1.25, pos.getZ() + 1.0);
			for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, box)) {
				item.setDeltaMovement(0.0, -0.05, 0.0);
			}
		}
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ItemGrateBlockEntity(pos, state);
	}
}
