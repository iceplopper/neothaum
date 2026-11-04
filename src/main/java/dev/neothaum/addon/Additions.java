package dev.neothaum.addon;

import dev.neothaum.config.NeoThaumConfig;

/**
 * NeoThaum Additions: features that are not in TC4, each behind its own config switch (all off by default). Everything for them lives in
 * dev.neothaum.addon and dev.neothaum.client.addon, so it can be split into its own mod later.
 */
public final class Additions {
	/** Whether the named addition is switched on, with the master switch; data (such as a research entry's "addition") names them this way. */
	public static boolean enabled(String name) {
		var cfg = NeoThaumConfig.current;
		return cfg.additionsEnabled && switch (name) {
			case "research_sharing" -> cfg.additionResearchSharing;
			case "essentia_overlay" -> cfg.additionEssentiaOverlay;
			case "warp_hud" -> cfg.additionWarpHud;
			case "marker_copy" -> cfg.additionMarkerCopy;
			case "modded_aspects" -> cfg.additionModdedAspects;
			case "wood_sets" -> cfg.additionWoodSets;
			case "hint_research" -> cfg.additionHintResearch;
			case "experience_siphon" -> cfg.additionExperienceSiphon;
			case "travellers_totems" -> cfg.additionTravellersTotems;
			default -> false;
		};
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
