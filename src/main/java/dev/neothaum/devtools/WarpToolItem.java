package dev.neothaum.devtools;

import dev.neothaum.research.PlayerKnowledge;
import dev.neothaum.research.ResearchManager;
import dev.neothaum.warp.Warp;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** Dev tool: use to gain 10 permanent warp; sneak-use to clear all warp (permanent, sticky and temporary). Reports the totals either way. */
public class WarpToolItem extends Item {
	public WarpToolItem(Item.Properties props) {
		super(props);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!player.isCreative()) {
			return InteractionResult.FAIL;
		}
		if (!level.isClientSide()) {
			if (player.isShiftKeyDown()) {
				PlayerKnowledge k = ResearchManager.knowledge(player).copy();
				k.warp = k.warpSticky = k.warpTemp = k.warpCounter = 0;
				ResearchManager.setKnowledge(player, k);
			} else {
				Warp.addWarpToPlayer(player, 10, false);
			}
			PlayerKnowledge k = ResearchManager.knowledge(player);
			player.sendSystemMessage(Component.literal("[Dev] Warp: " + k.warp + " permanent, " + k.warpSticky + " sticky, " + k.warpTemp + " temporary")
				.withStyle(ChatFormatting.LIGHT_PURPLE));
		}
		return InteractionResult.SUCCESS;
	}
}
