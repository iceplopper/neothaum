package dev.neothaum.world.outer;

import dev.neothaum.registry.TVBlocks;
import net.minecraft.core.Direction;

/** GenKeyRoom: a dead end holding the Runed Tablet on a capstone, watched by two to four Eldritch Guardians. */
final class GenKeyRoom {
	private GenKeyRoom() {
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
						if (cxxx > 3 && cxxx < 9 && (a == 8 || b == 8) || cxxx > 4 && cxxx < 8 && (a == 7 || b == 7 || a == 9 || b == 9)) {
							if (a != 8 && b != 8 || cxxx != 6) {
								g.placeBlock(x + a, y + cxxx, z + b, 19, cell);
							}
						} else {
							g.placeBlock(x + a, y + cxxx, z + b, 18, cell);
						}
					}
				}
			}
		}

		for (int a = 2; a <= 14; a++) {
			for (int b = 2; b <= 14; b++) {
				g.placeBlock(x + a, y - 1, z + b, 1, cell);
				g.placeBlock(x + a, y, z + b, 8, cell);
				g.placeBlock(x + a, y + 1, z + b, 2, cell);
				g.placeBlock(x + a, y + 13, z + b, 1, cell);
				g.placeBlock(x + a, y + 12, z + b, 8, cell);
				g.placeBlock(x + a, y + 11, z + b, 2, cell);
				if (a > 1 && a < 15 && b > 1 && b < 15) {
					int q = Math.min(Math.abs(8 - a), Math.abs(8 - b));

					for (int gi = 0; gi < q - 1; gi++) {
						g.placeBlock(x + a, y + 1 + gi, z + b, 2, cell);
					}
				}

				if (a > 3 && a < 13 && b > 3 && b < 13) {
					int q = Math.min(Math.abs(8 - a), Math.abs(8 - b));

					for (int gi = 0; gi < q; gi++) {
						g.placeBlock(x + a, y + 11 - gi, z + b, 2, cell);
					}
				}
			}
		}

		for (int gi = 0; gi < 5; gi++) {
			g.placeBlock(x + 6 + gi, y + 2, z + 4, 10, Direction.NORTH, cell);
			g.placeBlock(x + 6 + gi, y + 2, z + 12, 10, Direction.SOUTH, cell);
			g.placeBlock(x + 12, y + 2, z + 6 + gi, 10, Direction.EAST, cell);
			g.placeBlock(x + 4, y + 2, z + 6 + gi, 10, Direction.WEST, cell);
		}

		g.generateConnections(cx, cz, y, cell, 3, true);
		g.setTc(x + 8, y + 2, z + 8, "blockEldritch", 3);
		// the Runed Tablet, floating on the capstone, never to rot away
		var tablet = new net.minecraft.world.entity.item.ItemEntity(g.world.getLevel(), x + 8.5, y + 3.5, z + 8.5,
			new net.minecraft.world.item.ItemStack(dev.neothaum.registry.TVItems.RUNED_TABLET));
		tablet.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
		tablet.setUnlimitedLifetime();
		g.world.addFreshEntity(tablet);
		var difficulty = g.world.getLevel().getDifficulty();
		int zz = 2 + (difficulty == net.minecraft.world.Difficulty.HARD ? 2 : (difficulty == net.minecraft.world.Difficulty.NORMAL ? 1 : 0));
		for (int qq = 0; qq < zz; qq++) {
			var eg = dev.neothaum.entity.TVEntities.ELDRITCH_GUARDIAN.create(g.world.getLevel(), net.minecraft.world.entity.EntitySpawnReason.STRUCTURE);
			if (eg == null) {
				continue;
			}
			double i1 = x + 8.5 + net.minecraft.util.Mth.randomBetweenInclusive(g.rand, 1, 3) * net.minecraft.util.Mth.randomBetweenInclusive(g.rand, -1, 1);
			double k1 = z + 8.5 + net.minecraft.util.Mth.randomBetweenInclusive(g.rand, 1, 3) * net.minecraft.util.Mth.randomBetweenInclusive(g.rand, -1, 1);
			eg.snapTo(i1, y + 2, k1, g.rand.nextFloat() * 360.0F, 0.0F);
			eg.finalizeSpawn(g.world, g.world.getCurrentDifficultyAt(eg.blockPosition()), net.minecraft.world.entity.EntitySpawnReason.STRUCTURE, null);
			eg.setHomeTo(new net.minecraft.core.BlockPos(x + 8, y + 2, z + 8), 16);
			eg.setPersistenceRequired();
			if (qq == 0 && zz >= 4) {
				eg.addTag(dev.neothaum.entity.champion.ChampionEvents.TAG_CHECKED);
				dev.neothaum.entity.champion.ChampionEvents.makeChampion(eg, true);
			}
			g.world.addFreshEntity(eg);
		}
	}
}
