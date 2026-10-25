package dev.thaumvibe.crafting;

import dev.thaumvibe.api.RunicArmor;
import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.registry.TVComponents;
import dev.thaumvibe.registry.TVItems;
import dev.thaumvibe.research.ResearchManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * InfusionRunicAugmentRecipe: hardens any runic gear by one more point of shield. It takes a diamond and a salis mundus, plus another salis
 * mundus for every point of charge the piece already has, and the essentia doubles with each point.
 */
public final class RunicAugment {
	public static final String RESEARCH = "RUNICAUGMENTATION";

	public static List<Ingredient> components(ItemStack input) {
		List<Ingredient> com = new ArrayList<>();
		com.add(Ingredient.of(Items.DIAMOND));
		com.add(Ingredient.of(TVItems.SALIS_MUNDUS));
		int fc = RunicArmor.getFinalCharge(input);
		for (int c = 0; c < fc; c++) {
			com.add(Ingredient.of(TVItems.SALIS_MUNDUS));
		}
		return com;
	}

	public static boolean matches(List<ItemStack> input, ItemStack central, Player player) {
		return ResearchManager.isResearchComplete(player, RESEARCH) && central.getItem() instanceof RunicArmor
			&& InfusionRecipe.componentsMatch(components(central), input);
	}

	public static ItemStack output(ItemStack input) {
		ItemStack out = input.copy();
		out.set(TVComponents.RUNIC_HARDENING, RunicArmor.getHardening(input) + 1);
		return out;
	}

	public static AspectList aspects(ItemStack input) {
		AspectList out = new AspectList();
		int vis = (int) (32.0 * Math.pow(2.0, RunicArmor.getFinalCharge(input)));
		if (vis > 0) {
			out.add(Aspect.ARMOR, vis / 2);
			out.add(Aspect.MAGIC, vis / 2);
			out.add(Aspect.ENERGY, vis);
		}
		return out;
	}

	public static int instability(ItemStack input) {
		return 5 + RunicArmor.getFinalCharge(input) / 2;
	}

	private RunicAugment() {
	}
}
