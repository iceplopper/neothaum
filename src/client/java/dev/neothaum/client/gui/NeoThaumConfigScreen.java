package dev.neothaum.client.gui;

import dev.neothaum.config.NeoThaumConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * The ModMenu config page: every TC4 setting, with TC4's defaults and units, and NeoThaum's own. Each control explains itself when hovered.
 * Tabs in two rows; a tab taller than the screen scrolls with the mouse wheel.
 */
public class NeoThaumConfigScreen extends Screen {
	private static final String[] TABS = {"Research", "World Gen", "World Regen", "Nodes", "Mobs", "Golems & Runic", "Taint & Alchemy", "Client", "NeoThaum Additions"};
	/** Below the tab rows (four tabs to a row). */
	private static final int TOP = 30 + (TABS.length + 3) / 4 * 22 + 10;
	private static final int ROW = 24;
	private static final int CELL = 175;

	private final Screen parent;
	private int activeTab = 0;
	private final int[] scroll = new int[TABS.length];
	private int col1;
	private int col2;
	private int slot;
	/** The current tab's controls with where they sit before scrolling, and the labels of its text boxes. */
	private final List<Placed> placed = new ArrayList<>();
	private final List<Label> labels = new ArrayList<>();

	private record Placed(AbstractWidget widget, int y) {
	}

	private record Label(String text, int x, int y, Placed owner) {
	}

