package dev.neothaum.addon;

import dev.neothaum.config.NeoThaumConfig;
import dev.neothaum.golem.GolemBellItem;
import dev.neothaum.golem.GolemEntity;
import dev.neothaum.golem.GolemMarker;
import dev.neothaum.registry.TVComponents;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * NeoThaum Addition: copying golem markers. Holding a golemancer's bell in each hand, sneak and use the main-hand bell in the air: its markers
 * replace the off-hand bell's, and the off-hand bell's golem takes them too.
 */
public final class MarkerCopy {
	static void init() {
		UseItemCallback.EVENT.register((player, level, hand) -> {
			if (!Additions.enabled("marker_copy") || hand != InteractionHand.MAIN_HAND || !player.isShiftKeyDown()) {
				return InteractionResult.PASS;
			}
			ItemStack from = player.getMainHandItem();
			ItemStack to = player.getOffhandItem();
			if (!(from.getItem() instanceof GolemBellItem) || !(to.getItem() instanceof GolemBellItem)) {
				return InteractionResult.PASS;
			}
			if (!level.isClientSide()) {
				copy(player, from, to);
			}
			return InteractionResult.SUCCESS;
		});
	}

	/** Copies the markers; returns how many were copied. */
	public static int copy(Player player, ItemStack from, ItemStack to) {
		List<GolemMarker> markers = new ArrayList<>(GolemBellItem.getMarkers(from));
		to.set(TVComponents.GOLEM_MARKERS, markers);
		int gid = GolemBellItem.getGolemId(to);
		if (gid > -1 && player.level().getEntity(gid) instanceof GolemEntity golem) {
			golem.setMarkers(markers);
		}
		player.level().playSound(null, player, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7F, 1.2F);
		player.sendSystemMessage(Component.translatable("neothaum.addition.markers_copied", markers.size()));
		return markers.size();
	}

	private MarkerCopy() {
	}
}
