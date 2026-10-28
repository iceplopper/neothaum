package dev.neothaum.blocks.eldritch;

import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.tiles.eldritch.EldritchObeliskBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jspecify.annotations.Nullable;

/**
 * BlockEldritch metadata 1 and 2: the obelisk's base (1), which carries the tile and draws the whole floating column, and the four invisible
 * blocks above it (2) that give the column its solidity. Like the altar and the caps it has no block model of its own.
 */
public class EldritchObeliskBlock extends BaseEntityBlock {
	public static final BooleanProperty BASE = BooleanProperty.create("base");
	private static boolean collapsing;

	public EldritchObeliskBlock(Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(BASE, true));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BASE);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(BASE) ? new EldritchObeliskBlockEntity(pos, state) : null;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return state.getValue(BASE) ? createTickerHelper(type, TVBlockEntities.ELDRITCH_OBELISK, EldritchObeliskBlockEntity::tick) : null;
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		collapse(level, pos);
		super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
	}

	/** BlockEldritch.breakBlock: breaking any part of the altar, obelisk or caps brings down the rest within reach, with a small bang. */
	public static void collapse(ServerLevel level, BlockPos pos) {
		if (collapsing) {
			return;
		}
		collapsing = true;
		try {
			for (BlockPos p : BlockPos.betweenClosed(pos.offset(-3, -2, -3), pos.offset(3, 2, 3))) {
				BlockState s = level.getBlockState(p);
				if (s.is(TVBlocks.ELDRITCH_ALTAR) || s.is(TVBlocks.ELDRITCH_OBELISK) || s.is(TVBlocks.ELDRITCH_CAP)) {
					level.removeBlock(p, false);
				}
			}
		} finally {
			collapsing = false;
		}
		level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.0F, Level.ExplosionInteraction.NONE);
	}
}