	public NeoThaumConfigScreen(Screen parent) {
		super(Component.literal("NeoThaum Configuration"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		this.clearWidgets();
		this.placed.clear();
		this.labels.clear();
		int cx = this.width / 2;
		int tabWidth = 95;

		for (int i = 0; i < TABS.length; i++) {
			final int t = i;
			int column = i % 4;
			int tx = cx - (4 * tabWidth + 3 * 4) / 2 + column * (tabWidth + 4);
			int ty = 30 + i / 4 * 22;
			this.addRenderableWidget(Button.builder(Component.literal((i == this.activeTab ? "§6§l" : "§7") + TABS[i]), b -> {
				this.activeTab = t;
				this.init();
			}).bounds(tx, ty, Math.max(tabWidth, this.font.width(TABS[i]) + 14), 20).build());
		}

		var cfg = NeoThaumConfig.current;
		this.col1 = cx - CELL - 5;
		this.col2 = cx + 5;
		this.slot = 0;

		switch (this.activeTab) {
			case 0 -> {
				this.difficulty();
				this.intSlider("Aspect Total Cap", "aspect_total_cap. The research points of one aspect you can hold before the scanning soft cap kicks in: "
					+ "past it a scan gives only the square root of its points, and past 1.25 times it just 1.", 5, 1000, 5, "RP", 100,
					() -> cfg.aspectTotalCap, v -> cfg.aspectTotalCap = v);
				this.toggle("Warp", "The inverse of TC4's wuss_mode. Off disables warp and its events, and no research or crafting gives warp. You wuss. "
					+ "TC4 default: on.", () -> cfg.allowWarp, v -> cfg.allowWarp = v);
			}
			case 1 -> {
				this.toggle("Aura Nodes", "generate_aura_nodes. Wild aura nodes, and the nodes over desert temples, jungle temples and witch huts. "
					+ "TC4 default: on.", () -> cfg.generateAuraNodes, v -> cfg.generateAuraNodes = v);
				this.toggle("Structures", "generate_structures. Barrow mounds, eldritch rings, hilltop stones and obsidian totems. TC4 default: on.",
					() -> cfg.generateStructures, v -> cfg.generateStructures = v);
				this.toggle("Cinnabar Ore", "generate_cinnibar_ore. TC4 default: on.", () -> cfg.generateCinnabar, v -> cfg.generateCinnabar = v);
				this.toggle("Amber Ore", "generate_amber_ore. TC4 default: on.", () -> cfg.generateAmber, v -> cfg.generateAmber = v);
				this.toggle("Infused Stone", "generate_infused_stone. The six kinds of infused stone. TC4 default: on.", () -> cfg.generateInfusedStone,
					v -> cfg.generateInfusedStone = v);
				this.toggle("Trees", "generate_trees. Greatwood and silverwood trees, and cinderpearl and shimmerleaf. TC4 default: on.",
					() -> cfg.generateTrees, v -> cfg.generateTrees = v);
				this.toggle("Tainted Land", "generate_taint. Can taint biomes generate at worldgen. TC4 default: on.", () -> cfg.taintedLandGenerate,
					v -> cfg.taintedLandGenerate = v);
				this.slider("Tainted Land Size", "NeoThaum: how large the inland pockets of Tainted Land are (x1 is 5% of their region).",
					() -> cfg.taintedLandSizeMultiplier, v -> cfg.taintedLandSizeMultiplier = v);
				this.toggle("Magical Forest", "NeoThaum: whether Magical Forest generates in the overworld. Default: on.", () -> cfg.magicalForestGenerate,
					v -> cfg.magicalForestGenerate = v);
				this.slider("Magical Forest Size", "NeoThaum: how much of forest country is Magical Forest (x1 is 30%, close to TC4).",
					() -> cfg.magicalForestSizeMultiplier, v -> cfg.magicalForestSizeMultiplier = v);
				this.slider("Eldritch Rings", "NeoThaum: how often eldritch rings generate (x1 is TC4's 1 chunk in 66).", () -> cfg.eldritchRingMultiplier,
					v -> cfg.eldritchRingMultiplier = v);
				this.slider("Barrow Mounds", "NeoThaum: how often barrow mounds generate (x1 is TC4's 1 chunk in 150).", () -> cfg.barrowMoundMultiplier,
					v -> cfg.barrowMoundMultiplier = v);
				this.slider("Hilltop Stones", "NeoThaum: how often hilltop stones generate (x1 is TC4's 1 chunk in 40).",
					() -> cfg.hilltopStonesMultiplier, v -> cfg.hilltopStonesMultiplier = v);
				this.slider("Obsidian Totems", "NeoThaum: how often obsidian totems generate (x1 is TC4's 1 chunk in 360).",
					() -> cfg.obsidianTotemMultiplier, v -> cfg.obsidianTotemMultiplier = v);
				this.toggle("Village Buildings", "NeoThaum: whether villages can have wizard towers and bankers' houses. Default: on.",
					() -> cfg.villageBuildings, v -> cfg.villageBuildings = v);
			}
			case 2 -> {
				this.text("Regen Key", "regen_key. This key is used to keep track of which chunks have been generated/regenerated. Changing it will "
					+ "cause the regeneration code to run again, so only change it if you want it to happen. Useful to regen only one world feature at "
					+ "a time. TC4 default: DEFAULT.", 32, () -> cfg.regenKey, v -> cfg.regenKey = v);
				String regen = " in chunks that were made before, or under another regen key, as they load. TC4 default: off.";
				this.toggle("Regen Aura Nodes", "aura_nodes. Generates wild aura nodes again" + regen, () -> cfg.regenAuraNodes,
					v -> cfg.regenAuraNodes = v);
				this.toggle("Regen Structures", "structures. Generates TC4's structures again" + regen, () -> cfg.regenStructures,
					v -> cfg.regenStructures = v);
				this.toggle("Regen Cinnabar", "cinnibar_ore. Generates cinnabar ore again" + regen, () -> cfg.regenCinnabar, v -> cfg.regenCinnabar = v);
				this.toggle("Regen Amber", "amber_ore. Generates amber ore again" + regen, () -> cfg.regenAmber, v -> cfg.regenAmber = v);
				this.toggle("Regen Infused Stone", "infused_stone. Generates infused stone again" + regen, () -> cfg.regenInfusedStone,
					v -> cfg.regenInfusedStone = v);
				this.toggle("Regen Trees", "trees. Generates greatwood and silverwood trees again" + regen, () -> cfg.regenTrees,
					v -> cfg.regenTrees = v);
				this.toggle("Regen Taint", "taint. TC4 had this switch but never used it: taint biomes are made only when the world is first "
					+ "generated. Kept for parity; it has no effect. TC4 default: off.", () -> cfg.regenTaint, v -> cfg.regenTaint = v);
			}
			case 3 -> {
				this.slider("Nodes", "node_rarity. How often wild aura nodes generate (x1 is TC4's one node per 36 chunks).", () -> cfg.nodeMultiplier,
					v -> cfg.nodeMultiplier = v);
				this.slider("Special Nodes", "special_node_rarity. How often a wild node is special: pure, sinister, unstable and the rest (x1 is "
					+ "TC4's 1 in 18).", () -> cfg.nodeVariantMultiplier, v -> cfg.nodeVariantMultiplier = v);
				this.slider("Sinister Nodes", "NeoThaum: how likely a special node is sinister, against the other kinds (TC4 weight 3).",
					() -> cfg.darkNodeMultiplier, v -> cfg.darkNodeMultiplier = v);
				this.slider("Unstable Nodes", "NeoThaum: how likely a special node is unstable, against the other kinds (TC4 weight 3).",
					() -> cfg.unstableNodeMultiplier, v -> cfg.unstableNodeMultiplier = v);
				this.slider("Pure Nodes", "NeoThaum: how likely a special node is pure, against the other kinds (TC4 weight 3).",
					() -> cfg.pureNodeMultiplier, v -> cfg.pureNodeMultiplier = v);
				this.slider("Hungry Nodes", "NeoThaum: how likely a special node is hungry, against the other kinds (TC4 weight 1).",
					() -> cfg.hungryNodeMultiplier, v -> cfg.hungryNodeMultiplier = v);
				this.slider("Tainted Nodes", "NeoThaum: how likely a special node is tainted, against the other kinds (TC4 weight 1).",
					() -> cfg.taintedNodeMultiplier, v -> cfg.taintedNodeMultiplier = v);
				this.slider("Bright Nodes", "NeoThaum: how likely a special node is bright, against the other modifiers (TC4 weight 1).",
					() -> cfg.brightNodeMultiplier, v -> cfg.brightNodeMultiplier = v);
				this.slider("Pale Nodes", "NeoThaum: how likely a special node is pale, against the other modifiers (TC4 weight 1).",
					() -> cfg.paleNodeMultiplier, v -> cfg.paleNodeMultiplier = v);
				this.slider("Fading Nodes", "NeoThaum: how likely a special node is fading, against the other modifiers (TC4 weight 1).",
					() -> cfg.fadingNodeMultiplier, v -> cfg.fadingNodeMultiplier = v);
				this.toggle("Hard Nodes", "hard_mode_nodes. Negative nodes like hungry, tainted or dark nodes will have additional, much nastier, "
					+ "effects. TC4 default: on.", () -> cfg.hardNodes, v -> cfg.hardNodes = v);
				this.toggle("Bright Node Light", "NeoThaum: bright nodes light their surroundings fully (light 15; TC4 gave every node 8). Default: on.",
					() -> cfg.brightNodeLight, v -> cfg.brightNodeLight = v);
			}
			case 4 -> {
				String spawn = " Spawners and spawn eggs still work. TC4 default: on.";
				this.toggle("Angry Zombies", "spawn_angry_zombies. Angry and giant angry zombies spawn wherever monsters do." + spawn,
					() -> cfg.spawnAngryZombies, v -> cfg.spawnAngryZombies = v);
				this.toggle("Fire Bats", "spawn_fire_bats. Fire bats spawn in the nether (and everywhere at Halloween)." + spawn,
					() -> cfg.spawnFireBats, v -> cfg.spawnFireBats = v);
				this.toggle("Wisps", "spawn_wisps. Wisps spawn in the nether, the Magical Forest and the eerie biome." + spawn, () -> cfg.spawnWisps,
					v -> cfg.spawnWisps = v);
				this.toggle("Taintacles", "spawn_taintacles. Taintacles spawn in Tainted Land." + spawn, () -> cfg.spawnTaintacles,
					v -> cfg.spawnTaintacles = v);
				this.toggle("Taint Spores", "spawn_taint_spores. Crusted taint releases spore swarmers and taint fibres grow spores. TC4 default: on.",
					() -> cfg.spawnTaintSpores, v -> cfg.spawnTaintSpores = v);
				this.toggle("Pechs", "spawn_pechs. Pechs spawn in magical biomes." + spawn, () -> cfg.spawnPechs, v -> cfg.spawnPechs = v);
				this.toggle("Elder Creatures", "spawn_eldercreatures. Eldritch guardians spawn in the eerie biome." + spawn,
					() -> cfg.spawnElderCreatures, v -> cfg.spawnElderCreatures = v);
				this.toggle("Champion Mobs", "champion_mobs. Setting this to off will disable spawning champion mobs. TC4 default: on.",
					() -> cfg.championMobs, v -> cfg.championMobs = v);
				this.slider("Wisp Spawns", "NeoThaum: how often wisps spawn in the nether (x1 is TC4's weight 5). Takes effect on restart.",
					() -> cfg.wispSpawnMultiplier, v -> cfg.wispSpawnMultiplier = v);
				this.toggle("Taint Corrupts Dying Mobs", "NeoThaum: mobs dying of taint come back as tainted versions; off leaves only the tainted mobs "
					+ "that spawn. Default: on.", () -> cfg.corruptExistingMobs, v -> cfg.corruptExistingMobs = v);
			}
			case 5 -> {
				this.intSlider("Golem Delay", "golem_delay. How many ticks a golem waits between checking for tasks. Setting it higher will save "
					+ "server ticks, but will make the golems slower to react.", 1, 100, 1, "ticks", 5, () -> cfg.golemDelay, v -> cfg.golemDelay = v);
				this.intSlider("Golem Ignore Delay", "golem_ignore_delay. How many milliseconds a golem will ignore an item after it has failed to find "
					+ "a destination or use for it. Min value 1000.", 1000, 60000, 500, "ms", 10000, () -> cfg.golemIgnoreDelay,
					v -> cfg.golemIgnoreDelay = v);
				this.intSlider("Golem Link Quality", "golem_link_quality. The fx quality of the line connecting golems to marked blocks, seen while "
					+ "holding a golemancer's bell. Setting this below 4 deactivates the effect entirely.", 0, 32, 1, "", 16, () -> cfg.golemLinkQuality,
					v -> cfg.golemLinkQuality = v);
				this.toggle("Golem Chest Interact", "golem_chest_interact. If set to on golems will attempt to play the chest opening animations and "
					+ "sounds whenever they interact with them. TC4 default: on.", () -> cfg.golemChestInteract, v -> cfg.golemChestInteract = v);
				this.intSlider("Runic Recharge Speed", "runic_recharge_speed. How many milliseconds pass between runic shielding recharge ticks. Lower "
					+ "values equals faster recharge. Minimum of 500.", 500, 10000, 100, "ms", 2000, () -> cfg.runicRechargeSpeed,
					v -> cfg.runicRechargeSpeed = v);
				this.intSlider("Runic Recharge Delay", "runic_recharge_delay. How many game ticks pass after a shield has been reduced to zero before "
					+ "it can start recharging again. Minimum of 0.", 0, 400, 5, "ticks", 80, () -> cfg.runicRechargeDelay,
					v -> cfg.runicRechargeDelay = v);
				this.intSlider("Runic Cost", "runic_cost. How much aer and terra centi-vis (0.01 vis) it costs to recharge a single unit of shielding. "
					+ "Minimum of 0.", 0, 500, 5, "cv", 50, () -> cfg.runicCost, v -> cfg.runicCost = v);
			}
			case 6 -> {
				this.toggle("Taint Spread", "NeoThaum: whether taint fibres and Tainted Land spread. Default: on.", () -> cfg.taintSpread,
					v -> cfg.taintSpread = v);
				this.slider("Taint Spread Speed", "taint_spread_rate. How fast Tainted Land creeps outward (x1 is TC4's 1 in 200 per try).",
					() -> cfg.taintSpreadMultiplier, v -> cfg.taintSpreadMultiplier = v);
				this.slider("Flux", "NeoThaum: how much flux crucibles spill.", () -> cfg.fluxMultiplier, v -> cfg.fluxMultiplier = v);
				this.slider("Infernal Furnace Speed", "NeoThaum: how fast the infernal furnace smelts.", () -> cfg.infernalFurnaceSpeedMultiplier,
					v -> cfg.infernalFurnaceSpeedMultiplier = v);
				this.slider("Infernal Furnace Bonus", "NeoThaum: how many bonus nuggets the infernal furnace gives.",
					() -> cfg.infernalFurnaceBonusMultiplier, v -> cfg.infernalFurnaceBonusMultiplier = v);
				this.slider("Aspect Orb Drops", "NeoThaum: the chance of an aspect orb from a mob you kill.", () -> cfg.aspectOrbMultiplier,
					v -> cfg.aspectOrbMultiplier = v);
				this.text("Portable Hole Blacklist", "portablehole_blacklist. This is a comma-delimited list of any block names the portable hole is not "
					+ "allowed to pass through (minecraft: may be left off). TC4 default: iron_door.", 512, () -> cfg.portableHoleBlacklist,
					v -> cfg.portableHoleBlacklist = v);
			}
			case 8 -> {
				this.toggle("NeoThaum Additions", "The master switch for every addition on this page: features that are not in TC4. While it is off, "
					+ "none of them do anything, whatever their own switches say. Default: off.", () -> cfg.additionsEnabled, v -> {
						cfg.additionsEnabled = v;
						this.init();
					});
				this.slot++;
				int locked = this.placed.size();
				String off = " Not in TC4. Needs the NeoThaum Additions switch above. Default: on.";
				this.toggle("Research Sharing", "Adds the Shared Understanding research (Basics, after Research Mastery): sneak and use your Thaumonomicon "
					+ "on another player to teach them the research you know, except research that brings warp. Each lesson costs you 1 permanent and "
					+ "3 sticky warp. Takes effect on restart." + off, () -> cfg.additionResearchSharing, v -> cfg.additionResearchSharing = v);
				this.toggle("Essentia Overlay", "While wearing a thaumium fortress helm fitted with goggles, looking at an essentia tube shows what it "
					+ "holds, what it pulls and how hard, its open sides and its filter." + off, () -> cfg.additionEssentiaOverlay,
					v -> cfg.additionEssentiaOverlay = v);
				this.toggle("Warp Readout", "Shows your warp beside the hotbar: the total, then permanent/sticky/temporary." + off,
					() -> cfg.additionWarpHud, v -> cfg.additionWarpHud = v);
				this.toggle("Golem Marker Copy", "Hold a golemancer's bell in each hand, sneak and use the main-hand one in the air: its markers replace "
					+ "the off-hand bell's, and that bell's golem takes them too." + off, () -> cfg.additionMarkerCopy, v -> cfg.additionMarkerCopy = v);
				this.toggle("Modded Item Aspects", "Items from other mods that have no aspects and no recipe to work them out from take aspects from "
					+ "their common tags: ores, ingots, gems, dusts, tools, armour, food, crops, logs and so on. Items crafted from them then get aspects "
					+ "through TC4's recipe rule. Vanilla and NeoThaum items are untouched. Applies when the world loads or /reload runs." + off,
					() -> cfg.additionModdedAspects, v -> cfg.additionModdedAspects = v);
				for (Placed p : this.placed.subList(locked, this.placed.size())) {
					p.widget().active = cfg.additionsEnabled;
				}
			}
			case 7 -> {
				this.toggle("Color Blind", "color_blind. Setting this to on will make certain colors higher contrast or darker to prevent them from "
					+ "being 'invisible' to color blind people (the research table's aspect orbs). TC4 default: off.", () -> cfg.colorBlind,
					v -> cfg.colorBlind = v);
				this.toggle("Display Aspects", "display_aspects. Item aspects are hidden by default and pressing shift reveals them. Changing this "
					+ "setting to on will reverse this behaviour and always display aspects unless shift is pressed. TC4 default: off.",
					() -> cfg.displayAspects, v -> cfg.displayAspects = v);
				this.toggle("Wand Dial Bottom", "wand_dial_bottom. Set to on to have the wand dial display in the bottom left instead of the top left. "
					+ "TC4 default: off.", () -> cfg.wandDialBottom, v -> cfg.wandDialBottom = v);
				this.intSlider("Notification Delay", "notification_delay. Determines how fast notifications scroll downwards, in milliseconds.", 500,
					20000, 250, "ms", 5000, () -> cfg.notificationDelay, v -> cfg.notificationDelay = v);
				this.intSlider("Notification Max", "notification_max. The maximum amount of notifications that are displayed onscreen.", 1, 50, 1, "",
					15, () -> cfg.notificationMax, v -> cfg.notificationMax = v);
				this.toggle("Bright Node Lens Flare", "NeoThaum: looking toward a bright node in clear sight shows a lens flare, while an Iris shader "
					+ "pack is in use. Default: on.", () -> cfg.brightNodeFlare, v -> cfg.brightNodeFlare = v);
				this.toggle("World Map Nodes", "NeoThaum: the aura nodes you have seen are drawn on Xaero's World Map. Default: on.",
					() -> cfg.worldMapNodes, v -> cfg.worldMapNodes = v);
				this.toggle("Creative Dev Tab", "NeoThaum: shows the creative mode \"TC: Dev Tools\" tab. Default: on.", () -> cfg.showCreativeDebugTab,
					v -> cfg.showCreativeDebugTab = v);
			}
		}
		this.applyScroll();

		int by = this.height - 30;
		this.addRenderableWidget(Button.builder(Component.literal("Save & Close"), b -> {
			NeoThaumConfig.save();
			if (this.minecraft != null) {
				this.minecraft.gui.setScreen(this.parent);
			}
		}).bounds(cx - 155, by, 100, 20).tooltip(Tooltip.create(Component.literal("Saves config/neothaum.json and closes."))).build());

		this.addRenderableWidget(Button.builder(Component.literal("Reset Defaults"), b -> {
			NeoThaumConfig.current = new NeoThaumConfig.ConfigData();
			NeoThaumConfig.save();
			this.init();
		}).bounds(cx - 50, by, 100, 20).tooltip(Tooltip.create(Component.literal("Puts every setting back to TC4's default (and NeoThaum's own "
			+ "defaults for its additions), and saves."))).build());

		this.addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> {
			NeoThaumConfig.load();
			if (this.minecraft != null) {
				this.minecraft.gui.setScreen(this.parent);
			}
		}).bounds(cx + 55, by, 100, 20).tooltip(Tooltip.create(Component.literal("Closes without saving."))).build());
	}

