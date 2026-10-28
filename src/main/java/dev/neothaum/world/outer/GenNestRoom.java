package dev.neothaum.world.outer;

import dev.neothaum.registry.TVBlocks;
import net.minecraft.core.Direction;

/** GenNestRoom: a crusted cave of strange crystals and crab vents, with urns and crates on its floor. */
final class GenNestRoom {
	private GenNestRoom() {
	}

	static void generateRoom(GenCommon g, int cx, int cz, int y, Cell cell) {
		int x = cx * 16;
		int z = cz * 16;

		for (int a = 1; a <= 15; a++) {
			for (int b = 1; b <= 15; b++) {
				for (int c = 0; c < 11; c++) {
					if (a == 1 || a == 15 || b == 1 || b == 15) {
						g.placeBlock(x + a, y + c, z + b, 1, cell);
					}
				}
			}
		}

		for (int a = 2; a <= 14; a++) {
			for (int b = 2; b <= 14; b++) {
				for (int cxx = 1; cxx < 10; cxx++) {
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
				for (int cxxx = 2; cxxx < 9; cxxx++) {
					if (a == 3 || a == 13 || b == 3 || b == 13) {
						g.placeBlock(x + a, y + cxxx, z + b, 21, cell);
					}

					if ((a == 4 && !cell.west || a == 12 && !cell.east || b == 4 && !cell.north || b == 12 && !cell.south) && g.rand.nextBoolean()) {
						g.placeBlock(x + a, y + cxxx, z + b, 21, cell);
					}
				}
			}
		}

		for (int a = 2; a <= 14; a++) {
			for (int b = 2; b <= 14; b++) {
				g.placeBlock(x + a, y - 1, z + b, 1, cell);
				g.placeBlock(x + a, y, z + b, 8, cell);
				g.placeBlock(x + a, y + 1, z + b, 21, cell);
				g.placeBlock(x + a, y + 11, z + b, 1, cell);
				g.placeBlock(x + a, y + 10, z + b, 8, cell);
				g.placeBlock(x + a, y + 9, z + b, 21, cell);
				if (g.rand.nextBoolean()) {
					g.placeBlock(x + a, y + 8, z + b, 21, cell);
				} else if (g.rand.nextBoolean() && g.isAir(x + a, y + 8, z + b)) {
					g.set(x + a, y + 8, z + b, TVBlocks.STRANGE_CRYSTALS.defaultBlockState().setValue(dev.neothaum.blocks.CrystalClusterBlock.FACING, Direction.DOWN));
				}
			}
		}

		g.placeBlock(x + 8, y + 2, z + 8, 21, cell);
		g.placeBlock(x + 8, y + 3, z + 8, 21, cell);
		g.placeBlock(x + 8, y + 4, z + 8, 21, cell);
		g.placeBlock(x + 7, y + 2, z + 8, 21, cell);
		g.placeBlock(x + 8, y + 2, z + 7, 21, cell);
		g.placeBlock(x + 9, y + 2, z + 8, 21, cell);
		g.placeBlock(x + 8, y + 2, z + 9, 21, cell);
		if (g.rand.nextBoolean()) {
			g.placeBlock(x + 7, y + 3, z + 8, 21, cell);
		}

		if (g.rand.nextBoolean()) {
			g.placeBlock(x + 8, y + 3, z + 7, 21, cell);
		}

		if (g.rand.nextBoolean()) {
			g.placeBlock(x + 9, y + 3, z + 8, 21, cell);
		}

		if (g.rand.nextBoolean()) {
			g.placeBlock(x + 8, y + 3, z + 9, 21, cell);
		}

		if (g.rand.nextBoolean()) {
			g.placeBlock(x + 8, y + 5, z + 8, 7, cell);
		}

		g.placeBlock(x + 8, y + 8, z + 8, 21, cell);
		g.placeBlock(x + 8, y + 7, z + 8, 21, cell);
		g.placeBlock(x + 8, y + 6, z + 8, 21, cell);
		g.placeBlock(x + 7, y + 8, z + 8, 21, cell);
		g.placeBlock(x + 8, y + 8, z + 7, 21, cell);
		g.placeBlock(x + 9, y + 8, z + 8, 21, cell);
		g.placeBlock(x + 8, y + 8, z + 9, 21, cell);
		if (g.rand.nextBoolean()) {
			g.placeBlock(x + 7, y + 7, z + 8, 21, cell);
		}

		if (g.rand.nextBoolean()) {
			g.placeBlock(x + 8, y + 7, z + 7, 21, cell);
		}

		if (g.rand.nextBoolean()) {
			g.placeBlock(x + 9, y + 7, z + 8, 21, cell);
		}

		if (g.rand.nextBoolean()) {
			g.placeBlock(x + 8, y + 7, z + 9, 21, cell);
		}

		g.generateConnections(cx, cz, y, cell, 3, true);

		for (int a = -5; a <= 5; a++) {
			for (int bx = -5; bx <= 5; bx++) {
				if (g.rand.nextFloat() < 0.15F && g.isAir(x + 8 + a, y + 2, z + 8 + bx)) {
					float rr = g.rand.nextFloat();
					int md = rr < 0.15F ? 2 : (rr < 0.4F ? 1 : 0);
					g.set(x + 8 + a, y + 2, z + 8 + bx, g.rand.nextFloat() < 0.2F ? OuterBlocks.crate(md) : OuterBlocks.urn(md));
				}
			}
		}
	}
}
