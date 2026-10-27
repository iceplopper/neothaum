package dev.neothaum.menu;

import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.aspects.ObjectAspects;
import dev.neothaum.registry.TVMenus;
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

/** Port of ContainerAlchemyFurnace: the item to boil down above, the fuel below. */
public class AlchemyFurnaceMenu extends AbstractContainerMenu {
	public final Container tile;
	public final ContainerData data;
	private final Player player;
	/** Client side: where the furnace is, so the screen can read the essentia it holds. */
	public BlockPos pos;

	public AlchemyFurnaceMenu(int id, Inventory inv, BlockPos pos) {
		this(id, inv, new SimpleContainer(2), new SimpleContainerData(5));
		this.pos = pos;
	}

	public AlchemyFurnaceMenu(int id, Inventory inv, Container tile, ContainerData data) {
		super(TVMenus.ALCHEMY_FURNACE, id);
		this.tile = tile;
		this.data = data;
		this.player = inv.player;
		this.addSlot(new Slot(tile, 0, 80, 8) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return hasAspects(stack);
			}
		});
		this.addSlot(new Slot(tile, 1, 80, 48));
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}
		for (int i = 0; i < 9; i++) {
			this.addSlot(new Slot(inv, i, 8 + i * 18, 142));
		}
		this.addDataSlots(data);
	}

	private static boolean hasAspects(ItemStack stack) {
		AspectList al = ObjectAspects.getAspects(stack);
		return al != null && al.size() > 0;
	}

	private boolean isFuel(ItemStack stack) {
		return stack.has(net.minecraft.core.component.DataComponents.COOKING_FUEL);
	}

	public int cookTime() {
		return this.data.get(0);
	}

	public int burnTime() {
		return this.data.get(1);
	}

	public int itemBurnTime() {
		return this.data.get(2) == 0 ? 200 : this.data.get(2);
	}

	public int vis() {
		return this.data.get(3);
	}

	public int smeltTime() {
		return Math.max(1, this.data.get(4));
	}

	@Override
	public boolean stillValid(Player player) {
		return this.tile.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack result = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot.hasItem()) {
			ItemStack stack = slot.getItem();
			result = stack.copy();
			if (index != 1 && index != 0) {
				if (this.isFuel(stack)) {
					if (!this.moveItemStackTo(stack, 1, 2, false) && !this.moveItemStackTo(stack, 0, 1, false)) {
						return ItemStack.EMPTY;
					}
				} else if (hasAspects(stack)) {
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
			} else if (!this.moveItemStackTo(stack, 2, 38, false)) {
				return ItemStack.EMPTY;
			}
			if (stack.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
			if (stack.getCount() == result.getCount()) {
				return ItemStack.EMPTY;
			}
			slot.onTake(player, stack);
		}
		return result;
	}
}