	/** The bottom of the area the settings scroll in. */
	private int bottom() {
		return this.height - 36;
	}

	/** Moves the tab's controls by its scroll, hiding any outside the scrolling area. */
	private int maxScroll() {
		int lastRow = (this.slot + 1) / 2;
		return Math.max(0, TOP + lastRow * ROW - 4 - this.bottom());
	}

	private void applyScroll() {
		this.scroll[this.activeTab] = Mth.clamp(this.scroll[this.activeTab], 0, this.maxScroll());
		for (Placed p : this.placed) {
			int y = p.y() - this.scroll[this.activeTab];
			p.widget().setY(y);
			p.widget().visible = y >= TOP - 2 && y + 20 <= this.bottom() + 2;
		}
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		int before = this.scroll[this.activeTab];
		this.scroll[this.activeTab] -= (int) (scrollY * ROW);
		this.applyScroll();
		return before != this.scroll[this.activeTab] || super.mouseScrolled(x, y, scrollX, scrollY);
	}

	/** The next place in the two-column layout. */
	private int[] place() {
		int x = this.slot % 2 == 0 ? this.col1 : this.col2;
		int y = TOP + this.slot / 2 * ROW;
		this.slot++;
		return new int[]{x, y};
	}

	private <W extends AbstractWidget> W add(W widget, int y, String description) {
		widget.setTooltip(Tooltip.create(Component.literal(description)));
		this.placed.add(new Placed(widget, y));
		return this.addRenderableWidget(widget);
	}

