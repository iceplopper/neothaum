package dev.neothaum.crafting;

import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.registry.TVRecipes;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeType;

/** Port of thaumcraft.api.crafting.IArcaneRecipe: a 3x3 recipe that also costs primal vis and needs research. */
public interface ArcaneRecipe extends Recipe<CraftingInput> {
	String research();

	AspectList aspects();

	ItemStackTemplate result();

	/** Every non-empty ingredient, for aspect generation and display. */
	List<Ingredient> ingredientList();

	@Override
	default ItemStack assemble(CraftingInput input) {
		return this.result().create();
	}

	@Override
	default boolean showNotification() {
		return false;
	}

	@Override
	default String group() {
		return "";
	}

	@Override
	default RecipeType<? extends Recipe<CraftingInput>> getType() {
		return TVRecipes.ARCANE;
	}

	@Override
	default RecipeBookCategory recipeBookCategory() {
		return RecipeBookCategories.CRAFTING_MISC;
	}
}
