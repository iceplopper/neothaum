package dev.neothaum.addon.totems;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.neothaum.NeoThaum;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

/**
 * Latin names for Traveller's Totems, from what is round them. A totem surveys about 48 blocks (only chunks already loaded, so it never
 * generates terrain), scores each feature it finds, and is named for the best place feature and the best describing feature of another family
 * (totem_names.json). Ties are broken at random, so two totems in like places needn't share a name; a name already in use gets a numeral.
 */
public final class TotemNames {
	private record Place(String word, char gender, List<String> features) {
	}

	private record Describer(String m, String f, String n, List<String> features) {
		String forGender(char g) {
			return g == 'f' ? this.f : g == 'n' ? this.n : this.m;
		}
	}

	private static final List<Place> PLACES = new ArrayList<>();
	private static final List<Describer> DESCRIBERS = new ArrayList<>();
	private static final Map<String, String> FAMILY = new HashMap<>();
	private static final int RADIUS = 48;

	static {
		try (InputStream in = TotemNames.class.getResourceAsStream("/data/neothaum/neothaum/totem_names.json")) {
			JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
			for (JsonElement e : root.getAsJsonArray("places")) {
				JsonObject o = e.getAsJsonObject();
				PLACES.add(new Place(o.get("word").getAsString(), o.get("gender").getAsString().charAt(0), strings(o)));
			}
			for (JsonElement e : root.getAsJsonArray("describers")) {
				JsonObject o = e.getAsJsonObject();
				DESCRIBERS.add(new Describer(o.get("m").getAsString(), o.get("f").getAsString(), o.get("n").getAsString(), strings(o)));
			}
			for (var fam : root.getAsJsonObject("families").entrySet()) {
				fam.getValue().getAsJsonArray().forEach(f -> FAMILY.put(f.getAsString(), fam.getKey()));
			}
		} catch (Exception ex) {
			NeoThaum.LOGGER.error("Could not read totem_names.json", ex);
		}
	}

	private static List<String> strings(JsonObject o) {
		List<String> out = new ArrayList<>();
		o.getAsJsonArray("features").forEach(f -> out.add(f.getAsString()));
		return out;
	}

	private static String family(String feature) {
		return FAMILY.getOrDefault(feature, feature);
	}

	/** A name for a totem here: the best place word and describing word for its surroundings, numbered if the name is taken. */
	public static String name(ServerLevel level, BlockPos pos, Predicate<String> taken) {
		Map<String, Float> scores = survey(level, pos);
		RandomSource rand = RandomSource.create(pos.asLong() ^ level.getSeed());
		List<String> ranked = new ArrayList<>(scores.keySet());
		// highest first; equal scores in a random order
		java.util.Collections.shuffle(ranked, new java.util.Random(rand.nextLong()));
		ranked.sort(Comparator.comparing((String f) -> -scores.get(f)));
		Place place = null;
		String placeFeature = "nothing";
		for (String f : ranked) {
			List<Place> fits = PLACES.stream().filter(p -> p.features().contains(f)).toList();
			if (!fits.isEmpty()) {
				place = fits.get(rand.nextInt(fits.size()));
				placeFeature = f;
				break;
			}
		}
		if (place == null) {
			place = PLACES.stream().filter(p -> p.features().contains("nothing")).findFirst().orElse(PLACES.get(0));
		}
		Describer describer = null;
		String used = family(placeFeature);
		for (String f : ranked) {
			if (family(f).equals(used)) {
				continue;
			}
			List<Describer> fits = DESCRIBERS.stream().filter(d -> d.features().contains(f)).toList();
			if (!fits.isEmpty()) {
				describer = fits.get(rand.nextInt(fits.size()));
				break;
			}
		}
		if (describer == null) {
			describer = DESCRIBERS.stream().filter(d -> d.features().contains("nothing")).findFirst().orElse(DESCRIBERS.get(0));
		}
		String base = capital(place.word()) + " " + capital(describer.forGender(place.gender()));
		String name = base;
		for (int n = 2; taken.test(name); n++) {
			name = base + " " + roman(n);
		}
		return name;
	}

