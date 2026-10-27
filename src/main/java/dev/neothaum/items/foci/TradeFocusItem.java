package dev.neothaum.items.foci;

import dev.neothaum.NeoThaum;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.api.wands.Architect;
import dev.neothaum.api.wands.FocusItem;
import dev.neothaum.api.wands.FocusUpgradeType;
import dev.neothaum.blocks.TaintBlock;
import dev.neothaum.items.WandItem;
import dev.neothaum.registry.TVComponents;
import dev.neothaum.wands.BlockSwapper;
import dev.neothaum.wands.WandFoci;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * ItemFocusTrade, the Wand Focus: Equal Trade. Sneak-click to pick a block; click to swap it into the world, spreading across matching exposed
 * blocks (or, with the architect upgrade, across a sized area), and left-click to swap a single block.
 */
public class TradeFocusItem extends FocusItem implements Architect {
	private static final AspectList COST = new AspectList().add(Aspect.ENTROPY, 5).add(Aspect.EARTH, 5).add(Aspect.ORDER, 5);
	private static final AspectList COST_SILK = new AspectList().add(Aspect.AIR, 1).add(Aspect.FIRE, 1).add(Aspect.EARTH, 6).add(Aspect.WATER, 1)
		.add(Aspect.ORDER, 6).add(Aspect.ENTROPY, 6);
	private static final Identifier ORNAMENT = NeoThaum.id("items/focus_trade_orn");

	public TradeFocusItem(Item.Properties props) {
		super(props);
	}

	@Override
	public String getSortingHelper(ItemStack focus) {
		return "BT" + super.getSortingHelper(focus);
	}

	@Override
	public Identifier getOrnament(ItemStack focus) {
		return ORNAMENT;
	}

	@Override
	public int getFocusColor(ItemStack focus) {
		return 8747923;
	}

	@Override
	public AspectList getVisCost(ItemStack focus) {
		return this.isUpgradedWith(focus, FocusUpgradeType.silktouch) ? COST_SILK : COST;
	}

	/** The picked block lives on the wand, as in TC4. */
	public static ItemStack getPickedBlock(ItemStack wand) {
		ItemStack s = wand.get(TVComponents.PICKED_BLOCK);
		return s == null ? ItemStack.EMPTY : s;
	}

