package dev.neothaum.crafting;

import com.mojang.serialization.MapCodec;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.items.EssentiaPhialItem;
import dev.neothaum.registry.TVComponents;
import dev.neothaum.registry.TVItems;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** A blank jar label crafted with a phial of essentia is marked with that aspect; the emptied phial is handed back. */
public class JarLabelRecipe extends CustomRecipe {
	public static final JarLabelRecipe INSTANCE = new JarLabelRecipe();
	public static final MapCodec<JarLabelRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
	public static final StreamCodec<RegistryFriendlyByteBuf, JarLabelRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
	public static final RecipeSerializer<JarLabelRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

	private static Aspect find(CraftingInput input) {
		boolean label = false;
		Aspect aspect = null;
		for (int i = 0; i < input.size(); i++) {
			ItemStack s = input.getItem(i);
			if (s.isEmpty()) {
				continue;
			}
			if (s.is(TVItems.JAR_LABEL) && !s.has(TVComponents.ASPECT_FILTER) && !label) {
				label = true;
			} else if (s.is(TVItems.PHIAL_OF_ESSENTIA) && aspect == null && EssentiaPhialItem.aspectOf(s) != null) {
				aspect = EssentiaPhialItem.aspectOf(s);
			} else {
				return null;
			}
		}
		return label ? aspect : null;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return find(input) != null;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		Aspect a = find(input);
		if (a == null) {
			return ItemStack.EMPTY;
		}
		ItemStack out = new ItemStack(TVItems.JAR_LABEL);
		out.set(TVComponents.ASPECT_FILTER, a);
		return out;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		NonNullList<ItemStack> result = NonNullList.withSize(input.size(), ItemStack.EMPTY);
		for (int i = 0; i < input.size(); i++) {
			if (input.getItem(i).is(TVItems.PHIAL_OF_ESSENTIA)) {
				result.set(i, new ItemStack(TVItems.GLASS_PHIAL));
			}
		}
		return result;
	}

	@Override
	public RecipeSerializer<JarLabelRecipe> getSerializer() {
		return SERIALIZER;
	}
}
