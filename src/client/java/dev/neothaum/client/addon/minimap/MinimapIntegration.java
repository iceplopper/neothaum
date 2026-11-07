package dev.neothaum.client.addon.minimap;

import dev.neothaum.NeoThaum;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Map integrations. Aura nodes go on Xaero's World Map and JourneyMap's fullscreen map only, never on a minimap or as in-world waypoints,
 * and only once scanned and seen through Goggles of Revealing. The totems you have attuned to go on map items, Xaero's World Map and
 * Xaero's Minimap (TotemMapMarks).
 */
public final class MinimapIntegration {
	public static void init() {
		TotemMapMarks.init();
		boolean xaero = FabricLoader.getInstance().isModLoaded("xaeroworldmap");
		if (xaero) {
			NeoThaum.LOGGER.info("Xaero's World Map detected - aura nodes you have seen will be shown on it.");
		}
		// JourneyMap's markers (JourneyMapNodes, loaded by JourneyMap itself) show what this notes
		if (xaero || FabricLoader.getInstance().isModLoaded("journeymap")) {
			ClientTickEvents.END_CLIENT_TICK.register(WorldMapNodes::tick);
		}
		// Xaero's Minimap is ready some time after start-up; TotemMinimapElements is only loaded when it is installed
		if (FabricLoader.getInstance().isModLoaded("xaerominimap")) {
			ClientTickEvents.END_CLIENT_TICK.register(mc -> TotemMinimapElements.tick());
		}
	}

	private MinimapIntegration() {
	}
}
