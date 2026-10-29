package dev.neothaum.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;

/**
 * A smelting recipe whose result is the first item of a tag, as TC4 smelted its tin, silver and lead clusters into whichever mod's ingot the
 * ore dictionary listed first (Config.postInit). With nothing in the tag it does not match.
 */
public class TagResultSmeltingRecipe extends SmeltingRecipe {
	public static final MapCodec<TagResultSmeltingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		Recipe.CommonInfo.MAP_CODEC.forGetter(o -> o.commonInfo),
		AbstractCookingRecipe.CookingBookInfo.MAP_CODEC.forGetter(o -> o.bookInfo),
		Ingredient.CODEC.fieldOf("ingredient").forGetter(TagResultSmeltingRecipe::input),
		Identifier.CODEC.fieldOf("result_tag").forGetter(o -> o.resultTag.location()),
		Codec.INT.optionalFieldOf("count", 1).forGetter(o -> o.count),
		Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(AbstractCookingRecipe::experience),
		Codec.INT.fieldOf("cookingtime").forGetter(AbstractCookingRecipe::cookingTime)
	).apply(i, TagResultSmeltingRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, TagResultSmeltingRecipe> STREAM_CODEC = StreamCodec.composite(
		Recipe.CommonInfo.STREAM_CODEC, o -> o.commonInfo,
		AbstractCookingRecipe.CookingBookInfo.STREAM_CODEC, o -> o.bookInfo,
		Ingredient.CONTENTS_STREAM_CODEC, TagResultSmeltingRecipe::input,
		Identifier.STREAM_CODEC, o -> o.resultTag.location(),
		ByteBufCodecs.VAR_INT, o -> o.count,
		ByteBufCodecs.FLOAT, AbstractCookingRecipe::experience,
		ByteBufCodecs.VAR_INT, AbstractCookingRecipe::cookingTime,
		TagResultSmeltingRecipe::new);
	public static final RecipeSerializer<TagResultSmeltingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

	private final TagKey<Item> resultTag;
	private final int count;

	public TagResultSmeltingRecipe(Recipe.CommonInfo info, AbstractCookingRecipe.CookingBookInfo book, Ingredient ingredient, Identifier resultTag,
		int count, float experience, int cookingTime) {
		super(info, book, ingredient, new ItemStackTemplate(Items.BARRIER), experience, cookingTime);
		this.resultTag = TagKey.create(Registries.ITEM, resultTag);
		this.count = count;
	}

	/** The first item of a tag, or null when no loaded mod puts anything in it. */
	public static Item firstOf(TagKey<Item> tag) {
		var it = BuiltInRegistries.ITEM.getTagOrEmpty(tag).iterator();
		return it.hasNext() ? it.next().value() : null;
	}

	@Override
	public boolean matches(SingleRecipeInput input, Level level) {
		return firstOf(this.resultTag) != null && super.matches(input, level);
	}

	@Override
	protected ItemStackTemplate result() {
		Item item = firstOf(this.resultTag);
		return new ItemStackTemplate(item != null ? item : Items.BARRIER, this.count);
	}

	@Override
	public net.minecraft.world.item.ItemStack assemble(SingleRecipeInput input) {
		Item item = firstOf(this.resultTag);
		return item != null ? new net.minecraft.world.item.ItemStack(item, this.count) : net.minecraft.world.item.ItemStack.EMPTY;
	}

	@Override
	public RecipeSerializer<SmeltingRecipe> getSerializer() {
		return (RecipeSerializer) SERIALIZER;
	}
}
