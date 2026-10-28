package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import dev.neothaum.golem.GolemHelper;
import dev.neothaum.golem.GolemInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;

/** AISortingGoto: carrying something, walk to the nearest marked container that has room for it and already holds some. */
public class SortingGotoGoal extends GotoGoal {
	public SortingGotoGoal(GolemEntity golem) {
		super(golem);
	}

	@Override
	public boolean canUse() {
		if (this.golem.itemCarried.isEmpty() || !this.delayTick()) {
			return false;
		}
		BlockPos dest = null;
		double range = Double.MAX_VALUE;
		float dmod = this.golem.getRange();
		for (BlockPos p : GolemHelper.getContainersWithRoom(this.golem, (byte) -1, this.golem.itemCarried)) {
			double distance = GolemHelper.distSqr(this.golem, p);
			if (distance < range && distance <= dmod * dmod && !GolemHelper.isHomeContainer(this.golem, p)) {
				Container inv = GolemInventory.getContainer(this.golem.level(), p);
				for (int side : GolemHelper.getMarkedSides(this.golem, p, (byte) -1)) {
					if (GolemInventory.inventoryContains(inv, this.golem.itemCarried, side, this.golem.checkOreDict(), this.golem.ignoreDamage(),
						this.golem.ignoreNBT())) {
						dest = p;
						range = distance;
						break;
					}
				}
			}
		}
		if (dest == null) {
			return false;
		}
		this.setTarget(dest.getX(), dest.getY(), dest.getZ());
		return true;
	}
}
