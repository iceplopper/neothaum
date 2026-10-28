package dev.neothaum.world.outer;

import dev.neothaum.registry.TVBlocks;
import net.minecraft.core.Direction;

/** GenBossRoom: one quarter of the 2x2 boss room, and the sealed doorway with its lock where the maze comes in. */
final class GenBossRoom {
	private GenBossRoom() {
	}

	static final int[][] PAT_DOORWAY = new int[][]{
		{0, 2, 2, 2, 2, 2, 0},
		{2, 2, 9, 9, 9, 2, 2},
		{2, 9, 9, 9, 9, 9, 2},
		{2, 9, 9, 1, 9, 9, 2},
		{2, 9, 9, 9, 9, 9, 2},
		{2, 2, 9, 9, 9, 2, 2},
		{0, 2, 2, 2, 2, 2, 0}
	};

	static void generateRoom(GenCommon g, int cx, int cz, int y, Cell cell) {
		int x = cx * 16;
		int z = cz * 16;
		switch (cell.feature) {
			case 2:
				Gen2x2.generateUpperLeft(g, cx, cz, y, cell);
				break;
			case 3:
				Gen2x2.generateUpperRight(g, cx, cz, y, cell);
				break;
			case 4:
				Gen2x2.generateLowerLeft(g, cx, cz, y, cell);
				break;
			case 5:
				Gen2x2.generateLowerRight(g, cx, cz, y, cell);
		}

		for (int a = 0; a < 7; a++) {
			for (int b = 0; b < 7; b++) {
				int xx = 0;
				int zz = 0;
				Direction dir = null;
				if (cell.north) {
					xx = x + 5 + a;
					zz = z + 3;
					dir = Direction.NORTH;
				}

				if (cell.south) {
					xx = x + 5 + a;
					zz = z + 13;
					dir = Direction.SOUTH;
				}

				if (cell.east) {
					xx = x + 13;
					zz = z + 5 + a;
					dir = Direction.EAST;
				}

				if (cell.west) {
					xx = x + 3;
					zz = z + 5 + a;
					dir = Direction.WEST;
				}

				if (dir == null) {
					// a quarter with no way in has no doorway (TC4 drew one at the world's origin)
					continue;
				}
				switch (PAT_DOORWAY[a][b]) {
					case 1:
						g.placeBlock(xx, y + 2 + b, zz, 16, cell);
						g.set(xx, y + 2 + b, zz, TVBlocks.ANCIENT_LOCK.defaultBlockState().setValue(dev.neothaum.blocks.eldritch.AncientLockBlock.FACING, dir));
						break;
					case 2:
						g.placeBlock(xx, y + 2 + b, zz, 15, cell);
						break;
					case 9:
						g.placeBlock(xx, y + 2 + b, zz, 17, cell);
				}
			}
		}
	}
}
