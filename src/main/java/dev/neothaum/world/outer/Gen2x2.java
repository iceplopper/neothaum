package dev.neothaum.world.outer;

import dev.neothaum.registry.TVBlocks;
import net.minecraft.core.Direction;

/** Gen2x2: the four quarters of the boss room's hall. */
final class Gen2x2 {
	private Gen2x2() {
	}

	static void generateUpperLeft(GenCommon g, int cx, int cz, int y, Cell cell) {
		int x = cx * 16;
		int z = cz * 16;

		for (int a = 1; a <= 15; a++) {
			for (int b = 1; b <= 15; b++) {
				for (int c = 0; c < 13; c++) {
					if (a == 1 || b == 1) {
						g.placeBlock(x + a, y + c, z + b, 1, cell);
					}
				}
			}
		}

		for (int a = 2; a <= 15; a++) {
			for (int b = 2; b <= 15; b++) {
				for (int cxx = 1; cxx < 12; cxx++) {
					if ((a == 2 || b == 2) && (a != 2 || b <= 4 || b >= 12 || !cell.west || cxx >= 10) && (b != 2 || a <= 4 || a >= 12 || !cell.north || cxx >= 10)) {
						g.placeBlock(x + a, y + cxx, z + b, 8, cell);
					}
				}
			}
		}

		for (int a = 3; a <= 15; a++) {
			for (int b = 3; b <= 15; b++) {
				for (int cxxx = 2; cxxx < 11; cxxx++) {
					if (a == 3 || b == 3) {
						g.placeBlock(x + a, y + cxxx, z + b, 18, cell);
					}
				}
			}
		}

		for (int a = 2; a <= 15; a++) {
			for (int b = 2; b <= 15; b++) {
				g.placeBlock(x + a, y - 1, z + b, 1, cell);
				g.placeBlock(x + a, y, z + b, 8, cell);
				g.placeBlock(x + a, y + 1, z + b, 19, cell);
				g.placeBlock(x + a, y + 13, z + b, 1, cell);
				g.placeBlock(x + a, y + 12, z + b, 8, cell);
				g.placeBlock(x + a, y + 11, z + b, 2, cell);
			}
		}

		for (int gi = 4; gi <= 15; gi++) {
			g.placeBlock(x + gi, y + 2, z + 4, 10, Direction.NORTH, cell);
			g.placeBlock(x + gi, y + 10, z + 4, 11, Direction.NORTH, cell);
		}

		for (int gi = 4; gi <= 15; gi++) {
			g.placeBlock(x + 4, y + 2, z + gi, 10, Direction.WEST, cell);
			g.placeBlock(x + 4, y + 10, z + gi, 11, Direction.WEST, cell);
		}

		g.generateConnections(cx, cz, y, cell, 3, true);
	}

	static void generateUpperRight(GenCommon g, int cx, int cz, int y, Cell cell) {
		int x = cx * 16;
		int z = cz * 16;

		for (int a = 0; a <= 15; a++) {
			for (int b = 1; b <= 15; b++) {
				for (int c = 0; c < 13; c++) {
					if (a == 15 || b == 1) {
						g.placeBlock(x + a, y + c, z + b, 1, cell);
					}
				}
			}
		}

		for (int a = 0; a <= 14; a++) {
			for (int b = 2; b <= 15; b++) {
				for (int cxx = 1; cxx < 12; cxx++) {
					if ((a == 14 || b == 2)
						&& (a != 14 || b <= 4 || b >= 12 || !cell.east || cxx >= 10)
						&& (b != 2 || a <= 4 || a >= 12 || !cell.north || cxx >= 10)) {
						g.placeBlock(x + a, y + cxx, z + b, 8, cell);
					}
				}
			}
		}

		for (int a = 0; a <= 13; a++) {
			for (int b = 3; b <= 15; b++) {
				for (int cxxx = 2; cxxx < 11; cxxx++) {
					if (a == 13 || b == 3) {
						g.placeBlock(x + a, y + cxxx, z + b, 18, cell);
					}
				}
			}
		}

		for (int a = 0; a <= 14; a++) {
			for (int b = 2; b <= 15; b++) {
				g.placeBlock(x + a, y - 1, z + b, 1, cell);
				g.placeBlock(x + a, y, z + b, 8, cell);
				g.placeBlock(x + a, y + 1, z + b, 19, cell);
				g.placeBlock(x + a, y + 13, z + b, 1, cell);
				g.placeBlock(x + a, y + 12, z + b, 8, cell);
				g.placeBlock(x + a, y + 11, z + b, 2, cell);
			}
		}

		for (int gi = 0; gi <= 11; gi++) {
			g.placeBlock(x + gi, y + 2, z + 4, 10, Direction.NORTH, cell);
			g.placeBlock(x + gi, y + 10, z + 4, 11, Direction.NORTH, cell);
		}

		for (int gi = 4; gi <= 15; gi++) {
			g.placeBlock(x + 12, y + 2, z + gi, 10, Direction.EAST, cell);
			g.placeBlock(x + 12, y + 10, z + gi, 11, Direction.EAST, cell);
		}

		g.generateConnections(cx, cz, y, cell, 3, true);
	}

