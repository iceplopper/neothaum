package dev.neothaum.golem;

import dev.neothaum.api.aspects.EssentiaTransport;
import dev.neothaum.tiles.JarBlockEntity;
import dev.neothaum.tiles.ReservoirBlockEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

/**
 * GolemHelper: what golems look for among their markers. Containers are found by marked block faces (a marker's colour narrows which filter
 * slots it serves), items the golem could not place are ignored for golemIgnoreDelay, and liquid and essentia golems look for tanks, liquid
 * source blocks, jars, reservoirs and tubes. Liquids are in millibuckets as in TC4, converted to droplets at the transfer API.
 */
public final class GolemHelper {
	public static final double ADJACENT_RANGE = 4.0;
	/** Droplets per millibucket. */
	public static final long MB = FluidConstants.BUCKET / 1000;
	private static final List<Timeout> ITEM_TIMEOUT = new ArrayList<>();

	private GolemHelper() {
	}

	private record Timeout(int golemId, ItemStack stack, long time) {
		boolean same(int id, ItemStack s) {
			return this.golemId == id && ItemStack.isSameItemSameComponents(this.stack, s);
		}
	}

	// ------------------------------------------------------------------ containers

	/** getMarkedContainers: each marked block holding an inventory. */
	public static List<BlockPos> getMarkedContainers(GolemEntity golem) {
		List<BlockPos> out = new ArrayList<>();
		for (GolemMarker m : golem.getMarkers()) {
			if (!out.contains(m.pos()) && GolemInventory.getContainer(golem.level(), m.pos()) != null) {
				out.add(m.pos());
			}
		}
		return out;
	}

