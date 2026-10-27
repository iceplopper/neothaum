package dev.thaumvibe.menu;

import dev.thaumvibe.registry.TVItems;
import dev.thaumvibe.registry.TVMenus;
import dev.thaumvibe.tiles.SpaBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SpaMenu extends AbstractContainerMenu {
	public final Container tile;
	public final Player player;
	public final BlockPos pos;
	public final SpaBlockEntity spa;
	public final ContainerData data;

	public SpaMenu(int id, Inventory inv, BlockPos pos) {
		this(id, inv, inv.player.level().getBlockEntity(pos) instanceof Container c ? c : new SimpleContainer(1), pos,
			inv.player.level().getBlockEntity(pos) instanceof SpaBlockEntity s ? s.data : new SimpleContainerData(3));
	}

	public SpaMenu(int id, Inventory inv, Container tile, BlockPos pos, ContainerData data) {
		super(TVMenus.SPA, id);
		this.tile = tile;
		this.player = inv.player;
		this.pos = pos;
		this.spa = inv.player.level().getBlockEntity(pos) instanceof SpaBlockEntity s ? s : null;
		this.data = data;
		this.addDataSlots(data);

		// Slot 0: Bath salts only at (65, 31)
		this.addSlot(new Slot(tile, 0, 65, 31) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.is(TVItems.PURIFYING_BATH_SALTS);
			}
		});

		// Player 3x9 inventory at (8, 84)
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		// Player 1x9 hotbar at (8, 142)
		for (int i = 0; i < 9; i++) {
			this.addSlot(new Slot(inv, i, 8 + i * 18, 142));
		}
	}

	@Override
	public boolean clickMenuButton(Player player, int id) {
		if (id == 1 && spa != null) {
			spa.toggleMix();
			return true;
		}
		return false;
	}

	@Override
	public boolean stillValid(Player player) {
		return this.tile.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(slotIndex);
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (slotIndex == 0) {
				if (!this.moveItemStackTo(itemstack1, 1, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (itemstack1.is(TVItems.PURIFYING_BATH_SALTS)) {
				if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (slotIndex >= 1 && slotIndex < 28) {
				if (!this.moveItemStackTo(itemstack1, 28, 37, false)) {
					return ItemStack.EMPTY;
				}
			} else if (slotIndex >= 28 && slotIndex < 37) {
				if (!this.moveItemStackTo(itemstack1, 1, 28, false)) {
					return ItemStack.EMPTY;
				}
			}

			if (itemstack1.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}
		return itemstack;
	}
}
