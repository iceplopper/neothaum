package dev.neothaum.client.compat;

import dev.neothaum.NeoThaum;
import dev.neothaum.client.TVClientNetwork;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * NeoThaum's recipes as the client knows them, for the recipe viewers (JEI, REI): the ones the server synced, plus those of the integrated server
 * in single player. Free of any viewer's API, so either can use it without the other installed.
 */
public final class RecipeLookup {
	public static List<Recipe<?>> all() {
		return new ArrayList<>(byId().values());
	}

	public static Map<Identifier, Recipe<?>> byId() {
		Map<Identifier, Recipe<?>> map = new LinkedHashMap<>(TVClientNetwork.RECIPES);
		Minecraft mc = Minecraft.getInstance();
		if (mc != null && mc.getSingleplayerServer() != null) {
			for (RecipeHolder<?> holder : mc.getSingleplayerServer().getRecipeManager().getRecipes()) {
				Identifier id = holder.id().identifier();
				if (id.getNamespace().equals(NeoThaum.MOD_ID)) {
					map.putIfAbsent(id, holder.value());
				}
			}
		}
		return map;
	}

	public static Identifier idOf(Recipe<?> recipe) {
		for (Map.Entry<Identifier, Recipe<?>> entry : byId().entrySet()) {
			if (entry.getValue() == recipe) {
				return entry.getKey();
			}
		}
		return null;
	}

	private RecipeLookup() {
	}
}
