package dev.thaumvibe.items;

import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.registry.TVItems;
import dev.thaumvibe.registry.TVSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Port of ItemBathSalts:
 * Tossed or used on water, it dissolves into a silvery, purifying liquid that cures warp stresses.
 */
public class BathSaltsItem extends Item {
	public BathSaltsItem(Properties props) {
		super(props);
	}

	public static void init() {
		net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (net.minecraft.server.level.ServerLevel world : server.getAllLevels()) {
				if (world.getGameTime() % 10 != 0) {
					continue;
				}
				for (Entity entity : world.getAllEntities()) {
					if (entity instanceof ItemEntity itemEntity && itemEntity.getItem().is(TVItems.PURIFYING_BATH_SALTS) && itemEntity.isInWater()) {
						BlockPos pos = itemEntity.blockPosition();
						if (world.getBlockState(pos).is(Blocks.WATER)) {
							world.setBlock(pos, TVBlocks.PURIFYING_FLUID.defaultBlockState(), 3);
							world.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F);
							itemEntity.getItem().shrink(1);
							if (itemEntity.getItem().isEmpty()) {
								itemEntity.discard();
							}
						}
					}
				}
			}
		});
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockState state = level.getBlockState(pos);
		if (state.is(Blocks.WATER)) {
			return transformWater(level, pos, ctx.getPlayer(), ctx.getItemInHand());
		}
		BlockPos offset = pos.relative(ctx.getClickedFace());
		if (level.getBlockState(offset).is(Blocks.WATER)) {
			return transformWater(level, offset, ctx.getPlayer(), ctx.getItemInHand());
		}
		return super.useOn(ctx);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
		if (hit.getType() == HitResult.Type.BLOCK) {
			BlockPos pos = hit.getBlockPos();
			if (level.getBlockState(pos).is(Blocks.WATER)) {
				transformWater(level, pos, player, stack);
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}

	public static InteractionResult transformWater(Level level, BlockPos pos, Player player, ItemStack stack) {
		if (!level.isClientSide()) {
			level.setBlock(pos, TVBlocks.PURIFYING_FLUID.defaultBlockState(), 3);
		}
		level.playSound(player, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F);
		if (player != null && !player.getAbilities().instabuild) {
			stack.shrink(1);
		}
		return InteractionResult.SUCCESS;
	}
}
