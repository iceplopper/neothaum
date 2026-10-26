package dev.thaumvibe.wands;

import dev.thaumvibe.api.wands.FocusItem;
import dev.thaumvibe.api.wands.FocusUpgradeType;
import dev.thaumvibe.items.WandItem;
import dev.thaumvibe.network.TVNetwork;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * ServerTickEventsFML's block swapper: the equal trade focus queues swaps here, and each tick one swap goes through (paying vis and the
 * block from the inventory, handing back the old block's drops) and queues its matching exposed neighbours while it has life left.
 */
public final class BlockSwapper {
	record Swap(BlockPos pos, BlockState source, ItemStack target, int lifespan, ServerPlayer player, int wandSlot) {
	}

	private static final Map<ResourceKey<Level>, LinkedBlockingQueue<Swap>> QUEUES = new ConcurrentHashMap<>();

	public static void init() {
		ServerTickEvents.END_LEVEL_TICK.register(BlockSwapper::tick);
	}

	public static void add(ServerLevel level, BlockPos pos, BlockState source, ItemStack target, int life, ServerPlayer player, int wandSlot) {
		QUEUES.computeIfAbsent(level.dimension(), k -> new LinkedBlockingQueue<>()).offer(new Swap(pos, source, target, life, player, wandSlot));
	}

	/** BlockUtils.isBlockExposed: some face touches a block that is not a full opaque cube. */
	public static boolean isExposed(Level level, BlockPos pos) {
		for (Direction d : Direction.values()) {
			if (!level.getBlockState(pos.relative(d)).isSolidRender()) {
				return true;
			}
		}
		return false;
	}

	private static void tick(ServerLevel world) {
		LinkedBlockingQueue<Swap> queue = QUEUES.get(world.dimension());
		if (queue == null) {
			return;
		}
		boolean didSomething = false;
		while (!didSomething) {
			Swap vs = queue.poll();
			if (vs == null) {
				return;
			}
			BlockState bs = world.getBlockState(vs.pos());
			ItemStack wand = vs.player().getInventory().getItem(vs.wandSlot());
			FocusItem focus = wand.getItem() instanceof WandItem ? WandItem.getFocus(wand) : null;
			if (focus == null || vs.player().isRemoved() || !world.mayInteract(vs.player(), vs.pos())
				|| bs.getBlock().asItem() == vs.target().getItem()
				|| !WandItem.consumeAllVis(wand, vs.player(), focus.getVisCost(WandItem.getFocusStack(wand)), false, false)) {
				continue;
			}
			int slot = vs.player().isCreative() ? 1 : vs.player().getInventory().findSlotMatchingItem(vs.target());
			if (bs != vs.source() || slot < 0 || !(vs.target().getItem() instanceof BlockItem bi)) {
				continue;
			}
			didSomething = true;
			if (!vs.player().isCreative()) {
				int fortune = WandItem.getFocusTreasure(wand);
				boolean silk = focus.isUpgradedWith(WandItem.getFocusStack(wand), FocusUpgradeType.silktouch);
				vs.player().getInventory().removeItem(slot, 1);
				ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
				HolderLookup.RegistryLookup<net.minecraft.world.item.enchantment.Enchantment> ench = world.registryAccess()
					.lookupOrThrow(Registries.ENCHANTMENT);
				if (silk) {
					tool.enchant(ench.getOrThrow(Enchantments.SILK_TOUCH), 1);
				} else if (fortune > 0) {
					tool.enchant(ench.getOrThrow(Enchantments.FORTUNE), fortune);
				}
				for (ItemStack drop : Block.getDrops(bs, world, vs.pos(), world.getBlockEntity(vs.pos()), vs.player(), tool)) {
					if (!vs.player().getInventory().add(drop)) {
						vs.player().drop(drop, false, net.minecraft.util.Prediction.SERVER_ONLY);
					}
				}
				WandItem.consumeAllVis(wand, vs.player(), focus.getVisCost(WandItem.getFocusStack(wand)), true, false);
			}
			BlockState placed = bi.getBlock().defaultBlockState();
			world.setBlock(vs.pos(), placed, 3);
			placed.getBlock().setPlacedBy(world, vs.pos(), placed, vs.player(), vs.target());
			TVNetwork.sendBlockSparkle(world, vs.pos(), 12632319);
			world.levelEvent(2001, vs.pos(), Block.getId(vs.source()));
			if (vs.lifespan() > 0) {
				for (int xx = -1; xx <= 1; xx++) {
					for (int yy = -1; yy <= 1; yy++) {
						for (int zz = -1; zz <= 1; zz++) {
							BlockPos n = vs.pos().offset(xx, yy, zz);
							if ((xx != 0 || yy != 0 || zz != 0) && world.getBlockState(n) == vs.source() && isExposed(world, n)) {
								queue.offer(new Swap(n, vs.source(), vs.target(), vs.lifespan() - 1, vs.player(), vs.wandSlot()));
							}
						}
					}
				}
			}
		}
	}

	private BlockSwapper() {
	}
}
