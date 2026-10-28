package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

/** The shared shape of TC4's *Goto tasks: walk to a target for up to ten seconds, shuffling aside if stuck in the same block. */
public abstract class GotoGoal extends GolemGoal {
	protected double movePosX;
	protected double movePosY;
	protected double movePosZ;
	protected int count;
	private BlockPos prev = BlockPos.ZERO;

	protected GotoGoal(GolemEntity golem) {
		super(golem);
	}

	protected void setTarget(double x, double y, double z) {
		this.movePosX = x;
		this.movePosY = y;
		this.movePosZ = z;
	}

	@Override
	public boolean canContinueToUse() {
		return this.count > 0 && !this.navDone();
	}

	@Override
	public void tick() {
		this.count--;
		if (this.count == 0 && this.prev.equals(this.golem.blockPosition())) {
			Vec3 v = DefaultRandomPos.getPos(this.golem, 2, 1);
			if (v != null) {
				this.count = 20;
				this.moveTo(v.x, v.y, v.z);
			}
		}
	}

	@Override
	public void stop() {
		this.count = 0;
	}

	@Override
	public void start() {
		this.count = 200;
		this.prev = this.golem.blockPosition();
		this.moveTo(this.movePosX, this.movePosY, this.movePosZ);
	}
}