	private static String onOff(boolean on) {
		return on ? "ON" : "OFF";
	}

	private void toggle(String label, String description, BooleanSupplier get, Consumer<Boolean> set) {
		int[] at = this.place();
		this.add(Button.builder(Component.literal(label + ": " + onOff(get.getAsBoolean())), b -> {
			set.accept(!get.getAsBoolean());
			b.setMessage(Component.literal(label + ": " + onOff(get.getAsBoolean())));
		}).bounds(at[0], at[1], CELL, 20).build(), at[1], description);
	}

	/** research_difficulty, cycling easy, normal, hard. */
	private void difficulty() {
		var cfg = NeoThaumConfig.current;
		String[] names = {"Easy", "Normal", "Hard"};
		int[] at = this.place();
		this.add(Button.builder(Component.literal("Research Difficulty: " + names[cfg.researchDifficulty + 1]), b -> {
			cfg.researchDifficulty = cfg.researchDifficulty >= 1 ? -1 : cfg.researchDifficulty + 1;
			b.setMessage(Component.literal("Research Difficulty: " + names[cfg.researchDifficulty + 1]));
		}).bounds(at[0], at[1], CELL, 20).build(), at[1], "research_difficulty. Normal (TC4 default): secondary research is bought with research "
			+ "points, the rest solved at the research table. Easy: all research items are directly purchased with RP. Hard: all research items need "
			+ "to be solved via the research table.");
	}

