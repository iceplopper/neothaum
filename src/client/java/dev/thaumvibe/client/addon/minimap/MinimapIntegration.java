package dev.thaumvibe.client.addon.minimap;

import dev.thaumvibe.Thaumvibe;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

/**
 * Minimap integrations for Thaumvibe 4.
 * Node radar and map blips are strictly locked behind wearing Goggles of Revealing (NO Thaumometer).
 */
public final class MinimapIntegration {
	private static boolean xaeroLoaded;
	private static int tickCounter;

	public static void init() {
		xaeroLoaded = FabricLoader.getInstance().isModLoaded("xaerominimap");
		if (xaeroLoaded) {
			Thaumvibe.LOGGER.info("Xaero's Minimap detected - initializing Goggles Aura Node radar integration.");
		}

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			if (mc.player == null || mc.level == null) {
				return;
			}
			tickCounter++;
			if (tickCounter % 20 == 0) { // update once every second
				if (xaeroLoaded) {
					try {
						XaeroMinimapHandler.update(mc.player, mc.level);
					} catch (Throwable t) {
						Thaumvibe.LOGGER.error("Failed to update Xaero minimap node waypoints", t);
					}
				}
			}
		});
	}

	private MinimapIntegration() {
	}
}
