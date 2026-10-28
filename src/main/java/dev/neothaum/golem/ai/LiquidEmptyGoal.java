package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import dev.neothaum.golem.GolemHelper;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

/** AILiquidEmpty: back home with a liquid the home tank wants, pour it in. */
public class LiquidEmptyGoal extends GolemGoal {
	public LiquidEmptyGoal(GolemEntity golem) {
		super(golem);
	}

	@Override
	public boolean canUse() {
		return this.navDone() && this.golem.fluidCarried != null && this.golem.fluidAmount != 0 && this.atHome()
			&& GolemHelper.getMissingLiquids(this.golem).contains(this.golem.fluidCarried);
	}

	@Override
	public boolean canContinueToUse() {
		return false;
	}

	@Override
	public void start() {
		Storage<FluidVariant> tank = GolemHelper.fluidStorage(this.golem.level(), this.golem.getHomeContainerPos(), this.golem.getHomeFacingId());
		if (tank == null || this.golem.fluidCarried == null) {
			return;
		}
		long put;
		try (Transaction tx = Transaction.openOuter()) {
			put = tank.insert(FluidVariant.of(this.golem.fluidCarried), (long) this.golem.fluidAmount * GolemHelper.MB, tx);
			tx.commit();
		}
		int amt = (int) (put / GolemHelper.MB);
		this.golem.fluidAmount -= amt;
		if (this.golem.fluidAmount <= 0) {
			this.golem.fluidCarried = null;
			this.golem.fluidAmount = 0;
		}
		if (amt > 200) {
			this.golem.playSound(SoundEvents.GENERIC_SWIM, Math.min(0.2F, 0.2F * ((float) amt / this.golem.getFluidCarryLimit())),
				1.0F + (this.golem.getRandom().nextFloat() - this.golem.getRandom().nextFloat()) * 0.3F);
		}
		this.golem.updateCarried();
		this.golem.itemWatched = ItemStack.EMPTY;
	}
}
