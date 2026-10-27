package dev.thaumvibe.blocks;

import dev.thaumvibe.registry.TVItems;
import dev.thaumvibe.registry.TVMobEffects;
import dev.thaumvibe.research.PlayerKnowledge;
import dev.thaumvibe.research.ResearchManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
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
 * Port of BlockFluidPure:
 * Silvery restorative water that grants Warp Ward to players who step into it
 * (duration inversely scaled with current permanent warp), consuming the fluid.
 */
public class PurifyingFluidBlock extends Block {
	public PurifyingFluidBlock(BlockBehaviour.Properties props) {
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
				ItemStack filled = new ItemStack(TVItems.BUCKET_OF_PURIFYING_FLUID);
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
		if (!level.isClientSide() && entity instanceof Player player) {
			if (!player.hasEffect(TVMobEffects.WARP_WARD)) {
				PlayerKnowledge k = ResearchManager.knowledge(player);
				int warp = k.warp;
				int div = 1;
				if (warp > 0) {
					div = Math.max(1, (int) Math.sqrt(warp));
				}
				int duration = Math.min(32000, 200000 / div);
				player.addEffect(new MobEffectInstance(TVMobEffects.WARP_WARD, duration, 0, true, true));
				level.removeBlock(pos, false);
				level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F);
			}
		}
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
		double x = pos.getX() + rand.nextDouble();
		double y = pos.getY() + 0.9;
		double z = pos.getZ() + rand.nextDouble();
		level.addParticle(ParticleTypes.BUBBLE, x, y, z, 0.0, 0.05, 0.0);
		if (rand.nextInt(25) == 0) {
			level.playLocalSound(x, y, z, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.1F + rand.nextFloat() * 0.1F, 0.9F + rand.nextFloat() * 0.15F, false);
		}
	}
}
