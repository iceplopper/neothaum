package dev.thaumvibe.client.gui;

import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.menu.BaublesMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Baubles' expanded inventory: the vanilla inventory background with the crafting grid replaced by the
 * amulet / ring / ring / belt column to the right of the player portrait.
 */
public class BaublesScreen extends AbstractContainerScreen<BaublesMenu> {
	private static final Identifier[] SHADOW_ICONS = {
		Thaumvibe.id("textures/items/bauble_amulet.png"),
		Thaumvibe.id("textures/items/bauble_ring.png"),
		Thaumvibe.id("textures/items/bauble_ring.png"),
		Thaumvibe.id("textures/items/bauble_belt.png")
	};

	private int mx;
	private int my;

	public BaublesScreen(BaublesMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
	}

	@Override
	public void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
		this.mx = mouseX;
		this.my = mouseY;
		super.extractContents(g, mouseX, mouseY, a);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
		super.extractBackground(g, mouseX, mouseY, a);
		int x = this.leftPos;
		int y = this.topPos;
		g.blit(RenderPipelines.GUI_TEXTURED, INVENTORY_LOCATION, x, y, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

		// Cover the 2x2 crafting area and result arrow with the clean panel color
		g.fill(x + 95, y + 7, x + 170, y + 79, 0xFFC6C6C6);

		// Bauble slot frames (vanilla slot look) at the column used by BaublesMenu
		for (int i = 0; i < 4; i++) {
			int sx = x + 76;
			int sy = y + 7 + i * 18;
			g.fill(sx, sy, sx + 18, sy + 18, 0xFF8B8B8B);
			g.fill(sx, sy, sx + 17, sy + 1, 0xFF373737);
			g.fill(sx, sy, sx + 1, sy + 17, 0xFF373737);
			g.fill(sx + 1, sy + 17, sx + 18, sy + 18, 0xFFFFFFFF);
			g.fill(sx + 17, sy + 1, sx + 18, sy + 18, 0xFFFFFFFF);

			// Render shadowed silhouette icon when slot is empty
			if (this.menu.baubles.getItem(i).isEmpty()) {
				GuiUtil.blitFull(g, RenderPipelines.GUI_TEXTURED, SHADOW_ICONS[i], sx + 1, sy + 1, 16, 16, 0x60FFFFFF);
			}
		}
		InventoryScreen.extractEntityInInventoryFollowsMouse(g, x + 26, y + 8, x + 75, y + 78, 30, 0.0625F, this.mx, this.my, this.minecraft.player);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor g, int xm, int ym) {
		g.text(this.font, Component.translatable("container.baubles"), 97, 8, 0xFF404040, false);
	}
}
