package dev.neothaum.client.addon;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.config.NeoThaumConfig;
import dev.neothaum.registry.TVComponents;
import dev.neothaum.registry.TVItems;
import dev.neothaum.research.PlayerKnowledge;
import dev.neothaum.research.ResearchManager;
import dev.neothaum.tiles.TubeBlockEntity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** NeoThaum Additions drawn on the HUD: the essentia network readout (goggled fortress helm) and the warp readout. */
public final class AdditionsHud {
	public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null || mc.gui.hud.isHidden()) {
			return;
		}
		if (dev.neothaum.addon.Additions.enabled("essentia_overlay")) {
			essentia(g, mc);
		}
		if (dev.neothaum.addon.Additions.enabled("warp_hud")) {
			warp(g, mc);
		}
	}

	/**
	 * Essentia overlay: wearing a fortress helm fitted with goggles and looking at an essentia tube shows, beside the crosshair, what it holds,
	 * what it pulls and how hard, and which of its sides are open, the side it pulls hardest from marked.
	 */
	private static void essentia(GuiGraphicsExtractor g, Minecraft mc) {
		var helm = mc.player.getItemBySlot(EquipmentSlot.HEAD);
		if (!helm.is(TVItems.THAUMIUM_FORTRESS_HELM) || !Boolean.TRUE.equals(helm.get(TVComponents.GOGGLES))) {
			return;
		}
		if (!(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK
			|| !(mc.level.getBlockEntity(hit.getBlockPos()) instanceof TubeBlockEntity tube)) {
			return;
		}
		int x = g.guiWidth() / 2 + 10;
		int y = g.guiHeight() / 2 - 4;
		Aspect held = tube.getEssentiaType(null);
		int amount = tube.getEssentiaAmount(null);
		line(g, mc, x, y, held == null || amount <= 0 ? "Empty" : amount + " " + held.getName(), held == null ? 0xFFAAAAAA : 0xFF000000 | held.getColor());
		Aspect pull = tube.getSuctionType(null);
		int suction = tube.getSuctionAmount(null);
		line(g, mc, x, y + 10, "Suction " + suction + (pull == null ? "" : " for " + pull.getName()), 0xFFFFFFFF);
		StringBuilder sides = new StringBuilder("Open:");
		for (Direction d : Direction.values()) {
			if (tube.openSides[d.ordinal()]) {
				sides.append(' ').append(d.getName().charAt(0));
			}
		}
		if (tube.aspectFilter != null) {
			sides.append("  Filter ").append(tube.aspectFilter.getName());
		}
		line(g, mc, x, y + 20, sides.toString(), 0xFFC0C0FF);
	}

	/** Warp readout: the three kinds of warp, left of the hotbar. */
	private static void warp(GuiGraphicsExtractor g, Minecraft mc) {
		PlayerKnowledge k = ResearchManager.knowledge(mc.player);
		int total = k.warp + k.warpSticky + k.warpTemp;
		if (total <= 0) {
			return;
		}
		String text = "Warp " + total + " (" + k.warp + "/" + k.warpSticky + "/" + k.warpTemp + ")";
		line(g, mc, g.guiWidth() / 2 - 97 - mc.font.width(text), g.guiHeight() - 12, text, 0xFFB050E0);
	}

	private static void line(GuiGraphicsExtractor g, Minecraft mc, int x, int y, String text, int color) {
		g.text(mc.font, text, x, y, color, true);
	}

	private AdditionsHud() {
	}
}
