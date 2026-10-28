package dev.neothaum.golem;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * InventoryUtils (the parts golems use) and InventoryMob's filter matching. Inventories are reached through a side, as TC4's ISidedInventory
 * was; a double chest is one container. The 1.7 ore dictionary is the common tags: two stacks are "ore dictionary equal" when they share a
 * specific c: tag such as c:ingots/iron.
 */
public final class GolemInventory {
	private GolemInventory() {
	}

	/** The inventory at a block, a double chest as a whole. */
	public static @Nullable Container getContainer(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (state.getBlock() instanceof ChestBlock chest) {
			Container c = ChestBlock.getContainer(chest, state, level, pos, true);
			if (c != null) {
				return c;
			}
		}
		return level.getBlockEntity(pos) instanceof Container c ? c : null;
	}

	private static int[] slots(Container inv, int side) {
		if (inv instanceof WorldlyContainer w && side > -1) {
			return w.getSlotsForFace(Direction.from3DDataValue(side));
		}
		int[] all = new int[inv.getContainerSize()];
		for (int i = 0; i < all.length; i++) {
			all[i] = i;
		}
		return all;
	}

	/** placeItemStackIntoInventory: what is left of the stack after putting it in (empty when it all fits). */
	public static ItemStack placeItemStackIntoInventory(ItemStack stack, Container inv, int side, boolean doit) {
		ItemStack left = insertStack(inv, stack.copy(), side, doit);
		if (left.isEmpty() && doit) {
			inv.setChanged();
		}
		return left;
	}

	private static ItemStack insertStack(Container inv, ItemStack stack, int side, boolean doit) {
		int[] slots = slots(inv, side);
		// stacks of the same item first, then empty slots
		for (int s : slots) {
			if (stack.isEmpty()) {
				break;
			}
			if (!inv.getItem(s).isEmpty() && ItemStack.isSameItem(inv.getItem(s), stack)) {
				stack = attemptInsertion(inv, stack, s, side, doit);
			}
		}
		for (int s : slots) {
			if (stack.isEmpty()) {
				break;
			}
			stack = attemptInsertion(inv, stack, s, side, doit);
		}
		return stack;
	}

	private static ItemStack attemptInsertion(Container inv, ItemStack stack, int slot, int side, boolean doit) {
		ItemStack slotStack = inv.getItem(slot);
		if (!canInsert(inv, stack, slot, side)) {
			return stack;
		}
		boolean flag = false;
		if (slotStack.isEmpty()) {
			int max = Math.min(inv.getMaxStackSize(), stack.getMaxStackSize());
			if (max < stack.getCount()) {
				ItemStack in = stack.split(max);
				if (doit) {
					inv.setItem(slot, in);
				}
			} else {
				if (doit) {
					inv.setItem(slot, stack);
				}
				stack = ItemStack.EMPTY;
			}
			flag = true;
		} else if (areItemStacksEqualStrict(slotStack, stack)) {
			int k = Math.min(inv.getMaxStackSize() - slotStack.getCount(), stack.getMaxStackSize() - slotStack.getCount());
			int l = Math.min(stack.getCount(), k);
			if (l > 0) {
				stack = stack.copy();
				stack.shrink(l);
				if (doit) {
					slotStack.grow(l);
				}
				flag = true;
			}
		}
		if (flag && doit) {
			inv.setChanged();
		}
		return stack;
	}

	public static boolean canInsert(Container inv, ItemStack stack, int slot, int side) {
		return !stack.isEmpty() && inv.canPlaceItem(slot, stack)
			&& (!(inv instanceof WorldlyContainer w) || side < 0 || w.canPlaceItemThroughFace(slot, stack, Direction.from3DDataValue(side)));
	}

	public static boolean canExtract(Container inv, ItemStack stack, int slot, int side) {
		return !stack.isEmpty() && (!(inv instanceof WorldlyContainer w) || side < 0 || w.canTakeItemThroughFace(slot, stack, Direction.from3DDataValue(side)));
	}

	public static boolean inventoryContains(Container inv, ItemStack stack, int side, boolean useOre, boolean ignoreDamage, boolean ignoreNBT) {
		return !extractStack(inv, stack, side, useOre, ignoreDamage, ignoreNBT, false).isEmpty();
	}

