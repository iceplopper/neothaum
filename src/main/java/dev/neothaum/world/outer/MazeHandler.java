package dev.neothaum.world.outer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.neothaum.NeoThaum;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

/**
 * MazeHandler: every Outer Lands maze cell by chunk, shared by the whole server and kept in the overworld's saved data (TC4's
 * labyrinth.dat), with MapBossData's boss counter. Mazes are made on a worker thread (MazeThread) when an eldritch altar asks for one.
 */
public final class MazeHandler {
	/** The maze floor's height (TC4: y 50). */
	public static final int Y = 50;
	private static final Map<Long, Short> LABYRINTH = new ConcurrentHashMap<>();
	private static @Nullable Data data;

	private MazeHandler() {
	}

	/** The saved form: the cells, and how many bosses have been woken so far. */
	public static final class Data extends SavedData {
		private record Entry(int x, int z, short cell) {
			static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(Codec.INT.fieldOf("x").forGetter(Entry::x),
				Codec.INT.fieldOf("z").forGetter(Entry::z), Codec.SHORT.fieldOf("cell").forGetter(Entry::cell)).apply(i, Entry::new));
		}

		static final Codec<Data> CODEC = RecordCodecBuilder.create(i -> i.group(Entry.CODEC.listOf().fieldOf("cells").forGetter(Data::entries),
			Codec.INT.optionalFieldOf("bossCount", 0).forGetter(d -> d.bossCount)).apply(i, Data::new));
		static final SavedDataType<Data> TYPE = new SavedDataType<>(NeoThaum.id("labyrinth"), Data::new, CODEC, null);

		int bossCount;

		Data() {
		}

		private Data(List<Entry> cells, int bossCount) {
			this.bossCount = bossCount;
			for (Entry e : cells) {
				LABYRINTH.put(ChunkPos.pack(e.x, e.z), e.cell);
			}
		}

		private List<Entry> entries() {
			List<Entry> out = new ArrayList<>();
			LABYRINTH.forEach((k, v) -> {
				if (v > 0) {
					out.add(new Entry(ChunkPos.getX(k), ChunkPos.getZ(k), v));
				}
			});
			return out;
		}
	}

	/** loadMaze: read the server's mazes when it starts. */
	public static void load(MinecraftServer server) {
		LABYRINTH.clear();
		data = server.overworld().getDataStorage().computeIfAbsent(Data.TYPE);
	}

	public static void unload() {
		LABYRINTH.clear();
		data = null;
	}

	private static void dirty() {
		if (data != null) {
			data.setDirty();
		}
	}

	public static @Nullable Cell getCell(int cx, int cz) {
		Short v = LABYRINTH.get(ChunkPos.pack(cx, cz));
		return v == null ? null : new Cell(v);
	}

	static void putRaw(int cx, int cz, short cell) {
		LABYRINTH.put(ChunkPos.pack(cx, cz), cell);
	}

	static short getRaw(int cx, int cz) {
		return LABYRINTH.getOrDefault(ChunkPos.pack(cx, cz), (short) 0);
	}

	static void remove(int cx, int cz) {
		LABYRINTH.remove(ChunkPos.pack(cx, cz));
	}

	/** Any maze (or one being made) within w by h chunks of this one. */
	public static boolean mazesInRange(int chunkX, int chunkZ, int w, int h) {
		for (int x = -w; x <= w; x++) {
			for (int z = -h; z <= h; z++) {
				if (LABYRINTH.containsKey(ChunkPos.pack(chunkX + x, chunkZ + z))) {
					return true;
				}
			}
		}
		return false;
	}

	/** MapBossData: the next boss to wake (0 golem, 1 warden, 2 cultist portal, 3 giant taintacles); now and then one is skipped. */
	public static int nextBoss(RandomSource rand) {
		if (data == null) {
			return rand.nextInt(4);
		}
		data.bossCount++;
		if (rand.nextFloat() < 0.25F) {
			data.bossCount++;
		}
		data.setDirty();
		return data.bossCount % 4;
	}

	/** TileEldritchAltar.checkForMaze: start a maze of 15-29 by 15-29 cells centred here unless one already reaches it. */
	public static boolean checkForMaze(int chunkX, int chunkZ, RandomSource rand) {
		int w = 15 + rand.nextInt(8) * 2;
		int h = 15 + rand.nextInt(8) * 2;
		if (mazesInRange(chunkX, chunkZ, w, h)) {
			return true;
		}
		long seed = rand.nextLong();
		Thread t = new Thread(() -> {
			MazeThread.run(chunkX, chunkZ, w, h, seed);
			dirty();
		}, "NeoThaum maze");
		t.setDaemon(true);
		t.start();
		return false;
	}

	/** MazeHandler.generateEldritch: build this chunk's cell of the maze, if it has one. */
	public static void generateEldritch(WorldGenLevel world, RandomSource random, int cx, int cz) {
		Cell cell = getCell(cx, cz);
		if (cell == null) {
			return;
		}
		GenCommon gen = new GenCommon(world, random);
		switch (cell.feature) {
			case 1 -> GenPortal.generatePortal(gen, cx, cz, Y, cell);
			case 2, 3, 4, 5 -> GenBossRoom.generateRoom(gen, cx, cz, Y, cell);
			case 6 -> GenKeyRoom.generateRoom(gen, cx, cz, Y, cell);
			case 7 -> GenNestRoom.generateRoom(gen, cx, cz, Y, cell);
			case 8 -> GenLibraryRoom.generateRoom(gen, cx, cz, Y, cell);
			default -> GenPassage.generateDefaultPassage(gen, cx, cz, Y, cell);
		}
		gen.processDecorations();
	}
}