	private static BlockHitResult reachTarget(Level level, Player p) {
		Vec3 eye = p.getEyePosition();
		Vec3 end = eye.add(p.getViewVector(1.0F).scale(p.blockInteractionRange()));
		BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, p));
		return hit.getType() == HitResult.Type.BLOCK ? hit : null;
	}

	private static boolean swappable(Level level, BlockPos pos) {
		return level.getBlockEntity(pos) == null && !(level.getBlockState(pos).getBlock() instanceof TaintBlock);
	}

	@Override
	public boolean onFocusRightClick(ItemStack wand, Level level, Player player, HitResult ignored) {
		BlockHitResult mop = reachTarget(level, player);
		if (mop == null) {
			return false;
		}
		BlockPos pos = mop.getBlockPos();
		BlockState bs = level.getBlockState(pos);
		if (player.isShiftKeyDown()) {
			if (!level.isClientSide() && level.getBlockEntity(pos) == null && !bs.isAir()) {
				ItemStack picked = bs.getCloneItemStack(level, pos, false);
				if (!picked.isEmpty()) {
					wand.set(TVComponents.PICKED_BLOCK, picked.copyWithCount(1));
				}
			} else {
				player.swing(FireFocusItem.handOf(player, wand), net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
			}
			return true;
		}
		ItemStack pb = getPickedBlock(wand);
		if (!pb.isEmpty() && level.isClientSide()) {
			player.swing(FireFocusItem.handOf(player, wand), net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
		} else if (!pb.isEmpty() && swappable(level, pos) && level instanceof ServerLevel sl && player instanceof ServerPlayer sp) {
			int slot = sp.getInventory().getSelectedSlot();
			if (this.isUpgradedWith(WandItem.getFocusStack(wand), FocusUpgradeType.architect)) {
				for (BlockPos c : this.getArchitectBlocks(wand, level, pos, mop.getDirection(), player)) {
					BlockSwapper.add(sl, c, level.getBlockState(c), pb, 0, sp, slot);
				}
			} else {
				BlockSwapper.add(sl, pos, bs, pb, 3 + WandItem.getFocusEnlarge(wand), sp, slot);
			}
		}
		return true;
	}

	/** ItemFocusTrade.onEntitySwing: left click swaps just the block in reach. */
	@Override
	public void onEntitySwing(Player player, ItemStack wand) {
		if (!(player.level() instanceof ServerLevel sl) || !(player instanceof ServerPlayer sp)) {
			return;
		}
		ItemStack pb = getPickedBlock(wand);
		BlockHitResult mop = reachTarget(sl, player);
		if (mop != null && !pb.isEmpty() && swappable(sl, mop.getBlockPos())) {
			BlockSwapper.add(sl, mop.getBlockPos(), sl.getBlockState(mop.getBlockPos()), pb, 0, sp, sp.getInventory().getSelectedSlot());
		}
	}

	@Override
	public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
		return switch (rank) {
			case 1, 2, 4 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge};
			case 3 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge, FocusUpgradeType.treasure, FocusUpgradeType.architect};
			case 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge, FocusUpgradeType.silktouch};
			default -> null;
		};
	}

	@Override
	public int getMaxAreaSize(ItemStack focus) {
		return 3 + this.getUpgradeLevel(focus, FocusUpgradeType.enlarge) * 2;
	}

	@Override
	public List<BlockPos> getArchitectBlocks(ItemStack wand, Level level, BlockPos pos, Direction side, Player player) {
		BlockState bs = level.getBlockState(pos);
		List<BlockPos> out = new ArrayList<>();
		Set<BlockPos> checked = new HashSet<>();
		if (side.getAxis() != Direction.Axis.Z) {
			this.checkNeighbours(level, pos, bs, pos, side, WandFoci.getAreaX(wand), WandFoci.getAreaZ(wand), out, player, checked);
		} else {
			this.checkNeighbours(level, pos, bs, pos, side, WandFoci.getAreaZ(wand), WandFoci.getAreaX(wand), out, player, checked);
		}
		return out;
	}

	private void checkNeighbours(Level level, BlockPos origin, BlockState bs, BlockPos pos, Direction side, int sizeX, int sizeZ, List<BlockPos> list,
		Player player, Set<BlockPos> checked) {
		if (!checked.add(pos)) {
			return;
		}
		int dx = Math.abs(pos.getX() - origin.getX());
		int dy = Math.abs(pos.getY() - origin.getY());
		int dz = Math.abs(pos.getZ() - origin.getZ());
		switch (side.getAxis()) {
			case Y -> {
				if (dx > sizeX || dz > sizeZ) {
					return;
				}
			}
			case Z -> {
				if (dx > sizeX || dy > sizeZ) {
					return;
				}
			}
			case X -> {
				if (dy > sizeX || dz > sizeZ) {
					return;
				}
			}
		}
		BlockState s = level.getBlockState(pos);
		if (s == bs && BlockSwapper.isExposed(level, pos) && !s.isAir() && s.getDestroySpeed(level, pos) >= 0.0F && level.mayInteract(player, pos)) {
			list.add(pos);
			for (Direction d : Direction.values()) {
				if (d.getAxis() != side.getAxis()) {
					this.checkNeighbours(level, origin, bs, pos.relative(d), side, sizeX, sizeZ, list, player, checked);
				}
			}
		}
	}

	@Override
	public boolean showAxis(ItemStack wand, Level level, Player player, Direction side, Architect.Axis axis) {
		int dim = WandFoci.getAreaDim(wand);
		return switch (side.getAxis()) {
			case Y -> axis == Axis.X && (dim == 0 || dim == 1) || axis == Axis.Z && (dim == 0 || dim == 2);
			case Z -> axis == Axis.Y && (dim == 0 || dim == 1) || axis == Axis.X && (dim == 0 || dim == 2);
			case X -> axis == Axis.Y && (dim == 0 || dim == 1) || axis == Axis.Z && (dim == 0 || dim == 2);
		};
	}
}
