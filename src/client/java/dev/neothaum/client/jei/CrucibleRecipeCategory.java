package dev.neothaum.client.jei;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.client.gui.GuiUtil;
import dev.neothaum.crafting.CrucibleRecipe;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.research.ResearchItem;
import dev.neothaum.research.ResearchManager;
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

/** JEI Category for Thaumcraft 4 Crucible alchemy. */
public class CrucibleRecipeCategory extends AbstractRecipeCategory<CrucibleRecipe> {

	public CrucibleRecipeCategory(IGuiHelper helper) {
		super(NeoThaumJeiPlugin.CRUCIBLE, Component.translatable("recipe.type.crucible"),
			helper.createDrawableItemLike(TVBlocks.CRUCIBLE), 162, 78);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, CrucibleRecipe recipe, IFocusGroup focuses) {
		builder.addInputSlot(28, 12).setStandardSlotBackground().add(recipe.catalyst());
		builder.addOutputSlot(106, 8).setOutputSlotBackground().add(recipe.result());
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder extras, CrucibleRecipe recipe, IFocusGroup focuses) {
		extras.addRecipeArrowWidget().setPosition(62, 12);
	}

	@Override
	public void draw(CrucibleRecipe recipe, IRecipeSlotsView slotsView, GuiGraphicsExtractor g, double mouseX, double mouseY) {
		if (recipe.aspects() == null) {
			return;
		}
		Aspect[] aspects = recipe.aspects().getAspectsSorted();
		int count = aspects.length;
		if (count == 0) {
			return;
		}
		int cols = Math.min(count, 7);
		int startX = Math.max(6, 81 - (cols * 20) / 2);
		for (int i = 0; i < count; i++) {
			int vx = startX + (i % 7) * 20;
			int vy = 42 + (i / 7) * 18;
			int amt = recipe.aspects().getAmount(aspects[i]);
			GuiUtil.drawTag(g, vx, vy, aspects[i], amt, 0, 771, 1.0F, false);
		}
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, CrucibleRecipe recipe, IRecipeSlotsView slotsView, double mouseX, double mouseY) {
		if (recipe.aspects() != null) {
			Aspect[] aspects = recipe.aspects().getAspectsSorted();
			int count = aspects.length;
			int cols = Math.min(count, 7);
			int startX = Math.max(6, 81 - (cols * 20) / 2);
			for (int i = 0; i < count; i++) {
				int vx = startX + (i % 7) * 20;
				int vy = 42 + (i / 7) * 18;
				if (mouseX >= vx && mouseX < vx + 16 && mouseY >= vy && mouseY < vy + 16) {
					Aspect a = aspects[i];
					int amt = recipe.aspects().getAmount(a);
					tooltip.add(a.getDisplayName().copy().append(Component.literal(": " + amt)));
					tooltip.add(a.getLocalizedDescription());
					return;
				}
			}
		}

		if (recipe.research() != null && !recipe.research().isEmpty()) {
			ResearchItem ri = ResearchManager.getResearch(recipe.research());
			Component rName = ri != null ? ri.getName() : Component.literal(recipe.research());
			tooltip.add(Component.translatable("tc.research").append(": ").append(rName).withStyle(ChatFormatting.GOLD));
		}
	}
}
