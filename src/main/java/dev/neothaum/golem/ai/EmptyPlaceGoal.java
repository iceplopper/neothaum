package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import dev.neothaum.golem.GolemHelper;
import dev.neothaum.golem.GolemInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * AIEmptyPlace: beside a marked container (not home), put what it carries in through a face marked in one of the item's colours. The sorting
 * golem's AISortingPlace is the same, but only into containers that already hold the item.
 */
public class EmptyPlaceGoal extends GolemGoal {
	private final boolean sorting;
	private int countChest;
	private int count;
	private BlockPos target = BlockPos.ZERO;

	public EmptyPlaceGoal(GolemEntity golem) {
		this(golem, false);
	}

	protected EmptyPlaceGoal(GolemEntity golem, boolean sorting) {
		super(golem);
		this.sorting = sorting;
	}

	@Override
	public boolean canUse() {
		ItemStack carried = this.golem.itemCarried;
		if (carried.isEmpty() || !this.navDone()) {
			return false;
		}
		for (BlockPos p : GolemHelper.getMarkedContainersAdjacentToGolem(this.golem)) {
			if (GolemHelper.isHomeContainer(this.golem, p)) {
				continue;
			}
			Container inv = GolemInventory.getContainer(this.golem.level(), p);
			if (this.sorting) {
				for (int side : GolemHelper.getMarkedSides(this.golem, p, (byte) -1)) {
					if (!ItemStack.matches(GolemInventory.placeItemStackIntoInventory(carried, inv, side, false), carried)
						&& GolemInventory.inventoryContains(inv, carried, side, this.golem.checkOreDict(), this.golem.ignoreDamage(), this.golem.ignoreNBT())) {
						this.target = p;
						return true;
					}
				}
				continue;
			}
			for (byte color : this.golem.getColorsMatching(carried)) {
				for (int side : GolemHelper.getMarkedSides(this.golem, p, color)) {
					if (!ItemStack.matches(GolemInventory.placeItemStackIntoInventory(carried, inv, side, false), carried)) {
						this.target = p;
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean canContinueToUse() {
		return this.count > 0 && (this.canUse() || this.countChest > 0);
	}

	@Override
	public void stop() {
		this.golem.closeContainer();
	}

	@Override
	public void tick() {
		this.countChest--;
		this.count--;
	}

	@Override
	public void start() {
		this.count = 200;
		BlockPos p = this.target;
		Container inv = GolemInventory.getContainer(this.golem.level(), p);
		if (inv != null && !GolemHelper.isHomeContainer(this.golem, p)) {
			for (byte color : this.golem.getColorsMatching(this.golem.itemCarried)) {
				for (int side : GolemHelper.getMarkedSides(this.golem, p, color)) {
					this.golem.itemCarried = GolemInventory.placeItemStackIntoInventory(this.golem.itemCarried, inv, side, true);
					this.countChest = 5;
					if (this.golem.itemCarried.isEmpty()) {
						break;
					}
				}
				if (this.countChest == 5) {
					this.golem.openContainer(p);
					break;
				}
			}
		}
		this.golem.updateCarried();
	}
}
