package dev.neothaum.world;

import dev.neothaum.registry.TVItems;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

/**
 * Config.initLoot: what TC4 slipped into the world's loot chests through ChestGenHooks. Common things (a loot bag, thaumium, amber), uncommon
 * ones (a better bag, mundane baubles, knowledge fragments) and rare ones (the best bag, the Thaumonomicon, thaumium tools, a runic ring, a vis
 * amulet, apprentice's rings) go into dungeons, temples, mineshafts and strongholds at TC4's weights; the stronghold library also holds knowledge fragments and a
 * village smith's chest thaumium. 1.7 chests drew these from the same list as the vanilla loot; here they are a pool of their own, empty half
 * the time, of one to three draws.
 */
public final class TCChestLoot {
	private record Entry(Item item, int min, int max, int weight) {
	}

	private static final Map<ResourceKey<LootTable>, Integer> TABLES = Map.of(BuiltInLootTables.SIMPLE_DUNGEON, 0, BuiltInLootTables.JUNGLE_TEMPLE, 0,
		BuiltInLootTables.DESERT_PYRAMID, 0, BuiltInLootTables.ABANDONED_MINESHAFT, 1, BuiltInLootTables.STRONGHOLD_CORRIDOR, 1,
		BuiltInLootTables.STRONGHOLD_CROSSING, 1, BuiltInLootTables.STRONGHOLD_LIBRARY, 1);

	private TCChestLoot() {
	}

	/** kind 0: dungeons and temples (weights 5, 4, 1); kind 1: mineshafts and strongholds (4, 3, 1). */
	private static List<Entry> entries(int kind) {
		int c = kind == 0 ? 5 : 4, u = kind == 0 ? 4 : 3;
		int cmax = 3, umax = 2;
		return List.of(new Entry(TVItems.LOOT_BAG_COMMON, 1, cmax, c), new Entry(TVItems.THAUMIUM_INGOT, 1, cmax, c), new Entry(TVItems.AMBER, 1, cmax, c),
			new Entry(TVItems.LOOT_BAG_UNCOMMON, 1, umax, u), new Entry(TVItems.MUNDANE_AMULET, 1, umax, u), new Entry(TVItems.MUNDANE_RING, 1, umax, u),
			new Entry(TVItems.MUNDANE_BELT, 1, umax, u), new Entry(TVItems.KNOWLEDGE_FRAGMENT, 1, umax, u), new Entry(TVItems.LOOT_BAG_RARE, 1, 1, 1),
			new Entry(TVItems.THAUMONOMICON, 1, 1, 1), new Entry(TVItems.THAUMIUM_SWORD, 1, 1, 1), new Entry(TVItems.THAUMIUM_PICKAXE, 1, 1, 1),
			new Entry(TVItems.THAUMIUM_AXE, 1, 1, 1), new Entry(TVItems.THAUMIUM_HOE, 1, 1, 1), new Entry(TVItems.RING_OF_PROTECTION, 1, 1, 1),
			new Entry(TVItems.AMULET_OF_VIS_STORAGE, 1, 1, 1), new Entry(TVItems.APPRENTICE_RING_AER, 1, 1, 1),
			new Entry(TVItems.APPRENTICE_RING_TERRA, 1, 1, 1), new Entry(TVItems.APPRENTICE_RING_IGNIS, 1, 1, 1), new Entry(TVItems.APPRENTICE_RING_AQUA, 1, 1, 1),
			new Entry(TVItems.APPRENTICE_RING_ORDO, 1, 1, 1), new Entry(TVItems.APPRENTICE_RING_PERDITIO, 1, 1, 1));
	}

	private static LootPool.Builder pool(List<Entry> entries) {
		LootPool.Builder pool = LootPool.lootPool().setRolls(ContextIntProviders.between(1, 3));
		int total = 0;
		for (Entry e : entries) {
			total += e.weight;
			pool.add(LootItem.lootTableItem(e.item).setWeight(e.weight)
				.apply(SetItemCountFunction.setCount(ContextIntProviders.between(e.min, e.max))));
		}
		return pool.add(EmptyLootItem.emptyItem().setWeight(total));
	}

	public static void init() {
		LootTableEvents.MODIFY.register((key, table, source, registries) -> {
			if (!source.isBuiltin()) {
				return;
			}
			Integer kind = TABLES.get(key);
			if (kind != null) {
				table.withPool(pool(entries(kind)));
			}
			if (key.equals(BuiltInLootTables.STRONGHOLD_LIBRARY)) {
				table.withPool(pool(List.of(new Entry(TVItems.KNOWLEDGE_FRAGMENT, 3, 6, 20))));
			}
			if (key.equals(BuiltInLootTables.VILLAGE_WEAPONSMITH) || key.equals(BuiltInLootTables.VILLAGE_TOOLSMITH)) {
				table.withPool(pool(List.of(new Entry(TVItems.THAUMIUM_INGOT, 1, 3, 10))));
			}
		});
	}
}
