package dev.neothaum.items.foci;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.api.wands.FocusItem;
import dev.neothaum.api.wands.FocusUpgradeType;
import dev.neothaum.fx.FX;
import dev.neothaum.items.WandItem;
import dev.neothaum.registry.TVSounds;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * ItemFocusExcavation, the Wand Focus: Excavation. A held green beam that bores through what it touches, fastest through stone and earth;
 * upgraded with treasure (fortune), enlarge (it eats into matching neighbours), dowsing or silk touch.
 */
public class ExcavationFocusItem extends FocusItem {
	private static final AspectList COST = new AspectList().add(Aspect.EARTH, 15);
	private static final AspectList COST_SILK = new AspectList().add(Aspect.AIR, 1).add(Aspect.FIRE, 1).add(Aspect.EARTH, 16).add(Aspect.WATER, 1)
		.add(Aspect.ORDER, 1).add(Aspect.ENTROPY, 1);
	private static final AspectList COST_DOWSING = new AspectList().add(Aspect.FIRE, 2).add(Aspect.ORDER, 2).add(Aspect.EARTH, 15);
	public static final FocusUpgradeType DOWSING = new FocusUpgradeType(20, "dowsing", new AspectList().add(Aspect.MINE, 1));

	private static final Map<String, Long> SOUND_DELAY = new ConcurrentHashMap<>();
	private static final Map<String, Object> BEAM = new ConcurrentHashMap<>();
	private static final Map<String, Float> BREAKCOUNT = new ConcurrentHashMap<>();
	private static final Map<String, BlockPos> LAST = new ConcurrentHashMap<>();
	private static final BlockPos NONE = new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

	public ExcavationFocusItem(Item.Properties props) {
		super(props);
	}

	@Override
	public String getSortingHelper(ItemStack focus) {
		return "BE" + super.getSortingHelper(focus);
	}

	@Override
	public int getFocusColor(ItemStack focus) {
		return 409606;
	}

	@Override
	public AspectList getVisCost(ItemStack focus) {
		// TC4 caches the first upgraded cost it builds; the silk touch and dowsing prices it would settle on are these
		if (this.isUpgradedWith(focus, FocusUpgradeType.silktouch)) {
			return COST_SILK;
		}
		return this.isUpgradedWith(focus, DOWSING) ? COST_DOWSING : COST;
	}

	@Override
	public boolean isVisCostPerTick(ItemStack focus) {
		return true;
	}

	@Override
	public Animation getAnimation(ItemStack focus) {
		return Animation.CHARGE;
	}

	@Override
	public boolean onFocusRightClick(ItemStack wand, Level level, Player p, HitResult hit) {
		p.startUsingItem(FireFocusItem.handOf(p, wand));
		return true;
	}

	private static String key(Player p) {
		return (p.level().isClientSide() ? "R" : "S") + p.getName().getString();
	}

	@Override
	public void onUsingFocusTick(ItemStack wand, Player p, int count) {
		ItemStack fs = WandItem.getFocusStack(wand);
		if (!WandItem.consumeAllVis(wand, p, this.getVisCost(fs), false, false)) {
			p.stopUsingItem();
			return;
		}
		Level level = p.level();
		String pp = key(p);
		BlockHitResult mop = FocusUtil.targetBlock(level, p, 10.0);
		Vec3 v = p.getLookAngle();
		double tx = p.getX() + v.x * 10.0;
		double ty = p.getY() + v.y * 10.0;
		double tz = p.getZ() + v.z * 10.0;
		int impact = 0;
		if (mop != null) {
			tx = mop.getLocation().x;
			ty = mop.getLocation().y;
			tz = mop.getLocation().z;
			impact = 5;
			if (!level.isClientSide() && SOUND_DELAY.getOrDefault(pp, 0L) < System.currentTimeMillis()) {
				level.playSound(null, tx, ty, tz, TVSounds.RUMBLE, SoundSource.PLAYERS, 0.3F, 1.0F);
				SOUND_DELAY.put(pp, System.currentTimeMillis() + 1200L);
			}
		} else {
			SOUND_DELAY.put(pp, 0L);
		}
		if (level.isClientSide()) {
			Object beam = FX.beamCont(p, tx, ty, tz, 2, 65382, false, impact > 0 ? 2.0F : 0.0F, BEAM.get(pp), impact);
			if (beam != null) {
				BEAM.put(pp, beam);
			}
		}
		if (mop != null && level.mayInteract(p, mop.getBlockPos())) {
			BlockPos pos = mop.getBlockPos();
			BlockState bs = level.getBlockState(pos);
			float hardness = bs.getDestroySpeed(level, pos);
			if (hardness >= 0.0F) {
				int pot = WandItem.getFocusPotency(wand);
				float speed = 0.05F + pot * 0.1F;
				// Material.rock, grass, ground and sand: what pickaxes and shovels are for
				if (bs.is(BlockTags.MINEABLE_WITH_PICKAXE) || bs.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
					speed = 0.25F + pot * 0.25F;
				}
				if (bs.is(Blocks.OBSIDIAN)) {
					speed *= 3.0F;
				}
				if (pos.equals(LAST.get(pp))) {
					float bc = BREAKCOUNT.getOrDefault(pp, 0.0F);
					if (level.isClientSide() && bc > 0.0F && !bs.isAir()) {
						FX.excavate(level, pos, p, (int) (bc / hardness * 9.0F));
					}
					if (level.isClientSide()) {
						BREAKCOUNT.put(pp, bc >= hardness ? 0.0F : bc + speed);
					} else if (bc >= hardness && WandItem.consumeAllVis(wand, p, this.getVisCost(fs), true, false)) {
						if (this.excavate((ServerLevel) level, wand, p, bs, pos)) {
							for (int a = 0; a < WandItem.getFocusEnlarge(wand); a++) {
								if (WandItem.consumeAllVis(wand, p, this.getVisCost(fs), false, false) && this.breakNeighbour(p, pos, bs, wand)) {
									WandItem.consumeAllVis(wand, p, this.getVisCost(fs), true, false);
								}
							}
						}
						LAST.put(pp, NONE);
						BREAKCOUNT.put(pp, 0.0F);
					} else {
						BREAKCOUNT.put(pp, bc + speed);
					}
				} else {
					LAST.put(pp, pos);
					BREAKCOUNT.put(pp, 0.0F);
				}
			}
		} else {
			LAST.put(pp, NONE);
			BREAKCOUNT.put(pp, 0.0F);
		}
	}

