package dev.thaumvibe.items.equipment;

import dev.thaumvibe.network.TVNetwork;
import dev.thaumvibe.registry.TVSounds;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;

/** ItemElementalAxe.onBlockStartBreak: chopping a log with the Axe of the Stream takes the furthest log of the tree instead. */
public final class ToolEvents {
	public static void init() {
		PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, be) -> {
			if (ToolHelper.isHarvesting() || player.isShiftKeyDown() || !state.is(BlockTags.LOGS)
				|| !(player.getMainHandItem().getItem() instanceof ElementalAxeItem)) {
				return true;
			}
			// the struck log stays until it is the furthest one left; the break itself is always taken over
			ToolHelper.breakFurthestBlock(level, pos, state.getBlock(), player, true, 0);
			TVNetwork.sendBubbles(level, pos, 0xFF5454FF);
			level.playSound(null, pos, TVSounds.BUBBLE, SoundSource.BLOCKS, 0.15F, 1.0F);
			player.getMainHandItem().hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
			return false;
		});
	}

	private ToolEvents() {
	}
}
