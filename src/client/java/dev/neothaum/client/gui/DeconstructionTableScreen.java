package dev.neothaum.client.gui;

import dev.neothaum.NeoThaum;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.menu.DeconstructionTableMenu;
import dev.neothaum.registry.TVSounds;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class DeconstructionTableScreen extends AbstractContainerScreen<DeconstructionTableMenu> {
	private static final Identifier TEX = NeoThaum.id("textures/gui/gui_decontable.png");

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

	/** Where each primal's waiting points sit: in its own well, two rows of three below the input (gui_decontable.png). */
	private static int[] spot(int i) {
		return new int[]{60 + i % 3 * 19, 33 + i / 3 * 19};
	}

	/** The primal under the mouse with points waiting, or -1. */
	private int hovered(double mouseX, double mouseY) {
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		for (int i = 0; i < PRIMALS.length; i++) {
			int[] at = spot(i);
			if (this.menu.data.get(1 + i) > 0 && mouseX >= k + at[0] && mouseX < k + at[0] + 16 && mouseY >= l + at[1] && mouseY < l + at[1] + 16) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
		super.extractBackground(g, mouseX, mouseY, a);
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;

		this.blit(g, k, l, 0, 0, this.imageWidth, this.imageHeight);

		// the time left to break the item, emptying along the groove beside the input, in TC4's blue
		int breaktime = this.menu.data.get(0);
		if (breaktime > 0) {
			int w = breaktime * 39 / 40;
			g.fill(RenderPipelines.GUI, k + 85, l + 15, k + 85 + w, l + 20, 0xFF4C4EC6);
			g.fill(RenderPipelines.GUI, k + 85, l + 15, k + 85 + w, l + 16, 0xFF8C8EE6);
		}

		for (int i = 0; i < PRIMALS.length; i++) {
			int n = this.menu.data.get(1 + i);
			if (n > 0) {
				int[] at = spot(i);
				GuiUtil.drawTag(g, k + at[0], l + at[1], PRIMALS[i], n, 0, 771, 1.0F, false);
			}
		}
	}

	@Override
	protected void extractTooltip(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		super.extractTooltip(g, mouseX, mouseY);
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;

		int i = this.hovered(mouseX, mouseY);
		if (i >= 0) {
			Aspect aspect = PRIMALS[i];
			g.setComponentTooltipForNextFrame(this.font, java.util.List.of(Component.literal(aspect.getName()),
				aspect.getLocalizedDescription().copy().withStyle(net.minecraft.ChatFormatting.GRAY),
				Component.translatable("neothaum.decontable.take").withStyle(net.minecraft.ChatFormatting.DARK_GRAY)), mouseX, mouseY);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;

		int i = this.hovered(mouseX, mouseY);
		if (i >= 0) {
			if (this.minecraft != null && this.minecraft.gameMode != null) {
				this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, event.hasShiftDown() ? 0 : 1 + i);
				if (this.minecraft.player != null) {
					this.minecraft.player.playSound(TVSounds.HHOFF, 0.2F, 1.0F);
				}
				return true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}
	/** GuiDeconstructionTable.drawGuiContainerForegroundLayer draws nothing: the board has no room for the usual labels. */
	@Override
	protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
	}
}