	/** What is round a totem, each feature scored by how much of it there is. */
	public static Map<String, Float> survey(ServerLevel level, BlockPos centre) {
		Map<String, Float> s = new HashMap<>();
		int sea = level.getSeaLevel();
		int columns = 0;
		int water = 0;
		int oceanCols = 0;
		int riverCols = 0;
		int shallow = 0;
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		long sum = 0;
		int steep = 0;
		Map<String, Integer> biomes = new HashMap<>();
		int edgeOcean = 0;
		int edge = 0;
		int edgeHigher = 0;
		int centreHeight = centre.getY();
		int[][] heights = new int[17][17];
		for (int gx = 0; gx <= 16; gx++) {
			for (int gz = 0; gz <= 16; gz++) {
				heights[gx][gz] = Integer.MIN_VALUE;
				int dx = (gx - 8) * 6;
				int dz = (gz - 8) * 6;
				if (dx * dx + dz * dz > RADIUS * RADIUS) {
					continue;
				}
				int x = centre.getX() + dx;
				int z = centre.getZ() + dz;
				if (!level.hasChunk(x >> 4, z >> 4)) {
					continue;
				}
				columns++;
				int h = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
				int canopy = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
				BlockPos top = new BlockPos(x, h - 1, z);
				BlockState ground = level.getBlockState(top);
				BlockState above = level.getBlockState(top.above());
				heights[gx][gz] = h;
				Holder<Biome> biome = level.getBiome(top);
				biome.unwrapKey().ifPresent(k -> biomes.merge(k.identifier().toString(), 1, Integer::sum));
				boolean wet = !level.getFluidState(top).isEmpty() && level.getFluidState(top).is(Fluids.WATER);
				if (wet) {
					water++;
					int depth = 0;
					while (depth < 8 && level.getFluidState(top.below(depth)).is(Fluids.WATER)) {
						depth++;
					}
					if (depth <= 2) {
						shallow++;
					}
					if (biome.is(BiomeTags.IS_OCEAN)) {
						oceanCols++;
					}
					if (biome.is(BiomeTags.IS_RIVER)) {
						riverCols++;
					}
				} else {
					min = Math.min(min, h);
					max = Math.max(max, h);
					sum += h;
				}
				if (dx * dx + dz * dz > (RADIUS - 8) * (RADIUS - 8)) {
					edge++;
					if (wet && biome.is(BiomeTags.IS_OCEAN)) {
						edgeOcean++;
					}
					if (h > centreHeight + 3) {
						edgeHigher++;
					}
				}
				if (canopy > h + 1) {
					add(s, "forest", 1);
				}
				surface(s, ground, above);
				biome(s, biome);
			}
		}
		// the lie of the land
		for (int gx = 0; gx < 16; gx++) {
			for (int gz = 0; gz < 16; gz++) {
				int a = heights[gx][gz];
				int b = heights[gx + 1][gz];
				int c = heights[gx][gz + 1];
				if (a != Integer.MIN_VALUE && (b != Integer.MIN_VALUE && Math.abs(a - b) > 7 || c != Integer.MIN_VALUE && Math.abs(a - c) > 7)) {
					steep++;
				}
			}
		}
		if (columns == 0) {
			return s;
		}
		float land = columns - water;
		float wf = water / (float) columns;
		if (land > 0) {
			float avg = sum / land;
			int relief = max - min;
			if (relief < 4) {
				add(s, "flat", columns * 0.5F);
			}
			if (relief > 24) {
				add(s, "rugged", columns * 0.4F);
			}
			if (steep > 3) {
				add(s, "cliff", steep * 3);
			}
			if (centreHeight > 110) {
				add(s, "high", (centreHeight - 100) * 2);
			}
			if (centreHeight >= max - 1 && relief > 12) {
				add(s, "summit", columns * 0.7F);
			} else if (centreHeight > avg + 6) {
				add(s, "hill", columns * 0.5F);
			} else if (centreHeight < avg - 6) {
				add(s, edgeHigher >= edge - 1 && edge > 0 ? "enclosed_valley" : "valley", columns * 0.6F);
			} else if (relief > 10 && relief <= 24) {
				add(s, "slope", columns * 0.2F);
			}
			if (Math.abs(centreHeight - sea) <= 4 && relief < 6) {
				add(s, "low", columns * 0.3F);
			}
		}
		if (centreHeight < sea - 5) {
			add(s, "below_sea", (sea - centreHeight) * 3);
		}
		// water
		if (oceanCols > 0) {
			add(s, "ocean", oceanCols);
			if (land > 0 && edge > 0) {
				float edgeShare = edgeOcean / (float) edge;
				if (edgeShare > 0.95F) {
					add(s, "island", columns);
				} else if (edgeShare > 0.65F) {
					add(s, "peninsula", columns * 0.8F);
				} else if (edgeShare > 0.4F) {
					add(s, "bay", columns * 0.5F);
				}
				add(s, "shore", Math.min(oceanCols, land) * 1.5F);
				if (centreHeight > sea + 8) {
					add(s, "headland", columns * 0.5F);
				}
			}
			if (wf > 0.9F) {
				add(s, "ocean", columns * 0.5F);
			}
		}
		if (riverCols > 0) {
			add(s, "river", riverCols * 1.2F);
			add(s, riverCols > columns * 0.25F ? "wide_river" : "stream", riverCols);
			add(s, "riverbank", Math.min(riverCols, land));
			if (shallow > riverCols / 2) {
				add(s, "ford", shallow);
			}
		}
		int still = water - oceanCols - riverCols;
		if (still > 0) {
			add(s, "water", still * 0.5F);
			add(s, still > columns * 0.15F ? "lake" : "pond", still * 1.2F);
			if (s.getOrDefault("desert", 0F) > columns * 0.3F) {
				add(s, "oasis", columns);
			}
			if (centreHeight > 100 && still <= 3) {
				add(s, "spring", columns * 0.4F);
			}
		}
		// two or three biomes meeting
		final int tenth = columns / 10;
		long many = biomes.values().stream().filter(n -> n > tenth).count();
		if (many >= 3) {
			add(s, "crossroads", columns * 0.35F);
		} else if (many == 2) {
			add(s, "boundary", columns * 0.2F);
		}
		blocks(s, level, centre);
		creatures(s, level, centre);
		structures(s, level, centre);
		add(s, "nothing", 0.5F);
		return s;
	}

