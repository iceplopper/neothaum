package dev.neothaum.items.equipment;

import dev.neothaum.network.TVNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** BlockUtils.harvestBlock / breakFurthestBlock and Utils.isWoodLog, for the elemental tools. */
public final class ToolHelper {
	/** Set while a tool breaks extra blocks, so the extra breaks don't set off the tool again. */
	private static final ThreadLocal<Boolean> HARVESTING = ThreadLocal.withInitial(() -> false);

	public static boolean isHarvesting() {
		return HARVESTING.get();
	}

	public static boolean isWoodLog(Level level, BlockPos pos) {
		return level.getBlockState(pos).is(BlockTags.LOGS);
	}

	/**
	 * Breaks a block as the player would (drops, tool damage, stats). With followItem the drops drift to the player like TC4's
	 * EntityFollowingItem; a colour sends a puff of bubbles there.
	 */
	public static boolean harvestBlock(Level level, Player player, BlockPos pos, boolean followItem, int color) {
		if (!(player instanceof ServerPlayer sp) || !(level instanceof ServerLevel sl)) {
			return false;
		}
		if (level.getBlockState(pos).getDestroySpeed(level, pos) < 0.0F || !level.mayInteract(player, pos)) {
			return false;
		}
		HARVESTING.set(true);
		boolean worked;
		try {
			worked = sp.gameMode.destroyBlock(pos);
		} finally {
			HARVESTING.set(false);
		}
		if (worked) {
			if (followItem) {
				for (ItemEntity e : sl.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(0.5))) {
					var d = player.position().add(0, player.getBbHeight() / 2.0, 0).subtract(e.position());
					e.setDeltaMovement(d.normalize().scale(Math.min(0.6, d.length() / 6.0)));
					e.setPickUpDelay(0);
				}
			}
			if (color != 0) {
				TVNetwork.sendBubbles(level, pos, color);
			}
		}
		return worked;
	}

	/** BlockUtils.findBlocks: walk along same-kind logs, always outward, up to 24 blocks across and 48 up. */
	private static BlockPos findFurthest(Level level, BlockPos origin, Block block) {
		BlockPos last = origin;
		double lastDistance = 0.0;
		boolean moved = true;
		while (moved) {
			moved = false;
			search:
			for (int xx = -2; xx <= 2; xx++) {
				for (int yy = 2; yy >= -2; yy--) {
					for (int zz = -2; zz <= 2; zz++) {
						BlockPos p = last.offset(xx, yy, zz);
						if (Math.abs(p.getX() - origin.getX()) > 24 || Math.abs(p.getY() - origin.getY()) > 48 || Math.abs(p.getZ() - origin.getZ()) > 24) {
							return last;
						}
						BlockState s = level.getBlockState(p);
						if (s.is(block) && s.is(BlockTags.LOGS) && s.getDestroySpeed(level, p) >= 0.0F) {
							double d = p.distSqr(origin);
							if (d > lastDistance) {
								lastDistance = d;
								last = p;
								moved = true;
								break search;
							}
						}
					}
				}
			}
		}
		return last;
	}

	/** breakFurthestBlock: fells a tree one log at a time, from the far end inwards. */
	public static boolean breakFurthestBlock(Level level, BlockPos pos, Block block, Player player, boolean followItem, int color) {
		BlockPos target = findFurthest(level, pos, block);
		boolean worked = harvestBlock(level, player, target, followItem, color);
		if (worked && level instanceof ServerLevel sl) {
			// let the leaves around the felled log notice it has gone
			for (BlockPos p : BlockPos.betweenClosed(target.offset(-3, -3, -3), target.offset(3, 3, 3))) {
				BlockState s = sl.getBlockState(p);
				if (s.is(BlockTags.LEAVES)) {
					sl.scheduleTick(p.immutable(), s.getBlock(), 150 + sl.getRandom().nextInt(150));
				}
			}
		}
		return worked;
	}

	private ToolHelper() {
	}
}
