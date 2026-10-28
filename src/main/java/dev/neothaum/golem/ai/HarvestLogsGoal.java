package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import dev.neothaum.items.equipment.ToolHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;

/** AIHarvestLogs: find a log near home (the lowest of its trunk), chop at it, and fell the tree from the far end one log at a time. */
public class HarvestLogsGoal extends BlockWorkGoal {
	public HarvestLogsGoal(GolemEntity golem) {
		super(golem, 3.0F);
	}

	@Override
	public boolean canUse() {
		if (this.delay >= 0 || !this.delayTick() || !this.navDone()) {
			return false;
		}
		BlockPos p = this.findLog();
		if (p == null) {
			return false;
		}
		this.setTarget(p);
		return true;
	}

	@Override
	protected int workTime(float hardness) {
		return (int) Math.max(5.0F, (20.0F - this.golem.getGolemStrength() * 3.0F) * hardness);
	}

	@Override
	protected boolean stillTarget() {
		return ToolHelper.isWoodLog(this.golem.level(), this.target);
	}

	@Override
	protected void harvest() {
		this.count = 200;
		this.golem.level().levelEvent(2001, this.target, Block.getId(this.block));
		ToolHelper.breakFurthestBlock(this.golem.level(), this.target, this.block.getBlock(), this.fakePlayer(), false, 0);
		this.golem.startActionTimer();
		if (ToolHelper.isWoodLog(this.golem.level(), this.target)) {
			this.delay = -1;
			this.setTarget(this.target);
			this.start();
		} else {
			this.checkAdjacent();
		}
	}

	private void checkAdjacent() {
		for (int x2 = -1; x2 <= 1; x2++) {
			for (int z2 = -1; z2 <= 1; z2++) {
				for (int y2 = -1; y2 <= 1; y2++) {
					BlockPos p = this.target.offset(x2, y2, z2);
					if (this.withinHome(p) && ToolHelper.isWoodLog(this.golem.level(), p)) {
						this.setTarget(p);
						this.delay = -1;
						this.start();
						return;
					}
				}
			}
		}
	}

	private BlockPos findLog() {
		RandomSource rand = this.golem.getRandom();
		BlockPos h = this.golem.getHomePos();
		for (int i = 0; i < this.distance * 4.0F; i++) {
			int x = (int) (h.getX() + rand.nextInt((int) (1.0F + this.distance * 2.0F)) - this.distance);
			int y = (int) (h.getY() + rand.nextInt((int) (1.0F + this.distance)) - this.distance / 2.0F);
			int z = (int) (h.getZ() + rand.nextInt((int) (1.0F + this.distance * 2.0F)) - this.distance);
			BlockPos p = new BlockPos(x, y, z);
			if (ToolHelper.isWoodLog(this.golem.level(), p)) {
				BlockPos v = p;
				double dist = this.golem.distanceToSqr(x + 0.5, y + 0.5, z + 0.5);
				for (int yy = 1; ToolHelper.isWoodLog(this.golem.level(), p.below(yy))
					&& this.golem.distanceToSqr(x + 0.5, y - yy + 0.5, z + 0.5) < dist; yy++) {
					v = p.below(yy);
					dist = this.golem.distanceToSqr(x + 0.5, y - yy + 0.5, z + 0.5);
				}
				return v;
			}
		}
		return null;
	}
}