	/** extractStack: up to the stack's count of a matching item from the first slot holding one. */
	public static ItemStack extractStack(Container inv, ItemStack stack, int side, boolean useOre, boolean ignoreDamage, boolean ignoreNBT, boolean doit) {
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		for (int s : slots(inv, side)) {
			ItemStack out = attemptExtraction(inv, stack, s, side, useOre, ignoreDamage, ignoreNBT, doit);
			if (!out.isEmpty()) {
				return out.copy();
			}
		}
		return ItemStack.EMPTY;
	}

	public static ItemStack attemptExtraction(Container inv, ItemStack stack, int slot, int side, boolean useOre, boolean ignoreDamage, boolean ignoreNBT,
		boolean doit) {
		ItemStack slotStack = inv.getItem(slot);
		if (!canExtract(inv, slotStack, slot, side) || !areItemStacksEqual(slotStack, stack, useOre, ignoreDamage, ignoreNBT)) {
			return ItemStack.EMPTY;
		}
		ItemStack out = slotStack.copyWithCount(Math.min(stack.getCount(), slotStack.getCount()));
		if (doit) {
			if (stack.getCount() >= slotStack.getCount()) {
				inv.setItem(slot, ItemStack.EMPTY);
			} else {
				slotStack.shrink(out.getCount());
				inv.setItem(slot, slotStack);
			}
			inv.setChanged();
		}
		return out;
	}

	/** The stacks in a container as seen from one side. */
	public static List<ItemStack> contents(Container inv, int side) {
		List<ItemStack> out = new ArrayList<>();
		for (int s : slots(inv, side)) {
			if (!inv.getItem(s).isEmpty()) {
				out.add(inv.getItem(s).copy());
			}
		}
		return out;
	}

	/** Slot numbers of a container from one side (for GolemHelper's first-item search). */
	public static int[] sideSlots(Container inv, int side) {
		return slots(inv, side);
	}

	public static boolean areItemStacksEqualStrict(ItemStack a, ItemStack b) {
		return areItemStacksEqual(a, b, false, false, false);
	}

	public static boolean areItemStacksEqual(ItemStack a, ItemStack b, boolean useOre, boolean ignoreDamage, boolean ignoreNBT) {
		if (a.isEmpty() || b.isEmpty()) {
			return a.isEmpty() && b.isEmpty();
		}
		if (useOre && shareOreTag(a, b)) {
			return true;
		}
		if (!a.is(b.getItem())) {
			return false;
		}
		boolean damageDiffers = a.getDamageValue() != b.getDamageValue() && !(ignoreDamage && a.isDamageableItem() && b.isDamageableItem());
		if (damageDiffers) {
			return false;
		}
		if (ignoreNBT) {
			return true;
		}
		return withoutDamage(a).equals(withoutDamage(b));
	}

	private static DataComponentPatch withoutDamage(ItemStack s) {
		return s.getComponentsPatch().forget(t -> t == DataComponents.DAMAGE);
	}

	/** OreDictionary.getOreID: the stack's most specific common tag (c:ingots/iron rather than c:ingots), if it has one. */
	public static @Nullable TagKey<Item> oreTag(ItemStack stack) {
		TagKey<Item> best = null;
		for (TagKey<Item> t : stack.typeHolder().tags().toList()) {
			if (t.location().getNamespace().equals("c") && t.location().getPath().contains("/")) {
				if (best == null || t.location().getPath().length() > best.location().getPath().length()) {
					best = t;
				}
			}
		}
		return best;
	}

	public static boolean shareOreTag(ItemStack a, ItemStack b) {
		TagKey<Item> t = oreTag(a);
		return t != null && b.is(t);
	}

	/** Every item that shares a stack's ore tag (OreDictionary.getOres), or just the stack. */
	public static List<ItemStack> oreVariants(ItemStack stack) {
		TagKey<Item> t = oreTag(stack);
		List<ItemStack> out = new ArrayList<>();
		if (t == null) {
			out.add(stack.copy());
			return out;
		}
		for (var h : BuiltInRegistries.ITEM.getTagOrEmpty(t)) {
			out.add(new ItemStack(h, stack.getCount()));
		}
		if (out.isEmpty()) {
			out.add(stack.copy());
		}
		return out;
	}
}
