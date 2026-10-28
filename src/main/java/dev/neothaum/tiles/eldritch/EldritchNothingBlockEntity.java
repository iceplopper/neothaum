package dev.neothaum.tiles.eldritch;

import dev.neothaum.registry.TVBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** TileEldritchNothing: only shows its starfield on the faces that look onto something that isn't a full block. */
public class EldritchNothingBlockEntity extends TheEndPortalBlockEntity {
	public EldritchNothingBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.ELDRITCH_NOTHING, pos, state);
	}

	@Override
	public boolean shouldRenderFace(Direction dir) {
		if (this.level == null) {
			return false;
		}
		BlockPos n = this.worldPosition.relative(dir);
		BlockState ns = this.level.getBlockState(n);
		return !ns.isSolidRender() && !ns.is(this.getBlockState().getBlock());
	}
}
