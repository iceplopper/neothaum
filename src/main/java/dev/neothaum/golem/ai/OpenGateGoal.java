package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;

/** AIOpenDoor for fence gates: a golem bumping into an oak fence gate opens it, and shuts it behind itself (doors use vanilla OpenDoorGoal). */
public class OpenGateGoal extends Goal {
	private final GolemEntity golem;
	private BlockPos gate;
	private int ticks;

	public OpenGateGoal(GolemEntity golem) {
		this.golem = golem;
		this.setFlags(EnumSet.noneOf(Goal.Flag.class));
	}

	@Override
	public boolean canUse() {
		if (!this.golem.horizontalCollision) {
			return false;
		}
		for (BlockPos p : BlockPos.betweenClosed(this.golem.blockPosition().offset(-1, 0, -1), this.golem.blockPosition().offset(1, 1, 1))) {
			BlockState s = this.golem.level().getBlockState(p);
			if (s.is(Blocks.OAK_FENCE_GATE) && !s.getValue(FenceGateBlock.OPEN)) {
				this.gate = p.immutable();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canContinueToUse() {
		return this.ticks > 0;
	}

	@Override
	public void start() {
		this.ticks = 20;
		BlockState s = this.golem.level().getBlockState(this.gate);
		if (s.is(Blocks.OAK_FENCE_GATE)) {
			var facing = net.minecraft.core.Direction.from2DDataValue(Mth.floor(this.golem.getYRot() * 4.0F / 360.0F + 0.5) & 3);
			if (s.getValue(FenceGateBlock.FACING) == facing.getOpposite()) {
				s = s.setValue(FenceGateBlock.FACING, facing);
			}
			this.golem.level().setBlock(this.gate, s.setValue(FenceGateBlock.OPEN, true), 3);
			this.golem.level().playSound(null, this.gate, SoundEvents.FENCE_GATE_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
		}
	}

	@Override
	public void tick() {
		this.ticks--;
	}

	@Override
	public void stop() {
		BlockState s = this.golem.level().getBlockState(this.gate);
		if (s.is(Blocks.OAK_FENCE_GATE) && s.getValue(FenceGateBlock.OPEN)) {
			this.golem.level().setBlock(this.gate, s.setValue(FenceGateBlock.OPEN, false), 3);
			this.golem.level().playSound(null, this.gate, SoundEvents.FENCE_GATE_CLOSE, SoundSource.BLOCKS, 1.0F, 1.0F);
		}
	}
}
