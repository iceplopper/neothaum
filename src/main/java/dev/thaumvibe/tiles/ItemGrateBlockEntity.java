package dev.thaumvibe.tiles;

import dev.thaumvibe.blocks.ItemGrateBlock;
import dev.thaumvibe.registry.TVBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Port of TileGrate: when open, items inserted through the top (by hoppers or droppers)
 * are immediately ejected downwards underneath the grate.
 */
public class ItemGrateBlockEntity extends TVBlockEntity implements WorldlyContainer {
	private static final int[] SLOTS = new int[]{0};

	public ItemGrateBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.ITEM_GRATE, pos, state);
	}

	@Override
	public int getContainerSize() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public ItemStack getItem(int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (stack.isEmpty() || this.level == null) {
			return;
		}
		if (!this.level.isClientSide()) {
			ItemEntity item = new ItemEntity(this.level,
				this.worldPosition.getX() + 0.5,
				this.worldPosition.getY() + 0.5,
				this.worldPosition.getZ() + 0.5,
				stack.copy());
			item.setDeltaMovement(new Vec3(0.0, -0.1, 0.0));
			this.level.addFreshEntity(item);
		}
	}

	@Override
	public int getMaxStackSize() {
		return 64;
	}

	@Override
	public boolean stillValid(Player player) {
		return false;
	}

	@Override
	public void clearContent() {
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		if (side == Direction.UP && this.getBlockState().getValue(ItemGrateBlock.OPEN)) {
			return SLOTS;
		}
		return new int[0];
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
		return side == Direction.UP && this.getBlockState().getValue(ItemGrateBlock.OPEN);
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
		return false;
	}
}
