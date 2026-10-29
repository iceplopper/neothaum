package dev.neothaum.client.rei;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.client.gui.GuiUtil;
import dev.neothaum.crafting.ArcaneRecipe;
import dev.neothaum.crafting.CrucibleRecipe;
import dev.neothaum.crafting.InfusionEnchantmentRecipe;
import dev.neothaum.crafting.InfusionRecipe;
import dev.neothaum.crafting.ShapelessArcaneRecipe;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.research.PlayerKnowledge;
import dev.neothaum.research.ResearchItem;
import dev.neothaum.research.ResearchManager;
import java.util.ArrayList;
import java.util.List;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;

/** REI pages for NeoThaum's recipes, laid out as the JEI plugin's (dev.neothaum.client.jei) so both viewers look alike. */
final class ReiCategories {
	/** A page with JEI's size plus REI's 4-pixel border; positions below are JEI's, offset by that border. */
	private abstract static class Base<D extends me.shedaniel.rei.api.common.display.Display> implements DisplayCategory<D> {
		private final CategoryIdentifier<D> id;
		private final Component title;
		private final ItemLike icon;
		private final int width;
		private final int height;

		Base(CategoryIdentifier<D> id, String titleKey, ItemLike icon, int width, int height) {
			this.id = id;
			this.title = Component.translatable(titleKey);
			this.icon = icon;
			this.width = width;
			this.height = height;
		}

		@Override
		public CategoryIdentifier<? extends D> getCategoryIdentifier() {
			return this.id;
		}

		@Override
		public Component getTitle() {
			return this.title;
		}

		@Override
		public Renderer getIcon() {
			return EntryStacks.of(this.icon);
		}

		@Override
		public int getDisplayWidth(D display) {
			return this.width + 8;
		}

		@Override
		public int getDisplayHeight() {
			return this.height + 8;
		}

		@Override
		public List<Widget> setupDisplay(D display, Rectangle bounds) {
			List<Widget> w = new ArrayList<>();
			w.add(Widgets.createRecipeBase(bounds));
			this.build(display, bounds.x + 4, bounds.y + 4, w);
			return w;
		}

		abstract void build(D display, int x, int y, List<Widget> w);
	}

	static void input(List<Widget> w, int x, int y, EntryIngredient entries) {
		w.add(Widgets.createSlot(new Point(x, y)).entries(entries).markInput());
	}

	static void output(List<Widget> w, int x, int y, EntryIngredient entries) {
		w.add(Widgets.createResultSlotBackground(new Point(x, y)));
		w.add(Widgets.createSlot(new Point(x, y)).entries(entries).disableBackground().markOutput());
	}

	/** A row of aspect tags with their amounts, wrapping after perRow, centred on cx; hovering one names it. */
	static void aspects(List<Widget> w, AspectList al, int cx, int top, int perRow, int minX) {
		if (al == null || al.size() == 0) {
			return;
		}
		Aspect[] sorted = al.getAspectsSorted();
		int cols = Math.min(sorted.length, perRow);
		int startX = Math.max(minX, cx - (cols * 20) / 2);
		for (int i = 0; i < sorted.length; i++) {
			Aspect a = sorted[i];
			int vx = startX + (i % perRow) * 20;
			int vy = top + (i / perRow) * 18;
			int amt = al.getAmount(a);
			w.add(Widgets.createDrawableWidget((g, mx, my, d) -> GuiUtil.drawTag(g, vx, vy, a, amt, 0, 771, 1.0F, false)));
			w.add(Widgets.createTooltip(new Rectangle(vx, vy, 16, 16), a.getDisplayName().copy().append(Component.literal(": " + amt)),
				a.getLocalizedDescription()));
		}
	}

	/** The research that unlocks a recipe, shown when hovering anywhere else on the page. */
	static void research(List<Widget> w, String key, int x, int y, int width, int height) {
		if (key == null || key.isEmpty()) {
			return;
		}
		ResearchItem ri = ResearchManager.getResearch(key);
		Component name = ri != null ? ri.getName() : Component.literal(key);
		Component line = Component.translatable("tc.research").append(": ").append(name).withStyle(ChatFormatting.GOLD);
		w.add(Widgets.createTooltip(p -> p.x >= x && p.x < x + width && p.y >= y && p.y < y + height ? Tooltip.create(p, line) : null));
	}

