package dev.neothaum.tiles;

import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * TileNodeStabilizer: under an aura node it locks the node in place (the lock itself lives in TileNode.checkLock); this only drives the
 * pistons' extension for the renderer, while an unpowered node sits on top.
 */
public class NodeStabilizerBlockEntity extends TVBlockEntity {
	public int count;

	public NodeStabilizerBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.NODE_STABILIZER, pos, state);
	}

	public boolean advanced() {
		return this.getBlockState().is(TVBlocks.ADVANCED_NODE_STABILIZER);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, NodeStabilizerBlockEntity be) {
		if (!level.isClientSide()) {
			return;
		}
		BlockState above = level.getBlockState(pos.above());
		if ((above.is(TVBlocks.AURA_NODE) || above.is(TVBlocks.ENERGIZED_AURA_NODE)) && !level.hasNeighborSignal(pos)) {
			if (be.count < 37) {
				be.count++;
			}
		} else if (be.count > 0) {
			be.count--;
		}
	}
}
