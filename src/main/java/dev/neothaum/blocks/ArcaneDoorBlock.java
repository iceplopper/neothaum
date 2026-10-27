package dev.neothaum.blocks;

import dev.neothaum.registry.TVSounds;
import dev.neothaum.tiles.OwnedBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;

/** Port of BlockArcaneDoor: double door only openable/breakable by owner or permitted keyholders. */
public class ArcaneDoorBlock extends DoorBlock implements EntityBlock {
	public ArcaneDoorBlock(BlockBehaviour.Properties props) {
		super(BlockSetType.IRON, props);
	}

	public BlockPos getLowerPos(BlockPos pos, BlockState state) {
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (placer instanceof Player player) {
			if (level.getBlockEntity(pos) instanceof OwnedBlockEntity be) {
				be.setOwner(player);
			}
			if (level.getBlockEntity(pos.above()) instanceof OwnedBlockEntity beTop) {
				beTop.setOwner(player);
			}
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		BlockPos lowerPos = getLowerPos(pos, state);
		if (level.getBlockEntity(lowerPos) instanceof OwnedBlockEntity be) {
			if (be.hasAccess(player)) {
				this.setOpen(player, level, state, pos, !this.isOpen(state));
				level.playSound(null, pos, this.isOpen(state) ? SoundEvents.IRON_DOOR_CLOSE : SoundEvents.IRON_DOOR_OPEN,
					SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
				return InteractionResult.SUCCESS;
			} else {
				if (!level.isClientSide()) {
					player.sendOverlayMessage(Component.translatable("tc.doorfail").withStyle(ChatFormatting.DARK_PURPLE));
					level.playSound(null, pos, TVSounds.DOORFAIL, SoundSource.BLOCKS, 0.66F, 1.0F);
				}
				return InteractionResult.CONSUME;
			}
		}
		return super.useWithoutItem(state, level, pos, player, hit);
	}

	@Override
	public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
		BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
		if (level.getBlockEntity(lowerPos) instanceof OwnedBlockEntity be) {
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
