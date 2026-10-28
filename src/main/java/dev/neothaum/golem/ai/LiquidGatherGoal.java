package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import dev.neothaum.golem.GolemHelper;
import dev.neothaum.golem.GolemMarker;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;

/**
 * AILiquidGather: beside a marked tank or source block of a wanted liquid, fill up from it over a second or two. With an entropy upgrade it
 * pumps a marked pool from the far end inwards, so the marked block is taken last.
 */
public class LiquidGatherGoal extends GolemGoal {
	private int count;
	private final Map<BlockPos, List<BlockPos>> queue = new HashMap<>();

	public LiquidGatherGoal(GolemEntity golem) {
		super(golem);
	}

	private int carried() {
		return this.golem.fluidCarried == null ? 0 : this.golem.fluidAmount;
	}

	@Override
	public boolean canUse() {
		List<Fluid> fluids = GolemHelper.getMissingLiquids(this.golem);
		if (this.golem.itemWatched.isEmpty() || fluids.isEmpty() || !this.navDone()) {
			return false;
		}
		int room = this.golem.getFluidCarryLimit() - this.carried();
		for (Fluid fluid : fluids) {
			for (GolemMarker m : GolemHelper.getMarkedFluidHandlersAdjacentToGolem(fluid, this.golem)) {
				Storage<FluidVariant> s = GolemHelper.fluidStorage(this.golem.level(), m.pos(), m.side());
				if (s != null && GolemHelper.drainable(s, fluid, room * GolemHelper.MB) > 0) {
					return true;
				}
			}
			for (BlockPos loc : GolemHelper.getMarkedBlocksAdjacentToGolem(this.golem, (byte) -1)) {
				if (GolemHelper.isFluidSource(this.golem.level(), loc, fluid)) {
					return room >= 1000;
				}
			}
		}
		return false;
	}

	@Override
	public boolean canContinueToUse() {
		return this.count < 20 && !this.golem.itemWatched.isEmpty();
	}

	@Override
	public void start() {
		this.count = 0;
	}

	@Override
	public void stop() {
		this.count = 0;
		this.golem.itemWatched = ItemStack.EMPTY;
	}

	private void take(Fluid fluid, int mb) {
		if (this.golem.fluidCarried != null && this.golem.fluidAmount > 0) {
			this.golem.fluidAmount += mb;
		} else {
			this.golem.fluidCarried = fluid;
			this.golem.fluidAmount = mb;
		}
		this.golem.updateCarried();
		this.count = 0;
	}

	@Override
	public void tick() {
		this.count++;
		if (this.count < 10) {
			return;
		}
		Level level = this.golem.level();
		int max = this.golem.getFluidCarryLimit();
		for (Fluid fluid : GolemHelper.getMissingLiquids(this.golem)) {
			for (GolemMarker m : GolemHelper.getMarkedFluidHandlersAdjacentToGolem(fluid, this.golem)) {
				Storage<FluidVariant> s = GolemHelper.fluidStorage(level, m.pos(), m.side());
				if (s == null) {
					continue;
				}
				long got;
				try (Transaction tx = Transaction.openOuter()) {
					got = s.extract(FluidVariant.of(fluid), (long) (max - this.carried()) * GolemHelper.MB, tx);
					tx.commit();
				}
				int mb = (int) (got / GolemHelper.MB);
				if (mb > 0) {
					if (mb > 200) {
						this.golem.playSound(SoundEvents.GENERIC_SWIM, 0.2F * ((float) mb / max), 1.0F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.3F);
					}
					this.take(fluid, mb);
					if (this.golem.fluidAmount >= max) {
						this.golem.itemWatched = ItemStack.EMPTY;
					}
				}
			}
			for (BlockPos loc : GolemHelper.getMarkedBlocksAdjacentToGolem(this.golem, (byte) -1)) {
				BlockPos at = loc;
				if (this.golem.getUpgradeAmount(5) > 0) {
					List<BlockPos> t = this.queue.get(loc);
					if (t == null || t.isEmpty()) {
						t = this.rebuildQueue(loc, fluid);
						this.queue.put(loc, t);
					}
					while (!t.isEmpty()) {
						at = t.remove(0);
						if (GolemHelper.isFluidSource(level, at, fluid)) {
							break;
						}
					}
				}
				if (GolemHelper.isFluidSource(level, at, fluid) && max - this.carried() >= 1000) {
					level.setBlock(at, Blocks.AIR.defaultBlockState(), 3);
					this.golem.playSound(SoundEvents.GENERIC_SWIM, 0.2F, 1.0F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.3F);
					this.take(fluid, 1000);
					if (this.golem.fluidAmount > max - 1000) {
						this.golem.itemWatched = ItemStack.EMPTY;
					}
				}
			}
		}
	}

	/** The pool's source blocks connected to the marked one, furthest first. */
	private List<BlockPos> rebuildQueue(BlockPos origin, Fluid fluid) {
		float pumpDist = this.golem.getRange() * this.golem.getRange();
		Set<BlockPos> seen = new HashSet<>();
		List<BlockPos> sources = new ArrayList<>();
		List<BlockPos> open = new ArrayList<>();
		open.add(origin);
		seen.add(origin);
		Level level = this.golem.level();
		while (!open.isEmpty() && seen.size() < 4096) {
			BlockPos p = open.remove(open.size() - 1);
			for (BlockPos n : BlockPos.betweenClosed(p.offset(-1, -1, -1), p.offset(1, 1, 1))) {
				BlockPos q = n.immutable();
				if (seen.contains(q) || q.distSqr(origin) > pumpDist || !level.getFluidState(q).getType().isSame(fluid)) {
					continue;
				}
				seen.add(q);
				if (GolemHelper.isFluidSource(level, q, fluid)) {
					sources.add(q);
				}
				open.add(q);
			}
		}
		sources.sort(Comparator.comparingDouble((BlockPos q) -> q.distSqr(origin)).reversed());
		return sources;
	}

	static Direction side(int s) {
		return Direction.from3DDataValue(s);
	}
}
