package dev.thaumvibe.golem.ai;

import dev.thaumvibe.golem.GolemCoreType;
import dev.thaumvibe.golem.GolemEntity;
import dev.thaumvibe.golem.GolemUpgradeType;
import dev.thaumvibe.registry.TVSounds;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

public class GolemItemPickupGoal extends Goal {
	private final GolemEntity golem;
	private ItemEntity targetItem;
	private boolean returningHome = false;
	private int searchCooldown = 0;

	public GolemItemPickupGoal(GolemEntity golem) {
		this.golem = golem;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (this.golem.isInactive() || this.golem.getCore() != GolemCoreType.GATHER.id) return false;

		// If carrying item, need to deposit at home
		if (!this.golem.getCarried().isEmpty()) {
			this.returningHome = true;
			return true;
		}

		if (this.searchCooldown-- > 0) return false;
		this.searchCooldown = 15;

		return this.findTargetItem();
	}

	private boolean findTargetItem() {
		BlockPos home = this.golem.getHomePos();
		if (home.equals(BlockPos.ZERO)) home = this.golem.blockPosition();

		double range = 16.0 + this.golem.getUpgradeCount(GolemUpgradeType.AIR) * 8.0;
		AABB box = new AABB(home).inflate(range);
		List<ItemEntity> items = this.golem.level().getEntitiesOfClass(ItemEntity.class, box, e -> e.isAlive() && !e.hasPickUpDelay());

		double closest = Double.MAX_VALUE;
		ItemEntity best = null;

		for (ItemEntity item : items) {
			if (this.matchesFilter(item.getItem())) {
				double dist = this.golem.distanceToSqr(item);
				if (dist < closest) {
					closest = dist;
					best = item;
				}
			}
		}

		if (best != null) {
			this.targetItem = best;
			this.returningHome = false;
			return true;
		}
		return false;
	}

	private boolean matchesFilter(ItemStack stack) {
		boolean hasAnyFilter = false;
		int slotCount = this.golem.getFilterSlotCount();
		for (int i = 0; i < slotCount; i++) {
			ItemStack filter = this.golem.getFilterItem(i);
			if (!filter.isEmpty()) {
				hasAnyFilter = true;
				if (ItemStack.isSameItem(filter, stack)) {
					return true;
				}
			}
		}
		return !hasAnyFilter; // Empty filter means gather all items
	}

	@Override
	public boolean canContinueToUse() {
		if (this.returningHome) {
			return !this.golem.getCarried().isEmpty();
		}
		return this.targetItem != null && this.targetItem.isAlive();
	}

	@Override
	public void tick() {
		if (this.returningHome) {
			BlockPos home = this.golem.getHomePos();
			double distSq = this.golem.distanceToSqr(home.getX() + 0.5, home.getY() + 0.5, home.getZ() + 0.5);
			this.golem.getNavigation().moveTo(home.getX() + 0.5, home.getY() + 0.5, home.getZ() + 0.5, 1.0);

			if (distSq <= 4.0) {
				// Deposit carried item into container or drop
				ItemStack carried = this.golem.getCarried();
				BlockEntity be = this.golem.level().getBlockEntity(home);
				if (be instanceof Container container) {
					ItemStack remaining = this.insertIntoContainer(container, carried);
					this.golem.setCarried(remaining);
				} else {
					// Drop item near home
					this.golem.spawnAtLocation((net.minecraft.server.level.ServerLevel) this.golem.level(), carried);
					this.golem.setCarried(ItemStack.EMPTY);
				}
				this.golem.level().playSound(null, home.getX(), home.getY(), home.getZ(),
					TVSounds.CAMERACLACK, SoundSource.NEUTRAL, 0.5F, 1.2F);
				this.returningHome = false;
			}
		} else if (this.targetItem != null) {
			this.golem.getLookControl().setLookAt(this.targetItem, 30.0F, 30.0F);
			this.golem.getNavigation().moveTo(this.targetItem, 1.1);

			if (this.golem.distanceToSqr(this.targetItem) <= 2.25) {
				// Pick up item
				ItemStack stack = this.targetItem.getItem();
				int carryLimit = this.golem.getGolemType().carry * 16;
				int take = Math.min(stack.getCount(), carryLimit);

				ItemStack carried = stack.split(take);
				this.golem.setCarried(carried);
				if (stack.isEmpty()) {
					this.targetItem.discard();
				}
				this.golem.level().playSound(null, this.golem.getX(), this.golem.getY(), this.golem.getZ(),
					TVSounds.CAMERACLACK, SoundSource.NEUTRAL, 0.4F, 1.0F);

				this.targetItem = null;
				this.returningHome = true;
			}
		}
	}

	private ItemStack insertIntoContainer(Container container, ItemStack stack) {
		ItemStack remaining = stack.copy();
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack inSlot = container.getItem(i);
			if (inSlot.isEmpty()) {
				container.setItem(i, remaining);
				return ItemStack.EMPTY;
			} else if (ItemStack.isSameItemSameComponents(inSlot, remaining)) {
				int space = inSlot.getMaxStackSize() - inSlot.getCount();
				int toAdd = Math.min(space, remaining.getCount());
				if (toAdd > 0) {
					inSlot.grow(toAdd);
					remaining.shrink(toAdd);
					if (remaining.isEmpty()) return ItemStack.EMPTY;
				}
			}
		}
		return remaining;
	}

	@Override
	public void stop() {
		this.targetItem = null;
		this.golem.getNavigation().stop();
	}
}
