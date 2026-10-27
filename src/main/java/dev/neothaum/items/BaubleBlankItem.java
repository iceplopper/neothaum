package dev.neothaum.items;

import dev.neothaum.baubles.BaubleItem;
import dev.neothaum.baubles.BaubleType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Port of ItemBaubleBlanks: the mundane amulet, ring and belt. */
public class BaubleBlankItem extends Item implements BaubleItem, dev.neothaum.api.RunicArmor {
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
