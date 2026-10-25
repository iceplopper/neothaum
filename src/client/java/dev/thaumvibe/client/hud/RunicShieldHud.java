package dev.thaumvibe.client.hud;

import dev.thaumvibe.client.render.TVRenderTypes;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.Mth;

/** ClientTickEventsFML.renderRunicArmorBar: a row of golden runes above the hotbar, one per tenth of the shield's strength. */
public final class RunicShieldHud {
	private static int charge;
	private static int max;

	public static void set(int c, int m) {
		charge = c;
		max = m;
	}

	public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
		Minecraft mc = Minecraft.getInstance();
		var player = mc.player;
		if (player == null || player.isCreative() || player.isSpectator() || charge <= 0 || max <= 0) {
			return;
		}
		int k = g.guiWidth();
		int l = g.guiHeight();
		float fill = (float) charge / max;
		int x0 = k / 2 - 91;
		int y0 = l - 39;
		for (int a = 0; a < fill * 10.0F; a++) {
			g.blit(RenderPipelines.GUI_TEXTURED, TVRenderTypes.PARTICLES, x0 + a * 8, y0, 160, 16, 9, 9, 256, 256);
			float alpha = Mth.sin(player.tickCount / 4.0F + a) * 0.4F + 0.6F;
			int argb = (int) (alpha * 255) << 24 | 255 << 16 | (int) (0.75F * 255) << 8 | (int) (0.24F * 255);
			g.pose().pushMatrix();
			g.pose().translate(x0, y0);
			g.pose().scale(0.5F, 0.5F);
			g.blit(RenderPipelines.GUI_TEXTURED, TVRenderTypes.PARTICLES, a * 16, 0, a * 16, 96, 16, 16, 256, 256, argb);
			g.pose().popMatrix();
		}
	}

	private RunicShieldHud() {
	}
}