	private static void add(Map<String, Float> s, String feature, float amount) {
		s.merge(feature, amount, Float::sum);
	}

	/** The ground and what grows on it. */
	private static void surface(Map<String, Float> s, BlockState ground, BlockState above) {
		Block g = ground.getBlock();
		if (ground.is(BlockTags.SAND)) {
			add(s, g == Blocks.RED_SAND ? "red_sand" : "sand", 1);
		}
		if (g == Blocks.SNOW_BLOCK || above.is(Blocks.SNOW) || g == Blocks.POWDER_SNOW) {
			add(s, "snow", 1);
		}
		if (g == Blocks.ICE || g == Blocks.FROSTED_ICE) {
			add(s, "ice", 1.5F);
		}
		if (g == Blocks.PACKED_ICE) {
			add(s, "packed_ice", 2);
		}
		if (g == Blocks.BLUE_ICE) {
			add(s, "blue_ice", 3);
		}
		if (g == Blocks.MUD || g == Blocks.MUDDY_MANGROVE_ROOTS) {
			add(s, "mud", 1.5F);
		}
		if (ground.is(BlockTags.BASE_STONE_OVERWORLD) || g == Blocks.GRAVEL) {
			add(s, "stone", 0.7F);
		}
		if (g == Blocks.CALCITE) {
			add(s, "calcite", 2);
		}
		if (g == Blocks.TERRACOTTA || ground.is(BlockTags.TERRACOTTA)) {
			add(s, "red_sand", 1);
		}
		if (g == Blocks.FARMLAND || above.is(BlockTags.CROPS)) {
			add(s, "farmland", 2);
		}
		if (g == Blocks.DIRT_PATH) {
			add(s, "road", 2);
		}
		if (g == Blocks.MOSS_BLOCK || above.is(Blocks.MOSS_CARPET)) {
			add(s, "moss", 2);
		}
		if (g == Blocks.MYCELIUM || above.is(Blocks.RED_MUSHROOM) || above.is(Blocks.BROWN_MUSHROOM)) {
			add(s, "mushrooms", 1.5F);
		}
		if (above.is(BlockTags.SMALL_FLOWERS) || above.is(BlockTags.FLOWERS)) {
			add(s, "flowers", 1.2F);
		}
		if (above.is(Blocks.SHORT_GRASS) || above.is(Blocks.TALL_GRASS) || above.is(Blocks.FERN)) {
			add(s, "grass", 0.6F);
		}
		if (above.is(Blocks.SUGAR_CANE) || above.is(Blocks.BAMBOO)) {
			add(s, "reeds", 2.5F);
		}
		if (above.is(Blocks.CACTUS)) {
			add(s, "cactus", 3);
		}
		if (above.is(Blocks.SWEET_BERRY_BUSH)) {
			add(s, "berries", 3);
		}
		if (above.is(Blocks.BUSH) || above.is(Blocks.FIREFLY_BUSH)) {
			add(s, "thicket", 1);
		}
	}

