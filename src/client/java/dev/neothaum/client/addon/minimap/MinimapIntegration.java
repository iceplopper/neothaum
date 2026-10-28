package dev.neothaum.client.addon.minimap;

import dev.neothaum.NeoThaum;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Map integrations. Aura nodes go on Xaero's fullscreen World Map only, never on the minimap or as in-world waypoints, and only once seen
 * through Goggles of Revealing.
 */
public final class MinimapIntegration {
	public static void init() {
		if (FabricLoader.getInstance().isModLoaded("xaeroworldmap")) {
			NeoThaum.LOGGER.info("Xaero's World Map detected - aura nodes you have seen will be shown on it.");
			ClientTickEvents.END_CLIENT_TICK.register(WorldMapNodes::tick);
		}
	}

	private MinimapIntegration() {
	}
}