	private boolean excavate(ServerLevel level, ItemStack wand, Player player, BlockState state, BlockPos pos) {
		if (!(player instanceof ServerPlayer sp) || sp.blockActionRestricted(level, pos, sp.gameMode.getGameModeForPlayer())) {
			return false;
		}
		int fortune = WandItem.getFocusTreasure(wand);
		boolean silk = this.isUpgradedWith(WandItem.getFocusStack(wand), FocusUpgradeType.silktouch);
		// the drops come from a phantom pickaxe carrying the focus's fortune or silk touch
		ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
		HolderLookup.RegistryLookup<net.minecraft.world.item.enchantment.Enchantment> ench = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		if (silk) {
			tool.enchant(ench.getOrThrow(Enchantments.SILK_TOUCH), 1);
		} else if (fortune > 0) {
			tool.enchant(ench.getOrThrow(Enchantments.FORTUNE), fortune);
		}
		var be = level.getBlockEntity(pos);
		boolean dowsing = this.isUpgradedWith(WandItem.getFocusStack(wand), DOWSING);
		for (ItemStack drop : Block.getDrops(state, level, pos, be, player, tool)) {
			if (dowsing) {
				// EventHandlerWorld.harvestEvent: a dowsing beam sometimes turns ore into native clusters
				ItemStack smr = dev.neothaum.items.equipment.SpecialMining.find(drop, 0.2F + fortune * 0.075F, level.getRandom());
				if (smr.getItem() != drop.getItem()) {
					level.playSound(null, pos, net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.2F,
						0.7F + level.getRandom().nextFloat() * 0.2F);
				}
				drop = smr;
			}
			Block.popResource(level, pos, drop);
		}
		state.spawnAfterBreak(level, pos, tool, true);
		level.levelEvent(2001, pos, Block.getId(state));
		level.removeBlock(pos, false);
		return true;
	}

	private boolean breakNeighbour(Player p, BlockPos pos, BlockState state, ItemStack wand) {
		List<Direction> dirs = new ArrayList<>(List.of(Direction.values()));
		Collections.shuffle(dirs, new java.util.Random(p.getRandom().nextLong()));
		for (Direction dir : dirs) {
			BlockPos n = pos.relative(dir);
			if (p.level().getBlockState(n) == state && this.excavate((ServerLevel) p.level(), wand, p, state, n)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onPlayerStoppedUsingFocus(ItemStack wand, Level level, Player p, int count) {
		String pp = key(p);
		BEAM.remove(pp);
		LAST.put(pp, NONE);
		BREAKCOUNT.put(pp, 0.0F);
		if (level.isClientSide()) {
			FX.excavate(level, BlockPos.ZERO, p, -1);
		}
	}

	@Override
	public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
		return switch (rank) {
			case 1 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.treasure};
			case 2, 4 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.enlarge};
			case 3 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.treasure, DOWSING};
			case 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.treasure, FocusUpgradeType.silktouch};
			default -> null;
		};
	}
}
