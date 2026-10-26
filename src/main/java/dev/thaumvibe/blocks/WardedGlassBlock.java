package dev.thaumvibe.blocks;

import dev.thaumvibe.tiles.OwnedBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Port of BlockCosmeticOpaque meta 2: Warded Glass is cosmetic glass unbreakable except by owner. */
public class WardedGlassBlock extends TransparentBlock implements EntityBlock {
	public WardedGlassBlock(BlockBehaviour.Properties props) {
		super(props);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (placer instanceof Player player && level.getBlockEntity(pos) instanceof OwnedBlockEntity be) {
			be.setOwner(player);
		}
	}

	@Override
	public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof OwnedBlockEntity be) {
			if (!be.isOwner(player)) {
				return 0.0F; // Unbreakable to non-owners
			}
		}
		return super.getDestroyProgress(state, player, level, pos);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new OwnedBlockEntity(pos, state);
	}
}
