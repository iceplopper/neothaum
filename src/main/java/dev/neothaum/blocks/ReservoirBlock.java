package dev.neothaum.blocks;

import dev.neothaum.tiles.ReservoirBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * Port of BlockEssentiaReservoir:
 * Holds up to 256 essentia of any combination of aspects. High suction (24).
 * Directional tube connection face (FACING) configurable via wand.
 */
public class ReservoirBlock extends Block implements EntityBlock {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

	public ReservoirBlock(BlockBehaviour.Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.DOWN));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return this.defaultBlockState().setValue(FACING, ctx.getClickedFace().getOpposite());
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ReservoirBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return (lvl, pos, st, be) -> {
			if (be instanceof ReservoirBlockEntity res) {
				if (lvl.isClientSide()) {
					ReservoirBlockEntity.clientTick(lvl, pos, st, res);
				} else {
					ReservoirBlockEntity.serverTick(lvl, pos, st, res);
				}
			}
		};
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof ReservoirBlockEntity res) {
			float r = (float) res.aspects.visSize() / 256.0F;
			return Mth.floor(r * 14.0F) + (res.aspects.visSize() > 0 ? 1 : 0);
		}
		return 0;
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof ReservoirBlockEntity res) {
			if (res.aspects.visSize() > 0 && !level.isClientSide()) {
				level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.0F, Level.ExplosionInteraction.NONE);
			}
		}
		return super.playerWillDestroy(level, pos, state, player);
	}
}
