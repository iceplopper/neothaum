package dev.neothaum.items;

import dev.neothaum.api.aspects.Aspect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Port of thaumcraft.api.IVisDiscountGear. */
public interface VisDiscountGear {
	int getVisDiscount(ItemStack stack, Player player, Aspect aspect);
}
