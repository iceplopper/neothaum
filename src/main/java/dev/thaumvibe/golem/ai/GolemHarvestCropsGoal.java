package dev.thaumvibe.golem.ai;

import dev.thaumvibe.golem.GolemCoreType;
import dev.thaumvibe.golem.GolemEntity;
import dev.thaumvibe.golem.GolemUpgradeType;
import dev.thaumvibe.registry.TVSounds;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;

public class GolemHarvestCropsGoal extends Goal {
	private final GolemEntity golem;
	private BlockPos targetCrop;
	private int cooldown = 0;

	public GolemHarvestCropsGoal(GolemEntity golem) {
		this.golem = golem;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (this.golem.isInactive() || this.golem.getCore() != GolemCoreType.HARVEST.id) return false;
		if (this.cooldown-- > 0) return false;
		this.cooldown = 20;

		return this.findMatureCrop();
	}

	private boolean findMatureCrop() {
		BlockPos home = this.golem.getHomePos();
		if (home.equals(BlockPos.ZERO)) home = this.golem.blockPosition();

		int range = 10 + this.golem.getUpgradeCount(GolemUpgradeType.AIR) * 4;
		BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();

		double closest = Double.MAX_VALUE;
		BlockPos best = null;

		for (int dx = -range; dx <= range; dx++) {
			for (int dz = -range; dz <= range; dz++) {
				for (int dy = -2; dy <= 2; dy++) {
					mpos.set(home.getX() + dx, home.getY() + dy, home.getZ() + dz);
					BlockState state = this.golem.level().getBlockState(mpos);
					if (this.isMature(state)) {
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
			this.targetCrop = best;
			return true;
		}
		return false;
	}

	private boolean isMature(BlockState state) {
		Block block = state.getBlock();
		if (block instanceof CropBlock crop) {
			return crop.isMaxAge(state);
		}
		if (block instanceof NetherWartBlock) {
			return state.getValue(NetherWartBlock.AGE) >= 3;
		}
		return false;
	}

	@Override
	public boolean canContinueToUse() {
		return this.targetCrop != null && this.isMature(this.golem.level().getBlockState(this.targetCrop));
	}

	@Override
	public void tick() {
		if (this.targetCrop == null) return;

		this.golem.getLookControl().setLookAt(this.targetCrop.getX() + 0.5, this.targetCrop.getY() + 0.5, this.targetCrop.getZ() + 0.5);
		this.golem.getNavigation().moveTo(this.targetCrop.getX() + 0.5, this.targetCrop.getY() + 0.5, this.targetCrop.getZ() + 0.5, 1.0);

		if (this.golem.distanceToSqr(this.targetCrop.getX() + 0.5, this.targetCrop.getY() + 0.5, this.targetCrop.getZ() + 0.5) <= 4.0) {
			if (this.golem.level() instanceof ServerLevel sl) {
				BlockState state = sl.getBlockState(this.targetCrop);
				if (this.isMature(state)) {
					// Break crop
					Block.dropResources(state, sl, this.targetCrop);

					// Replant seed: reset crop age to 0
					if (state.getBlock() instanceof CropBlock crop) {
						sl.setBlock(this.targetCrop, crop.getStateForAge(0), 3);
					} else if (state.getBlock() instanceof NetherWartBlock) {
						sl.setBlock(this.targetCrop, state.setValue(NetherWartBlock.AGE, 0), 3);
					} else {
						sl.destroyBlock(this.targetCrop, true);
					}

					sl.playSound(null, this.targetCrop.getX(), this.targetCrop.getY(), this.targetCrop.getZ(),
						TVSounds.CAMERACLACK, SoundSource.BLOCKS, 0.5F, 1.0F);
				}
			}
			this.targetCrop = null;
		}
	}

	@Override
	public void stop() {
		this.targetCrop = null;
		this.golem.getNavigation().stop();
	}
}
