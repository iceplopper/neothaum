package dev.neothaum.client.addon.minimap;

import dev.neothaum.NeoThaum;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Map integrations. Aura nodes go on Xaero's World Map and JourneyMap's fullscreen map only, never on a minimap or as in-world waypoints,
 * and only once scanned and seen through Goggles of Revealing.
 */
public final class MinimapIntegration {
	public static void init() {
		boolean xaero = FabricLoader.getInstance().isModLoaded("xaeroworldmap");
		if (xaero) {
			NeoThaum.LOGGER.info("Xaero's World Map detected - aura nodes you have seen will be shown on it.");
		}
		// JourneyMap's markers (JourneyMapNodes, loaded by JourneyMap itself) show what this notes
		if (xaero || FabricLoader.getInstance().isModLoaded("journeymap")) {
			ClientTickEvents.END_CLIENT_TICK.register(WorldMapNodes::tick);
		}
	}

	private MinimapIntegration() {
	}
}
