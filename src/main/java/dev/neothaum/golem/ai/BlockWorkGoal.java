package dev.neothaum.golem.ai;

import com.mojang.authlib.GameProfile;
import dev.neothaum.golem.GolemEntity;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The shared shape of AIHarvestCrops and AIHarvestLogs: walk to a block, then work at it for a while (its hardness, less for strong golems)
 * with knocking sounds and cracks, and break it.
 */
public abstract class BlockWorkGoal extends GolemGoal {
	public static final GameProfile FAKE_PROFILE = new GameProfile(UUID.nameUUIDFromBytes("FakeThaumcraftGolem".getBytes()), "FakeThaumcraftGolem");
	protected BlockPos target = BlockPos.ZERO;
	protected BlockState block = Blocks.AIR.defaultBlockState();
	protected int delay = -1;
	protected int maxDelay = 1;
	protected int mod = 1;
	protected int count;
	protected final float distance;

	protected BlockWorkGoal(GolemEntity golem, float rangeDivisor) {
		super(golem);
		this.distance = (float) Math.ceil(golem.getRange() / rangeDivisor);
	}

	protected FakePlayer fakePlayer() {
		FakePlayer fp = FakePlayer.get((ServerLevel) this.golem.level(), FAKE_PROFILE);
		fp.snapTo(this.golem.getX(), this.golem.getY(), this.golem.getZ(), this.golem.getYRot(), this.golem.getXRot());
		return fp;
	}

	protected void setTarget(BlockPos p) {
		this.target = p.immutable();
		this.block = this.golem.level().getBlockState(p);
	}

	/** How long working the block takes: hardness times (base - strength * per), at least min. */
	protected abstract int workTime(float hardness);

	protected abstract boolean stillTarget();

	/** Breaks the block; then (if this returns false) the task looks for the next one. */
	protected abstract void harvest();

	@Override
	public boolean canContinueToUse() {
		return this.golem.level().getBlockState(this.target) == this.block && this.count-- > 0 && (this.delay > 0 || this.stillTarget() || !this.navDone());
	}

	@Override
	public void tick() {
		double dist = this.golem.distanceToSqr(this.target.getX() + 0.5, this.target.getY() + 0.5, this.target.getZ() + 0.5);
		this.golem.getLookControl().setLookAt(this.target.getX() + 0.5, this.target.getY() + 0.5, this.target.getZ() + 0.5, 30.0F, 30.0F);
		if (dist > 4.0) {
			return;
		}
		if (this.delay < 0) {
			this.delay = this.workTime(this.block.getDestroySpeed(this.golem.level(), this.target));
			this.maxDelay = this.delay;
			this.mod = Math.max(1, this.delay / Math.max(1, Math.round(this.delay / 6.0F)));
		}
		if (this.delay > 0) {
			if (--this.delay > 0 && this.delay % this.mod == 0 && this.navDone()) {
				this.golem.startActionTimer();
				SoundType st = this.block.getSoundType();
				this.golem.level().playSound(null, this.target, st.getHitSound(), SoundSource.BLOCKS, (st.getVolume() + 0.7F) / 8.0F, st.getPitch() * 0.5F);
				((ServerLevel) this.golem.level()).destroyBlockProgress(this.golem.getId(), this.target,
					(int) (9.0F * (1.0F - (float) this.delay / this.maxDelay)));
			}
			if (this.delay == 0) {
				this.harvest();
			}
		}
	}

	@Override
	public void stop() {
		((ServerLevel) this.golem.level()).destroyBlockProgress(this.golem.getId(), this.target, -1);
		this.delay = -1;
	}

	@Override
	public void start() {
		this.count = 200;
		this.moveTo(this.target.getX() + 0.5, this.target.getY() + 0.5, this.target.getZ() + 0.5);
	}

	protected boolean withinHome(BlockPos p) {
		BlockPos h = this.golem.getHomePos();
		return Math.abs(h.getX() - p.getX()) <= this.distance && Math.abs(h.getY() - p.getY()) <= this.distance
			&& Math.abs(h.getZ() - p.getZ()) <= this.distance;
	}
}
