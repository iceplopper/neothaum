package dev.neothaum.client.rei;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import dev.neothaum.client.compat.RecipeLookup;
import dev.neothaum.crafting.ArcaneRecipe;
import dev.neothaum.crafting.CrucibleRecipe;
import dev.neothaum.crafting.InfusionEnchantmentRecipe;
import dev.neothaum.crafting.InfusionRecipe;
import dev.neothaum.crafting.ShapedArcaneRecipe;
import dev.neothaum.crafting.ShapelessArcaneRecipe;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

/** One NeoThaum recipe shown in REI: arcane, crucible, infusion or infusion enchanting. Serialized as its recipe id. */
public class RecipeDisplay extends BasicDisplay {
	public static final DisplaySerializer<RecipeDisplay> SERIALIZER = DisplaySerializer.of(
		Identifier.CODEC.fieldOf("recipe").flatXmap(RecipeDisplay::lookup, d -> DataResult.success(d.id)),
		StreamCodec.of((RegistryFriendlyByteBuf buf, RecipeDisplay d) -> Identifier.STREAM_CODEC.encode(buf, d.id),
			buf -> lookup(Identifier.STREAM_CODEC.decode(buf)).getOrThrow()),
		false);

	public final Identifier id;
	public final Recipe<?> recipe;
	private final CategoryIdentifier<RecipeDisplay> category;

	private RecipeDisplay(Identifier id, Recipe<?> recipe, CategoryIdentifier<RecipeDisplay> category, List<EntryIngredient> inputs,
		List<EntryIngredient> outputs) {
		super(inputs, outputs, Optional.of(id));
		this.id = id;
		this.recipe = recipe;
		this.category = category;
	}

	private static DataResult<RecipeDisplay> lookup(Identifier id) {
		Recipe<?> r = RecipeLookup.byId().get(id);
		RecipeDisplay d = r != null ? of(id, r) : null;
		return d != null ? DataResult.success(d) : DataResult.error(() -> "No NeoThaum recipe " + id);
	}

	private static EntryIngredient of(Ingredient ingredient) {
		return EntryIngredients.ofIngredient(ingredient);
	}

	/** The display for a recipe, or null for a recipe type REI does not show. */
	public static RecipeDisplay of(Identifier id, Recipe<?> recipe) {
		List<EntryIngredient> in = new ArrayList<>();
		if (recipe instanceof ShapedArcaneRecipe shaped) {
			int w = shaped.pattern().width();
			int h = shaped.pattern().height();
			for (int r = 0; r < 3; r++) {
				for (int c = 0; c < 3; c++) {
					in.add(c < w && r < h ? shaped.pattern().ingredients().get(c + r * w).map(RecipeDisplay::of).orElse(EntryIngredient.empty())
						: EntryIngredient.empty());
				}
			}
			return new RecipeDisplay(id, recipe, NeoThaumReiPlugin.ARCANE, in, List.of(EntryIngredients.of(shaped.result())));
		}
		if (recipe instanceof ShapelessArcaneRecipe shapeless) {
			shapeless.ingredients().forEach(i -> in.add(of(i)));
			return new RecipeDisplay(id, recipe, NeoThaumReiPlugin.ARCANE, in, List.of(EntryIngredients.of(shapeless.result())));
		}
		if (recipe instanceof ArcaneRecipe) {
			return null;
		}
		if (recipe instanceof CrucibleRecipe crucible) {
			return new RecipeDisplay(id, recipe, NeoThaumReiPlugin.CRUCIBLE, List.of(of(crucible.catalyst())),
				List.of(EntryIngredients.of(crucible.result())));
		}
		if (recipe instanceof InfusionRecipe infusion) {
			in.add(of(infusion.central()));
			infusion.components().forEach(i -> in.add(of(i)));
			return new RecipeDisplay(id, recipe, NeoThaumReiPlugin.INFUSION, in, List.of(EntryIngredients.of(infusion.result())));
		}
		if (recipe instanceof InfusionEnchantmentRecipe ench) {
			in.add(EntryIngredients.of(new ItemStack(Items.BOOK)));
			ench.components().forEach(i -> in.add(of(i)));
			ItemStack book = EnchantmentHelper.createBook(new EnchantmentInstance(ench.enchantment(), 1));
			return new RecipeDisplay(id, recipe, NeoThaumReiPlugin.INFUSION_ENCHANTMENT, in, List.of(EntryIngredients.of(book)));
		}
		return null;
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return this.category;
	}

	@Override
	public DisplaySerializer<? extends RecipeDisplay> getSerializer() {
		return SERIALIZER;
	}
}
