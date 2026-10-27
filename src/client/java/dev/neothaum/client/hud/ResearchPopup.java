package dev.neothaum.client.hud;

import dev.neothaum.client.gui.GuiUtil;
import dev.neothaum.client.gui.ThaumonomiconScreen;
import dev.neothaum.research.ResearchItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

/** Port of GuiResearchPopup: the "Research Completed!" banner that drops in at the top left. */
public final class ResearchPopup {
	private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("toast/advancement");
	private static final List<ResearchItem> RESEARCH = new ArrayList<>();
	private static long researchTime;

	public static void queue(ResearchItem research) {
		if (researchTime == 0L) {
			researchTime = Util.getMillis();
		}
		RESEARCH.add(research);
		ThaumonomiconScreen.lastX = research.displayColumn;
		ThaumonomiconScreen.lastY = research.displayRow;
	}

	public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
		if (RESEARCH.isEmpty() || researchTime == 0L) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		double var1 = (Util.getMillis() - researchTime) / 3000.0;
		if (var1 < 0.0 || var1 > 1.0) {
			RESEARCH.remove(0);
			researchTime = RESEARCH.isEmpty() ? 0L : Util.getMillis();
			return;
		}
		double var3 = var1 * 2.0;
		if (var3 > 1.0) {
			var3 = 2.0 - var3;
		}
		var3 *= 4.0;
		var3 = 1.0 - var3;
		if (var3 < 0.0) {
			var3 = 0.0;
		}
		var3 *= var3;
		var3 *= var3;
		int x = 0;
		int y = -(int) (var3 * 36.0);
		ResearchItem r = RESEARCH.get(0);
		g.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y, 160, 32);
		g.text(mc.font, "Research Completed!", x + 30, y + 7, 0xFFFFFF00, false);
		String name = r.getName().getString();
		int offset = mc.font.width(name);
		if (offset <= 125) {
			g.text(mc.font, name, x + 30, y + 18, 0xFFFFFFFF, false);
		} else {
			float vv = 125.0F / offset;
			g.pose().pushMatrix();
			g.pose().translate(x + 30, y + 16 + 2.0F / vv);
			g.pose().scale(vv, vv);
			g.text(mc.font, name, 0, 0, 0xFFFFFFFF, false);
			g.pose().popMatrix();
		}
		if (r.iconItem != null) {
			g.item(r.iconItem, x + 8, y + 8);
		} else if (r.iconResource != null) {
			GuiUtil.blitFull(g, RenderPipelines.GUI_TEXTURED, r.iconResource, x + 8, y + 8, 16, 16, 0xFFFFFFFF);
		}
	}

	private ResearchPopup() {
	}
}
