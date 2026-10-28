package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import dev.neothaum.golem.GolemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

/** AILiquidGoto: with room for another bucketful, head for the nearest marked tank or source block of a liquid the home tank wants. */
public class LiquidGotoGoal extends GotoGoal {
	public LiquidGotoGoal(GolemEntity golem) {
		super(golem);
	}

	@Override
	public boolean canUse() {
		if (!this.delayTick() || this.golem.fluidCarried != null && this.golem.fluidAmount > this.golem.getFluidCarryLimit() - 1000) {
			return false;
		}
		for (Fluid fluid : GolemHelper.getMissingLiquids(this.golem)) {
			BlockPos v = GolemHelper.findPossibleLiquid(fluid, this.golem);
			if (v != null) {
				this.golem.itemWatched = new ItemStack(fluid.getBucket());
				double wx = v.getX();
				double wz = v.getZ();
				double dd = Math.sqrt(this.golem.distanceToSqr(wx, v.getY(), wz));
				for (int xx = -1; xx <= 1; xx++) {
					for (int zz = -1; zz <= 1; zz++) {
						double dd2 = Math.sqrt(this.golem.distanceToSqr(v.getX() + xx, v.getY(), v.getZ() + zz));
						BlockPos side = new BlockPos(v.getX() + xx, v.getY(), v.getZ() + zz);
						if (dd2 < dd && this.golem.level().getBlockState(side).isFaceSturdy(this.golem.level(), side, net.minecraft.core.Direction.UP)) {
							wx = v.getX() + xx;
							wz = v.getZ() + zz;
							dd = dd2;
						}
					}
				}
				this.setTarget(wx, v.getY(), wz);
				return true;
			}
		}
		return false;
	}
}
