package dev.neothaum.blocks;

import com.mojang.serialization.MapCodec;
import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.tiles.NodeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** BlockAiry meta 0: an aura node. Invisible block; the node itself is drawn by the block entity renderer. */
public class AuraNodeBlock extends BaseEntityBlock {
	private static final VoxelShape SHAPE = box(4.8, 4.8, 4.8, 11.2, 11.2, 11.2);

	/** A bright node lights its surroundings fully (light 15, the addon in dev.neothaum.addon.BrightNodes); every other node gives TC4's 8. */
	public static final net.minecraft.world.level.block.state.properties.BooleanProperty BRIGHT =
		net.minecraft.world.level.block.state.properties.BooleanProperty.create("bright");

	public AuraNodeBlock(BlockBehaviour.Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(BRIGHT, false));
	}

	@Override
	protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
		builder.add(BRIGHT);
	}


	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		BlockState inside = embedded(level, pos);
		return inside != null ? inside.getShape(level, pos, context) : SHAPE;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		BlockState inside = embedded(level, pos);
		return inside != null ? inside.getCollisionShape(level, pos, context) : Shapes.empty();
	}

	/** A node with a block inside breaks like that block (the block comes out, the node stays); a bare node can't be broken. */
	@Override
	protected float getDestroyProgress(BlockState state, net.minecraft.world.entity.player.Player player, BlockGetter level, BlockPos pos) {
		BlockState inside = embedded(level, pos);
		return inside != null ? inside.getDestroyProgress(player, level, pos) : super.getDestroyProgress(state, player, level, pos);
	}

	/** The block placed inside this node, if any. */
	public static BlockState embedded(BlockGetter level, BlockPos pos) {
		return level.getBlockEntity(pos) instanceof NodeBlockEntity node ? node.embedded : null;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new NodeBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, TVBlockEntities.NODE, NodeBlockEntity::tick);
	}
}
