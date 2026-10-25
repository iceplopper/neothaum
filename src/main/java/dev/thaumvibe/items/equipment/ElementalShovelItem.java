package dev.thaumvibe.items.equipment;

import dev.thaumvibe.network.TVNetwork;
import dev.thaumvibe.registry.TVComponents;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * ItemElementalShovel, the Shovel of the Earthmover: digs 3x3 (sneak for one block); used on a block, it lays a 3x3 of that block against the
 * face you clicked, from your inventory, in the orientation set with the misc key.
 */
public class ElementalShovelItem extends Item {
	/** isEffective: what the shovel bites into besides anything it is the right tool for. */
	private static final Block[] IS_EFFECTIVE = {Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.SAND, Blocks.GRAVEL, Blocks.SNOW, Blocks.SNOW_BLOCK, Blocks.CLAY,
		Blocks.FARMLAND, Blocks.SOUL_SAND, Blocks.MYCELIUM};

	public ElementalShovelItem(Properties props) {
		super(props);
	}

	public static int getOrientation(ItemStack stack) {
		return stack.getOrDefault(TVComponents.ORIENTATION, 0);
	}

	public static void setOrientation(ItemStack stack, int o) {
		stack.set(TVComponents.ORIENTATION, o % 3);
	}

	private static boolean effective(ItemStack stack, BlockState state) {
		if (stack.isCorrectToolForDrops(state) || state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
			return true;
		}
		for (Block b : IS_EFFECTIVE) {
			if (state.is(b)) {
				return true;
			}
		}
		return false;
	}

	/** The nine offsets of the placement square for this face, orientation and facing (ItemElementalShovel.onItemUse). */
	static List<BlockPos> square(int o, Direction face, float yaw) {
		List<BlockPos> out = new ArrayList<>();
		int side = face.get3DDataValue();
		int l = Mth.floor(yaw * 4.0F / 360.0F + 0.5) & 3;
		for (int aa = -1; aa <= 1; aa++) {
			for (int bb = -1; bb <= 1; bb++) {
				int xx = 0, yy = 0, zz = 0;
				if (o == 1) {
					yy = bb;
					if (side <= 1) {
						if (l != 0 && l != 2) {
							zz = aa;
						} else {
							xx = aa;
						}
					} else if (side <= 3) {
						zz = aa;
					} else {
						xx = aa;
					}
				} else if (o == 2) {
					if (side <= 1) {
						yy = bb;
						if (l != 0 && l != 2) {
							zz = aa;
						} else {
							xx = aa;
						}
					} else {
						zz = bb;
						xx = aa;
					}
				} else if (side <= 1) {
					xx = aa;
					zz = bb;
				} else if (side <= 3) {
					xx = aa;
					yy = bb;
				} else {
					zz = aa;
					yy = bb;
				}
				out.add(new BlockPos(xx, yy, zz));
			}
		}
		return out;
	}

	private static boolean replaceable(Level level, BlockPos p) {
		BlockState s = level.getBlockState(p);
		return s.isAir() || s.is(Blocks.VINE) || s.is(Blocks.SHORT_GRASS) || s.is(Blocks.DEAD_BUSH) || s.canBeReplaced() || !s.getFluidState().isEmpty();
	}

	/** IArchitect.getArchitectBlocks: where a sneaking click would place blocks, for the preview. */
	public static List<BlockPos> architectBlocks(ItemStack stack, Level level, BlockPos pos, Direction face, Player player) {
		List<BlockPos> out = new ArrayList<>();
		for (BlockPos off : square(getOrientation(stack), face, player.getYRot())) {
			BlockPos p = pos.offset(off).relative(face);
			if (replaceable(level, p)) {
				out.add(p);
			}
		}
		return out;
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		Player player = ctx.getPlayer();
		BlockPos pos = ctx.getClickedPos();
		Direction face = ctx.getClickedFace();
		ItemStack stack = ctx.getItemInHand();
		if (player == null || level.getBlockEntity(pos) != null) {
			return InteractionResult.PASS;
		}
		BlockState bi = level.getBlockState(pos);
		boolean placed = false;
		for (BlockPos off : square(getOrientation(stack), face, player.getYRot())) {
			BlockPos p = pos.offset(off).relative(face);
			if (!replaceable(level, p)) {
				continue;
			}
			BlockState place = null;
			if (player.isCreative() || consume(player, new ItemStack(bi.getBlock()))) {
				place = bi;
			} else if (bi.is(Blocks.GRASS_BLOCK) && consume(player, new ItemStack(Blocks.DIRT))) {
				place = Blocks.DIRT.defaultBlockState();
			}
			if (place != null) {
				if (!level.isClientSide()) {
					level.playSound(null, p, place.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 0.6F, 0.9F + level.getRandom().nextFloat() * 0.2F);
					level.setBlockAndUpdate(p, place);
					stack.hurtAndBreak(1, player, ctx.getHand());
					TVNetwork.sendBlockSparkle(level, p, place == bi ? 8401408 : 3);
				}
				placed = true;
			}
		}
		return placed ? InteractionResult.SUCCESS : InteractionResult.PASS;
	}

	private static boolean consume(Player player, ItemStack want) {
		if (want.isEmpty()) {
			return false;
		}
		var inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack s = inv.getItem(i);
			if (ItemStack.isSameItem(s, want)) {
				if (!player.level().isClientSide()) {
					s.shrink(1);
				}
				return true;
			}
		}
		return false;
	}

	/** onBlockDestroyed: the eight neighbours in the plane of the face you dug into go too. */
	@Override
	public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
		boolean r = super.mineBlock(stack, level, state, pos, miner);
		if (miner.isShiftKeyDown() || level.isClientSide() || ToolHelper.isHarvesting() || !(miner instanceof Player player) || !effective(stack, state)) {
			return r;
		}
		HitResult hit = player.pick(player.blockInteractionRange(), 0.0F, false);
		int side = hit instanceof BlockHitResult bh ? bh.getDirection().get3DDataValue() : 1;
		for (int aa = -1; aa <= 1; aa++) {
			for (int bb = -1; bb <= 1; bb++) {
				if (aa == 0 && bb == 0) {
					continue;
				}
				BlockPos p = side <= 1 ? pos.offset(aa, 0, bb) : side <= 3 ? pos.offset(aa, bb, 0) : pos.offset(0, bb, aa);
				BlockState bl = level.getBlockState(p);
				if (bl.getDestroySpeed(level, p) >= 0.0F && effective(stack, bl)) {
					stack.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
					ToolHelper.harvestBlock(level, player, p, true, 3);
				}
			}
		}
		return r;
	}
}
