package dev.neothaum.world;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.api.nodes.NodeModifier;
import dev.neothaum.api.nodes.NodeType;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.tiles.NodeBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Ports of ThaumcraftWorldGenerator.createRandomNodeAt/createNodeAt and BiomeHandler (BiomeDictionary -> convention tags). */
public final class NodeGenerator {
	/** BiomeHandler.registerBiomeInfo table: aura level, aspect, supports greatwood, greatwood chance. */
	private record BiomeInfo(TagKey<Biome> tag, int aura, Aspect aspect, boolean greatwood, float greatwoodChance) {
	}

	private static final List<BiomeInfo> INFO = List.of(
		new BiomeInfo(ConventionalBiomeTags.IS_AQUATIC, 100, Aspect.WATER, false, 0.0F),
		new BiomeInfo(ConventionalBiomeTags.IS_OCEAN, 120, Aspect.WATER, false, 0.0F),
		new BiomeInfo(ConventionalBiomeTags.IS_RIVER, 100, Aspect.WATER, false, 0.0F),
		new BiomeInfo(ConventionalBiomeTags.IS_WET, 80, Aspect.WATER, false, 0.0F),
		new BiomeInfo(ConventionalBiomeTags.IS_HOT, 100, Aspect.FIRE, false, 0.0F),
		new BiomeInfo(ConventionalBiomeTags.IS_DESERT, 100, Aspect.FIRE, false, 0.0F),
		new BiomeInfo(ConventionalBiomeTags.IS_NETHER, 120, Aspect.FIRE, false, 0.0F),
		new BiomeInfo(ConventionalBiomeTags.IS_BADLANDS, 80, Aspect.FIRE, false, 0.0F),
		new BiomeInfo(ConventionalBiomeTags.IS_VEGETATION_DENSE, 100, Aspect.ORDER, false, 0.0F),
		new BiomeInfo(ConventionalBiomeTags.IS_SNOWY, 80, Aspect.ORDER, false, 0.0F),
		new BiomeInfo(ConventionalBiomeTags.IS_COLD, 80, Aspect.ORDER, false, 0.0F),
		new BiomeInfo(ConventionalBiomeTags.IS_ICY, 100, Aspect.ORDER, false, 0.0F),
		new BiomeInfo(ConventionalBiomeTags.IS_MUSHROOM, 140, Aspect.ORDER, false, 0.0F),
		new BiomeInfo(ConventionalBiomeTags.IS_CONIFEROUS_TREE, 100, Aspect.EARTH, true, 0.2F),
		new BiomeInfo(ConventionalBiomeTags.IS_FOREST, 120, Aspect.EARTH, true, 1.0F),
		new BiomeInfo(ConventionalBiomeTags.IS_SANDY, 80, Aspect.EARTH, false, 0.0F),
		new BiomeInfo(ConventionalBiomeTags.IS_BEACH, 80, Aspect.EARTH, false, 0.0F),
		new BiomeInfo(ConventionalBiomeTags.IS_SAVANNA, 80, Aspect.AIR, true, 0.2F),
		new BiomeInfo(ConventionalBiomeTags.IS_MOUNTAIN, 100, Aspect.AIR, false, 0.0F),
		new BiomeInfo(ConventionalBiomeTags.IS_HILL, 120, Aspect.AIR, false, 0.0F),
		new BiomeInfo(ConventionalBiomeTags.IS_PLAINS, 80, Aspect.AIR, true, 0.2F),
		new BiomeInfo(ConventionalBiomeTags.IS_DRY, 80, Aspect.ENTROPY, false, 0.0F),
		new BiomeInfo(ConventionalBiomeTags.IS_VEGETATION_SPARSE, 80, Aspect.ENTROPY, false, 0.0F),
		new BiomeInfo(ConventionalBiomeTags.IS_SWAMP, 120, Aspect.ENTROPY, true, 0.2F),
		new BiomeInfo(ConventionalBiomeTags.IS_WASTELAND, 80, Aspect.ENTROPY, false, 0.0F),
		new BiomeInfo(ConventionalBiomeTags.IS_JUNGLE, 100, Aspect.PLANT, false, 0.0F),
		new BiomeInfo(ConventionalBiomeTags.IS_LUSH, 100, Aspect.PLANT, true, 0.5F),
		new BiomeInfo(ConventionalBiomeTags.IS_MAGICAL, 100, null, true, 1.0F),
		new BiomeInfo(ConventionalBiomeTags.IS_END, 80, Aspect.VOID, false, 0.0F),
		new BiomeInfo(ConventionalBiomeTags.IS_SPOOKY, 80, Aspect.SOUL, false, 0.0F),
		new BiomeInfo(ConventionalBiomeTags.IS_DEAD, 50, Aspect.DEATH, false, 0.0F)
	);

