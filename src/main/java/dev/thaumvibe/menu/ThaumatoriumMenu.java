package dev.thaumvibe.menu;

import dev.thaumvibe.registry.TVMenus;
import dev.thaumvibe.tiles.ThaumatoriumBlockEntity;
import dev.thaumvibe.tiles.ThaumatoriumBlockEntity.CrucibleEntry;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Port of ContainerThaumatorium:
 * Slot 0 is the catalyst input at (48, 16); recipe selection is driven via menu buttons and sync.
 */
public class ThaumatoriumMenu extends AbstractContainerMenu {
	public final Container tile;
	public final Player player;
	public final BlockPos pos;
	public final ThaumatoriumBlockEntity thaumatorium;

	public ThaumatoriumMenu(int id, Inventory inv, BlockPos pos) {
		this(id, inv, inv.player.level().getBlockEntity(pos) instanceof Container c ? c : new SimpleContainer(1), pos);
	}

	public ThaumatoriumMenu(int id, Inventory inv, Container tile, BlockPos pos) {
		super(TVMenus.THAUMATORIUM, id);
		this.tile = tile;
		this.player = inv.player;
		this.pos = pos;
		this.thaumatorium = inv.player.level().getBlockEntity(pos) instanceof ThaumatoriumBlockEntity t ? t : null;

		// Slot 0: Catalyst
		this.addSlot(new Slot(tile, 0, 48, 16));

		// Player 3x9 inventory
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		// Player 1x9 hotbar
		for (int i = 0; i < 9; i++) {
			this.addSlot(new Slot(inv, i, 8 + i * 18, 142));
		}
	}

	@Override
	public boolean clickMenuButton(Player player, int button) {
		if (this.thaumatorium != null) {
			return this.thaumatorium.toggleRecipe(player, button);
		}
		return false;
	}

	public List<CrucibleEntry> getAvailableRecipes() {
		if (this.thaumatorium != null) {
			return this.thaumatorium.getAvailableRecipes(this.player);
		}
		return List.of();
	}

	@Override
	public boolean stillValid(Player player) {
		return this.tile.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (index == 0) {
				if (!this.moveItemStackTo(itemstack1, 1, 37, true)) {
					return ItemStack.EMPTY;
				}
				slot.onQuickCraft(itemstack1, itemstack);
			} else {
				if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
					return ItemStack.EMPTY;
				}
			}

			if (itemstack1.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, itemstack1);
		}
		return itemstack;
	}
}
