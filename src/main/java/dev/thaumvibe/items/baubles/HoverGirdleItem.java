package dev.thaumvibe.items.baubles;

import dev.thaumvibe.api.RunicArmor;
import dev.thaumvibe.baubles.BaubleItem;
import dev.thaumvibe.baubles.BaubleType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** ItemGirdleHover, the Thaumostatic Girdle: whittles away fall distance as you drop. */
public class HoverGirdleItem extends Item implements BaubleItem, RunicArmor {
	public HoverGirdleItem(Properties props) {
		super(props);
	}

	@Override
	public BaubleType getBaubleType(ItemStack stack) {
		return BaubleType.BELT;
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity wearer) {
		if (wearer.fallDistance > 0.0F) {
			wearer.fallDistance -= 0.33F;
		}
	}

	@Override
	public int getRunicCharge(ItemStack stack) {
		return 0;
	}
}
