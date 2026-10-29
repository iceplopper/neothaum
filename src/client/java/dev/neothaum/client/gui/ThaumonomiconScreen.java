package dev.neothaum.client.gui;

import dev.neothaum.NeoThaum;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.client.TVClientNetwork;
import dev.neothaum.client.render.TVRenderTypes;
import dev.neothaum.network.TVNetwork;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.research.PlayerKnowledge;
import dev.neothaum.research.ResearchCategory;
import dev.neothaum.research.ResearchItem;
import dev.neothaum.research.ResearchManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.Items;

/** Port of GuiResearchBrowser: the Thaumonomicon's research map. */
public class ThaumonomiconScreen extends Screen {
	public static final Identifier GUI = NeoThaum.id("textures/gui/gui_research.png");
	private static final Identifier UNKNOWN = NeoThaum.id("textures/aspects/_unknown.png");
	private static final Style GALACTIC = Style.EMPTY.withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("alt")));
	private static int guiMapTop;
	private static int guiMapLeft;
	private static int guiMapBottom;
	private static int guiMapRight;
	public static int lastX = -5;
	public static int lastY = -6;
	/** NeoThaum: mouse-wheel zoom of the research map (later Thaumcraft versions had it, TC4 did not). Kept between openings. */
	public static float zoom = 1.0F;
	private static String selectedCategory = null;
	protected int paneWidth = 256;
	protected int paneHeight = 230;
	protected int mouseX = 0;
	protected int mouseY = 0;
	protected double prevMapX;
	protected double prevMapY;
	protected double guiMapX;
	protected double guiMapY;
	protected double targetX;
	protected double targetY;
	private boolean dragging;
	private boolean mouseDown;
	private final List<ResearchItem> research = new ArrayList<>();
	private ResearchItem currentHighlight = null;
	long popuptime = 0L;
	String popupmessage = "";
	public boolean hasScribestuff = false;

	public ThaumonomiconScreen() {
		super(Component.translatable("item.neothaum.thaumonomicon"));
		short v = 141;
		this.prevMapX = this.guiMapX = this.targetX = lastX * 24 - v / 2 - 12;
		this.prevMapY = this.guiMapY = this.targetY = lastY * 24 - v / 2;
		this.updateResearch();
	}

	public ThaumonomiconScreen(double x, double y) {
		super(Component.translatable("item.neothaum.thaumonomicon"));
		this.prevMapX = this.guiMapX = this.targetX = x;
		this.prevMapY = this.guiMapY = this.targetY = y;
		this.updateResearch();
	}

	private PlayerKnowledge knowledge() {
		return ResearchManager.knowledge(Minecraft.getInstance().player);
	}

	private boolean complete(String key) {
		return knowledge().research.contains(key);
	}

	public void updateResearch() {
		Minecraft mc = Minecraft.getInstance();
		this.research.clear();
		this.hasScribestuff = false;
		var cats = ResearchManager.categories();
		if (cats.isEmpty()) {
			return;
		}
		if (selectedCategory == null || !cats.containsKey(selectedCategory)) {
			selectedCategory = cats.keySet().iterator().next();
		}
		ResearchCategory cat = cats.get(selectedCategory);
		cat.research.values().stream().filter(ResearchManager::isAvailable).forEach(this.research::add);
		if (ResearchManager.consumeInkFromPlayer(mc.player, false) && mc.player.getInventory().contains(new net.minecraft.world.item.ItemStack(Items.PAPER))) {
			this.hasScribestuff = true;
		}
		guiMapTop = cat.minDisplayColumn * 24 - 85;
		guiMapLeft = cat.minDisplayRow * 24 - 112;
		guiMapBottom = cat.maxDisplayColumn * 24 - 112;
		guiMapRight = cat.maxDisplayRow * 24 - 61;
	}

	@Override
	public void removed() {
		short v = 141;
		lastX = (int) ((this.guiMapX + v / 2 + 12.0) / 24.0);
		lastY = (int) ((this.guiMapY + v / 2) / 24.0);
		super.removed();
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (this.minecraft.options.keyInventory.matches(event)) {
			TVClientNetwork.HIGHLIGHTED.clear();
			this.onClose();
			return true;
		}
		if (event.isEscape()) {
			TVClientNetwork.HIGHLIGHTED.clear();
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void tick() {
		this.prevMapX = this.guiMapX;
		this.prevMapY = this.guiMapY;
		double var1 = this.targetX - this.guiMapX;
		double var3 = this.targetY - this.guiMapY;
		if (var1 * var1 + var3 * var3 < 4.0) {
			this.guiMapX += var1;
			this.guiMapY += var3;
		} else {
			this.guiMapX += var1 * 0.85;
			this.guiMapY += var3 * 0.85;
		}
	}

	/** 1.7.10 drawDefaultBackground: a dark gradient, no blur. */
	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float pt) {
		g.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float pt) {
		int var4 = (this.width - this.paneWidth) / 2;
		int var5 = (this.height - this.paneHeight) / 2;
		if (this.mouseDown) {
			int var6 = var4 + 8;
			int var7 = var5 + 17;
			if (mx >= var6 && mx < var6 + 224 && my >= var7 && my < var7 + 196) {
				if (!this.dragging) {
					this.dragging = true;
				} else {
					this.guiMapX -= (mx - this.mouseX) / zoom;
					this.guiMapY -= (my - this.mouseY) / zoom;
					this.targetX = this.prevMapX = this.guiMapX;
					this.targetY = this.prevMapY = this.guiMapY;
				}
				this.mouseX = mx;
				this.mouseY = my;
			}
			this.targetX = Mth.clamp(this.targetX, guiMapTop, guiMapBottom - 1);
			this.targetY = Mth.clamp(this.targetY, guiMapLeft, guiMapRight - 1);
		} else {
			this.dragging = false;
		}
		this.genResearchBackground(g, mx, my, pt);
		if (this.popuptime > System.currentTimeMillis()) {
			int xq = var4 + 128;
			int yq = var5 + 128;
			List<FormattedCharSequence> lines = this.font.split(Component.literal(this.popupmessage), 150);
			int var41 = lines.size() * this.font.lineHeight / 2;
			g.fillGradient(xq - 78, yq - var41 - 3, xq + 78, yq + var41 + 3, 0xC0000000, 0xC0000000);
			for (int i = 0; i < lines.size(); i++) {
				g.text(this.font, lines.get(i), xq - 75, yq - var41 + i * this.font.lineHeight, 0xFF90A4FF, false);
			}
		}
		int count = 0;
		boolean swop = false;
		for (String key : ResearchManager.categories().keySet()) {
			if (count == 9) {
				count = 0;
				swop = true;
			}
			if (!key.equals("ELDRITCH") || complete("ELDRITCHMINOR")) {
				int mposx = mx - (var4 - 24 + (swop ? 280 : 0));
				int mposy = my - (var5 + count * 24);
				if (mposx >= 0 && mposx < 24 && mposy >= 0 && mposy < 24) {
					g.text(this.font, ResearchManager.categories().get(key).getName(), mx, my - 8, 0xFFFFFFFF, true);
				}
				count++;
			}
		}
	}

	protected void genResearchBackground(GuiGraphicsExtractor g, int par1, int par2, float pt) {
		long t = System.nanoTime() / 50000000L;
		int var4 = Mth.floor(this.prevMapX + (this.guiMapX - this.prevMapX) * pt);
		int var5 = Mth.floor(this.prevMapY + (this.guiMapY - this.prevMapY) * pt);
		var4 = Mth.clamp(var4, guiMapTop, guiMapBottom - 1);
		var5 = Mth.clamp(var5, guiMapLeft, guiMapRight - 1);
		int var8 = (this.width - this.paneWidth) / 2;
		int var9 = (this.height - this.paneHeight) / 2;
		int var10 = var8 + 16;
		int var11 = var9 + 17;
		ResearchCategory cat = ResearchManager.categories().get(selectedCategory);
		if (cat == null) {
			return;
		}
		PlayerKnowledge k = knowledge();
		// scrolling parchment background (drawn at 2x)
		int vx = (int) ((float) (var4 - guiMapTop) / Math.max(1, Math.abs(guiMapTop - guiMapBottom)) * 288.0F);
		int vy = (int) ((float) (var5 - guiMapLeft) / Math.max(1, Math.abs(guiMapLeft - guiMapRight)) * 316.0F);
		g.pose().pushMatrix();
		g.pose().scale(2.0F, 2.0F);
		g.blit(RenderPipelines.GUI_TEXTURED, cat.background, var10 / 2, var11 / 2, vx / 2, vy / 2, 112, 98, 256, 256);
		g.pose().popMatrix();
		g.enableScissor(var10, var11, var10 + 224, var11 + 196);
		// zoom about the middle of the map pane; the mouse is mapped back into map space for hovering
		float cx = var10 + 112.0F, cy = var11 + 98.0F;
		g.pose().pushMatrix();
		g.pose().translate(cx, cy);
		g.pose().scale(zoom, zoom);
		g.pose().translate(-cx, -cy);
		int mapMx = Math.round(cx + (par1 - cx) / zoom);
		int mapMy = Math.round(cy + (par2 - cy) / zoom);
		for (ResearchItem r : this.research) {
			for (String pk : r.parents) {
				ResearchItem parent = ResearchManager.getResearch(pk);
				if (parent == null || !parent.category.equals(selectedCategory) || parent.isVirtual()) {
					continue;
				}
				int x1 = r.displayColumn * 24 - var4 + 11 + var10;
				int y1 = r.displayRow * 24 - var5 + 11 + var11;
				int x2 = parent.displayColumn * 24 - var4 + 11 + var10;
				int y2 = parent.displayRow * 24 - var5 + 11 + var11;
				boolean done = k.research.contains(r.key);
				boolean parentDone = k.research.contains(parent.key);
				if (done) {
					drawLine(g, x1, y1, x2, y2, 0.1F, 0.1F, 0.1F, pt, false);
				} else if (!r.isLost() && (!r.isHidden() && !r.isLost() || k.research.contains("@" + r.key))
					&& (!r.isConcealed() || canUnlockResearch(r))) {
					if (parentDone) {
						drawLine(g, x1, y1, x2, y2, 0.0F, 1.0F, 0.0F, pt, true);
					} else if ((!parent.isHidden() && !r.isLost() || k.research.contains("@" + parent.key))
						&& (!parent.isConcealed() || canUnlockResearch(parent))) {
						drawLine(g, x1, y1, x2, y2, 0.0F, 0.0F, 1.0F, pt, true);
					}
				}
			}
			for (String sk : r.siblings) {
				ResearchItem sibling = ResearchManager.getResearch(sk);
				if (sibling == null || !sibling.category.equals(selectedCategory) || sibling.isVirtual() || sibling.parents.contains(r.key)) {
					continue;
				}
				int x1 = r.displayColumn * 24 - var4 + 11 + var10;
				int y1 = r.displayRow * 24 - var5 + 11 + var11;
				int x2 = sibling.displayColumn * 24 - var4 + 11 + var10;
				int y2 = sibling.displayRow * 24 - var5 + 11 + var11;
				boolean done = k.research.contains(r.key);
				boolean sibDone = k.research.contains(sibling.key);
				if (done) {
					drawLine(g, x1, y1, x2, y2, 0.1F, 0.1F, 0.2F, pt, false);
				} else if (!r.isLost() && (!r.isHidden() || k.research.contains("@" + r.key)) && (!r.isConcealed() || canUnlockResearch(r))) {
					if (sibDone) {
						drawLine(g, x1, y1, x2, y2, 0.0F, 1.0F, 0.0F, pt, true);
					} else if ((!sibling.isHidden() || k.research.contains("@" + sibling.key)) && (!sibling.isConcealed() || canUnlockResearch(sibling))) {
						drawLine(g, x1, y1, x2, y2, 0.0F, 0.0F, 1.0F, pt, true);
					}
				}
			}
		}
		this.currentHighlight = null;
		for (ResearchItem r : this.research) {
			int var26 = r.displayColumn * 24 - var4;
			int var27 = r.displayRow * 24 - var5;
			float sx = cx + (var10 + var26 - cx) * zoom, sy = cy + (var11 + var27 - cy) * zoom;
			if (r.isVirtual() || sx < var10 - 24 * zoom || sy < var11 - 24 * zoom || sx > var10 + 224 || sy > var11 + 196) {
				continue;
			}
			int var42 = var10 + var26;
			int var41 = var11 + var27;
			float shade;
			if (k.research.contains(r.key)) {
				if (r.warp > 0) {
					drawForbidden(g, var42 + 11, var41 + 11);
				}
				shade = 1.0F;
			} else {
				if (!k.research.contains("@" + r.key) && (r.isLost() || r.isHidden() && !k.research.contains("@" + r.key) || r.isConcealed() && !canUnlockResearch(r))) {
					continue;
				}
				if (r.warp > 0) {
					drawForbidden(g, var42 + 11, var41 + 11);
				}
				shade = canUnlockResearch(r) ? (float) Math.sin(Util.getMillis() % 600L / 600.0 * Math.PI * 2.0) * 0.25F + 0.75F : 0.3F;
			}
			int col = GuiUtil.color(((int) (shade * 255) << 16) | ((int) (shade * 255) << 8) | (int) (shade * 255), 1.0F);
			if (r.isRound()) {
				g.blit(RenderPipelines.GUI_TEXTURED, GUI, var42 - 2, var41 - 2, 54, 230, 26, 26, 256, 256, col);
			} else if (r.isHidden()) {
				g.blit(RenderPipelines.GUI_TEXTURED, GUI, var42 - 2, var41 - 2, ResearchManager.boughtWithPoints(r) ? 230 : 86, 230, 26, 26, 256, 256, col);
			} else {
				g.blit(RenderPipelines.GUI_TEXTURED, GUI, var42 - 2, var41 - 2, ResearchManager.boughtWithPoints(r) ? 110 : 0, 230, 26, 26, 256, 256, col);
			}
			if (r.isSpecial()) {
				g.blit(RenderPipelines.GUI_TEXTURED, GUI, var42 - 2, var41 - 2, 26, 230, 26, 26, 256, 256, col);
			}
			boolean unlockable = canUnlockResearch(r);
			if (TVClientNetwork.HIGHLIGHTED.contains(r.key)) {
				int px = (int) (t % 16L) * 16;
				g.blit(RenderPipelines.GUI_TEXTURED, TVRenderTypes.PARTICLES, var42 - 5, var41 - 5, px, 80, 16, 16, 256, 256);
			}
			if (r.iconItem != null) {
				g.item(r.iconItem, var42 + 3, var41 + 3);
				if (!unlockable) {
					g.fill(var42 + 3, var41 + 3, var42 + 19, var41 + 19, 0xE0101010);
				}
			} else if (r.iconResource != null) {
				GuiUtil.blitFull(g, RenderPipelines.GUI_TEXTURED, r.iconResource, var42 + 3, var41 + 3, 16, 16, unlockable ? 0xFFFFFFFF : 0xFF333333);
			}
			if (par1 >= var10 && par2 >= var11 && par1 < var10 + 224 && par2 < var11 + 196 && mapMx >= var42 && mapMx <= var42 + 22 && mapMy >= var41
				&& mapMy <= var41 + 22) {
				this.currentHighlight = r;
			}
		}
		g.pose().popMatrix();
		g.disableScissor();
		// category tabs
		int count = 0;
		boolean swop = false;
		for (var e : ResearchManager.categories().entrySet()) {
			String key = e.getKey();
			ResearchCategory rcl = e.getValue();
			if (key.equals("ELDRITCH") && !complete("ELDRITCHMINOR")) {
				continue;
			}
			if (count == 9) {
				count = 0;
				swop = true;
			}
			int s0 = !swop ? 0 : 264;
			int s1 = 0;
			int s2 = swop ? 14 : 0;
			if (!selectedCategory.equals(key)) {
				s1 = 24;
				s2 = swop ? 6 : 8;
			}
			if (swop) {
				blitReversed(g, var8 + s0 - 8, var9 + count * 24, 176 + s1, 232, 24, 24);
			} else {
				g.blit(RenderPipelines.GUI_TEXTURED, GUI, var8 - 24 + s0, var9 + count * 24, 152 + s1, 232, 24, 24, 256, 256);
			}
			if (TVClientNetwork.HIGHLIGHTED.contains(key)) {
				int px = (int) (16L * (t % 16L));
				g.blit(RenderPipelines.GUI_TEXTURED, TVRenderTypes.PARTICLES, var8 - 27 + s2 + s0, var9 - 4 + count * 24, px, 80, 16, 16, 256, 256);
			}
			GuiUtil.blitFull(g, RenderPipelines.GUI_TEXTURED, rcl.icon, var8 - 19 + s2 + s0, var9 + 4 + count * 24, 16, 16, 0xFFFFFFFF);
			if (!selectedCategory.equals(key)) {
				if (swop) {
					blitReversed(g, var8 + s0 - 8, var9 + count * 24, 224, 232, 24, 24);
				} else {
					g.blit(RenderPipelines.GUI_TEXTURED, GUI, var8 - 24 + s0, var9 + count * 24, 200, 232, 24, 24, 256, 256);
				}
			}
			count++;
		}
		g.blit(RenderPipelines.GUI_TEXTURED, GUI, var8, var9, 0, 0, this.paneWidth, this.paneHeight, 256, 256);
		if (this.currentHighlight != null) {
			drawTooltip(g, par1, par2, k);
		}
	}

	private void drawTooltip(GuiGraphicsExtractor g, int par1, int par2, PlayerKnowledge k) {
		ResearchItem h = this.currentHighlight;
		Component name = h.getName();
		int var26 = par1 + 6;
		int var27 = par2 - 4;
		int var99 = 0;
		Font fr = this.font;
		boolean galactic = !k.research.contains(h.key) && !canUnlockResearch(h);
		Component shownName = galactic ? Component.literal(name.getString()).withStyle(GALACTIC) : name;
		if (!canUnlockResearch(h)) {
			Component missing = Component.translatable("tc.researchmissing");
			int w = (int) Math.max((float) fr.width(shownName), fr.width(missing) / 1.5F);
			List<FormattedCharSequence> lines = fr.split(missing, w * 2);
			int h2 = lines.size() * fr.lineHeight;
			g.fillGradient(var26 - 3, var27 - 3, var26 + w + 3, var27 + h2 / 2 + 13, 0xC0000000, 0xC0000000);
			g.pose().pushMatrix();
			g.pose().translate(var26, var27 + 12);
			g.pose().scale(0.5F, 0.5F);
			for (int i = 0; i < lines.size(); i++) {
				g.text(fr, lines.get(i), 0, i * fr.lineHeight, 0xFF6F5050, false);
			}
			g.pose().popMatrix();
		} else {
			boolean secondary = !k.research.contains(h.key) && h.tags.size() > 0 && ResearchManager.boughtWithPoints(h);
			boolean primary = !secondary && !k.research.contains(h.key);
			int w = (int) Math.max((float) fr.width(shownName), fr.width(h.getText()) / 1.9F);
			int var41x = fr.split(shownName, Math.max(1, w)).size() * fr.lineHeight + 5;
			if (primary) {
				var99 += 9;
				w = (int) Math.max((float) w, fr.width(Component.translatable("tc.research.shortprim")) / 1.9F);
			}
			if (secondary) {
				var99 += 29;
				w = (int) Math.max((float) w, fr.width(Component.translatable("tc.research.short")) / 1.9F);
			}
			int warp = Math.min(h.warp, 5);
			String wte = Component.translatable("tc.forbidden").getString().replaceAll("%n", Component.translatable("tc.forbidden.level." + warp).getString());
			if (h.warp > 0) {
				var99 += 9;
				w = (int) Math.max((float) w, fr.width(wte) / 1.9F);
			}
			g.fillGradient(var26 - 3, var27 - 3, var26 + w + 3, var27 + var41x + 6 + var99, 0xC0000000, 0xC0000000);
			g.pose().pushMatrix();
			g.pose().translate(var26, var27 + var41x - 1);
			g.pose().scale(0.5F, 0.5F);
			g.text(fr, h.getText(), 0, 0, 0xFF90A4FF, true);
			g.pose().popMatrix();
			if (warp > 0) {
				g.pose().pushMatrix();
				g.pose().translate(var26, var27 + var41x + 8);
				g.pose().scale(0.5F, 0.5F);
				g.text(fr, wte, 0, 0, 0xFFFFFFFF, true);
				g.pose().popMatrix();
				var41x += 9;
			}
			if (primary) {
				g.pose().pushMatrix();
				g.pose().translate(var26, var27 + var41x + 8);
				g.pose().scale(0.5F, 0.5F);
				if (ResearchManager.getResearchSlot(this.minecraft.player, h.key) >= 0) {
					g.text(fr, Component.translatable("tc.research.hasnote"), 0, 0, 0xFFFFA500, true);
				} else if (this.hasScribestuff) {
					g.text(fr, Component.translatable("tc.research.getprim"), 0, 0, 0xFF87CEEB, true);
				} else {
					g.text(fr, Component.translatable("tc.research.shortprim"), 0, 0, 0xFFDC143C, true);
				}
				g.pose().popMatrix();
			} else if (secondary) {
				boolean enough = true;
				int cc = 0;
				for (Aspect a : h.tags.getAspectsSortedAmount()) {
					if (k.hasDiscoveredAspect(a)) {
						float alpha = 1.0F;
						if (k.getAspectPool(a) < h.tags.getAmount(a)) {
							alpha = (float) Math.sin(Util.getMillis() % 600L / 600.0 * Math.PI * 2.0) * 0.25F + 0.75F;
							enough = false;
						}
						GuiUtil.drawTag(g, var26 + cc * 16, var27 + var41x + 8, a, h.tags.getAmount(a), 0, 771, alpha, false);
					} else {
						enough = false;
						GuiUtil.blitFull(g, RenderPipelines.GUI_TEXTURED, UNKNOWN, var26 + cc * 16, var27 + var41x + 8, 16, 16, 0x80808080);
					}
					cc++;
				}
				g.pose().pushMatrix();
				g.pose().translate(var26, var27 + var41x + 27);
				g.pose().scale(0.5F, 0.5F);
				g.text(fr, Component.translatable(enough ? "tc.research.purchase" : "tc.research.short"), 0, 0, enough ? 0xFF87CEEB : 0xFFDC143C, true);
				g.pose().popMatrix();
			}
		}
		int color = canUnlockResearch(h) ? (h.isSpecial() ? 0xFFFFFF80 : 0xFFFFFFFF) : (h.isSpecial() ? 0xFF808040 : 0xFF808080);
		g.text(fr, shownName, var26, var27, color, true);
	}

	private void blitReversed(GuiGraphicsExtractor g, int x, int y, int u, int v, int w, int h) {
		g.blit(GUI, x, y, x + w, y + h, u / 256.0F, (u - w) / 256.0F, v / 256.0F, (v + h) / 256.0F);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT) {
			this.mouseDown = true;
			this.mouseX = (int) event.x();
			this.mouseY = (int) event.y();
		}
		this.popuptime = System.currentTimeMillis() - 1L;
		PlayerKnowledge k = knowledge();
		int par1 = (int) event.x();
		int par2 = (int) event.y();
		if (this.currentHighlight != null && !k.research.contains(this.currentHighlight.key) && canUnlockResearch(this.currentHighlight)) {
			this.updateResearch();
			boolean secondary = this.currentHighlight.tags.size() > 0 && ResearchManager.boughtWithPoints(this.currentHighlight);
			if (secondary) {
				boolean enough = true;
				for (Aspect a : this.currentHighlight.tags.getAspects()) {
					if (k.getAspectPool(a) < this.currentHighlight.tags.getAmount(a)) {
						enough = false;
						break;
					}
				}
				if (enough) {
					TVNetwork.clientSender.accept(new TVNetwork.StartResearch(this.currentHighlight.key, 0));
				}
			} else if (this.hasScribestuff && ResearchManager.getResearchSlot(this.minecraft.player, this.currentHighlight.key) == -1) {
				TVNetwork.clientSender.accept(new TVNetwork.StartResearch(this.currentHighlight.key, 1));
				this.popuptime = System.currentTimeMillis() + 3000L;
				this.popupmessage = Component.translatable("tc.research.popup", this.currentHighlight.getName().getString()).getString();
			}
		} else if (this.currentHighlight != null && k.research.contains(this.currentHighlight.key)) {
			this.minecraft.gui.setScreen(new ResearchPagesScreen(this.currentHighlight, 0, this.guiMapX, this.guiMapY));
			return true;
		} else {
			int var4 = (this.width - this.paneWidth) / 2;
			int var5 = (this.height - this.paneHeight) / 2;
			int count = 0;
			boolean swop = false;
			for (String key : ResearchManager.categories().keySet()) {
				if (key.equals("ELDRITCH") && !complete("ELDRITCHMINOR")) {
					continue;
				}
				if (count == 9) {
					count = 0;
					swop = true;
				}
				int mposx = par1 - (var4 - 24 + (swop ? 280 : 0));
				int mposy = par2 - (var5 + count * 24);
				if (mposx >= 0 && mposx < 24 && mposy >= 0 && mposy < 24) {
					selectedCategory = key;
					this.updateResearch();
					this.minecraft.player.playSound(TVSounds.CAMERACLACK, 0.4F, 1.0F);
					break;
				}
				count++;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double sx, double sy) {
		if (sy != 0.0) {
			zoom = Mth.clamp(zoom * (sy > 0 ? 1.1F : 1.0F / 1.1F), 0.5F, 1.5F);
			return true;
		}
		return super.mouseScrolled(mx, my, sx, sy);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (event.button() == com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT) {
			this.mouseDown = false;
		}
		return super.mouseReleased(event);
	}

	private boolean canUnlockResearch(ResearchItem res) {
		PlayerKnowledge k = knowledge();
		for (String pt : res.parents) {
			ResearchItem parent = ResearchManager.getResearch(pt);
			if (parent != null && !k.research.contains(parent.key)) {
				return false;
			}
		}
		for (String pt : res.parentsHidden) {
			ResearchItem parent = ResearchManager.getResearch(pt);
			if (parent != null && !k.research.contains(parent.key)) {
				return false;
			}
		}
		return true;
	}

	/** GuiResearchBrowser.drawLine: a 3px line, optionally wiggling and fading towards the parent. */
	private void drawLine(GuiGraphicsExtractor g, int x, int y, int x2, int y2, float r, float gr, float b, float te, boolean wiggle) {
		float count = this.minecraft.player.tickCount + te;
		double d3 = x - x2;
		double d4 = y - y2;
		float dist = Mth.sqrt((float) (d3 * d3 + d4 * d4));
		int inc = Math.max(1, (int) (dist / 2.0F));
		float dx = (float) (d3 / inc);
		float dy = (float) (d4 / inc);
		if (Math.abs(d3) > Math.abs(d4)) {
			dx *= 2.0F;
		} else {
			dy *= 2.0F;
		}
		float px = x, py = y;
		boolean first = true;
		for (int a = 0; a <= inc; a++) {
			float r2 = r, g2 = gr, b2 = b;
			float mx = 0.0F, my = 0.0F;
			float op = 0.6F;
			if (wiggle) {
				float phase = (float) a / inc;
				mx = Mth.sin((count + a) / 7.0F) * 5.0F * (1.0F - phase);
				my = Mth.sin((count + a) / 5.0F) * 5.0F * (1.0F - phase);
				r2 = r * (1.0F - phase);
				g2 = gr * (1.0F - phase);
				b2 = b * (1.0F - phase);
				op *= phase;
			}
			float cx = x - dx * a + mx;
			float cy = y - dy * a + my;
			if (!first) {
				segment(g, px, py, cx, cy, GuiUtil.color(((int) (r2 * 255) << 16) | ((int) (g2 * 255) << 8) | (int) (b2 * 255), op));
			}
			first = false;
			px = cx;
			py = cy;
			if (Math.abs(d3) > Math.abs(d4)) {
				dx *= 1.0F - 1.0F / (inc * 3.0F / 2.0F);
			} else {
				dy *= 1.0F - 1.0F / (inc * 3.0F / 2.0F);
			}
		}
	}

	private static void segment(GuiGraphicsExtractor g, float x0, float y0, float x1, float y1, int argb) {
		float len = Mth.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0));
		if (len < 0.01F) {
			return;
		}
		g.pose().pushMatrix();
		g.pose().translate(x0, y0);
		g.pose().rotate((float) Math.atan2(y1 - y0, x1 - x0));
		g.fill(0, -1, Mth.ceil(len) + 1, 1, argb);
		g.pose().popMatrix();
	}

	/** drawForbidden: a pulsing dark node strip behind warp-giving research. */
	private void drawForbidden(GuiGraphicsExtractor g, double x, double y) {
		int count = this.minecraft.player.tickCount;
		int frames = 32;
		int part = count % frames;
		int frame = frames - 1 - part;
		float size = 80.0F * 0.66F / 2.0F;
		float u0 = (float) frame / frames;
		float u1 = (float) (frame + 1) / frames;
		float v0 = 5.0F / frames;
		float v1 = 6.0F / frames;
		g.blit(TVRenderTypes.NODES, (int) (x - size / 2), (int) (y - size / 2), (int) (x + size / 2), (int) (y + size / 2), u0, u1, v0, v1);
	}
}
