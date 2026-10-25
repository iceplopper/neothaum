package dev.thaumvibe.devtools;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.research.PlayerKnowledge;
import dev.thaumvibe.research.ResearchItem;
import dev.thaumvibe.research.ResearchManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** Dev tool: use to complete every loaded research entry and discover every aspect; sneak-use to reset to a brand new player. */
public class ResearchToolItem extends Item {
	public ResearchToolItem(Item.Properties props) {
		super(props);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!player.isCreative()) {
			return InteractionResult.FAIL;
		}
		if (player instanceof ServerPlayer sp) {
			PlayerKnowledge k;
			if (player.isShiftKeyDown()) {
				k = PlayerKnowledge.empty();
				ResearchManager.setKnowledge(sp, k);
				ResearchManager.grantStartingKnowledge(sp);
				sp.sendSystemMessage(Component.literal("[Dev] Research reset to a new player's").withStyle(ChatFormatting.LIGHT_PURPLE));
			} else {
				k = ResearchManager.knowledge(sp).copy();
				for (ResearchItem r : ResearchManager.entries()) {
					k.research.add(r.key);
				}
				for (Aspect a : Aspect.aspects.values()) {
					k.addDiscoveredAspect(a);
				}
				ResearchManager.setKnowledge(sp, k);
				sp.sendSystemMessage(Component.literal("[Dev] All " + ResearchManager.entries().size() + " research entries complete")
					.withStyle(ChatFormatting.LIGHT_PURPLE));
			}
		}
		return InteractionResult.SUCCESS;
	}
}
