package dev.thaumvibe.tiles;

import dev.thaumvibe.blocks.InfusionPillarBlock;
import dev.thaumvibe.registry.TVBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** TileInfusionPillar: only there so the pillar's OBJ model can be drawn; its corner comes from the block state. */
public class InfusionPillarBlockEntity extends TVBlockEntity {
	public InfusionPillarBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.INFUSION_PILLAR, pos, state);
	}

	/** TC4's orientation byte, 2 to 5. */
	public int orientation() {
		return this.getBlockState().getValue(InfusionPillarBlock.CORNER);
	}
}
