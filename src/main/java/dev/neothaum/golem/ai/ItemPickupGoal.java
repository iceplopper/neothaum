package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import dev.neothaum.golem.GolemInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/** AIItemPickup: fetch the nearest loose item within range of home (that the filter wants, and that it can carry with what it holds). */
public class ItemPickupGoal extends GolemGoal {
	private Entity targetEntity;
	private int count;

	public ItemPickupGoal(GolemEntity golem) {
		super(golem);
	}

	@Override
	public boolean canUse() {
		return this.delayTick() && this.findItem();
	}

	private boolean findItem() {
		double range = Double.MAX_VALUE;
		float dmod = this.golem.getRange();
		BlockPos h = this.golem.getHomePos();
		this.targetEntity = null;
		for (ItemEntity e : this.golem.level().getEntitiesOfClass(ItemEntity.class, new AABB(h).inflate(dmod))) {
			ItemStack st = e.getItem();
			if (e.hasPickUpDelay()) {
				continue;
			}
			if (!this.golem.allEmpty() && this.golem.getAmountNeededSmart(st, this.golem.getUpgradeAmount(5) > 0) <= 0) {
				continue;
			}
			ItemStack carried = this.golem.getCarried();
			if (!carried.isEmpty() && !(GolemInventory.areItemStacksEqualStrict(carried, st) && st.getCount() <= this.golem.getCarrySpace())) {
				continue;
			}
			double distance = e.distanceToSqr(h.getX() + 0.5, h.getY() + 0.5, h.getZ() + 0.5);
			double distance2 = e.distanceToSqr(this.golem);
			if (distance2 < range && distance <= dmod * dmod) {
				range = distance2;
				this.targetEntity = e;
			}
		}
		return this.targetEntity != null;
	}

	@Override
	public boolean canContinueToUse() {
		return this.count-- > 0 && !this.navDone() && this.targetEntity.isAlive();
	}

	@Override
	public void stop() {
		this.count = 0;
		this.targetEntity = null;
		this.golem.getNavigation().stop();
	}

	@Override
	public void tick() {
		this.golem.getLookControl().setLookAt(this.targetEntity, 30.0F, 30.0F);
		if (this.golem.distanceToSqr(this.targetEntity) <= 2.0) {
			this.pickUp();
		}
	}

	private void pickUp() {
		if (!(this.targetEntity instanceof ItemEntity item)) {
			return;
		}
		ItemStack st = item.getItem();
		int amount = Math.min(st.getCount(), this.golem.getCarrySpace());
		if (amount <= 0) {
			return;
		}
		ItemStack stack = st.copyWithCount(amount);
		st.shrink(amount);
		item.setItem(st.isEmpty() ? ItemStack.EMPTY : st);
		if (st.isEmpty()) {
			item.discard();
		}
		if (this.golem.getCarried().isEmpty()) {
			this.golem.setCarried(stack);
		} else {
			this.golem.getCarried().grow(amount);
			this.golem.updateCarried();
		}
		item.playSound(SoundEvents.ITEM_PICKUP, 0.2F, ((this.golem.getRandom().nextFloat() - this.golem.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
	}

	@Override
	public void start() {
		this.count = 200;
		this.golem.getNavigation().moveTo(this.targetEntity, 1.0);
	}
}
