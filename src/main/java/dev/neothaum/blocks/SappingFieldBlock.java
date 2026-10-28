package dev.neothaum.blocks;

import dev.neothaum.api.EldritchMob;
import dev.neothaum.fx.FX;
import dev.neothaum.registry.TVSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BlockAiry meta 11, the sapping field: purple crackling air that the Eldritch Warden trails behind it and casts in rings when its ward breaks.
 * It drags at anything but eldritch things, makes players hungry, weakens, and now and then withers; it fades on its own.
 */
public class SappingFieldBlock extends Block {
	public SappingFieldBlock(BlockBehaviour.Properties props) {
		super(props);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
		if (entity instanceof EldritchMob) {
			return;
		}
		if (level instanceof ServerLevel sl && level.getRandom().nextInt(100) == 0) {
			entity.hurtServer(sl, sl.damageSources().wither(), 1.0F);
		}
		entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.66, 1.0, 0.66));
		if (entity instanceof Player p) {
			p.causeFoodExhaustion(0.05F);
		}
		if (entity instanceof LivingEntity le && !level.isClientSide()) {
			le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1, true, true));
		}
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		level.removeBlock(pos, false);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		level.removeBlock(pos, false);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource r) {
		float h = r.nextFloat() * 0.33F;
		FX.spark(level, pos.getX() + r.nextFloat(), pos.getY() + 0.1515F + h / 2.0F, pos.getZ() + r.nextFloat(), 0.33F + h,
			0.3F - r.nextFloat() * 0.1F, 0.0F, 0.5F + r.nextFloat() * 0.2F, 1.0F);
		if (r.nextInt(50) == 0) {
			level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), TVSounds.JACOBS, SoundSource.BLOCKS, 0.5F,
				1.0F + (r.nextFloat() - r.nextFloat()) * 0.2F, false);
		}
	}
}
