package dev.thaumvibe.items.foci;

import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.api.wands.Architect;
import dev.thaumvibe.api.wands.FocusItem;
import dev.thaumvibe.api.wands.FocusUpgradeType;
import dev.thaumvibe.blocks.WardedBlock;
import dev.thaumvibe.items.WandItem;
import dev.thaumvibe.network.TVNetwork;
import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.registry.TVSounds;
import dev.thaumvibe.tiles.WardedBlockEntity;
import dev.thaumvibe.wands.WandFoci;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * ItemFocusWarding, the Wand Focus: Warding. Seals a solid block (and, with the architect upgrade, the connected face around it) so that only
 * its warder can unseal it again.
 */
public class WardingFocusItem extends FocusItem implements Architect {
	private static final AspectList COST = new AspectList().add(Aspect.EARTH, 25).add(Aspect.ORDER, 25).add(Aspect.WATER, 10);
	private static final Map<String, Long> DELAY = new ConcurrentHashMap<>();
	private static final Identifier ORNAMENT = Thaumvibe.id("items/focus_warding_orn");
	private static final Identifier DEPTH = Thaumvibe.id("items/focus_warding_depth");

	public WardingFocusItem(Item.Properties props) {
		super(props);
	}

	@Override
	public String getSortingHelper(ItemStack focus) {
		return "BWA" + super.getSortingHelper(focus);
	}

	@Override
	public Identifier getOrnament(ItemStack focus) {
		return ORNAMENT;
	}

	@Override
	public Identifier getDepthLayer(ItemStack focus) {
		return DEPTH;
	}

	@Override
	public int getFocusColor(ItemStack focus) {
		return 16771535;
	}

	@Override
	public AspectList getVisCost(ItemStack focus) {
		return COST.copy();
	}

	/** TileWarded.owner: TC4 keys ownership by the hash of the player's name. */
	public static int ownerId(Player p) {
		return p.getName().getString().hashCode();
	}

	private static boolean solid(Level level, BlockPos pos) {
		BlockState s = level.getBlockState(pos);
		return s.isRedstoneConductor(level, pos) && s.isCollisionShapeFullBlock(level, pos);
	}