	private static List<BiomeInfo> typesFor(Holder<Biome> biome) {
		List<BiomeInfo> out = new ArrayList<>();
		for (BiomeInfo i : INFO) {
			if (biome.is(i.tag())) {
				out.add(i);
			}
		}
		return out;
	}

	public static int getBiomeAura(Holder<Biome> biome) {
		List<BiomeInfo> types = typesFor(biome);
		if (types.isEmpty()) {
			return 100;
		}
		int average = 0;
		for (BiomeInfo t : types) {
			average += t.aura();
		}
		return average / types.size();
	}

	public static Aspect getRandomBiomeTag(Holder<Biome> biome, RandomSource random) {
		List<BiomeInfo> types = typesFor(biome);
		return types.isEmpty() ? null : types.get(random.nextInt(types.size())).aspect();
	}

	public static float getBiomeSupportsGreatwood(Holder<Biome> biome) {
		for (BiomeInfo t : typesFor(biome)) {
			if (t.greatwood()) {
				return t.greatwoodChance();
			}
		}
		return 0.0F;
	}

	public static boolean isMagical(Holder<Biome> biome) {
		return biome.is(ConventionalBiomeTags.IS_MAGICAL);
	}

	public static void createRandomNodeAt(LevelAccessor world, BlockPos pos, RandomSource random, boolean silverwood, boolean eerie, boolean small,
		boolean forcePlace) {
		createRandomNodeAt(world, pos, random, silverwood, eerie, small, forcePlace, null);
	}

