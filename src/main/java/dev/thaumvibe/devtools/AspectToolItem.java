package dev.thaumvibe.devtools;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.research.PlayerKnowledge;
import dev.thaumvibe.research.ResearchManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** Dev tool: use to discover every aspect and add 50 research points of each; sneak-use to empty every aspect pool. */
public class AspectToolItem extends Item {
	public AspectToolItem(Item.Properties props) {
		super(props);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!player.isCreative()) {
			return InteractionResult.FAIL;
		}
		if (player instanceof ServerPlayer sp) {
			PlayerKnowledge k = ResearchManager.knowledge(sp).copy();
			for (Aspect a : Aspect.aspects.values()) {
				k.addDiscoveredAspect(a);
				if (player.isShiftKeyDown()) {
					k.setAspectPool(a, 0);
				} else {
					k.addAspectPool(a, 50);
				}
			}
			ResearchManager.setKnowledge(sp, k);
			sp.sendSystemMessage(Component.literal(player.isShiftKeyDown() ? "[Dev] All aspect pools emptied"
				: "[Dev] All aspects discovered, +50 research points of each").withStyle(ChatFormatting.LIGHT_PURPLE));
		}
		return InteractionResult.SUCCESS;
	}
}
