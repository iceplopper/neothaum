package dev.thaumvibe.api.wands;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Port of thaumcraft.api.IArchitect: a focus that works on an area the player sizes with the misc key, previewed as an outline. */
public interface Architect {
	enum Axis { X, Y, Z }

	List<BlockPos> getArchitectBlocks(ItemStack wand, Level level, BlockPos pos, Direction side, Player player);

	boolean showAxis(ItemStack wand, Level level, Player player, Direction side, Axis axis);
}
