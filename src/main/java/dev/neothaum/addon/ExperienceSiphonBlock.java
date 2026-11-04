package dev.neothaum.addon;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Experience Siphon: a half-height arcane stone plate. Use it with an empty warded jar to put the jar in; use it with an empty hand to
 * take the jar out (full or not). Hoppers can feed it empty jars from the sides and draw full ones from below. A redstone signal stops it; a
 * comparator reads how full its store is.
 */
public class ExperienceSiphonBlock extends BaseEntityBlock {
	private static final VoxelShape SHAPE = box(0, 0, 0, 16, 8, 16);

	public ExperienceSiphonBlock(Properties props) {
		super(props);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ExperienceSiphonBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide() ? null : createTickerHelper(type, ExperienceSiphon.SIPHON_ENTITY, ExperienceSiphonBlockEntity::tick);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof ExperienceSiphonBlockEntity siphon) || !ExperienceSiphon.isEmptyJar(stack)) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (!siphon.jar.isEmpty()) {
			return InteractionResult.FAIL;
		}
		if (!level.isClientSide()) {
			siphon.setItem(0, stack.copyWithCount(1));
			stack.consume(1, player);
			level.playSound(null, pos, dev.neothaum.registry.TVSounds.JAR, SoundSource.BLOCKS, 0.4F, 1.0F);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof ExperienceSiphonBlockEntity siphon) || siphon.jar.isEmpty()) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			ItemStack jar = siphon.removeItemNoUpdate(0);
			siphon.markForUpdate();
			if (!player.getInventory().add(jar)) {
				player.drop(jar, false, net.minecraft.util.Prediction.SERVER_ONLY);
			}
			level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.3F, 1.0F);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, net.minecraft.core.Direction direction) {
		return level.getBlockEntity(pos) instanceof ExperienceSiphonBlockEntity siphon
			? (int) Math.ceil(siphon.store * 15.0 / ExperienceSiphon.STORE) : 0;
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, boolean moved) {
		// its jar drops with it; the store of experience is lost
		net.minecraft.world.Containers.updateNeighboursAfterDestroy(state, level, pos);
		super.affectNeighborsAfterRemoval(state, level, pos, moved);
	}
}
