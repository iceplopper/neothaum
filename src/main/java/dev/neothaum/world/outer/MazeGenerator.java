package dev.neothaum.world.outer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * MazeGenerator: carves a maze of width x height cells by a growing-tree walk from the portal cell in the middle. A 2x2 boss room sits in a
 * corner, a few random blocks of cells are left solid, and once carved the key room goes in one dead end, nests and libraries in half of
 * the rest, and one passage in 25 gets a variant. generate() returns false when the layout fails and a new seed should be tried.
 */
public final class MazeGenerator {
	public static final int N = 1;
	public static final int S = 2;
	public static final int E = 4;
	public static final int W = 8;

	public final int width;
	public final int height;
	public final int[][] grid;
	private final Random rand;

	private record Loc(int x, int y) {
	}

	public MazeGenerator(int width, int height, long seed) {
		this.width = width;
		this.height = height;
		this.rand = new Random(seed);
		this.grid = new int[height][width];
	}

	static int getOPP(int in) {
		return switch (in) {
			case N -> S;
			case S -> N;
			case E -> W;
			case W -> E;
			default -> -99;
		};
	}

	static int getDX(int in) {
		return switch (in) {
			case E -> 1;
			case W -> -1;
			case N, S -> 0;
			default -> -99;
		};
	}

	static int getDY(int in) {
		return switch (in) {
			case N -> -1;
			case S -> 1;
			case E, W -> 0;
			default -> -99;
		};
	}

	private boolean inside(int nx, int ny) {
		return 0 < nx && nx < this.width - 1 && 0 < ny && ny < this.height - 1;
	}

