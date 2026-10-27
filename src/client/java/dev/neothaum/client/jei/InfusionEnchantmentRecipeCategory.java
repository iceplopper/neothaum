package dev.neothaum.client.jei;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.client.gui.GuiUtil;
import dev.neothaum.crafting.InfusionEnchantmentRecipe;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.research.ResearchItem;
import dev.neothaum.research.ResearchManager;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

/** JEI Category for Thaumcraft 4 Infusion Enchanting. */
public class InfusionEnchantmentRecipeCategory extends AbstractRecipeCategory<InfusionEnchantmentRecipe> {

	public InfusionEnchantmentRecipeCategory(IGuiHelper helper) {
		super(NeoThaumJeiPlugin.INFUSION_ENCHANTMENT, Component.translatable("recipe.type.infusionenchantment"),
			helper.createDrawableItemLike(TVBlocks.RUNIC_MATRIX), 168, 134);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, InfusionEnchantmentRecipe recipe, IFocusGroup focuses) {
		ItemStack book = EnchantmentHelper.createBook(new EnchantmentInstance(recipe.enchantment(), 1));

		// Central pedestal slot: accepting unenchanted items / book
		builder.addInputSlot(42, 34).setStandardSlotBackground().add(new ItemStack(Items.BOOK));

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

		// Output slot: enchanted result book
		builder.addOutputSlot(130, 30).setOutputSlotBackground().add(book);
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder extras, InfusionEnchantmentRecipe recipe, IFocusGroup focuses) {
		extras.addRecipeArrowWidget().setPosition(94, 34);
	}

	@Override
	public void draw(InfusionEnchantmentRecipe recipe, IRecipeSlotsView slotsView, GuiGraphicsExtractor g, double mouseX, double mouseY) {
		Font font = Minecraft.getInstance().font;
		Component name = Enchantment.getFullname(recipe.enchantment(), 1);
		g.text(font, name, 84 - font.width(name) / 2, 4, 0xFF705090, false);

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
	public void getTooltip(ITooltipBuilder tooltip, InfusionEnchantmentRecipe recipe, IRecipeSlotsView slotsView, double mouseX, double mouseY) {
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
