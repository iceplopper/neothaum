package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import dev.neothaum.golem.GolemHelper;
import dev.neothaum.golem.GolemInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/** AIFillTake: at a marked container, take the item it came for (its full stack with "any amount", else the filter's count). */
public class FillTakeGoal extends GolemGoal {
	private int countChest;
	private int count;

	public FillTakeGoal(GolemEntity golem) {
		super(golem);
	}

	@Override
	public boolean canUse() {
		if (!this.golem.getCarried().isEmpty() || this.golem.itemWatched.isEmpty() || !this.navDone() || !this.golem.hasSomething()) {
			return false;
		}
		for (BlockPos p : GolemHelper.getMarkedContainersAdjacentToGolem(this.golem)) {
			if (GolemHelper.isHomeContainer(this.golem, p)) {
				continue;
			}
			Container inv = GolemInventory.getContainer(this.golem.level(), p);
			for (byte color : this.golem.getColorsMatching(this.golem.itemWatched)) {
				for (int side : GolemHelper.getMarkedSides(this.golem, p, color)) {
					ItemStack target = this.golem.itemWatched.copy();
					target.setCount(this.golem.getToggle(0) ? this.golem.getCarrySpace() : Math.min(target.getCount(), this.golem.getCarrySpace()));
					ItemStack result = GolemInventory.extractStack(inv, target, side, this.golem.checkOreDict(), this.golem.ignoreDamage(),
						this.golem.ignoreNBT(), true);
					if (!result.isEmpty()) {
						this.golem.setCarried(result);
						this.golem.openContainer(p);
						this.countChest = 5;
						this.count = 200;
						this.golem.itemWatched = ItemStack.EMPTY;
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean canContinueToUse() {
		return this.count > 0 && (!this.navDone() || this.countChest > 0);
	}

	@Override
	public void tick() {
		this.count--;
		this.countChest--;
	}

	@Override
	public void stop() {
		this.golem.closeContainer();
	}
}
