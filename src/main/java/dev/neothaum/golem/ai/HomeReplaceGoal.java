package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import dev.neothaum.golem.GolemHelper;
import dev.neothaum.golem.GolemInventory;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/** AIHomeReplace: back home holding something it has nowhere to take (or that is on timeout), put it back in the home container. */
public class HomeReplaceGoal extends HomePlaceGoal {
	public HomeReplaceGoal(GolemEntity golem) {
		super(golem);
	}

	@Override
	public boolean canUse() {
		ItemStack carried = this.golem.getCarried();
		if (carried.isEmpty() || !this.delayTick() || !this.navDone() || !this.atHome()) {
			return false;
		}
		if (GolemHelper.isOnTimeOut(this.golem, carried)) {
			return true;
		}
		switch (this.golem.getCore()) {
			case 1 -> {
				return !GolemHelper.findSomethingEmptyCore(this.golem, carried);
			}
			case 8 -> {
				return !GolemHelper.findSomethingUseCore(this.golem, carried);
			}
			case 10 -> {
				return !GolemHelper.findSomethingSortCore(this.golem, carried);
			}
			default -> {
				List<ItemStack> needed = GolemHelper.getItemsNeeded(this.golem, this.golem.getUpgradeAmount(5) > 0);
				if (needed == null || needed.isEmpty()) {
					return false;
				}
				for (ItemStack stack : needed) {
					if (GolemInventory.areItemStacksEqual(stack, this.golem.itemCarried, this.golem.checkOreDict(), this.golem.ignoreDamage(),
						this.golem.ignoreNBT())) {
						return false;
					}
				}
				return true;
			}
		}
	}
}
