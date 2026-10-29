package dev.neothaum.items.equipment;

import dev.neothaum.blocks.TCSaplingBlock;
import dev.neothaum.network.TVNetwork;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * ItemElementalHoe, the Hoe of Growth: tills 3x3 (sneak for one); where there is nothing to till it acts as bone meal, and it can force a
 * greatwood (as a full 2x2 square) or silverwood sapling to grow at once, at a heavy cost in durability.
 */
public class ElementalHoeItem extends Item {
	public ElementalHoeItem(Properties props) {
		super(props);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Player player = ctx.getPlayer();
		if (player == null || player.isShiftKeyDown()) {
			return super.useOn(ctx);
		}
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		ItemStack stack = ctx.getItemInHand();
		boolean did = false;
		for (int xx = -1; xx <= 1; xx++) {
			for (int zz = -1; zz <= 1; zz++) {
				BlockPos p = pos.offset(xx, 0, zz);
				Vec3 hit = ctx.getClickLocation().add(xx, 0, zz);
				UseOnContext sub = new UseOnContext(level, player, ctx.getHand(), stack, new BlockHitResult(hit, ctx.getClickedFace(), p, false));
				if (super.useOn(sub).consumesAction()) {
					TVNetwork.sendBlockSparkle(level, p, 8401408);
					did = true;
				}
			}
		}
		if (did) {
			return InteractionResult.SUCCESS;
		}
		BlockState bi = level.getBlockState(pos);
		// TC4's saplings took no bone meal, so the hoe always forced them; NeoThaum's do, so check for one before trying bone meal
		if (bi.getBlock() instanceof TCSaplingBlock sapling) {
			// a lone greatwood sapling cannot grow (NeoThaum's 2x2 rule), so it costs nothing
			boolean silver = bi.is(TVBlocks.SILVERWOOD_SAPLING);
			int cost = silver ? 25 : 5;
			int needed = silver ? 150 : 20;
			if (sapling.canGrow(level, pos) && stack.getDamageValue() + needed <= stack.getMaxDamage()) {
				if (level instanceof ServerLevel sl) {
					sapling.grow(sl, pos, sl.getRandom());
					stack.hurtAndBreak(cost, player, ctx.getHand());
					TVNetwork.sendBlockSparkle(level, pos, 0);
				}
				did = true;
			}
		} else if (BoneMealItem.growCrop(stack.copy(), level, pos)) {
			// Utils.useBonemealAtLoc: the hoe stands in for the bone meal
			if (!level.isClientSide()) {
				stack.hurtAndBreak(1, player, ctx.getHand());
				TVNetwork.sendBlockSparkle(level, pos, 0);
			}
			did = true;
		}
		if (did) {
			level.playSound(null, pos, TVSounds.WAND, SoundSource.BLOCKS, 0.75F, 0.9F + level.getRandom().nextFloat() * 0.2F);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}
}
