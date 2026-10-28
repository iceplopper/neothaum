package dev.neothaum.world.outer;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/** ChunkProviderOuter.populate: the Outer Lands biome's only feature, building this chunk's cell of whatever maze covers it. */
public record OuterMazeFeature() implements Feature {
	public static final MapCodec<OuterMazeFeature> CODEC = MapCodec.unit(new OuterMazeFeature());

	@Override
	public MapCodec<OuterMazeFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel world, ChunkGenerator gen, RandomSource random, BlockPos origin) {
		MazeHandler.generateEldritch(world, random, origin.getX() >> 4, origin.getZ() >> 4);
		return true;
	}
}
