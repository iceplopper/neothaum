package dev.neothaum.client.jei;

import dev.neothaum.NeoThaum;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.client.gui.ArcaneWorkbenchScreen;
import dev.neothaum.crafting.ArcaneRecipe;
import dev.neothaum.crafting.CrucibleRecipe;
import dev.neothaum.crafting.InfusionEnchantmentRecipe;
import dev.neothaum.crafting.InfusionRecipe;
import dev.neothaum.menu.ArcaneWorkbenchMenu;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVItems;
import dev.neothaum.registry.TVMenus;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;

/** Main JEI plugin for NeoThaum: registers recipe categories, recipes, crafting stations, and handlers. */
@JeiPlugin
public class NeoThaumJeiPlugin implements IModPlugin {
	public static final IRecipeType<ArcaneRecipe> ARCANE = IRecipeType.create("neothaum", "arcane", ArcaneRecipe.class);
	public static final IRecipeType<CrucibleRecipe> CRUCIBLE = IRecipeType.create("neothaum", "crucible", CrucibleRecipe.class);
	public static final IRecipeType<InfusionRecipe> INFUSION = IRecipeType.create("neothaum", "infusion", InfusionRecipe.class);
	public static final IRecipeType<InfusionEnchantmentRecipe> INFUSION_ENCHANTMENT = IRecipeType.create("neothaum", "infusion_enchantment",
		InfusionEnchantmentRecipe.class);
	public static final IRecipeType<AspectDecompRecipe> ASPECT_DECOMP = IRecipeType.create("neothaum", "aspect_decomp",
		AspectDecompRecipe.class);

	private static IJeiRuntime jeiRuntime;

	@Override
	public Identifier getPluginUid() {
		return NeoThaum.id("jei_plugin");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		IGuiHelper helper = registration.getJeiHelpers().getGuiHelper();
		registration.addRecipeCategories(
			new ArcaneRecipeCategory(helper),
			new CrucibleRecipeCategory(helper),
			new InfusionRecipeCategory(helper),
			new InfusionEnchantmentRecipeCategory(helper),
			new AspectDecompCategory(helper)
		);
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		List<Recipe<?>> all = getAllKnownRecipes();
		List<ArcaneRecipe> arcane = new ArrayList<>();
		List<CrucibleRecipe> crucible = new ArrayList<>();
		List<InfusionRecipe> infusion = new ArrayList<>();
		List<InfusionEnchantmentRecipe> infusionEnch = new ArrayList<>();

		for (Recipe<?> r : all) {
			if (r instanceof ArcaneRecipe ar) {
				arcane.add(ar);
			} else if (r instanceof CrucibleRecipe cr) {
				crucible.add(cr);
			} else if (r instanceof InfusionRecipe ir) {
				infusion.add(ir);
			} else if (r instanceof InfusionEnchantmentRecipe ier) {
				infusionEnch.add(ier);
			}
		}

		if (!arcane.isEmpty()) {
			registration.addRecipes(ARCANE, arcane);
		}
		if (!crucible.isEmpty()) {
			registration.addRecipes(CRUCIBLE, crucible);
		}
		if (!infusion.isEmpty()) {
			registration.addRecipes(INFUSION, infusion);
		}
		if (!infusionEnch.isEmpty()) {
			registration.addRecipes(INFUSION_ENCHANTMENT, infusionEnch);
		}

		List<AspectDecompRecipe> decomps = new ArrayList<>();
		for (Aspect a : Aspect.aspects.values()) {
			if (!a.isPrimal() && a.getComponents() != null && a.getComponents().length >= 2) {
				decomps.add(new AspectDecompRecipe(a, a.getComponents()[0], a.getComponents()[1]));
			}
		}
		if (!decomps.isEmpty()) {
			registration.addRecipes(ASPECT_DECOMP, decomps);
		}
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addCraftingStation(ARCANE, TVBlocks.ARCANE_WORKTABLE);
		registration.addCraftingStation(RecipeTypes.CRAFTING, TVBlocks.ARCANE_WORKTABLE);
		registration.addCraftingStation(CRUCIBLE, TVBlocks.CRUCIBLE);
		registration.addCraftingStation(INFUSION, TVBlocks.RUNIC_MATRIX, TVBlocks.ARCANE_PEDESTAL);
		registration.addCraftingStation(INFUSION_ENCHANTMENT, TVBlocks.RUNIC_MATRIX, TVBlocks.ARCANE_PEDESTAL);
		// the research table has no item (it is built in place from two tables), so only the thaumometer
		registration.addCraftingStation(ASPECT_DECOMP, TVItems.THAUMOMETER);
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addRecipeClickArea(ArcaneWorkbenchScreen.class, 110, 60, 40, 24, ARCANE, RecipeTypes.CRAFTING);
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		registration.addRecipeTransferHandler(ArcaneWorkbenchMenu.class, TVMenus.ARCANE_WORKBENCH, ARCANE, 2, 9, 11, 36);
		registration.addRecipeTransferHandler(ArcaneWorkbenchMenu.class, TVMenus.ARCANE_WORKBENCH, RecipeTypes.CRAFTING, 2, 9, 11, 36);
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime runtime) {
		jeiRuntime = runtime;
		updateRuntimeRecipes();
	}

	@Override
	public void onRuntimeUnavailable() {
		jeiRuntime = null;
	}

	/** Called when server-synced recipes arrive or are updated on the client. */
	public static void onRecipesUpdated() {
		if (jeiRuntime != null) {
			updateRuntimeRecipes();
		}
	}

	private static void updateRuntimeRecipes() {
		if (jeiRuntime == null) {
			return;
		}
		List<Recipe<?>> all = getAllKnownRecipes();
		List<ArcaneRecipe> arcane = new ArrayList<>();
		List<CrucibleRecipe> crucible = new ArrayList<>();
		List<InfusionRecipe> infusion = new ArrayList<>();
		List<InfusionEnchantmentRecipe> infusionEnch = new ArrayList<>();

		for (Recipe<?> r : all) {
			if (r instanceof ArcaneRecipe ar) {
				arcane.add(ar);
			} else if (r instanceof CrucibleRecipe cr) {
				crucible.add(cr);
			} else if (r instanceof InfusionRecipe ir) {
				infusion.add(ir);
			} else if (r instanceof InfusionEnchantmentRecipe ier) {
				infusionEnch.add(ier);
			}
		}

		if (!arcane.isEmpty()) {
			jeiRuntime.getRecipeManager().addRecipes(ARCANE, arcane);
		}
		if (!crucible.isEmpty()) {
			jeiRuntime.getRecipeManager().addRecipes(CRUCIBLE, crucible);
		}
		if (!infusion.isEmpty()) {
			jeiRuntime.getRecipeManager().addRecipes(INFUSION, infusion);
		}
		if (!infusionEnch.isEmpty()) {
			jeiRuntime.getRecipeManager().addRecipes(INFUSION_ENCHANTMENT, infusionEnch);
		}
	}

	public static List<Recipe<?>> getAllKnownRecipes() {
		return dev.neothaum.client.compat.RecipeLookup.all();
	}

	public static Identifier getRecipeId(Recipe<?> recipe) {
		return dev.neothaum.client.compat.RecipeLookup.idOf(recipe);
	}
}
