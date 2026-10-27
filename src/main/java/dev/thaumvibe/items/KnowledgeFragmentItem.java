package dev.thaumvibe.items;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.network.TVNetwork;
import dev.thaumvibe.research.PlayerKnowledge;
import dev.thaumvibe.research.ResearchManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Port of TC4 Knowledge Fragment:
 * Right-clicking consumes 1 fragment and awards 1-2 points in all 6 primal aspects
 * to the player's research pool.
 * Also can be crafted 3x3 into Research Notes.
 */
public class KnowledgeFragmentItem extends Item {
	public KnowledgeFragmentItem(Properties props) {
		super(props);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (player instanceof ServerPlayer sp) {
			PlayerKnowledge k = ResearchManager.knowledge(sp).copy();
			for (Aspect a : Aspect.getPrimalAspects()) {
				int q = level.getRandom().nextInt(2) + 1; // 1 or 2 points
				k.addAspectPool(a, q);
				TVNetwork.sendAspectPool(sp, a, q, k.getAspectPool(a));
			}
			ResearchManager.setKnowledge(sp, k);
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5F, 1.2F + level.getRandom().nextFloat() * 0.2F);
		}
		player.awardStat(Stats.ITEM_USED.get(this));
		stack.consume(1, player);
		return InteractionResult.SUCCESS;
	}
}
