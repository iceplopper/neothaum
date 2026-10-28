package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import dev.neothaum.golem.GolemHelper;
import net.minecraft.core.BlockPos;

/** AIEmptyDrop: beside a marked block that is not a container (in one of the item's colours), toss what it carries onto it. */
public class EmptyDropGoal extends GolemGoal {
	private int count;

	public EmptyDropGoal(GolemEntity golem) {
		super(golem);
	}

	private BlockPos spot() {
		BlockPos home = this.golem.getHomePos();
		for (byte color : this.golem.getColorsMatching(this.golem.itemCarried)) {
			for (BlockPos cc : GolemHelper.getMarkedBlocksAdjacentToGolem(this.golem, color)) {
				if (!cc.equals(home)) {
					return cc;
				}
			}
		}
		return null;
	}

	@Override
	public boolean canUse() {
		return !this.golem.itemCarried.isEmpty() && this.navDone() && this.spot() != null;
	}

	@Override
	public boolean canContinueToUse() {
		return this.count > 0 && this.canUse();
	}

	@Override
	public void tick() {
		this.count--;
	}

	@Override
	public void start() {
		this.count = 200;
		BlockPos cc = this.spot();
		if (cc != null) {
			HomeDropGoal.toss(this.golem, cc);
		}
	}
}
