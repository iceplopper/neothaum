package dev.thaumvibe.items;

import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.registry.TVMobEffects;
import dev.thaumvibe.registry.TVSounds;
import dev.thaumvibe.research.PlayerKnowledge;
import dev.thaumvibe.research.ResearchManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

/**
 * Port of ItemSanitySoap:
 * Cleanse the body and mind: removes all temporary warp, and has a chance
 * (boosted by Warp Ward and Purifying Fluid) to remove sticky warp.
 */
public class SanitizingSoapItem extends Item {
	public SanitizingSoapItem(Properties props) {
		super(props.stacksTo(16));
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 200;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.BRUSH;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		player.startUsingItem(hand);
		return InteractionResult.CONSUME;
	}

	@Override
	public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
		int ticks = getUseDuration(stack, entity) - count;
		if (ticks > 195 && entity instanceof Player player) {
			player.stopUsingItem();
		}
		if (level.isClientSide()) {
			if (level.getRandom().nextFloat() < 0.2F) {
				level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), TVSounds.ROOTS, SoundSource.PLAYERS, 0.1F, 1.5F + level.getRandom().nextFloat() * 0.2F, false);
			}
			for (int a = 0; a < 3; a++) {
				double x = entity.getX() + (level.getRandom().nextDouble() - 0.5) * 0.8;
				double y = entity.getY() + level.getRandom().nextDouble() * entity.getBbHeight();
				double z = entity.getZ() + (level.getRandom().nextDouble() - 0.5) * 0.8;
				level.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, 0, 0.05, 0);
			}
		}
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		if (entity instanceof Player player) {
			if (!player.getAbilities().instabuild) {
				stack.shrink(1);
			}
			if (!level.isClientSide()) {
				float chance = 0.33F;
				if (player.hasEffect(TVMobEffects.WARP_WARD)) {
					chance += 0.25F;
				}
				BlockPos ppos = player.blockPosition();
				if (level.getBlockState(ppos).is(TVBlocks.PURIFYING_FLUID) || level.getBlockState(ppos.below()).is(TVBlocks.PURIFYING_FLUID)) {
					chance += 0.25F;
				}
				PlayerKnowledge k = ResearchManager.knowledge(player).copy();
				boolean changed = false;
				if (k.warpTemp > 0) {
					k.warpTemp = 0;
					changed = true;
				}
				if (level.getRandom().nextFloat() < chance && k.warpSticky > 0) {
					k.warpSticky = Math.max(0, k.warpSticky - 1);
					changed = true;
				}
				if (changed) {
					ResearchManager.setKnowledge(player, k);
				}
			} else {
				level.playLocalSound(player.getX(), player.getY(), player.getZ(), TVSounds.CRAFTSTART, SoundSource.PLAYERS, 0.25F, 1.0F, false);
				for (int a = 0; a < 20; a++) {
					double x = player.getX() + (level.getRandom().nextDouble() - 0.5) * 1.5;
					double y = player.getY() + level.getRandom().nextDouble() * player.getBbHeight();
					double z = player.getZ() + (level.getRandom().nextDouble() - 0.5) * 1.5;
					level.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, 0, 0.1, 0);
				}
			}
		}
		return stack;
	}
}
