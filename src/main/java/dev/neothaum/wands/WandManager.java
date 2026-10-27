package dev.neothaum.wands;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.api.wands.WandCap;
import dev.neothaum.api.wands.WandRod;
import dev.neothaum.blocks.InfusionPillarBlock;
import dev.neothaum.items.VisDiscountGear;
import dev.neothaum.items.WandItem;
import dev.neothaum.network.TVNetwork;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVItems;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.research.ResearchManager;
import dev.neothaum.tiles.NodeBlockEntity;
import dev.neothaum.tiles.ArcaneWorktableBlockEntity;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/** Port of thaumcraft.common.items.wands.WandManager: wand triggers, vis discounts and the active "object in use". */
public final class WandManager {
	private static final Map<UUID, BlockPos> SERVER_IN_USE = new ConcurrentHashMap<>();
	private static final Map<UUID, BlockPos> CLIENT_IN_USE = new ConcurrentHashMap<>();
	private static BooleanSupplier shiftDown = () -> false;
	private static Supplier<Player> clientPlayer = () -> null;

	public static void init() {
		// ConfigItems.initializeWands: caps and rods with Thaumcraft 4's discounts, capacities and craft costs
		new WandCap("iron", 1.1F, () -> new ItemStack(TVItems.IRON_CAP), 1);
		new WandCap("gold", 1.0F, () -> new ItemStack(TVItems.GOLD_CAP), 3);
		new WandCap("thaumium", 0.9F, () -> new ItemStack(TVItems.THAUMIUM_CAP), 6);
		new WandCap("void", 0.8F, () -> new ItemStack(TVItems.VOID_CAP), 9);
		new WandCap("copper", 1.1F, List.of(Aspect.ORDER, Aspect.ENTROPY), 1.0F, () -> new ItemStack(TVItems.COPPER_CAP), 2);
		new WandCap("silver", 1.0F, List.of(Aspect.AIR, Aspect.EARTH, Aspect.FIRE, Aspect.WATER), 0.95F, () -> new ItemStack(TVItems.SILVER_CAP), 4);
		new WandRod("wood", 25, () -> new ItemStack(Items.STICK), 1);
		new WandRod("greatwood", 50, () -> new ItemStack(TVItems.GREATWOOD_ROD), 3);
		new WandRod("obsidian", 75, () -> new ItemStack(TVItems.OBSIDIAN_ROD), 6, new PrimalRodUpdate(Aspect.EARTH), false);
		new WandRod("blaze", 75, () -> new ItemStack(TVItems.BLAZING_ROD), 6, new PrimalRodUpdate(Aspect.FIRE), false).setGlowing(true);
		new WandRod("ice", 75, () -> new ItemStack(TVItems.ICY_ROD), 6, new PrimalRodUpdate(Aspect.WATER), false);
		new WandRod("quartz", 75, () -> new ItemStack(TVItems.QUARTZ_ROD), 6, new PrimalRodUpdate(Aspect.ORDER), false);
		new WandRod("bone", 75, () -> new ItemStack(TVItems.BONE_ROD), 6, new PrimalRodUpdate(Aspect.ENTROPY), false);
		new WandRod("reed", 75, () -> new ItemStack(TVItems.REED_ROD), 6, new PrimalRodUpdate(Aspect.AIR), false);
		new WandRod("silverwood", 100, () -> new ItemStack(TVItems.SILVERWOOD_ROD), 9);
		// StaffRods: the tag gains "_staff" and the texture stays the wand rod's
		new WandRod("greatwood", 125, () -> new ItemStack(TVItems.GREATWOOD_STAFF_CORE), 8, null, true);
		new WandRod("obsidian", 175, () -> new ItemStack(TVItems.OBSIDIAN_STAFF_CORE), 14, new PrimalRodUpdate(Aspect.EARTH), true);
		new WandRod("blaze", 175, () -> new ItemStack(TVItems.BLAZING_STAFF_CORE), 14, new PrimalRodUpdate(Aspect.FIRE), true).setGlowing(true);
		new WandRod("ice", 175, () -> new ItemStack(TVItems.ICY_STAFF_CORE), 14, new PrimalRodUpdate(Aspect.WATER), true);
		new WandRod("quartz", 175, () -> new ItemStack(TVItems.QUARTZ_STAFF_CORE), 14, new PrimalRodUpdate(Aspect.ORDER), true);
		new WandRod("bone", 175, () -> new ItemStack(TVItems.BONE_STAFF_CORE), 14, new PrimalRodUpdate(Aspect.ENTROPY), true);
		new WandRod("reed", 175, () -> new ItemStack(TVItems.REED_STAFF_CORE), 14, new PrimalRodUpdate(Aspect.AIR), true);
		new WandRod("silverwood", 250, () -> new ItemStack(TVItems.SILVERWOOD_STAFF_CORE), 24, null, true);
		new WandRod("primal", 250, () -> new ItemStack(TVItems.PRIMAL_STAFF_CORE), 32, new PrimalRodUpdate(), true).setRunes(true);
	}

