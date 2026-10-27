package dev.neothaum.items;

import dev.neothaum.baubles.BaubleItem;
import dev.neothaum.baubles.BaubleType;
import dev.neothaum.menu.FocusPouchMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

/** ItemFocusPouch (and ItemFocusPouchBauble): eighteen foci on a belt; worn or carried, the wand's focus key draws from it. */
public class FocusPouchItem extends Item implements BaubleItem {
	public static final int SIZE = 18;

	public FocusPouchItem(Item.Properties props) {
		super(props);
	}

	public static List<ItemStack> getInventory(ItemStack pouch) {
		NonNullList<ItemStack> list = NonNullList.withSize(SIZE, ItemStack.EMPTY);
		pouch.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(list);
		return list;
	}

	public static void setInventory(ItemStack pouch, List<ItemStack> items) {
		List<ItemStack> copy = new ArrayList<>();
		for (ItemStack s : items) {
			copy.add(s.copy());
		}
		pouch.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(copy));
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!level.isClientSide() && hand == InteractionHand.MAIN_HAND) {
			player.openMenu(new SimpleMenuProvider((id, inv, p) -> new FocusPouchMenu(id, inv), Component.translatable("item.neothaum.focus_pouch")));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public BaubleType getBaubleType(ItemStack stack) {
		return BaubleType.BELT;
	}
}
