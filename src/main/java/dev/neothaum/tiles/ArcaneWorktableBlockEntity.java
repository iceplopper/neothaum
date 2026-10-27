package dev.neothaum.tiles;

import dev.neothaum.menu.ArcaneWorkbenchMenu;
import dev.neothaum.registry.TVBlockEntities;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Port of TileArcaneWorkbench / TileMagicWorkbench: slots 0-8 grid, 9 result (not saved), 10 wand. */
public class ArcaneWorktableBlockEntity extends TVBlockEntity implements Container, ExtendedMenuProvider<BlockPos> {
	public static final int RESULT_SLOT = 9;
	public static final int WAND_SLOT = 10;
	private final NonNullList<ItemStack> items = NonNullList.withSize(11, ItemStack.EMPTY);
	public ArcaneWorkbenchMenu eventHandler;

	public ArcaneWorktableBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.ARCANE_WORKTABLE, pos, state);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.items.clear();
		ContainerHelper.loadAllItems(input, this.items);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ItemStack result = this.items.get(RESULT_SLOT);
		this.items.set(RESULT_SLOT, ItemStack.EMPTY);
		ContainerHelper.saveAllItems(output, this.items);
		this.items.set(RESULT_SLOT, result);
	}

	@Override
	public int getContainerSize() {
		return this.items.size();
	}

	@Override
	public boolean isEmpty() {
		return this.items.stream().allMatch(ItemStack::isEmpty);
	}

	@Override
	public ItemStack getItem(int slot) {
		return this.items.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack s = ContainerHelper.removeItem(this.items, slot, amount);
		if (!s.isEmpty()) {
			changed(slot);
		}
		return s;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(this.items, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		this.items.set(slot, stack);
		changed(slot);
	}

	/** setInventorySlotContentsSoftly: change without re-evaluating the grid. */
	public void setItemSoftly(int slot, ItemStack stack) {
		this.items.set(slot, stack);
	}

	private void changed(int slot) {
		this.setChanged();
		if (slot != RESULT_SLOT && this.eventHandler != null) {
			this.eventHandler.slotsChanged(this);
		}
		if (slot == WAND_SLOT) {
			this.markForUpdate();
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(this, player);
	}

	@Override
	public void clearContent() {
		this.items.clear();
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		this.items.set(RESULT_SLOT, ItemStack.EMPTY);
		super.preRemoveSideEffects(pos, state);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("tile.blockTable.15.name");
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ArcaneWorkbenchMenu(id, inv, this);
	}

	@Override
	public BlockPos getScreenOpeningData(ServerPlayer player) {
		return this.worldPosition;
	}
}
