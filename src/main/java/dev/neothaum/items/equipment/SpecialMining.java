package dev.neothaum.items.equipment;

import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVItems;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Utils.addSpecialMiningResult / findSpecialMiningResult: the native clusters that dowsing (and the pickaxe of the core) can turn ore into.
 * TC4 keyed the ore block, which 1.7 ores dropped; modern ores drop their raw metal, so both are keyed.
 */
public final class SpecialMining {
	private record Result(Item item, float chance) {
	}

	private static Map<Item, Result> table;

	private static Map<Item, Result> table() {
		if (table == null) {
			table = new HashMap<>();
			for (Item i : new Item[]{Items.IRON_ORE, Items.DEEPSLATE_IRON_ORE, Items.RAW_IRON}) {
				table.put(i, new Result(TVItems.NATIVE_IRON_CLUSTER, 1.0F));
			}
			for (Item i : new Item[]{Items.GOLD_ORE, Items.DEEPSLATE_GOLD_ORE, Items.RAW_GOLD}) {
				table.put(i, new Result(TVItems.NATIVE_GOLD_CLUSTER, 0.9F));
			}
			for (Item i : new Item[]{Items.COPPER_ORE, Items.DEEPSLATE_COPPER_ORE, Items.RAW_COPPER}) {
				table.put(i, new Result(TVItems.NATIVE_COPPER_CLUSTER, 1.0F));
			}
			for (Item i : new Item[]{TVBlocks.CINNABAR_ORE.asItem(), TVBlocks.DEEPSLATE_CINNABAR_ORE.asItem(), TVItems.RAW_CINNABAR}) {
				table.put(i, new Result(TVItems.NATIVE_CINNABAR_CLUSTER, 0.9F));
			}
		}
		return table;
	}

	/** Config.postInit: anything in c:ores/tin, silver or lead (or their raw materials) gives that metal's cluster. */
	private static Result crossModOre(ItemStack is) {
		for (String metal : new String[]{"tin", "silver", "lead"}) {
			if (is.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
				net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "ores/" + metal)))
				|| is.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
					net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "raw_materials/" + metal)))) {
				Item cluster = metal.equals("tin") ? TVItems.NATIVE_TIN_CLUSTER : metal.equals("silver") ? TVItems.NATIVE_SILVER_CLUSTER
					: TVItems.NATIVE_LEAD_CLUSTER;
				return new Result(cluster, 1.0F);
			}
		}
		return null;
	}

	/** The cluster this drop becomes, with probability chance x the ore's own chance; otherwise the drop unchanged. */
	public static ItemStack find(ItemStack is, float chance, RandomSource rand) {
		Result r = table().get(is.getItem());
		if (r == null) {
			r = crossModOre(is);
		}
		if (r != null && rand.nextFloat() <= chance * r.chance()) {
			return new ItemStack(r.item(), is.getCount());
		}
		return is.copy();
	}

	private SpecialMining() {
	}
}
