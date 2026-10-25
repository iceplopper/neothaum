package dev.thaumvibe.api;

import dev.thaumvibe.registry.TVComponents;
import net.minecraft.world.item.ItemStack;

/** IRunicArmor: gear that holds a runic shield charge, and that the runic augment infusion can harden. */
public interface RunicArmor {
	int getRunicCharge(ItemStack stack);

	/** EventHandlerRunic.getFinalCharge: the item's own charge plus any hardening. */
	static int getFinalCharge(ItemStack stack) {
		if (!(stack.getItem() instanceof RunicArmor armor)) {
			return 0;
		}
		return armor.getRunicCharge(stack) + getHardening(stack);
	}

	/** "RS.HARDEN": levels of runic augment infused into the item. */
	static int getHardening(ItemStack stack) {
		return stack.getItem() instanceof RunicArmor ? stack.getOrDefault(TVComponents.RUNIC_HARDENING, 0) : 0;
	}
}
