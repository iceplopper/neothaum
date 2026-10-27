package dev.neothaum.client.gui;

import dev.neothaum.NeoThaum;
import dev.neothaum.menu.PechMenu;
import dev.neothaum.registry.TVSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** GuiPech: the offer slot, four payout slots, and the dice to roll once a valued offer is in. */
public class PechScreen extends AbstractContainerScreen<PechMenu> {
	private static final Identifier TEX = NeoThaum.id("textures/gui/gui_pech.png");

	public PechScreen(PechMenu menu, Inventory inv, Component title) {
		super(menu, inv, title, 175, 232);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
		super.extractBackground(g, mouseX, mouseY, a);
		int x = (this.width - this.imageWidth) / 2;
		int y = (this.height - this.imageHeight) / 2;
		g.blit(RenderPipelines.GUI_TEXTURED, TEX, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
		if (this.menu.canTrade()) {
			g.blit(RenderPipelines.GUI_TEXTURED, TEX, x + 67, y + 24, 176, 0, 25, 25, 256, 256);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		int x = (this.width - this.imageWidth) / 2;
		int y = (this.height - this.imageHeight) / 2;
		double dx = event.x() - (x + 67), dy = event.y() - (y + 24);
		if (dx >= 0 && dy >= 0 && dx < 25 && dy < 25 && this.menu.canTrade()) {
			Minecraft mc = Minecraft.getInstance();
			mc.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
			if (mc.player != null) {
				mc.player.playSound(TVSounds.PECH_DICE, 0.5F, 0.95F + mc.player.getRandom().nextFloat() * 0.1F);
			}
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor g, int xm, int ym) {
		// GuiPech draws no labels
	}
}
