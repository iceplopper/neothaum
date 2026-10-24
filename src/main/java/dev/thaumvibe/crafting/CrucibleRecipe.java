package dev.thaumvibe.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.registry.TVRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/** Port of thaumcraft.api.crafting.CrucibleRecipe. */
public record CrucibleRecipe(String research, Ingredient catalyst, AspectList aspects, ItemStackTemplate result) implements Recipe<SingleRecipeInput> {
	public static final MapCodec<CrucibleRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		Codec.STRING.fieldOf("research").forGetter(CrucibleRecipe::research),
		Ingredient.CODEC.fieldOf("catalyst").forGetter(CrucibleRecipe::catalyst),
		AspectList.CODEC.fieldOf("aspects").forGetter(CrucibleRecipe::aspects),
		ItemStackTemplate.CODEC.fieldOf("result").forGetter(CrucibleRecipe::result)
	).apply(i, CrucibleRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, CrucibleRecipe> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, CrucibleRecipe::research, Ingredient.CONTENTS_STREAM_CODEC, CrucibleRecipe::catalyst,
		AspectList.STREAM_CODEC, CrucibleRecipe::aspects, ItemStackTemplate.STREAM_CODEC, CrucibleRecipe::result, CrucibleRecipe::new);
	public static final RecipeSerializer<CrucibleRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

	/** CrucibleRecipe.matches(AspectList itags, ItemStack cat). */
	public boolean matches(AspectList itags, ItemStack cat) {
		if (!this.catalyst.test(cat) || itags == null) {
			return false;
		}
		for (Aspect tag : this.aspects.getAspects()) {
			if (itags.getAmount(tag) < this.aspects.getAmount(tag)) {
				return false;
			}
		}
		return true;
	}

	/** CrucibleRecipe.removeMatching: subtract this recipe's aspects. */
	public AspectList removeMatching(AspectList itags) {
		AspectList temptags = itags.copy();
		for (Aspect tag : this.aspects.getAspects()) {
			if (!temptags.reduce(tag, this.aspects.getAmount(tag))) {
				return null;
			}
		}
		return temptags.cull();
	}

	@Override
	public boolean matches(SingleRecipeInput input, Level level) {
		return this.catalyst.test(input.item());
	}

	@Override
	public ItemStack assemble(SingleRecipeInput input) {
		return this.result.create();
	}

	@Override
	public boolean showNotification() {
		return false;
	}

	@Override
	public String group() {
		return "";
	}

	@Override
	public RecipeSerializer<CrucibleRecipe> getSerializer() {
		return TVRecipes.CRUCIBLE_SERIALIZER;
	}

	@Override
	public RecipeType<CrucibleRecipe> getType() {
		return TVRecipes.CRUCIBLE;
	}

	@Override
	public PlacementInfo placementInfo() {
		return PlacementInfo.NOT_PLACEABLE;
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		return RecipeBookCategories.CRAFTING_MISC;
	}
}
