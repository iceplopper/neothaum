package dev.neothaum.addon;

import dev.neothaum.config.NeoThaumConfig;

/**
 * NeoThaum Additions: features that are not in TC4, each behind its own config switch (all off by default). Everything for them lives in
 * dev.neothaum.addon and dev.neothaum.client.addon, so it can be split into its own mod later.
 */
public final class Additions {
	/** Whether the named addition is switched on; data (such as a research entry's "addition") names them this way. */
	public static boolean enabled(String name) {
		var cfg = NeoThaumConfig.current;
		return switch (name) {
			case "research_sharing" -> cfg.additionResearchSharing;
			case "essentia_overlay" -> cfg.additionEssentiaOverlay;
			case "warp_hud" -> cfg.additionWarpHud;
			default -> false;
		};
	}

	public static void init() {
		ResearchSharing.init();
	}

	private Additions() {
	}
}
