package dev.thaumvibe.client.jei;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.client.gui.GuiUtil;
import dev.thaumvibe.crafting.InfusionRecipe;
import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.research.ResearchItem;
import dev.thaumvibe.research.ResearchManager;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;

/** JEI Category for Thaumcraft 4 Arcane Infusion crafting. */
public class InfusionRecipeCategory extends AbstractRecipeCategory<InfusionRecipe> {

	public InfusionRecipeCategory(IGuiHelper helper) {
		super(ThaumvibeJeiPlugin.INFUSION, Component.translatable("recipe.type.infusion"),
			helper.createDrawableItemLike(TVBlocks.RUNIC_MATRIX), 168, 134);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, InfusionRecipe recipe, IFocusGroup focuses) {
		// Central pedestal slot: center (cx, cy) = (50, 42)
		builder.addInputSlot(42, 34).setStandardSlotBackground().add(recipe.central());

		// Outer pedestal component slots arranged in a ring
		List<Ingredient> components = recipe.components();
		int count = components.size();
		if (count > 0) {
			double radius = 30.0;
			double angleStep = (2.0 * Math.PI) / count;
			double startAngle = -Math.PI / 2.0;
			for (int i = 0; i < count; i++) {
				double angle = startAngle + i * angleStep;
				int vx = 50 + (int) Math.round(Math.cos(angle) * radius) - 8;
				int vy = 42 + (int) Math.round(Math.sin(angle) * radius) - 8;
				builder.addInputSlot(vx, vy).setStandardSlotBackground().add(components.get(i));
			}
		}

		// Output slot
		builder.addOutputSlot(130, 30).setOutputSlotBackground().add(recipe.result());
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder extras, InfusionRecipe recipe, IFocusGroup focuses) {
		extras.addRecipeArrowWidget().setPosition(94, 34);
	}

	@Override
	public void draw(InfusionRecipe recipe, IRecipeSlotsView slotsView, GuiGraphicsExtractor g, double mouseX, double mouseY) {
		Font font = Minecraft.getInstance().font;
		int inst = Math.min(5, recipe.instability() / 2);
		Component instText = Component.translatable("tc.inst").append(Component.translatable("tc.inst." + inst));
		g.text(font, instText, 84 - font.width(instText) / 2, 78, 0xFF404040, false);

		if (recipe.aspects() == null) {
			return;
		}
		Aspect[] aspects = recipe.aspects().getAspectsSorted();
		int count = aspects.length;
		if (count == 0) {
			return;
		}
		int cols = Math.min(count, 8);
		int startX = Math.max(4, 84 - (cols * 20) / 2);
		for (int i = 0; i < count; i++) {
			int vx = startX + (i % 8) * 20;
			int vy = 94 + (i / 8) * 18;
			int amt = recipe.aspects().getAmount(aspects[i]);
			GuiUtil.drawTag(g, vx, vy, aspects[i], amt, 0, 771, 1.0F, false);
		}
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, InfusionRecipe recipe, IRecipeSlotsView slotsView, double mouseX, double mouseY) {
		if (recipe.aspects() != null) {
			Aspect[] aspects = recipe.aspects().getAspectsSorted();
			int count = aspects.length;
			int cols = Math.min(count, 8);
			int startX = Math.max(4, 84 - (cols * 20) / 2);
			for (int i = 0; i < count; i++) {
				int vx = startX + (i % 8) * 20;
				int vy = 94 + (i / 8) * 18;
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