	static void generateLowerLeft(GenCommon g, int cx, int cz, int y, Cell cell) {
		int x = cx * 16;
		int z = cz * 16;

		for (int a = 1; a <= 15; a++) {
			for (int b = 0; b <= 15; b++) {
				for (int c = 0; c < 13; c++) {
					if (a == 1 || b == 15) {
						g.placeBlock(x + a, y + c, z + b, 1, cell);
					}
				}
			}
		}

		for (int a = 2; a <= 15; a++) {
			for (int b = 0; b <= 14; b++) {
				for (int cxx = 1; cxx < 12; cxx++) {
					if ((a == 2 || b == 14)
						&& (a != 2 || b <= 4 || b >= 12 || !cell.west || cxx >= 10)
						&& (b != 14 || a <= 4 || a >= 12 || !cell.south || cxx >= 10)) {
						g.placeBlock(x + a, y + cxx, z + b, 8, cell);
					}
				}
			}
		}

		for (int a = 3; a <= 15; a++) {
			for (int b = 0; b <= 13; b++) {
				for (int cxxx = 2; cxxx < 11; cxxx++) {
					if (a == 3 || b == 13) {
						g.placeBlock(x + a, y + cxxx, z + b, 18, cell);
					}
				}
			}
		}

		for (int a = 2; a <= 15; a++) {
			for (int b = 0; b <= 14; b++) {
				g.placeBlock(x + a, y - 1, z + b, 1, cell);
				g.placeBlock(x + a, y, z + b, 8, cell);
				g.placeBlock(x + a, y + 1, z + b, 19, cell);
				g.placeBlock(x + a, y + 13, z + b, 1, cell);
				g.placeBlock(x + a, y + 12, z + b, 8, cell);
				g.placeBlock(x + a, y + 11, z + b, 2, cell);
			}
		}

		for (int gi = 4; gi <= 15; gi++) {
			g.placeBlock(x + gi, y + 2, z + 12, 10, Direction.SOUTH, cell);
			g.placeBlock(x + gi, y + 10, z + 12, 11, Direction.SOUTH, cell);
		}

		for (int gi = 0; gi <= 11; gi++) {
			g.placeBlock(x + 4, y + 2, z + gi, 10, Direction.WEST, cell);
			g.placeBlock(x + 4, y + 10, z + gi, 11, Direction.WEST, cell);
		}

		g.generateConnections(cx, cz, y, cell, 3, true);
	}

	static void generateLowerRight(GenCommon g, int cx, int cz, int y, Cell cell) {
		int x = cx * 16;
		int z = cz * 16;

		for (int a = 0; a <= 15; a++) {
			for (int b = 0; b <= 15; b++) {
				for (int c = 0; c < 13; c++) {
					if (a == 15 || b == 15) {
						g.placeBlock(x + a, y + c, z + b, 1, cell);
					}
				}
			}
		}

		for (int a = 0; a <= 14; a++) {
			for (int b = 0; b <= 14; b++) {
				for (int cxx = 1; cxx < 12; cxx++) {
					if ((a == 14 || b == 14)
						&& (a != 14 || b <= 4 || b >= 12 || !cell.east || cxx >= 10)
						&& (b != 14 || a <= 4 || a >= 12 || !cell.south || cxx >= 10)) {
						g.placeBlock(x + a, y + cxx, z + b, 8, cell);
					}
				}
			}
		}

		for (int a = 0; a <= 13; a++) {
			for (int b = 0; b <= 13; b++) {
				for (int cxxx = 2; cxxx < 11; cxxx++) {
					if (a == 13 || b == 13) {
						g.placeBlock(x + a, y + cxxx, z + b, 18, cell);
					}
				}
			}
		}

		for (int a = 0; a <= 14; a++) {
			for (int b = 0; b <= 14; b++) {
				g.placeBlock(x + a, y - 1, z + b, 1, cell);
				g.placeBlock(x + a, y, z + b, 8, cell);
				g.placeBlock(x + a, y + 1, z + b, 19, cell);
				g.placeBlock(x + a, y + 13, z + b, 1, cell);
				g.placeBlock(x + a, y + 12, z + b, 8, cell);
				g.placeBlock(x + a, y + 11, z + b, 2, cell);
			}
		}

		for (int gi = 0; gi <= 11; gi++) {
			g.placeBlock(x + gi, y + 2, z + 12, 10, Direction.SOUTH, cell);
			g.placeBlock(x + gi, y + 10, z + 12, 11, Direction.SOUTH, cell);
		}

		for (int gi = 0; gi <= 12; gi++) {
			g.placeBlock(x + 12, y + 2, z + gi, 10, Direction.EAST, cell);
			g.placeBlock(x + 12, y + 10, z + gi, 11, Direction.EAST, cell);
		}

		g.generateConnections(cx, cz, y, cell, 3, true);
	}
}
