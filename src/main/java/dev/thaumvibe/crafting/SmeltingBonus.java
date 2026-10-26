package dev.thaumvibe.crafting;

import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.registry.TVItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class SmeltingBonus {
	public static ItemStack getBonus(ItemStack input) {
		if (input == null || input.isEmpty()) {
			return null;
		}
		if (input.is(Items.RAW_IRON) || input.is(Items.IRON_ORE) || input.is(Items.DEEPSLATE_IRON_ORE)) {
			return new ItemStack(Items.IRON_NUGGET);
		}
		if (input.is(Items.RAW_GOLD) || input.is(Items.GOLD_ORE) || input.is(Items.DEEPSLATE_GOLD_ORE) || input.is(Items.NETHER_GOLD_ORE)) {
			return new ItemStack(Items.GOLD_NUGGET);
		}
		if (input.is(Items.RAW_COPPER) || input.is(Items.COPPER_ORE) || input.is(Items.DEEPSLATE_COPPER_ORE)) {
			return new ItemStack(TVItems.COPPER_NUGGET);
		}
		if (input.is(TVBlocks.CINNABAR_ORE.asItem())) {
			return new ItemStack(TVItems.QUICKSILVER);
		}
		if (input.is(TVItems.NATIVE_IRON_CLUSTER)) {
			return new ItemStack(Items.IRON_NUGGET);
		}
		if (input.is(TVItems.NATIVE_GOLD_CLUSTER)) {
			return new ItemStack(Items.GOLD_NUGGET);
		}
		if (input.is(TVItems.NATIVE_COPPER_CLUSTER)) {
			return new ItemStack(TVItems.COPPER_NUGGET);
		}
		if (input.is(TVItems.NATIVE_CINNABAR_CLUSTER)) {
			return new ItemStack(TVItems.QUICKSILVER);
		}
		if (input.is(Items.BEEF)) {
			return new ItemStack(TVItems.BEEF_NUGGET);
		}
		if (input.is(Items.CHICKEN)) {
			return new ItemStack(TVItems.CHICKEN_NUGGET);
		}
		if (input.is(Items.PORKCHOP)) {
			return new ItemStack(TVItems.PORK_NUGGET);
		}
		if (input.is(Items.COD) || input.is(Items.SALMON)) {
			return new ItemStack(TVItems.FISH_NUGGET);
		}
		Identifier id = BuiltInRegistries.ITEM.getKey(input.getItem());
		String path = id.getPath();
		if (path.contains("iron") && (path.contains("ore") || path.contains("raw"))) {
			return new ItemStack(Items.IRON_NUGGET);
		}
		if (path.contains("gold") && (path.contains("ore") || path.contains("raw"))) {
			return new ItemStack(Items.GOLD_NUGGET);
		}
		if (path.contains("copper") && (path.contains("ore") || path.contains("raw"))) {
			return new ItemStack(TVItems.COPPER_NUGGET);
		}
		if (path.contains("cinnabar") || path.contains("mercury")) {
			return new ItemStack(TVItems.QUICKSILVER);
		}
		return null;
	}

	private SmeltingBonus() {}
}
