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
		// --- Node Generation & Variants ---
		/** 1 in N chunks generates a wild aura node (lower = more frequent, 0 = disabled). Default: 36. */
		public int nodeRarity = 36;
		/** 1 in N wild nodes will be a special variant (Dark, Unstable, Pure, Hungry, Tainted). Default: 18. */
		public int nodeVariantRarity = 18;
		public int darkNodeWeight = 3;
		public int unstableNodeWeight = 3;
		public int pureNodeWeight = 3;
		public int hungryNodeWeight = 1;
		public int taintedNodeWeight = 1;
		public int brightNodeWeight = 1;
		public int paleNodeWeight = 1;
		public int fadingNodeWeight = 1;

		// --- Biomes (Size & Generation) ---
		/** Whether Magical Forest generates in the overworld. Default: true. */
		public boolean magicalForestGenerate = true;
		/** Percentage size of Magical Forest within forest regions (1-100). Default: 30. */
		public int magicalForestSize = 30;
		/** Whether Tainted Land generates naturally at worldgen. Default: true. */
		public boolean taintedLandGenerate = true;
		/** Percentage size of Tainted Land inland pockets (1-100). Default: 5. */
		public int taintedLandSize = 5;

		// --- Structures (Generation Frequency per Type) ---
		/** 1 in N chunks generates an Eldritch Ring (lower = more frequent, 0 = disabled). Default: 66. */
		public int eldritchRingRarity = 66;
		/** 1 in N chunks generates a Barrow Mound (lower = more frequent, 0 = disabled). Default: 150. */
		public int barrowMoundRarity = 150;
		/** 1 in N chunks generates Hilltop Stones above y=85 (lower = more frequent, 0 = disabled). Default: 40. */
		public int hilltopStonesRarity = 40;
		/** 1 in N chunks generates a Wizard Tower (lower = more frequent, 0 = disabled). Default: 120. */
		public int wizardTowerRarity = 120;
		/** 1 in N chunks generates a Banker/Thaumaturge House (lower = more frequent, 0 = disabled). Default: 140. */
		public int bankerHomeRarity = 140;

		// --- Taint Mechanics ---
		/** Whether fibrous taint and taint biomes can spread. Default: true. */
		public boolean taintSpread = true;
		/** Rate of taint biome spread (higher = slower, default: 200). */
		public int taintSpreadRate = 200;
		/** Whether taint corrupts dying regular mobs into tainted variants, or only naturally spawned variants exist. Default: true. */
		public boolean corruptExistingMobs = true;

		// --- Alchemy & Magic Multipliers ---
		/** Multiplier for flux generated / spilled from crucibles, furnaces, and infusion instability. Default: 1.0. */
		public float fluxMultiplier = 1.0f;
		/** Smelting speed multiplier for the Infernal Furnace (higher = faster). Default: 1.0. */
		public float infernalFurnaceSpeedMultiplier = 1.0f;
		/** Bonus nugget/item yield multiplier for the Infernal Furnace. Default: 1.0. */
		public float infernalFurnaceBonusMultiplier = 1.0f;

		// --- Creative & Debug ---
		/** Toggle to show or hide the Creative Mode "TC: Dev Tools" tab. Default: true. */
		public boolean showCreativeDebugTab = true;

		// --- Other TC4 Options ---
		public boolean hardNodes = true;
		public float aspectOrbDropChance = 0.25f;
		public boolean allowWarp = true;
		public float wispSpawnRate = 1.0f;
		public boolean championMobs = true;
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
