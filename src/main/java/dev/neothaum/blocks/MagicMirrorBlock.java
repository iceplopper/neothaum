package dev.neothaum.blocks;

import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.tiles.MagicMirrorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class MagicMirrorBlock extends MirrorBlock implements EntityBlock {
	public MagicMirrorBlock(Properties props) {
		super(false, props);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new MagicMirrorBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide() ? null : createTickerHelper(type, TVBlockEntities.MAGIC_MIRROR, (lvl, pos, st, be) -> be.tick(lvl, pos, st));
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, net.minecraft.world.entity.InsideBlockEffectApplier effectApplier, boolean isPrecise) {
		if (!level.isClientSide() && entity instanceof ItemEntity item && !item.isRemoved()) {
			if (level.getBlockEntity(pos) instanceof MagicMirrorBlockEntity mirror) {
				mirror.transport(item);
			}
		}
	}
}
