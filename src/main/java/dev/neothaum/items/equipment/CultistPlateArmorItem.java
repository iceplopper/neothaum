package dev.neothaum.items.equipment;

import dev.neothaum.api.RunicArmor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Port of ItemCultistPlateArmor:
 * Forged iron plate armour worn by the Crimson Knights.
 */
public class CultistPlateArmorItem extends Item implements RunicArmor {
	private final EquipmentSlot slot;

	public CultistPlateArmorItem(EquipmentSlot slot, Properties props) {
		super(props);
		this.slot = slot;
	}

	public EquipmentSlot getSlot() {
		return this.slot;
	}

	@Override
	public int getRunicCharge(ItemStack stack) {
		return 0;
	}
}
