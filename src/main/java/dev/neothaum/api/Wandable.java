package dev.neothaum.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

/** IWandable: a block entity that reacts to being right-clicked with a wand (tubes toggle sides, alembics turn, the matrix starts). */
public interface Wandable {
	/** Returns true when the click was handled. */
	boolean onWandRightClick(Level level, ItemStack wand, Player player, BlockPos pos, Direction side, BlockHitResult hit);
}