	/** A multiplier on TC4's own setting: 0 to 10 in steps of 0.1, where 1 is TC4 and 0 is off. */
	private void slider(String label, String description, Supplier<Float> get, Consumer<Float> set) {
		int[] at = this.place();
		this.add(new MultiplierSlider(at[0], at[1], label, get.get(), set), at[1], description + " From x0 (off) to x10 in steps of 0.1.");
	}

	/** A whole number in TC4's unit, from min to max in steps of step, marked where it is TC4's default. */
	private void intSlider(String label, String description, int min, int max, int step, String unit, int tc4, IntSupplier get, IntConsumer set) {
		int[] at = this.place();
		this.add(new IntSlider(at[0], at[1], label, min, max, step, unit, tc4, get.getAsInt(), set), at[1],
			description + " TC4 default: " + tc4 + (unit.isEmpty() ? "" : " " + unit) + ". From " + min + " to " + max + " in steps of " + step + ".");
	}

	/** A text setting: its label on the left of the cell, the box on the right. */
	private void text(String label, String description, int maxLength, Supplier<String> get, Consumer<String> set) {
		int[] at = this.place();
		int labelWidth = Math.min(this.font.width(label) + 6, 80);
		EditBox box = new EditBox(this.font, at[0] + labelWidth, at[1], CELL - labelWidth, 20, Component.literal(label));
		box.setMaxLength(maxLength);
		box.setValue(get.get());
		box.setResponder(set);
		this.add(box, at[1], description);
		this.labels.add(new Label(label, at[0], at[1], this.placed.getLast()));
	}

