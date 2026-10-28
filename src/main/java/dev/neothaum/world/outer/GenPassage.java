package dev.neothaum.world.outer;

import dev.neothaum.registry.TVBlocks;
import net.minecraft.core.Direction;

/** GenPassage: a plain cell of corridor, with the trapped, crusted, tainted and spider-hall variants. */
final class GenPassage {
	private GenPassage() {
	}

	static void generateDefaultPassage(GenCommon g, int cx, int cz, int y, Cell cell) {
		int x = cx * 16;
		int z = cz * 16;
		g.generateConnections(cx, cz, y, cell, 4, false);
		int mod = 0;
		if (cell.north && cell.south && cell.west && cell.east && g.rand.nextBoolean()) {
			mod = 1;
		}

		for (int w = 1; w < 8; w++) {
			for (int h = 1; h < 8; h++) {
				if (w == 4 && h == 4 && mod == 1) {
					g.placeBlock(x + 4 + w, y + 2, z + 4 + h, 7, cell);
					g.placeBlock(x + 4 + w, y + 8, z + 4 + h, 7, cell);
				} else {
					g.placeBlock(x + 4 + w, y + 2, z + 4 + h, cell.feature == 11 && g.rand.nextInt(3) == 0 ? 20 : 2, cell);
					g.placeBlock(x + 4 + w, y + 8, z + 4 + h, cell.feature == 11 && g.rand.nextInt(3) == 0 ? 20 : 2, cell);
				}

				g.placeBlock(x + 4 + w, y, z + 4 + h, 1, cell);
				g.placeBlock(x + 4 + w, y + 10, z + 4 + h, 1, cell);
				g.placeBlock(x + 4 + w, y + 1, z + 4 + h, 8, cell);
				g.placeBlock(x + 4 + w, y + 9, z + 4 + h, 8, cell);
			}
		}

		if (cell.north) {
			for (int w = 2 + mod; w < 9 - mod; w++) {
				for (int h = 2 + mod; h < 9 - mod; h++) {
					g.placeBlock(x + 3 + w, y + 10 - h, z + 5, GenCommon.PAT_CONNECT[h][w], Direction.NORTH, cell);
				}
			}

			if (mod == 0) {
				if (cell.west) {
					g.placeBlock(x + 6, y + 3, z + 6, 3, Direction.EAST, cell);
					g.placeBlock(x + 6, y + 7, z + 6, 5, Direction.EAST, cell);
				}

				if (cell.east) {
					g.placeBlock(x + 10, y + 3, z + 6, 3, Direction.EAST, cell);
					g.placeBlock(x + 10, y + 7, z + 6, 5, Direction.EAST, cell);
				}
			}
		} else {
			for (int w = 1; w < 8; w++) {
				for (int h = 1; h < 8; h++) {
					g.placeBlock(x + 4 + w, y + 9 - h, z + 5, cell.feature == 11 && g.rand.nextInt(3) == 0 ? 20 : 2, cell);
					g.placeBlock(x + 4 + w, y + 9 - h, z + 4, 8, cell);
					g.placeBlock(x + 4 + w, y + 9 - h, z + 3, 1, cell);
					if (h == 7) {
						g.placeBlock(x + 4 + w, y + 1, z + 4, 1, cell);
						g.placeBlock(x + 4 + w, y + 9, z + 4, 1, cell);
					}

					if (w == 7) {
						g.placeBlock(x + 4, y + 9 - h, z + 4, 1, cell);
						g.placeBlock(x + 12, y + 9 - h, z + 4, 1, cell);
					}
				}
			}

			for (int w = 2; w < 7; w++) {
				g.placeBlock(x + 4 + w, y + 3, z + 6, 3, Direction.EAST, cell);
				g.placeBlock(x + 4 + w, y + 7, z + 6, 5, Direction.EAST, cell);
			}
		}

		if (cell.south) {
			for (int w = 2 + mod; w < 9 - mod; w++) {
				for (int h = 2 + mod; h < 9 - mod; h++) {
					g.placeBlock(x + 3 + w, y + 10 - h, z + 11, GenCommon.PAT_CONNECT[h][w], Direction.SOUTH, cell);
				}
			}

			if (mod == 0) {
				if (cell.west) {
					g.placeBlock(x + 6, y + 3, z + 10, 4, Direction.EAST, cell);
					g.placeBlock(x + 6, y + 7, z + 10, 6, Direction.EAST, cell);
				}

				if (cell.east) {
					g.placeBlock(x + 10, y + 3, z + 10, 4, Direction.EAST, cell);
					g.placeBlock(x + 10, y + 7, z + 10, 6, Direction.EAST, cell);
				}
			}
		} else {
			for (int w = 1; w < 8; w++) {
				for (int h = 1; h < 8; h++) {
					g.placeBlock(x + 4 + w, y + 9 - h, z + 11, cell.feature == 11 && g.rand.nextInt(3) == 0 ? 20 : 2, cell);
					g.placeBlock(x + 4 + w, y + 9 - h, z + 12, 8, cell);
					g.placeBlock(x + 4 + w, y + 9 - h, z + 13, 1, cell);
					if (h == 7) {
						g.placeBlock(x + 4 + w, y + 1, z + 12, 1, cell);
						g.placeBlock(x + 4 + w, y + 9, z + 12, 1, cell);
					}

					if (w == 7) {
						g.placeBlock(x + 4, y + 9 - h, z + 12, 1, cell);
						g.placeBlock(x + 12, y + 9 - h, z + 12, 1, cell);
					}
				}
			}

			for (int w = 2; w < 7; w++) {
				g.placeBlock(x + 4 + w, y + 3, z + 10, 4, Direction.EAST, cell);
				g.placeBlock(x + 4 + w, y + 7, z + 10, 6, Direction.EAST, cell);
			}
		}

		if (cell.east) {
			for (int w = 2 + mod; w < 9 - mod; w++) {
				for (int h = 2 + mod; h < 9 - mod; h++) {
					g.placeBlock(x + 11, y + 10 - h, z + 3 + w, GenCommon.PAT_CONNECT[h][w], Direction.EAST, cell);
				}
			}

			if (mod == 0) {
				if (cell.north) {
					g.placeBlock(x + 10, y + 3, z + 6, 4, Direction.NORTH, cell);
					g.placeBlock(x + 10, y + 7, z + 6, 6, Direction.NORTH, cell);
				}

				if (cell.south) {
					g.placeBlock(x + 10, y + 3, z + 10, 4, Direction.NORTH, cell);
					g.placeBlock(x + 10, y + 7, z + 10, 6, Direction.NORTH, cell);
				}
			}
		} else {
			for (int w = 1; w < 8; w++) {
				for (int h = 1; h < 8; h++) {
					g.placeBlock(x + 11, y + 9 - h, z + 4 + w, cell.feature == 11 && g.rand.nextInt(3) == 0 ? 20 : 2, cell);
					g.placeBlock(x + 12, y + 9 - h, z + 4 + w, 8, cell);
					g.placeBlock(x + 13, y + 9 - h, z + 4 + w, 1, cell);
					if (h == 7) {
						g.placeBlock(x + 12, y + 1, z + 4 + w, 1, cell);
						g.placeBlock(x + 12, y + 9, z + 4 + w, 1, cell);
					}

					if (w == 7) {
						g.placeBlock(x + 12, y + 9 - h, z + 4, 1, cell);
						g.placeBlock(x + 12, y + 9 - h, z + 12, 1, cell);
					}
				}
			}

			for (int w = 2; w < 7; w++) {
				g.placeBlock(x + 10, y + 3, z + 4 + w, 4, Direction.NORTH, cell);
				g.placeBlock(x + 10, y + 7, z + 4 + w, 6, Direction.NORTH, cell);
			}
		}

		if (cell.west) {
			for (int w = 2 + mod; w < 9 - mod; w++) {
				for (int h = 2 + mod; h < 9 - mod; h++) {
					g.placeBlock(x + 5, y + 10 - h, z + 3 + w, GenCommon.PAT_CONNECT[h][w], Direction.WEST, cell);
				}
			}

			if (mod == 0) {
				if (cell.north) {
					g.placeBlock(x + 6, y + 3, z + 6, 3, Direction.NORTH, cell);
					g.placeBlock(x + 6, y + 7, z + 6, 5, Direction.NORTH, cell);
				}

				if (cell.south) {
					g.placeBlock(x + 6, y + 3, z + 10, 3, Direction.NORTH, cell);
					g.placeBlock(x + 6, y + 7, z + 10, 5, Direction.NORTH, cell);
				}
			}
		} else {
			for (int w = 1; w < 8; w++) {
				for (int h = 1; h < 8; h++) {
					g.placeBlock(x + 5, y + 9 - h, z + 4 + w, cell.feature == 11 && g.rand.nextInt(3) == 0 ? 20 : 2, cell);
					g.placeBlock(x + 4, y + 9 - h, z + 4 + w, 8, cell);
					g.placeBlock(x + 3, y + 9 - h, z + 4 + w, 1, cell);
					if (h == 7) {
						g.placeBlock(x + 4, y + 1, z + 4 + w, 1, cell);
						g.placeBlock(x + 4, y + 9, z + 4 + w, 1, cell);
					}

					if (w == 7) {
						g.placeBlock(x + 4, y + 9 - h, z + 4, 1, cell);
						g.placeBlock(x + 4, y + 9 - h, z + 12, 1, cell);
					}
				}
			}

			for (int w = 2; w < 7; w++) {
				g.placeBlock(x + 6, y + 3, z + 4 + w, 3, Direction.NORTH, cell);
				g.placeBlock(x + 6, y + 7, z + 4 + w, 5, Direction.NORTH, cell);
			}
		}

		if (mod == 1) {
			g.placeBlock(x + 5, y + 3, z + 5, 3, Direction.EAST, cell);
			g.placeBlock(x + 5, y + 7, z + 5, 5, Direction.EAST, cell);
			g.placeBlock(x + 5, y + 3, z + 6, 3, Direction.NORTH, cell);
			g.placeBlock(x + 5, y + 7, z + 6, 5, Direction.NORTH, cell);
			g.placeBlock(x + 11, y + 3, z + 5, 3, Direction.EAST, cell);
			g.placeBlock(x + 11, y + 7, z + 5, 5, Direction.EAST, cell);
			g.placeBlock(x + 11, y + 3, z + 6, 4, Direction.NORTH, cell);
			g.placeBlock(x + 11, y + 7, z + 6, 6, Direction.NORTH, cell);
			g.placeBlock(x + 5, y + 3, z + 11, 3, Direction.NORTH, cell);
			g.placeBlock(x + 5, y + 7, z + 11, 5, Direction.NORTH, cell);
			g.placeBlock(x + 6, y + 3, z + 11, 4, Direction.EAST, cell);
			g.placeBlock(x + 6, y + 7, z + 11, 6, Direction.EAST, cell);
			g.placeBlock(x + 11, y + 3, z + 11, 4, Direction.NORTH, cell);
			g.placeBlock(x + 11, y + 7, z + 11, 6, Direction.NORTH, cell);
			g.placeBlock(x + 10, y + 3, z + 11, 4, Direction.EAST, cell);
			g.placeBlock(x + 10, y + 7, z + 11, 6, Direction.EAST, cell);
		}

		if (cell.feature == 12) {
			for (int w = -4; w <= 4; w++) {
				for (int h = -4; h < 5; h++) {
					for (int j = -4; j <= 4; j++) {
						if ((
								g.isAir(x + 8 + w, y + 4 + h, z + 8 + j)
									|| g.isAncientStone(x + 8 + w, y + 4 + h, z + 8 + j)
									|| g.get(x + 8 + w, y + 4 + h, z + 8 + j).is(TVBlocks.ANCIENT_STONE_STAIRS)
							)
							&& g.rand.nextBoolean()) {
							g.placeBlock(x + 8 + w, y + 4 + h, z + 8 + j, 21, cell);
						}
					}
				}
			}
		}

		if (cell.feature == 13) {
			for (int w = -4; w <= 4; w++) {
				for (int h = -3; h <= 3; h++) {
					for (int jx = -4; jx <= 4; jx++) {
						if (g.isAir(x + 8 + w, y + 4 + h, z + 8 + jx) && g.isAdjacentToSolid(x + 8 + w, y + 4 + h, z + 8 + jx)) {
							if (g.rand.nextInt(3) != 0) {
								g.setTc(x + 8 + w, y + 4 + h, z + 8 + jx, "blockTaintFibres", g.rand.nextInt(4) == 0 ? 1 : 0);
							}

							// (TC4 also turned these columns to Tainted Land; biomes can't safely be changed mid-generation here)
						}
					}
				}
			}
		}

		if (cell.feature == 14) {
			for (int w = -3; w <= 3; w++) {
				for (int h = -3; h <= 3; h++) {
					for (int jxx = -3; jxx <= 3; jxx++) {
						if (g.isAir(x + 8 + w, y + 4 + h, z + 8 + jxx) && g.rand.nextFloat() < 0.35F) {
							g.setTc(x + 8 + w, y + 4 + h, z + 8 + jxx, "web", 0);
						}

						g.spawner(x + 8, y + 4, z + 8, dev.neothaum.entity.TVEntities.MIND_SPIDER);
					}
				}
			}
		}
	}
}
