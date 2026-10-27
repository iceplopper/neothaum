package dev.neothaum.crafting;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.api.wands.WandCap;
import dev.neothaum.api.wands.WandRod;
import dev.neothaum.items.WandItem;
import dev.neothaum.registry.TVItems;
import dev.neothaum.registry.TVRecipes;
import dev.neothaum.research.ResearchManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/** Port of ThaumcraftCraftingManager.findMatchingArcaneRecipe(+Aspects) and ArcaneWandRecipe. */
public final class ArcaneCraftingManager {
	public record Match(ItemStack result, AspectList aspects, boolean arcane) {
		public static final Match NONE = new Match(ItemStack.EMPTY, new AspectList(), false);
	}

	/** Recipes the client knows about (synced by the server) so GUIs can show results without a server round-trip. */
	public static List<ArcaneRecipe> clientArcane = new ArrayList<>();

	public static CraftingInput input(Container inv) {
		List<ItemStack> items = new ArrayList<>(9);
		for (int i = 0; i < 9; i++) {
			items.add(inv.getItem(i));
		}
		return CraftingInput.of(3, 3, items);
	}

	private static ItemStack at(Container inv, int row, int column) {
		return inv.getItem(row + column * 3);
	}

	/** Normal crafting first (the worktable is also a crafting table), then arcane recipes, then the wand recipe. */
	public static Match find(Container inv, Player player, Level level) {
		CraftingInput in = input(inv);
		if (level instanceof ServerLevel sl) {
			var normal = sl.recipeAccess().getRecipeFor(RecipeType.CRAFTING, in, sl);
			if (normal.isPresent()) {
				return new Match(normal.get().value().assemble(in), new AspectList(), false);
			}
			for (RecipeHolder<?> h : sl.getServer().getRecipeManager().getRecipes()) {
				if (h.value() instanceof ArcaneRecipe ar && ResearchManager.isResearchComplete(player, ar.research()) && ar.matches(in, level)) {
					return new Match(ar.assemble(in), ar.aspects(), true);
				}
			}
		} else {
			for (ArcaneRecipe ar : clientArcane) {
				if (ResearchManager.isResearchComplete(player, ar.research()) && ar.matches(in, level)) {
					return new Match(ar.assemble(in), ar.aspects(), true);
				}
			}
		}
		Match wand = wandRecipe(inv, player);
		return wand.result().isEmpty() ? sceptreRecipe(inv, player) : wand;
	}

	/** ArcaneSceptreRecipe: three matching caps around a rod with a primal charm in the top right; costs half again a wand's vis. */
	private static Match sceptreRecipe(Container inv, Player player) {
		if (!ResearchManager.isResearchComplete(player, "SCEPTRE") || !at(inv, 0, 0).isEmpty() || !at(inv, 0, 1).isEmpty()
			|| !at(inv, 1, 2).isEmpty() || !at(inv, 2, 2).isEmpty()) {
			return Match.NONE;
		}
		ItemStack cap1 = at(inv, 1, 0);
		ItemStack cap2 = at(inv, 2, 1);
		ItemStack cap3 = at(inv, 0, 2);
		ItemStack rod = at(inv, 1, 1);
		ItemStack charm = at(inv, 2, 0);
		if (cap1.isEmpty() || rod.isEmpty() || !charm.is(TVItems.PRIMAL_CHARM) || cap1.getItem() != cap2.getItem() || cap1.getItem() != cap3.getItem()) {
			return Match.NONE;
		}
		WandCap bc = null;
		WandRod br = null;
		for (WandCap wc : WandCap.caps.values()) {
			ItemStack ci = wc.getItem();
			if (!ci.isEmpty() && ci.getItem() == cap1.getItem() && ResearchManager.isResearchComplete(player, "CAP_" + wc.getTag())) {
				bc = wc;
				break;
			}
		}
		for (WandRod wr : WandRod.rods.values()) {
			ItemStack ri = wr.getItem();
			if (!ri.isEmpty() && ri.getItem() == rod.getItem() && ResearchManager.isResearchComplete(player, "ROD_" + wr.getTag())) {
				br = wr;
				break;
			}
		}
		if (bc == null || br == null) {
			return Match.NONE;
		}
		int cost = (int) (bc.getCraftCost() * br.getCraftCost() * 1.5F);
		AspectList al = new AspectList();
		for (Aspect as : Aspect.getPrimalAspects()) {
			al.add(as, cost);
		}
		ItemStack out = WandItem.create(TVItems.WAND, bc.getTag(), br.getTag());
		out.set(dev.neothaum.registry.TVComponents.SCEPTRE, true);
		return new Match(out, al, true);
	}

	private static Match wandRecipe(Container inv, Player player) {
		if (!at(inv, 0, 0).isEmpty() || !at(inv, 0, 1).isEmpty() || !at(inv, 1, 0).isEmpty() || !at(inv, 1, 2).isEmpty() || !at(inv, 2, 1).isEmpty()
			|| !at(inv, 2, 2).isEmpty()) {
			return Match.NONE;
		}
		ItemStack cap1 = at(inv, 0, 2);
		ItemStack cap2 = at(inv, 2, 0);
		ItemStack rod = at(inv, 1, 1);
		if (cap1.isEmpty() || cap2.isEmpty() || rod.isEmpty() || cap1.getItem() != cap2.getItem()) {
			return Match.NONE;
		}
		WandCap bc = null;
		WandRod br = null;
		for (WandCap wc : WandCap.caps.values()) {
			ItemStack ci = wc.getItem();
			if (!ci.isEmpty() && ci.getItem() == cap1.getItem() && ResearchManager.isResearchComplete(player, "CAP_" + wc.getTag())) {
				bc = wc;
				break;
			}
		}
		for (WandRod wr : WandRod.rods.values()) {
			ItemStack ri = wr.getItem();
			if (!ri.isEmpty() && ri.getItem() == rod.getItem() && ResearchManager.isResearchComplete(player, "ROD_" + wr.getTag())) {
				br = wr;
				break;
			}
		}
		if (bc == null || br == null || br.getTag().equals("wood") && bc.getTag().equals("iron")) {
			return Match.NONE;
		}
		int cost = bc.getCraftCost() * br.getCraftCost();
		AspectList al = new AspectList();
		for (Aspect as : Aspect.getPrimalAspects()) {
			al.add(as, cost);
		}
		return new Match(WandItem.create(TVItems.WAND, bc.getTag(), br.getTag()), al, true);
	}

	public static NonNullList<ItemStack> remainders(Container inv) {
		NonNullList<ItemStack> out = NonNullList.withSize(9, ItemStack.EMPTY);
		for (int i = 0; i < 9; i++) {
			ItemStack s = inv.getItem(i);
			var rem = s.getItem().getCraftingRemainder();
			if (rem != null) {
				out.set(i, rem.create());
			}
		}
		return out;
	}

	private ArcaneCraftingManager() {
	}
}
