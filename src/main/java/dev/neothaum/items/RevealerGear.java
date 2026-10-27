package dev.neothaum.items;

import net.minecraft.world.item.ItemStack;

/** Port of IGoggles / IRevealer: gear that reveals aura nodes and object aspects. */
public interface RevealerGear {
	boolean showNodes(ItemStack stack);
}
