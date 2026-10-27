package dev.thaumvibe.menu;

import dev.thaumvibe.items.foci.ExcavationFocusItem;
import dev.thaumvibe.registry.TVMenus;
import dev.thaumvibe.tiles.ArcaneBoreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Port of TC4 ContainerArcaneBore:
 * Left slot (26, 18): Excavation Focus.
 * Right slot (74, 18): Pickaxe.
 */
public class ArcaneBoreMenu extends AbstractContainerMenu {
	public final Container bore;
	public final Player player;
	public final BlockPos pos;
	public final ContainerData data;

	public ArcaneBoreMenu(int id, Inventory inv, BlockPos pos) {
		this(id, inv, inv.player.level().getBlockEntity(pos) instanceof Container c ? c : new SimpleContainer(2), pos,
			inv.player.level().getBlockEntity(pos) instanceof ArcaneBoreBlockEntity b ? b.data : new SimpleContainerData(6));
	}

	public ArcaneBoreMenu(int id, Inventory inv, Container bore, BlockPos pos, ContainerData data) {
		super(TVMenus.ARCANE_BORE, id);
		this.bore = bore;
		this.player = inv.player;
		this.pos = pos;
		this.data = data;
		this.addDataSlots(data);

		// Slot 0: Excavation Focus only
		this.addSlot(new Slot(bore, 0, 26, 18) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.getItem() instanceof ExcavationFocusItem;
			}
		});

		// Slot 1: Pickaxe only
		this.addSlot(new Slot(bore, 1, 74, 18) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.is(ItemTags.PICKAXES);
			}
		});

		// Player 3x9 inventory at (8, 59)
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 59 + i * 18));
			}
		}

		// Player 1x9 hotbar at (8, 117)
		for (int i = 0; i < 9; i++) {
			this.addSlot(new Slot(inv, i, 8 + i * 18, 117));
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return this.bore.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex) {
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = this.slots.get(slotIndex);
		if (slot != null && slot.hasItem()) {
			ItemStack inSlot = slot.getItem();
			stack = inSlot.copy();

			if (slotIndex < 2) {
				if (!this.moveItemStackTo(inSlot, 2, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else {
				if (inSlot.getItem() instanceof ExcavationFocusItem) {
					if (!this.moveItemStackTo(inSlot, 0, 1, false)) {
						return ItemStack.EMPTY;
					}
				} else if (inSlot.is(ItemTags.PICKAXES)) {
					if (!this.moveItemStackTo(inSlot, 1, 2, false)) {
						return ItemStack.EMPTY;
					}
				} else if (slotIndex < 29) {
					if (!this.moveItemStackTo(inSlot, 29, 38, false)) {
						return ItemStack.EMPTY;
					}
				} else if (!this.moveItemStackTo(inSlot, 2, 29, false)) {
					return ItemStack.EMPTY;
				}
			}

			if (inSlot.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}
		return stack;
	}
}
