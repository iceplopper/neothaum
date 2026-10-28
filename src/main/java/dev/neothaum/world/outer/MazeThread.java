package dev.neothaum.world.outer;

/**
 * MazeThread: lays a new maze's cells into the labyrinth with its portal cell on the given chunk. The centre and corners are claimed first
 * so a second altar nearby sees the space as taken while the maze is still being made.
 */
final class MazeThread {
	private MazeThread() {
	}

	static void run(int x, int z, int w, int h, long seed) {
		int[][] claims = {{x, z}, {x - w, z - h}, {x + w, z + h}, {x - w, z + h}, {x + w, z - h}};
		for (int[] c : claims) {
			MazeHandler.putRaw(c[0], c[1], (short) 0);
		}
		MazeGenerator gen = new MazeGenerator(w, h, seed++);
		while (!gen.generate()) {
			gen = new MazeGenerator(w, h, seed++);
		}
		int col = x - (1 + w / 2);
		int row = z - (1 + h / 2);
		for (int a = 0; a < w; a++) {
			for (int b = 0; b < h; b++) {
				if (gen.grid[b][a] > 0) {
					MazeHandler.putRaw(a + col, b + row, (short) gen.grid[b][a]);
				}
			}
		}
		for (int[] c : claims) {
			if (MazeHandler.getRaw(c[0], c[1]) == 0) {
				MazeHandler.remove(c[0], c[1]);
			}
		}
	}
}
