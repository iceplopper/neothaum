package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemCoreType;
import dev.neothaum.golem.GolemEntity;
import dev.neothaum.golem.GolemUpgradeType;
import dev.neothaum.registry.TVSounds;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class GolemFillGoal extends Goal {
	private final GolemEntity golem;
	private BlockPos sourceContainerPos;
	private boolean returningHome = false;
	private int cooldown = 0;

	public GolemFillGoal(GolemEntity golem) {
		this.golem = golem;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		int core = this.golem.getCore();
		if (this.golem.isInactive() || (core != GolemCoreType.FILL.id && core != GolemCoreType.EMPTY.id)) {
			return false;
		}

		if (!this.golem.getCarried().isEmpty()) {
			this.returningHome = true;
			return true;
		}

		if (this.cooldown-- > 0) return false;
		this.cooldown = 20;

		return this.findContainerTask();
	}

	private boolean findContainerTask() {
		BlockPos home = this.golem.getHomePos();
		if (home.equals(BlockPos.ZERO)) return false;

		BlockEntity homeBe = this.golem.level().getBlockEntity(home);
		if (!(homeBe instanceof Container homeContainer)) return false;

		int core = this.golem.getCore();
		int range = 12 + this.golem.getUpgradeCount(GolemUpgradeType.AIR) * 4;

		if (core == GolemCoreType.FILL.id) {
			// Find items needed in home container
			for (int f = 0; f < this.golem.getFilterSlotCount(); f++) {
				ItemStack filter = this.golem.getFilterItem(f);
				if (!filter.isEmpty()) {
					int countInHome = this.countInContainer(homeContainer, filter);
					boolean anyAmount = this.golem.getToggle(0);
					if (anyAmount ? countInHome == 0 : countInHome < filter.getCount()) {
						// Need this item! Look for a nearby source container with it
						BlockPos found = this.findContainerWithItem(home, range, filter);
						if (found != null) {
							this.sourceContainerPos = found;
							this.returningHome = false;
							return true;
						}
					}
				}
			}
		} else if (core == GolemCoreType.EMPTY.id) {
			// Take items from home container matching filter
			for (int h = 0; h < homeContainer.getContainerSize(); h++) {
				ItemStack inHome = homeContainer.getItem(h);
				if (!inHome.isEmpty() && this.matchesFilter(inHome)) {
					// Take item from home
					int carryLimit = this.golem.getGolemType().carry * 16;
					int take = Math.min(inHome.getCount(), carryLimit);
					ItemStack taken = inHome.split(take);
					this.golem.setCarried(taken);
					this.golem.level().playSound(null, home.getX(), home.getY(), home.getZ(),
						TVSounds.CAMERACLACK, SoundSource.NEUTRAL, 0.4F, 1.0F);

					// Find destination container
					BlockPos dest = this.findDestinationContainer(home, range, taken);
					this.sourceContainerPos = dest != null ? dest : home.relative(this.golem.getHomeFacing());
					this.returningHome = false;
					return true;
				}
			}
		}

		return false;
	}

	private boolean matchesFilter(ItemStack stack) {
		boolean hasAnyFilter = false;
		for (int i = 0; i < this.golem.getFilterSlotCount(); i++) {
			ItemStack filter = this.golem.getFilterItem(i);
			if (!filter.isEmpty()) {
				hasAnyFilter = true;
				if (ItemStack.isSameItem(filter, stack)) return true;
			}
		}
		return !hasAnyFilter;
	}

	private int countInContainer(Container container, ItemStack match) {
		int total = 0;
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack s = container.getItem(i);
			if (ItemStack.isSameItem(s, match)) total += s.getCount();
		}
		return total;
	}

	private BlockPos findContainerWithItem(BlockPos home, int range, ItemStack match) {
		BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
		for (int dx = -range; dx <= range; dx++) {
			for (int dz = -range; dz <= range; dz++) {
				for (int dy = -2; dy <= 2; dy++) {
					mpos.set(home.getX() + dx, home.getY() + dy, home.getZ() + dz);
					if (mpos.equals(home)) continue;
					if (this.golem.level().getBlockEntity(mpos) instanceof Container c) {
						if (this.countInContainer(c, match) > 0) {
							return mpos.immutable();
						}
					}
				}
			}
		}
		return null;
	}

	private BlockPos findDestinationContainer(BlockPos home, int range, ItemStack stack) {
		BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
		for (int dx = -range; dx <= range; dx++) {
			for (int dz = -range; dz <= range; dz++) {
				for (int dy = -2; dy <= 2; dy++) {
					mpos.set(home.getX() + dx, home.getY() + dy, home.getZ() + dz);
					if (mpos.equals(home)) continue;
					if (this.golem.level().getBlockEntity(mpos) instanceof Container c) {
						return mpos.immutable();
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean canContinueToUse() {
		return this.returningHome ? !this.golem.getCarried().isEmpty() : this.sourceContainerPos != null;
	}

	@Override
	public void tick() {
		if (this.returningHome) {
			BlockPos home = this.golem.getHomePos();
			this.golem.getNavigation().moveTo(home.getX() + 0.5, home.getY() + 0.5, home.getZ() + 0.5, 1.0);
			if (this.golem.distanceToSqr(home.getX() + 0.5, home.getY() + 0.5, home.getZ() + 0.5) <= 4.0) {
				if (this.golem.level().getBlockEntity(home) instanceof Container c) {
					ItemStack rem = this.insertIntoContainer(c, this.golem.getCarried());
					this.golem.setCarried(rem);
				}
				this.returningHome = false;
			}
		} else if (this.sourceContainerPos != null) {
			this.golem.getNavigation().moveTo(this.sourceContainerPos.getX() + 0.5, this.sourceContainerPos.getY() + 0.5, this.sourceContainerPos.getZ() + 0.5, 1.0);
			if (this.golem.distanceToSqr(this.sourceContainerPos.getX() + 0.5, this.sourceContainerPos.getY() + 0.5, this.sourceContainerPos.getZ() + 0.5) <= 4.0) {
				if (this.golem.getCore() == GolemCoreType.FILL.id) {
					// Take item from source container
					if (this.golem.level().getBlockEntity(this.sourceContainerPos) instanceof Container src) {
						for (int i = 0; i < src.getContainerSize(); i++) {
							ItemStack s = src.getItem(i);
							if (!s.isEmpty() && this.matchesFilter(s)) {
								int carryLimit = this.golem.getGolemType().carry * 16;
								int take = Math.min(s.getCount(), carryLimit);
								this.golem.setCarried(s.split(take));
								this.golem.level().playSound(null, this.sourceContainerPos.getX(), this.sourceContainerPos.getY(), this.sourceContainerPos.getZ(),
									TVSounds.CAMERACLACK, SoundSource.NEUTRAL, 0.4F, 1.0F);
								break;
							}
						}
					}
					this.returningHome = true;
				} else if (this.golem.getCore() == GolemCoreType.EMPTY.id) {
					// Deposit item into destination container
					if (this.golem.level().getBlockEntity(this.sourceContainerPos) instanceof Container dst) {
						ItemStack rem = this.insertIntoContainer(dst, this.golem.getCarried());
						this.golem.setCarried(rem);
					} else {
						this.golem.spawnAtLocation((net.minecraft.server.level.ServerLevel) this.golem.level(), this.golem.getCarried());
						this.golem.setCarried(ItemStack.EMPTY);
					}
					this.sourceContainerPos = null;
				}
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
		this.sourceContainerPos = null;
		this.golem.getNavigation().stop();
	}
}
