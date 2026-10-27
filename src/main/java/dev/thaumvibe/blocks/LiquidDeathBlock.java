package dev.thaumvibe.blocks;

import dev.thaumvibe.registry.TVItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Port of BlockFluidDeath:
 * Highly corrosive magical liquid that dissolves living tissue (4 magic damage)
 * and emits bubbling lavapop sounds and eerie particles.
 */
public class LiquidDeathBlock extends Block {
	public LiquidDeathBlock(BlockBehaviour.Properties props) {
		super(props);
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (stack.is(Items.BUCKET)) {
			level.removeBlock(pos, false);
			level.playSound(player, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
			if (player != null && !player.getAbilities().instabuild) {
				stack.shrink(1);
				ItemStack filled = new ItemStack(TVItems.BUCKET_OF_LIQUID_DEATH);
				if (stack.isEmpty()) {
					player.setItemInHand(hand, filled);
				} else if (!player.getInventory().add(filled)) {
					player.drop(filled, false, net.minecraft.util.Prediction.SERVER_ONLY);
				}
			}
			return InteractionResult.SUCCESS;
		}
		return super.useItemOn(stack, state, level, pos, player, hand, hit);
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
		entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.8, 0.8, 0.8));
		if (!level.isClientSide() && entity instanceof LivingEntity living && level instanceof ServerLevel sl) {
			living.hurtServer(sl, level.damageSources().magic(), 4.0F);
		}
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
		double x = pos.getX() + rand.nextDouble();
		double y = pos.getY() + 0.9;
		double z = pos.getZ() + rand.nextDouble();
		level.addParticle(ParticleTypes.WITCH, x, y, z, 0.0, 0.05, 0.0);
		if (rand.nextInt(50) == 0) {
			level.playLocalSound(x, y, z, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.1F + rand.nextFloat() * 0.1F, 0.9F + rand.nextFloat() * 0.15F, false);
		}
	}
}
