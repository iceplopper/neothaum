package dev.neothaum.client.hud;

import dev.neothaum.NeoThaum;
import dev.neothaum.registry.TVItems;
import dev.neothaum.research.PlayerKnowledge;
import dev.neothaum.research.ResearchManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

/** ClientTickEventsFML.renderSanityHud: while the sanity checker is held, a gauge in the corner shows temporary, sticky and permanent warp. */
public final class SanityHud {
	private static final Identifier HUD = NeoThaum.id("textures/gui/hud.png");

	public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
		Player player = Minecraft.getInstance().player;
		if (player == null || !player.getMainHandItem().is(TVItems.SANITY_CHECKER)) {
			return;
		}
		PlayerKnowledge k = ResearchManager.knowledge(player);
		g.blit(RenderPipelines.GUI_TEXTURED, HUD, 1, 1, 152, 0, 20, 76, 256, 256);
		float tw = k.warp + k.warpSticky + k.warpTemp;
		int p = k.warp, s = k.warpSticky, t = k.warpTemp;
		float mod = 1.0F;
		if (tw > 100.0F) {
			mod = 100.0F / tw;
			tw = 100.0F;
		}
		int gap = (int) ((100.0F - tw) / 100.0F * 48.0F);
		int wt = (int) (t / 100.0F * 48.0F * mod);
		int ws = (int) (s / 100.0F * 48.0F * mod);
		// UtilsFX.drawTexturedQuad(x, y, u, v, width, height), tinted
		if (t > 0) {
			g.blit(RenderPipelines.GUI_TEXTURED, HUD, 7, 21 + gap, 200, gap, 8, wt + gap, 256, 256, 0xFFFF80FF);
		}
		if (s > 0) {
			g.blit(RenderPipelines.GUI_TEXTURED, HUD, 7, 21 + wt + gap, 200, wt + gap, 8, wt + ws + gap, 256, 256, 0xFFBF00BF);
		}
		if (p > 0) {
			g.blit(RenderPipelines.GUI_TEXTURED, HUD, 7, 21 + wt + ws + gap, 200, wt + ws + gap, 8, 48, 256, 256, 0xFF800080);
		}
		g.blit(RenderPipelines.GUI_TEXTURED, HUD, 1, 1, 176, 0, 20, 76, 256, 256);
		if (tw >= 100.0F) {
			g.blit(RenderPipelines.GUI_TEXTURED, HUD, 1, 1, 216, 0, 20, 16, 256, 256);
		}
	}

	private SanityHud() {
	}
}
