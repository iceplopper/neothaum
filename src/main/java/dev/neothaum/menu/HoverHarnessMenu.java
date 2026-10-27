package dev.neothaum.menu;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.items.equipment.ThaumostaticHarnessItem;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVComponents;
import dev.neothaum.registry.TVMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * ContainerHoverHarness: 1 jar slot (80, 32) holding potentia essentia to fuel flight.
 */
public class HoverHarnessMenu extends AbstractContainerMenu {
	private final Container input = new SimpleContainer(1);
	private final Player player;
	private final int lockedSlot;

	public HoverHarnessMenu(int id, Inventory inv) {
		this(id, inv, inv.getSelectedSlot());
	}

	public HoverHarnessMenu(int id, Inventory inv, int lockedSlot) {
		super(TVMenus.HOVER_HARNESS, id);
		this.player = inv.player;
		this.lockedSlot = lockedSlot;

		ItemStack harness = this.findHarness();
		if (!this.player.level().isClientSide() && !harness.isEmpty()) {
			ItemStack jar = harness.get(TVComponents.HARNESS_JAR);
			if (jar != null && !jar.isEmpty()) {
				this.input.setItem(0, jar.copy());
			}
		}

		// Jar slot at (80, 32)
		this.addSlot(new Slot(this.input, 0, 80, 32) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return isValidJar(stack);
			}

			@Override
			public int getMaxStackSize() {
				return 1;
			}
		});

		// Player inventory (3 rows of 9)
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		// Player hotbar (9 slots)
		for (int i = 0; i < 9; i++) {
			this.addSlot(new Slot(inv, i, 8 + i * 18, 142));
		}
	}

	public static boolean isValidJar(ItemStack stack) {
		if (stack.isEmpty()) return false;
		if (stack.is(TVBlocks.WARDED_JAR.asItem()) || stack.is(TVBlocks.VOID_JAR.asItem())) {
			AspectList al = stack.get(TVComponents.ASPECTS);
			return al != null && al.getAmount(Aspect.ENERGY) > 0;
		}
		return false;
	}

	public int lockedHotbarSlot() {
		return this.lockedSlot;
	}

	private ItemStack findHarness() {
		if (this.lockedSlot >= 0 && this.lockedSlot < 9) {
			ItemStack s = this.player.getInventory().getItem(this.lockedSlot);
			if (s.getItem() instanceof ThaumostaticHarnessItem) return s;
		}
		ItemStack chest = this.player.getItemBySlot(EquipmentSlot.CHEST);
		if (chest.getItem() instanceof ThaumostaticHarnessItem) return chest;
		ItemStack main = this.player.getMainHandItem();
		if (main.getItem() instanceof ThaumostaticHarnessItem) return main;
		ItemStack off = this.player.getOffhandItem();
		if (off.getItem() instanceof ThaumostaticHarnessItem) return off;
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack quickMoveStack(Player p, int slotIndex) {
		Slot slot = this.slots.get(slotIndex);
		if (!slot.hasItem()) return ItemStack.EMPTY;
		ItemStack item = slot.getItem();
		ItemStack copy = item.copy();

		if (slotIndex == 0) {
			// Jar slot -> inventory
			if (!this.moveItemStackTo(item, 1, this.slots.size(), true)) {
				return ItemStack.EMPTY;
			}
		} else {
			// Inventory -> jar slot
			if (isValidJar(item)) {
				if (!this.moveItemStackTo(item, 0, 1, false)) {
					return ItemStack.EMPTY;
				}
			} else {
				return ItemStack.EMPTY;
			}
		}

		if (item.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		saveJar();
		return copy;
	}

	@Override
	public void clicked(int slotId, int button, ContainerInput inputType, Player p) {
		if (this.lockedSlot >= 0 && slotId == this.lockedSlot + 28) {
			// Hotbar slot index in menu is 28 + hotbarSlot (slot 0 is jar, 1..27 is main inv, 28..36 is hotbar)
			return;
		}
		super.clicked(slotId, button, inputType, p);
		saveJar();
	}

	@Override
	public boolean stillValid(Player p) {
		return !this.findHarness().isEmpty();
	}

	private void saveJar() {
		if (!this.player.level().isClientSide()) {
			ItemStack harness = this.findHarness();
			if (!harness.isEmpty()) {
				ItemStack jar = this.input.getItem(0);
				if (!jar.isEmpty()) {
					harness.set(TVComponents.HARNESS_JAR, jar.copy());
				} else {
					harness.remove(TVComponents.HARNESS_JAR);
				}
			}
		}
	}

	@Override
	public void removed(Player p) {
		super.removed(p);
		saveJar();
	}
}
