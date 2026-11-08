package dev.neothaum.addon.totems;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Wild Traveller's Totems for land generated before they were placed everywhere (/neothaum totems overworld [radius] [frequency]). Each
 * chunk already generated within the radius gets world generation's chance of one ({@link TotemFeature#RARITY}, times the frequency),
 * rolled the same for that chunk every time, so running the command twice adds no more. A chunk that wins gets a totem only on natural,
 * dry, level, open ground: never where a structure stands, near another totem, or where players have spent time (a base), and nothing
 * players built is cleared for it.
 */
public final class WildTotems {
	/** How long (in ticks) players may have spent in and round a chunk before it counts as theirs: ten minutes. */
	private static final long LIVED_IN = 20 * 60 * 10;
	/** No wild totem within this many blocks of another. */
	private static final int APART = 64;

	/** What the command did: chunks looked at, chunks that won the roll, totems raised, and why the rest were passed over. */
	public record Survey(int chunks, int won, List<BlockPos> raised, java.util.Map<String, Integer> passed) {
	}

	public static Survey raiseAround(ServerLevel level, BlockPos centre, int radius, double frequency) {
		int ccx = centre.getX() >> 4;
		int ccz = centre.getZ() >> 4;
		int r = radius >> 4;
		int chunks = 0;
		int won = 0;
		List<BlockPos> raised = new ArrayList<>();
		java.util.Map<String, Integer> passed = new java.util.TreeMap<>();
		for (int cx = ccx - r; cx <= ccx + r; cx++) {
			for (int cz = ccz - r; cz <= ccz + r; cz++) {
				if ((cx - ccx) * (cx - ccx) + (cz - ccz) * (cz - ccz) > r * r) {
					continue;
				}
				chunks++;
				// the same roll for this chunk every time the command is run
				RandomSource rand = RandomSource.create(level.getSeed() ^ ((long) cx * 341873128712L + (long) cz * 132897987541L) ^ 0x7074656D6FL);
				if (rand.nextDouble() * TotemFeature.RARITY >= frequency) {
					continue;
				}
				won++;
				String why = tryChunk(level, rand, cx, cz, raised);
				if (why != null) {
					passed.merge(why, 1, Integer::sum);
				}
			}
		}
		return new Survey(chunks, won, raised, passed);
	}

	/** Raises a totem in this chunk if it may have one; why not, or null if it did. */
	private static String tryChunk(ServerLevel level, RandomSource rand, int cx, int cz, List<BlockPos> raised) {
		var chunk = level.getChunkSource().getChunk(cx, cz, ChunkStatus.STRUCTURE_STARTS, true);
		if (chunk == null || !chunk.getPersistedStatus().isOrAfter(ChunkStatus.FULL)) {
			return "not generated yet (world generation will see to it)";
		}
		if (!chunk.getAllReferences().isEmpty() || !chunk.getAllStarts().isEmpty()) {
			return "a structure there";
		}
		// it and its neighbours, loaded, and not somewhere players have lived
		for (int x = cx - 1; x <= cx + 1; x++) {
			for (int z = cz - 1; z <= cz + 1; z++) {
				var c = level.getChunkSource().getChunk(x, z, ChunkStatus.STRUCTURE_STARTS, true);
				if (c == null || !c.getPersistedStatus().isOrAfter(ChunkStatus.FULL)) {
					return "beside land not generated yet";
				}
				if (level.getChunk(x, z).getInhabitedTime() > LIVED_IN) {
					return "players have spent time there";
				}
			}
		}
		if (near(level, (cx << 4) + 8, (cz << 4) + 8)) {
			return "another totem close by";
		}
		// a few spots in the chunk, the first that suits
		for (int tries = 0; tries < 8; tries++) {
			int x = (cx << 4) + 2 + rand.nextInt(12);
			int z = (cz << 4) + 2 + rand.nextInt(12);
			BlockPos ground = spot(level, x, z);
			if (ground != null) {
				TotemFeature.build(level, rand, ground);
				raised.add(ground.above(2));
				return null;
			}
		}
		return "no level, open, natural ground";
	}

	/** The ground block at this column if a dais fits there: natural, dry, level and open, with nothing built above it. */
	private static BlockPos spot(ServerLevel level, int x, int z) {
		int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				int h = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x + dx, z + dz);
				if (Math.abs(h - y) > 1) {
					return null;
				}
				BlockPos g = new BlockPos(x + dx, h - 1, z + dz);
				BlockState ground = level.getBlockState(g);
				if (!natural(ground) || !level.getFluidState(g.above()).isEmpty()) {
					return null;
				}
				// nothing but air, plants and snow where the dais and the four blocks over it go: no trees, nothing built
				for (int up = 1; up <= 5; up++) {
					BlockState s = level.getBlockState(g.above(up));
					if (!s.isAir() && !s.canBeReplaced() && !s.is(BlockTags.FLOWERS) || s.hasBlockEntity()) {
						return null;
					}
				}
			}
		}
		return new BlockPos(x, y - 1, z);
	}

	/** The ground worlds make: soil, sand, snow, stone, gravel, clay and the like. */
	private static boolean natural(BlockState s) {
		return s.is(BlockTags.DIRT) || s.is(BlockTags.SAND) || s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.PODZOL) || s.is(Blocks.MYCELIUM)
			|| s.is(Blocks.COARSE_DIRT) || s.is(Blocks.MOSS_BLOCK) || s.is(Blocks.SNOW_BLOCK) || s.is(Blocks.GRAVEL) || s.is(Blocks.SANDSTONE)
			|| s.is(Blocks.RED_SANDSTONE) || s.is(BlockTags.TERRACOTTA) || s.is(Blocks.MUD) || s.is(Blocks.CLAY) || s.is(Blocks.PACKED_ICE)
			|| s.is(BlockTags.BASE_STONE_OVERWORLD) || s.is(Blocks.CALCITE);
	}

	/** Whether a Traveller's Totem (of any kind) stands within {@link #APART} blocks. */
	private static boolean near(ServerLevel level, int x, int z) {
		var data = TotemData.get(level.getServer());
		for (TotemData.Totem t : data.all()) {
			if (t.pos().dimension() == level.dimension() && t.pos().pos().distManhattan(new BlockPos(x, t.pos().pos().getY(), z)) < APART * 2
				&& t.pos().pos().distSqr(new BlockPos(x, t.pos().pos().getY(), z)) < (double) APART * APART) {
				return true;
			}
		}
		return false;
	}

	private WildTotems() {
	}
}
