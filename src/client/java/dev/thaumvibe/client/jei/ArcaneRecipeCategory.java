package dev.thaumvibe.client.jei;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.client.gui.GuiUtil;
import dev.thaumvibe.crafting.ArcaneRecipe;
import dev.thaumvibe.crafting.ShapedArcaneRecipe;
import dev.thaumvibe.crafting.ShapelessArcaneRecipe;
import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.research.ResearchItem;
import dev.thaumvibe.research.ResearchManager;
import java.util.Optional;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;

/** JEI Category for Thaumcraft 4 Arcane Workbench crafting. */
public class ArcaneRecipeCategory extends AbstractRecipeCategory<ArcaneRecipe> {
	private static final Aspect[] PRIMALS = {
		Aspect.AIR, Aspect.FIRE, Aspect.WATER, Aspect.EARTH, Aspect.ORDER, Aspect.ENTROPY
	};

	public ArcaneRecipeCategory(IGuiHelper helper) {
		super(ThaumvibeJeiPlugin.ARCANE, Component.translatable("recipe.type.arcane"),
			helper.createDrawableItemLike(TVBlocks.ARCANE_WORKTABLE), 162, 70);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ArcaneRecipe recipe, IFocusGroup focuses) {
		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 3; c++) {
				var slot = builder.addInputSlot(4 + c * 18, 8 + r * 18).setStandardSlotBackground();
				if (recipe instanceof ShapedArcaneRecipe shaped) {
					int w = shaped.pattern().width();
					int h = shaped.pattern().height();
					if (c < w && r < h) {
						Optional<Ingredient> opt = shaped.pattern().ingredients().get(c + r * w);
						opt.ifPresent(slot::add);
					}
				} else if (recipe instanceof ShapelessArcaneRecipe shapeless) {
					int index = c + r * 3;
					if (index < shapeless.ingredients().size()) {
						slot.add(shapeless.ingredients().get(index));
					}
				}
			}
		}

		if (recipe instanceof ShapelessArcaneRecipe) {
			builder.setShapeless();
		}

		builder.addOutputSlot(88, 26).setOutputSlotBackground().add(recipe.result());
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder extras, ArcaneRecipe recipe, IFocusGroup focuses) {
		extras.addRecipeArrowWidget().setPosition(60, 26);
	}

	@Override
	public void draw(ArcaneRecipe recipe, IRecipeSlotsView slotsView, GuiGraphicsExtractor g, double mouseX, double mouseY) {
		AspectList aspects = recipe.aspects();
		for (int i = 0; i < PRIMALS.length; i++) {
			Aspect p = PRIMALS[i];
			int col = i % 2;
			int row = i / 2;
			int px = 122 + col * 19;
			int py = 8 + row * 19;
			int amt = aspects != null ? aspects.getAmount(p) : 0;
			if (amt > 0) {
				GuiUtil.drawTag(g, px, py, p, amt, 0, 771, 1.0F, false);
			} else {
				GuiUtil.drawTag(g, px, py, p, 0, 0, 771, 0.25F, true);
			}
		}
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, ArcaneRecipe recipe, IRecipeSlotsView slotsView, double mouseX, double mouseY) {
		AspectList aspects = recipe.aspects();
		for (int i = 0; i < PRIMALS.length; i++) {
			Aspect p = PRIMALS[i];
			int col = i % 2;
			int row = i / 2;
			int px = 122 + col * 19;
			int py = 8 + row * 19;
			if (mouseX >= px && mouseX < px + 16 && mouseY >= py && mouseY < py + 16) {
				int amt = aspects != null ? aspects.getAmount(p) : 0;
				tooltip.add(p.getDisplayName().copy().append(Component.literal(": " + amt + " Vis")));
				tooltip.add(p.getLocalizedDescription());
				return;
			}
		}

		if (recipe.research() != null && !recipe.research().isEmpty()) {
			ResearchItem ri = ResearchManager.getResearch(recipe.research());
			Component rName = ri != null ? ri.getName() : Component.literal(recipe.research());
			tooltip.add(Component.translatable("tc.research").append(": ").append(rName).withStyle(ChatFormatting.GOLD));
		}
	}
}
