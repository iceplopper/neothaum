package dev.thaumvibe.client.gui;

import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.menu.DeconstructionTableMenu;
import dev.thaumvibe.registry.TVSounds;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class DeconstructionTableScreen extends AbstractContainerScreen<DeconstructionTableMenu> {
	private static final Identifier TEX = Thaumvibe.id("textures/gui/gui_decontable.png");

	private static final Aspect[] PRIMALS = {
		Aspect.AIR, Aspect.FIRE, Aspect.WATER, Aspect.EARTH, Aspect.ORDER, Aspect.ENTROPY
	};

	public DeconstructionTableScreen(DeconstructionTableMenu menu, Inventory inv, Component title) {
		super(menu, inv, title, 176, 166);
	}

	private void blit(GuiGraphicsExtractor g, int x, int y, int u, int v, int w, int h) {
		if (w > 0 && h > 0) {
			g.blit(RenderPipelines.GUI_TEXTURED, TEX, x, y, u, v, w, h, 256, 256);
		}
	}

	private Aspect currentAspect() {
		int idx = this.menu.data.get(1);
		if (idx >= 0 && idx < PRIMALS.length) {
			return PRIMALS[idx];
		}
		return null;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
		super.extractBackground(g, mouseX, mouseY, a);
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;

		this.blit(g, k, l, 0, 0, this.imageWidth, this.imageHeight);

		int breaktime = this.menu.data.get(0);
		if (breaktime > 0) {
			int i1 = breaktime * 46 / 40;
			this.blit(g, k + 93, l + 15 + 46 - i1, 176, 46 - i1, 9, i1);
		}

		Aspect aspect = currentAspect();
		if (aspect != null) {
			GuiUtil.drawTag(g, k + 64, l + 48, aspect);
		}
	}

	@Override
	protected void extractTooltip(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		super.extractTooltip(g, mouseX, mouseY);
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;

		Aspect aspect = currentAspect();
		if (aspect != null && mouseX >= k + 64 && mouseX < k + 80 && mouseY >= l + 48 && mouseY < l + 64) {
			g.setTooltipForNextFrame(this.font, Component.literal(aspect.getName() + " - " + aspect.getLocalizedDescription()), mouseX, mouseY);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;

		Aspect aspect = currentAspect();
		if (aspect != null && mouseX >= k + 64 && mouseX < k + 80 && mouseY >= l + 48 && mouseY < l + 64) {
			if (this.minecraft != null && this.minecraft.gameMode != null) {
				this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1);
				if (this.minecraft.player != null) {
					this.minecraft.player.playSound(TVSounds.HHOFF, 0.2F, 1.0F);
				}
				return true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}
}
