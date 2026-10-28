package dev.neothaum.client.hud;

import com.mojang.blaze3d.platform.InputConstants;
import dev.neothaum.NeoThaum;
import dev.neothaum.client.gui.GuiUtil;
import dev.neothaum.items.WandItem;
import dev.neothaum.network.TVNetwork;
import dev.neothaum.wands.WandFoci;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * KeyHandler and REHWandHandler.handleFociRadial: hold F with a wand to open a ring of every focus carried (inventory and pouches) and click one,
 * or let go over it, to fit it; sneak+F stows the current focus. G is the misc wand key that sizes an architect focus's area.
 */
public final class FocusRadial {
	private static final Identifier RADIAL = NeoThaum.id("textures/misc/radial.png");
	private static final Identifier RADIAL2 = NeoThaum.id("textures/misc/radial2.png");
	public static KeyMapping keyF;
	public static KeyMapping keyG;
	private static boolean keyPressedF;
	private static boolean keyPressedG;
	static boolean radialActive;
	static boolean radialLock;
	private static float radialHudScale;
	private static boolean lastState;
	private static long lastTime;
	private static final Map<String, ItemStack> FOCI = new LinkedHashMap<>();
	private static final Map<String, Boolean> HOVER = new LinkedHashMap<>();
	private static final Map<String, Float> SCALE = new LinkedHashMap<>();

	public static void init() {
		keyF = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.neothaum.focus", InputConstants.KEY_F, KeyMapping.Category.MISC));
		keyG = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.neothaum.misc", InputConstants.KEY_G, KeyMapping.Category.MISC));
	}

	/** F is also vanilla's swap-hands key; while a wand is in hand the focus key wins (1.7 had no off hand to swap to). */
	public static void startTick(Minecraft mc) {
		if (mc.player != null && (mc.player.getMainHandItem().getItem() instanceof WandItem
			|| mc.player.getMainHandItem().getItem() instanceof dev.neothaum.golem.GolemBellItem) && keyF.same(mc.options.keySwapOffhand)) {
			while (mc.options.keySwapOffhand.consumeClick()) {
				// swallowed
			}
		}
	}

	/** KeyHandler.playerTick. */
	public static void tick(Minecraft mc) {
		Player player = mc.player;
		boolean inGame = mc.gui.screen() == null;
		if (keyF.isDown()) {
			if (inGame && player != null) {
				if (!keyPressedF) {
					radialLock = false;
				}
				ItemStack held = player.getMainHandItem();
				if (held.getItem() instanceof dev.neothaum.golem.GolemBellItem && !keyPressedF) {
					// KeyHandler: with the bell in hand the key clears its golem's markers
					TVNetwork.clientSender.accept(new TVNetwork.FocusChange("BELL_RESET"));
				} else if (!radialLock && held.getItem() instanceof WandItem && !WandItem.isSceptre(held)) {
					if (player.isShiftKeyDown()) {
						if (!keyPressedF) {
							TVNetwork.clientSender.accept(new TVNetwork.FocusChange("REMOVE"));
						}
					} else {
						radialActive = true;
					}
				}
				keyPressedF = true;
			}
		} else {
			radialActive = false;
			keyPressedF = false;
		}
		if (keyG.isDown()) {
			if (inGame && player != null && !keyPressedG) {
				TVNetwork.clientSender.accept(new TVNetwork.WandMisc());
			}
			keyPressedG = true;
		} else {
			keyPressedG = false;
		}
	}

