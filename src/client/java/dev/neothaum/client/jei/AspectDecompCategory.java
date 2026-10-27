package dev.neothaum.client.jei;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.client.gui.GuiUtil;
import dev.neothaum.registry.TVItems;
import dev.neothaum.research.PlayerKnowledge;
import dev.neothaum.research.ResearchManager;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class AspectDecompCategory extends AbstractRecipeCategory<AspectDecompRecipe> {

	public AspectDecompCategory(IGuiHelper helper) {
		super(NeoThaumJeiPlugin.ASPECT_DECOMP, Component.translatable("recipe.type.aspect_decomp"),
			helper.createDrawableItemLike(TVItems.THAUMOMETER), 150, 44);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, AspectDecompRecipe recipe, IFocusGroup focuses) {
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder extras, AspectDecompRecipe recipe, IFocusGroup focuses) {
		extras.addRecipeArrowWidget().setPosition(46, 12);
	}

	@Override
	public void draw(AspectDecompRecipe recipe, IRecipeSlotsView slotsView, GuiGraphicsExtractor g, double mouseX, double mouseY) {
		var mc = Minecraft.getInstance();
		var player = mc.player;
		PlayerKnowledge k = player != null ? ResearchManager.knowledge(player) : null;
		boolean targetUnlocked = k == null || k.hasDiscoveredAspect(recipe.aspect());
		boolean c1Unlocked = k == null || k.hasDiscoveredAspect(recipe.component1());
		boolean c2Unlocked = k == null || k.hasDiscoveredAspect(recipe.component2());

		// Target aspect
		if (targetUnlocked) {
			GuiUtil.drawTag(g, 16, 12, recipe.aspect());
		} else {
			GuiUtil.drawTag(g, 16, 12, recipe.aspect(), 0.0F, 0, 771, 0.25F, true);
		}

		// Component 1
		if (targetUnlocked && c1Unlocked) {
			GuiUtil.drawTag(g, 80, 12, recipe.component1());
		} else {
			GuiUtil.drawTag(g, 80, 12, recipe.component1(), 0.0F, 0, 771, 0.25F, true);
		}

		// Plus sign
		g.text(mc.font, "+", 102, 16, 0xFF888888, false);

		// Component 2
		if (targetUnlocked && c2Unlocked) {
			GuiUtil.drawTag(g, 114, 12, recipe.component2());
		} else {
			GuiUtil.drawTag(g, 114, 12, recipe.component2(), 0.0F, 0, 771, 0.25F, true);
		}
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, AspectDecompRecipe recipe, IRecipeSlotsView slotsView, double mouseX, double mouseY) {
		var player = Minecraft.getInstance().player;
		PlayerKnowledge k = player != null ? ResearchManager.knowledge(player) : null;
		boolean targetUnlocked = k == null || k.hasDiscoveredAspect(recipe.aspect());
		boolean c1Unlocked = k == null || k.hasDiscoveredAspect(recipe.component1());
		boolean c2Unlocked = k == null || k.hasDiscoveredAspect(recipe.component2());

		// Check hover on target aspect (x: 16..32, y: 12..28)
		if (mouseX >= 16 && mouseX < 32 && mouseY >= 12 && mouseY < 28) {
			if (targetUnlocked) {
				tooltip.add(recipe.aspect().getDisplayName().copy().withColor(recipe.aspect().getColor()));
				tooltip.add(recipe.aspect().getLocalizedDescription());
			} else {
				tooltip.add(Component.literal("§7??? (Undiscovered Aspect)"));
				tooltip.add(Component.literal("§cRequires scanning or research discovery").withStyle(ChatFormatting.ITALIC));
			}
			return;
		}

		// Check hover on Component 1 (x: 80..96, y: 12..28)
		if (mouseX >= 80 && mouseX < 96 && mouseY >= 12 && mouseY < 28) {
			if (targetUnlocked && c1Unlocked) {
				tooltip.add(recipe.component1().getDisplayName().copy().withColor(recipe.component1().getColor()));
				tooltip.add(recipe.component1().getLocalizedDescription());
			} else {
				tooltip.add(Component.literal("§7??? (Undiscovered Component)"));
				tooltip.add(Component.literal("§cAspect combination not yet deciphered").withStyle(ChatFormatting.ITALIC));
			}
			return;
		}

		// Check hover on Component 2 (x: 114..130, y: 12..28)
		if (mouseX >= 114 && mouseX < 130 && mouseY >= 12 && mouseY < 28) {
			if (targetUnlocked && c2Unlocked) {
				tooltip.add(recipe.component2().getDisplayName().copy().withColor(recipe.component2().getColor()));
				tooltip.add(recipe.component2().getLocalizedDescription());
			} else {
				tooltip.add(Component.literal("§7??? (Undiscovered Component)"));
				tooltip.add(Component.literal("§cAspect combination not yet deciphered").withStyle(ChatFormatting.ITALIC));
			}
			return;
		}
	}
}
