package dev.thaumvibe.api.visnet;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.network.TVNetwork;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * Port of thaumcraft.api.visnet.VisNetHandler: the energized nodes of each dimension, the trees of relays hanging off them, and drainVis, which
 * pulls centivis for a machine from the closest node of each tree in range.
 */
public final class VisNetHandler {
	private static final Map<ResourceKey<Level>, Map<BlockPos, WeakReference<VisNodeBlockEntity>>> SOURCES = new HashMap<>();
	private static final Map<ResourceKey<Level>, Map<BlockPos, List<WeakReference<VisNodeBlockEntity>>>> NEARBY = new HashMap<>();

	private static Map<BlockPos, WeakReference<VisNodeBlockEntity>> sources(Level level) {
		return SOURCES.computeIfAbsent(level.dimension(), k -> new HashMap<>());
	}

	private static Map<BlockPos, List<WeakReference<VisNodeBlockEntity>>> nearby(Level level) {
		return NEARBY.computeIfAbsent(level.dimension(), k -> new HashMap<>());
	}

	/** Centivis of a primal drawn from the network for a machine at pos; returns how much was found. */
	public static int drainVis(Level level, BlockPos pos, Aspect aspect, int amount) {
		int drained = 0;
		List<WeakReference<VisNodeBlockEntity>> nodes = nearby(level).get(pos);
		if (nodes == null) {
			calculateNearbyNodes(level, pos);
			nodes = nearby(level).get(pos);
		}
		if (nodes != null) {
			for (WeakReference<VisNodeBlockEntity> ref : nodes) {
				VisNodeBlockEntity node = ref.get();
				if (node == null) {
					continue;
				}
				int a = node.consumeVis(aspect, amount);
				drained += a;
				amount -= a;
				if (a > 0) {
					TVNetwork.sendVisEffect(level, pos, node.getBlockPos(), Aspect.getPrimalAspects().indexOf(aspect));
				}
				if (amount <= 0) {
					break;
				}
			}
		}
		return drained;
	}

	public static void addSource(Level level, VisNodeBlockEntity vs) {
		sources(level).put(vs.getBlockPos(), new WeakReference<>(vs));
		nearby(level).clear();
	}

	static void removeSource(Level level, BlockPos pos) {
		sources(level).remove(pos);
		nearby(level).clear();
	}

	public static boolean isNodeValid(WeakReference<VisNodeBlockEntity> node) {
		return node != null && node.get() != null && !node.get().isRemoved();
	}

	/** Finds the closest node (source or child) this node can link to, and adopts it there. */
	public static WeakReference<VisNodeBlockEntity> addNode(Level level, VisNodeBlockEntity vn) {
		Map<BlockPos, WeakReference<VisNodeBlockEntity>> sourcelist = sources(level);
		if (sourcelist.isEmpty()) {
			return null;
		}
		List<Object[]> near = new ArrayList<>();
		for (WeakReference<VisNodeBlockEntity> root : sourcelist.values()) {
			if (isNodeValid(root)) {
				VisNodeBlockEntity source = root.get();
				float r = inRange(vn.getBlockPos(), source.getBlockPos(), vn.getRange());
				if (r > 0.0F) {
					near.add(new Object[]{source, r - vn.getRange() * 2});
				}
				findClosestNodes(vn, source, near, new HashSet<>());
			}
		}
		float dist = Float.MAX_VALUE;
		VisNodeBlockEntity closest = null;
		for (Object[] o : near) {
			VisNodeBlockEntity n = (VisNodeBlockEntity) o[0];
			if ((Float) o[1] < dist && (vn.getAttunement() == -1 || n.getAttunement() == -1 || vn.getAttunement() == n.getAttunement())
				&& canNodeBeSeen(vn, n)) {
				dist = (Float) o[1];
				closest = n;
			}
		}
		if (closest == null) {
			return null;
		}
		closest.getChildren().add(new WeakReference<>(vn));
		nearby(level).clear();
		return new WeakReference<>(closest);
	}

	private static void findClosestNodes(VisNodeBlockEntity target, VisNodeBlockEntity parent, List<Object[]> in, Set<BlockPos> seen) {
		if (seen.size() > 512 || !seen.add(parent.getBlockPos())) {
			return;
		}
		for (WeakReference<VisNodeBlockEntity> childRef : parent.getChildren()) {
			VisNodeBlockEntity child = childRef.get();
			if (child != null && child != target && child != parent) {
				float r2 = inRange(child.getBlockPos(), target.getBlockPos(), target.getRange());
				if (r2 > 0.0F) {
					in.add(new Object[]{child, r2});
				}
				findClosestNodes(target, child, in, seen);
			}
		}
	}

	private static float inRange(BlockPos a, BlockPos b, int range) {
		float d = (float) a.distSqr(b);
		return d > range * range ? -1.0F : d;
	}

	private static void calculateNearbyNodes(Level level, BlockPos drainer) {
		List<WeakReference<VisNodeBlockEntity>> cn = new ArrayList<>();
		for (WeakReference<VisNodeBlockEntity> root : sources(level).values()) {
			if (!isNodeValid(root)) {
				continue;
			}
			VisNodeBlockEntity source = root.get();
			VisNodeBlockEntity closest = null;
			float range = Float.MAX_VALUE;
			float r = inRange(drainer, source.getBlockPos(), source.getRange());
			if (r > 0.0F) {
				range = r;
				closest = source;
			}
			for (WeakReference<VisNodeBlockEntity> child : allChildren(source, new ArrayList<>())) {
				VisNodeBlockEntity n = child.get();
				if (n != null && n != source) {
					float r2 = inRange(n.getBlockPos(), drainer, n.getRange());
					if (r2 > 0.0F && r2 < range) {
						range = r2;
						closest = n;
					}
				}
			}
			if (closest != null) {
				cn.add(new WeakReference<>(closest));
			}
		}
		nearby(level).put(drainer.immutable(), cn);
	}

	private static List<WeakReference<VisNodeBlockEntity>> allChildren(VisNodeBlockEntity source, List<WeakReference<VisNodeBlockEntity>> list) {
		for (WeakReference<VisNodeBlockEntity> child : source.getChildren()) {
			VisNodeBlockEntity n = child.get();
			if (n != null && n.getLevel() != null && n.getLevel().isLoaded(n.getBlockPos()) && list.size() < 4096) {
				list.add(child);
				allChildren(n, list);
			}
		}
		return list;
	}

	/** ThaumcraftApiHelper.rayTraceIgnoringSource: nothing solid between the two, or what is hit is the target itself. */
	public static boolean canNodeBeSeen(VisNodeBlockEntity source, VisNodeBlockEntity target) {
		Level level = source.getLevel();
		if (level == null) {
			return false;
		}
		Vec3 from = Vec3.atCenterOf(source.getBlockPos());
		Vec3 to = Vec3.atCenterOf(target.getBlockPos());
		BlockHitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()) {
			@Override
			public net.minecraft.world.phys.shapes.VoxelShape getBlockShape(net.minecraft.world.level.block.state.BlockState state,
				net.minecraft.world.level.BlockGetter getter, BlockPos pos) {
				return pos.equals(source.getBlockPos()) ? net.minecraft.world.phys.shapes.Shapes.empty() : super.getBlockShape(state, getter, pos);
			}
		});
		return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(target.getBlockPos());
	}

	private VisNetHandler() {
	}
}
