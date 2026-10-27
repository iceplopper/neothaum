package dev.neothaum.client.gui;

import dev.neothaum.config.NeoThaumConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class NeoThaumConfigScreen extends Screen {
	private final Screen parent;
	private int activeTab = 0; // 0: Nodes, 1: Biomes & Structures, 2: Taint & Flux, 3: Alchemy & Debug

	public NeoThaumConfigScreen(Screen parent) {
		super(Component.literal("NeoThaum Configuration"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		this.clearWidgets();
		int cx = this.width / 2;
		int tabWidth = 95;
		int tabY = 32;

		// Tab Selectors
		String[] tabs = {"Nodes", "Biomes & Structures", "Taint & Flux", "Alchemy & Debug"};
		for (int i = 0; i < tabs.length; i++) {
			final int t = i;
			int tx = cx - (tabs.length * tabWidth + (tabs.length - 1) * 4) / 2 + i * (tabWidth + 4);
			this.addRenderableWidget(Button.builder(Component.literal((i == this.activeTab ? "§6§l" : "§7") + tabs[i]), b -> {
				this.activeTab = t;
				this.init();
			}).bounds(tx, tabY, tabWidth, 20).build());
		}

		var cfg = NeoThaumConfig.current;
		int col1 = cx - 180;
		int col2 = cx + 5;
		int yStart = 62;
		int dy = 24;

		if (this.activeTab == 0) {
			// Tab 0: Nodes
			this.addRenderableWidget(Button.builder(Component.literal("Node Rarity: " + (cfg.nodeRarity <= 0 ? "Disabled" : "1 in " + cfg.nodeRarity)), b -> {
				int[] steps = {0, 18, 24, 30, 36, 48, 64, 96, 128};
				cfg.nodeRarity = nextVal(steps, cfg.nodeRarity);
				b.setMessage(Component.literal("Node Rarity: " + (cfg.nodeRarity <= 0 ? "Disabled" : "1 in " + cfg.nodeRarity)));
			}).bounds(col1, yStart, 175, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("Variant Rarity: 1 in " + cfg.nodeVariantRarity), b -> {
				int[] steps = {6, 10, 14, 18, 24, 32};
				cfg.nodeVariantRarity = nextVal(steps, cfg.nodeVariantRarity);
				b.setMessage(Component.literal("Variant Rarity: 1 in " + cfg.nodeVariantRarity));
			}).bounds(col2, yStart, 175, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("Dark Node Weight: " + cfg.darkNodeWeight), b -> {
				cfg.darkNodeWeight = (cfg.darkNodeWeight + 1) % 6;
				b.setMessage(Component.literal("Dark Node Weight: " + cfg.darkNodeWeight));
			}).bounds(col1, yStart + dy, 175, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("Unstable Node Weight: " + cfg.unstableNodeWeight), b -> {
				cfg.unstableNodeWeight = (cfg.unstableNodeWeight + 1) % 6;
				b.setMessage(Component.literal("Unstable Node Weight: " + cfg.unstableNodeWeight));
			}).bounds(col2, yStart + dy, 175, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("Pure Node Weight: " + cfg.pureNodeWeight), b -> {
				cfg.pureNodeWeight = (cfg.pureNodeWeight + 1) % 6;
				b.setMessage(Component.literal("Pure Node Weight: " + cfg.pureNodeWeight));
			}).bounds(col1, yStart + dy * 2, 175, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("Hungry Node Weight: " + cfg.hungryNodeWeight), b -> {
				cfg.hungryNodeWeight = (cfg.hungryNodeWeight + 1) % 6;
				b.setMessage(Component.literal("Hungry Node Weight: " + cfg.hungryNodeWeight));
			}).bounds(col2, yStart + dy * 2, 175, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("Tainted Node Weight: " + cfg.taintedNodeWeight), b -> {
				cfg.taintedNodeWeight = (cfg.taintedNodeWeight + 1) % 6;
				b.setMessage(Component.literal("Tainted Node Weight: " + cfg.taintedNodeWeight));
			}).bounds(col1, yStart + dy * 3, 175, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("Hard Nodes: " + (cfg.hardNodes ? "ON" : "OFF")), b -> {
				cfg.hardNodes = !cfg.hardNodes;
				b.setMessage(Component.literal("Hard Nodes: " + (cfg.hardNodes ? "ON" : "OFF")));
			}).bounds(col2, yStart + dy * 3, 175, 20).build());
		} else if (this.activeTab == 1) {
			// Tab 1: Biomes & Structures
			this.addRenderableWidget(Button.builder(Component.literal("Magical Forest: " + (cfg.magicalForestGenerate ? "Enabled" : "Disabled")), b -> {
				cfg.magicalForestGenerate = !cfg.magicalForestGenerate;
				b.setMessage(Component.literal("Magical Forest: " + (cfg.magicalForestGenerate ? "Enabled" : "Disabled")));
			}).bounds(col1, yStart, 175, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("Forest Size: " + cfg.magicalForestSize + "%"), b -> {
				int[] steps = {10, 20, 30, 40, 50, 70};
				cfg.magicalForestSize = nextVal(steps, cfg.magicalForestSize);
				b.setMessage(Component.literal("Forest Size: " + cfg.magicalForestSize + "%"));
			}).bounds(col2, yStart, 175, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("Tainted Land: " + (cfg.taintedLandGenerate ? "Enabled" : "Disabled")), b -> {
				cfg.taintedLandGenerate = !cfg.taintedLandGenerate;
				b.setMessage(Component.literal("Tainted Land: " + (cfg.taintedLandGenerate ? "Enabled" : "Disabled")));
			}).bounds(col1, yStart + dy, 175, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("Tainted Size: " + cfg.taintedLandSize + "%"), b -> {
				int[] steps = {2, 5, 10, 15, 25};
				cfg.taintedLandSize = nextVal(steps, cfg.taintedLandSize);
				b.setMessage(Component.literal("Tainted Size: " + cfg.taintedLandSize + "%"));
			}).bounds(col2, yStart + dy, 175, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("Eldritch Ring: " + (cfg.eldritchRingRarity <= 0 ? "Off" : "1/" + cfg.eldritchRingRarity)), b -> {
				int[] steps = {0, 33, 50, 66, 100, 150};
				cfg.eldritchRingRarity = nextVal(steps, cfg.eldritchRingRarity);
				b.setMessage(Component.literal("Eldritch Ring: " + (cfg.eldritchRingRarity <= 0 ? "Off" : "1/" + cfg.eldritchRingRarity)));
			}).bounds(col1, yStart + dy * 2, 175, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("Barrow Mound: " + (cfg.barrowMoundRarity <= 0 ? "Off" : "1/" + cfg.barrowMoundRarity)), b -> {
				int[] steps = {0, 75, 100, 150, 200, 300};
				cfg.barrowMoundRarity = nextVal(steps, cfg.barrowMoundRarity);
				b.setMessage(Component.literal("Barrow Mound: " + (cfg.barrowMoundRarity <= 0 ? "Off" : "1/" + cfg.barrowMoundRarity)));
			}).bounds(col2, yStart + dy * 2, 175, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("Wizard Tower: " + (cfg.wizardTowerRarity <= 0 ? "Off" : "1/" + cfg.wizardTowerRarity)), b -> {
				int[] steps = {0, 60, 90, 120, 180, 250};
				cfg.wizardTowerRarity = nextVal(steps, cfg.wizardTowerRarity);
				b.setMessage(Component.literal("Wizard Tower: " + (cfg.wizardTowerRarity <= 0 ? "Off" : "1/" + cfg.wizardTowerRarity)));
			}).bounds(col1, yStart + dy * 3, 175, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("Banker House: " + (cfg.bankerHomeRarity <= 0 ? "Off" : "1/" + cfg.bankerHomeRarity)), b -> {
				int[] steps = {0, 70, 100, 140, 200, 280};
				cfg.bankerHomeRarity = nextVal(steps, cfg.bankerHomeRarity);
				b.setMessage(Component.literal("Banker House: " + (cfg.bankerHomeRarity <= 0 ? "Off" : "1/" + cfg.bankerHomeRarity)));
			}).bounds(col2, yStart + dy * 3, 175, 20).build());
		} else if (this.activeTab == 2) {
			// Tab 2: Taint & Flux
			this.addRenderableWidget(Button.builder(Component.literal("Taint Spread: " + (cfg.taintSpread ? "ON" : "OFF")), b -> {
				cfg.taintSpread = !cfg.taintSpread;
				b.setMessage(Component.literal("Taint Spread: " + (cfg.taintSpread ? "ON" : "OFF")));
			}).bounds(col1, yStart, 175, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("Spread Delay: " + cfg.taintSpreadRate + " ticks"), b -> {
				int[] steps = {50, 100, 200, 350, 500};
				cfg.taintSpreadRate = nextVal(steps, cfg.taintSpreadRate);
				b.setMessage(Component.literal("Spread Delay: " + cfg.taintSpreadRate + " ticks"));
			}).bounds(col2, yStart, 175, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("Corrupt Mobs: " + (cfg.corruptExistingMobs ? "Dying Mobs (TC4)" : "Spawn Only")), b -> {
				cfg.corruptExistingMobs = !cfg.corruptExistingMobs;
				b.setMessage(Component.literal("Corrupt Mobs: " + (cfg.corruptExistingMobs ? "Dying Mobs (TC4)" : "Spawn Only")));
			}).bounds(col1, yStart + dy, 175, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("Flux Multiplier: " + cfg.fluxMultiplier + "x"), b -> {
				float[] steps = {0.0f, 0.5f, 1.0f, 1.5f, 2.0f, 3.0f};
				cfg.fluxMultiplier = nextVal(steps, cfg.fluxMultiplier);
				b.setMessage(Component.literal("Flux Multiplier: " + cfg.fluxMultiplier + "x"));
			}).bounds(col2, yStart + dy, 175, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("Warp Enabled: " + (cfg.allowWarp ? "ON" : "OFF")), b -> {
				cfg.allowWarp = !cfg.allowWarp;
				b.setMessage(Component.literal("Warp Enabled: " + (cfg.allowWarp ? "ON" : "OFF")));
			}).bounds(col1, yStart + dy * 2, 175, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("Wisp Spawn Rate: " + cfg.wispSpawnRate + "x"), b -> {
				float[] steps = {0.0f, 0.5f, 1.0f, 1.5f, 2.0f};
				cfg.wispSpawnRate = nextVal(steps, cfg.wispSpawnRate);
				b.setMessage(Component.literal("Wisp Spawn Rate: " + cfg.wispSpawnRate + "x"));
			}).bounds(col2, yStart + dy * 2, 175, 20).build());
		} else {
			// Tab 3: Alchemy & Creative
			this.addRenderableWidget(Button.builder(Component.literal("Infernal Speed: " + cfg.infernalFurnaceSpeedMultiplier + "x"), b -> {
				float[] steps = {0.5f, 1.0f, 1.5f, 2.0f, 3.0f, 5.0f};
				cfg.infernalFurnaceSpeedMultiplier = nextVal(steps, cfg.infernalFurnaceSpeedMultiplier);
				b.setMessage(Component.literal("Infernal Speed: " + cfg.infernalFurnaceSpeedMultiplier + "x"));
			}).bounds(col1, yStart, 175, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("Infernal Bonus: " + cfg.infernalFurnaceBonusMultiplier + "x"), b -> {
				float[] steps = {0.0f, 0.5f, 1.0f, 1.5f, 2.0f, 3.0f};
				cfg.infernalFurnaceBonusMultiplier = nextVal(steps, cfg.infernalFurnaceBonusMultiplier);
				b.setMessage(Component.literal("Infernal Bonus: " + cfg.infernalFurnaceBonusMultiplier + "x"));
			}).bounds(col2, yStart, 175, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("Creative Dev Tab: " + (cfg.showCreativeDebugTab ? "Visible" : "Hidden")), b -> {
				cfg.showCreativeDebugTab = !cfg.showCreativeDebugTab;
				b.setMessage(Component.literal("Creative Dev Tab: " + (cfg.showCreativeDebugTab ? "Visible" : "Hidden")));
			}).bounds(col1, yStart + dy, 175, 20).build());

			this.addRenderableWidget(Button.builder(Component.literal("Champion Mobs: " + (cfg.championMobs ? "ON" : "OFF")), b -> {
				cfg.championMobs = !cfg.championMobs;
				b.setMessage(Component.literal("Champion Mobs: " + (cfg.championMobs ? "ON" : "OFF")));
			}).bounds(col2, yStart + dy, 175, 20).build());
		}

		// Bottom Buttons
		int by = this.height - 30;
		this.addRenderableWidget(Button.builder(Component.literal("Save & Close"), b -> {
			NeoThaumConfig.save();
			if (this.minecraft != null) {
				this.minecraft.gui.setScreen(this.parent);
			}
		}).bounds(cx - 155, by, 100, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("Reset Defaults"), b -> {
			NeoThaumConfig.current = new NeoThaumConfig.ConfigData();
			NeoThaumConfig.save();
			this.init();
		}).bounds(cx - 50, by, 100, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> {
			NeoThaumConfig.load();
			if (this.minecraft != null) {
				this.minecraft.gui.setScreen(this.parent);
			}
		}).bounds(cx + 55, by, 100, 20).build());
	}

	private static int nextVal(int[] steps, int cur) {
		for (int i = 0; i < steps.length; i++) {
			if (steps[i] == cur) {
				return steps[(i + 1) % steps.length];
			}
		}
		return steps[0];
	}

	private static float nextVal(float[] steps, float cur) {
		for (int i = 0; i < steps.length; i++) {
			if (Math.abs(steps[i] - cur) < 0.05f) {
				return steps[(i + 1) % steps.length];
			}
		}
		return steps[0];
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float pt) {
		super.extractRenderState(g, mx, my, pt);
		g.text(this.font, this.title, this.width / 2 - this.font.width(this.title) / 2, 14, 0xFFFFFFFF, true);
	}

	@Override
	public void onClose() {
		NeoThaumConfig.load();
		if (this.minecraft != null) {
			this.minecraft.gui.setScreen(this.parent);
		}
	}
}
