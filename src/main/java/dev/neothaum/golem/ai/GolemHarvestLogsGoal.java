package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemCoreType;
import dev.neothaum.golem.GolemEntity;
import dev.neothaum.golem.GolemUpgradeType;
import dev.neothaum.registry.TVSounds;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class GolemHarvestLogsGoal extends Goal {
	private final GolemEntity golem;
	private BlockPos targetLog;
	private int cooldown = 0;

	public GolemHarvestLogsGoal(GolemEntity golem) {
		this.golem = golem;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (this.golem.isInactive() || this.golem.getCore() != GolemCoreType.CHOP.id) return false;
		if (this.cooldown-- > 0) return false;
		this.cooldown = 20;

		return this.findTreeLog();
	}

	private boolean findTreeLog() {
		BlockPos home = this.golem.getHomePos();
		if (home.equals(BlockPos.ZERO)) home = this.golem.blockPosition();

		int range = 12 + this.golem.getUpgradeCount(GolemUpgradeType.AIR) * 4;
		BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();

		double closest = Double.MAX_VALUE;
		BlockPos best = null;

		for (int dx = -range; dx <= range; dx++) {
			for (int dz = -range; dz <= range; dz++) {
				for (int dy = -1; dy <= 4; dy++) {
					mpos.set(home.getX() + dx, home.getY() + dy, home.getZ() + dz);
					BlockState state = this.golem.level().getBlockState(mpos);
					if (state.is(BlockTags.LOGS)) {
						double dist = this.golem.distanceToSqr(mpos.getX() + 0.5, mpos.getY() + 0.5, mpos.getZ() + 0.5);
						if (dist < closest) {
							closest = dist;
							best = mpos.immutable();
						}
					}
				}
			}
		}

		if (best != null) {
			this.targetLog = best;
			return true;
		}
		return false;
	}

	@Override
	public boolean canContinueToUse() {
		return this.targetLog != null && this.golem.level().getBlockState(this.targetLog).is(BlockTags.LOGS);
	}

	@Override
	public void tick() {
		if (this.targetLog == null) return;

		this.golem.getLookControl().setLookAt(this.targetLog.getX() + 0.5, this.targetLog.getY() + 0.5, this.targetLog.getZ() + 0.5);
		this.golem.getNavigation().moveTo(this.targetLog.getX() + 0.5, this.targetLog.getY() + 0.5, this.targetLog.getZ() + 0.5, 1.0);

		if (this.golem.distanceToSqr(this.targetLog.getX() + 0.5, this.targetLog.getY() + 0.5, this.targetLog.getZ() + 0.5) <= 5.0) {
			if (this.golem.level() instanceof ServerLevel sl) {
				BlockState state = sl.getBlockState(this.targetLog);
				if (state.is(BlockTags.LOGS)) {
					sl.destroyBlock(this.targetLog, true);
					sl.playSound(null, this.targetLog.getX(), this.targetLog.getY(), this.targetLog.getZ(),
						TVSounds.CAMERACLACK, SoundSource.BLOCKS, 0.5F, 0.9F);
				}
			}
			this.targetLog = null;
		}
	}

	@Override
	public void stop() {
		this.targetLog = null;
		this.golem.getNavigation().stop();
	}
}
