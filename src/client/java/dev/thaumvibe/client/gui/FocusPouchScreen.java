package dev.thaumvibe.client.gui;

import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.menu.FocusPouchMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** GuiFocusPouch: the pouch's eighteen slots, with the pouch's own hotbar slot shaded as locked. */
public class FocusPouchScreen extends AbstractContainerScreen<FocusPouchMenu> {
	private static final Identifier TEX = Thaumvibe.id("textures/gui/gui_focuspouch.png");

	public FocusPouchScreen(FocusPouchMenu menu, Inventory inv, Component title) {
		super(menu, inv, title, 175, 232);
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
		// GuiFocusPouch draws no labels, just the lock over the pouch's own slot
		g.blit(RenderPipelines.GUI_TEXTURED, TEX, 8 + this.menu.lockedHotbarSlot() * 18, 209, 240, 0, 16, 16, 256, 256);
	}
}
