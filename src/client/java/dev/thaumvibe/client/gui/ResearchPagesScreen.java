package dev.thaumvibe.client.gui;

import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.aspects.ObjectAspects;
import dev.thaumvibe.client.TVClientNetwork;
import dev.thaumvibe.crafting.CrucibleRecipe;
import dev.thaumvibe.crafting.InfusionEnchantmentRecipe;
import dev.thaumvibe.crafting.InfusionRecipe;
import dev.thaumvibe.crafting.ShapedArcaneRecipe;
import dev.thaumvibe.crafting.ShapelessArcaneRecipe;
import dev.thaumvibe.registry.TVSounds;
import dev.thaumvibe.research.PlayerKnowledge;
import dev.thaumvibe.research.ResearchItem;
import dev.thaumvibe.research.ResearchManager;
import dev.thaumvibe.research.ResearchPage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;

/** Port of GuiResearchRecipe: the open-book view of a research entry's pages. */
public class ResearchPagesScreen extends Screen {
	private static final Identifier TEX1 = Thaumvibe.id("textures/gui/gui_researchbook.png");
	private static final Identifier TEX2 = Thaumvibe.id("textures/gui/gui_researchbook_overlay.png");
	private static final Identifier BACK = Thaumvibe.id("textures/aspects/_back.png");
	private static final Deque<Object[]> HISTORY = new ArrayDeque<>();
	private static Map<Item, Object[]> recipeKeys;
	protected int paneWidth = 256;
	protected int paneHeight = 181;
	protected double guiMapX;
	protected double guiMapY;
	private final ResearchItem research;
	private final List<ResearchPage> pages;
	private int page;
	private final int maxPages;
	private final Map<Aspect, List<ItemStack>> aspectItems = new HashMap<>();
	private List<Component> tooltip;
	private int tooltipX, tooltipY;
	private long lastCycle = 0L;
	private int cycle = -1;
	private final List<Object[]> reference = new ArrayList<>();

	public ResearchPagesScreen(ResearchItem research, int page, double x, double y) {
		super(research.getName());
		this.research = research;
		this.guiMapX = x;
		this.guiMapY = y;
		PlayerKnowledge k = ResearchManager.knowledge(net.minecraft.client.Minecraft.getInstance().player);
		List<ResearchPage> p2 = new ArrayList<>();
		for (ResearchPage pp : research.pages) {
			if (pp.type() != ResearchPage.Type.TEXT_CONCEALED || k.research.contains(pp.research())) {
				p2.add(pp);
			}
		}
		if (research.key.equals("ASPECTS")) {
			for (String s : k.objects) {
				String id = s.substring(1);
				if (!id.startsWith("item:")) {
					continue;
				}
				Optional<Item> it = BuiltInRegistries.ITEM.getOptional(Identifier.parse(id.substring(5)));
				if (it.isEmpty()) {
					continue;
				}
				ItemStack is = new ItemStack(it.get());
				AspectList tags = ObjectAspects.getAspects(is);
				if (tags != null) {
					for (Aspect a : tags.getAspects()) {
						aspectItems.computeIfAbsent(a, q -> new ArrayList<>()).add(is.copyWithCount(tags.getAmount(a)));
					}
				}
			}
			AspectList tal = new AspectList();
			int count = 0;
			for (Aspect aspect : k.aspects.getAspectsSorted()) {
				if (count <= 4) {
					count++;
					tal.add(aspect, k.aspects.getAmount(aspect));
				}
				if (count == 4) {
					count = 0;
					p2.add(new ResearchPage(ResearchPage.Type.ASPECTS, null, null, List.of(), null, tal.copy(), null, null));
					tal = new AspectList();
				}
			}
			if (count > 0) {
				p2.add(new ResearchPage(ResearchPage.Type.ASPECTS, null, null, List.of(), null, tal, null, null));
			}
		}
		this.pages = p2;
		this.maxPages = p2.size();
		this.page = page % 2 == 1 ? page - 1 : page;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (this.minecraft.options.keyInventory.matches(event) || event.isEscape()) {
			HISTORY.clear();
			this.minecraft.gui.setScreen(new ThaumonomiconScreen(this.guiMapX, this.guiMapY));
			return true;
		}
		return super.keyPressed(event);
	}

