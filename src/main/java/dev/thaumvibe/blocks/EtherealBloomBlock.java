package dev.thaumvibe.blocks;

import dev.thaumvibe.fx.FX;
import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.registry.TVSounds;
import dev.thaumvibe.tiles.EtherealBloomBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Ethereal Bloom (BlockCustomPlant meta 4):
 * Purifies tainted land and eerie biomes back to plains, and cleanses taint blocks in an 8-block radius.
 */
public class EtherealBloomBlock extends VegetationBlock implements EntityBlock {
	private static final VoxelShape SHAPE = box(3.2, 0.0, 3.2, 12.8, 14.4, 12.8);

	public EtherealBloomBlock(BlockBehaviour.Properties props) {
		super(props);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE.move(state.getOffset(pos));
	}

	@Override
	protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
		return state.is(BlockTags.SUPPORTS_VEGETATION) || state.is(TVBlocks.TAINTED_SOIL) || state.is(BlockTags.DIRT);
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, level, pos, oldState, isMoving);
		level.playSound(null, pos, TVSounds.ROOTS, SoundSource.BLOCKS, 1.0F, 0.6F);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new EtherealBloomBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (type != TVBlockEntities.ETHEREAL_BLOOM) return null;
		return (lvl, p, s, be) -> {
			if (lvl.isClientSide()) {
				((EtherealBloomBlockEntity) be).clientTick(lvl, p, s);
			} else if (lvl instanceof ServerLevel sl) {
				((EtherealBloomBlockEntity) be).serverTick(sl, p, s);
			}
		};
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextInt(3) == 0) {
			float xr = pos.getX() + 0.5F + (random.nextFloat() - random.nextFloat()) * 0.2F;
			float yr = pos.getY() + 0.4F + random.nextFloat() * 0.4F;
			float zr = pos.getZ() + 0.5F + (random.nextFloat() - random.nextFloat()) * 0.2F;
			FX.wisp(level, xr, yr, zr, 0.2F, 0.3F, 0.85F, 0.8F, false, false, 0.01F);
		}
	}
}
