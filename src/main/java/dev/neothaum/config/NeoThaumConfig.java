package dev.neothaum.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.neothaum.NeoThaum;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public class NeoThaumConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("neothaum.json");

	public static ConfigData current = new ConfigData();

	public static class ConfigData {
		// --- Addons ---
		/** Whether the aura nodes you have seen are drawn on Xaero's World Map (toggled by the button on the map). Default: true. */
		public boolean worldMapNodes = true;

		// Every number below is a multiplier on TC4's own setting, from 0 to 10 in steps of 0.1: 1 is TC4, 3 is three times as much (as many
		// nodes, as large a forest, taint spreading three times as fast), 0 turns it off. The TC4 values are the constants in NeoThaumConfig.

		// --- Node Generation & Variants ---
		/** How often wild aura nodes generate (TC4: 1 chunk in 36). */
		public float nodeMultiplier = 1.0f;
		/** How often a wild node is one of the special types (TC4: 1 in 18; modifiers half as rare). */
		public float nodeVariantMultiplier = 1.0f;
		/** The odds of each special type and modifier relative to the others (TC4 weights: dark, unstable, pure 3; the rest 1). */
		public float darkNodeMultiplier = 1.0f;
		public float unstableNodeMultiplier = 1.0f;
		public float pureNodeMultiplier = 1.0f;
		public float hungryNodeMultiplier = 1.0f;
		public float taintedNodeMultiplier = 1.0f;
		public float brightNodeMultiplier = 1.0f;
		public float paleNodeMultiplier = 1.0f;
		public float fadingNodeMultiplier = 1.0f;

		// --- Biomes (Size & Generation) ---
		/** Whether Magical Forest generates in the overworld. Default: true. */
		public boolean magicalForestGenerate = true;
		/** How much of forest country is Magical Forest (TC4-like: 30%). */
		public float magicalForestSizeMultiplier = 1.0f;
		/** Whether Tainted Land generates naturally at worldgen. Default: true. */
		public boolean taintedLandGenerate = true;
		/** How large the inland pockets of Tainted Land are (5% of their region). */
		public float taintedLandSizeMultiplier = 1.0f;

		// --- Structures ---
		/** How often each structure generates (TC4: eldritch ring 1 chunk in 66, barrow mound 1 in 150, hilltop stones 1 in 40). */
		public float eldritchRingMultiplier = 1.0f;
		public float barrowMoundMultiplier = 1.0f;
		public float hilltopStonesMultiplier = 1.0f;
		/** Whether wizard towers and bankers' houses are among the buildings villages can have (TC4 built them nowhere else). Default: true. */
		public boolean villageBuildings = true;

		// --- Taint Mechanics ---
		/** Whether fibrous taint and taint biomes can spread. Default: true. */
		public boolean taintSpread = true;
		/** How fast tainted land creeps outward (TC4: 1 in 200x5 tries). */
		public float taintSpreadMultiplier = 1.0f;
		/** Whether taint corrupts dying regular mobs into tainted variants, or only naturally spawned variants exist. Default: true. */
		public boolean corruptExistingMobs = true;

		// --- Alchemy & Magic ---
		/** Flux spilled by crucibles. */
		public float fluxMultiplier = 1.0f;
		/** Infernal Furnace smelting speed. */
		public float infernalFurnaceSpeedMultiplier = 1.0f;
		/** Infernal Furnace bonus nuggets. */
		public float infernalFurnaceBonusMultiplier = 1.0f;
		/** Chance of an aspect orb from a mob you kill (25%). */
		public float aspectOrbMultiplier = 1.0f;
		/** How often wisps spawn in the nether (weight 5; takes effect on restart). */
		public float wispSpawnMultiplier = 1.0f;

		// --- Creative & Debug ---
		/** Toggle to show or hide the Creative Mode "TC: Dev Tools" tab. Default: true. */
		public boolean showCreativeDebugTab = true;

		// --- Other TC4 Options ---
		public boolean hardNodes = true;
		public boolean allowWarp = true;
		public boolean championMobs = true;
	}

	// TC4's values, which the multipliers above scale
	public static final int TC4_NODE_RARITY = 36;
	public static final int TC4_NODE_VARIANT_RARITY = 18;
	public static final int TC4_ELDRITCH_RING_RARITY = 66;
	public static final int TC4_BARROW_MOUND_RARITY = 150;
	public static final int TC4_HILLTOP_STONES_RARITY = 40;
	public static final int TC4_TAINT_SPREAD_RATE = 200;
	public static final int MAGICAL_FOREST_PERCENT = 30;
	public static final int TAINTED_LAND_PERCENT = 5;
	public static final float ASPECT_ORB_CHANCE = 0.5f;
	public static final int WISP_SPAWN_WEIGHT = 5;

	/** A "1 in {@code oneIn}" roll made {@code multiplier} times as likely (never, at 0). */
	public static boolean roll(net.minecraft.util.RandomSource random, int oneIn, float multiplier) {
		return multiplier > 0.0f && random.nextFloat() * oneIn < multiplier;
	}

	/** A weight or amount scaled by its multiplier, kept to whole numbers. */
	public static int scale(int base, float multiplier) {
		return Math.max(0, Math.round(base * multiplier));
	}

	public static void load() {
		try {
			if (Files.exists(CONFIG_PATH)) {
				try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
					ConfigData loaded = GSON.fromJson(reader, ConfigData.class);
					if (loaded != null) {
						current = loaded;
					}
				}
			} else {
				save();
			}
		} catch (Exception e) {
			NeoThaum.LOGGER.error("Failed to load neothaum config, using defaults", e);
		}
	}

	public static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(current, writer);
			}
		} catch (Exception e) {
			NeoThaum.LOGGER.error("Failed to save neothaum config", e);
		}
	}
}
