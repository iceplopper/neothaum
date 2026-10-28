package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVItems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * AIHarvestCrops: walk the fields around home in a shuffled order, harvest each grown crop it finds and then its ripe neighbours. With an
 * order upgrade it replants from the drops: seeds back into the farmland round about, cocoa beans back onto jungle logs, mana beans back
 * where the pod was.
 */
public class HarvestCropsGoal extends BlockWorkGoal {
	private final List<BlockPos> checklist = new ArrayList<>();

	public HarvestCropsGoal(GolemEntity golem) {
		super(golem, 4.0F);
	}

	@Override
	public boolean canUse() {
		if (this.delay >= 0 || !this.delayTick() || !this.navDone()) {
			return false;
		}
		BlockPos p = this.findGrownCrop();
		if (p == null) {
			return false;
		}
		this.setTarget(p);
		return true;
	}

	@Override
	protected int workTime(float hardness) {
		return (int) Math.max(10.0F, (20.0F - this.golem.getGolemStrength() * 2.0F) * hardness);
	}

	@Override
	protected boolean stillTarget() {
		return false;
	}

	private BlockPos findGrownCrop() {
		BlockPos h = this.golem.getHomePos();
		if (this.checklist.isEmpty()) {
			for (int a = (int) -this.distance; a <= this.distance; a++) {
				for (int b = (int) -this.distance; b <= this.distance; b++) {
					this.checklist.add(new BlockPos(h.getX() + a, 0, h.getZ() + b));
				}
			}
			Collections.shuffle(this.checklist, new java.util.Random(this.golem.getRandom().nextLong()));
		}
		BlockPos c = this.checklist.remove(0);
		for (int y = h.getY() - 3; y <= h.getY() + 3; y++) {
			BlockPos p = new BlockPos(c.getX(), y, c.getZ());
			if (CropUtils.isGrownCrop(this.golem.level(), p)) {
				return p;
			}
		}
		return null;
	}

	@Override
	protected void harvest() {
		this.count = 200;
		FakePlayer fp = this.fakePlayer();
		var level = this.golem.level();
		if (CropUtils.isClickable(this.block)) {
			this.block.useWithoutItem(level, fp, new BlockHitResult(Vec3.atCenterOf(this.target), Direction.UP, this.target, false));
		} else {
			level.destroyBlock(this.target, true, this.golem);
			if (this.golem.getUpgradeAmount(4) > 0) {
				this.replant(fp);
			}
		}
		this.golem.startActionTimer();
		this.checkAdjacent();
	}

	private void replant(FakePlayer fp) {
		var level = this.golem.level();
		for (ItemEntity e : level.getEntitiesOfClass(ItemEntity.class, this.golem.getBoundingBox().inflate(6.0))) {
			if (e.tickCount < 2) {
				Vec3 v = e.position().subtract(this.golem.position()).normalize();
				e.setDeltaMovement(-v.x / 4.0, 0.075, -v.z / 4.0);
			}
			ItemStack st = e.getItem();
			boolean done = false;
			if (st.is(Items.COCOA_BEANS) && this.block.getBlock() instanceof CocoaBlock) {
				Direction facing = this.block.getValue(CocoaBlock.FACING);
				if (level.getBlockState(this.target.relative(facing)).is(BlockTags.JUNGLE_LOGS)) {
					st.shrink(1);
					level.setBlock(this.target, Blocks.COCOA.defaultBlockState().setValue(CocoaBlock.FACING, facing), 3);
				}
				done = true;
			} else if (st.is(TVItems.MANA_BEAN)) {
				if (this.block.is(TVBlocks.MANA_POD) && level.getBlockState(this.target).isAir()) {
					st.shrink(1);
					level.setBlock(this.target, TVBlocks.MANA_POD.defaultBlockState(), 3);
				}
				done = true;
			} else if (st.getItem() instanceof BlockItem bi && bi.getBlock() instanceof CropBlock) {
				int[] xm = {0, 0, 1, 1, -1, 0, -1, -1, 1};
				int[] zm = {0, 1, 0, 1, 0, -1, -1, 1, -1};
				for (int c = 0; !st.isEmpty() && c < 9; c++) {
					BlockPos soil = this.target.offset(xm[c], -1, zm[c]);
					fp.setItemInHand(InteractionHand.MAIN_HAND, st.copyWithCount(1));
					var ctx = new UseOnContext(fp, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(soil).add(0, 0.5, 0), Direction.UP, soil, false));
					if (st.getItem().useOn(ctx).consumesAction()) {
						st.shrink(1);
					}
				}
				fp.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
			}
			if (st.isEmpty()) {
				e.discard();
			} else {
				e.setItem(st);
			}
			if (done) {
				break;
			}
		}
	}

	private void checkAdjacent() {
		for (int x2 = -2; x2 <= 2; x2++) {
			for (int z2 = -2; z2 <= 2; z2++) {
				for (int y2 = -1; y2 <= 1; y2++) {
					BlockPos p = this.target.offset(x2, y2, z2);
					if (this.withinHome(p) && CropUtils.isGrownCrop(this.golem.level(), p)) {
						this.setTarget(p);
						this.delay = -1;
						this.start();
						return;
					}
				}
			}
		}
	}
}