	private static final class MultiplierSlider extends AbstractSliderButton {
		private final String label;
		private final Consumer<Float> set;

		MultiplierSlider(int x, int y, String label, float initial, Consumer<Float> set) {
			super(x, y, CELL, 20, Component.empty(), Mth.clamp(initial, 0.0F, 10.0F) / 10.0);
			this.label = label;
			this.set = set;
			this.updateMessage();
		}

		private float multiplier() {
			return Math.round(this.value * 100.0) / 10.0F;
		}

		@Override
		protected void updateMessage() {
			float m = this.multiplier();
			String v = m == 0.0F ? "Off" : String.format("x%.1f", m) + (m == 1.0F ? " (TC4)" : "");
			this.setMessage(Component.literal(this.label + ": " + v));
		}

		@Override
		protected void applyValue() {
			float m = this.multiplier();
			this.value = m / 10.0;
			this.set.accept(m);
		}
	}

	private static final class IntSlider extends AbstractSliderButton {
		private final String label;
		private final int min;
		private final int max;
		private final int step;
		private final String unit;
		private final int tc4;
		private final IntConsumer set;

		IntSlider(int x, int y, String label, int min, int max, int step, String unit, int tc4, int initial, IntConsumer set) {
			super(x, y, CELL, 20, Component.empty(), (Mth.clamp(initial, min, max) - min) / (double) (max - min));
			this.label = label;
			this.min = min;
			this.max = max;
			this.step = step;
			this.unit = unit;
			this.tc4 = tc4;
			this.set = set;
			this.updateMessage();
		}

