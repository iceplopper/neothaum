package dev.neothaum.client.gui;

import dev.neothaum.NeoThaum;
import dev.neothaum.menu.HoverHarnessMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** GuiHoverHarness: 1 jar slot at (80, 32). */
public class HoverHarnessScreen extends AbstractContainerScreen<HoverHarnessMenu> {
	private static final Identifier TEX = NeoThaum.id("textures/gui/guihoverharness.png");

	public HoverHarnessScreen(HoverHarnessMenu menu, Inventory inv, Component title) {
		super(menu, inv, title, 176, 166);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
		super.extractBackground(g, mouseX, mouseY, a);
		int x = (this.width - this.imageWidth) / 2;
		int y = (this.height - this.imageHeight) / 2;
		g.blit(RenderPipelines.GUI_TEXTURED, TEX, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor g, int xm, int ym) {
		if (this.menu.lockedHotbarSlot() >= 0) {
			g.blit(RenderPipelines.GUI_TEXTURED, TEX, 8 + this.menu.lockedHotbarSlot() * 18, 142, 240, 0, 16, 16, 256, 256);
		}
	}
}
