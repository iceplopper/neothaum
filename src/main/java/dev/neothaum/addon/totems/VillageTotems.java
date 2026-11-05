package dev.neothaum.addon.totems;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

/**
 * Traveller's Totems for villages that didn't get one as they generated: those with no room in the chunks world generation may build in
 * (tried again once the village around them is built), and those in worlds made before villages had totems (/neothaum totems villages).
 */
public final class VillageTotems {
	/** Villages (their start chunks) found with no room while generating, waiting for the server thread. */
	private static final ConcurrentLinkedQueue<long[]> QUEUE = new ConcurrentLinkedQueue<>();

	private VillageTotems() {
	}

	/** From world generation (any thread): this village is to have its totem once it is built. */
	static void defer(int chunkX, int chunkZ) {
		QUEUE.add(new long[]{chunkX, chunkZ});
	}

	static void init() {
		ServerTickEvents.END_SERVER_TICK.register(VillageTotems::tick);
	}

	private static long key(int cx, int cz) {
		return (long) cx << 32 | cz & 0xffffffffL;
	}

	private static void tick(MinecraftServer server) {
		if (server.getTickCount() % 100 != 0) {
			return;
		}
		TotemData data = TotemData.get(server);
		for (long[] v; (v = QUEUE.poll()) != null; ) {
			data.defer(key((int) v[0], (int) v[1]));
		}
		if (data.deferred().isEmpty() || !TravellersTotems.enabled()) {
			return;
		}
		ServerLevel level = server.overworld();
		for (long key : List.copyOf(data.deferred())) {
			int cx = (int) (key >> 32);
			int cz = (int) key;
			if (!built(level, cx, cz)) {
				continue;
			}
			data.undefer(key);
			var village = TotemFeature.villageIn(level.registryAccess(), level.getChunk(cx, cz));
			if (village != null && totemIn(level, cx, cz) == null) {
				BlockPos at = TotemFeature.inBuiltVillage(level, RandomSource.create(key), cx << 4, cz << 4, village);
				dev.neothaum.NeoThaum.LOGGER.info("Traveller's Totem for the village at chunk {}, {}, now it is built: {}", cx, cz,
					at != null ? at.toShortString() : "no room");
			}
		}
	}

	/** Chunks either side of a village's start that its totem's search (and the look for one already there) reaches into. */
	private static int reachChunks() {
		return (TotemFeature.BUILT_REACH + 8 + 15) >> 4;
	}

	/** Whether every chunk within reach of a village's start is loaded, and so built. */
	private static boolean built(ServerLevel level, int cx, int cz) {
		int r = reachChunks();
		for (int x = cx - r; x <= cx + r; x++) {
			for (int z = cz - r; z <= cz + r; z++) {
				if (!level.hasChunk(x, z)) {
					return false;
				}
			}
		}
		return true;
	}

	/** A Traveller's Totem standing within reach of the village starting in this chunk (looking at the blocks), or null. */
	public static BlockPos totemIn(ServerLevel level, int cx, int cz) {
		int vx = (cx << 4) + 8;
		int vz = (cz << 4) + 8;
		int reach = TotemFeature.BUILT_REACH + 8;
		for (int x = vx - reach; x <= vx + reach; x++) {
			for (int z = vz - reach; z <= vz + reach; z++) {
				int top = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
				for (int y = top - 5; y <= top; y++) {
					if (level.getBlockState(new BlockPos(x, y, z)).is(TravellersTotems.TOTEM)) {
						return new BlockPos(x, y, z);
					}
				}
			}
		}
		return null;
	}

	/** What /neothaum totems villages found. */
	public record Survey(int villages, int had, List<BlockPos> raised, List<BlockPos> noRoom) {
	}

	/**
	 * Every village whose start is within {@code radius} blocks of {@code centre} and already generated: those without a Traveller's Totem
	 * get one. Villages not generated yet are left to world generation, which gives them theirs.
	 */
	public static Survey raiseAround(ServerLevel level, BlockPos centre, int radius) {
		var registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
		var state = level.getChunkSource().getGeneratorState();
		Set<StructurePlacement> placements = new LinkedHashSet<>();
		registry.getTagOrEmpty(StructureTags.VILLAGE).forEach(h -> placements.addAll(state.getPlacementsForStructure(h)));
		int ccx = centre.getX() >> 4;
		int ccz = centre.getZ() >> 4;
		int r = radius >> 4;
		int villages = 0;
		int had = 0;
		List<BlockPos> raised = new ArrayList<>();
		List<BlockPos> noRoom = new ArrayList<>();
		for (int cx = ccx - r; cx <= ccx + r; cx++) {
			for (int cz = ccz - r; cz <= ccz + r; cz++) {
				int fx = cx;
				int fz = cz;
				if ((cx - ccx) * (cx - ccx) + (cz - ccz) * (cz - ccz) > r * r || placements.stream().noneMatch(p -> p.isStructureChunk(state, fx, fz))) {
					continue;
				}
				var chunk = level.getChunkSource().getChunk(cx, cz, ChunkStatus.STRUCTURE_STARTS, true);
				if (chunk == null || !chunk.getPersistedStatus().isOrAfter(ChunkStatus.FULL)) {
					continue;
				}
				var village = TotemFeature.villageIn(level.registryAccess(), chunk);
				if (village == null) {
					continue;
				}
				villages++;
				int reach = reachChunks();
				for (int x = cx - reach; x <= cx + reach; x++) {
					for (int z = cz - reach; z <= cz + reach; z++) {
						level.getChunk(x, z);
					}
				}
				if (totemIn(level, cx, cz) != null) {
					had++;
					continue;
				}
				BlockPos at = TotemFeature.inBuiltVillage(level, RandomSource.create(key(cx, cz)), cx << 4, cz << 4, village);
				if (at != null) {
					raised.add(at);
				} else {
					noRoom.add(new BlockPos((cx << 4) + 8, level.getHeight(Heightmap.Types.WORLD_SURFACE, (cx << 4) + 8, (cz << 4) + 8), (cz << 4) + 8));
				}
			}
		}
		return new Survey(villages, had, raised, noRoom);
	}
}
