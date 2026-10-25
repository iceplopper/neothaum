package dev.thaumvibe.world;

import dev.thaumvibe.Thaumvibe;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

/** Thaumcraft's biomes (ThaumcraftWorldGenerator.biomeTaint, biomeMagicalForest, biomeEerie) and Utils.setBiomeAt. */
public final class TVBiomes {
	public static final ResourceKey<Biome> MAGICAL_FOREST = key("magical_forest");
	public static final ResourceKey<Biome> TAINTED_LAND = key("tainted_land");
	public static final ResourceKey<Biome> EERIE = key("eerie");

	/** Config: magical forests and taint at worldgen. */
	public static boolean GENERATE = true;

	private static ResourceKey<Biome> key(String name) {
		return ResourceKey.create(Registries.BIOME, Thaumvibe.id(name));
	}

	public static boolean is(Level level, BlockPos pos, ResourceKey<Biome> biome) {
		return level.getBiome(pos).is(biome);
	}

	/**
	 * Utils.setBiomeAt: turns the column at x, z into the biome, from the bottom of the world to the top, and tells nearby players. Biomes are
	 * stored four blocks to a cell now, so the whole 4x4 column the block is in changes.
	 */
	public static void setBiomeAt(ServerLevel level, int x, int z, ResourceKey<Biome> biome) {
		Holder<Biome> holder = level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(biome);
		LevelChunk chunk = level.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
		int qx = (x & 15) >> 2;
		int qz = (z & 15) >> 2;
		boolean changed = false;
		LevelChunkSection[] sections = chunk.getSections();
		for (int i = 0; i < sections.length; i++) {
			LevelChunkSection section = sections[i];
			if (section.getNoiseBiome(qx, 0, qz).is(biome)) {
				continue;
			}
			int quartMinY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(i)) >> 2;
			section.fillBiomesFromNoise((bx, by, bz) -> {
				int lx = bx & 3, ly = by - quartMinY, lz = bz & 3;
				return lx == qx && lz == qz ? holder : section.getNoiseBiome(lx, ly, lz);
			}, SectionPos.blockToSectionCoord(x) << 2, quartMinY, SectionPos.blockToSectionCoord(z) << 2);
			changed = true;
		}
		if (changed) {
			chunk.markUnsaved();
			level.getChunkSource().chunkMap.resendBiomesForChunks(List.of(chunk));
		}
	}

	private TVBiomes() {
	}
}
