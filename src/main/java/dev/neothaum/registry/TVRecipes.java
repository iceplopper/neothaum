package dev.neothaum.registry;

import dev.neothaum.NeoThaum;
import dev.neothaum.crafting.ArcaneRecipe;
import dev.neothaum.crafting.CrucibleRecipe;
import dev.neothaum.crafting.InfusionEnchantmentRecipe;
import dev.neothaum.crafting.InfusionRecipe;
import dev.neothaum.crafting.JarLabelRecipe;
import dev.neothaum.crafting.ShapedArcaneRecipe;
import dev.neothaum.crafting.ShapelessArcaneRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public final class TVRecipes {
	public static final RecipeType<ArcaneRecipe> ARCANE = type("arcane");
	public static final RecipeType<CrucibleRecipe> CRUCIBLE = type("crucible");
	public static final RecipeType<InfusionRecipe> INFUSION = type("infusion");
	public static final RecipeType<InfusionEnchantmentRecipe> INFUSION_ENCHANTMENT = type("infusion_enchantment");

	public static final RecipeSerializer<ShapedArcaneRecipe> ARCANE_SHAPED = serializer("arcane_shaped", ShapedArcaneRecipe.SERIALIZER);
	public static final RecipeSerializer<ShapelessArcaneRecipe> ARCANE_SHAPELESS = serializer("arcane_shapeless", ShapelessArcaneRecipe.SERIALIZER);
	public static final RecipeSerializer<CrucibleRecipe> CRUCIBLE_SERIALIZER = serializer("crucible", CrucibleRecipe.SERIALIZER);
	public static final RecipeSerializer<InfusionRecipe> INFUSION_SERIALIZER = serializer("infusion", InfusionRecipe.SERIALIZER);
	public static final RecipeSerializer<InfusionEnchantmentRecipe> INFUSION_ENCHANTMENT_SERIALIZER = serializer("infusion_enchantment",
		InfusionEnchantmentRecipe.SERIALIZER);
	public static final RecipeSerializer<JarLabelRecipe> JAR_LABEL = serializer("jar_label", JarLabelRecipe.SERIALIZER);

	private static <T extends Recipe<?>> RecipeType<T> type(String name) {
		return Registry.register(BuiltInRegistries.RECIPE_TYPE, NeoThaum.id(name), new RecipeType<T>() {
			@Override
			public String toString() {
				return "neothaum:" + name;
			}
		});
	}

	private static <T extends Recipe<?>> RecipeSerializer<T> serializer(String name, RecipeSerializer<T> s) {
		return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, NeoThaum.id(name), s);
	}

	public static void init() {
	}
}