	public boolean generate() {
		int bx = 0;
		int by = 0;
		switch (this.rand.nextInt(4)) {
			case 1 -> {
				bx = this.width - 2;
				by = this.height - 2;
			}
			case 2 -> bx = this.width - 2;
			case 3 -> by = this.height - 2;
			default -> {
			}
		}
		// the boss room's four quarters (features 2-5) and the portal (feature 1)
		this.grid[by][bx] = 512;
		this.grid[by][bx + 1] = 768;
		this.grid[by + 1][bx] = 1024;
		this.grid[by + 1][bx + 1] = 1280;
		int px = 1 + this.width / 2;
		int py = 1 + this.height / 2;
		this.grid[py][px] = 256;
		List<Loc> cells = new ArrayList<>();
		// solid blocks the maze winds around
		int l = (this.width + this.height) / 4;
		for (int z = 0; z < l; z++) {
			int w = 1 + this.rand.nextInt(3);
			if (w > 2) {
				l--;
			}
			int qq = this.rand.nextInt(this.width - w);
			int ww = this.rand.nextInt(this.height - w);
			for (int a = qq; a < qq + w; a++) {
				for (int b = ww; b < ww + w; b++) {
					if (this.grid[b][a] == 0) {
						this.grid[b][a] = -1;
					}
				}
			}
		}
		List<Integer> directions = Arrays.asList(N, S, E, W);
		Collections.shuffle(directions, this.rand);
		int xx = px + getDX(directions.get(0));
		int yy = py + getDY(directions.get(0));
		this.grid[py][px] |= directions.get(0);
		if (this.grid[yy][xx] < 0) {
			this.grid[yy][xx] = 0;
		}
		this.grid[yy][xx] |= getOPP(directions.get(0));
		cells.add(new Loc(xx, yy));
		boolean success = false;
		while (!cells.isEmpty()) {
			int index = this.getNextIndex(cells.size());
			int x = cells.get(index).x;
			int y = cells.get(index).y;
			Collections.shuffle(directions, this.rand);
			boolean carved = false;
			for (int dir : directions) {
				int nx = x + getDX(dir);
				int ny = y + getDY(dir);
				if (this.inside(nx, ny)) {
					if (this.grid[ny][nx] == 0) {
						this.grid[y][x] |= dir;
						this.grid[ny][nx] |= getOPP(dir);
						cells.add(new Loc(nx, ny));
						carved = true;
					}
					if (carved) {
						success = true;
						break;
					}
				}
			}
			if (!carved) {
				cells.remove(index);
			}
		}
		if (!success) {
			return false;
		}
		for (int aa = 0; aa < this.height; aa++) {
			for (int bb = 0; bb < this.width; bb++) {
				if (this.grid[aa][bb] < 0) {
					this.grid[aa][bb] = 0;
				}
			}
		}
		// open the portal room to some of its neighbours
		Collections.shuffle(directions, this.rand);
		for (int dir : directions) {
			int nx = px + getDX(dir);
			int ny = py + getDY(dir);
			if (this.inside(nx, ny) && this.grid[ny][nx] > 0 && this.rand.nextBoolean()) {
				this.grid[ny][nx] |= getOPP(dir);
				this.grid[py][px] |= dir;
			}
		}
		// connect the boss room to the maze, carving a corridor to it if no maze cell touches it
		Collections.shuffle(directions, this.rand);
		boolean connected = false;
		outer:
		for (int ax = 0; ax < 2; ax++) {
			for (int ay = 0; ay < 2; ay++) {
				for (int dir : directions) {
					int nx = bx + ax + getDX(dir);
					int ny = by + ay + getDY(dir);
					if (this.inside(nx, ny) && this.grid[ny][nx] > 0 && new Cell((short) this.grid[ny][nx]).feature == 0) {
						this.grid[ny][nx] |= getOPP(dir);
						this.grid[by + ay][bx + ax] |= dir;
						connected = true;
						break outer;
					}
				}
			}
		}
		if (!connected) {
			List<Integer> directions2 = Arrays.asList(N, S, E, W);
			Collections.shuffle(directions2, this.rand);
			success = false;
			outer2:
			for (int ax = 0; ax < 2; ax++) {
				for (int ay = 0; ay < 2; ay++) {
					for (int dir2 : directions2) {
						int qx = bx + ax + getDX(dir2);
						int qy = by + ay + getDY(dir2);
						if (this.inside(qx, qy) && this.grid[qy][qx] == 0) {
							cells.add(new Loc(qx, qy));
							while (!cells.isEmpty()) {
								int index = this.getNextIndex(cells.size());
								int x = cells.get(index).x;
								int y = cells.get(index).y;
								Collections.shuffle(directions, this.rand);
								boolean carved = false;
								for (int dir : directions) {
									int nx = x + getDX(dir);
									int ny = y + getDY(dir);
									if (this.inside(nx, ny)) {
										if (this.grid[ny][nx] == 0) {
											this.grid[y][x] |= dir | 25344;
											this.grid[ny][nx] |= getOPP(dir) | 25344;
											cells.add(new Loc(nx, ny));
											carved = true;
										} else if (new Cell((short) this.grid[ny][nx]).feature == 0) {
											this.grid[y][x] |= dir;
											this.grid[ny][nx] |= getOPP(dir);
											this.grid[qy][qx] |= getOPP(dir2);
											this.grid[by + ay][bx + ax] |= dir2;
											success = true;
											break outer2;
										}
										if (carved) {
											break;
										}
									}
								}
								if (!carved) {
									cells.remove(index);
								}
							}
						}
					}
				}
			}
			if (!success) {
				return false;
			}
		}
		for (int aa = 0; aa < this.height; aa++) {
			for (int bb = 0; bb < this.width; bb++) {
				Cell c = new Cell((short) this.grid[aa][bb]);
				if (c.feature == 99) {
					c.feature = 0;
					this.grid[aa][bb] = c.pack();
				}
			}
		}
		// the key room in one dead end, nests, libraries and plain rooms in half of the others
		List<int[]> deadEnds = new ArrayList<>();
		for (int aa = 0; aa < this.height; aa++) {
			for (int bb = 0; bb < this.width; bb++) {
				Cell c = new Cell((short) this.grid[aa][bb]);
				int exits = (c.north ? 1 : 0) + (c.south ? 1 : 0) + (c.east ? 1 : 0) + (c.west ? 1 : 0);
				if (exits == 1 && c.feature == 0) {
					deadEnds.add(new int[]{aa, bb});
				}
			}
		}
		if (deadEnds.isEmpty()) {
			return false;
		}
		int r = this.rand.nextInt(deadEnds.size());
		int[] ll = deadEnds.get(r);
		Cell key = new Cell((short) this.grid[ll[0]][ll[1]]);
		key.feature = 6;
		this.grid[ll[0]][ll[1]] = key.pack();
		deadEnds.remove(r);
		if (!deadEnds.isEmpty()) {
			r = 0;
			while (r < deadEnds.size() / 2) {
				int rx = this.rand.nextInt(deadEnds.size());
				int[] llx = deadEnds.get(rx);
				Cell cx = new Cell((short) this.grid[llx[0]][llx[1]]);
				if (cx.feature == 0) {
					cx.feature = (byte) (7 + this.rand.nextInt(3));
					this.grid[llx[0]][llx[1]] = cx.pack();
					deadEnds.remove(rx);
					r++;
				}
			}
		}
		// passage variants
		for (int aa = 0; aa < this.height; aa++) {
			for (int bb = 0; bb < this.width; bb++) {
				Cell c = new Cell((short) this.grid[aa][bb]);
				if (c.feature == 0 && (c.north || c.south || c.west || c.east) && this.rand.nextInt(25) == 0) {
					c.feature = switch (this.rand.nextInt(8)) {
						case 0 -> 8;
						case 1 -> 10;
						case 2, 3 -> 11;
						case 4, 5 -> 12;
						case 6 -> 13;
						default -> 14;
					};
					this.grid[aa][bb] = c.pack();
				}
			}
		}
		return true;
	}

	/** Mostly the newest cell, sometimes a random one, now and then the oldest: long winding corridors with some branching. */
	private int getNextIndex(int ceil) {
		float r = this.rand.nextFloat();
		if (r <= 0.45F) {
			return ceil - 1;
		}
		return r <= 0.9F ? this.rand.nextInt(ceil) : 0;
	}
}
