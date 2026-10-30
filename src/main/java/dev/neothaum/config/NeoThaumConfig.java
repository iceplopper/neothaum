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
		/** Whether bright nodes light their surroundings fully (light 15; TC4 gave every node 8). Default: true. */
		public boolean brightNodeLight = true;
		/** Whether looking toward a bright node in clear sight shows a lens flare, while an Iris shader pack is in use (client only). Default: true. */
		public boolean brightNodeFlare = true;

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
		/** How often each structure generates (TC4: eldritch ring 1 chunk in 66, barrow mound 1 in 150, hilltop stones 1 in 40, obsidian totem 1 in 360). */
		public float eldritchRingMultiplier = 1.0f;
		public float barrowMoundMultiplier = 1.0f;
		public float hilltopStonesMultiplier = 1.0f;
		public float obsidianTotemMultiplier = 1.0f;
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
		/** TC4's hard_mode_nodes: hungry nodes pull in and eat creatures, tainted nodes grow fibres, sinister nodes call giant zombies. Default: true. */
		public boolean hardNodes = true;
		/** Warp at all; off is TC4's wuss_mode (no warp events, no warp from research or crafting). Default: true. */
		public boolean allowWarp = true;
		/** TC4's champion_mobs: hostile mobs can spawn as champions. Default: true. */
		public boolean championMobs = true;

		// Below: TC4's own settings, under its names, with its defaults and units.

		// --- Research ---
		/** research_difficulty: -1 easy (everything bought with research points), 0 normal (secondary research bought, the rest solved), 1 hard (everything solved at the research table). */
		public int researchDifficulty = 0;
		/** aspect_total_cap: research points of one aspect you can hold before scanning gives less (the square root; just 1 past 1.25 times this). Default: 100. */
		public int aspectTotalCap = 100;

		// --- World_Generation (taintedLandGenerate above is generate_taint) ---
		public boolean generateAuraNodes = true;
		public boolean generateStructures = true;
		public boolean generateCinnabar = true;
		public boolean generateAmber = true;
		public boolean generateInfusedStone = true;
		public boolean generateTrees = true;

		// --- World_Regeneration ---
		/** regen_key: stored in each chunk once it has been generated or regenerated; changing it makes the regeneration below run again. */
		public String regenKey = "DEFAULT";
		public boolean regenAuraNodes = false;
		public boolean regenStructures = false;
		public boolean regenCinnabar = false;
		public boolean regenAmber = false;
		public boolean regenInfusedStone = false;
		public boolean regenTrees = false;
		/** TC4 had this switch but never read it (taint is a biome, made only when the world is first generated); kept for parity, no effect. */
		public boolean regenTaint = false;

		// --- Monster_Spawning (championMobs above) ---
		public boolean spawnAngryZombies = true;
		public boolean spawnFireBats = true;
		public boolean spawnWisps = true;
		public boolean spawnTaintacles = true;
		public boolean spawnTaintSpores = true;
		public boolean spawnPechs = true;
		public boolean spawnElderCreatures = true;

		// --- Golems ---
		/** golem_delay: ticks a golem waits between looking for tasks (minimum 1). Default: 5. */
		public int golemDelay = 5;
		/** golem_ignore_delay: milliseconds a golem ignores an item it found no use for (minimum 1000). Default: 10000. */
		public int golemIgnoreDelay = 10000;
		/** golem_link_quality: detail of the line from a golem to its marked blocks; below 4 turns it off. Default: 16. */
		public int golemLinkQuality = 16;
		/** golem_chest_interact: golems open chests (lid and sound) while they use them. Default: true. */
		public boolean golemChestInteract = true;

		// --- Runic_Shielding ---
		/** runic_recharge_speed: milliseconds between recharge ticks (minimum 500). Default: 2000. */
		public int runicRechargeSpeed = 2000;
		/** runic_recharge_delay: game ticks after a shield is broken before it starts recharging (minimum 0). Default: 80. */
		public int runicRechargeDelay = 80;
		/** runic_cost: centi-vis (0.01 vis) of aer and of terra per point of shielding recharged (minimum 0). Default: 50. */
		public int runicCost = 50;

		// --- Client ---
		/** color_blind: darker, higher contrast research orbs. Default: false. */
		public boolean colorBlind = false;
		/** display_aspects: item aspects always show, and shift hides them (TC4's default is the other way round). Default: false. */
		public boolean displayAspects = false;
		/** wand_dial_bottom: the wand's vis dial in the bottom left instead of the top left. Default: false. */
		public boolean wandDialBottom = false;
		/** notification_delay: how fast notifications scroll down and fade, in milliseconds. Default: 5000. */
		public int notificationDelay = 5000;
		/** notification_max: the most notifications on screen at once. Default: 15. */
		public int notificationMax = 15;

		// --- Portable hole ---
		/** portablehole_blacklist: comma-separated blocks the portable hole won't pass through ("minecraft:" may be left off). Default: iron_door. */
		public String portableHoleBlacklist = "iron_door";

		// --- NeoThaum Additions: not in TC4 (code in dev.neothaum.addon, to be split off later). The master switch is off by default; each
		// addition is on by default but does nothing until the master is on. ---
		/** The master switch for every NeoThaum Addition below. */
		public boolean additionsEnabled = false;
		/** Research Sharing: a research that lets a player teach their research to another, for warp. Takes effect on restart. */
		public boolean additionResearchSharing = true;
		/** Goggled fortress helm shows essentia tubes' flow, suction and contents. */
		public boolean additionEssentiaOverlay = true;
		/** A small warp readout beside the hotbar. */
		public boolean additionWarpHud = true;
		/** Sneak-use a golemancer's bell with another in the off hand to copy its markers to that bell and its golem. */
		public boolean additionMarkerCopy = true;

		/** Keeps every number within TC4's limits, as TC4 did when it read its config. */
		void validate() {
			this.researchDifficulty = Math.max(-1, Math.min(1, this.researchDifficulty));
			this.aspectTotalCap = Math.max(1, this.aspectTotalCap);
			this.golemDelay = Math.max(1, this.golemDelay);
			this.golemIgnoreDelay = Math.max(1000, this.golemIgnoreDelay);
			this.golemLinkQuality = Math.max(0, this.golemLinkQuality);
			this.runicRechargeSpeed = Math.max(500, this.runicRechargeSpeed);
			this.runicRechargeDelay = Math.max(0, this.runicRechargeDelay);
			this.runicCost = Math.max(0, this.runicCost);
			this.notificationDelay = Math.max(500, this.notificationDelay);
			this.notificationMax = Math.max(1, this.notificationMax);
			if (this.regenKey == null || this.regenKey.isBlank()) {
				this.regenKey = "DEFAULT";
			}
			if (this.portableHoleBlacklist == null) {
				this.portableHoleBlacklist = "";
			}
		}

		public boolean anyRegen() {
			return this.regenAuraNodes || this.regenStructures || this.regenCinnabar || this.regenAmber || this.regenInfusedStone || this.regenTrees;
		}
	}

	/**
	 * TC4's PacketConfig: the settings the server decides that the client also needs, sent when a player joins. Null when not connected; read
	 * through the accessors below, which fall back to this game's own config.
	 */
	public record Synced(int researchDifficulty, int aspectTotalCap, boolean hardNodes, boolean allowWarp) {
		public static Synced of(ConfigData c) {
			return new Synced(c.researchDifficulty, c.aspectTotalCap, c.hardNodes, c.allowWarp);
		}
	}

	public static Synced synced;

	public static int researchDifficulty() {
		return synced != null ? synced.researchDifficulty() : current.researchDifficulty;
	}

	public static int aspectTotalCap() {
		return synced != null ? synced.aspectTotalCap() : current.aspectTotalCap;
	}

	public static boolean hardNodes() {
		return synced != null ? synced.hardNodes() : current.hardNodes;
	}

	public static boolean allowWarp() {
		return synced != null ? synced.allowWarp() : current.allowWarp;
	}

	private static String blacklistText;
	private static java.util.Set<net.minecraft.resources.Identifier> blacklist = java.util.Set.of();

	/** The portable hole blacklist as block ids, read again whenever the setting's text changes. */
	public static java.util.Set<net.minecraft.resources.Identifier> portableHoleBlacklist() {
		String text = current.portableHoleBlacklist;
		if (!text.equals(blacklistText)) {
			java.util.Set<net.minecraft.resources.Identifier> ids = new java.util.HashSet<>();
			for (String part : text.split(",")) {
				net.minecraft.resources.Identifier id = part.isBlank() ? null
					: net.minecraft.resources.Identifier.tryParse(part.trim().toLowerCase(java.util.Locale.ROOT));
				if (id != null) {
					ids.add(id);
				}
			}
			blacklist = ids;
			blacklistText = text;
		}
		return blacklist;
	}

	// TC4's values, which the multipliers above scale
	public static final int TC4_NODE_RARITY = 36;
	public static final int TC4_NODE_VARIANT_RARITY = 18;
	public static final int TC4_ELDRITCH_RING_RARITY = 66;
	public static final int TC4_BARROW_MOUND_RARITY = 150;
	public static final int TC4_HILLTOP_STONES_RARITY = 40;
	/** generateTotem: nodeRarity x 10. */
	public static final int TC4_OBSIDIAN_TOTEM_RARITY = TC4_NODE_RARITY * 10;
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
						loaded.validate();
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
		current.validate();
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
