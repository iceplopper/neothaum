package dev.neothaum.blocks.eldritch;

import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVItems;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.tiles.eldritch.AncientLockBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * BlockEldritch 8, Ancient Locking Mechanism: guards an Outer Lands boss room. Fitting it with a Runed Tablet sets it working, and five
 * seconds later the room beyond wakes and the way in opens. FACING is the side that shows the keyhole, toward the corridor.
 */
public class AncientLockBlock extends BaseEntityBlock {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

	public AncientLockBlock(Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new AncientLockBlockEntity(pos, state);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, TVBlockEntities.ANCIENT_LOCK, AncientLockBlockEntity::tick);
	}

	/** BlockEldritch.onBlockActivated (meta 8): a Runed Tablet sets the lock working. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult hit) {
		if (!stack.is(TVItems.RUNED_TABLET) || !(level.getBlockEntity(pos) instanceof AncientLockBlockEntity lock) || lock.count >= 0) {
			return super.useItemOn(stack, state, level, pos, player, hand, hit);
		}
		if (!level.isClientSide()) {
			lock.count = 0;
			lock.setChanged();
			level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
			stack.consume(1, player);
			level.playSound(null, pos, TVSounds.RUNICSHIELDCHARGE, SoundSource.BLOCKS, 1.0F, 1.0F);
		}
		return InteractionResult.SUCCESS;
	}
}
