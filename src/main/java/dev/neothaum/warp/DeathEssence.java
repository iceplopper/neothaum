package dev.neothaum.warp;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.config.NeoThaumConfig;
import dev.neothaum.network.TVNetwork;
import dev.neothaum.research.PlayerKnowledge;
import dev.neothaum.research.ResearchManager;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.gamerules.GameRules;

/**
 * Not in TC4: with keepInventory on, death still takes something. The returning player gains a little warp (1 sticky, 2 temporary; none in
 * wuss mode) and loses 2 to 6 points of each of 1 to 3 non-primal aspects they have discovered and hold points in, told in a message only
 * they see, the losses in its tooltip. With no such aspects there is no message.
 */
public final class DeathEssence {
	public static void init() {
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, player, alive) -> {
			if (!alive && player.level().getGameRules().get(GameRules.KEEP_INVENTORY)) {
				take(player);
			}
		});
	}

	public static void take(ServerPlayer player) {
		var rand = player.getRandom();
		if (NeoThaumConfig.current.allowWarp) {
			Warp.addStickyWarpToPlayer(player, 1);
			Warp.addWarpToPlayer(player, 2, true);
		}
		PlayerKnowledge k = ResearchManager.knowledge(player).copy();
		List<Aspect> held = new ArrayList<>();
		for (Aspect a : Aspect.aspects.values()) {
			if (!a.isPrimal() && k.hasDiscoveredAspect(a) && k.getAspectPool(a) > 0) {
				held.add(a);
			}
		}
		if (held.isEmpty()) {
			return;
		}
		int kinds = Math.min(held.size(), 1 + rand.nextInt(3));
		MutableComponent lost = Component.empty();
		for (int i = 0; i < kinds; i++) {
			Aspect a = held.remove(rand.nextInt(held.size()));
			int n = Math.min(k.getAspectPool(a), 2 + rand.nextInt(5));
			k.addAspectPool(a, -n);
			TVNetwork.sendAspectPool(player, a, -n, k.getAspectPool(a));
			if (i > 0) {
				lost.append("\n");
			}
			lost.append(Component.literal("-" + n + " " + a.getName()).withColor(a.getColor()));
		}
		ResearchManager.setKnowledge(player, k);
		player.sendSystemMessage(Component.translatable("neothaum.death.essence").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)
			.withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(lost))));
	}

	private DeathEssence() {
	}
}
