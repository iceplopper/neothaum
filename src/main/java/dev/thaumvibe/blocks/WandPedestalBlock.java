package dev.thaumvibe.blocks;

import dev.thaumvibe.items.WandItem;
import dev.thaumvibe.items.baubles.VisAmuletItem;
import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.tiles.WandPedestalBlockEntity;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * BlockStoneDevice 5 and 8: the wand recharge pedestal (click a wand or vis amulet onto it, click again to take it back) and the compound
 * recharge focus that sits on top of it, which clicks through to the pedestal below.
 */
public class WandPedestalBlock extends BaseEntityBlock {
	private static final VoxelShape PEDESTAL = box(4, 0, 4, 12, 16, 12);
	private static final VoxelShape FOCUS = box(1, 0, 1, 15, 7, 15);
	private final boolean focus;

	public WandPedestalBlock(boolean focus, BlockBehaviour.Properties props) {
		super(props);
		this.focus = focus;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return this.focus ? FOCUS : PEDESTAL;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return this.focus ? null : new WandPedestalBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return this.focus ? null : createTickerHelper(type, TVBlockEntities.WAND_PEDESTAL, WandPedestalBlockEntity::tick);
	}

	private static float pitch(Level level, float base) {
		return ((level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.7F + 1.0F) * base;
	}

	private BlockPos pedestal(BlockPos pos) {
		return this.focus ? pos.below() : pos;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult hit) {
		if (stack.is(TVBlocks.COMPOUND_RECHARGE_FOCUS.asItem())) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		BlockPos pp = this.pedestal(pos);
		if (!(level.getBlockEntity(pp) instanceof WandPedestalBlockEntity ped)) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (!ped.getItem().isEmpty()) {
			return this.useWithoutItem(state, level, pos, player, hit);
		}
		if (!(stack.getItem() instanceof WandItem) && !(stack.getItem() instanceof VisAmuletItem)) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (!level.isClientSide()) {
			ped.setItem(0, stack.copyWithCount(1));
			stack.consume(1, player);
			level.playSound(null, pp, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2F, pitch(level, 1.6F));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		BlockPos pp = this.pedestal(pos);
		if (!(level.getBlockEntity(pp) instanceof WandPedestalBlockEntity ped) || ped.getItem().isEmpty()) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			ItemStack out = ped.removeItemNoUpdate(0);
			ped.setItem(0, ItemStack.EMPTY);
			ItemEntity e = new ItemEntity(level, player.getX(), player.getY() + player.getEyeHeight() / 2.0F, player.getZ(), out);
			e.setNoPickUpDelay();
			level.addFreshEntity(e);
			level.playSound(null, pp, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2F, pitch(level, 1.5F));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && !this.focus && level.getBlockEntity(pos) instanceof WandPedestalBlockEntity ped) {
			Containers.dropContents(level, pos, ped);
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return !this.focus;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
		return level.getBlockEntity(pos) instanceof WandPedestalBlockEntity ped ? ped.comparator() : 0;
	}
}
