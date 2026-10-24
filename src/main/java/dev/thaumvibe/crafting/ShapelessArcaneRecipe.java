package dev.thaumvibe.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.registry.TVRecipes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** Port of thaumcraft.api.crafting.ShapelessArcaneRecipe. */
public record ShapelessArcaneRecipe(String research, AspectList aspects, List<Ingredient> ingredients, ItemStackTemplate result) implements ArcaneRecipe {
	public static final MapCodec<ShapelessArcaneRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		Codec.STRING.fieldOf("research").forGetter(ShapelessArcaneRecipe::research),
		AspectList.CODEC.fieldOf("aspects").forGetter(ShapelessArcaneRecipe::aspects),
		Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(ShapelessArcaneRecipe::ingredients),
		ItemStackTemplate.CODEC.fieldOf("result").forGetter(ShapelessArcaneRecipe::result)
	).apply(i, ShapelessArcaneRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, ShapelessArcaneRecipe> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, ShapelessArcaneRecipe::research, AspectList.STREAM_CODEC, ShapelessArcaneRecipe::aspects,
		Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), ShapelessArcaneRecipe::ingredients, ItemStackTemplate.STREAM_CODEC,
		ShapelessArcaneRecipe::result, ShapelessArcaneRecipe::new);
	public static final RecipeSerializer<ShapelessArcaneRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

	@Override
	public boolean matches(CraftingInput input, Level level) {
		List<Ingredient> required = new ArrayList<>(this.ingredients);
		for (int i = 0; i < input.size(); i++) {
			ItemStack s = input.getItem(i);
			if (s.isEmpty()) {
				continue;
			}
			boolean found = false;
			for (int j = 0; j < required.size(); j++) {
				if (required.get(j).test(s)) {
					required.remove(j);
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return required.isEmpty();
	}

	@Override
	public List<Ingredient> ingredientList() {
		return this.ingredients;
	}

	@Override
	public RecipeSerializer<ShapelessArcaneRecipe> getSerializer() {
		return TVRecipes.ARCANE_SHAPELESS;
	}

	@Override
	public PlacementInfo placementInfo() {
		return PlacementInfo.create(this.ingredients);
	}
}
