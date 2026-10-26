package dev.thaumvibe.items.equipment;

import dev.thaumvibe.api.WarpingGear;
import dev.thaumvibe.registry.TVBlocks;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * ItemPrimalCrusher: a pickaxe and shovel in one that digs 3x3 in the plane of the face you dig (sneak for one block). It mends itself like
 * void metal and carries warp 2.
 */
public class PrimalCrusherItem extends Item implements WarpingGear {
	/** isEffective: blocks it bites into besides anything a pickaxe or shovel is the right tool for. */
	private static final Block[] IS_EFFECTIVE = {Blocks.COBBLESTONE, Blocks.STONE, Blocks.STONE_SLAB, Blocks.SANDSTONE, Blocks.MOSSY_COBBLESTONE,
		Blocks.ICE, Blocks.NETHERRACK, Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.SAND, Blocks.GRAVEL, Blocks.SNOW, Blocks.SNOW_BLOCK, Blocks.CLAY,
		Blocks.FARMLAND, Blocks.SOUL_SAND, Blocks.MYCELIUM, Blocks.OBSIDIAN};

	public PrimalCrusherItem(Item.Properties props) {
		super(props);
	}

	private static boolean effective(ItemStack stack, BlockState state) {
		if (stack.isCorrectToolForDrops(state) || state.is(TVBlocks.CRUSTED_TAINT) || state.is(TVBlocks.TAINTED_SOIL) || state.is(TVBlocks.TAINT_FIBRES)) {
			return true;
		}
		for (Block b : IS_EFFECTIVE) {
			if (state.is(b)) {
				return true;
			}
		}
		return false;
	}

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
					ToolHelper.harvestBlock(level, player, p, true, 2);
				}
			}
		}
		return r;
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, EquipmentSlot slot) {
		VoidGear.selfRepair(stack, owner);
	}

	@Override
	public int getWarp(ItemStack stack, Player player) {
		return 2;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		VoidGear.warpTooltip(2, builder);
	}
}
