package dev.neothaum.menu;

import dev.neothaum.api.wands.FocusItem;
import dev.neothaum.items.FocusPouchItem;
import dev.neothaum.registry.TVMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** ContainerFocusPouch: the held pouch's eighteen focus slots over the player's inventory; the pouch's own hotbar slot is locked. */
public class FocusPouchMenu extends AbstractContainerMenu {
	private final Container input = new SimpleContainer(FocusPouchItem.SIZE);
	private final Player player;
	private final int blockSlot;

	public FocusPouchMenu(int id, Inventory inv) {
		super(TVMenus.FOCUS_POUCH, id);
		this.player = inv.player;
		this.blockSlot = inv.getSelectedSlot() + 45;
		if (!this.player.level().isClientSide()) {
			var items = FocusPouchItem.getInventory(this.player.getMainHandItem());
			for (int i = 0; i < items.size(); i++) {
				this.input.setItem(i, items.get(i));
			}
		}
		for (int a = 0; a < FocusPouchItem.SIZE; a++) {
			this.addSlot(new Slot(this.input, a, 37 + a % 6 * 18, 51 + a / 6 * 18) {
				@Override
				public boolean mayPlace(ItemStack stack) {
					return stack.getItem() instanceof FocusItem;
				}
			});
		}
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 151 + i * 18));
			}
		}
		for (int i = 0; i < 9; i++) {
			this.addSlot(new Slot(inv, i, 8 + i * 18, 209));
		}
	}

	public int lockedHotbarSlot() {
		return this.blockSlot - 45;
	}

	@Override
	public ItemStack quickMoveStack(Player p, int slot) {
		if (slot == this.blockSlot) {
			return ItemStack.EMPTY;
		}
		Slot s = this.slots.get(slot);
		if (!s.hasItem()) {
			return ItemStack.EMPTY;
		}
		ItemStack in = s.getItem();
		ItemStack copy = in.copy();
		if (slot < FocusPouchItem.SIZE) {
			if (!this.moveItemStackTo(in, FocusPouchItem.SIZE, this.slots.size(), true)) {
				return ItemStack.EMPTY;
			}
		} else if (!(in.getItem() instanceof FocusItem) || !this.moveItemStackTo(in, 0, FocusPouchItem.SIZE, false)) {
			return ItemStack.EMPTY;
		}
		if (in.isEmpty()) {
			s.setByPlayer(ItemStack.EMPTY);
		} else {
			s.setChanged();
		}
		return copy;
	}

	@Override
	public void clicked(int slot, int button, ContainerInput input, Player p) {
		if (slot == this.blockSlot) {
			return;
		}
		super.clicked(slot, button, input, p);
	}

	@Override
	public boolean stillValid(Player p) {
		return p.getMainHandItem().getItem() instanceof FocusPouchItem;
	}

	@Override
	public void removed(Player p) {
		super.removed(p);
		if (!p.level().isClientSide() && p.getMainHandItem().getItem() instanceof FocusPouchItem) {
			java.util.List<ItemStack> items = new java.util.ArrayList<>();
			for (int i = 0; i < FocusPouchItem.SIZE; i++) {
				items.add(this.input.getItem(i));
			}
			FocusPouchItem.setInventory(p.getMainHandItem(), items);
		}
	}
}
