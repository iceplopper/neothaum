package dev.thaumvibe.warp;

import dev.thaumvibe.network.TVNetwork;
import dev.thaumvibe.research.PlayerKnowledge;
import dev.thaumvibe.research.ResearchManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Thaumcraft.addWarpToPlayer / addStickyWarpToPlayer: warp lives in the player's knowledge and every change is announced. */
public final class Warp {
	public static int total(Player player) {
		PlayerKnowledge k = ResearchManager.knowledge(player);
		return k.warp + k.warpSticky + k.warpTemp;
	}

	public static void addWarpToPlayer(Player player, int amount, boolean temporary) {
		if (player.level().isClientSide() || amount == 0 || !temporary && amount < 0) {
			return;
		}
		PlayerKnowledge k = ResearchManager.knowledge(player).copy();
		if (temporary) {
			if (amount < 0 && k.warpTemp <= 0) {
				return;
			}
			k.warpTemp = Math.max(0, k.warpTemp + amount);
		} else {
			k.warp = Math.max(0, k.warp + amount);
		}
		k.warpCounter = k.warp + k.warpSticky + k.warpTemp;
		ResearchManager.setKnowledge(player, k);
		if (player instanceof ServerPlayer sp) {
			ServerPlayNetworking.send(sp, new TVNetwork.WarpMessage(temporary ? 2 : 0, amount));
		}
	}

	public static void addStickyWarpToPlayer(Player player, int amount) {
		if (player.level().isClientSide() || amount == 0) {
			return;
		}
		PlayerKnowledge k = ResearchManager.knowledge(player).copy();
		if (amount < 0 && k.warpSticky <= 0) {
			return;
		}
		k.warpSticky = Math.max(0, k.warpSticky + amount);
		k.warpCounter = k.warp + k.warpSticky + k.warpTemp;
		ResearchManager.setKnowledge(player, k);
		if (player instanceof ServerPlayer sp) {
			ServerPlayNetworking.send(sp, new TVNetwork.WarpMessage(1, amount));
		}
	}

	private Warp() {
	}
}
