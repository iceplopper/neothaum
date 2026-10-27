package dev.thaumvibe.items.equipment;

import dev.thaumvibe.api.RunicArmor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Port of ItemCultistLeaderArmor:
 * Imposing thaumium fortress-grade plate armor worn by the Crimson Praetor.
 */
public class CultistLeaderArmorItem extends Item implements RunicArmor {
	private final EquipmentSlot slot;

	public CultistLeaderArmorItem(EquipmentSlot slot, Properties props) {
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
