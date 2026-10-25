package dev.thaumvibe.items;

import dev.thaumvibe.api.RunicArmor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** ItemThaumiumArmor: plain armour, but runic augments take to it. */
public class ThaumiumArmorItem extends Item implements RunicArmor {
	public ThaumiumArmorItem(Properties props) {
		super(props);
	}

	@Override
	public int getRunicCharge(ItemStack stack) {
		return 0;
	}
}