	private static void biome(Map<String, Float> s, Holder<Biome> b) {
		String id = b.unwrapKey().map(k -> k.identifier().toString()).orElse("");
		if (b.is(BiomeTags.IS_FOREST)) {
			add(s, "forest", 1);
		}
		if (b.is(BiomeTags.IS_TAIGA)) {
			add(s, id.contains("snowy") ? "snowy_taiga" : "taiga", 1.2F);
			add(s, "spruce", 0.8F);
		}
		if (b.is(BiomeTags.IS_JUNGLE)) {
			add(s, "jungle", 1.2F);
			if (id.contains("sparse")) {
				add(s, "thicket", 1);
			}
		}
		if (b.is(BiomeTags.IS_SAVANNA)) {
			add(s, "savanna", 1.2F);
		}
		if (b.is(BiomeTags.IS_MOUNTAIN)) {
			add(s, "mountain", 1.2F);
		}
		if (b.is(BiomeTags.IS_DEEP_OCEAN)) {
			add(s, "deep_ocean", 1.5F);
		}
		if (b.is(BiomeTags.IS_BEACH)) {
			add(s, "beach", 1.5F);
		}
		if (b.is(BiomeTags.IS_BADLANDS)) {
			add(s, "red_sand", 1);
			add(s, "desert", 0.5F);
		}
		if (b.is(BiomeTags.IS_HILL) || id.contains("windswept")) {
			add(s, "windswept", 1);
			add(s, "ridge", 0.8F);
		}
		switch (id) {
			case "minecraft:plains", "minecraft:sunflower_plains" -> add(s, "plains", 1.2F);
			case "minecraft:meadow" -> add(s, "meadow", 1.5F);
			case "minecraft:desert" -> add(s, "desert", 1.5F);
			case "minecraft:swamp" -> add(s, "swamp", 1.5F);
			case "minecraft:mangrove_swamp" -> {
				add(s, "mangrove", 1.5F);
				add(s, "swamp", 0.5F);
			}
			case "minecraft:dark_forest" -> add(s, "dark_forest", 1.5F);
			case "minecraft:birch_forest", "minecraft:old_growth_birch_forest" -> {
				add(s, "birch", 1.2F);
				add(s, "grove", 0.8F);
			}
			case "minecraft:flower_forest" -> {
				add(s, "grove", 1.2F);
				add(s, "flowers", 1);
			}
			case "minecraft:cherry_grove" -> add(s, "cherry", 1.5F);
			case "minecraft:pale_garden" -> add(s, "pale_garden", 1.5F);
			case "minecraft:stony_peaks", "minecraft:stony_shore" -> add(s, "crag", 1.5F);
			case "minecraft:jagged_peaks", "minecraft:frozen_peaks" -> {
				add(s, "summit", 0.5F);
				add(s, "snow", 0.5F);
			}
			case "minecraft:ice_spikes", "minecraft:frozen_ocean", "minecraft:deep_frozen_ocean" -> add(s, "ice", 1.5F);
			case "minecraft:lush_caves" -> add(s, "lush_caves", 1.5F);
			case "minecraft:dripstone_caves" -> add(s, "dripstone", 1.5F);
			case "minecraft:deep_dark" -> add(s, "deep_dark", 1.5F);
			case "minecraft:warm_ocean", "minecraft:lukewarm_ocean", "minecraft:deep_lukewarm_ocean" -> add(s, "warm_ocean", 1.2F);
			case "minecraft:cold_ocean", "minecraft:deep_cold_ocean", "minecraft:snowy_plains", "minecraft:snowy_beach", "minecraft:grove" ->
				add(s, "cold", 1);
			case "minecraft:mushroom_fields" -> add(s, "mushrooms", 1.5F);
			case "minecraft:sparse_jungle" -> add(s, "sparse_forest", 0.8F);
			case "neothaum:magical_forest" -> add(s, "magical_forest", 2);
			case "neothaum:tainted_land" -> add(s, "taint", 2);
			case "neothaum:eerie" -> add(s, "eerie", 2);
			default -> {
			}
		}
		if (id.contains("forest") && !id.contains("dark") && !id.contains("birch") && !id.contains("flower") && !id.contains("magical")) {
			add(s, "sparse_forest", 0.2F);
		}
	}