	/** As above, with the node type forced (the developer node placers); a tainted node also carries Vitium, as in the taint biome. */
	public static void createRandomNodeAt(LevelAccessor world, BlockPos pos, RandomSource random, boolean silverwood, boolean eerie, boolean small,
		boolean forcePlace, NodeType forcedType) {
		List<Aspect> basicAspects = new ArrayList<>();
		List<Aspect> complexAspects = new ArrayList<>();
		for (Aspect as : Aspect.aspects.values()) {
			if (as.getComponents() != null) {
				complexAspects.add(as);
			} else {
				basicAspects.add(as);
			}
		}
		var cfg = dev.neothaum.config.NeoThaumConfig.current;
		NodeType type = NodeType.NORMAL;
		if (silverwood) {
			type = NodeType.PURE;
		} else if (eerie) {
			type = NodeType.DARK;
		} else if (dev.neothaum.config.NeoThaumConfig.roll(random, dev.neothaum.config.NeoThaumConfig.TC4_NODE_VARIANT_RARITY, cfg.nodeVariantMultiplier)) {
			// TC4's weights, 3 3 3 1 1, each scaled by its multiplier
			int dark = dev.neothaum.config.NeoThaumConfig.scale(3, cfg.darkNodeMultiplier), unstable = dev.neothaum.config.NeoThaumConfig.scale(3, cfg.unstableNodeMultiplier);
			int pure = dev.neothaum.config.NeoThaumConfig.scale(3, cfg.pureNodeMultiplier), hungry = dev.neothaum.config.NeoThaumConfig.scale(1, cfg.hungryNodeMultiplier);
			int total = dark + unstable + pure + hungry + dev.neothaum.config.NeoThaumConfig.scale(1, cfg.taintedNodeMultiplier);
			if (total > 0) {
				int r = random.nextInt(total);
				if ((r -= dark) < 0) {
					type = NodeType.DARK;
				} else if ((r -= unstable) < 0) {
					type = NodeType.UNSTABLE;
				} else if ((r -= pure) < 0) {
					type = NodeType.PURE;
				} else if ((r -= hungry) < 0) {
					type = NodeType.HUNGRY;
				} else {
					type = NodeType.TAINTED;
				}
			}
		}
		if (forcedType != null) {
			type = forcedType;
		}
		NodeModifier modifier = null;
		int bright = dev.neothaum.config.NeoThaumConfig.scale(1, cfg.brightNodeMultiplier), pale = dev.neothaum.config.NeoThaumConfig.scale(1, cfg.paleNodeMultiplier);
		int modTotal = bright + pale + dev.neothaum.config.NeoThaumConfig.scale(1, cfg.fadingNodeMultiplier);
		if (forcedType == null && modTotal > 0 && dev.neothaum.config.NeoThaumConfig.roll(random, dev.neothaum.config.NeoThaumConfig.TC4_NODE_VARIANT_RARITY / 2, cfg.nodeVariantMultiplier)) {
			int r = random.nextInt(modTotal);
			if ((r -= bright) < 0) {
				modifier = NodeModifier.BRIGHT;
				} else if ((r -= pale) < 0) {
					modifier = NodeModifier.PALE;
				} else {
					modifier = NodeModifier.FADING;
				}
		}
		Holder<Biome> bg = world.getBiome(pos);
		int baura = getBiomeAura(bg);
		if (silverwood || small) {
			baura /= 4;
		}
		int value = random.nextInt(Math.max(1, baura / 2)) + baura / 2;
		Aspect ra = getRandomBiomeTag(bg, random);
		AspectList al = new AspectList();
		if (ra != null) {
			al.add(ra, 2);
		} else {
			al.add(complexAspects.get(random.nextInt(complexAspects.size())), 1);
			al.add(basicAspects.get(random.nextInt(basicAspects.size())), 1);
		}
		for (int a = 0; a < 3; a++) {
			if (random.nextBoolean()) {
				if (dev.neothaum.config.NeoThaumConfig.roll(random, dev.neothaum.config.NeoThaumConfig.TC4_NODE_VARIANT_RARITY, cfg.nodeVariantMultiplier)) {
					al.merge(complexAspects.get(random.nextInt(complexAspects.size())), 1);
				} else {
					al.merge(basicAspects.get(random.nextInt(basicAspects.size())), 1);
				}
			}
		}
		if (type == NodeType.TAINTED) {
			al.merge(Aspect.TAINT, 2);
		}
		if (type == NodeType.HUNGRY) {
			al.merge(Aspect.HUNGER, 2);
			if (random.nextBoolean()) {
				al.merge(Aspect.GREED, 1);
			}
		} else if (type == NodeType.PURE) {
			if (random.nextBoolean()) {
				al.merge(Aspect.LIFE, 2);
			} else {
				al.merge(Aspect.ORDER, 2);
			}
		} else if (type == NodeType.DARK) {
			if (random.nextBoolean()) {
				al.merge(Aspect.DEATH, 1);
			}
			if (random.nextBoolean()) {
				al.merge(Aspect.UNDEAD, 1);
			}
			if (random.nextBoolean()) {
				al.merge(Aspect.ENTROPY, 1);
			}
			if (random.nextBoolean()) {
				al.merge(Aspect.DARKNESS, 1);
			}
		}
		int water = 0, lava = 0, stone = 0, foliage = 0;
		BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
		for (int xx = -5; xx <= 5; xx++) {
			for (int yy = -5; yy <= 5; yy++) {
				for (int zz = -5; zz <= 5; zz++) {
					m.set(pos.getX() + xx, pos.getY() + yy, pos.getZ() + zz);
					if (!world.hasChunk(m.getX() >> 4, m.getZ() >> 4) || world.isOutsideBuildHeight(m)) {
						continue;
					}
					BlockState bi = world.getBlockState(m);
					if (bi.getFluidState().is(FluidTags.WATER)) {
						water++;
					} else if (bi.getFluidState().is(FluidTags.LAVA)) {
						lava++;
					} else if (bi.is(Blocks.STONE)) {
						stone++;
					}
					if (bi.is(BlockTags.LEAVES)) {
						foliage++;
					}
				}
			}
		}
		if (water > 100) {
			al.merge(Aspect.WATER, 1);
		}
		if (lava > 100) {
			al.merge(Aspect.FIRE, 1);
			al.merge(Aspect.EARTH, 1);
		}
		if (stone > 500) {
			al.merge(Aspect.EARTH, 1);
		}
		if (foliage > 100) {
			al.merge(Aspect.PLANT, 1);
		}
		int[] spread = new int[al.size()];
		float total = 0.0F;
		Aspect[] sorted = al.getAspectsSorted();
		for (int ax = 0; ax < spread.length; ax++) {
			spread[ax] = al.getAmount(sorted[ax]) == 2 ? 50 + random.nextInt(25) : 25 + random.nextInt(50);
			total += spread[ax];
		}
		for (int ax = 0; ax < spread.length; ax++) {
			al.merge(sorted[ax], (int) (spread[ax] / total * value));
		}
		createNodeAt(world, pos, type, modifier, al, forcePlace);
	}

	public static void createNodeAt(LevelAccessor world, BlockPos pos, NodeType nt, NodeModifier nm, AspectList al, boolean force) {
		if (force || world.isEmptyBlock(pos)) {
			world.setBlock(pos, TVBlocks.AURA_NODE.defaultBlockState(), 2);
		}
		if (world.getBlockEntity(pos) instanceof NodeBlockEntity node) {
			node.setNodeType(nt);
			node.setNodeModifier(nm);
			node.setAspects(al);
			// outside worldgen the client already has the empty node, so send it the contents
			if (world instanceof net.minecraft.world.level.Level) {
				node.markForUpdate();
			} else {
				node.setChanged();
			}
		}
	}

	/** WorldGenCustomFlowers. */
	public static void generateFlowers(LevelAccessor world, RandomSource rand, BlockPos pos, BlockState plant) {
		for (int i = 0; i < 18; i++) {
			BlockPos p = pos.offset(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));
			BlockState below = world.getBlockState(p.below());
			if (world.isEmptyBlock(p) && (below.is(Blocks.GRASS_BLOCK) || below.is(BlockTags.SAND))) {
				world.setBlock(p, plant, 2);
			}
		}
	}

	private NodeGenerator() {
	}
}
