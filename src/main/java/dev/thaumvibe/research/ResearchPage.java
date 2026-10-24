package dev.thaumvibe.research;

import com.google.gson.JsonElement;
import dev.thaumvibe.api.aspects.AspectList;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Port of thaumcraft.api.research.ResearchPage.
 * TEXT pages hold a translation key; recipe pages hold recipe ids resolved on the client from the synced recipe list.
 */
public record ResearchPage(Type type, String text, String research, List<Identifier> recipes, Identifier image, AspectList aspects,
	ItemStack smeltingInput, JsonElement compound) {
	public enum Type { TEXT, TEXT_CONCEALED, IMAGE, CRUCIBLE_CRAFTING, NORMAL_CRAFTING, INFUSION_CRAFTING, ARCANE_CRAFTING, ASPECTS, SMELTING, COMPOUND_CRAFTING,
		INFUSION_ENCHANTMENT, RECIPE, MISSING }
}
