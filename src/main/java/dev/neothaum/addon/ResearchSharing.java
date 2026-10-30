package dev.neothaum.addon;

import dev.neothaum.config.NeoThaumConfig;
import dev.neothaum.network.TVNetwork;
import dev.neothaum.registry.TVItems;
import dev.neothaum.research.ResearchItem;
import dev.neothaum.research.ResearchManager;
import dev.neothaum.warp.Warp;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

/**
 * NeoThaum Addition (Shared Understanding): with the RESEARCHSHARING research, sneak and use a Thaumonomicon on another player to teach them
 * every research you know and they don't, as far as they know what it builds on. Research that gives warp can't be taught. Each lesson costs
 * the teacher one permanent and three sticky warp (none in wuss mode).
 */
public final class ResearchSharing {
	public static final String RESEARCH = "RESEARCHSHARING";
	public static final int PERMANENT_WARP = 1;
	public static final int STICKY_WARP = 3;

	static void init() {
		UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
			if (!NeoThaumConfig.current.additionResearchSharing || !player.isShiftKeyDown() || !(entity instanceof Player student)
				|| !player.getItemInHand(hand).is(TVItems.THAUMONOMICON) || !ResearchManager.isResearchComplete(player, RESEARCH)) {
				return InteractionResult.PASS;
			}
			if (player instanceof ServerPlayer teacher && student instanceof ServerPlayer target) {
				share(teacher, target);
			}
			return InteractionResult.SUCCESS;
		});
	}

	/** Teaches what can be taught; returns how many research were passed on. */
	public static int share(ServerPlayer teacher, ServerPlayer student) {
		int taught = 0;
		// round after round, since one lesson can open the way to the next
		boolean progress = true;
		while (progress) {
			progress = false;
			for (ResearchItem ri : ResearchManager.entries()) {
				if (ri.warp > 0 || ri.isVirtual() || !ResearchManager.isResearchComplete(teacher, ri.key)
					|| ResearchManager.isResearchComplete(student, ri.key) || !ResearchManager.doesPlayerHaveRequisites(student, ri.key)) {
					continue;
				}
				if (ResearchManager.completeResearch(student, ri.key)) {
					TVNetwork.sendResearchComplete(student, ri.key);
					taught++;
					progress = true;
				}
			}
		}
		if (taught == 0) {
			teacher.sendSystemMessage(Component.translatable("neothaum.addition.nothing_to_share", student.getDisplayName()));
			return 0;
		}
		if (NeoThaumConfig.current.allowWarp) {
			Warp.addWarpToPlayer(teacher, PERMANENT_WARP, false);
			Warp.addStickyWarpToPlayer(teacher, STICKY_WARP);
		}
		teacher.sendSystemMessage(Component.translatable("neothaum.addition.shared", student.getDisplayName(), taught));
		student.sendSystemMessage(Component.translatable("neothaum.addition.shared_to", teacher.getDisplayName(), taught));
		return taught;
	}

	private ResearchSharing() {
	}
}
