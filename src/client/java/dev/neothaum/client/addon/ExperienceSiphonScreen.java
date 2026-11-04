package dev.neothaum.client.addon;

import dev.neothaum.NeoThaum;
import dev.neothaum.addon.ExperienceSiphon;
import dev.neothaum.addon.ExperienceSiphonMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

/**
 * The Experience Siphon's screen, drawn as TC4's spa screen: empty jars in above, full jars out below, a groove between them that fills as a
 * jar does, and the spa's glass gauge showing the store. Both glow and shimmer as experience orbs do.
 */
public class ExperienceSiphonScreen extends AbstractContainerScreen<ExperienceSiphonMenu> {
	private static final Identifier TEX = NeoThaum.id("textures/gui/gui_experience_siphon.png");

	public ExperienceSiphonScreen(ExperienceSiphonMenu menu, Inventory inv, Component title) {
		super(menu, inv, title, 176, 166);
	}

	/**
	 * The colour of experience: vanilla tints its orbs with red rising and falling, full green and a little blue, so they shimmer between green
	 * and yellow. {@code phase} sets one row or point apart from the next.
	 */
	public static int glow(float phase) {
		float t = (System.currentTimeMillis() % 100000L) / 100.0F + phase;
		int r = (int) ((Mth.sin(t) + 1.0F) * 0.5F * 255.0F);
		int b = (int) ((Mth.sin(t + (float) (Math.PI * 4.0 / 3.0)) + 1.0F) * 0.1F * 255.0F);
		return 0xFF000000 | r << 16 | 0xFF00 | b;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
		super.extractBackground(g, mouseX, mouseY, a);
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		g.blit(RenderPipelines.GUI_TEXTURED, TEX, k, l, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
		// the groove between the slots fills downward as the jar fills
		int fill = (int) (15 * this.menu.progress());
		for (int y = 0; y < fill; y++) {
			g.fill(RenderPipelines.GUI, k + 72, l + 32 + y, k + 76, l + 33 + y, glow(y * 0.35F));
		}
		// the store, in the spa's gauge
		int bar = (int) (48.0F * Math.min(1.0F, this.menu.store() / (float) ExperienceSiphon.STORE));
		int y1 = l + 15 + 48;
		for (int y = 0; y < bar; y++) {
			g.fill(RenderPipelines.GUI, k + 107, y1 - y - 1, k + 115, y1 - y, glow(y * 0.2F));
		}
		g.blit(RenderPipelines.GUI_TEXTURED, TEX, k + 106, l + 11, 232, 0, 10, 55, 256, 256);
	}

	@Override
	protected void extractTooltip(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		super.extractTooltip(g, mouseX, mouseY);
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		if (mouseX >= k + 104 && mouseX < k + 118 && mouseY >= l + 10 && mouseY < l + 66) {
			g.setTooltipForNextFrame(java.util.List.of(
				Component.translatable("block.neothaum.experience_siphon").getVisualOrderText(),
				Component.translatable("neothaum.experience.store", ExperienceSiphon.levels(this.menu.store()), ExperienceSiphon.levels(ExperienceSiphon.STORE))
					.withStyle(net.minecraft.ChatFormatting.GREEN).getVisualOrderText()), mouseX, mouseY);
		}
	}

	/** TC4's screens draw no labels. */
	@Override
	protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
	}
}