		private int number() {
			int raw = this.min + (int) Math.round(this.value * (this.max - this.min) / this.step) * this.step;
			return Mth.clamp(raw, this.min, this.max);
		}

		@Override
		protected void updateMessage() {
			int n = this.number();
			String v = this.label.equals("Golem Link Quality") && n < 4 ? "Off" : n + (this.unit.isEmpty() ? "" : " " + this.unit);
			this.setMessage(Component.literal(this.label + ": " + v + (n == this.tc4 ? " (TC4)" : "")));
		}

		@Override
		protected void applyValue() {
			int n = this.number();
			this.value = (n - this.min) / (double) (this.max - this.min);
			this.set.accept(n);
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float pt) {
		super.extractRenderState(g, mx, my, pt);
		g.text(this.font, this.title, this.width / 2 - this.font.width(this.title) / 2, 12, 0xFFFFFFFF, true);
		int max = this.maxScroll();
		if (max > 0) {
			// a scrollbar beside the settings when the tab is taller than the screen
			int x = this.col2 + CELL + 4;
			int track = this.bottom() - TOP;
			int thumb = Math.max(12, track * track / (track + max));
			int at = TOP + (track - thumb) * this.scroll[this.activeTab] / max;
			g.fill(x, TOP, x + 3, TOP + track, 0x60000000);
			g.fill(x, at, x + 3, at + thumb, 0xFFC0C0C0);
		}
		for (Label l : this.labels) {
			if (l.owner().widget().visible) {
				g.text(this.font, l.text(), l.x(), l.owner().widget().getY() + 6, 0xFFE0E0E0, true);
			}
		}
		if (this.minecraft != null && this.minecraft.getConnection() != null && !this.minecraft.hasSingleplayerServer()) {
			String note = "This server's settings apply here; changes on this page only edit your own config.";
			g.text(this.font, note, this.width / 2 - this.font.width(note) / 2, this.bottom() + 1, 0xFFFFAA55, true);
		}
	}

	@Override
	public void onClose() {
		NeoThaumConfig.load();
		if (this.minecraft != null) {
			this.minecraft.gui.setScreen(this.parent);
		}
	}
}
