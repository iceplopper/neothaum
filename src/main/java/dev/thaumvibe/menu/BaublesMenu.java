package dev.thaumvibe.menu;

import com.mojang.datafixers.util.Pair;
import dev.thaumvibe.baubles.BaubleItem;
import dev.thaumvibe.baubles.BaubleType;
import dev.thaumvibe.baubles.Baubles;
import dev.thaumvibe.registry.TVMenus;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Port of Baubles' ContainerPlayerExpanded: the player's armor and inventory plus the amulet, two ring and belt slots,
 * laid out like the vanilla inventory with the bauble column to the left of the player preview.
 */
public class BaublesMenu extends AbstractContainerMenu {
	private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
	private final Player player;
	public final Container baubles;

	public BaublesMenu(int id, Inventory inv) {
		super(TVMenus.BAUBLES, id);
		this.player = inv.player;
		Baubles stored = Baubles.get(this.player);
		this.baubles = new SimpleContainer(Baubles.SIZE) {
			@Override
			public void setChanged() {
				super.setChanged();
				if (!BaublesMenu.this.player.level().isClientSide()) {
					List<ItemStack> l = new ArrayList<>();
					for (int i = 0; i < Baubles.SIZE; i++) {
						l.add(this.getItem(i).copy());
					}
					Baubles.set(BaublesMenu.this.player, new Baubles(l));
				}
			}
		};
		for (int i = 0; i < Baubles.SIZE; i++) {
			((SimpleContainer) this.baubles).setItem(i, stored.get(i).copy());
		}
		// armor column (vanilla InventoryMenu positions)
		for (int i = 0; i < 4; i++) {
			EquipmentSlot es = ARMOR[i];
			this.addSlot(new Slot(inv, 39 - i, 8, 8 + i * 18) {
				@Override
				public int getMaxStackSize() {
					return 1;
				}

				@Override
				public boolean mayPlace(ItemStack stack) {
					return BaublesMenu.this.player.getEquipmentSlotForItem(stack) == es;
				}

			});
		}
		// bauble column: amulet, ring, ring, belt
		BaubleType[] types = {BaubleType.AMULET, BaubleType.RING, BaubleType.RING, BaubleType.BELT};
		for (int i = 0; i < Baubles.SIZE; i++) {
			final int slot = i;
			this.addSlot(new Slot(this.baubles, i, 77, 8 + i * 18) {
				@Override
				public int getMaxStackSize() {
					return 1;
				}

				@Override
				public boolean mayPlace(ItemStack stack) {
					return stack.getItem() instanceof BaubleItem b && b.getBaubleType(stack).fitsSlot(slot) && b.canEquip(stack, BaublesMenu.this.player);
				}

				@Override
				public boolean mayPickup(Player p) {
					ItemStack s = this.getItem();
					return s.isEmpty() || !(s.getItem() instanceof BaubleItem b) || b.canUnequip(s, p);
				}
			});
		}
		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 9; c++) {
				this.addSlot(new Slot(inv, c + (r + 1) * 9, 8 + c * 18, 84 + r * 18));
			}
		}
		for (int c = 0; c < 9; c++) {
			this.addSlot(new Slot(inv, c, 8 + c * 18, 142));
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack out = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot.hasItem()) {
			ItemStack s = slot.getItem();
			out = s.copy();
			if (index < 8) {
				if (!this.moveItemStackTo(s, 8, 44, false)) {
					return ItemStack.EMPTY;
				}
			} else if (s.getItem() instanceof BaubleItem) {
				if (!this.moveItemStackTo(s, 4, 8, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index < 35) {
				if (!this.moveItemStackTo(s, 35, 44, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(s, 8, 35, false)) {
				return ItemStack.EMPTY;
			}
			if (s.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}
		return out;
	}
}