	/** Blocks near the totem itself: ores, stone, crystals, nodes, taint, caves, lava, altars. */
	private static void blocks(Map<String, Float> s, ServerLevel level, BlockPos centre) {
		int caveAir = 0;
		for (BlockPos p : BlockPos.betweenClosed(centre.offset(-16, -12, -16), centre.offset(16, 12, 16))) {
			if ((p.getX() + p.getY() + p.getZ()) % 2 != 0 || !level.hasChunk(p.getX() >> 4, p.getZ() >> 4)) {
				continue;
			}
			BlockState b = level.getBlockState(p);
			Block k = b.getBlock();
			Identifier id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(k);
			String path = id.getPath();
			if (b.isAir()) {
				if (p.getY() < level.getHeight(Heightmap.Types.WORLD_SURFACE, p.getX(), p.getZ()) - 2) {
					caveAir++;
				}
				continue;
			}
			if (k == Blocks.IRON_ORE || k == Blocks.DEEPSLATE_IRON_ORE || k == Blocks.RAW_IRON_BLOCK) {
				add(s, "iron", 2);
			} else if (k == Blocks.GOLD_ORE || k == Blocks.DEEPSLATE_GOLD_ORE || k == Blocks.RAW_GOLD_BLOCK) {
				add(s, "gold", 3);
			} else if (k == Blocks.AMETHYST_BLOCK || k == Blocks.BUDDING_AMETHYST || k == Blocks.AMETHYST_CLUSTER) {
				add(s, "amethyst", 3);
			} else if (k == Blocks.BLACKSTONE || k == Blocks.BASALT || k == Blocks.OBSIDIAN) {
				add(s, "blackstone", 1.5F);
			} else if (k == Blocks.CALCITE) {
				add(s, "calcite", 1);
			} else if (k == Blocks.MAGMA_BLOCK || b.getFluidState().is(Fluids.LAVA)) {
				add(s, "lava", 2);
			} else if (k == Blocks.SCULK || k == Blocks.SCULK_VEIN || k == Blocks.SCULK_SENSOR) {
				add(s, "deep_dark", 1.5F);
			} else if (k == Blocks.POINTED_DRIPSTONE || k == Blocks.DRIPSTONE_BLOCK) {
				add(s, "dripstone", 1);
			} else if (k == Blocks.BEE_NEST || k == Blocks.BEEHIVE) {
				add(s, "bees", 8);
			} else if (id.getNamespace().equals(NeoThaum.MOD_ID)) {
				if (path.contains("infused_stone") || path.contains("crystal_cluster")) {
					add(s, "infused_stone", 3);
				} else if (path.equals("aura_node") || path.equals("energized_aura_node")) {
					add(s, "aura_node", 40);
				} else if (path.startsWith("silverwood")) {
					add(s, "silverwood", 1.5F);
				} else if (path.startsWith("greatwood")) {
					add(s, "magical_forest", 0.6F);
				} else if (path.contains("taint") || path.equals("flesh_block")) {
					add(s, "taint", 2);
				} else if (path.contains("obsidian_totem") || path.equals("eldritch_altar")) {
					add(s, "altar", 10);
				} else if (path.equals("eldritch_obelisk") || path.equals("eldritch_cap")) {
					add(s, "tower", 4);
				}
			}
		}
		if (caveAir > 120) {
			add(s, "cave", caveAir / 30.0F);
		}
	}