	@Override
	public boolean onFocusRightClick(ItemStack wand, Level level, Player player, HitResult hit) {
		player.swing(FireFocusItem.handOf(player, wand), net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
		if (level.isClientSide() || !(hit instanceof BlockHitResult mop) || hit.getType() != HitResult.Type.BLOCK) {
			return true;
		}
		BlockPos pos = mop.getBlockPos();
		String key = pos.toShortString() + ":" + level.dimension().identifier();
		if (DELAY.getOrDefault(key, 0L) > System.currentTimeMillis()) {
			return true;
		}
		DELAY.put(key, System.currentTimeMillis() + 500L);
		var tt = level.getBlockEntity(pos);
		if (tt == null && solid(level, pos)) {
			for (BlockPos c : this.getArchitectBlocks(wand, level, pos, mop.getDirection(), player)) {
				if (!WandItem.consumeAllVis(wand, player, this.getVisCost(WandItem.getFocusStack(wand)), true, false)) {
					break;
				}
				if (level.getBlockEntity(c) == null && solid(level, c)) {
					BlockState held = level.getBlockState(c);
					int light = held.getLightEmission();
					level.setBlock(c, TVBlocks.WARDED_BLOCK.defaultBlockState().setValue(WardedBlock.LIGHT, light), 3);
					if (level.getBlockEntity(c) instanceof WardedBlockEntity tw) {
						tw.held = held;
						tw.owner = ownerId(player);
						tw.markForUpdate();
						TVNetwork.sendBlockSparkle(level, c, 16556032);
					}
				}
			}
			level.playSound(null, pos, TVSounds.ZAP, SoundSource.PLAYERS, 0.25F, 1.0F);
		} else if (tt instanceof WardedBlockEntity tw && tw.owner == ownerId(player)) {
			for (BlockPos c : this.getArchitectBlocks(wand, level, pos, mop.getDirection(), player)) {
				if (level.getBlockEntity(c) instanceof WardedBlockEntity tw2 && tw2.owner == ownerId(player)) {
					level.setBlock(c, tw2.held, 3);
					TVNetwork.sendBlockSparkle(level, c, 16556032);
				}
			}
			level.playSound(null, pos, TVSounds.ZAP, SoundSource.PLAYERS, 0.25F, 1.0F);
		}
		return true;
	}

	@Override
	public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
		return switch (rank) {
			case 1 -> new FocusUpgradeType[]{FocusUpgradeType.frugal};
			case 2 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.architect};
			case 3, 4, 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge};
			default -> null;
		};
	}

	@Override
	public boolean canApplyUpgrade(ItemStack focus, Player player, FocusUpgradeType type, int rank) {
		return !type.equals(FocusUpgradeType.enlarge) || this.isUpgradedWith(focus, FocusUpgradeType.architect);
	}

	@Override
	public int getMaxAreaSize(ItemStack focus) {
		return 3 + this.getUpgradeLevel(focus, FocusUpgradeType.enlarge);
	}

	@Override
	public List<BlockPos> getArchitectBlocks(ItemStack wand, Level level, BlockPos pos, Direction side, Player player) {
		List<BlockPos> out = new ArrayList<>();
		boolean tiles = level.getBlockEntity(pos) instanceof WardedBlockEntity;
		int sx = 0;
		int sy = 0;
		int sz = 0;
		if (this.isUpgradedWith(WandItem.getFocusStack(wand), FocusUpgradeType.architect)) {
			sx = WandFoci.getAreaX(wand);
			sy = WandFoci.getAreaY(wand);
			sz = WandFoci.getAreaZ(wand);
		}
		Set<BlockPos> checked = new HashSet<>();
		if (side.getAxis() != Direction.Axis.Z) {
			this.checkNeighbours(level, pos, pos, side, sx, sy, sz, out, player, tiles, checked);
		} else {
			this.checkNeighbours(level, pos, pos, side, sz, sy, sx, out, player, tiles, checked);
		}
		return out;
	}

	private void checkNeighbours(Level level, BlockPos origin, BlockPos pos, Direction side, int sizeX, int sizeY, int sizeZ, List<BlockPos> list,
		Player player, boolean tiles, Set<BlockPos> checked) {
		if (!checked.add(pos)) {
			return;
		}
		int dx = Math.abs(pos.getX() - origin.getX());
		int dy = Math.abs(pos.getY() - origin.getY());
		int dz = Math.abs(pos.getZ() - origin.getZ());
		switch (side.getAxis()) {
			case Y -> {
				if (dx > sizeX || dz > sizeZ || dy > sizeY) {
					return;
				}
			}
			case Z -> {
				if (dx > sizeX || dy > sizeZ || dz > sizeY) {
					return;
				}
			}
			case X -> {
				if (dy > sizeX || dz > sizeZ || dx > sizeY) {
					return;
				}
			}
		}
		var tt = level.getBlockEntity(pos);
		if (tiles && !(tt instanceof WardedBlockEntity)) {
			return;
		}
		if (!tiles && !(tt == null && solid(level, pos))) {
			return;
		}
		if (tiles && ((WardedBlockEntity) tt).owner != ownerId(player)) {
			return;
		}
		if (level.isEmptyBlock(pos)) {
			return;
		}
		list.add(pos);
		for (Direction d : Direction.values()) {
			this.checkNeighbours(level, origin, pos.relative(d), side, sizeX, sizeY, sizeZ, list, player, tiles, checked);
		}
	}

	@Override
	public boolean showAxis(ItemStack wand, Level level, Player player, Direction side, Architect.Axis axis) {
		int dim = WandFoci.getAreaDim(wand);
		if (dim == 0) {
			return true;
		}
		return switch (side.getAxis()) {
			case Y -> axis == Axis.X && dim == 1 || axis == Axis.Z && dim == 2 || axis == Axis.Y && dim == 3;
			case Z -> axis == Axis.Y && dim == 1 || axis == Axis.X && dim == 2 || axis == Axis.Z && dim == 3;
			case X -> axis == Axis.Y && dim == 1 || axis == Axis.Z && dim == 2 || axis == Axis.X && dim == 3;
		};
	}
}
