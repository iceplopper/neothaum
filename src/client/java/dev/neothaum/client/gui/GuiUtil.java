package dev.neothaum.client.gui;

import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dev.neothaum.NeoThaum;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.client.render.TVRenderTypes;
import java.text.DecimalFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

/** GUI-side ports of UtilsFX (drawTag, drawTexturedQuad...). */
public final class GuiUtil {
	public static final RenderPipeline GUI_ADDITIVE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
		.withLocation(NeoThaum.id("pipeline/gui_additive"))
		.withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
		.build());
	private static final DecimalFormat FORMAT = new DecimalFormat("#######.##");

	public static RenderPipeline pipeline(int blend) {
		return blend == 1 ? GUI_ADDITIVE : RenderPipelines.GUI_TEXTURED;
	}

	public static int color(int rgb, float alpha) {
		return ARGB.color(Math.round(Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F), rgb & 0xFFFFFF);
	}

	/** Blits the whole of a texture into a w x h box. */
	public static void blitFull(GuiGraphicsExtractor g, RenderPipeline p, Identifier tex, float x, float y, float w, float h, int argb) {
		g.pose().pushMatrix();
		g.pose().translate(x, y);
		g.pose().scale(w / 16.0F, h / 16.0F);
		g.blit(p, tex, 0, 0, 0.0F, 0.0F, 16, 16, 16, 16, 16, 16, argb);
		g.pose().popMatrix();
	}

	public static void drawTag(GuiGraphicsExtractor g, double x, double y, Aspect aspect) {
		drawTag(g, x, y, aspect, 0.0F, 0, 771, 1.0F, false);
	}

	public static void drawTag(GuiGraphicsExtractor g, double x, double y, Aspect aspect, float amount, int bonus, int blend, float alpha, boolean bw) {
		if (aspect == null) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		Font font = mc.font;
		int argb = bw ? color(0x1A1A1A, alpha * 0.8F) : color(aspect.getColor(), alpha);
		blitFull(g, pipeline(blend), aspect.getImage(), (float) x, (float) y, 16, 16, argb);
		if (amount > 0.0F) {
			g.pose().pushMatrix();
			g.pose().scale(0.5F, 0.5F);
			String am = FORMAT.format(amount);
			int sw = font.width(am);
			int bx = 32 - sw + (int) x * 2;
			int by = 32 - font.lineHeight + (int) y * 2;
			if (blend > 1) {
				for (int a = -1; a <= 1; a++) {
					for (int b = -1; b <= 1; b++) {
						if ((a == 0 || b == 0) && (a != 0 || b != 0)) {
							g.text(font, am, bx + a, by + b, 0xFF000000, false);
						}
					}
				}
			}
			g.text(font, am, bx, by, 0xFFFFFFFF, false);
			g.pose().popMatrix();
		}
		if (bonus > 0) {
			int px = 16 * (mc.player == null ? 0 : mc.player.tickCount % 16);
			g.blit(RenderPipelines.GUI_TEXTURED, TVRenderTypes.PARTICLES, (int) x - 4, (int) y - 4, px, 80, 16, 16, 256, 256);
			if (bonus > 1) {
				g.pose().pushMatrix();
				g.pose().scale(0.5F, 0.5F);
				String am = "" + bonus;
				int sw = font.width(am) / 2;
				g.text(font, am, 8 - sw + (int) x * 2, 15 - font.lineHeight + (int) y * 2, 0xFFFFFFFF, true);
				g.pose().popMatrix();
			}
		}
	}

	private GuiUtil() {
	}
}
