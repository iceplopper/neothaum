package dev.neothaum.addon;

import dev.neothaum.api.nodes.NodeModifier;
import dev.neothaum.blocks.AuraNodeBlock;
import dev.neothaum.config.NeoThaumConfig;
import dev.neothaum.registry.TVBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * NeoThaum addition, not TC4 (where every node gives light 8): a bright node lights its surroundings fully, light 15, which shader packs
 * also turn into a lit glow around it. The node block's "bright" state carries the light; this keeps it in step with the node's modifier
 * and the brightNodeLight setting. The client adds a lens flare (client.addon.BrightNodeFlare).
 */
public final class BrightNodes {
	public static boolean shouldGlow(NodeModifier modifier) {
		return modifier == NodeModifier.BRIGHT && NeoThaumConfig.current.brightNodeLight;
	}

	/** Sets the node block at pos bright or not to match its modifier; anything but a free-floating aura node is left alone. */
	public static void sync(LevelAccessor level, BlockPos pos, NodeModifier modifier) {
		BlockState st = level.getBlockState(pos);
		if (st.is(TVBlocks.AURA_NODE) && st.getValue(AuraNodeBlock.BRIGHT) != shouldGlow(modifier)) {
			level.setBlock(pos, st.setValue(AuraNodeBlock.BRIGHT, shouldGlow(modifier)), 3);
		}
	}

	private BrightNodes() {
	}
}
