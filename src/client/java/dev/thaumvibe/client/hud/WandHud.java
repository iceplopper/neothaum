package dev.thaumvibe.client.hud;

import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.client.gui.GuiUtil;
import dev.thaumvibe.items.WandItem;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** ClientTickEventsFML.renderCastingWandHud: the vis dial in the top-left corner while holding a wand. */
public final class WandHud {
	private static final Identifier HUD = Thaumvibe.id("textures/gui/hud.png");
	private static final Map<Integer, AspectList> OLD = new HashMap<>();
	private static long nextsync;
	private static final java.text.DecimalFormat FORMAT = new java.text.DecimalFormat("#######.##");
	private static final java.text.DecimalFormat FORMAT2 = new java.text.DecimalFormat("#######.#");

	public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if (player == null) {
			return;
		}
		ItemStack wandstack = player.getMainHandItem();
		if (!(wandstack.getItem() instanceof WandItem)) {
			return;
		}
		int slot = player.getInventory().getSelectedSlot();
		long time = System.currentTimeMillis();
		if (!OLD.containsKey(slot) || nextsync <= time) {
			OLD.put(slot, WandItem.getAllVis(wandstack));
			nextsync = time + 1000L;
		}
		AspectList old = OLD.get(slot);
		ItemStack focusStack = WandItem.getFocusStack(wandstack);
		dev.thaumvibe.api.wands.FocusItem focus = WandItem.getFocus(wandstack);
		g.pose().pushMatrix();
		g.pose().pushMatrix();
		g.pose().scale(0.5F, 0.5F);
		g.blit(RenderPipelines.GUI_TEXTURED, HUD, 0, 0, 0, 0, 64, 64, 256, 256);
		g.pose().popMatrix();
		g.pose().translate(16.0F, 16.0F);
		int max = WandItem.getMaxVis(wandstack);
		int count = 0;
		AspectList aspects = WandItem.getAllVis(wandstack);
		for (Aspect aspect : aspects.getAspects()) {
			int amt = aspects.getAmount(aspect);
			g.pose().pushMatrix();
			g.pose().rotate((float) Math.toRadians(90.0F));
			g.pose().rotate((float) Math.toRadians(-15 + count * 24));
			g.pose().translate(0.0F, -32.0F);
			g.pose().scale(0.5F, 0.5F);
			int loc = (int) (30.0F * amt / Math.max(1, max));
			if (loc > 0) {
				g.blit(RenderPipelines.GUI_TEXTURED, HUD, -4, 35 - loc, 104, 0, 8, loc, 256, 256, GuiUtil.color(aspect.getColor(), 0.8F));
			}
			g.blit(RenderPipelines.GUI_TEXTURED, HUD, -8, -3, 72, 0, 16, 42, 256, 256);
			// a marker over each primal the fitted focus spends
			boolean spends = focus != null && focus.getVisCost(focusStack).getAmount(aspect) > 0;
			int sh = 0;
			if (spends) {
				g.blit(RenderPipelines.GUI_TEXTURED, HUD, -4, -8, 136, 0, 8, 8, 256, 256);
				sh = 8;
			}
			if (old.getAmount(aspect) > amt) {
				g.blit(RenderPipelines.GUI_TEXTURED, HUD, -4, -8 - sh, 128, 0, 8, 8, 256, 256);
			} else if (old.getAmount(aspect) < amt) {
				g.blit(RenderPipelines.GUI_TEXTURED, HUD, -4, -8 - sh, 120, 0, 8, 8, 256, 256);
			}
			if (player.isShiftKeyDown()) {
				g.pose().pushMatrix();
				g.pose().rotate((float) Math.toRadians(-90.0F));
				g.text(mc.font, String.valueOf(amt / 100), -32, -4, 0xFFFFFFFF, true);
				if (spends) {
					float mod = WandItem.getConsumptionModifier(wandstack, player, aspect, false);
					g.text(mc.font, FORMAT.format(focus.getVisCost(focusStack).getAmount(aspect) * mod / 100.0F), 8, -4, 0xFFFFFFFF, true);
				}
				g.pose().popMatrix();
			}
			g.pose().popMatrix();
			count++;
		}
		if (focus != null) {
			ItemStack picked = focus instanceof dev.thaumvibe.items.foci.TradeFocusItem ? dev.thaumvibe.items.foci.TradeFocusItem.getPickedBlock(wandstack)
				: ItemStack.EMPTY;
			if (!picked.isEmpty()) {
				renderTradeHud(g, mc, player, picked);
			} else {
				g.item(focusStack, -8, -8);
				float f = dev.thaumvibe.wands.WandFoci.getCooldown(player);
				if (f > 0.0F) {
					g.pose().pushMatrix();
					g.pose().scale(0.5F, 0.5F);
					String secs = FORMAT2.format(f) + "s";
					g.text(mc.font, secs, -mc.font.width(secs) / 2, -4, 0xFFFFFFFF, true);
					g.pose().popMatrix();
				}
			}
		}
		g.pose().popMatrix();
	}

	/** ClientTickEventsFML.renderWandTradeHud: the equal trade focus's picked block and how many the player carries. */
	private static void renderTradeHud(GuiGraphicsExtractor g, Minecraft mc, Player player, ItemStack picked) {
		int amount = 0;
		for (ItemStack is : player.getInventory().getNonEquipmentItems()) {
			if (ItemStack.isSameItem(is, picked)) {
				amount += is.getCount();
			}
		}
		g.item(picked, -8, -8);
		String am = String.valueOf(amount);
		int sw = mc.font.width(am);
		g.pose().pushMatrix();
		g.pose().translate(0.0F, -mc.font.lineHeight);
		g.pose().scale(0.5F, 0.5F);
		for (int a = -1; a <= 1; a++) {
			for (int b = -1; b <= 1; b++) {
				if ((a == 0 || b == 0) && (a != 0 || b != 0)) {
					g.text(mc.font, am, a + 16 - sw, b + 24, 0xFF000000, false);
				}
			}
		}
		g.text(mc.font, am, 16 - sw, 24, 0xFFFFFFFF, false);
		g.pose().popMatrix();
	}

	private WandHud() {
	}
}
