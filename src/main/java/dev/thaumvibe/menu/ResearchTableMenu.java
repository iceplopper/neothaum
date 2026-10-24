package dev.thaumvibe.menu;

import dev.thaumvibe.registry.TVItems;
import dev.thaumvibe.registry.TVMenus;
import dev.thaumvibe.tiles.ResearchTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Port of ContainerResearchTable. */
public class ResearchTableMenu extends AbstractContainerMenu {
	public final Container tile;
	public final BlockPos pos;

	public ResearchTableMenu(int id, Inventory inv, BlockPos pos) {
		this(id, inv, inv.player.level().getBlockEntity(pos) instanceof ResearchTableBlockEntity be ? be : new SimpleContainer(2), pos);
	}

	public ResearchTableMenu(int id, Inventory inv, ResearchTableBlockEntity be) {
		this(id, inv, be, be.getBlockPos());
	}

	private ResearchTableMenu(int id, Inventory inv, Container tile, BlockPos pos) {
		super(TVMenus.RESEARCH_TABLE, id);
		this.tile = tile;
		this.pos = pos;
		this.addSlot(new Slot(tile, 0, 14, 10) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.is(TVItems.SCRIBING_TOOLS);
			}
		});
		this.addSlot(new Slot(tile, 1, 70, 10) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.is(TVItems.RESEARCH_NOTES);
			}
		});
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				this.addSlot(new Slot(inv, j + i * 9 + 9, 48 + j * 18, 175 + i * 18));
			}
		}
		for (int i = 0; i < 9; i++) {
			this.addSlot(new Slot(inv, i, 48 + i * 18, 233));
		}
	}

	public ResearchTableBlockEntity table() {
		return this.tile instanceof ResearchTableBlockEntity be ? be : null;
	}

	@Override
	public boolean clickMenuButton(Player player, int button) {
		if (button == 1) {
			return true;
		}
		if (button == 5 && table() != null) {
			table().duplicate(player);
			return true;
		}
		return false;
	}

	@Override
	public boolean stillValid(Player player) {
		return this.tile.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slot) {
		ItemStack stack = ItemStack.EMPTY;
		Slot slotObject = this.slots.get(slot);
		if (slotObject.hasItem()) {
			ItemStack stackInSlot = slotObject.getItem();
			stack = stackInSlot.copy();
			if (slot < 2) {
				if (!this.moveItemStackTo(stackInSlot, 2, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (stackInSlot.is(TVItems.SCRIBING_TOOLS)) {
				if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (stackInSlot.is(TVItems.RESEARCH_NOTES)) {
				if (!this.moveItemStackTo(stackInSlot, 1, 2, false)) {
					return ItemStack.EMPTY;
				}
			} else {
				return ItemStack.EMPTY;
			}
			if (stackInSlot.isEmpty()) {
				slotObject.setByPlayer(ItemStack.EMPTY);
			} else {
				slotObject.setChanged();
			}
		}
		return stack;
	}
}
