package dev.thaumvibe.blocks;

import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.tiles.WardingStoneBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** BlockAiry meta 4: invisible collision barrier maintained above Paving Stone of Warding. */
public class WardingAuraBlock extends Block {
	public WardingAuraBlock(BlockBehaviour.Properties props) {
		super(props);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		// Powered paving stone deactivates barrier
		for (int dy = 1; dy <= 2; dy++) {
			BlockPos below = pos.below(dy);
			if (level.getBlockState(below).is(TVBlocks.PAVING_STONE_OF_WARDING)) {
				if (level.getBlockEntity(below) instanceof WardingStoneBlockEntity be && be.isGettingPower()) {
					return Shapes.empty();
				}
			}
		}
		// Players pass freely; mobs are blocked
		if (context instanceof EntityCollisionContext ecc && ecc.getEntity() instanceof Player) {
			return Shapes.empty();
		}
		return Shapes.block();
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos,
		Direction dir, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		boolean hasBase = level.getBlockState(pos.below(1)).is(TVBlocks.PAVING_STONE_OF_WARDING)
			|| level.getBlockState(pos.below(2)).is(TVBlocks.PAVING_STONE_OF_WARDING);
		if (!hasBase) {
			return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, level, tickAccess, pos, dir, neighborPos, neighborState, random);
	}
}