	static final class Arcane extends Base<RecipeDisplay> {
		private static final Aspect[] PRIMALS = {Aspect.AIR, Aspect.FIRE, Aspect.WATER, Aspect.EARTH, Aspect.ORDER, Aspect.ENTROPY};

		Arcane() {
			super(NeoThaumReiPlugin.ARCANE, "recipe.type.arcane", TVBlocks.ARCANE_WORKTABLE, 162, 70);
		}

		@Override
		void build(RecipeDisplay d, int x, int y, List<Widget> w) {
			var in = d.getInputEntries();
			for (int i = 0; i < 9; i++) {
				input(w, x + 4 + (i % 3) * 18, y + 8 + (i / 3) * 18, i < in.size() ? in.get(i) : EntryIngredient.empty());
			}
			w.add(Widgets.createArrow(new Point(x + 60, y + 26)));
			output(w, x + 88, y + 26, d.getOutputEntries().getFirst());
			if (d.recipe instanceof ShapelessArcaneRecipe) {
				w.add(Widgets.createLabel(new Point(x + 70, y + 46), Component.translatable("recipe.type.workbenchshapeless")).noShadow()
					.color(0xFF404040).centered());
			}
			ArcaneRecipe recipe = (ArcaneRecipe) d.recipe;
			AspectList al = recipe.aspects();
			for (int i = 0; i < PRIMALS.length; i++) {
				Aspect p = PRIMALS[i];
				int px = x + 122 + (i % 2) * 19;
				int py = y + 8 + (i / 2) * 19;
				int amt = al != null ? al.getAmount(p) : 0;
				w.add(Widgets.createDrawableWidget((g, mx, my, dt) -> GuiUtil.drawTag(g, px, py, p, amt, 0, 771, amt > 0 ? 1.0F : 0.25F, amt <= 0)));
				w.add(Widgets.createTooltip(new Rectangle(px, py, 16, 16), p.getDisplayName().copy().append(Component.literal(": " + amt + " Vis")),
					p.getLocalizedDescription()));
			}
			research(w, recipe.research(), x, y, 118, 70);
		}
	}

	static final class Crucible extends Base<RecipeDisplay> {
		Crucible() {
			super(NeoThaumReiPlugin.CRUCIBLE, "recipe.type.crucible", TVBlocks.CRUCIBLE, 162, 78);
		}

		@Override
		void build(RecipeDisplay d, int x, int y, List<Widget> w) {
			CrucibleRecipe recipe = (CrucibleRecipe) d.recipe;
			input(w, x + 28, y + 12, d.getInputEntries().getFirst());
			w.add(Widgets.createArrow(new Point(x + 62, y + 12)));
			output(w, x + 106, y + 8, d.getOutputEntries().getFirst());
			aspects(w, recipe.aspects(), x + 81, y + 42, 7, x + 6);
			research(w, recipe.research(), x, y, 162, 40);
		}
	}

	/**
	 * The central pedestal and a ring of component pedestals, the instability, and the essentia below. Laid out 8 pixels lower than JEI's page,
	 * so the enchantment name clears the top pedestal and the instability line clears the bottom one.
	 */
	private abstract static class Infusion extends Base<RecipeDisplay> {
		private static final int RING_Y = 50;

		Infusion(CategoryIdentifier<RecipeDisplay> id, String title) {
			super(id, title, TVBlocks.RUNIC_MATRIX, 168, 146);
		}