	/** 1.7.10 drawDefaultBackground: a dark gradient, no blur. */
	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float pt) {
		g.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float pt) {
		int sw = (this.width - this.paneWidth) / 2;
		int sh = (this.height - this.paneHeight) / 2;
		float var10 = (this.width - this.paneWidth * 1.3F) / 2.0F;
		float var11 = (this.height - this.paneHeight * 1.3F) / 2.0F;
		g.pose().pushMatrix();
		g.pose().translate(var10, var11);
		g.pose().scale(1.3F, 1.3F);
		g.blit(RenderPipelines.GUI_TEXTURED, TEX1, 0, 0, 0, 0, this.paneWidth, this.paneHeight, 256, 256);
		g.pose().popMatrix();
		this.reference.clear();
		this.tooltip = null;
		if (this.lastCycle < System.currentTimeMillis()) {
			this.cycle++;
			this.lastCycle = System.currentTimeMillis() + 1000L;
		}
		int current = 0;
		for (ResearchPage p : this.pages) {
			if ((current == this.page || current == this.page + 1) && current < this.maxPages) {
				drawPage(g, p, current % 2, sw, sh, mx, my);
			}
			if (++current > this.page + 1) {
				break;
			}
		}
		float bob = Mth.sin(this.minecraft.player.tickCount / 3.0F) * 0.2F + 0.1F;
		if (!HISTORY.isEmpty()) {
			blitScaled(g, sw + 118, sh + 189, 38, 202, 20, 12, bob);
		}
		if (this.page > 0) {
			blitScaled(g, sw - 16, sh + 190, 0, 184, 12, 8, bob);
		}
		if (this.page < this.maxPages - 2) {
			blitScaled(g, sw + 262, sh + 190, 12, 184, 12, 8, bob);
		}
		if (this.tooltip != null) {
			g.setComponentTooltipForNextFrame(this.font, this.tooltip, this.tooltipX, this.tooltipY);
		}
		if (!HISTORY.isEmpty()) {
			int hx = mx - (sw + 118);
			int hy = my - (sh + 189);
			if (hx >= 0 && hy >= 0 && hx < 20 && hy < 12) {
				g.text(this.font, Component.translatable("recipe.return"), mx, my, 0xFFFFFFFF, true);
			}
		}
	}

	private void blitScaled(GuiGraphicsExtractor g, int x, int y, int u, int v, int w, int h, float scale) {
		g.pose().pushMatrix();
		g.pose().translate(x + w / 2.0F, y + h / 2.0F);
		g.pose().scale(1.0F + scale, 1.0F + scale);
		g.blit(RenderPipelines.GUI_TEXTURED, TEX1, -w / 2, -h / 2, u, v, w, h, 256, 256);
		g.pose().popMatrix();
	}

	private void tip(List<Component> lines, int x, int y) {
		this.tooltip = lines;
		this.tooltipX = x;
		this.tooltipY = y;
	}

	private void drawPage(GuiGraphicsExtractor g, ResearchPage p, int side, int x, int y, int mx, int my) {
		if (this.page == 0 && side == 0) {
			g.blit(RenderPipelines.GUI_TEXTURED, TEX1, x + 4, y - 13, 24, 184, 96, 4, 256, 256);
			g.blit(RenderPipelines.GUI_TEXTURED, TEX1, x + 4, y + 4, 24, 184, 96, 4, 256, 256);
			Component name = this.research.getName();
			int offset = this.font.width(name);
			if (offset <= 130) {
				g.text(this.font, name, x + 52 - offset / 2, y - 6, 0xFF303030, false);
			} else {
				float vv = 130.0F / offset;
				g.pose().pushMatrix();
				g.pose().translate(x + 52 - offset / 2.0F * vv, y - 6.0F * vv);
				g.pose().scale(vv, vv);
				g.text(this.font, name, 0, 0, 0xFF303030, false);
				g.pose().popMatrix();
			}
			y += 25;
		}
		switch (p.type()) {
			case TEXT, TEXT_CONCEALED -> drawTextPage(g, side, x, y - 10, Component.translatable(p.text()).getString());
			case ASPECTS -> drawAspectPage(g, side, x - 8, y - 8, mx, my, p.aspects());
			case COMPOUND_CRAFTING -> drawCompoundPage(g, side, x - 4, y - 8, mx, my, p.compound());
			case SMELTING -> {
				// ResearchPage(ItemStack): the furnace recipe that takes this input
				SmeltingRecipe sr = null;
				for (Recipe<?> rec : TVClientNetwork.RECIPES.values()) {
					if (rec instanceof SmeltingRecipe s && p.smeltingInput() != null && s.input().test(p.smeltingInput())) {
						sr = s;
						break;
					}
				}
				if (sr != null) {
					drawSmeltingPage(g, side, x - 4, y - 8, mx, my, sr);
				}
			}
			case RECIPE -> {
				Recipe<?> r = pick(p.recipes());
				if (r instanceof ShapedArcaneRecipe || r instanceof ShapelessArcaneRecipe) {
					drawArcanePage(g, side, x - 4, y - 8, mx, my, r);
				} else if (r instanceof CrucibleRecipe cr) {
					drawCruciblePage(g, side, x - 4, y - 8, mx, my, cr);
				} else if (r instanceof SmeltingRecipe sr) {
					drawSmeltingPage(g, side, x - 4, y - 8, mx, my, sr);
				} else if (r instanceof InfusionRecipe ir) {
					drawInfusionPage(g, side, x - 4, y - 8, mx, my, ir);
				} else if (r instanceof InfusionEnchantmentRecipe er) {
					drawInfusionEnchantingPage(g, side, x - 4, y - 8, mx, my, er);
				} else if (r != null) {
					drawCraftingPage(g, side, x - 4, y - 8, mx, my, r);
				}
			}
			default -> {
			}
		}
	}

	private Recipe<?> pick(List<Identifier> ids) {
		List<Recipe<?>> found = new ArrayList<>();
		for (Identifier id : ids) {
			Recipe<?> r = TVClientNetwork.RECIPES.get(id);
			if (r != null) {
				found.add(r);
			}
		}
		if (found.isEmpty()) {
			return null;
		}
		if (this.cycle < 0 || this.cycle >= found.size()) {
			this.cycle = 0;
		}
		return found.get(this.cycle % found.size());
	}

	private ItemStack cycleIngredient(Ingredient ing) {
		List<Holder<Item>> items = ing.items().toList();
		if (items.isEmpty()) {
			return ItemStack.EMPTY;
		}
		int idx = (int) (System.currentTimeMillis() / 1000L % items.size());
		return new ItemStack(items.get(idx));
	}

	/** TCFontRenderer was created with the unicode flag on: book text uses the small unicode font. */
	private static final net.minecraft.network.chat.FontDescription UNICODE = new net.minecraft.network.chat.FontDescription.Resource(Identifier.withDefaultNamespace("uniform"));

	/**
	 * The text in the small font. Legacy formatting codes are applied here rather than left in the string, because a {@code §r} inside a
	 * literal resets the whole style, font included, and the rest of the line would fall back to the large default font.
	 */
	static Component small(String text) {
		net.minecraft.network.chat.MutableComponent out = Component.empty();
		net.minecraft.network.chat.Style style = net.minecraft.network.chat.Style.EMPTY.withFont(UNICODE);
		StringBuilder run = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '§' && i + 1 < text.length()) {
				net.minecraft.ChatFormatting f = net.minecraft.ChatFormatting.getByCode(text.charAt(i + 1));
				if (f != null) {
					if (run.length() > 0) {
						out.append(Component.literal(run.toString()).withStyle(style));
						run.setLength(0);
					}
					style = f == net.minecraft.ChatFormatting.RESET ? net.minecraft.network.chat.Style.EMPTY.withFont(UNICODE)
						: style.applyLegacyFormat(f);
					i++;
					continue;
				}
			}
			run.append(c);
		}
		if (run.length() > 0) {
			out.append(Component.literal(run.toString()).withStyle(style));
		}
		return out;
	}

	/** TCFontRenderer.drawSplitString with <BR>, <LINE> and <IMG> support. */
	private void drawTextPage(GuiGraphicsExtractor g, int side, int x, int y, String text) {
		int px = x - 15 + side * 152;
		int width = 139;
		text = text.replace("<BR/>", "\n").replace("<BR>", "\n").replace("<LINE/>", "<LINE>");
		List<String> chunks = new ArrayList<>();
		int i = 0;
		while (i < text.length()) {
			int line = text.indexOf("<LINE>", i);
			int img = text.indexOf("<IMG>", i);
			int next = line < 0 ? img : img < 0 ? line : Math.min(line, img);
			if (next < 0) {
				chunks.add(text.substring(i));
				break;
			}
			chunks.add(text.substring(i, next));
			if (next == line) {
				chunks.add("<LINE>");
				i = next + 6;
			} else {
				int end = text.indexOf("</IMG>", next);
				chunks.add(text.substring(next, end + 6));
				i = end + 6;
			}
		}
		for (String c : chunks) {
			if (c.equals("<LINE>")) {
				y += this.font.lineHeight;
				g.blit(RenderPipelines.GUI_TEXTURED, TEX1, px + width / 2 - 48, y + 2, 24, 184, 96, 4, 256, 256);
				y += this.font.lineHeight;
			} else if (c.startsWith("<IMG>")) {
				String[] s = c.substring(5, c.length() - 6).split(":");
				Identifier tex = Identifier.fromNamespaceAndPath(s[0].equals("thaumcraft") ? "thaumvibe" : s[0], s[1]);
				float scale = Float.parseFloat(s[6]);
				int u = Integer.parseInt(s[2]), v = Integer.parseInt(s[3]), w = Integer.parseInt(s[4]), h = Integer.parseInt(s[5]);
				y += this.font.lineHeight;
				g.pose().pushMatrix();
				g.pose().translate(px - 3 + width / 2.0F - w / 2.0F * scale, y);
				g.pose().scale(scale, scale);
				g.blit(RenderPipelines.GUI_TEXTURED, tex, 0, 0, u, v, w, h, 256, 256);
				g.pose().popMatrix();
				y += (int) (h * scale);
			} else {
				for (String para : c.split("\n", -1)) {
					List<FormattedCharSequence> lines = this.font.split(small(para), width);
					if (lines.isEmpty()) {
						y += this.font.lineHeight;
					}
					for (FormattedCharSequence l : lines) {
						g.text(this.font, l, px, y, 0xFF000000, false);
						y += this.font.lineHeight;
					}
				}
			}
		}
	}

	private void drawAspectPage(GuiGraphicsExtractor g, int side, int x, int y, int mx, int my, AspectList aspects) {
		if (aspects == null || aspects.size() == 0) {
			return;
		}
		int start = side * 152;
		int count = 0;
		for (Aspect aspect : aspects.getAspectsSorted()) {
			int tx = x + start;
			int ty = y + count * 50;
			if (mx >= tx && my >= ty && mx < tx + 40 && my < ty + 40) {
				GuiUtil.blitFull(g, RenderPipelines.GUI_TEXTURED, BACK, x + start - 5, y + count * 50 - 5, 40, 40, 0xFFFFFFFF);
			}
			g.pose().pushMatrix();
			g.pose().scale(2.0F, 2.0F);
			GuiUtil.drawTag(g, (x + start) / 2, (y + count * 50) / 2, aspect, aspects.getAmount(aspect), 0, 771, 1.0F, false);
			g.pose().popMatrix();
			Component name = small(aspect.getName());
			int offset = this.font.width(name) / 2;
			g.text(this.font, name, x + start + 16 - offset, y + 33 + count * 50, 0xFF505050, false);
			if (aspect.getComponents() != null) {
				g.pose().pushMatrix();
				g.pose().scale(1.5F, 1.5F);
				GuiUtil.drawTag(g, (int) ((x + start + 54) / 1.5F), (int) ((y + 4 + count * 50) / 1.5F), aspect.getComponents()[0], 0.0F, 0, 771, 1.0F, false);
				GuiUtil.drawTag(g, (int) ((x + start + 96) / 1.5F), (int) ((y + 4 + count * 50) / 1.5F), aspect.getComponents()[1], 0.0F, 0, 771, 1.0F, false);
				g.pose().popMatrix();
				name = small("§o" + aspect.getComponents()[0].getName());
				offset = this.font.width(name) / 2;
				g.text(this.font, name, x + start + 16 - offset + 50, y + 30 + count * 50, 0xFF505050, false);
				name = small("§o" + aspect.getComponents()[1].getName());
				offset = this.font.width(name) / 2;
				g.text(this.font, name, x + start + 16 - offset + 92, y + 30 + count * 50, 0xFF505050, false);
				g.text(this.font, "=", x + start + 7 + 32, y + 12 + count * 50, 0xFF999999, false);
				g.text(this.font, "+", x + start + 4 + 79, y + 12 + count * 50, 0xFF999999, false);
			} else {
				g.text(this.font, small(Component.translatable("tc.aspect.primal").getString()), x + start + 48, y + 12 + count * 50, 0xFF444444, false);
			}
			count++;
		}
		count = 0;
		for (Aspect aspect : aspects.getAspectsSorted()) {
			int tx = x + start;
			int ty = y + count * 50;
			if (mx >= tx && my >= ty && mx < tx + 40 && my < ty + 40) {
				List<ItemStack> items = this.aspectItems.get(aspect);
				if (items != null) {
					int xc = 0, yc = 0;
					for (ItemStack item : items) {
						int ix = mx + 8 + xc * 17;
						int iy = 17 * yc + (my - (4 + items.size() / 8 * 8));
						g.item(item, ix, iy);
						g.itemDecorations(this.font, item, ix, iy);
						if (++xc >= 8) {
							xc = 0;
							yc++;
						}
					}
				}
			}
			count++;
		}
	}

	/** A cell of a compound (multiblock) page: an item, the lava/water still texture, or null for TC4's blank "empty space". */
	private record Cell(ItemStack stack, Identifier fluid) {
	}

	private static Cell cell(com.google.gson.JsonElement e) {
		if (e == null || e.isJsonNull()) {
			return null;
		}
		com.google.gson.JsonObject o = e.getAsJsonObject();
		String id = o.has("item") ? o.get("item").getAsString() : null;
		if (id == null || id.equals("thaumvibe:empty_space")) {
			return null;
		}
		if (id.equals("minecraft:lava") || id.equals("minecraft:water")) {
			return new Cell(ItemStack.EMPTY, Identifier.withDefaultNamespace("textures/block/" + id.substring(10) + "_still.png"));
		}
		if (id.equals("thaumvibe:wand") && o.has("components")) {
			var c = o.getAsJsonObject("components");
			return new Cell(dev.thaumvibe.items.WandItem.createFull(dev.thaumvibe.registry.TVItems.WAND, c.get("thaumvibe:wand_cap").getAsString(),
				c.get("thaumvibe:wand_rod").getAsString()), null);
		}
		return BuiltInRegistries.ITEM.getOptional(Identifier.parse(id)).map(i -> new Cell(new ItemStack(i), null)).orElse(null);
	}

	/** GuiResearchRecipe.drawCompoundCraftingPage: the structure drawn layer by layer in isometric view, with its essentia cost. */
	private void drawCompoundPage(GuiGraphicsExtractor g, int side, int x, int y, int mx, int my, com.google.gson.JsonElement data) {
		if (data == null || !data.isJsonArray()) {
			return;
		}
		var r = data.getAsJsonArray();
		AspectList aspects = new AspectList();
		if (r.get(0).isJsonObject() && r.get(0).getAsJsonObject().has("aspects")) {
			for (var e : r.get(0).getAsJsonObject().getAsJsonObject("aspects").entrySet()) {
				Aspect a = Aspect.getAspect(e.getKey());
				if (a != null) {
					aspects.add(a, e.getValue().getAsInt());
				}
			}
		}
		int dx = r.get(1).getAsInt(), dy = r.get(2).getAsInt(), dz = r.get(3).getAsInt();
		var list = r.get(4).getAsJsonArray();
		Cell[] cells = new Cell[dx * dy * dz];
		for (int i = 0; i < cells.length && i < list.size(); i++) {
			cells[i] = cell(list.get(i));
		}
		int xoff = 64 - (dx * 16 + dz * 16) / 2;
		int yoff = -dy * 25;
		int start = side * 152;
		String text = Component.translatable("recipe.type.construct").getString();
		g.text(this.font, text, x + start + 56 - this.font.width(text) / 2, y, 0xFF505050, false);
		Aspect[] sorted = aspects.getAspectsSortedAmount();
		if (sorted.length > 0) {
			g.pose().pushMatrix();
			g.pose().translate(x + start, y + 174);
			g.pose().scale(2.0F, 2.0F);
			g.blit(RenderPipelines.GUI_TEXTURED, TEX2, 0, 0, 68, 76, 12, 12, 256, 256, 0x66FFFFFF);
			g.pose().popMatrix();
			for (int c = 0; c < sorted.length; c++) {
				int tx = x + start + 14 + 18 * c + (5 - sorted.length) * 8;
				int ty = y + 182;
				GuiUtil.drawTag(g, tx, ty, sorted[c], aspects.getAmount(sorted[c]), 0, 771, 1.0F, false);
				if (mx >= tx && my >= ty && mx < tx + 16 && my < ty + 16) {
					tip(List.of(Component.literal(sorted[c].getName()), sorted[c].getLocalizedDescription()), mx, my - 8);
				}
			}
		}
		float sz = dy > 3 ? (dy - 3) * 0.2F : 0.0F;
		g.pose().pushMatrix();
		g.pose().translate(x + start + xoff * (1.0F + sz), y + 108 + yoff * (1.0F - sz));
		g.pose().scale(1.0F - sz, 1.0F - sz);
		g.pose().pushMatrix();
		g.pose().translate(-8 - xoff, -119 + Math.max(3 - dx, 3 - dz) * 8 + dx * 4 + dz * 4 + dy * 50);
		g.pose().scale(2.0F, 2.0F);
		g.blit(RenderPipelines.GUI_TEXTURED, TEX2, 0, 0, 0, 72, 64, 44, 256, 256, 0x80FFFFFF);
		g.pose().popMatrix();
		// TC4 sorted the layers with z offsets; here the bottom layer goes down first so the upper ones overlap it
		for (int j = dy - 1; j >= 0; j--) {
			int count = j * dz * dx;
			for (int k = dz - 1; k >= 0; k--) {
				for (int i = dx - 1; i >= 0; i--) {
					Cell c = cells[count++];
					if (c == null) {
						continue;
					}
					int px = i * 16 + k * 16;
					int py = -i * 8 + k * 8 + j * 50;
					if (c.fluid() != null) {
						g.blit(RenderPipelines.GUI_TEXTURED, c.fluid(), px, py, 0, 0, 16, 16, 16, 16);
					} else {
						g.item(c.stack(), px, py);
					}
				}
			}
		}
		g.pose().popMatrix();
		int count = 0;
		for (int j = 0; j < dy; j++) {
			for (int k = dz - 1; k >= 0; k--) {
				for (int i = dx - 1; i >= 0; i--) {
					Cell c = cells[count++];
					int px = (int) (x + start + xoff * (1.0F + sz) + i * 16 * (1.0F - sz) + k * 16 * (1.0F - sz));
					int py = (int) (y + 108 + yoff * (1.0F - sz) - i * 8 * (1.0F - sz) + k * 8 * (1.0F - sz) + j * 50 * (1.0F - sz));
					if (c != null && !c.stack().isEmpty() && mx >= px && my >= py && mx < px + 16.0F * (1.0F - sz) && my < py + 16.0F * (1.0F - sz)) {
						List<Component> lines = new ArrayList<>(Screen.getTooltipFromItem(this.minecraft, c.stack()));
						Object[] ref = findRecipeReference(c.stack());
						if (ref != null && !ref[0].equals(this.research.key)) {
							lines.add(Component.literal("§8§o").append(Component.translatable("recipe.clickthrough")));
							this.reference.add(new Object[]{mx, my, ref[0], ref[1]});
						}
						tip(lines, mx, my);
					}
				}
			}
		}
	}

	private void stackWithTip(GuiGraphicsExtractor g, ItemStack s, int x, int y, int mx, int my, boolean ref) {
		if (s.isEmpty()) {
			return;
		}
		g.item(s, x, y);
		g.itemDecorations(this.font, s, x, y);
		if (mx >= x && my >= y && mx < x + 16 && my < y + 16) {
			List<Component> lines = new ArrayList<>(Screen.getTooltipFromItem(this.minecraft, s));
			if (ref) {
				Object[] r = findRecipeReference(s);
				if (r != null && !r[0].equals(this.research.key)) {
					lines.add(Component.literal("§8§o").append(Component.translatable("recipe.clickthrough")));
					this.reference.add(new Object[]{mx, my, r[0], r[1]});
				}
			}
			tip(lines, mx, my);
		}
	}

	private void drawArcanePage(GuiGraphicsExtractor g, int side, int x, int y, int mx, int my, Recipe<?> r) {
		int start = side * 152;
		g.pose().pushMatrix();
		g.pose().translate(x + start, y);
		g.pose().scale(2.0F, 2.0F);
		g.blit(RenderPipelines.GUI_TEXTURED, TEX2, 2, 27, 112, 15, 52, 52, 256, 256);
		g.blit(RenderPipelines.GUI_TEXTURED, TEX2, 20, 7, 20, 3, 16, 16, 256, 256);
		g.pose().popMatrix();
		g.pose().pushMatrix();
		g.pose().translate(x + start, y + 164);
		g.pose().scale(2.0F, 2.0F);
		g.blit(RenderPipelines.GUI_TEXTURED, TEX2, 0, 0, 68, 76, 12, 12, 256, 256, 0x66FFFFFF);
		g.pose().popMatrix();
		AspectList tags = r instanceof ShapedArcaneRecipe s ? s.aspects() : ((ShapelessArcaneRecipe) r).aspects();
		int count = 0;
		for (Aspect tag : tags.getAspectsSortedAmount()) {
			int tx = x + start + 14 + 18 * count + (5 - tags.size()) * 8;
			int ty = y + 172;
			GuiUtil.drawTag(g, tx, ty, tag, tags.getAmount(tag), 0, 771, 1.0F, false);
			if (mx >= tx && my >= ty && mx < tx + 16 && my < ty + 16) {
				tip(List.of(Component.literal(tag.getName()), tag.getLocalizedDescription()), mx, my - 8);
			}
			count++;
		}
		ItemStack out = r instanceof ShapedArcaneRecipe s ? s.result().create() : ((ShapelessArcaneRecipe) r).result().create();
		stackWithTip(g, out, x + 48 + start, y + 22, mx, my, false);
		Component text = Component.translatable("recipe.type.arcane");
		g.text(this.font, text, x + start + 56 - this.font.width(text) / 2, y, 0xFF505050, false);
		if (r instanceof ShapedArcaneRecipe s) {
			int rw = s.pattern().width(), rh = s.pattern().height();
			var items = s.pattern().ingredients();
			for (int i = 0; i < rw && i < 3; i++) {
				for (int j = 0; j < rh && j < 3; j++) {
					var ing = items.get(i + j * rw);
					if (ing.isPresent()) {
						stackWithTip(g, cycleIngredient(ing.get()), x + start + 16 + i * 32, y + 66 + j * 32, mx, my, true);
					}
				}
			}
		} else {
			List<Ingredient> items = ((ShapelessArcaneRecipe) r).ingredients();
			for (int i = 0; i < items.size() && i < 9; i++) {
				stackWithTip(g, cycleIngredient(items.get(i)), x + start + 16 + i % 3 * 32, y + 66 + i / 3 * 32, mx, my, true);
			}
		}
	}

	private void drawCraftingPage(GuiGraphicsExtractor g, int side, int x, int y, int mx, int my, Recipe<?> r) {
		int start = side * 152;
		g.pose().pushMatrix();
		g.pose().translate(x + start, y);
		g.pose().scale(2.0F, 2.0F);
		g.blit(RenderPipelines.GUI_TEXTURED, TEX2, 2, 32, 60, 15, 52, 52, 256, 256);
		g.blit(RenderPipelines.GUI_TEXTURED, TEX2, 20, 12, 20, 3, 16, 16, 256, 256);
		g.pose().popMatrix();
		stackWithTip(g, ObjectAspects.resultOf(r), x + 48 + start, y + 32, mx, my, false);
		if (r instanceof ShapedRecipe s) {
			Component text = Component.translatable("recipe.type.workbench");
			g.text(this.font, text, x + start + 56 - this.font.width(text) / 2, y, 0xFF505050, false);
			int rw = s.getWidth(), rh = s.getHeight();
			var items = s.getIngredients();
			for (int i = 0; i < rw && i < 3; i++) {
				for (int j = 0; j < rh && j < 3; j++) {
					var ing = items.get(i + j * rw);
					if (ing.isPresent()) {
						stackWithTip(g, cycleIngredient(ing.get()), x + start + 16 + i * 32, y + 76 + j * 32, mx, my, true);
					}
				}
			}
		} else if (r instanceof ShapelessRecipe) {
			Component text = Component.translatable("recipe.type.workbenchshapeless");
			g.text(this.font, text, x + start + 56 - this.font.width(text) / 2, y, 0xFF505050, false);
			List<Ingredient> items = r.placementInfo().ingredients();
			for (int i = 0; i < items.size() && i < 9; i++) {
				stackWithTip(g, cycleIngredient(items.get(i)), x + start + 16 + i % 3 * 32, y + 76 + i / 3 * 32, mx, my, true);
			}
		}
	}

	private void drawCruciblePage(GuiGraphicsExtractor g, int side, int x, int y, int mx, int my, CrucibleRecipe rc) {
		int start = side * 152;
		Component text = Component.translatable("recipe.type.crucible");
		g.text(this.font, text, x + start + 56 - this.font.width(text) / 2, y, 0xFF505050, false);
		g.pose().pushMatrix();
		g.pose().translate(x + start, y + 28);
		g.pose().scale(2.0F, 2.0F);
		g.blit(RenderPipelines.GUI_TEXTURED, TEX2, 0, 0, 0, 3, 56, 17, 256, 256);
		g.pose().translate(0.0F, 32.0F);
		g.blit(RenderPipelines.GUI_TEXTURED, TEX2, 0, 0, 0, 20, 56, 48, 256, 256);
		g.pose().translate(21.0F, -8.0F);
		g.blit(RenderPipelines.GUI_TEXTURED, TEX2, 0, 0, 100, 84, 11, 13, 256, 256);
		g.pose().popMatrix();
		int total = 0;
		int rows = (rc.aspects().size() - 1) / 3;
		int shift = (3 - rc.aspects().size() % 3) * 10;
		int sx = x + start + 28;
		int sy = y + 96 + 32 - 10 * rows;
		for (Aspect tag : rc.aspects().getAspectsSorted()) {
			int m = total / 3 >= rows && (rows > 1 || rc.aspects().size() < 3) ? 1 : 0;
			int vx = sx + total % 3 * 20 + shift * m;
			int vy = sy + total / 3 * 20;
			GuiUtil.drawTag(g, vx, vy, tag, rc.aspects().getAmount(tag), 0, 771, 1.0F, false);
			if (mx >= vx && my >= vy && mx < vx + 16 && my < vy + 16) {
				tip(List.of(Component.literal(tag.getName()), tag.getLocalizedDescription()), mx, my);
			}
			total++;
		}
		stackWithTip(g, rc.result().create(), x + 48 + start, y + 36, mx, my, false);
		stackWithTip(g, cycleIngredient(rc.catalyst()), x + 26 + start, y + 72, mx, my, true);
	}

	private void drawSmeltingPage(GuiGraphicsExtractor g, int side, int x, int y, int mx, int my, SmeltingRecipe sr) {
		int start = side * 152;
		Component text = Component.translatable("recipe.type.smelting");
		g.text(this.font, text, x + start + 56 - this.font.width(text) / 2, y, 0xFF505050, false);
		g.pose().pushMatrix();
		g.pose().translate(x + start, y + 28);
		g.pose().scale(2.0F, 2.0F);
		g.blit(RenderPipelines.GUI_TEXTURED, TEX2, 0, 0, 0, 192, 56, 64, 256, 256);
		g.pose().popMatrix();
		stackWithTip(g, cycleIngredient(sr.input()), x + 48 + start, y + 64, mx, my, true);
		stackWithTip(g, ObjectAspects.resultOf(sr), x + 48 + start, y + 144, mx, my, false);
	}

	/** The essentia cost in rows of five along the bottom of an infusion page. */
	private void drawInfusionAspects(GuiGraphicsExtractor g, int x, int start, int y, int mx, int my, AspectList aspects, int mult) {
		int total = 0;
		int rows = (aspects.size() - 1) / 5;
		int shift = (5 - aspects.size() % 5) * 10;
		int sx = x + start + 8;
		int sy = y + 164 - 10 * rows;
		for (Aspect tag : aspects.getAspectsSorted()) {
			int m = total / 5 >= rows && (rows > 1 || aspects.size() < 5) ? 1 : 0;
			int vx = sx + total % 5 * 20 + shift * m;
			int vy = sy + total / 5 * 20;
			GuiUtil.drawTag(g, vx, vy, tag, aspects.getAmount(tag) * mult, 0, 771, 1.0F, false);
			if (mx >= vx && my >= vy && mx < vx + 16 && my < vy + 16) {
				tip(List.of(Component.literal(tag.getName()), tag.getLocalizedDescription()), mx, my);
			}
			total++;
		}
	}

	/** The components set around the central item in a ring of radius 40. */
	private void drawInfusionComponents(GuiGraphicsExtractor g, int x, int start, int y, int mx, int my, List<Ingredient> components) {
		int le = components.size();
		if (le == 0) {
			return;
		}
		float pieSlice = 360 / le;
		float currentRot = -90.0F;
		int sx = x + 56 + start;
		int sy = y + 102;
		for (Ingredient ing : components) {
			int vx = sx + (int) (Mth.cos(currentRot / 180.0F * (float) Math.PI) * 40.0F) - 8;
			int vy = sy + (int) (Mth.sin(currentRot / 180.0F * (float) Math.PI) * 40.0F) - 8;
			currentRot += pieSlice;
			stackWithTip(g, cycleIngredient(ing), vx, vy, mx, my, true);
		}
	}

	private void drawInstability(GuiGraphicsExtractor g, int x, int start, int y, int instability) {
		int inst = Math.min(5, instability / 2);
		Component text = Component.translatable("tc.inst").append(" ").append(Component.translatable("tc.inst." + inst));
		g.text(this.font, text, x + start + 56 - this.font.width(text) / 2, y + 194, 0xFF505050, false);
	}

	/** GuiResearchRecipe.drawInfusionPage. */
	private void drawInfusionPage(GuiGraphicsExtractor g, int side, int x, int y, int mx, int my, InfusionRecipe ri) {
		int start = side * 152;
		Component text = Component.translatable("recipe.type.infusion");
		g.text(this.font, text, x + start + 56 - this.font.width(text) / 2, y, 0xFF505050, false);
		drawInstability(g, x, start, y, ri.instability());
		g.pose().pushMatrix();
		g.pose().translate(x + start, y + 20);
		g.pose().scale(2.0F, 2.0F);
		g.blit(RenderPipelines.GUI_TEXTURED, TEX2, 0, 0, 0, 3, 56, 17, 256, 256);
		g.pose().translate(0.0F, 19.0F);
		g.blit(RenderPipelines.GUI_TEXTURED, TEX2, 0, 0, 200, 77, 60, 44, 256, 256);
		g.pose().popMatrix();
		drawInfusionAspects(g, x, start, y, mx, my, ri.aspects(), 1);
		stackWithTip(g, ri.result().create(), x + 48 + start, y + 28, mx, my, false);
		stackWithTip(g, cycleIngredient(ri.central()), x + 48 + start, y + 94, mx, my, true);
		drawInfusionComponents(g, x, start, y, mx, my, ri.components());
	}

	/** GuiResearchRecipe.drawInfusionEnchantingPage: the enchantment, its experience cost and essentia at each level in turn. */
	private void drawInfusionEnchantingPage(GuiGraphicsExtractor g, int side, int x, int y, int mx, int my, InfusionEnchantmentRecipe ri) {
		int start = side * 152;
		var ench = ri.enchantment();
		int level = (int) (1L + System.currentTimeMillis() / 1000L % ench.value().getMaxLevel());
		Component text = Component.translatable("recipe.type.infusionenchantment");
		g.text(this.font, text, x + start + 56 - this.font.width(text) / 2, y, 0xFF505050, false);
		drawInstability(g, x, start, y, ri.instability());
		Component name = net.minecraft.world.item.enchantment.Enchantment.getFullname(ench, level);
		g.text(this.font, name.getString(), x + start + 56 - this.font.width(name.getString()) / 2, y + 24, 0xFF705090, false);
		int xp = Math.max(1, ench.value().getMinCost(1) / 3) * level;
		String levels = xp + " levels";
		g.text(this.font, levels, x + start + 56 - this.font.width(levels) / 2, y + 40, 0xFF508850, false);
		g.pose().pushMatrix();
		g.pose().translate(x + start, y + 20);
		g.pose().scale(2.0F, 2.0F);
		g.pose().translate(0.0F, 19.0F);
		g.blit(RenderPipelines.GUI_TEXTURED, TEX2, 0, 0, 200, 77, 60, 44, 256, 256);
		g.pose().popMatrix();
		drawInfusionAspects(g, x, start, y, mx, my, ri.aspects(), level);
		drawInfusionComponents(g, x, start, y, mx, my, ri.components());
	}

	/** ThaumcraftApi.getCraftingRecipeKey: which research page shows how to make this item. */
	private static Object[] findRecipeReference(ItemStack item) {
		if (recipeKeys == null) {
			recipeKeys = new HashMap<>();
			for (ResearchItem ri : ResearchManager.entries()) {
				for (int p = 0; p < ri.pages.size(); p++) {
					for (Identifier id : ri.pages.get(p).recipes()) {
						Recipe<?> r = TVClientNetwork.RECIPES.get(id);
						if (r != null) {
							ItemStack out = r instanceof CrucibleRecipe cr ? cr.result().create() : ObjectAspects.resultOf(r);
							if (!out.isEmpty()) {
								recipeKeys.putIfAbsent(out.getItem(), new Object[]{ri.key, p});
							}
						}
					}
				}
			}
		}
		Object[] r = recipeKeys.get(item.getItem());
		if (r == null) {
			return null;
		}
		var mc = net.minecraft.client.Minecraft.getInstance();
		return ResearchManager.isResearchComplete(mc.player, (String) r[0]) ? r : null;
	}

	public static void invalidateReferences() {
		recipeKeys = null;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		int par1 = (int) event.x(), par2 = (int) event.y();
		int var4 = (this.width - this.paneWidth) / 2;
		int var5 = (this.height - this.paneHeight) / 2;
		int mx = par1 - (var4 + 261);
		int my = par2 - (var5 + 189);
		if (this.page < this.maxPages - 2 && mx >= 0 && my >= 0 && mx < 14 && my < 10) {
			this.page += 2;
			this.lastCycle = 0L;
			this.cycle = -1;
			this.minecraft.player.playSound(TVSounds.PAGE, 0.66F, 1.0F);
		}
		mx = par1 - (var4 - 17);
		my = par2 - (var5 + 189);
		if (this.page >= 2 && mx >= 0 && my >= 0 && mx < 14 && my < 10) {
			this.page -= 2;
			this.lastCycle = 0L;
			this.cycle = -1;
			this.minecraft.player.playSound(TVSounds.PAGE, 0.66F, 1.0F);
		}
		if (!HISTORY.isEmpty()) {
			mx = par1 - (var4 + 118);
			my = par2 - (var5 + 189);
			if (mx >= 0 && my >= 0 && mx < 20 && my < 12) {
				this.minecraft.player.playSound(TVSounds.PAGE, 0.66F, 1.0F);
				Object[] o = HISTORY.pop();
				this.minecraft.gui.setScreen(new ResearchPagesScreen(ResearchManager.getResearch((String) o[0]), (Integer) o[1], this.guiMapX, this.guiMapY));
				return true;
			}
		}
		for (Object[] c : this.reference) {
			if (par1 >= (Integer) c[0] && par2 >= (Integer) c[1] && par1 < (Integer) c[0] + 16 && par2 < (Integer) c[1] + 16) {
				this.minecraft.player.playSound(TVSounds.PAGE, 0.66F, 1.0F);
				HISTORY.push(new Object[]{this.research.key, this.page});
				this.minecraft.gui.setScreen(new ResearchPagesScreen(ResearchManager.getResearch((String) c[2]), (Integer) c[3], this.guiMapX, this.guiMapY));
				return true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	static TooltipFlag flag() {
		return TooltipFlag.NORMAL;
	}
}
