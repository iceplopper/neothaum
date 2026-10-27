package dev.thaumvibe.world;

import dev.thaumvibe.registry.TVBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * 1:1 Port of ComponentBankerHome.
 * Traditional village dwelling with a banking counter, cobblestone foundation, and oak structure.
 */
public final class BankerHomeFeature {

	private BankerHomeFeature() {}

	public static boolean generate(WorldGenLevel world, RandomSource random, BlockPos origin) {
		int cx = origin.getX();
		int cy = origin.getY();
		int cz = origin.getZ();

		// Dimensions: 5x6 base, 5 blocks tall
		for (int x = -2; x <= 2; x++) {
			for (int z = -2; z <= 3; z++) {
				// Foundation
				BlockPos floor = new BlockPos(cx + x, cy, cz + z);
				world.setBlock(floor, Blocks.COBBLESTONE.defaultBlockState(), 3);

				boolean isWall = Math.abs(x) == 2 || z == -2 || z == 3;
				for (int y = 1; y <= 4; y++) {
					BlockPos p = floor.above(y);
					if (isWall) {
						if (y == 4) {
							world.setBlock(p, Blocks.OAK_LOG.defaultBlockState(), 3);
						} else {
							world.setBlock(p, Blocks.OAK_PLANKS.defaultBlockState(), 3);
						}
					} else {
						world.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
					}
				}
				// Roof
				world.setBlock(floor.above(5), Blocks.OAK_PLANKS.defaultBlockState(), 3);
			}
		}

		// Door on North face
		BlockPos doorBottom = new BlockPos(cx, cy + 1, cz - 2);
		BlockPos doorTop = doorBottom.above();
		world.setBlock(doorBottom, Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER).setValue(DoorBlock.FACING, Direction.NORTH), 3);
		world.setBlock(doorTop, Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER).setValue(DoorBlock.FACING, Direction.NORTH), 3);

		// Banking counter table
		world.setBlock(new BlockPos(cx - 1, cy + 1, cz), TVBlocks.TABLE.defaultBlockState(), 3);
		world.setBlock(new BlockPos(cx, cy + 1, cz), TVBlocks.TABLE.defaultBlockState(), 3);

		// Storage chest behind counter
		world.setBlock(new BlockPos(cx - 1, cy + 1, cz + 2), Blocks.CHEST.defaultBlockState(), 3);

		return true;
	}
}
