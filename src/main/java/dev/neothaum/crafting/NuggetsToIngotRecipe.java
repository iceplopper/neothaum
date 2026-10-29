package dev.neothaum.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** Nine of TC4's tin, silver or lead nuggets in a full grid make the first ingot of a tag (ConfigRecipes.oreDictRecipe in Config.postInit). */
public class NuggetsToIngotRecipe extends CustomRecipe {
	public static final MapCodec<NuggetsToIngotRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		BuiltInRegistries.ITEM.byNameCodec().fieldOf("nugget").forGetter(o -> o.nugget),
		Identifier.CODEC.fieldOf("result_tag").forGetter(o -> o.resultTag.location())
	).apply(i, NuggetsToIngotRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, NuggetsToIngotRecipe> STREAM_CODEC = StreamCodec.composite(
		net.minecraft.network.codec.ByteBufCodecs.registry(Registries.ITEM), o -> o.nugget,
		Identifier.STREAM_CODEC, o -> o.resultTag.location(),
		NuggetsToIngotRecipe::new);
	public static final RecipeSerializer<NuggetsToIngotRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

	private final Item nugget;
	private final TagKey<Item> resultTag;

	public NuggetsToIngotRecipe(Item nugget, Identifier resultTag) {
		this.nugget = nugget;
		this.resultTag = TagKey.create(Registries.ITEM, resultTag);
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		if (input.size() != 9 || TagResultSmeltingRecipe.firstOf(this.resultTag) == null) {
			return false;
		}
		for (int i = 0; i < 9; i++) {
			if (!input.getItem(i).is(this.nugget)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		Item ingot = TagResultSmeltingRecipe.firstOf(this.resultTag);
		return ingot != null ? new ItemStack(ingot) : ItemStack.EMPTY;
	}

	@Override
	public RecipeSerializer<NuggetsToIngotRecipe> getSerializer() {
		return SERIALIZER;
	}
}