	/** REHWandHandler.handleFociRadial and renderFocusRadialHUD. */
	public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if (player == null || !radialActive && radialHudScale <= 0.0F) {
			return;
		}
		if (!radialActive) {
			if (mc.gui.screen() == null && lastState && !mc.mouseHandler.isMouseGrabbed()) {
				mc.mouseHandler.grabMouse();
			}
			lastState = false;
		} else {
			if (mc.gui.screen() != null) {
				radialActive = false;
				radialLock = true;
				return;
			}
			if (radialHudScale == 0.0F) {
				FOCI.clear();
				HOVER.clear();
				SCALE.clear();
				FOCI.putAll(WandFoci.radialFoci(player));
				for (String k : FOCI.keySet()) {
					HOVER.put(k, false);
					SCALE.put(k, 1.0F);
				}
				if (!FOCI.isEmpty() && mc.mouseHandler.isMouseGrabbed()) {
					mc.mouseHandler.releaseMouse();
				}
			}
		}
		float pt = delta.getGameTimeDeltaPartialTick(false);
		draw(g, mc, player, pt);
		long time = System.currentTimeMillis();
		if (time > lastTime) {
			for (String key : HOVER.keySet()) {
				if (HOVER.get(key)) {
					if (!radialActive && !radialLock) {
						TVNetwork.clientSender.accept(new TVNetwork.FocusChange(key));
						radialLock = true;
					}
					if (SCALE.get(key) < 1.3F) {
						SCALE.put(key, SCALE.get(key) + 0.025F);
					}
				} else if (SCALE.get(key) > 1.0F) {
					SCALE.put(key, SCALE.get(key) - 0.025F);
				}
			}
			radialHudScale += radialActive ? 0.05F : -0.05F;
			if (radialHudScale > 1.0F) {
				radialHudScale = 1.0F;
			}
			if (radialHudScale < 0.0F) {
				radialHudScale = 0.0F;
				radialLock = false;
			}
			lastTime = time + 5L;
			lastState = radialActive;
		}
	}

	private static void draw(GuiGraphicsExtractor g, Minecraft mc, Player player, float pt) {
		ItemStack wand = player.getMainHandItem();
		if (!(wand.getItem() instanceof WandItem) || FOCI.isEmpty()) {
			return;
		}
		int sw = g.guiWidth();
		int sh = g.guiHeight();
		double i = mc.mouseHandler.getScaledXPos(mc.getWindow());
		double j = mc.mouseHandler.getScaledYPos(mc.getWindow());
		boolean click = mc.mouseHandler.isLeftPressed();
		ItemStack tooltip = ItemStack.EMPTY;
		float width = 16.0F + FOCI.size() * 2.5F;
		float spin = pt + player.tickCount % 720 / 2.0F;
		g.pose().pushMatrix();
		g.pose().translate(sw / 2.0F, sh / 2.0F);
		ring(g, RADIAL, spin, width * 2.75F * radialHudScale);
		ring(g, RADIAL2, -spin, width * 2.55F * radialHudScale);
		ItemStack current = WandItem.getFocusStack(wand);
		if (!current.isEmpty()) {
			g.item(current, -8, -8);
			double mx = i - sw / 2.0;
			double my = j - sh / 2.0;
			if (mx >= -10 && mx <= 10 && my >= -10 && my <= 10) {
				tooltip = current;
			}
		}
		g.pose().pushMatrix();
		g.pose().scale(radialHudScale, radialHudScale);
		float currentRot = -90.0F * radialHudScale;
		float pieSlice = 360.0F / FOCI.size();
		for (Map.Entry<String, ItemStack> e : FOCI.entrySet()) {
			String key = e.getKey();
			double xx = Mth.cos(currentRot / 180.0F * (float) Math.PI) * width;
			double yy = Mth.sin(currentRot / 180.0F * (float) Math.PI) * width;
			currentRot += pieSlice;
			g.pose().pushMatrix();
			g.pose().translate((float) xx, (float) yy);
			float s = SCALE.get(key);
			g.pose().scale(s, s);
			g.item(e.getValue(), -8, -8);
			g.pose().popMatrix();
			if (!radialLock && radialActive) {
				double mx = i - sw / 2.0 - xx * radialHudScale;
				double my = j - sh / 2.0 - yy * radialHudScale;
				if (mx >= -10 && mx <= 10 && my >= -10 && my <= 10) {
					HOVER.put(key, true);
					tooltip = e.getValue();
					if (click) {
						radialActive = false;
						radialLock = true;
						TVNetwork.clientSender.accept(new TVNetwork.FocusChange(key));
						break;
					}
				} else {
					HOVER.put(key, false);
				}
			}
		}
		g.pose().popMatrix();
		g.pose().popMatrix();
		if (!tooltip.isEmpty()) {
			g.setTooltipForNextFrame(mc.font, tooltip, sw / 2 - 4, sh / 2 + 20);
		}
	}

	/** UtilsFX.renderQuadCenteredFromTexture at half alpha, slowly turning. */
	private static void ring(GuiGraphicsExtractor g, Identifier tex, float rot, float size) {
		if (size <= 0.0F) {
			return;
		}
		g.pose().pushMatrix();
		g.pose().rotate(rot * Mth.DEG_TO_RAD);
		g.pose().scale(size / 256.0F, size / 256.0F);
		g.blit(RenderPipelines.GUI_TEXTURED, tex, -128, -128, 0, 0, 256, 256, 256, 256, GuiUtil.color(0xFFFFFF, 0.5F));
		g.pose().popMatrix();
	}

	private FocusRadial() {
	}
}
