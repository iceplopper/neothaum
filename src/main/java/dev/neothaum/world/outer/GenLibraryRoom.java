package dev.neothaum.world.outer;

import dev.neothaum.registry.TVBlocks;
import net.minecraft.core.Direction;

/** GenLibraryRoom: shelves of glyphed stone and slabs around a central pedestal. */
final class GenLibraryRoom {
	private GenLibraryRoom() {
	}

	static void generateRoom(GenCommon g, int cx, int cz, int y, Cell cell) {
		int x = cx * 16;
		int z = cz * 16;

		for (int a = 1; a <= 15; a++) {
			for (int b = 1; b <= 15; b++) {
				for (int c = 0; c < 13; c++) {
					if (a == 1 || a == 15 || b == 1 || b == 15) {
						g.placeBlock(x + a, y + c, z + b, 1, cell);
					}
				}
			}
		}

		for (int a = 2; a <= 14; a++) {
			for (int b = 2; b <= 14; b++) {
				for (int cxx = 1; cxx < 12; cxx++) {
					if ((a == 2 || a == 14 || b == 2 || b == 14)
						&& (a != 2 || b <= 3 || b >= 12 || !cell.west || cxx >= 10)
						&& (a != 14 || b <= 3 || b >= 12 || !cell.east || cxx >= 10)
						&& (b != 2 || a <= 3 || a >= 12 || !cell.north || cxx >= 10)
						&& (b != 14 || a <= 3 || a >= 12 || !cell.south || cxx >= 10)) {
						g.placeBlock(x + a, y + cxx, z + b, 8, cell);
					}
				}
			}
		}

		for (int a = 3; a <= 13; a++) {
			for (int b = 3; b <= 13; b++) {
				for (int cxxx = 2; cxxx < 11; cxxx++) {
					if (a == 3 || a == 13 || b == 3 || b == 13) {
						g.placeBlock(x + a, y + cxxx, z + b, 2, cell);
					}
				}
			}
		}

		for (int a = 2; a <= 14; a++) {
			for (int b = 2; b <= 14; b++) {
				g.placeBlock(x + a, y - 1, z + b, 1, cell);
				g.placeBlock(x + a, y, z + b, 8, cell);
				g.placeBlock(x + a, y + 1, z + b, 2, cell);
				g.placeBlock(x + a, y + 12, z + b, 1, cell);
				g.placeBlock(x + a, y + 11, z + b, 8, cell);
				g.placeBlock(x + a, y + 10, z + b, 2, cell);
				if (a > 3 && a < 13 && b > 3 && b < 13) {
					if (a <= 5 && b <= 5 || a <= 5 && b >= 11 || a >= 11 && b <= 5 || a >= 11 && b >= 11) {
						g.placeBlock(x + a, y + 2, z + b, 2, cell);
						g.placeBlock(x + a, y + 9, z + b, 2, cell);
					}

					if (a == 5 && b == 5 || a == 5 && b == 11 || a == 11 && b == 5 || a == 11 && b == 11) {
						g.setTc(x + a, y + 3, z + b, "blockCosmeticSolid", 15);
						g.setTc(x + a, y + 8, z + b, "blockCosmeticSolid", 15);
					}
				}
			}
		}

		for (int gi = 0; gi < 5; gi++) {
			g.placeBlock(x + 6 + gi, y + 2, z + 4, 10, Direction.NORTH, cell);
			g.placeBlock(x + 6 + gi, y + 2, z + 12, 10, Direction.SOUTH, cell);
			g.placeBlock(x + 12, y + 2, z + 6 + gi, 10, Direction.EAST, cell);
			g.placeBlock(x + 4, y + 2, z + 6 + gi, 10, Direction.WEST, cell);
			g.placeBlock(x + 6 + gi, y + 9, z + 4, 11, Direction.NORTH, cell);
			g.placeBlock(x + 6 + gi, y + 9, z + 12, 11, Direction.SOUTH, cell);
			g.placeBlock(x + 12, y + 9, z + 6 + gi, 11, Direction.EAST, cell);
			g.placeBlock(x + 4, y + 9, z + 6 + gi, 11, Direction.WEST, cell);
		}

		g.setTc(x + 5, y + 4, z + 5, "blockEldritch", 5);
		g.setTc(x + 5, y + 5, z + 5, "blockSlabStone", 1);
		g.setTc(x + 5, y + 4, z + 11, "blockEldritch", 5);
		g.setTc(x + 5, y + 5, z + 11, "blockSlabStone", 1);
		g.setTc(x + 11, y + 4, z + 5, "blockEldritch", 5);
		g.setTc(x + 11, y + 5, z + 5, "blockSlabStone", 1);
		g.setTc(x + 11, y + 4, z + 11, "blockEldritch", 5);
		g.setTc(x + 11, y + 5, z + 11, "blockSlabStone", 1);
		g.setTc(x + 5, y + 7, z + 5, "blockEldritch", 5);
		g.setTc(x + 5, y + 6, z + 5, "blockSlabStone", 9);
		g.setTc(x + 5, y + 7, z + 11, "blockEldritch", 5);
		g.setTc(x + 5, y + 6, z + 11, "blockSlabStone", 9);
		g.setTc(x + 11, y + 7, z + 5, "blockEldritch", 5);
		g.setTc(x + 11, y + 6, z + 5, "blockSlabStone", 9);
		g.setTc(x + 11, y + 7, z + 11, "blockEldritch", 5);
		g.setTc(x + 11, y + 6, z + 11, "blockSlabStone", 9);
		g.setTc(x + 8, y + 2, z + 8, "blockCosmeticSolid", 15);
		g.setTc(x + 8, y + 3, z + 8, "blockEldritch", 5);
		g.setTc(x + 8, y + 4, z + 8, "blockSlabStone", 1);
		g.setTc(x + 8, y + 9, z + 8, "blockCosmeticSolid", 15);
		g.setTc(x + 8, y + 8, z + 8, "blockEldritch", 5);
		g.setTc(x + 8, y + 7, z + 8, "blockSlabStone", 9);
		g.generateConnections(cx, cz, y, cell, 3, true);
	}
}
