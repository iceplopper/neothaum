package dev.thaumvibe.client.jei;

import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.client.TVClientNetwork;
import dev.thaumvibe.client.gui.ArcaneWorkbenchScreen;
import dev.thaumvibe.crafting.ArcaneRecipe;
import dev.thaumvibe.crafting.CrucibleRecipe;
import dev.thaumvibe.crafting.InfusionEnchantmentRecipe;
import dev.thaumvibe.crafting.InfusionRecipe;
import dev.thaumvibe.menu.ArcaneWorkbenchMenu;
import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.registry.TVMenus;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

/** Main JEI plugin for Thaumvibe: registers recipe categories, recipes, crafting stations, and handlers. */
@JeiPlugin
public class ThaumvibeJeiPlugin implements IModPlugin {
	public static final IRecipeType<ArcaneRecipe> ARCANE = IRecipeType.create("thaumvibe", "arcane", ArcaneRecipe.class);
	public static final IRecipeType<CrucibleRecipe> CRUCIBLE = IRecipeType.create("thaumvibe", "crucible", CrucibleRecipe.class);
	public static final IRecipeType<InfusionRecipe> INFUSION = IRecipeType.create("thaumvibe", "infusion", InfusionRecipe.class);
	public static final IRecipeType<InfusionEnchantmentRecipe> INFUSION_ENCHANTMENT = IRecipeType.create("thaumvibe", "infusion_enchantment",
		InfusionEnchantmentRecipe.class);

	private static IJeiRuntime jeiRuntime;

	@Override
	public Identifier getPluginUid() {
		return Thaumvibe.id("jei_plugin");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		IGuiHelper helper = registration.getJeiHelpers().getGuiHelper();
		registration.addRecipeCategories(
			new ArcaneRecipeCategory(helper),
			new CrucibleRecipeCategory(helper),
			new InfusionRecipeCategory(helper),
			new InfusionEnchantmentRecipeCategory(helper)
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
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addCraftingStation(ARCANE, TVBlocks.ARCANE_WORKTABLE);
		registration.addCraftingStation(RecipeTypes.CRAFTING, TVBlocks.ARCANE_WORKTABLE);
		registration.addCraftingStation(CRUCIBLE, TVBlocks.CRUCIBLE);
		registration.addCraftingStation(INFUSION, TVBlocks.RUNIC_MATRIX, TVBlocks.ARCANE_PEDESTAL);
		registration.addCraftingStation(INFUSION_ENCHANTMENT, TVBlocks.RUNIC_MATRIX, TVBlocks.ARCANE_PEDESTAL);
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
		Map<Identifier, Recipe<?>> map = new LinkedHashMap<>(TVClientNetwork.RECIPES);
		Minecraft mc = Minecraft.getInstance();
		if (mc != null && mc.getSingleplayerServer() != null) {
			for (RecipeHolder<?> holder : mc.getSingleplayerServer().getRecipeManager().getRecipes()) {
				Identifier id = holder.id().identifier();
				if (id.getNamespace().equals(Thaumvibe.MOD_ID)) {
					map.putIfAbsent(id, holder.value());
				}
			}
		}
		return new ArrayList<>(map.values());
	}

	public static Identifier getRecipeId(Recipe<?> recipe) {
		for (Map.Entry<Identifier, Recipe<?>> entry : TVClientNetwork.RECIPES.entrySet()) {
			if (entry.getValue() == recipe) {
				return entry.getKey();
			}
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc != null && mc.getSingleplayerServer() != null) {
			for (RecipeHolder<?> holder : mc.getSingleplayerServer().getRecipeManager().getRecipes()) {
				if (holder.value() == recipe) {
					return holder.id().identifier();
				}
			}
		}
		return null;
	}
}
