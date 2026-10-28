package dev.neothaum.fluids;

import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVItems;
import dev.neothaum.registry.TVMobEffects;
import dev.neothaum.research.ResearchManager;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

/**
 * BlockFluidPure (Forge BlockFluidClassic, viscosity 1000): a flowing fluid that spreads like water but never makes new sources. A player
 * stepping into a source block without Warp Ward gets Warp Ward for 200000 / sqrt(permanent warp) ticks (at most 32000), and the source is
 * used up.
 */
public abstract class PurifyingFluid extends FlowingFluid {
	@Override
	public Fluid getFlowing() {
		return TVFluids.FLOWING_PURIFYING;
	}

	@Override
	public Fluid getSource() {
		return TVFluids.PURIFYING;
	}

	@Override
	public Item getBucket() {
		return TVItems.BUCKET_OF_PURIFYING_FLUID;
	}

	@Override
	protected boolean canConvertToSource(ServerLevel level) {
		return false;
	}

	@Override
	protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
		BlockEntity be = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
		Block.dropResources(state, level, pos, be);
	}

	@Override
	protected void entityInside(Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects) {
		if (level.isClientSide() || !level.getFluidState(pos).isSource() || !(entity instanceof Player player)
			|| player.hasEffect(TVMobEffects.WARP_WARD)) {
			return;
		}
		int warp = ResearchManager.knowledge(player).warp;
		int div = warp > 0 ? Math.max(1, (int) Math.sqrt(warp)) : 1;
		player.addEffect(new MobEffectInstance(TVMobEffects.WARP_WARD, Math.min(32000, 200000 / div), 0, true, true));
		level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
	}

	@Override
	public void animateTick(Level level, BlockPos pos, FluidState state, RandomSource rand) {
		// FXBubble rising through the fluid, and now and then a lava pop
		float top = state.getHeight(level, pos);
		level.addParticle(ParticleTypes.BUBBLE, pos.getX() + rand.nextFloat(), pos.getY() + rand.nextFloat() * top, pos.getZ() + rand.nextFloat(),
			0.0, 0.02, 0.0);
		if (rand.nextInt(25) == 0) {
			level.playLocalSound(pos.getX() + rand.nextFloat(), pos.getY() + top, pos.getZ() + rand.nextFloat(), SoundEvents.LAVA_POP,
				SoundSource.BLOCKS, 0.1F + rand.nextFloat() * 0.1F, 0.9F + rand.nextFloat() * 0.15F, false);
		}
	}

	/** Forge BlockFluidClassic: quanta 8, so it runs 7 blocks out over flat ground. */
	@Override
	protected int getSlopeFindDistance(LevelReader level) {
		return 4;
	}

	@Override
	protected int getDropOff(LevelReader level) {
		return 1;
	}

	/** Viscosity 1000 / 200. */
	@Override
	public int getTickDelay(LevelReader level) {
		return 5;
	}

	@Override
	public boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction dir) {
		return dir == Direction.DOWN && !this.isSame(fluid);
	}

	@Override
	protected float getExplosionResistance() {
		return 100.0F;
	}

	@Override
	public BlockState createLegacyBlock(FluidState state) {
		return TVBlocks.PURIFYING_FLUID.defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
	}

	@Override
	public boolean isSame(Fluid fluid) {
		return fluid == TVFluids.PURIFYING || fluid == TVFluids.FLOWING_PURIFYING;
	}

	@Override
	public Optional<SoundEvent> getPickupSound() {
		return Optional.of(SoundEvents.BUCKET_FILL);
	}

	public static class Source extends PurifyingFluid {
		@Override
		public int getAmount(FluidState state) {
			return 8;
		}

		@Override
		public boolean isSource(FluidState state) {
			return true;
		}
	}

	public static class Flowing extends PurifyingFluid {
		@Override
		protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}

		@Override
		public int getAmount(FluidState state) {
			return state.getValue(LEVEL);
		}

		@Override
		public boolean isSource(FluidState state) {
			return false;
		}
	}
}
