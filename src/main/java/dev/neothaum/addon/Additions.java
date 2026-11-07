package dev.neothaum.addon;

import dev.neothaum.config.NeoThaumConfig;

/**
 * NeoThaum Additions: features that are not in TC4, each behind its own config switch (all off by default). Everything for them lives in
 * dev.neothaum.addon and dev.neothaum.client.addon, so it can be split into its own mod later.
 */
public final class Additions {
	/** Whether the named addition is switched on, with the master switch; data (such as a research entry's "addition") names them this way. */
	public static boolean enabled(String name) {
		return dev.neothaum.config.NeoThaumConfig.additionsEnabled() && own(name);
	}

	/** Every addition, by the name /neothaum additions and data files use, in the order the settings screen shows them. */
	public static final java.util.List<String> ALL = java.util.List.of("travellers_totems", "experience_siphon", "hint_research", "wood_sets",
		"research_sharing", "essentia_overlay", "warp_hud", "marker_copy", "modded_aspects", "vanilla_candles", "bright_nodes",
		"keepinventory_cost", "node_embedding", "arcane_campfire", "storage_blocks");

	/** An addition's own switch (whatever the master switch says). */
	public static boolean own(String name) {
		// on another's server, its switches
		if (NeoThaumConfig.synced != null) {
			return ALL.contains(name) && NeoThaumConfig.synced.addition(name);
		}
		var cfg = NeoThaumConfig.current;
		return switch (name) {
			case "research_sharing" -> cfg.additionResearchSharing;
			case "essentia_overlay" -> cfg.additionEssentiaOverlay;
			case "warp_hud" -> cfg.additionWarpHud;
			case "marker_copy" -> cfg.additionMarkerCopy;
			case "modded_aspects" -> cfg.additionModdedAspects;
			case "wood_sets" -> cfg.additionWoodSets;
			case "hint_research" -> cfg.additionHintResearch;
			case "experience_siphon" -> cfg.additionExperienceSiphon;
			case "travellers_totems" -> cfg.additionTravellersTotems;
			case "vanilla_candles" -> cfg.additionVanillaCandles;
			case "bright_nodes" -> cfg.additionBrightNodes;
			case "keepinventory_cost" -> cfg.additionKeepInventoryCost;
			case "node_embedding" -> cfg.additionNodeEmbedding;
			case "arcane_campfire" -> cfg.additionArcaneCampfire;
			case "storage_blocks" -> cfg.additionStorageBlocks;
			default -> false;
		};
	}

	/** The additions switched off by their own switches, comma separated (as the server tells its players). */
	public static String switchedOff() {
		return String.join(",", ALL.stream().filter(a -> !own(a)).toList());
	}

	/** Which additions are on just now, for noticing a change. */
	public static String state() {
		return String.join(",", ALL.stream().filter(Additions::enabled).toList());
	}

	/** Sets an addition's own switch; false for a name that isn't one. */
	public static boolean set(String name, boolean on) {
		var cfg = NeoThaumConfig.current;
		switch (name) {
			case "research_sharing" -> cfg.additionResearchSharing = on;
			case "essentia_overlay" -> cfg.additionEssentiaOverlay = on;
			case "warp_hud" -> cfg.additionWarpHud = on;
			case "marker_copy" -> cfg.additionMarkerCopy = on;
			case "modded_aspects" -> cfg.additionModdedAspects = on;
			case "wood_sets" -> cfg.additionWoodSets = on;
			case "hint_research" -> cfg.additionHintResearch = on;
			case "experience_siphon" -> cfg.additionExperienceSiphon = on;
			case "travellers_totems" -> cfg.additionTravellersTotems = on;
			case "vanilla_candles" -> cfg.additionVanillaCandles = on;
			case "bright_nodes" -> cfg.additionBrightNodes = on;
			case "keepinventory_cost" -> cfg.additionKeepInventoryCost = on;
			case "node_embedding" -> cfg.additionNodeEmbedding = on;
			case "arcane_campfire" -> cfg.additionArcaneCampfire = on;
			case "storage_blocks" -> cfg.additionStorageBlocks = on;
			default -> {
				return false;
			}
		}
		return true;
	}

	private static final java.util.Map<net.minecraft.world.item.Item, String> HIDDEN = new java.util.HashMap<>();

	/** Hides the item from the creative tab and recipe viewers while the named addition is off. */
	public static void hideWith(net.minecraft.world.item.Item item, String addition) {
		HIDDEN.put(item, addition);
	}

	/** Whether the creative tab and recipe viewers show the item: an addition's items only while it is on. */
	public static boolean shown(net.minecraft.world.item.Item item) {
		String addition = HIDDEN.get(item);
		return addition == null || enabled(addition);
	}

	/** The items an addition brings, for the creative tab's grouping. */
	public static boolean isAddition(net.minecraft.world.item.Item item) {
		return HIDDEN.containsKey(item);
	}

	public static void init() {
		AdditionCondition.init();
		HintResearch.init();
		ResearchSharing.init();
		MarkerCopy.init();
	}

	private Additions() {
	}
}
