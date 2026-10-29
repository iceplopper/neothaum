package dev.neothaum.client.gui;

import dev.neothaum.config.NeoThaumConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class NeoThaumConfigScreen extends Screen {
	private final Screen parent;
	private int activeTab = 0; // 0: Nodes, 1: Biomes & Structures, 2: Taint & Flux, 3: Alchemy & Debug
	private int col1;
	private int col2;
	private int slot;

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
		String[] tabs = {"Nodes", "World", "Taint & Flux", "Alchemy & Debug"};
		for (int i = 0; i < tabs.length; i++) {
			final int t = i;
			int tx = cx - (tabs.length * tabWidth + (tabs.length - 1) * 4) / 2 + i * (tabWidth + 4);
			this.addRenderableWidget(Button.builder(Component.literal((i == this.activeTab ? "§6§l" : "§7") + tabs[i]), b -> {
				this.activeTab = t;
				this.init();
			}).bounds(tx, tabY, tabWidth, 20).build());
		}

		var cfg = NeoThaumConfig.current;
		this.col1 = cx - 180;
		this.col2 = cx + 5;
		this.slot = 0;

		if (this.activeTab == 0) {
			this.slider("Nodes", () -> cfg.nodeMultiplier, v -> cfg.nodeMultiplier = v);
			this.slider("Special Nodes", () -> cfg.nodeVariantMultiplier, v -> cfg.nodeVariantMultiplier = v);
			this.slider("Sinister Nodes", () -> cfg.darkNodeMultiplier, v -> cfg.darkNodeMultiplier = v);
			this.slider("Unstable Nodes", () -> cfg.unstableNodeMultiplier, v -> cfg.unstableNodeMultiplier = v);
			this.slider("Pure Nodes", () -> cfg.pureNodeMultiplier, v -> cfg.pureNodeMultiplier = v);
			this.slider("Hungry Nodes", () -> cfg.hungryNodeMultiplier, v -> cfg.hungryNodeMultiplier = v);
			this.slider("Tainted Nodes", () -> cfg.taintedNodeMultiplier, v -> cfg.taintedNodeMultiplier = v);
			this.slider("Bright Nodes", () -> cfg.brightNodeMultiplier, v -> cfg.brightNodeMultiplier = v);
			this.slider("Pale Nodes", () -> cfg.paleNodeMultiplier, v -> cfg.paleNodeMultiplier = v);
			this.slider("Fading Nodes", () -> cfg.fadingNodeMultiplier, v -> cfg.fadingNodeMultiplier = v);
			this.toggle("Hard Nodes", () -> cfg.hardNodes, v -> cfg.hardNodes = v);
		} else if (this.activeTab == 1) {
			this.toggle("Magical Forest", () -> cfg.magicalForestGenerate, v -> cfg.magicalForestGenerate = v);
			this.slider("Magical Forest Size", () -> cfg.magicalForestSizeMultiplier, v -> cfg.magicalForestSizeMultiplier = v);
			this.toggle("Tainted Land", () -> cfg.taintedLandGenerate, v -> cfg.taintedLandGenerate = v);
			this.slider("Tainted Land Size", () -> cfg.taintedLandSizeMultiplier, v -> cfg.taintedLandSizeMultiplier = v);
			this.slider("Eldritch Rings", () -> cfg.eldritchRingMultiplier, v -> cfg.eldritchRingMultiplier = v);
			this.slider("Barrow Mounds", () -> cfg.barrowMoundMultiplier, v -> cfg.barrowMoundMultiplier = v);
			this.slider("Hilltop Stones", () -> cfg.hilltopStonesMultiplier, v -> cfg.hilltopStonesMultiplier = v);
			this.slider("Obsidian Totems", () -> cfg.obsidianTotemMultiplier, v -> cfg.obsidianTotemMultiplier = v);
			this.toggle("Village Buildings", () -> cfg.villageBuildings, v -> cfg.villageBuildings = v);
		} else if (this.activeTab == 2) {
			this.toggle("Taint Spread", () -> cfg.taintSpread, v -> cfg.taintSpread = v);
			this.slider("Taint Spread Speed", () -> cfg.taintSpreadMultiplier, v -> cfg.taintSpreadMultiplier = v);
			this.toggle("Taint Corrupts Dying Mobs", () -> cfg.corruptExistingMobs, v -> cfg.corruptExistingMobs = v);
			this.slider("Flux", () -> cfg.fluxMultiplier, v -> cfg.fluxMultiplier = v);
			this.toggle("Warp", () -> cfg.allowWarp, v -> cfg.allowWarp = v);
			this.slider("Wisp Spawns", () -> cfg.wispSpawnMultiplier, v -> cfg.wispSpawnMultiplier = v);
		} else {
			this.slider("Infernal Furnace Speed", () -> cfg.infernalFurnaceSpeedMultiplier, v -> cfg.infernalFurnaceSpeedMultiplier = v);
			this.slider("Infernal Furnace Bonus", () -> cfg.infernalFurnaceBonusMultiplier, v -> cfg.infernalFurnaceBonusMultiplier = v);
			this.slider("Aspect Orb Drops", () -> cfg.aspectOrbMultiplier, v -> cfg.aspectOrbMultiplier = v);
			this.toggle("Creative Dev Tab", () -> cfg.showCreativeDebugTab, v -> cfg.showCreativeDebugTab = v);
			this.toggle("Champion Mobs", () -> cfg.championMobs, v -> cfg.championMobs = v);
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

	/** The next place in the two-column layout. */
	private int[] place() {
		int x = this.slot % 2 == 0 ? this.col1 : this.col2;
		int y = 62 + this.slot / 2 * 24;
		this.slot++;
		return new int[]{x, y};
	}

	private void toggle(String label, BooleanSupplier get, Consumer<Boolean> set) {
		int[] at = this.place();
		this.addRenderableWidget(Button.builder(Component.literal(label + ": " + (get.getAsBoolean() ? "ON" : "OFF")), b -> {
			set.accept(!get.getAsBoolean());
			b.setMessage(Component.literal(label + ": " + (get.getAsBoolean() ? "ON" : "OFF")));
		}).bounds(at[0], at[1], 175, 20).build());
	}

	/** A multiplier on TC4's own setting: 0 to 10 in steps of 0.1, where 1 is TC4 and 0 is off. */
	private void slider(String label, Supplier<Float> get, Consumer<Float> set) {
		int[] at = this.place();
		this.addRenderableWidget(new MultiplierSlider(at[0], at[1], label, get.get(), set));
	}

	private static final class MultiplierSlider extends AbstractSliderButton {
		private final String label;
		private final Consumer<Float> set;

		MultiplierSlider(int x, int y, String label, float initial, Consumer<Float> set) {
			super(x, y, 175, 20, Component.empty(), Mth.clamp(initial, 0.0F, 10.0F) / 10.0);
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
