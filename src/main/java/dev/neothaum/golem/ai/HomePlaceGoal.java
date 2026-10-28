package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import dev.neothaum.golem.GolemHelper;
import dev.neothaum.golem.GolemInventory;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/** AIHomePlace: back home, put what it carries into the home container. */
public class HomePlaceGoal extends GolemGoal {
	protected int countChest;

	public HomePlaceGoal(GolemEntity golem) {
		super(golem);
	}

	@Override
	public boolean canUse() {
		if (this.golem.getCarried().isEmpty() || !this.delayTick() || !this.navDone() || !this.atHome()) {
			return false;
		}
		Container inv = GolemHelper.getHomeContainer(this.golem);
		return inv != null && !ItemStack.matches(GolemInventory.placeItemStackIntoInventory(this.golem.getCarried(), inv, this.golem.getHomeFacingId(),
			false), this.golem.itemCarried);
	}

	@Override
	public boolean canContinueToUse() {
		return this.canUse() || this.countChest > 0;
	}

	@Override
	public void stop() {
		this.golem.closeContainer();
	}

	@Override
	public void tick() {
		this.countChest--;
	}

	@Override
	public void start() {
		Container inv = GolemHelper.getHomeContainer(this.golem);
		if (inv == null) {
			return;
		}
		ItemStack result = GolemInventory.placeItemStackIntoInventory(this.golem.getCarried(), inv, this.golem.getHomeFacingId(), true);
		if (!ItemStack.matches(result, this.golem.itemCarried)) {
			this.golem.setCarried(result);
			this.golem.openContainer(this.golem.getHomeContainerPos());
			this.countChest = 5;
		}
	}
}
