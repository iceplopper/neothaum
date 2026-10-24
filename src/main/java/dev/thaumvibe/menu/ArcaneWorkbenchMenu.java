package dev.thaumvibe.menu;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.crafting.ArcaneCraftingManager;
import dev.thaumvibe.items.WandItem;
import dev.thaumvibe.registry.TVMenus;
import dev.thaumvibe.tiles.ArcaneWorktableBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Port of ContainerArcaneWorkbench (+ SlotCraftingArcaneWorkbench, SlotLimitedByWand). */
public class ArcaneWorkbenchMenu extends AbstractContainerMenu {
	public final Container tile;
	private final Player player;
	/** 0-5: primal vis cost of the matched arcane recipe; 6: 1 if the match is arcane. */
	public final ContainerData cost;

	public ArcaneWorkbenchMenu(int id, Inventory inv, BlockPos pos) {
		this(id, inv, inv.player.level().getBlockEntity(pos) instanceof ArcaneWorktableBlockEntity be ? be : new SimpleContainer(11));
	}

	public ArcaneWorkbenchMenu(int id, Inventory inv, Container tile) {
		super(TVMenus.ARCANE_WORKBENCH, id);
		this.tile = tile;
		this.player = inv.player;
		this.cost = new SimpleContainerData(7);
		if (tile instanceof ArcaneWorktableBlockEntity be && !inv.player.level().isClientSide()) {
			be.eventHandler = this;
		}
		this.addSlot(new ResultSlot(tile, ArcaneWorktableBlockEntity.RESULT_SLOT, 160, 64));
		this.addSlot(new Slot(tile, ArcaneWorktableBlockEntity.WAND_SLOT, 160, 24) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.getItem() instanceof WandItem && !WandItem.isStaff(stack);
			}

			@Override
			public int getMaxStackSize() {
				return 1;
			}
		});
		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 3; c++) {
				this.addSlot(new Slot(tile, c + r * 3, 40 + c * 24, 40 + r * 24));
			}
		}
		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 9; c++) {
				this.addSlot(new Slot(inv, c + r * 9 + 9, 16 + c * 18, 151 + r * 18));
			}
		}
		for (int c = 0; c < 9; c++) {
			this.addSlot(new Slot(inv, c, 16 + c * 18, 209));
		}
		this.addDataSlots(this.cost);
		this.slotsChanged(tile);
	}

	public AspectList getCost() {
		AspectList l = new AspectList();
		List<Aspect> primals = Aspect.getPrimalAspects();
		for (int i = 0; i < 6; i++) {
			if (this.cost.get(i) > 0) {
				l.add(primals.get(i), this.cost.get(i));
			}
		}
		return l;
	}

	@Override
	public void slotsChanged(Container container) {
		if (this.player.level().isClientSide() || !(this.tile instanceof ArcaneWorktableBlockEntity be)) {
			return;
		}
		ArcaneCraftingManager.Match m = ArcaneCraftingManager.find(this.tile, this.player, this.player.level());
		List<Aspect> primals = Aspect.getPrimalAspects();
		for (int i = 0; i < 6; i++) {
			this.cost.set(i, m.arcane() ? m.aspects().getAmount(primals.get(i)) : 0);
		}
		this.cost.set(6, m.arcane() ? 1 : 0);
		ItemStack result = ItemStack.EMPTY;
		if (!m.arcane()) {
			result = m.result();
		} else {
			ItemStack wand = this.tile.getItem(ArcaneWorktableBlockEntity.WAND_SLOT);
			if (wand.getItem() instanceof WandItem && WandItem.consumeAllVisCrafting(wand, this.player, m.aspects(), false)) {
				result = m.result();
			}
		}
		be.setItemSoftly(ArcaneWorktableBlockEntity.RESULT_SLOT, result);
		this.broadcastChanges();
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		if (this.tile instanceof ArcaneWorktableBlockEntity be && be.eventHandler == this) {
			be.eventHandler = null;
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return this.tile.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack var3 = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot.hasItem()) {
			ItemStack var5 = slot.getItem();
			var3 = var5.copy();
			if (index == 0) {
				if (!this.moveItemStackTo(var5, 11, 47, true)) {
					return ItemStack.EMPTY;
				}
				slot.onQuickCraft(var5, var3);
			} else if (index >= 11 && index < 38) {
				if (var5.getItem() instanceof WandItem && !WandItem.isStaff(var5)) {
					if (!this.moveItemStackTo(var5, 1, 2, false)) {
						return ItemStack.EMPTY;
					}
					slot.onQuickCraft(var5, var3);
				} else if (!this.moveItemStackTo(var5, 38, 47, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= 38 && index < 47) {
				if (var5.getItem() instanceof WandItem && !WandItem.isStaff(var5)) {
					if (!this.moveItemStackTo(var5, 1, 2, false)) {
						return ItemStack.EMPTY;
					}
					slot.onQuickCraft(var5, var3);
				} else if (!this.moveItemStackTo(var5, 11, 38, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(var5, 11, 47, false)) {
				return ItemStack.EMPTY;
			}
			if (var5.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
			if (var5.getCount() == var3.getCount()) {
				return ItemStack.EMPTY;
			}
			slot.onTake(player, var5);
		}
		return var3;
	}

	/** SlotCraftingArcaneWorkbench. */
	private class ResultSlot extends Slot {
		ResultSlot(Container c, int slot, int x, int y) {
			super(c, slot, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return false;
		}

		@Override
		public void onTake(Player p, ItemStack stack) {
			stack.onCraftedBy(p, stack.getCount());
			Container inv = ArcaneWorkbenchMenu.this.tile;
			if (!p.level().isClientSide()) {
				ArcaneCraftingManager.Match m = ArcaneCraftingManager.find(inv, p, p.level());
				ItemStack wand = inv.getItem(ArcaneWorktableBlockEntity.WAND_SLOT);
				if (m.arcane() && m.aspects().size() > 0 && wand.getItem() instanceof WandItem) {
					WandItem.consumeAllVisCrafting(wand, p, m.aspects(), true);
				}
				NonNullList<ItemStack> rem = ArcaneCraftingManager.remainders(inv);
				for (int i = 0; i < 9; i++) {
					ItemStack s = inv.getItem(i);
					if (!s.isEmpty()) {
						s.shrink(1);
					}
					ItemStack r = rem.get(i);
					if (!r.isEmpty()) {
						if (inv.getItem(i).isEmpty()) {
							inv.setItem(i, r);
						} else {
							p.getInventory().placeItemBackInInventory(r, net.minecraft.util.Prediction.SERVER_ONLY);
						}
					}
				}
				inv.setItem(0, inv.getItem(0)); // re-evaluate the grid
			}
			super.onTake(p, stack);
		}
	}
}
