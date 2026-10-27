package dev.neothaum.blocks;

import dev.neothaum.fx.FX;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Shimmerleaf, cinderpearl and vishroom (BlockCustomPlant metas 2, 3, 5). */
public class TCPlantBlock extends VegetationBlock {
	public enum Kind { SHIMMERLEAF, CINDERPEARL, VISHROOM }

	private static final VoxelShape SHAPE = box(3.2, 0.0, 3.2, 12.8, 12.8, 12.8);
	private final Kind kind;

	public TCPlantBlock(Kind kind, BlockBehaviour.Properties props) {
		super(props);
		this.kind = kind;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE.move(state.getOffset(pos));
	}

	@Override
	protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
		if (this.kind == Kind.CINDERPEARL) {
			return state.is(BlockTags.SAND) || state.is(BlockTags.TERRACOTTA) || state.is(BlockTags.SUPPORTS_VEGETATION);
		}
		return state.is(BlockTags.SUPPORTS_VEGETATION); // BlockBush: grass, dirt and farmland (26.3 dropped grass from #dirt)
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
		if (this.kind == Kind.VISHROOM && entity instanceof LivingEntity living && !level.isClientSide()) {
			living.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0));
		}
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		int i = pos.getX(), j = pos.getY(), k = pos.getZ();
		switch (this.kind) {
			case SHIMMERLEAF -> {
				if (random.nextInt(3) == 0) {
					float cr = 0.3F + random.nextFloat() * 0.3F;
					float cg = 0.7F + random.nextFloat() * 0.3F;
					float cb = 0.7F + random.nextFloat() * 0.3F;
					float xr = i + 0.5F + (random.nextFloat() - random.nextFloat()) * 0.1F;
					float yr = j + 0.5F + (random.nextFloat() - random.nextFloat()) * 0.15F;
					float zr = k + 0.5F + (random.nextFloat() - random.nextFloat()) * 0.1F;
					FX.wisp(level, xr, yr, zr, 0.2F, cr, cg, cb, false, false, 0.0F);
				}
			}
			case CINDERPEARL -> {
				if (random.nextBoolean()) {
					float xr = i + 0.5F + (random.nextFloat() - random.nextFloat()) * 0.1F;
					float yr = j + 0.6F + (random.nextFloat() - random.nextFloat()) * 0.1F;
					float zr = k + 0.5F + (random.nextFloat() - random.nextFloat()) * 0.1F;
					level.addParticle(ParticleTypes.SMOKE, xr, yr, zr, 0.0, 0.0, 0.0);
					level.addParticle(ParticleTypes.FLAME, xr, yr, zr, 0.0, 0.0, 0.0);
				}
			}
			case VISHROOM -> {
				if (random.nextInt(3) == 0) {
					float xr = i + 0.5F + (random.nextFloat() - random.nextFloat()) * 0.4F;
					float yr = j + 0.3F;
					float zr = k + 0.5F + (random.nextFloat() - random.nextFloat()) * 0.4F;
					FX.wisp(level, xr, yr, zr, 0.1F, 0.5F, 0.3F, 0.8F, false, true, 0.015F);
				}
			}
		}
	}
}
