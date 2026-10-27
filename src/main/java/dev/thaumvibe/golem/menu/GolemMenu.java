package dev.thaumvibe.golem.menu;

import dev.thaumvibe.golem.GolemCoreType;
import dev.thaumvibe.golem.GolemEntity;
import dev.thaumvibe.registry.TVMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GolemMenu extends AbstractContainerMenu {
	public final GolemEntity golem;
	public final Player player;
	private final Container dummyContainer = new SimpleContainer(6);
	public int currentScroll = 0;
	public int maxScroll = 0;

	public GolemMenu(int id, Inventory inv, Integer golemId) {
		this(id, inv, inv.player.level().getEntity(golemId) instanceof GolemEntity g ? g : null);
	}

	public GolemMenu(int id, Inventory inv, GolemEntity golem) {
		super(TVMenus.GOLEM, id);
		this.golem = golem;
		this.player = inv.player;

		if (this.golem != null) {
			this.golem.setInactive(true);
			int totalSlots = this.golem.getFilterSlotCount();
			this.maxScroll = Math.max(0, (totalSlots - 1) / 6);
			this.syncDummySlots();
		}

		// 6 ghost filter slots
		for (int a = 0; a < 6; a++) {
			int x = 100 + (a / 2) * 28;
			int y = 16 + (a % 2) * 31;
			this.addSlot(new Slot(this.dummyContainer, a, x, y) {
				@Override
				public boolean mayPlace(ItemStack stack) {
					return false;
				}

				@Override
				public boolean mayPickup(Player player) {
					return false;
				}
			});
		}

		// Player inventory (3 rows of 9)
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
			}
		}

		// Player hotbar (1 row of 9)
		for (int col = 0; col < 9; col++) {
			this.addSlot(new Slot(inv, col, 8 + col * 18, 142));
		}
	}

	public void syncDummySlots() {
		if (this.golem == null) return;
		for (int a = 0; a < 6; a++) {
			int filterIndex = a + this.currentScroll * 6;
			if (filterIndex < this.golem.getFilterSlotCount()) {
				this.dummyContainer.setItem(a, this.golem.getFilterItem(filterIndex));
			} else {
				this.dummyContainer.setItem(a, ItemStack.EMPTY);
			}
		}
	}

	public void setScroll(int scroll) {
		this.currentScroll = Math.max(0, Math.min(this.maxScroll, scroll));
		this.syncDummySlots();
	}

	@Override
	public void clicked(int slotId, int button, ContainerInput clickType, Player player) {
		if (slotId >= 0 && slotId < 6 && this.golem != null) {
			int filterIndex = slotId + this.currentScroll * 6;
			if (filterIndex < this.golem.getFilterSlotCount()) {
				ItemStack carried = this.getCarried();
				if (carried.isEmpty()) {
					if (button == 1) { // right click: shrink or clear
						ItemStack current = this.golem.getFilterItem(filterIndex);
						if (!current.isEmpty()) {
							if (current.getCount() > 1) {
								current.shrink(1);
							} else {
								this.golem.setFilterItem(filterIndex, ItemStack.EMPTY);
							}
						}
					} else { // left click: clear
						this.golem.setFilterItem(filterIndex, ItemStack.EMPTY);
					}
				} else {
					// Set ghost slot
					boolean single = button == 1 || this.golem.getCore() != 0 || this.golem.getToggle(0);
					ItemStack copy = carried.copyWithCount(single ? 1 : carried.getCount());
					this.golem.setFilterItem(filterIndex, copy);
				}
				this.syncDummySlots();
			}
			return;
		}
		super.clicked(slotId, button, clickType, player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex) {
		if (slotIndex >= 6 && this.golem != null) {
			// Clicked an item in player inventory -> copy to first empty filter slot
			Slot playerSlot = this.slots.get(slotIndex);
			if (playerSlot.hasItem()) {
				ItemStack stack = playerSlot.getItem();
				for (int i = 0; i < this.golem.getFilterSlotCount(); i++) {
					if (this.golem.getFilterItem(i).isEmpty()) {
						this.golem.setFilterItem(i, stack.copyWithCount(1));
						this.syncDummySlots();
						break;
					}
				}
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		return this.golem != null && this.golem.isAlive() && player.distanceToSqr(this.golem) < 64.0;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		if (this.golem != null) {
			this.golem.setInactive(false);
		}
	}
}
