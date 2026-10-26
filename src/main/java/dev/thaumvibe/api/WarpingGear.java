package dev.thaumvibe.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** IWarpingGear: gear that adds to its wearer's or holder's warp while carried (void metal, the primal crusher). */
public interface WarpingGear {
	int getWarp(ItemStack stack, Player player);
}
