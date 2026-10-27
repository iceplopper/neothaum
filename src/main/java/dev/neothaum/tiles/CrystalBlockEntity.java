package dev.neothaum.tiles;

import dev.neothaum.registry.TVBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** TileCrystal: exists so TileCrystalRenderer can draw the cluster; the orientation lives in the block state. */
public class CrystalBlockEntity extends BlockEntity {
	public CrystalBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.CRYSTAL, pos, state);
	}
}
