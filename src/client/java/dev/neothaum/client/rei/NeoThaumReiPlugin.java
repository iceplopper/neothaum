package dev.neothaum.client.rei;

import dev.neothaum.NeoThaum;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.client.compat.RecipeLookup;
import dev.neothaum.client.gui.ArcaneWorkbenchScreen;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVItems;
import java.util.HashSet;
import java.util.Set;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;

/** REI plugin for NeoThaum: the same five pages as the JEI plugin (arcane, crucible, infusion, infusion enchanting, aspect combinations). */
public class NeoThaumReiPlugin implements REIClientPlugin {
	public static final CategoryIdentifier<RecipeDisplay> ARCANE = CategoryIdentifier.of(NeoThaum.MOD_ID, "arcane");
	public static final CategoryIdentifier<RecipeDisplay> CRUCIBLE = CategoryIdentifier.of(NeoThaum.MOD_ID, "crucible");
	public static final CategoryIdentifier<RecipeDisplay> INFUSION = CategoryIdentifier.of(NeoThaum.MOD_ID, "infusion");
	public static final CategoryIdentifier<RecipeDisplay> INFUSION_ENCHANTMENT = CategoryIdentifier.of(NeoThaum.MOD_ID, "infusion_enchantment");
	public static final CategoryIdentifier<AspectDisplay> ASPECT_DECOMP = CategoryIdentifier.of(NeoThaum.MOD_ID, "aspect_decomp");

	/** Recipes already shown, so the recipes the server syncs later are added once. */
	private static final Set<Identifier> SHOWN = new HashSet<>();

	@Override
	public void registerCategories(CategoryRegistry registry) {
		registry.add(new ReiCategories.Arcane(), new ReiCategories.Crucible(), new ReiCategories.InfusionPage(),
			new ReiCategories.InfusionEnchantmentPage(), new ReiCategories.AspectPage());
		registry.addWorkstations(ARCANE, EntryStacks.of(TVBlocks.ARCANE_WORKTABLE));
		registry.addWorkstations(BuiltinPlugin.CRAFTING, EntryStacks.of(TVBlocks.ARCANE_WORKTABLE));
		registry.addWorkstations(CRUCIBLE, EntryStacks.of(TVBlocks.CRUCIBLE));
		registry.addWorkstations(INFUSION, EntryStacks.of(TVBlocks.RUNIC_MATRIX), EntryStacks.of(TVBlocks.ARCANE_PEDESTAL));
		registry.addWorkstations(INFUSION_ENCHANTMENT, EntryStacks.of(TVBlocks.RUNIC_MATRIX), EntryStacks.of(TVBlocks.ARCANE_PEDESTAL));
		// the research table has no item (it is built in place from two tables), so only the thaumometer
		registry.addWorkstations(ASPECT_DECOMP, EntryStacks.of(TVItems.THAUMOMETER));
	}

	@Override
	public void registerDisplays(DisplayRegistry registry) {
		SHOWN.clear();
		addRecipes(registry);
		for (Aspect a : Aspect.aspects.values()) {
			if (!a.isPrimal() && a.getComponents() != null && a.getComponents().length >= 2) {
				registry.add(new AspectDisplay(a));
			}
		}
	}

	@Override
	public void registerScreens(ScreenRegistry registry) {
		registry.registerContainerClickArea(new Rectangle(110, 60, 40, 24), ArcaneWorkbenchScreen.class, ARCANE, BuiltinPlugin.CRAFTING);
	}

	private static void addRecipes(DisplayRegistry registry) {
		for (var e : RecipeLookup.byId().entrySet()) {
			Recipe<?> recipe = e.getValue();
			RecipeDisplay d = SHOWN.contains(e.getKey()) ? null : RecipeDisplay.of(e.getKey(), recipe);
			if (d != null) {
				registry.add(d);
				SHOWN.add(e.getKey());
			}
		}
	}

	/** The server's recipes reached the client (TVClientNetwork): show any REI has not got yet. */
	public static void onRecipesUpdated() {
		try {
			addRecipes(DisplayRegistry.getInstance());
		} catch (RuntimeException e) {
			// REI has not loaded its registries yet; its own reload will pick the recipes up
		}
	}
}