	private static void creatures(Map<String, Float> s, ServerLevel level, BlockPos centre) {
		for (Entity e : level.getEntities((Entity) null, new AABB(centre).inflate(RADIUS, 24, RADIUS), e -> true)) {
			EntityType<?> t = e.getType();
			if (t == net.minecraft.world.entity.EntityTypes.COW || t == net.minecraft.world.entity.EntityTypes.SHEEP || t == net.minecraft.world.entity.EntityTypes.PIG || t == net.minecraft.world.entity.EntityTypes.GOAT || t == net.minecraft.world.entity.EntityTypes.CHICKEN) {
				add(s, "pasture", 2);
			} else if (t == net.minecraft.world.entity.EntityTypes.HORSE || t == net.minecraft.world.entity.EntityTypes.DONKEY || t == net.minecraft.world.entity.EntityTypes.MULE) {
				add(s, "horses", 4);
			} else if (t == net.minecraft.world.entity.EntityTypes.WOLF) {
				add(s, "wolves", 4);
			} else if (t == net.minecraft.world.entity.EntityTypes.FOX) {
				add(s, "foxes", 4);
			} else if (t == net.minecraft.world.entity.EntityTypes.POLAR_BEAR || t == net.minecraft.world.entity.EntityTypes.PANDA) {
				add(s, "bears", 5);
			} else if (t == net.minecraft.world.entity.EntityTypes.COD || t == net.minecraft.world.entity.EntityTypes.SALMON || t == net.minecraft.world.entity.EntityTypes.TROPICAL_FISH || t == net.minecraft.world.entity.EntityTypes.PUFFERFISH) {
				add(s, "fish", 1);
			} else if (t == net.minecraft.world.entity.EntityTypes.BEE) {
				add(s, "bees", 2);
			} else if (t == net.minecraft.world.entity.EntityTypes.ZOMBIE_VILLAGER) {
				add(s, "abandoned_village", 3);
			} else if (t == net.minecraft.world.entity.EntityTypes.VILLAGER) {
				add(s, "village", 4);
			}
		}
	}

	/** Structures within a few chunks: villages, outposts, temples, ruins. */
	private static void structures(Map<String, Float> s, ServerLevel level, BlockPos centre) {
		var registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
		Set<String> seen = new HashSet<>();
		int cx = centre.getX() >> 4;
		int cz = centre.getZ() >> 4;
		for (int x = cx - 3; x <= cx + 3; x++) {
			for (int z = cz - 3; z <= cz + 3; z++) {
				if (!level.hasChunk(x, z)) {
					continue;
				}
				var chunk = level.getChunk(x, z);
				for (var structure : chunk.getAllReferences().keySet()) {
					registry.getResourceKey(structure).ifPresent(k -> seen.add(k.identifier().getPath()));
				}
			}
		}
		boolean water = s.getOrDefault("ocean", 0F) + s.getOrDefault("river", 0F) + s.getOrDefault("lake", 0F) > 4;
		for (String path : seen) {
			if (path.startsWith("village")) {
				add(s, water ? "harbour" : "village", 60);
				if (water) {
					add(s, "village", 20);
				}
			} else if (path.contains("outpost")) {
				add(s, "outpost", 60);
			} else if (path.contains("pyramid") || path.contains("temple")) {
				add(s, "temple", 60);
			} else if (path.contains("ruin") || path.contains("trail")) {
				add(s, "ruins", 40);
			}
		}
	}

	private static String capital(String w) {
		return w.isEmpty() ? w : Character.toUpperCase(w.charAt(0)) + w.substring(1);
	}

	private static String roman(int n) {
		String[] r = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
		return n < r.length ? r[n] : String.valueOf(n);
	}

	private TotemNames() {
	}
}
