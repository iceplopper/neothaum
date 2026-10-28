package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import dev.neothaum.golem.GolemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

/** AIHomeDrop: with no container at home, toss what it carries onto the home spot. */
public class HomeDropGoal extends GolemGoal {
	private int count;

	public HomeDropGoal(GolemEntity golem) {
		super(golem);
	}

	@Override
	public boolean canUse() {
		return !this.golem.getCarried().isEmpty() && this.navDone() && this.atHome() && GolemHelper.getHomeContainer(this.golem) == null;
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
		toss(this.golem, this.golem.getHomeContainerPos());
	}

	/** Throw the carried stack at a block. */
	static void toss(GolemEntity golem, BlockPos c) {
		double y = golem.getY() + golem.getBbHeight() / 2.0F;
		ItemEntity item = new ItemEntity(golem.level(), golem.getX(), y, golem.getZ(), golem.itemCarried.copy());
		double distance = Math.sqrt(golem.distanceToSqr(c.getX() + 0.5, c.getY() + 0.5, c.getZ() + 0.5));
		item.setDeltaMovement((c.getX() + 0.5 - golem.getX()) * (distance / 3.0), 0.1 + (c.getY() + 0.5 - y) * (distance / 3.0),
			(c.getZ() + 0.5 - golem.getZ()) * (distance / 3.0));
		item.setPickUpDelay(10);
		golem.level().addFreshEntity(item);
		golem.itemCarried = ItemStack.EMPTY;
		golem.startActionTimer();
		golem.updateCarried();
	}
}
