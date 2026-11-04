package dev.neothaum.addon;

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

/**
 * The Experience Siphon's screen, laid out as TC4's spa screen: empty jars in above, full jars out below, its store (data 0, in points), how
 * far the jar being filled has got (data 1, of ExperienceSiphonBlockEntity.FILL) and the player's inventory.
 */
public class ExperienceSiphonMenu extends AbstractContainerMenu {
	public final Container tile;
	public final ContainerData data;

	public ExperienceSiphonMenu(int id, Inventory inv, BlockPos pos) {
		this(id, inv, inv.player.level().getBlockEntity(pos) instanceof ExperienceSiphonBlockEntity s ? s : new SimpleContainer(2),
			inv.player.level().getBlockEntity(pos) instanceof ExperienceSiphonBlockEntity s ? s.data : new SimpleContainerData(2));
	}

	public ExperienceSiphonMenu(int id, Inventory inv, Container tile, ContainerData data) {
		super(ExperienceSiphon.MENU, id);
		this.tile = tile;
		this.data = data;
		this.addDataSlots(data);
		this.addSlot(new Slot(tile, ExperienceSiphonBlockEntity.IN, 65, 13) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return ExperienceSiphon.isEmptyJar(stack);
			}
		});
		this.addSlot(new Slot(tile, ExperienceSiphonBlockEntity.OUT, 65, 49) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}
		});
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}
		for (int i = 0; i < 9; i++) {
			this.addSlot(new Slot(inv, i, 8 + i * 18, 142));
		}
	}

	/** The siphon's store, in points. */
	public int store() {
		return this.data.get(0);
	}

	/** How far the jar being filled has got, from 0 to 1. */
	public float progress() {
		return Math.min(1.0F, this.data.get(1) / (float) ExperienceSiphonBlockEntity.FILL);
	}

	@Override
	public boolean stillValid(Player player) {
		return this.tile.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = this.slots.get(index);
		if (slot == null || !slot.hasItem()) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = slot.getItem();
		ItemStack copy = stack.copy();
		if (index < 2) {
			if (!this.moveItemStackTo(stack, 2, this.slots.size(), true)) {
				return ItemStack.EMPTY;
			}
		} else if (ExperienceSiphon.isEmptyJar(stack)) {
			if (!this.moveItemStackTo(stack, 0, 1, false)) {
				return ItemStack.EMPTY;
			}
		} else if (index < 29) {
			if (!this.moveItemStackTo(stack, 29, 38, false)) {
				return ItemStack.EMPTY;
			}
		} else if (!this.moveItemStackTo(stack, 2, 29, false)) {
			return ItemStack.EMPTY;
		}
		if (stack.isEmpty()) {
			slot.set(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		return copy;
	}
}
