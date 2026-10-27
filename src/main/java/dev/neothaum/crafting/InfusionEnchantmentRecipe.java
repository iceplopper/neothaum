package dev.neothaum.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.registry.TVRecipes;
import dev.neothaum.research.ResearchManager;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

/**
 * Port of thaumcraft.api.crafting.InfusionEnchantmentRecipe: raises one enchantment on the central tool by a level, costing more essentia,
 * experience and stability the more enchanted it already is.
 */
public record InfusionEnchantmentRecipe(String research, Holder<Enchantment> enchantment, int instability, AspectList aspects,
	List<Ingredient> components) implements Recipe<SingleRecipeInput> {
	public static final MapCodec<InfusionEnchantmentRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		Codec.STRING.fieldOf("research").forGetter(InfusionEnchantmentRecipe::research),
		Enchantment.CODEC.fieldOf("enchantment").forGetter(InfusionEnchantmentRecipe::enchantment),
		Codec.INT.fieldOf("instability").forGetter(InfusionEnchantmentRecipe::instability),
		AspectList.CODEC.fieldOf("aspects").forGetter(InfusionEnchantmentRecipe::aspects),
		Ingredient.CODEC.listOf().fieldOf("components").forGetter(InfusionEnchantmentRecipe::components)
	).apply(i, InfusionEnchantmentRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, InfusionEnchantmentRecipe> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, InfusionEnchantmentRecipe::research, Enchantment.STREAM_CODEC, InfusionEnchantmentRecipe::enchantment,
		ByteBufCodecs.VAR_INT, InfusionEnchantmentRecipe::instability, AspectList.STREAM_CODEC, InfusionEnchantmentRecipe::aspects,
		Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), InfusionEnchantmentRecipe::components, InfusionEnchantmentRecipe::new);
	public static final RecipeSerializer<InfusionEnchantmentRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

	/** InfusionEnchantmentRecipe.matches. */
	public boolean matches(List<ItemStack> input, ItemStack central, Player player) {
		if (!this.research.isEmpty() && !ResearchManager.isResearchComplete(player, this.research)) {
			return false;
		}
		Enchantment ench = this.enchantment.value();
		// canApply && isItemTool: the enchantment must suit the item, and the item must take enchantments
		if (!ench.canEnchant(central) || !central.isEnchantable() && !central.isEnchanted()) {
			return false;
		}
		ItemEnchantments existing = EnchantmentHelper.getEnchantmentsForCrafting(central);
		for (var entry : existing.entrySet()) {
			Holder<Enchantment> other = entry.getKey();
			if (other.equals(this.enchantment)) {
				if (entry.getIntValue() >= ench.getMaxLevel()) {
					return false;
				}
			} else if (!Enchantment.areCompatible(this.enchantment, other)) {
				return false;
			}
		}
		return InfusionRecipe.componentsMatch(this.components, input);
	}

	public int calcInstability(ItemStack input) {
		int i = 0;
		for (var entry : EnchantmentHelper.getEnchantmentsForCrafting(input).entrySet()) {
			i += entry.getIntValue();
		}
		return i / 2 + this.instability;
	}

	/** recipeXP = max(1, minimum enchantability at level 1 / 3), times one more than the level already on the item. */
	public int calcXP(ItemStack input) {
		int base = Math.max(1, this.enchantment.value().getMinCost(1) / 3);
		return base * (1 + EnchantmentHelper.getItemEnchantmentLevel(this.enchantment, input));
	}

	public float getEssentiaMod(ItemStack input) {
		float mod = EnchantmentHelper.getItemEnchantmentLevel(this.enchantment, input);
		for (var entry : EnchantmentHelper.getEnchantmentsForCrafting(input).entrySet()) {
			if (!entry.getKey().equals(this.enchantment)) {
				mod += entry.getIntValue() * 0.1F;
			}
		}
		return mod;
	}

	@Override
	public boolean matches(SingleRecipeInput input, Level level) {
		return false;
	}

	@Override
	public ItemStack assemble(SingleRecipeInput input) {
		return ItemStack.EMPTY;
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
	public RecipeSerializer<InfusionEnchantmentRecipe> getSerializer() {
		return TVRecipes.INFUSION_ENCHANTMENT_SERIALIZER;
	}

	@Override
	public RecipeType<InfusionEnchantmentRecipe> getType() {
		return TVRecipes.INFUSION_ENCHANTMENT;
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
