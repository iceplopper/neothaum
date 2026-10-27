package dev.neothaum.blocks;

import dev.neothaum.NeoThaum;
import dev.neothaum.fx.FX;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVMobEffects;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.world.TVBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/**
 * BlockTaint: crusted taint (which creeps, drips, sags over edges and slowly turns to flux goo), tainted soil (which reverts to dirt outside the
 * taint) and blocks of flesh (which do nothing at all).
 */
public class TaintBlock extends Block {
	public enum Kind { CRUST, SOIL, FLESH }

	/** Config.taintMaterial: what counts as taint for the fibres' adjacency rules. */
	public static final TagKey<Block> TAINT = TagKey.create(Registries.BLOCK, NeoThaum.id("taint"));
	/** CustomSoundType("gore", 0.5, 0.8) */
	public static final SoundType GORE = new SoundType(0.5F, 0.8F, TVSounds.GORE, TVSounds.GORE, TVSounds.GORE, TVSounds.GORE, TVSounds.GORE);

	private final Kind kind;

	public TaintBlock(Kind kind, Properties props) {
		super(props);
		this.kind = kind;
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return this.kind != Kind.FLESH;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		TaintFibreBlock.taintBiomeSpread(level, pos, random);
		if (this.kind == Kind.CRUST) {
			if (this.tryToFall(level, pos, pos)) {
				return;
			}
			if (level.isEmptyBlock(pos.above())) {
				boolean doIt = true;
				Direction dir = Direction.from2DDataValue(random.nextInt(4));
				for (int a = 0; a < 4; a++) {
					if (!level.isEmptyBlock(pos.relative(dir).below(a))) {
						doIt = false;
						break;
					}
					if (level.getBlockState(pos.below(a)).getBlock() != this) {
						doIt = false;
						break;
					}
				}
				if (doIt && this.tryToFall(level, pos, pos.relative(dir))) {
					return;
				}
			}
		}
		BlockPos p2 = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
		if (TVBiomes.is(level, p2, TVBiomes.TAINTED_LAND)) {
			TaintFibreBlock.spreadFibres(level, p2);
			if (this.kind == Kind.CRUST) {
				if (level.isEmptyBlock(pos.above()) && random.nextInt(200) == 0) {
					dev.neothaum.entity.monster.TaintSporeSwarmer.spawnFromCrust(level, pos);
				} else {
					boolean doIt = level.getBlockState(pos.above()).getBlock() == this;
					if (doIt) {
						for (Direction d : Direction.Plane.HORIZONTAL) {
							if (level.getBlockState(pos.relative(d)).getBlock() != this) {
								doIt = false;
								break;
							}
						}
					}
					if (doIt) {
						level.setBlock(pos, ((FluxFluidBlock) TVBlocks.FLUX_GOO).fullState(), Block.UPDATE_ALL);
					}
				}
			}
		} else if (this.kind == Kind.CRUST && random.nextInt(20) == 0) {
			level.setBlock(pos, ((FluxFluidBlock) TVBlocks.FLUX_GOO).fullState(), Block.UPDATE_ALL);
		} else if (this.kind == Kind.SOIL && random.nextInt(10) == 0) {
			level.setBlock(pos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
		}
	}

	public static boolean canFallBelow(Level level, BlockPos pos) {
		for (BlockPos p : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
			if (level.getBlockState(p).is(BlockTags.LOGS)) {
				return false;
			}
		}
		BlockState state = level.getBlockState(pos);
		if (state.isAir()) {
			return true;
		}
		if (state.is(TVBlocks.FLUX_GOO) && state.getValue(FluxFluidBlock.LEVEL) >= 4) {
			return false;
		}
		if (state.is(Blocks.FIRE) || state.getBlock() instanceof TaintFibreBlock || state.canBeReplaced()) {
			return true;
		}
		FluidState fluid = state.getFluidState();
		return !fluid.isEmpty();
	}

	private boolean tryToFall(ServerLevel level, BlockPos pos, BlockPos pos2) {
		if (canFallBelow(level, pos2.below()) && pos2.getY() >= level.getMinY()) {
			BlockState state = level.getBlockState(pos);
			level.removeBlock(pos, false);
			if (!pos2.equals(pos)) {
				level.setBlock(pos2, state, Block.UPDATE_CLIENTS);
			}
			FallingBlockEntity.fall(level, pos2, state);
			return true;
		}
		return false;
	}

	@Override
	public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
		if (this.kind != Kind.FLESH && !level.isClientSide() && entity instanceof LivingEntity le && !le.isInvertedHealAndHarm()) {
			if (entity instanceof Player && level.getRandom().nextInt(100) == 0) {
				le.addEffect(new MobEffectInstance(TVMobEffects.FLUX_TAINT, 80, 0));
			} else if (!(entity instanceof Player) && level.getRandom().nextInt(20) == 0) {
				le.addEffect(new MobEffectInstance(TVMobEffects.FLUX_TAINT, 160, 0));
			}
		}
		super.stepOn(level, pos, state, entity);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (this.kind == Kind.CRUST && level.isEmptyBlock(pos.below()) && random.nextInt(10) == 0) {
			FX.droplet(level, pos.getX() + 0.1F + random.nextFloat() * 0.8F, pos.getY(), pos.getZ() + 0.1F + random.nextFloat() * 0.8F, 0.3F, 0.1F,
				0.8F);
		}
	}

	@Override
	protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
		if (id == 1) {
			if (level.isClientSide()) {
				level.playLocalSound(pos, TVSounds.ROOTS, SoundSource.BLOCKS, 0.1F, 0.9F + level.getRandom().nextFloat() * 0.2F, false);
			}
			return true;
		}
		return super.triggerEvent(state, level, pos, id, param);
	}
}
