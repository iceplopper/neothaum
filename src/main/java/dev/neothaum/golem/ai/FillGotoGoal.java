package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import dev.neothaum.golem.GolemHelper;
import dev.neothaum.golem.GolemInventory;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

/** AIFillGoto: find the nearest marked container (not home) holding something the home container is missing, and walk there. */
public class FillGotoGoal extends GotoGoal {
	public FillGotoGoal(GolemEntity golem) {
		super(golem);
	}

	@Override
	public boolean canUse() {
		if (!this.golem.getCarried().isEmpty() || !this.delayTick() || !this.golem.hasSomething()) {
			return false;
		}
		List<ItemStack> mi = GolemHelper.getMissingItems(this.golem);
		if (mi == null || mi.isEmpty()) {
			return false;
		}
		List<ItemStack> missing = new ArrayList<>();
		for (ItemStack s : mi) {
			if (this.golem.getUpgradeAmount(5) > 0) {
				missing.addAll(GolemInventory.oreVariants(s));
			} else {
				missing.add(s.copy());
			}
		}
		List<BlockPos> results = new ArrayList<>();
		for (ItemStack s : missing) {
			this.golem.itemWatched = s.copy();
			for (byte color : this.golem.getColorsMatching(this.golem.itemWatched)) {
				results = GolemHelper.getContainersWithGoods(this.golem, this.golem.itemWatched, color);
			}
			if (!results.isEmpty()) {
				break;
			}
		}
		BlockPos dest = null;
		double range = Double.MAX_VALUE;
		float dmod = this.golem.getRange();
		for (BlockPos p : results) {
			double distance = GolemHelper.distSqr(this.golem, p);
			if (distance < range && distance <= dmod * dmod && !GolemHelper.isHomeContainer(this.golem, p)) {
				range = distance;
				dest = p;
			}
		}
		if (dest == null) {
			return false;
		}
		this.setTarget(dest.getX(), dest.getY(), dest.getZ());
		return true;
	}
}
