package dev.neothaum.warp;

import dev.neothaum.config.NeoThaumConfig;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVItems;
import java.util.Map;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * EventHandlerWorld.onCrafting with ThaumcraftApi.addWarpToItem (ConfigResearch): taking one of these from a crafting grid (vanilla's or the
 * arcane worktable's) gives sticky warp, unless warp is off (TC4's wuss mode). Like TC4, items made by infusion or in the crucible give none.
 */
public final class CraftingWarp {
	private static Map<Item, Integer> warp;

	private static Map<Item, Integer> warp() {
		if (warp == null) {
			warp = Map.of(TVItems.FOCUS_HELLBAT, 1, TVItems.SINISTER_LODESTONE, 1, TVBlocks.VOID_JAR.asItem(), 1, TVItems.BUCKET_OF_LIQUID_DEATH, 1,
				TVItems.BOTTLED_TAINT, 1, TVItems.GOLEM_FLESH, 1, TVItems.FOCUS_PRIMAL, 1, TVItems.PRIMAL_STAFF_CORE, 1);
		}
		return warp;
	}

	public static void onCrafted(ItemStack stack, Player player) {
		Integer amount = warp().get(stack.getItem());
		if (amount != null && !player.level().isClientSide() && NeoThaumConfig.current.allowWarp) {
			Warp.addStickyWarpToPlayer(player, amount);
		}
	}

	private CraftingWarp() {
	}
}
