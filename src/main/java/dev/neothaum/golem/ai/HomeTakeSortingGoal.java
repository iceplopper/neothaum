package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import dev.neothaum.golem.GolemHelper;
import dev.neothaum.golem.GolemInventory;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/** AIHomeTakeSorting: empty-handed at home, take something from the home container that a marked container already holds. */
public class HomeTakeSortingGoal extends GolemGoal {
	private int countChest;
	private int count;

	public HomeTakeSortingGoal(GolemEntity golem) {
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
		List<ItemStack> needed = GolemHelper.getItemsNeeded(this.golem, this.golem.getUpgradeAmount(5) > 0);
		if (needed != null) {
			for (ItemStack stack : needed) {
				if (!this.extract(inv, stack, false).isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}

	private ItemStack extract(Container inv, ItemStack stack, boolean doit) {
		return GolemInventory.extractStack(inv, stack.copyWithCount(this.golem.getCarrySpace()), this.golem.getHomeFacingId(), this.golem.checkOreDict(),
			this.golem.ignoreDamage(), this.golem.ignoreNBT(), doit);
	}

	@Override
	public boolean canContinueToUse() {
		return this.count > 0 && this.countChest > 0;
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
		Container inv = GolemHelper.getHomeContainer(this.golem);
		List<ItemStack> needed = GolemHelper.getItemsNeeded(this.golem, this.golem.getUpgradeAmount(5) > 0);
		if (inv == null || needed == null) {
			return;
		}
		for (ItemStack stack : needed) {
			ItemStack result = this.extract(inv, stack, true);
			if (!result.isEmpty()) {
				this.golem.setCarried(result);
				this.golem.openContainer(this.golem.getHomeContainerPos());
				this.countChest = 5;
				break;
			}
		}
	}
}
