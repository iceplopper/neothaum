package dev.neothaum.items;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Bucket containing custom magical fluid (Liquid Death, Purifying Fluid).
 */
public class FluidBucketItem extends Item {
	private final Supplier<Block> fluidBlock;

	public FluidBucketItem(Supplier<Block> fluidBlock, Properties props) {
		super(props.stacksTo(1).craftRemainder(Items.BUCKET));
		this.fluidBlock = fluidBlock;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
		if (hit.getType() != HitResult.Type.BLOCK) {
			return InteractionResult.PASS;
		}
		BlockPos pos = hit.getBlockPos();
		Direction dir = hit.getDirection();
		BlockPos target = pos.relative(dir);
		BlockState targetState = level.getBlockState(target);
		if (targetState.canBeReplaced()) {
			if (!level.isClientSide()) {
				level.setBlock(target, this.fluidBlock.get().defaultBlockState(), Block.UPDATE_ALL);
			}
			level.playSound(player, target, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
			if (player != null && !player.getAbilities().instabuild) {
				player.setItemInHand(hand, new ItemStack(Items.BUCKET));
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}
}
