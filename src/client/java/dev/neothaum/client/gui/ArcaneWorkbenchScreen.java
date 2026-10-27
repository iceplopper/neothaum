package dev.neothaum.client.gui;

import dev.neothaum.NeoThaum;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.items.WandItem;
import dev.neothaum.menu.ArcaneWorkbenchMenu;
import dev.neothaum.tiles.ArcaneWorktableBlockEntity;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Port of GuiArcaneWorkbench. */
public class ArcaneWorkbenchScreen extends AbstractContainerScreen<ArcaneWorkbenchMenu> {
	private static final Identifier TEX = NeoThaum.id("textures/gui/gui_arcaneworkbench.png");
	private static final int[][] ASPECT_LOCS = {{72, 21}, {24, 43}, {24, 102}, {72, 124}, {120, 102}, {120, 43}};

	public ArcaneWorkbenchScreen(ArcaneWorkbenchMenu menu, Inventory inv, Component title) {
		super(menu, inv, title, 190, 234);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
		super.extractBackground(g, mouseX, mouseY, a);
		int x = (this.width - this.imageWidth) / 2;
		int y = (this.height - this.imageHeight) / 2;
		g.blit(RenderPipelines.GUI_TEXTURED, TEX, x, y, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
		ItemStack wandStack = this.menu.tile.getItem(ArcaneWorktableBlockEntity.WAND_SLOT);
		boolean hasWand = wandStack.getItem() instanceof WandItem;
		AspectList cost = this.menu.cost.get(6) == 1 ? this.menu.getCost() : null;
		if (cost != null) {
			int count = 0;
			List<Aspect> primals = Aspect.getPrimalAspects();
			for (Aspect primal : primals) {
				float amt = cost.getAmount(primal);
				if (amt > 0) {
					float alpha = 0.5F + (Mth.sin((this.minecraft.player.tickCount + count * 10) / 2.0F) * 0.2F - 0.2F);
					if (hasWand) {
						amt *= WandItem.getConsumptionModifier(wandStack, this.minecraft.player, primal, true);
						if (amt * 100.0F <= WandItem.getVis(wandStack, primal)) {
							alpha = 1.0F;
						}
					}
					GuiUtil.drawTag(g, x + ASPECT_LOCS[count][0] - 8, y + ASPECT_LOCS[count][1] - 8, primal, amt, 0, 771, alpha, false);
				}
				if (++count > 5) {
					break;
				}
			}
			if (hasWand && !WandItem.consumeAllVisCrafting(wandStack, this.minecraft.player, cost, false)) {
				g.pose().pushMatrix();
				g.pose().translate(x + 168, y + 46);
				g.pose().scale(0.5F, 0.5F);
				String text = "Insufficient vis";
				int ll = this.font.width(text) / 2;
				g.text(this.font, text, -ll, 0, 0xFFEE6E6E, false);
				g.pose().popMatrix();
			}
		}
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor g, int xm, int ym) {
		// GuiArcaneWorkbench draws no labels
	}
}