	public static double distSqr(GolemEntity golem, BlockPos p) {
		return golem.distanceToSqr(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
	}

	public static List<BlockPos> getMarkedContainersAdjacentToGolem(GolemEntity golem) {
		List<BlockPos> out = new ArrayList<>();
		for (BlockPos p : getMarkedContainers(golem)) {
			if (distSqr(golem, p) < ADJACENT_RANGE) {
				out.add(p);
			}
		}
		return out;
	}

	/** Marked blocks that are not inventories (places to drop things or use items), next to the golem. */
	public static List<BlockPos> getMarkedBlocksAdjacentToGolem(GolemEntity golem, byte color) {
		List<BlockPos> out = new ArrayList<>();
		for (GolemMarker m : golem.getMarkers()) {
			if ((m.color() == color || color == -1) && GolemInventory.getContainer(golem.level(), m.pos()) == null
				&& distSqr(golem, m.pos()) < ADJACENT_RANGE) {
				out.add(m.pos());
			}
		}
		return out;
	}

	/** getMarkedSides: the faces of a block marked for this colour (-1 matches any marker). */
	public static List<Integer> getMarkedSides(GolemEntity golem, BlockPos pos, byte color) {
		List<Integer> out = new ArrayList<>();
		List<GolemMarker> gm = golem.getMarkers();
		for (int a = 0; a < 6; a++) {
			for (GolemMarker m : gm) {
				if (m.pos().equals(pos) && m.side() == a && (color == m.color() || color == -1)) {
					out.add(a);
					break;
				}
			}
		}
		return out;
	}

	/** getContainersWithRoom: marked containers that would take some of this stack through a face marked in this colour. */
	public static List<BlockPos> getContainersWithRoom(GolemEntity golem, byte color, ItemStack item) {
		List<BlockPos> out = new ArrayList<>();
		for (BlockPos p : getMarkedContainers(golem)) {
			Container inv = GolemInventory.getContainer(golem.level(), p);
			for (int side : getMarkedSides(golem, p, color)) {
				ItemStack result = GolemInventory.placeItemStackIntoInventory(item, inv, side, false);
				if (!ItemStack.matches(result, item)) {
					out.add(p);
					break;
				}
			}
		}
		return out;
	}

	public static List<BlockPos> getContainersWithGoods(GolemEntity golem, ItemStack goods, byte color) {
		List<BlockPos> out = new ArrayList<>();
		for (BlockPos p : getMarkedContainers(golem)) {
			Container inv = GolemInventory.getContainer(golem.level(), p);
			for (int side : getMarkedSides(golem, p, color)) {
				if (!GolemInventory.extractStack(inv, goods, side, golem.checkOreDict(), golem.ignoreDamage(), golem.ignoreNBT(), false).isEmpty()) {
					out.add(p);
					break;
				}
			}
		}
		return out;
	}

	public static @Nullable Container getHomeContainer(GolemEntity golem) {
		return GolemInventory.getContainer(golem.level(), golem.getHomeContainerPos());
	}

	public static boolean isHomeContainer(GolemEntity golem, BlockPos p) {
		return p.equals(golem.getHomeContainerPos());
	}

	/** getMissingItems: what the fill golem's filter asks for that the home container is short of (null when there is no container). */
	public static @Nullable List<ItemStack> getMissingItems(GolemEntity golem) {
		int slotCount = golem.getInventorySize();
		List<ItemStack> qr = new ArrayList<>();
		if (golem.getToggle(0)) {
			for (int q = 0; q < slotCount; q++) {
				if (!golem.getFilterItem(q).isEmpty()) {
					qr.add(golem.getFilterItem(q).copy());
				}
			}
			return qr;
		}
		Container tile = getHomeContainer(golem);
		if (tile == null) {
			return null;
		}
		int side = golem.getHomeFacingId();
		boolean fuzzy = golem.getUpgradeAmount(5) > 0;
		outer:
		for (int q = 0; q < slotCount; q++) {
			ItemStack toCheck = golem.getFilterItem(q);
			if (toCheck.isEmpty()) {
				continue;
			}
			int found = 0;
			for (ItemStack s : GolemInventory.contents(tile, side)) {
				if (GolemInventory.areItemStacksEqual(s, toCheck, golem.checkOreDict(), golem.ignoreDamage(), golem.ignoreNBT())) {
					found += s.getCount();
					if (found >= golem.getAmountNeededSmart(s, fuzzy)) {
						continue outer;
					}
				}
			}
			ItemStack ret = toCheck.copy();
			ret.shrink(found);
			qr.add(ret);
		}
		return qr;
	}

	/** getItemsInHomeContainer: what the sorting golem may take from its home container. */
	public static @Nullable List<ItemStack> getItemsInHomeContainer(GolemEntity golem) {
		Container inv = getHomeContainer(golem);
		return inv == null ? null : GolemInventory.contents(inv, golem.getHomeFacingId());
	}

	/** getItemsNeeded: for empty, use and sorting golems, the items they still have somewhere to take (null for other cores). */
	public static @Nullable List<ItemStack> getItemsNeeded(GolemEntity golem, boolean fuzzy) {
		switch (golem.getCore()) {
			case 1, 8 -> {
				List<ItemStack> needed = golem.getItemsNeeded(fuzzy);
				if (needed.isEmpty()) {
					return null;
				}
				List<ItemStack> out = new ArrayList<>();
				for (ItemStack it : needed) {
					if (!isOnTimeOut(golem, it) && (golem.getCore() == 1 ? findSomethingEmptyCore(golem, it) : findSomethingUseCore(golem, it))) {
						out.add(it);
					}
				}
				return out;
			}
			case 10 -> {
				List<ItemStack> needed = getItemsInHomeContainer(golem);
				List<ItemStack> out = new ArrayList<>();
				if (needed != null) {
					for (ItemStack it : needed) {
						if (!isOnTimeOut(golem, it) && findSomethingSortCore(golem, it)) {
							out.add(it);
						}
					}
				}
				return out;
			}
			default -> {
				return null;
			}
		}
	}

	/** findSomethingUseCore: a marked block (or empty space, with the toggle) in one of the item's colours, with air on its marked side. */
	public static boolean findSomethingUseCore(GolemEntity golem, ItemStack item) {
		Level level = golem.level();
		for (byte col : golem.getColorsMatching(item)) {
			for (GolemMarker m : golem.getMarkers()) {
				boolean air = level.getBlockState(m.pos()).isAir();
				if ((m.color() == col || col == -1) && golem.getToggle(0) == air && level.getBlockState(m.pos().relative(Direction.from3DDataValue(m.side())))
					.isAir()) {
					return true;
				}
			}
		}
		timeOut(golem, item);
		return false;
	}

	/** findSomethingEmptyCore: a marked container in range (not the home one) with room, or a marked spot to drop it. */
	public static boolean findSomethingEmptyCore(GolemEntity golem, ItemStack item) {
		List<Byte> colors = golem.getColorsMatching(item);
		float dmod = golem.getRange();
		for (byte color : colors) {
			for (BlockPos p : getContainersWithRoom(golem, color, item)) {
				if (distSqr(golem, p) <= dmod * dmod && !isHomeContainer(golem, p)) {
					return true;
				}
			}
		}
		for (byte color : colors) {
			for (GolemMarker m : golem.getMarkers()) {
				if ((m.color() == color || color == -1) && GolemInventory.getContainer(golem.level(), m.pos()) == null) {
					return true;
				}
			}
		}
		timeOut(golem, item);
		return false;
	}

	/** findSomethingSortCore: a marked container in range that already holds this item and has room for it. */
	public static boolean findSomethingSortCore(GolemEntity golem, ItemStack item) {
		float dmod = golem.getRange();
		for (BlockPos p : getContainersWithRoom(golem, (byte) -1, item)) {
			if (distSqr(golem, p) <= dmod * dmod && !isHomeContainer(golem, p)) {
				Container inv = GolemInventory.getContainer(golem.level(), p);
				for (int side : getMarkedSides(golem, p, (byte) -1)) {
					if (GolemInventory.inventoryContains(inv, item, side, golem.checkOreDict(), golem.ignoreDamage(), golem.ignoreNBT())) {
						return true;
					}
				}
			}
		}
		timeOut(golem, item);
		return false;
	}

	private static void timeOut(GolemEntity golem, ItemStack item) {
		ITEM_TIMEOUT.add(new Timeout(golem.getId(), item.copy(), System.currentTimeMillis() + dev.neothaum.config.NeoThaumConfig.current.golemIgnoreDelay));
	}

	public static boolean isOnTimeOut(GolemEntity golem, ItemStack stack) {
		Iterator<Timeout> it = ITEM_TIMEOUT.iterator();
		while (it.hasNext()) {
			Timeout t = it.next();
			if (t.same(golem.getId(), stack)) {
				if (System.currentTimeMillis() < t.time) {
					return true;
				}
				it.remove();
				return false;
			}
		}
		return false;
	}

	/** validTargetForItem: whether the golem has anywhere to take this item. */
	public static boolean validTargetForItem(GolemEntity golem, ItemStack stack) {
		if (isOnTimeOut(golem, stack)) {
			return false;
		}
		switch (golem.getCore()) {
			case 1 -> {
				return findSomethingEmptyCore(golem, stack);
			}
			case 8 -> {
				return findSomethingUseCore(golem, stack);
			}
			case 10 -> {
				return findSomethingSortCore(golem, stack);
			}
			default -> {
				List<ItemStack> needed = getItemsNeeded(golem, golem.getUpgradeAmount(5) > 0);
				if (needed != null) {
					for (ItemStack ss : needed) {
						if (GolemInventory.areItemStacksEqual(ss, golem.itemCarried, golem.checkOreDict(), golem.ignoreDamage(), golem.ignoreNBT())) {
							return true;
						}
					}
				}
				timeOut(golem, stack);
				return false;
			}
		}
	}

	/** getFirstItemUsingTimeout: as much as the golem can carry of the first item in the container not on timeout. */
	public static ItemStack getFirstItemUsingTimeout(GolemEntity golem, Container inv, int side, boolean doit) {
		ItemStack stack = ItemStack.EMPTY;
		for (int s : GolemInventory.sideSlots(inv, side)) {
			if (stack.isEmpty() && !inv.getItem(s).isEmpty()) {
				if (isOnTimeOut(golem, inv.getItem(s))) {
					continue;
				}
				stack = inv.getItem(s).copyWithCount(golem.getCarrySpace());
			}
			if (!stack.isEmpty()) {
				ItemStack out = GolemInventory.attemptExtraction(inv, stack, s, side, false, false, false, doit);
				if (!out.isEmpty()) {
					return out.copy();
				}
				stack = ItemStack.EMPTY;
			}
		}
		if (doit) {
			inv.setChanged();
		}
		return ItemStack.EMPTY;
	}

	// ------------------------------------------------------------------ liquids

	public static @Nullable Storage<FluidVariant> fluidStorage(Level level, BlockPos pos, int side) {
		return FluidStorage.SIDED.find(level, pos, Direction.from3DDataValue(side));
	}

	/** getMissingLiquids: the liquids the home tank would take (only those the filter shows a container of, if it shows any). */
	public static List<Fluid> getMissingLiquids(GolemEntity golem) {
		List<Fluid> out = new ArrayList<>();
		Storage<FluidVariant> tank = fluidStorage(golem.level(), golem.getHomeContainerPos(), golem.getHomeFacingId());
		if (tank == null) {
			return out;
		}
		for (Fluid f : BuiltInRegistries.FLUID) {
			if (f == Fluids.EMPTY || !f.isSource(f.defaultFluidState())) {
				continue;
			}
			if (golem.fluidCarried != null && golem.fluidAmount > 0 && golem.fluidCarried != f) {
				continue;
			}
			if (StorageUtil.simulateInsert(tank, FluidVariant.of(f), FluidConstants.BUCKET, null) <= 0) {
				continue;
			}
			if (golem.hasSomething() && !filterShowsFluid(golem, f)) {
				continue;
			}
			out.add(f);
		}
		return out;
	}

	private static boolean filterShowsFluid(GolemEntity golem, Fluid f) {
		for (int a = 0; a < golem.getInventorySize(); a++) {
			ItemStack s = golem.getFilterItem(a);
			if (!s.isEmpty() && fluidIn(s) == f) {
				return true;
			}
		}
		return false;
	}

	/** FluidContainerRegistry.getFluidForFilledItem: the liquid a filled container holds. */
	public static @Nullable Fluid fluidIn(ItemStack s) {
		var storage = net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext.withConstant(s).find(FluidStorage.ITEM);
		if (storage != null) {
			for (var view : storage.nonEmptyViews()) {
				return view.getResource().getFluid();
			}
		}
		return null;
	}

	/** Can this much of the liquid be drained from a tank through a face (simulated)? */
	public static long drainable(Storage<FluidVariant> s, Fluid f, long droplets) {
		return StorageUtil.simulateExtract(s, FluidVariant.of(f), droplets, null);
	}

	/** getMarkedFluidHandlersAdjacentToGolem: marked tanks within reach that hold this liquid. */
	public static List<GolemMarker> getMarkedFluidHandlersAdjacentToGolem(Fluid f, GolemEntity golem) {
		List<GolemMarker> out = new ArrayList<>();
		for (GolemMarker m : golem.getMarkers()) {
			Storage<FluidVariant> s = fluidStorage(golem.level(), m.pos(), m.side());
			if (s != null && distSqr(golem, m.pos()) < ADJACENT_RANGE && drainable(s, f, MB) > 0) {
				out.add(m);
			}
		}
		return out;
	}

	/** getMarkedFluidHandlers: marked tanks anywhere that hold this liquid. */
	public static List<BlockPos> getMarkedFluidHandlers(Fluid f, GolemEntity golem) {
		List<BlockPos> out = new ArrayList<>();
		for (GolemMarker m : golem.getMarkers()) {
			Storage<FluidVariant> s = fluidStorage(golem.level(), m.pos(), m.side());
			if (s != null && drainable(s, f, MB) > 0) {
				out.add(m.pos());
			}
		}
		return out;
	}

	/** Is this a source block of the liquid that could be picked up? */
	public static boolean isFluidSource(Level level, BlockPos p, Fluid f) {
		FluidState fs = level.getFluidState(p);
		return fs.isSource() && fs.getType().isSame(f) && level.getBlockState(p).getBlock() instanceof LiquidBlock;
	}

	/** getMarkedFluidBlocks: marked source blocks of this liquid. */
	public static List<BlockPos> getMarkedFluidBlocks(Fluid f, GolemEntity golem) {
		List<BlockPos> out = new ArrayList<>();
		for (GolemMarker m : golem.getMarkers()) {
			if (isFluidSource(golem.level(), m.pos(), f)) {
				out.add(m.pos());
			}
		}
		return out;
	}

	/** findPossibleLiquid: the nearest marked tank in range holding the liquid, or else the nearest marked source block. */
	public static @Nullable BlockPos findPossibleLiquid(Fluid f, GolemEntity golem) {
		float dmod = golem.getRange();
		BlockPos v = null;
		double dd = Double.MAX_VALUE;
		for (BlockPos p : getMarkedFluidHandlers(f, golem)) {
			double d = distSqr(golem, p);
			if (d <= dmod * dmod && d < dd) {
				dd = d;
				v = p;
			}
		}
		if (v == null) {
			dd = Double.MAX_VALUE;
			for (BlockPos p : getMarkedFluidBlocks(f, golem)) {
				double d = distSqr(golem, p);
				if (d <= dmod * dmod && d < dd) {
					dd = d;
					v = p;
				}
			}
		}
		return v;
	}

	// ------------------------------------------------------------------ essentia

	/**
	 * findJarWithRoom: where an essentia golem should take its essentia. Jars (and those joined to them) are tried in TC4's order: labelled
	 * jars already holding it, empty labelled jars that accept it, full labelled void jars of it, unlabelled jars holding it, empty unlabelled
	 * jars, unlabelled void jars holding it, empty unlabelled void jars; marked reservoirs and tubes that want it are always candidates.
	 */
	public static @Nullable BlockPos findJarWithRoom(GolemEntity golem) {
		Level level = golem.level();
		float dmod = golem.getRange();
		dmod *= dmod;
		BlockPos home = golem.getHomePos();
		List<JarBlockEntity> jars = new ArrayList<>();
		List<BlockEntity> others = new ArrayList<>();
		for (GolemMarker m : golem.getMarkers()) {
			BlockEntity te = level.getBlockEntity(m.pos());
			if (te == null || te.getBlockPos().distSqr(home) > dmod) {
				continue;
			}
			if (te instanceof JarBlockEntity jar) {
				jars.add(jar);
			} else if (te instanceof ReservoirBlockEntity res) {
				if (res.getSuctionAmount(res.facing) > 0 && (res.getSuctionType(res.facing) == null || res.getSuctionType(res.facing) == golem.essentia)) {
					others.add(te);
				}
			} else if (te instanceof EssentiaTransport trans) {
				Direction d = Direction.from3DDataValue(m.side());
				if (golem.essentia != null && golem.essentiaAmount > 0 && trans.canInputFrom(d) && trans.getSuctionAmount(d) > 0
					&& (trans.getSuctionType(d) == null || trans.getSuctionType(d) == golem.essentia)) {
					others.add(te);
				}
			}
		}
		Map<BlockPos, JarBlockEntity> jarlist = new HashMap<>();
		if (!jars.isEmpty()) {
			for (JarBlockEntity jar : jars) {
				jarlist.put(jar.getBlockPos(), jar);
				getConnectedJars(jar, jarlist);
			}
		} else if (others.isEmpty()) {
			return null;
		}
		List<BlockEntity> found = new ArrayList<>(others);
		boolean has = golem.essentia != null && golem.essentiaAmount > 0;
		java.util.function.Predicate<JarBlockEntity> holdsIt = j -> has && j.aspect != null && j.aspect.equals(golem.essentia);
		List<java.util.function.Predicate<JarBlockEntity>> passes = List.of(
			j -> j.aspect != null && j.amount > 0 && j.amount < JarBlockEntity.MAX && j.aspectFilter != null && holdsIt.test(j),
			j -> (j.aspect == null || j.amount == 0) && j.aspectFilter != null,
			j -> j.aspect != null && j.amount >= JarBlockEntity.MAX && j.isVoid() && j.aspectFilter != null && holdsIt.test(j),
			j -> j.aspect != null && j.amount > 0 && j.amount < JarBlockEntity.MAX && j.aspectFilter == null && holdsIt.test(j),
			j -> (j.aspect == null || j.amount == 0) && j.aspectFilter == null && !j.isVoid(),
			j -> j.aspect != null && j.isVoid() && j.aspectFilter == null && holdsIt.test(j),
			j -> (j.aspect == null || j.amount == 0) && j.aspectFilter == null && j.isVoid());
		for (int i = 0; i < passes.size(); i++) {
			if (i > 0 && !found.isEmpty()) {
				break;
			}
			for (JarBlockEntity j : jarlist.values()) {
				if (passes.get(i).test(j) && j.doesContainerAccept(golem.essentia)) {
					found.add(j);
				}
			}
		}
		BlockPos dest = null;
		double dist = Double.MAX_VALUE;
		for (BlockEntity te : found) {
			double d = te.getBlockPos().distSqr(home);
			if (te instanceof JarBlockEntity j && j.isVoid()) {
				d += dmod;
			}
			if (d < dist) {
				dist = d;
				dest = te.getBlockPos();
			}
		}
		return dest;
	}

	private static void getConnectedJars(JarBlockEntity jar, Map<BlockPos, JarBlockEntity> jarlist) {
		for (Direction d : Direction.values()) {
			BlockPos p = jar.getBlockPos().relative(d);
			if (!jarlist.containsKey(p) && jar.getLevel().getBlockEntity(p) instanceof JarBlockEntity other) {
				jarlist.put(p, other);
				getConnectedJars(other, jarlist);
			}
		}
	}
}
