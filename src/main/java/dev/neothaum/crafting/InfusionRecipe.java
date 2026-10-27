package dev.neothaum.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.registry.TVRecipes;
import dev.neothaum.research.ResearchManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
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

/**
 * Port of thaumcraft.api.crafting.InfusionRecipe: the central item on the altar's middle pedestal, every component on the surrounding
 * pedestals (no more, no fewer), and the essentia drawn from nearby jars.
 */
public record InfusionRecipe(String research, int instability, AspectList aspects, Ingredient central, List<Ingredient> components,
	ItemStackTemplate result) implements Recipe<SingleRecipeInput> {
	public static final MapCodec<InfusionRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		Codec.STRING.fieldOf("research").forGetter(InfusionRecipe::research),
		Codec.INT.fieldOf("instability").forGetter(InfusionRecipe::instability),
		AspectList.CODEC.fieldOf("aspects").forGetter(InfusionRecipe::aspects),
		Ingredient.CODEC.fieldOf("central").forGetter(InfusionRecipe::central),
		Ingredient.CODEC.listOf().fieldOf("components").forGetter(InfusionRecipe::components),
		ItemStackTemplate.CODEC.fieldOf("result").forGetter(InfusionRecipe::result)
	).apply(i, InfusionRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, InfusionRecipe> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, InfusionRecipe::research, ByteBufCodecs.VAR_INT, InfusionRecipe::instability, AspectList.STREAM_CODEC,
		InfusionRecipe::aspects, Ingredient.CONTENTS_STREAM_CODEC, InfusionRecipe::central,
		Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), InfusionRecipe::components, ItemStackTemplate.STREAM_CODEC,
		InfusionRecipe::result, InfusionRecipe::new);
	public static final RecipeSerializer<InfusionRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

	/** InfusionRecipe.matches. */
	public boolean matches(List<ItemStack> input, ItemStack central, Player player) {
		if (!this.research.isEmpty() && !ResearchManager.isResearchComplete(player, this.research)) {
			return false;
		}
		return this.central.test(central) && componentsMatch(this.components, input);
	}

	/** Every component finds its own pedestal item, and no pedestal item is left over. */
	public static boolean componentsMatch(List<Ingredient> components, List<ItemStack> input) {
		List<ItemStack> ii = new ArrayList<>(input);
		for (Ingredient comp : components) {
			boolean found = false;
			for (int a = 0; a < ii.size(); a++) {
				if (comp.test(ii.get(a))) {
					ii.remove(a);
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return ii.isEmpty();
	}

	@Override
	public boolean matches(SingleRecipeInput input, Level level) {
		return this.central.test(input.item());
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
	public RecipeSerializer<InfusionRecipe> getSerializer() {
		return TVRecipes.INFUSION_SERIALIZER;
	}

	@Override
	public RecipeType<InfusionRecipe> getType() {
		return TVRecipes.INFUSION;
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
