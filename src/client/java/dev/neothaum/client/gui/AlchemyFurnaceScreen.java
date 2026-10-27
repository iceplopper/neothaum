package dev.neothaum.client.gui;

import dev.neothaum.NeoThaum;
import dev.neothaum.menu.AlchemyFurnaceMenu;
import dev.neothaum.tiles.AlchemyFurnaceBlockEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Port of GuiAlchemyFurnace: flame, cook progress and the essentia gauge. */
public class AlchemyFurnaceScreen extends AbstractContainerScreen<AlchemyFurnaceMenu> {
	private static final Identifier TEX = NeoThaum.id("textures/gui/gui_alchemyfurnace.png");

	public AlchemyFurnaceScreen(AlchemyFurnaceMenu menu, Inventory inv, Component title) {
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
		if (this.menu.burnTime() > 0) {
			int i1 = this.menu.burnTime() * 20 / this.menu.itemBurnTime();
			this.blit(g, k + 80, l + 26 + 20 - i1, 176, 20 - i1, 16, i1);
		}
		int i1 = this.menu.cookTime() * 46 / this.menu.smeltTime();
		this.blit(g, k + 106, l + 13 + 46 - i1, 216, 46 - i1, 9, i1);
		i1 = this.menu.vis() * 48 / AlchemyFurnaceBlockEntity.MAX_VIS;
		this.blit(g, k + 61, l + 12 + 48 - i1, 200, 48 - i1, 8, i1);
		this.blit(g, k + 60, l + 8, 232, 0, 10, 55);
	}

	/** The essentia gauge reads out the furnace's vis against its capacity (TileAlchemyFurnace.vis / maxVis). */
	@Override
	protected void extractTooltip(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		super.extractTooltip(g, mouseX, mouseY);
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		if (mouseX >= k + 60 && mouseX < k + 70 && mouseY >= l + 8 && mouseY < l + 63) {
			var mc = net.minecraft.client.Minecraft.getInstance();
			// holding shift shows what the furnace holds, as shift-hovering an item shows its aspects
			if (mc.hasShiftDown() && this.menu.pos != null && mc.level != null && mc.player != null
				&& mc.level.getBlockEntity(this.menu.pos) instanceof AlchemyFurnaceBlockEntity f && f.aspects.size() > 0) {
				dev.neothaum.client.hud.AspectOverlays.drawAspectRow(g, f.aspects, mouseX + 17 - 8, mouseY + 7 - 33 - 8,
					dev.neothaum.research.ResearchManager.knowledge(mc.player));
			} else {
				g.setTooltipForNextFrame(Component.literal(this.menu.vis() + " / " + AlchemyFurnaceBlockEntity.MAX_VIS), mouseX, mouseY);
			}
		}
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor g, int xm, int ym) {
		// GuiAlchemyFurnace draws no labels
	}
}
