package dev.neothaum.menu;

import dev.neothaum.aspects.ObjectAspects;
import dev.neothaum.registry.TVMenus;
import dev.neothaum.tiles.DeconstructionTableBlockEntity;
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

public class DeconstructionTableMenu extends AbstractContainerMenu {
	public final Container tile;
	public final Player player;
	public final BlockPos pos;
	public final DeconstructionTableBlockEntity table;
	public final ContainerData data;

	public DeconstructionTableMenu(int id, Inventory inv, BlockPos pos) {
		this(id, inv, inv.player.level().getBlockEntity(pos) instanceof Container c ? c : new SimpleContainer(1), pos,
			inv.player.level().getBlockEntity(pos) instanceof DeconstructionTableBlockEntity t ? t.data : new SimpleContainerData(2));
	}

	public DeconstructionTableMenu(int id, Inventory inv, Container tile, BlockPos pos, ContainerData data) {
		super(TVMenus.DECONSTRUCTION_TABLE, id);
		this.tile = tile;
		this.player = inv.player;
		this.pos = pos;
		this.table = inv.player.level().getBlockEntity(pos) instanceof DeconstructionTableBlockEntity t ? t : null;
		this.data = data;
		this.addDataSlots(data);

		// Slot 0: Deconstruction input slot at (64, 16)
		this.addSlot(new Slot(tile, 0, 64, 16) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				var al = ObjectAspects.getAspects(stack);
				return al != null && al.size() > 0;
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
		if (id == 1 && table != null) {
			table.claimAspect(player);
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
			} else {
				var al = ObjectAspects.getAspects(itemstack1);
				if (al != null && al.size() > 0) {
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
