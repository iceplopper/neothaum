package dev.thaumvibe.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.registry.TVRecipes;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

/** Port of thaumcraft.api.crafting.ShapedArcaneRecipe. */
public record ShapedArcaneRecipe(String research, AspectList aspects, ShapedRecipePattern pattern, ItemStackTemplate result) implements ArcaneRecipe {
	public static final MapCodec<ShapedArcaneRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		Codec.STRING.fieldOf("research").forGetter(ShapedArcaneRecipe::research),
		AspectList.CODEC.fieldOf("aspects").forGetter(ShapedArcaneRecipe::aspects),
		ShapedRecipePattern.MAP_CODEC.forGetter(ShapedArcaneRecipe::pattern),
		ItemStackTemplate.CODEC.fieldOf("result").forGetter(ShapedArcaneRecipe::result)
	).apply(i, ShapedArcaneRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, ShapedArcaneRecipe> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, ShapedArcaneRecipe::research, AspectList.STREAM_CODEC, ShapedArcaneRecipe::aspects,
		ShapedRecipePattern.STREAM_CODEC, ShapedArcaneRecipe::pattern, ItemStackTemplate.STREAM_CODEC, ShapedArcaneRecipe::result,
		ShapedArcaneRecipe::new);
	public static final RecipeSerializer<ShapedArcaneRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return this.pattern.matches(input);
	}

	@Override
	public List<Ingredient> ingredientList() {
		return this.pattern.ingredients().stream().flatMap(Optional::stream).toList();
	}

	@Override
	public RecipeSerializer<ShapedArcaneRecipe> getSerializer() {
		return TVRecipes.ARCANE_SHAPED;
	}

	@Override
	public PlacementInfo placementInfo() {
		return PlacementInfo.createFromOptionals(this.pattern.ingredients());
	}
}
