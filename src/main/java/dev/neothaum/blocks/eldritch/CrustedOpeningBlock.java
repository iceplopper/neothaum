package dev.neothaum.blocks.eldritch;

import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.tiles.eldritch.CrustedOpeningBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * BlockEldritch 9, Crusted Opening: a vent in the crusted stone that breeds eldritch crabs while a player is within 16 blocks, up to six
 * crabs around it. FACING is the open side the crabs come out of. Drops nothing but 6-10 experience.
 */
public class CrustedOpeningBlock extends BaseEntityBlock {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

	public CrustedOpeningBlock(Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CrustedOpeningBlockEntity(pos, state);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, TVBlockEntities.CRUSTED_OPENING, CrustedOpeningBlockEntity::tick);
	}

	@Override
	protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
		super.triggerEvent(state, level, pos, id, param);
		return level.getBlockEntity(pos) instanceof CrustedOpeningBlockEntity be && be.triggerEvent(id, param);
	}

	@Override
	protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool, boolean dropExperience) {
		super.spawnAfterBreak(state, level, pos, tool, dropExperience);
		if (dropExperience) {
			this.tryDropExperience(level, pos, tool, UniformInt.of(6, 10));
		}
	}
}
