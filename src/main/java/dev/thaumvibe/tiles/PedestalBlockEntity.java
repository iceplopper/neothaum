package dev.thaumvibe.tiles;

import dev.thaumvibe.fx.FX;
import dev.thaumvibe.registry.TVBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** TilePedestal: holds one item for the infusion altar, shown floating above it. */
public class PedestalBlockEntity extends TVBlockEntity implements WorldlyContainer {
	private static final int[] SLOTS = {0};
	private final NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);

	public PedestalBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.PEDESTAL, pos, state);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.inventory.clear();
		ContainerHelper.loadAllItems(input, this.inventory);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, this.inventory);
	}

	public ItemStack getItem() {
		return this.inventory.get(0);
	}

	/** setInventorySlotContentsFromInfusion: the result may stack past one. */
	public void setItemFromInfusion(ItemStack stack) {
		this.inventory.set(0, stack);
		this.markForUpdate();
	}

	/** Block events: 11 = an item thrown off by instability (pink sparks), 12 = infusion finished (rainbow sparks). */
	@Override
	public boolean triggerEvent(int id, int param) {
		if (id == 11 || id == 12) {
			if (this.level != null && this.level.isClientSide()) {
				for (int a = 0; a < (id == 11 ? 5 : 10); a++) {
					FX.blockSparkle(this.level, this.worldPosition.above(), id == 11 ? 12583104 : -9999);
				}
			}
			return true;
		}
		return super.triggerEvent(id, param);
	}

	// ------------------------------------------------------------------ inventory

	@Override
	public int getContainerSize() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return this.inventory.get(0).isEmpty();
	}

	@Override
	public ItemStack getItem(int slot) {
		return this.inventory.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack s = ContainerHelper.removeItem(this.inventory, slot, amount);
		this.markForUpdate();
		return s;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(this.inventory, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		this.inventory.set(slot, stack);
		stack.limitSize(this.getMaxStackSize());
		this.markForUpdate();
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(this, player);
	}

	@Override
	public void clearContent() {
		this.inventory.clear();
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return SLOTS;
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
		return this.getItem(slot).isEmpty();
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
		return true;
	}
}
