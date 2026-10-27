package dev.neothaum.api.aspects;

import dev.neothaum.network.TVNetwork;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Port of thaumcraft.common.lib.events.EssentiaHandler: finds the essentia sources (jars) around a device and drains them a point at a time.
 * The source list is cached per device; when it runs dry the device waits five seconds before looking again.
 */
public final class EssentiaHandler {
	static final long DELAY = 5000L;

	private record Key(ResourceKey<Level> dim, BlockPos pos) {
	}

	private static final Map<Key, List<BlockPos>> SOURCES = new HashMap<>();
	private static final Map<Key, Long> SOURCES_DELAY = new HashMap<>();

	/** direction null is ForgeDirection.UNKNOWN: a cube of the given range all around the device. */
	public static boolean drainEssentia(BlockEntity tile, Aspect aspect, Direction direction, int range) {
		Level level = tile.getLevel();
		Key key = new Key(level.dimension(), tile.getBlockPos());
		if (!SOURCES.containsKey(key)) {
			getSources(level, key, direction, range);
			return SOURCES.containsKey(key) && drainEssentia(tile, aspect, direction, range);
		}
		for (BlockPos source : SOURCES.get(key)) {
			if (!(level.getBlockEntity(source) instanceof AspectSource as)) {
				break;
			}
			if (as.takeFromContainer(aspect, 1)) {
				TVNetwork.sendEssentiaSource(level, tile.getBlockPos(), source, aspect.getColor());
				return true;
			}
		}
		SOURCES.remove(key);
		SOURCES_DELAY.put(key, System.currentTimeMillis() + DELAY);
		return false;
	}

	public static boolean findEssentia(BlockEntity tile, Aspect aspect, Direction direction, int range) {
		Level level = tile.getLevel();
		Key key = new Key(level.dimension(), tile.getBlockPos());
		if (!SOURCES.containsKey(key)) {
			getSources(level, key, direction, range);
			return SOURCES.containsKey(key) && findEssentia(tile, aspect, direction, range);
		}
		for (BlockPos source : SOURCES.get(key)) {
			if (!(level.getBlockEntity(source) instanceof AspectSource as)) {
				break;
			}
			if (as.doesContainerContainAmount(aspect, 1)) {
				return true;
			}
		}
		SOURCES.remove(key);
		SOURCES_DELAY.put(key, System.currentTimeMillis() + DELAY);
		return false;
	}

	private static void getSources(Level level, Key key, Direction direction, int range) {
		Long d = SOURCES_DELAY.get(key);
		if (d != null) {
			if (d > System.currentTimeMillis()) {
				return;
			}
			SOURCES_DELAY.remove(key);
		}
		List<BlockPos> sourceList = new ArrayList<>();
		int start = 0;
		if (direction == null) {
			start = -range;
			direction = Direction.UP;
		}
		BlockPos p = key.pos();
		BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
		for (int aa = -range; aa <= range; aa++) {
			for (int bb = -range; bb <= range; bb++) {
				for (int cc = start; cc < range; cc++) {
					if (aa == 0 && bb == 0 && cc == 0) {
						continue;
					}
					if (direction.getStepY() != 0) {
						m.set(p.getX() + aa, p.getY() + cc * direction.getStepY(), p.getZ() + bb);
					} else if (direction.getStepX() == 0) {
						m.set(p.getX() + aa, p.getY() + bb, p.getZ() + cc * direction.getStepZ());
					} else {
						m.set(p.getX() + cc * direction.getStepX(), p.getY() + aa, p.getZ() + bb);
					}
					if (level.isLoaded(m) && level.getBlockEntity(m) instanceof AspectSource) {
						sourceList.add(m.immutable());
					}
				}
			}
		}
		if (!sourceList.isEmpty()) {
			SOURCES.put(key, sourceList);
		} else {
			SOURCES_DELAY.put(key, System.currentTimeMillis() + DELAY);
		}
	}

	public static void refreshSources(BlockEntity tile) {
		SOURCES.remove(new Key(tile.getLevel().dimension(), tile.getBlockPos()));
	}

	private EssentiaHandler() {
	}
}
