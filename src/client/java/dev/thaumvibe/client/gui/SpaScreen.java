package dev.thaumvibe.client.gui;

import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.menu.SpaMenu;
import dev.thaumvibe.registry.TVSounds;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class SpaScreen extends AbstractContainerScreen<SpaMenu> {
	private static final Identifier TEX = Thaumvibe.id("textures/gui/gui_spa.png");

	public SpaScreen(SpaMenu menu, Inventory inv, Component title) {
		super(menu, inv, title, 176, 166);
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

		boolean mix = this.menu.data.get(1) != 0;
		int v = mix ? 16 : 32;
		this.blit(g, k + 89, l + 35, 208, v, 8, 8);

		int fluidAmount = this.menu.data.get(0);
		int fluidType = this.menu.data.get(2);
		if (fluidAmount > 0) {
			float bar = Math.min(1.0f, (float) fluidAmount / 5000.0f);
			int barH = (int) (48.0f * bar);
			int color = fluidType == 1 ? 0xFF88DDEE : 0xFF3F76E4;
			int x0 = k + 107;
			int y1 = l + 15 + 48;
			int y0 = y1 - barH;
			g.fill(RenderPipelines.GUI, x0, y0, x0 + 8, y1, color);
		}

		this.blit(g, k + 106, l + 11, 232, 0, 10, 55);
	}

	@Override
	protected void extractTooltip(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		super.extractTooltip(g, mouseX, mouseY);
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;

		if (mouseX >= k + 104 && mouseX < k + 118 && mouseY >= l + 10 && mouseY < l + 66) {
			int fluidAmount = this.menu.data.get(0);
			int fluidType = this.menu.data.get(2);
			String name = fluidAmount == 0 ? "Empty" : (fluidType == 1 ? "Purifying Fluid" : "Water");
			g.setTooltipForNextFrame(java.util.List.of(
				Component.literal(name).getVisualOrderText(),
				Component.literal(fluidAmount + " / 5000 mB").getVisualOrderText()
			), mouseX, mouseY);
		}

		if (mouseX >= k + 88 && mouseX < k + 98 && mouseY >= l + 34 && mouseY < l + 44) {
			boolean mix = this.menu.data.get(1) != 0;
			g.setTooltipForNextFrame(this.font, Component.translatable(mix ? "text.spa.mix.true" : "text.spa.mix.false"), mouseX, mouseY);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		if (mouseX >= k + 89 && mouseX < k + 97 && mouseY >= l + 35 && mouseY < l + 43) {
			if (this.minecraft != null && this.minecraft.gameMode != null) {
				this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1);
				if (this.minecraft.player != null) {
					this.minecraft.player.playSound(TVSounds.CAMERACLACK, 0.4F, 1.0F);
				}
				return true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}
}
