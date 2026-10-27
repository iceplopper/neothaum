package dev.neothaum.blocks;

import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.tiles.EnergizedNodeBlockEntity;
import dev.neothaum.tiles.NodeStabilizerBlockEntity;
import dev.neothaum.tiles.NodeTransducerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The node machines: BlockStoneDevice 9 and 10 (node stabilizers), 11 (node transducer) and BlockAiry 5 (the energized node itself). All are
 * drawn by their block entity renderers.
 */
public class NodeDeviceBlock extends BaseEntityBlock {
	public enum Kind { STABILIZER, ADVANCED_STABILIZER, TRANSDUCER, ENERGIZED_NODE }

	private static final VoxelShape NODE = box(4.8, 4.8, 4.8, 11.2, 11.2, 11.2);
	private final Kind kind;

	public NodeDeviceBlock(Kind kind, BlockBehaviour.Properties props) {
		super(props);
		this.kind = kind;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return this.kind == Kind.ENERGIZED_NODE ? NODE : Shapes.block();
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return this.kind == Kind.ENERGIZED_NODE ? Shapes.empty() : Shapes.block();
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return switch (this.kind) {
			case STABILIZER, ADVANCED_STABILIZER -> new NodeStabilizerBlockEntity(pos, state);
			case TRANSDUCER -> new NodeTransducerBlockEntity(pos, state);
			case ENERGIZED_NODE -> new EnergizedNodeBlockEntity(pos, state);
		};
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return switch (this.kind) {
			case STABILIZER, ADVANCED_STABILIZER -> createTickerHelper(type, TVBlockEntities.NODE_STABILIZER, NodeStabilizerBlockEntity::tick);
			case TRANSDUCER -> createTickerHelper(type, TVBlockEntities.NODE_TRANSDUCER, NodeTransducerBlockEntity::tick);
			case ENERGIZED_NODE -> createTickerHelper(type, TVBlockEntities.ENERGIZED_NODE, EnergizedNodeBlockEntity::tick);
		};
	}

	/** BlockStoneDevice.onNeighborBlockChange: a transducer rechecks its node when the redstone or its neighbours change. */
	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean moved) {
		if (this.kind == Kind.TRANSDUCER && !level.isClientSide() && level.getBlockEntity(pos) instanceof NodeTransducerBlockEntity t) {
			t.checkStatus();
		}
		super.neighborChanged(state, level, pos, block, orientation, moved);
	}
}