	public static void setClientHooks(BooleanSupplier shift, Supplier<Player> player) {
		shiftDown = shift;
		clientPlayer = player;
	}

	public static boolean isShiftDown() {
		return shiftDown.getAsBoolean();
	}

	public static Player clientPlayer() {
		return clientPlayer.get();
	}

	private static Map<UUID, BlockPos> inUse(Player p) {
		return p.level().isClientSide() ? CLIENT_IN_USE : SERVER_IN_USE;
	}

	public static void setObjectInUse(Player p, BlockPos pos) {
		inUse(p).put(p.getUUID(), pos.immutable());
	}

	public static BlockPos getObjectInUse(Player p) {
		return inUse(p).get(p.getUUID());
	}

	public static void clearObjectInUse(Player p) {
		inUse(p).remove(p.getUUID());
	}

	/** Pays a centivis cost from an amulet of vis storage, else from the last wand in the inventory that can cover it. */
	public static boolean consumeVisFromInventory(Player player, AspectList cost) {
		for (ItemStack s : dev.neothaum.baubles.Baubles.get(player).items()) {
			if (s.getItem() instanceof dev.neothaum.items.baubles.VisAmuletItem amulet && amulet.consumeAllVis(s, player, cost, true, true)) {
				return true;
			}
		}
		var inv = player.getInventory();
		for (int a = 35; a >= 0; a--) {
			ItemStack item = inv.getItem(a);
			if (item.getItem() instanceof WandItem && WandItem.consumeAllVis(item, player, cost, true, true)) {
				return true;
			}
		}
		return false;
	}

