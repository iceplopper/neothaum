package dev.neothaum.tiles.eldritch;

import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.tiles.TVBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1:1 Port of TileEldritchCap.
 * Capstone seated above the Eldritch Altar, holding the obelisk above it.
 */
public class EldritchCapBlockEntity extends TVBlockEntity {
	public EldritchCapBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.ELDRITCH_CAP, pos, state);
	}
}
