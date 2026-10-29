package dev.neothaum.addon.trinkets;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Whether Trinkets Updated is installed. Kept apart from {@link TrinketsIntegration}, which uses the Trinkets API, so that checking this never
 * loads a class Trinkets would have to supply.
 */
public final class TrinketsCompat {
	public static final boolean LOADED = FabricLoader.getInstance().isModLoaded("trinkets_updated");

	public static void init() {
		TrinketsIntegration.init();
	}

	/** Moves whatever is in NeoThaum's own bauble slots into Trinkets slots (done for every player on joining). Only call when {@link #LOADED}. */
	public static void moveBaublesIntoTrinkets(net.minecraft.server.level.ServerPlayer player) {
		TrinketsIntegration.moveBaublesIntoTrinkets(player);
	}

	private TrinketsCompat() {
	}
}
