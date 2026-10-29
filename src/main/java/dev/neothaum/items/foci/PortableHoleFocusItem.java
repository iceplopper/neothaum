package dev.neothaum.items.foci;

import dev.neothaum.NeoThaum;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.api.wands.FocusItem;
import dev.neothaum.api.wands.FocusUpgradeType;
import dev.neothaum.items.WandItem;
import dev.neothaum.network.TVNetwork;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.tiles.HoleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** ItemFocusPortableHole, the Wand Focus: Portable Hole. Opens a temporary tunnel straight through solid blocks, paying vis by its depth. */
public class PortableHoleFocusItem extends FocusItem {
	private static final AspectList COST = new AspectList().add(Aspect.ENTROPY, 10).add(Aspect.AIR, 10);
	/** ThaumcraftApi.portableHoleBlackList, as a block tag (for other mods); Config.portablehole_blacklist is the player's own list. */
	public static final TagKey<Block> BLACKLIST = TagKey.create(Registries.BLOCK, NeoThaum.id("portable_hole_blacklist"));
	private static final Identifier DEPTH = NeoThaum.id("items/focus_portablehole_depth");

	public PortableHoleFocusItem(Item.Properties props) {
		super(props);
	}

	@Override
	public String getSortingHelper(ItemStack focus) {
		return "BPH" + super.getSortingHelper(focus);
	}

	@Override
	public Identifier getDepthLayer(ItemStack focus) {
		return DEPTH;
	}

	@Override
	public int getFocusColor(ItemStack focus) {
		return 594985;
	}

	@Override
	public AspectList getVisCost(ItemStack focus) {
		return COST.copy();
	}

	private static boolean blocks(Level level, BlockPos pos, BlockState bs) {
		return bs.is(BLACKLIST) || dev.neothaum.config.NeoThaumConfig.portableHoleBlacklist().contains(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(bs.getBlock()))
			|| bs.is(Blocks.BEDROCK) || bs.is(TVBlocks.EMPTY_SPACE) || bs.isAir() || bs.getDestroySpeed(level, pos) == -1.0F;
	}

	/** ItemFocusPortableHole.createHole: swap a block for empty space that remembers it; {@code side} is the bore's face (or -1 for a ring). */
	public static boolean createHole(Level level, BlockPos pos, int side, byte count, int max) {
		BlockState bs = level.getBlockState(pos);
		if (level.getBlockEntity(pos) != null || blocks(level, pos, bs) || bs.canBeReplaced()) {
			return false;
		}
		level.setBlock(pos, TVBlocks.EMPTY_SPACE.defaultBlockState(), 2);
		if (level.getBlockEntity(pos) instanceof HoleBlockEntity hole) {
			hole.oldState = bs;
			hole.countdownmax = (short) max;
			hole.count = count;
			hole.direction = (byte) side;
			hole.setChanged();
		}
		TVNetwork.sendBlockSparkle(level, pos, 4194368);
		return true;
	}

	@Override
	public boolean onFocusRightClick(ItemStack wand, Level level, Player player, HitResult hit) {
		if (!(hit instanceof BlockHitResult mop) || hit.getType() != HitResult.Type.BLOCK) {
			return false;
		}
		BlockPos pos = mop.getBlockPos();
		if (level.dimension().equals(dev.neothaum.world.outer.OuterLands.KEY)) {
			// the Outer Lands won't be holed
			if (!level.isClientSide()) {
				level.playSound(null, pos, dev.neothaum.registry.TVSounds.WANDFAIL, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
			}
			return true;
		}
		Direction side = mop.getDirection();
		int enlarge = WandItem.getFocusEnlarge(wand);
		int maxdis = 33 + enlarge * 8;
		int distance;
		BlockPos at = pos;
		for (distance = 0; distance < maxdis; distance++) {
			if (blocks(level, at, level.getBlockState(at))) {
				break;
			}
			at = at.relative(side.getOpposite());
		}
		AspectList c = this.getVisCost(WandItem.getFocusStack(wand));
		for (Aspect a : c.getAspects()) {
			c.merge(a, c.getAmount(a) * distance);
		}
		if (WandItem.consumeAllVis(wand, player, c, true, false) && !level.isClientSide()) {
			int di = this.getUpgradeLevel(WandItem.getFocusStack(wand), FocusUpgradeType.extend);
			createHole(level, pos, side.get3DDataValue(), (byte) (distance + 1), 120 + 60 * di);
		}
		player.swing(FireFocusItem.handOf(player, wand), net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
		if (!level.isClientSide()) {
			level.playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
		}
		return true;
	}

	@Override
	public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
		return rank >= 1 && rank <= 5 ? new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge, FocusUpgradeType.extend} : null;
	}
}
