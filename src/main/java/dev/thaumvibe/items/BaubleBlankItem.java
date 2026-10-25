package dev.thaumvibe.items;

import dev.thaumvibe.baubles.BaubleItem;
import dev.thaumvibe.baubles.BaubleType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Port of ItemBaubleBlanks: the mundane amulet, ring and belt. */
public class BaubleBlankItem extends Item implements BaubleItem, dev.thaumvibe.api.RunicArmor {
	private final BaubleType type;

	public BaubleBlankItem(BaubleType type, Item.Properties props) {
		super(props);
		this.type = type;
	}

	@Override
	public BaubleType getBaubleType(ItemStack stack) {
		return this.type;
	}

	@Override
	public int getRunicCharge(ItemStack stack) {
		return 0;
	}
}
