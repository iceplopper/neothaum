package dev.neothaum.blocks;

import dev.neothaum.tiles.PedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * BlockStoneDevice metadata 1: the arcane pedestal. Click with an item to set one on it; click again to take it back. Anything placed on
 * top knocks the item off.
 */
public class PedestalBlock extends BaseEntityBlock {
	private static final VoxelShape OUTLINE = box(4, 0, 4, 12, 15.84, 12);
	/** addCollisionBoxesToList: you stand on a full block. */
	private static final VoxelShape COLLISION = Shapes.block();

	public PedestalBlock(BlockBehaviour.Properties props) {
		super(props);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return OUTLINE;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return COLLISION;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PedestalBlockEntity(pos, state);
	}

	private static float pitch(Level level, float base) {
		return ((level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.7F + 1.0F) * base;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof PedestalBlockEntity ped)) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (!ped.getItem().isEmpty()) {
			return this.useWithoutItem(state, level, pos, player, hit);
		}
		if (!level.isClientSide()) {
			ped.setItem(0, stack.copyWithCount(1));
			stack.consume(1, player);
			level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2F, pitch(level, 1.6F));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof PedestalBlockEntity ped) || ped.getItem().isEmpty()) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			// InventoryUtils.dropItemsAtEntity: the item pops out at the player's feet
			ItemStack out = ped.removeItemNoUpdate(0);
			ped.markForUpdate();
			ItemEntity e = new ItemEntity(level, player.getX(), player.getY() + player.getEyeHeight() / 2.0F, player.getZ(), out);
			e.setNoPickUpDelay();
			level.addFreshEntity(e);
			level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2F, pitch(level, 1.5F));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean moved) {
		if (!level.isClientSide() && !level.isEmptyBlock(pos.above()) && level.getBlockEntity(pos) instanceof PedestalBlockEntity ped
			&& !ped.getItem().isEmpty()) {
			Containers.dropContents(level, pos, ped);
			ped.markForUpdate();
		}
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof PedestalBlockEntity ped) {
			Containers.dropContents(level, pos, ped);
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
		return level.getBlockEntity(pos) instanceof PedestalBlockEntity ped && !ped.getItem().isEmpty() ? 15 : 0;
	}
}