		void ring(RecipeDisplay d, int x, int y, List<Widget> w, int instability, AspectList al, String research) {
			var in = d.getInputEntries();
			input(w, x + 42, y + RING_Y - 8, in.getFirst());
			int count = in.size() - 1;
			for (int i = 0; i < count; i++) {
				double angle = -Math.PI / 2.0 + i * (2.0 * Math.PI / count);
				input(w, x + 50 + (int) Math.round(Math.cos(angle) * 30.0) - 8, y + RING_Y + (int) Math.round(Math.sin(angle) * 30.0) - 8, in.get(i + 1));
			}
			w.add(Widgets.createArrow(new Point(x + 94, y + RING_Y - 8)));
			output(w, x + 130, y + RING_Y - 12, d.getOutputEntries().getFirst());
			Component inst = Component.translatable("tc.inst").append(Component.translatable("tc.inst." + Math.min(5, instability / 2)));
			w.add(Widgets.createLabel(new Point(x + 84, y + RING_Y + 42), inst).noShadow().color(0xFF404040).centered());
			aspects(w, al, x + 84, y + RING_Y + 56, 8, x + 4);
			research(w, research, x, y, 168, RING_Y + 52);
		}
	}

	static final class InfusionPage extends Infusion {
		InfusionPage() {
			super(NeoThaumReiPlugin.INFUSION, "recipe.type.infusion");
		}

		@Override
		void build(RecipeDisplay d, int x, int y, List<Widget> w) {
			InfusionRecipe recipe = (InfusionRecipe) d.recipe;
			this.ring(d, x, y, w, recipe.instability(), recipe.aspects(), recipe.research());
		}
	}

	static final class InfusionEnchantmentPage extends Infusion {
		InfusionEnchantmentPage() {
			super(NeoThaumReiPlugin.INFUSION_ENCHANTMENT, "recipe.type.infusionenchantment");
		}

		@Override
		void build(RecipeDisplay d, int x, int y, List<Widget> w) {
			InfusionEnchantmentRecipe recipe = (InfusionEnchantmentRecipe) d.recipe;
			w.add(Widgets.createLabel(new Point(x + 84, y + 1), Enchantment.getFullname(recipe.enchantment(), 1).copy().withColor(0x705090)).noShadow()
				.centered());
			this.ring(d, x, y, w, recipe.instability(), recipe.aspects(), recipe.research());
		}
	}

	/** A compound aspect = its two components; undiscovered ones are greyed and unnamed, as in JEI's page. */
	static final class AspectPage extends Base<AspectDisplay> {
		AspectPage() {
			super(NeoThaumReiPlugin.ASPECT_DECOMP, "recipe.type.aspect_decomp", dev.neothaum.registry.TVItems.THAUMOMETER, 162, 40);
		}

		@Override
		void build(AspectDisplay d, int x, int y, List<Widget> w) {
			var player = Minecraft.getInstance().player;
			PlayerKnowledge k = player != null ? ResearchManager.knowledge(player) : null;
			boolean known = k == null || k.hasDiscoveredAspect(d.aspect);
			this.tag(w, x + 16, y + 12, d.aspect, known, "Undiscovered Aspect");
			w.add(Widgets.createArrow(new Point(x + 46, y + 12)));
			this.tag(w, x + 80, y + 12, d.component1, known && (k == null || k.hasDiscoveredAspect(d.component1)), "Undiscovered Component");
			w.add(Widgets.createLabel(new Point(x + 102, y + 16), Component.literal("+")).noShadow().color(0xFF888888).leftAligned());
			this.tag(w, x + 114, y + 12, d.component2, known && (k == null || k.hasDiscoveredAspect(d.component2)), "Undiscovered Component");
		}

		private void tag(List<Widget> w, int x, int y, Aspect a, boolean known, String unknownText) {
			w.add(Widgets.createDrawableWidget((g, mx, my, dt) -> GuiUtil.drawTag(g, x, y, a, 0.0F, 0, 771, known ? 1.0F : 0.25F, !known)));
			if (known) {
				w.add(Widgets.createTooltip(new Rectangle(x, y, 16, 16), a.getDisplayName().copy().withColor(a.getColor()), a.getLocalizedDescription()));
			} else {
				w.add(Widgets.createTooltip(new Rectangle(x, y, 16, 16), Component.literal("§7??? (" + unknownText + ")")));
			}
		}
	}

	private ReiCategories() {
	}
}
