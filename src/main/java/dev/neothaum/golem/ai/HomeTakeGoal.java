package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import dev.neothaum.golem.GolemHelper;
import dev.neothaum.golem.GolemInventory;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/** AIHomeTake: empty-handed at home, take something from the home container that it has somewhere to put (empty and use cores). */
public class HomeTakeGoal extends GolemGoal {
	private int countChest;

	public HomeTakeGoal(GolemEntity golem) {
		super(golem);
	}

	@Override
	public boolean canUse() {
		if (!this.golem.getCarried().isEmpty() || !this.delayTick() || !this.navDone() || !this.atHome()) {
			return false;
		}
		Container inv = GolemHelper.getHomeContainer(this.golem);
		if (inv == null) {
			return false;
		}
		int side = this.golem.getHomeFacingId();
		List<ItemStack> needed = GolemHelper.getItemsNeeded(this.golem, this.golem.getUpgradeAmount(5) > 0);
		if (needed == null) {
			ItemStack is;
			do {
				is = GolemHelper.getFirstItemUsingTimeout(this.golem, inv, side, false);
				if (!is.isEmpty() && GolemHelper.validTargetForItem(this.golem, is)) {
					this.take(GolemHelper.getFirstItemUsingTimeout(this.golem, inv, side, true));
					return true;
				}
			} while (!is.isEmpty());
			return false;
		}
		for (ItemStack stack : needed) {
			if (GolemHelper.validTargetForItem(this.golem, stack)) {
				ItemStack result = GolemInventory.extractStack(inv, stack.copyWithCount(this.golem.getCarrySpace()), side, this.golem.checkOreDict(),
					this.golem.ignoreDamage(), this.golem.ignoreNBT(), true);
				if (!result.isEmpty()) {
					this.take(result);
					return true;
				}
			}
		}
		return false;
	}

	private void take(ItemStack result) {
		this.golem.setCarried(result);
		this.golem.openContainer(this.golem.getHomeContainerPos());
		this.countChest = 5;
	}

	@Override
	public boolean canContinueToUse() {
		return this.countChest > 0;
	}

	@Override
	public void stop() {
		this.golem.closeContainer();
	}

	@Override
	public void tick() {
		this.countChest--;
	}
}
