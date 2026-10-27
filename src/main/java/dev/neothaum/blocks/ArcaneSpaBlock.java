package dev.neothaum.blocks;

import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVItems;
import dev.neothaum.tiles.SpaBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Port of BlockStoneDevice metadata 12: Arcane Spa.
 * Automates creating a rejuvenating bath by mixing water with Bath Salts to produce
 * Purifying Fluid that fills a 5x5 pool above the spa.
 */
public class ArcaneSpaBlock extends Block implements EntityBlock {
	public ArcaneSpaBlock(BlockBehaviour.Properties props) {
		super(props);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SpaBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide()) return null;
		return type == TVBlockEntities.SPA ? (lvl, pos, st, be) -> SpaBlockEntity.tick(lvl, pos, st, (SpaBlockEntity) be) : null;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof SpaBlockEntity spa) {
			if (!level.isClientSide()) {
				if (stack.is(Items.WATER_BUCKET)) {
					if (spa.fillFluid("water", 1000)) {
						if (!player.getAbilities().instabuild) {
							stack.shrink(1);
							ItemStack empty = new ItemStack(Items.BUCKET);
							if (stack.isEmpty()) {
								player.setItemInHand(hand, empty);
							} else if (!player.getInventory().add(empty)) {
								player.drop(empty, false, Prediction.SERVER_ONLY);
							}
						}
						level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
						return InteractionResult.SUCCESS;
					}
				} else if (stack.is(TVItems.BUCKET_OF_PURIFYING_FLUID)) {
					if (spa.fillFluid("purifying_fluid", 1000)) {
						if (!player.getAbilities().instabuild) {
							stack.shrink(1);
							ItemStack empty = new ItemStack(Items.BUCKET);
							if (stack.isEmpty()) {
								player.setItemInHand(hand, empty);
							} else if (!player.getInventory().add(empty)) {
								player.drop(empty, false, Prediction.SERVER_ONLY);
							}
						}
						level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
						return InteractionResult.SUCCESS;
					}
				} else if (stack.is(Items.BUCKET)) {
					if (spa.getFluidAmount() >= 1000) {
						ItemStack filled = "purifying_fluid".equals(spa.getFluidType())
							? new ItemStack(TVItems.BUCKET_OF_PURIFYING_FLUID)
							: new ItemStack(Items.WATER_BUCKET);
						spa.drainFluid(1000);
						if (!player.getAbilities().instabuild) {
							stack.shrink(1);
							if (stack.isEmpty()) {
								player.setItemInHand(hand, filled);
							} else if (!player.getInventory().add(filled)) {
								player.drop(filled, false, Prediction.SERVER_ONLY);
							}
						}
						level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
						return InteractionResult.SUCCESS;
					}
				} else {
					player.openMenu(spa);
					return InteractionResult.SUCCESS;
				}
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof SpaBlockEntity spa) {
			Containers.dropContents(level, pos, spa);
		}
		return super.playerWillDestroy(level, pos, state, player);
	}
}