	public static float getTotalVisDiscount(Player player, Aspect aspect) {
		if (player == null) {
			return 0.0F;
		}
		int total = 0;
		for (ItemStack s : dev.neothaum.baubles.Baubles.get(player).items()) {
			if (s.getItem() instanceof VisDiscountGear g) {
				total += g.getVisDiscount(s, player, aspect);
			}
		}
		for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
			ItemStack s = player.getItemBySlot(slot);
			if (s.getItem() instanceof VisDiscountGear g) {
				total += g.getVisDiscount(s, player, aspect);
			}
		}
		// Flux Flu and Flux Phage: 10% dearer per level
		var ve = player.getEffect(dev.neothaum.registry.TVMobEffects.VIS_EXHAUST);
		var ive = player.getEffect(dev.neothaum.registry.TVMobEffects.INFECTIOUS_VIS_EXHAUST);
		if (ve != null || ive != null) {
			int level1 = ve == null ? 0 : ve.getAmplifier();
			int level2 = ive == null ? 0 : ive.getAmplifier();
			total -= (Math.max(level1, level2) + 1) * 10;
		}
		return total / 100.0F;
	}

	/** WandTriggerRegistry + IWandable.onWandRightClick for the phase-1 blocks. */
	public static boolean performTrigger(Level level, ItemStack wand, Player player, BlockPos pos, Direction side, InteractionHand hand) {
		BlockState state = level.getBlockState(pos);
		if (state.is(Blocks.BOOKSHELF)) {
			return createThaumonomicon(wand, player, level, pos);
		}
		if (state.getBlock() instanceof AbstractCauldronBlock) {
			return createCrucible(wand, player, level, pos);
		}
		if (state.is(TVBlocks.TABLE)) {
			return createArcaneTable(wand, player, level, pos, hand);
		}
		if (state.is(TVBlocks.ARCANE_BORE_BASE)) {
			if (level.getBlockState(pos).getBlock() instanceof dev.neothaum.blocks.ArcaneBoreBaseBlock baseBlock) {
				baseBlock.rotateFacing(level, pos, side);
				return true;
			}
		}
		if (state.is(TVBlocks.ARCANE_BORE)) {
			if (level.getBlockState(pos).getBlock() instanceof dev.neothaum.blocks.ArcaneBoreBlock boreBlock) {
				boreBlock.rotateFacing(level, pos, side);
				return true;
			}
		}
		if (state.is(TVBlocks.FLUX_SCRUBBER)) {
			if (level.getBlockState(pos).getBlock() instanceof dev.neothaum.blocks.FluxScrubberBlock scrubberBlock) {
				scrubberBlock.rotateFacing(level, pos, side);
				return true;
			}
		}
		if (state.is(TVBlocks.NODE_IN_A_JAR)) {
			if (!level.isClientSide() && level.getBlockEntity(pos) instanceof dev.neothaum.tiles.JarNodeBlockEntity jar) {
				jar.release();
			}
			return true;
		}
		if (state.is(Blocks.IRON_BARS) && ResearchManager.isResearchComplete(player, "INFERNALFURNACE")) {
			return createArcaneFurnace(wand, player, level, pos);
		}
		if (state.is(Blocks.GLASS) && ResearchManager.isResearchComplete(player, "NODEJAR")) {
			return createNodeJar(wand, player, level, pos);
		}
		if (state.is(TVBlocks.ALCHEMICAL_CONSTRUCT) && ResearchManager.isResearchComplete(player, "THAUMATORIUM")) {
			return createThaumatorium(wand, player, level, pos, side);
		}
		if ((state.is(TVBlocks.ALCHEMICAL_FURNACE) || state.is(TVBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT)
			|| state.is(TVBlocks.ALCHEMICAL_CONSTRUCT) || state.is(TVBlocks.ARCANE_ALEMBIC))
			&& ResearchManager.isResearchComplete(player, "ADVALCHEMYFURNACE")) {
			if (createAdvancedAlchemicalFurnace(wand, player, level, pos)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Wand trigger: 3x3x2 Advanced Alchemical Furnace.
	 * Layer 0: Alchemical Furnace center, 8 Advanced Alchemical Constructs.
	 * Layer 1: Air center, 4 Alchemical Constructs (sides), 4 Arcane Alembics (corners).
	 * Cost: 50 Fire + 50 Water + 50 Order vis.
	 */
	public static boolean createAdvancedAlchemicalFurnace(ItemStack wand, Player player, Level level, BlockPos pos) {
		BlockPos corePos = null;
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 0; dy++) {
				for (int dz = -1; dz <= 1; dz++) {
					BlockPos p = pos.offset(dx, dy, dz);
					if (level.getBlockState(p).is(TVBlocks.ALCHEMICAL_FURNACE)) {
						corePos = p;
						break;
					}
				}
				if (corePos != null) break;
			}
			if (corePos != null) break;
		}
		if (corePos == null) {
			return false;
		}

		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				BlockPos p0 = corePos.offset(dx, 0, dz);
				BlockState s0 = level.getBlockState(p0);
				if (dx == 0 && dz == 0) {
					if (!s0.is(TVBlocks.ALCHEMICAL_FURNACE)) return false;
				} else {
					if (!s0.is(TVBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT)) return false;
				}
			}
		}

		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				BlockPos p1 = corePos.offset(dx, 1, dz);
				BlockState s1 = level.getBlockState(p1);
				if (dx == 0 && dz == 0) {
					if (!s1.isAir()) return false;
				} else if (dx == 0 || dz == 0) {
					if (!s1.is(TVBlocks.ALCHEMICAL_CONSTRUCT)) return false;
				} else {
					if (!s1.is(TVBlocks.ARCANE_ALEMBIC)) return false;
				}
			}
		}

		AspectList cost = new AspectList().add(Aspect.FIRE, 50).add(Aspect.WATER, 50).add(Aspect.ORDER, 50);
		if (!WandItem.consumeAllVisCrafting(wand, player, cost, true)) {
			return false;
		}

		if (!level.isClientSide()) {
			Block furnaceBlock = TVBlocks.ADVANCED_ALCHEMICAL_FURNACE;
			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					BlockPos p0 = corePos.offset(dx, 0, dz);
					if (dx == 0 && dz == 0) {
						level.setBlock(p0, furnaceBlock.defaultBlockState().setValue(dev.neothaum.blocks.AdvancedAlchemyFurnaceBlock.PART, dev.neothaum.blocks.AdvancedAlchemyFurnaceBlock.FurnacePart.CORE), 3);
					} else if (dx == 0 || dz == 0) {
						Direction dir = dx > 0 ? Direction.EAST : dx < 0 ? Direction.WEST : dz > 0 ? Direction.SOUTH : Direction.NORTH;
						level.setBlock(p0, furnaceBlock.defaultBlockState()
							.setValue(dev.neothaum.blocks.AdvancedAlchemyFurnaceBlock.PART, dev.neothaum.blocks.AdvancedAlchemyFurnaceBlock.FurnacePart.NOZZLE)
							.setValue(dev.neothaum.blocks.AdvancedAlchemyFurnaceBlock.FACING, dir), 3);
					} else {
						level.setBlock(p0, furnaceBlock.defaultBlockState().setValue(dev.neothaum.blocks.AdvancedAlchemyFurnaceBlock.PART, dev.neothaum.blocks.AdvancedAlchemyFurnaceBlock.FurnacePart.BOTTOM_CORNER), 3);
					}
					TVNetwork.sendBlockSparkle(level, p0, -9999);
				}
			}

			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					if (dx == 0 && dz == 0) continue;
					BlockPos p1 = corePos.offset(dx, 1, dz);
					if (dx == 0 || dz == 0) {
						Direction dir = dx > 0 ? Direction.EAST : dx < 0 ? Direction.WEST : dz > 0 ? Direction.SOUTH : Direction.NORTH;
						level.setBlock(p1, furnaceBlock.defaultBlockState()
							.setValue(dev.neothaum.blocks.AdvancedAlchemyFurnaceBlock.PART, dev.neothaum.blocks.AdvancedAlchemyFurnaceBlock.FurnacePart.TOP_SIDE)
							.setValue(dev.neothaum.blocks.AdvancedAlchemyFurnaceBlock.FACING, dir), 3);
					} else {
						level.setBlock(p1, furnaceBlock.defaultBlockState().setValue(dev.neothaum.blocks.AdvancedAlchemyFurnaceBlock.PART, dev.neothaum.blocks.AdvancedAlchemyFurnaceBlock.FurnacePart.TOP_CORNER), 3);
					}
					TVNetwork.sendBlockSparkle(level, p1, -9999);
				}
			}

			level.playSound(null, corePos, TVSounds.WAND, SoundSource.BLOCKS, 1.0F, 1.0F);
		}

		return true;
	}

	/** Wand trigger: 2 Alchemical Constructs over a Crucible becomes a Thaumatorium, for 15 Fire + 30 Order + 30 Water vis. */
	public static boolean createThaumatorium(ItemStack wand, Player player, Level level, BlockPos pos, Direction side) {
		BlockPos basePos = null;
		if (level.getBlockState(pos.above()).is(TVBlocks.ALCHEMICAL_CONSTRUCT)
			&& level.getBlockState(pos.below()).is(TVBlocks.CRUCIBLE)) {
			basePos = pos;
		} else if (level.getBlockState(pos.below()).is(TVBlocks.ALCHEMICAL_CONSTRUCT)
			&& level.getBlockState(pos.below(2)).is(TVBlocks.CRUCIBLE)) {
			basePos = pos.below();
		}
		if (basePos == null) {
			return false;
		}

		AspectList cost = new AspectList().add(Aspect.FIRE, 15).add(Aspect.ORDER, 30).add(Aspect.WATER, 30);
		if (!WandItem.consumeAllVisCrafting(wand, player, cost, true)) {
			return false;
		}

		if (!level.isClientSide()) {
			Direction facing = side.getAxis().isHorizontal() ? side : player.getDirection().getOpposite();
			level.setBlock(basePos, TVBlocks.THAUMATORIUM.defaultBlockState().setValue(dev.neothaum.blocks.ThaumatoriumBlock.FACING, facing), 3);
			level.setBlock(basePos.above(), TVBlocks.THAUMATORIUM_TOP.defaultBlockState(), 3);

			if (level.getBlockEntity(basePos) instanceof dev.neothaum.tiles.ThaumatoriumBlockEntity be) {
				be.setFacing(facing);
			}

			level.playSound(null, basePos, TVSounds.WAND, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
			if (level instanceof net.minecraft.server.level.ServerLevel sl) {
				sl.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANTED_HIT,
					basePos.getX() + 0.5, basePos.getY() + 1.0, basePos.getZ() + 0.5, 30, 0.4, 0.8, 0.4, 0.1);
			}
		}
		return true;
	}

	/** Wand trigger: 3x3x3 Nether Bricks, Obsidian, Lava and Iron Bars becomes an Infernal Furnace, for 50 Fire + 50 Earth vis. */
	public static boolean createArcaneFurnace(ItemStack wand, Player player, Level level, BlockPos pos) {
		AspectList cost = new AspectList().add(Aspect.FIRE, 50).add(Aspect.EARTH, 50);
		for (int xx = pos.getX() - 2; xx <= pos.getX(); xx++) {
			for (int yy = pos.getY() - 1; yy <= pos.getY() - 1; yy++) {
				for (int zz = pos.getZ() - 2; zz <= pos.getZ(); zz++) {
					BlockPos origin = new BlockPos(xx, yy, zz);
					Direction facing = fitArcaneFurnace(level, origin);
					if (facing != null && WandItem.consumeAllVisCrafting(wand, player, cost, true)) {
						if (!level.isClientSide()) {
							replaceArcaneFurnace(level, origin, facing);
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	private static Direction fitArcaneFurnace(Level level, BlockPos origin) {
		Block bo = Blocks.OBSIDIAN;
		Block bn = Blocks.NETHER_BRICKS;
		Block bf = Blocks.IRON_BARS;

		Direction foundFacing = null;
		int ironBarsCount = 0;

		for (int yy = 0; yy < 3; yy++) {
			for (int xx = 0; xx < 3; xx++) {
				for (int zz = 0; zz < 3; zz++) {
					BlockPos p = origin.offset(xx, yy, zz);
					BlockState s = level.getBlockState(p);
					boolean corner = (xx != 1 && zz != 1);
					boolean center = (xx == 1 && zz == 1);

					if (yy == 0) {
						if (corner) {
							if (!s.is(bn)) return null;
						} else {
							if (!s.is(bo)) return null;
						}
					} else if (yy == 1) {
						if (corner) {
							if (!s.is(bn)) return null;
						} else if (center) {
							if (!s.is(Blocks.LAVA)) return null;
						} else {
							if (s.is(bf)) {
								ironBarsCount++;
								if (xx == 1 && zz == 0) foundFacing = Direction.NORTH;
								else if (xx == 1 && zz == 2) foundFacing = Direction.SOUTH;
								else if (xx == 0 && zz == 1) foundFacing = Direction.WEST;
								else if (xx == 2 && zz == 1) foundFacing = Direction.EAST;
							} else if (!s.is(bo)) {
								return null;
							}
						}
					} else {
						if (corner) {
							if (!s.is(bn)) return null;
						} else if (center) {
							if (!s.isAir()) return null;
						} else {
							if (!s.is(bo)) return null;
						}
					}
				}
			}
		}
		return ironBarsCount == 1 ? foundFacing : null;
	}

	private static void replaceArcaneFurnace(Level level, BlockPos origin, Direction facing) {
		BlockPos mouthPos = origin.offset(1, 1, 1).relative(facing);
		for (int yy = 0; yy < 3; yy++) {
			for (int xx = 0; xx < 3; xx++) {
				for (int zz = 0; zz < 3; zz++) {
					if (yy == 2 && xx == 1 && zz == 1) {
						continue;
					}
					BlockPos p = origin.offset(xx, yy, zz);
					dev.neothaum.blocks.ArcaneFurnaceBlock.FurnacePart part;
					if (yy == 1 && xx == 1 && zz == 1) {
						part = dev.neothaum.blocks.ArcaneFurnaceBlock.FurnacePart.CORE;
					} else if (p.equals(mouthPos)) {
						part = dev.neothaum.blocks.ArcaneFurnaceBlock.FurnacePart.MOUTH;
					} else if (yy == 1 && (xx == 1 || zz == 1)) {
						part = dev.neothaum.blocks.ArcaneFurnaceBlock.FurnacePart.NOZZLE;
					} else {
						part = dev.neothaum.blocks.ArcaneFurnaceBlock.FurnacePart.CASING;
					}
					BlockState state = TVBlocks.ARCANE_FURNACE.defaultBlockState()
						.setValue(dev.neothaum.blocks.ArcaneFurnaceBlock.PART, part)
						.setValue(dev.neothaum.blocks.ArcaneFurnaceBlock.FACING, facing);
					level.setBlock(p, state, Block.UPDATE_ALL);
					TVNetwork.sendBlockSparkle(level, p, -9999);
				}
			}
		}
		level.playSound(null, origin.offset(1, 1, 1), TVSounds.WAND, SoundSource.BLOCKS, 1.0F, 1.0F);
	}

		/** Wand trigger 4: a node walled in by three layers of glass under a lid of wooden slabs is caught in a jar, for 70 of each primal vis. */
	public static boolean createNodeJar(ItemStack wand, Player player, Level level, BlockPos pos) {
		AspectList cost = new AspectList().add(Aspect.FIRE, 70).add(Aspect.EARTH, 70).add(Aspect.ORDER, 70).add(Aspect.AIR, 70)
			.add(Aspect.ENTROPY, 70).add(Aspect.WATER, 70);
		for (int xx = pos.getX() - 2; xx <= pos.getX(); xx++) {
			for (int yy = pos.getY() - 3; yy <= pos.getY(); yy++) {
				for (int zz = pos.getZ() - 2; zz <= pos.getZ(); zz++) {
					BlockPos o = new BlockPos(xx, yy, zz);
					if (fitNodeJar(level, o) && WandItem.consumeAllVisCrafting(wand, player, cost, true)) {
						if (!level.isClientSide()) {
							replaceNodeJar(level, o);
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	/** The blueprint, from the top layer down (y + 2 to y - 1): wooden slabs, then glass, then glass around the node, then glass. */
	private static boolean fitNodeJar(Level level, BlockPos o) {
		for (int yy = 0; yy < 4; yy++) {
			for (int xx = 0; xx < 3; xx++) {
				for (int zz = 0; zz < 3; zz++) {
					BlockPos p = o.offset(xx, 2 - yy, zz);
					BlockState s = level.getBlockState(p);
					if (yy == 0) {
						if (!s.is(net.minecraft.tags.BlockTags.WOODEN_SLABS)) {
							return false;
						}
					} else if (yy == 2 && xx == 1 && zz == 1) {
						if (!(level.getBlockEntity(p) instanceof NodeBlockEntity)) {
							return false;
						}
					} else if (!s.is(Blocks.GLASS)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/** The frame vanishes and the node shrinks into a jar; three times in four the capture dims it a step (bright to plain to pale to fading). */
	private static void replaceNodeJar(Level level, BlockPos o) {
		BlockPos np = o.offset(1, 0, 1);
		NodeBlockEntity node = (NodeBlockEntity) level.getBlockEntity(np);
		AspectList na = node.getAspects().copy();
		dev.neothaum.api.nodes.NodeType nt = node.getNodeType();
		dev.neothaum.api.nodes.NodeModifier nm = node.getNodeModifier();
		if (level.getRandom().nextFloat() < 0.75F) {
			if (nm == null) {
				nm = dev.neothaum.api.nodes.NodeModifier.PALE;
			} else if (nm == dev.neothaum.api.nodes.NodeModifier.BRIGHT) {
				nm = null;
			} else if (nm == dev.neothaum.api.nodes.NodeModifier.PALE) {
				nm = dev.neothaum.api.nodes.NodeModifier.FADING;
			}
		}
		for (int yy = 0; yy < 4; yy++) {
			for (int xx = 0; xx < 3; xx++) {
				for (int zz = 0; zz < 3; zz++) {
					BlockPos p = o.offset(xx, 2 - yy, zz);
					if (p.equals(np)) {
						level.removeBlockEntity(p);
						level.setBlock(p, TVBlocks.NODE_IN_A_JAR.defaultBlockState(), 3);
						if (level.getBlockEntity(p) instanceof dev.neothaum.tiles.JarNodeBlockEntity jar) {
							jar.setAspects(na);
							jar.setNodeModifier(nm);
							jar.setNodeType(nt);
							jar.markForUpdate();
						}
						level.blockEvent(p, TVBlocks.NODE_IN_A_JAR, 9, 0);
					} else {
						level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
					}
				}
			}
		}
		level.playSound(null, o.offset(1, 0, 1), TVSounds.WAND, SoundSource.BLOCKS, 1.0F, 1.0F);
	}

	public static boolean createThaumonomicon(ItemStack wand, Player player, Level level, BlockPos pos) {
		if (level.isClientSide()) {
			return true;
		}
		level.removeBlock(pos, false);
		ItemEntity e = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5, new ItemStack(TVItems.THAUMONOMICON));
		e.setDeltaMovement(0, 0, 0);
		e.setNoGravity(true);
		level.addFreshEntity(e);
		TVNetwork.sendBlockSparkle(level, pos, -9999);
		level.playSound(null, pos, TVSounds.WAND, SoundSource.BLOCKS, 1.0F, 1.0F);
		return true;
	}

	public static boolean createCrucible(ItemStack wand, Player player, Level level, BlockPos pos) {
		if (level.isClientSide()) {
			return true;
		}
		level.playSound(null, pos, TVSounds.WAND, SoundSource.BLOCKS, 1.0F, 1.0F);
		level.setBlock(pos, TVBlocks.CRUCIBLE.defaultBlockState(), 3);
		TVNetwork.sendBlockSparkle(level, pos, -9999);
		return true;
	}

	public static boolean createArcaneTable(ItemStack wand, Player player, Level level, BlockPos pos, InteractionHand hand) {
		if (level.isClientSide()) {
			return true;
		}
		level.setBlock(pos, TVBlocks.ARCANE_WORKTABLE.defaultBlockState(), 3);
		if (level.getBlockEntity(pos) instanceof ArcaneWorktableBlockEntity tawb && !WandItem.isStaff(wand)) {
			tawb.setItem(ArcaneWorktableBlockEntity.WAND_SLOT, wand.copy());
			player.setItemInHand(hand, ItemStack.EMPTY);
			tawb.markForUpdate();
		}
		level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.15F, 0.5F);
		return true;
	}

	/**
	 * Wand trigger 3: a runic matrix two above a pedestal, with arcane stone corner columns (bricks below, block above), becomes an infusion
	 * altar, for 25 of each primal vis.
	 */
	public static boolean createInfusionAltar(ItemStack wand, Player player, Level level, BlockPos pos) {
		if (!ResearchManager.isResearchComplete(player, "INFUSION")) {
			return false;
		}
		AspectList cost = new AspectList().add(Aspect.FIRE, 25).add(Aspect.EARTH, 25).add(Aspect.ORDER, 25).add(Aspect.AIR, 25)
			.add(Aspect.ENTROPY, 25).add(Aspect.WATER, 25);
		for (int xx = pos.getX() - 2; xx <= pos.getX(); xx++) {
			for (int yy = pos.getY() - 2; yy <= pos.getY(); yy++) {
				for (int zz = pos.getZ() - 2; zz <= pos.getZ(); zz++) {
					BlockPos o = new BlockPos(xx, yy, zz);
					if (fitInfusionAltar(level, o) && WandItem.consumeAllVisCrafting(wand, player, cost, true)) {
						if (!level.isClientSide()) {
							replaceInfusionAltar(level, o);
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	/** The 3x3x3 blueprint, from the top layer down: matrix; arcane stone blocks at the corners; arcane stone bricks at the corners, pedestal in the middle. */
	private static boolean fitInfusionAltar(Level level, BlockPos o) {
		for (int yy = 0; yy < 3; yy++) {
			for (int xx = 0; xx < 3; xx++) {
				for (int zz = 0; zz < 3; zz++) {
					BlockPos p = o.offset(xx, 2 - yy, zz);
					BlockState s = level.getBlockState(p);
					boolean corner = xx != 1 && zz != 1;
					boolean middle = xx == 1 && zz == 1;
					if (yy == 0 && middle) {
						if (!s.is(TVBlocks.RUNIC_MATRIX)) {
							return false;
						}
					} else if (yy == 1 && corner) {
						if (!s.is(TVBlocks.ARCANE_STONE_BLOCK)) {
							return false;
						}
					} else if (yy == 2 && corner) {
						if (!s.is(TVBlocks.ARCANE_STONE_BRICKS)) {
							return false;
						}
					} else if (yy == 2 && middle) {
						if (!(level.getBlockEntity(p) instanceof dev.neothaum.tiles.PedestalBlockEntity)) {
							return false;
						}
					} else if (!s.isAir()) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private static void replaceInfusionAltar(Level level, BlockPos o) {
		// orientation by corner: (0,0) 2, (0,2) 3, (2,0) 4, (2,2) 5
		int[][] corners = {{0, 0, 2}, {0, 2, 3}, {2, 0, 4}, {2, 2, 5}};
		for (int[] c : corners) {
			BlockPos lower = o.offset(c[0], 0, c[1]);
			BlockState pillar = TVBlocks.INFUSION_PILLAR.defaultBlockState().setValue(InfusionPillarBlock.CORNER, c[2]);
			level.setBlock(lower, pillar.setValue(InfusionPillarBlock.HALF, DoubleBlockHalf.LOWER), Block.UPDATE_CLIENTS);
			level.setBlock(lower.above(), pillar.setValue(InfusionPillarBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
			TVNetwork.sendBlockSparkle(level, lower, -9999);
			TVNetwork.sendBlockSparkle(level, lower.above(), -9999);
		}
		if (level.getBlockEntity(o.offset(1, 2, 1)) instanceof dev.neothaum.tiles.InfusionMatrixBlockEntity tis) {
			tis.active = true;
			tis.markForUpdate();
		}
		level.playSound(null, o, TVSounds.WAND, SoundSource.BLOCKS, 1.0F, 1.0F);
	}

	private WandManager() {
	}
}
