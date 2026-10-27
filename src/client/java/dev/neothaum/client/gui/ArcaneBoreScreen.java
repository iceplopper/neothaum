package dev.neothaum.client.gui;

import dev.neothaum.NeoThaum;
import dev.neothaum.menu.ArcaneBoreMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Port of TC4 GuiArcaneBore:
 * Displays slots for Excavation Focus and Pickaxe, along with calculated width, speed, and enchantment stats.
 */
public class ArcaneBoreScreen extends AbstractContainerScreen<ArcaneBoreMenu> {
	private static final Identifier TEX = NeoThaum.id("textures/gui/gui_arcanebore.png");

	public ArcaneBoreScreen(ArcaneBoreMenu menu, Inventory inv, Component title) {
		super(menu, inv, title, 176, 141);
	}

	private void blit(GuiGraphicsExtractor g, int x, int y, int u, int v, int w, int h) {
		if (w > 0 && h > 0) {
			g.blit(RenderPipelines.GUI_TEXTURED, TEX, x, y, u, v, w, h, 256, 256);
		}
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
		super.extractBackground(g, mouseX, mouseY, a);
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;

		this.blit(g, k, l, 0, 0, this.imageWidth, this.imageHeight);

		// Warning indicator for near-broken pickaxe
		if (this.menu.data.get(5) != 0) {
			this.blit(g, k + 74, l + 18, 184, 0, 16, 16);
		}

		// Stats text on right panel
		int speed = this.menu.data.get(0);
		int area = this.menu.data.get(1);
		int fortune = this.menu.data.get(2);
		boolean silk = this.menu.data.get(3) != 0;
		boolean clusters = this.menu.data.get(4) != 0;

		int tx = k + 104;
		int ty = l + 8;
		g.text(this.font, Component.literal("Width: " + (1 + (area + 2) * 2)), tx, ty, 0xFFFFFFFF, false);
		g.text(this.font, Component.literal("Speed: +" + speed), tx, ty + 10, 0xFFFFFFFF, false);
		g.text(this.font, Component.literal("Properties:"), tx, ty + 20, 0xFFAAAAAA, false);

		int line = 0;
		if (clusters) {
			g.text(this.font, Component.literal("Clusters"), tx + 4, ty + 30 + line * 9, 0xFFC0C0C0, false);
			line++;
		}
		if (fortune > 0) {
			g.text(this.font, Component.literal("Fortune " + fortune), tx + 4, ty + 30 + line * 9, 0xFFEECC66, false);
			line++;
		}
		if (silk) {
			g.text(this.font, Component.literal("Silk Touch"), tx + 4, ty + 30 + line * 9, 0xFF88AAFF, false);
		}
	}
}
