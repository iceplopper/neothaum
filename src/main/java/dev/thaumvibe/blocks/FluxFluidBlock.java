package dev.thaumvibe.blocks;

import dev.thaumvibe.fx.FX;
import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.registry.TVMobEffects;
import dev.thaumvibe.registry.TVSounds;
import dev.thaumvibe.world.TVBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * BlockFluxGoo and BlockFluxGas: Forge finite fluids of 8 quanta (LEVEL holds quanta - 1). Goo sinks and spreads, slows and exhausts what wades
 * through it, slowly evaporates into gas and can curdle into thaumic slimes; gas rises and makes anything breathing it ill. FULL marks a block
 * the renderer draws as a whole cube (goo under more goo; gas with nothing to cling to above it).
 */
public class FluxFluidBlock extends Block {
	public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 7);
	public static final BooleanProperty FULL = BooleanProperty.create("full");
	public static final int QUANTA = 8;
	private final boolean gas;
	/** Forge's densityDir: -1 sinks (goo), 1 rises (gas). */
	private final int densityDir;
	/** BlockFluidBase.tickRate: the fluid's viscosity / 200. */
	private final int tickRate;

	public FluxFluidBlock(boolean gas, BlockBehaviour.Properties props) {
		super(props);
		this.gas = gas;
		this.densityDir = gas ? 1 : -1;
		this.tickRate = gas ? 2500 / 200 : 6000 / 200;
		this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 7).setValue(FULL, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LEVEL, FULL);
	}

	public int quanta(BlockState state) {
		return state.getValue(LEVEL) + 1;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	protected boolean canBeReplaced(BlockState state, net.minecraft.world.item.context.BlockPlaceContext context) {
		return state.getValue(LEVEL) < 2;
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
		level.scheduleTick(pos, this, this.tickRate);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir, BlockPos neighborPos,
		BlockState neighbor, RandomSource random) {
		ticks.scheduleTick(pos, this, this.tickRate);
		return state.setValue(FULL, this.isFull(level, pos));
	}

	private boolean isFull(BlockGetter level, BlockPos pos) {
		if (this.gas) {
			// BlockGasRenderer: gas with no solid ceiling fills its block
			return !level.getBlockState(pos.above()).isFaceSturdy(level, pos.above(), Direction.DOWN);
		}
		return level.getBlockState(pos.above()).is(this);
	}

	/** A full block of it (getQuanta()). */
	public BlockState fullState() {
		return this.defaultBlockState().setValue(LEVEL, 7).setValue(FULL, true);
	}

	public BlockState withQuanta(BlockGetter level, BlockPos pos, int quanta) {
		return this.defaultBlockState().setValue(LEVEL, quanta - 1).setValue(FULL, this.isFull(level, pos));
	}

	// ------------------------------------------------------------------ BlockFluidFinite.updateTick

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
		this.flow(state, level, pos, rand);
		BlockState now = level.getBlockState(pos);
		if (now.is(this)) {
			if (this.gas) {
				return;
			}
			this.gooTick(now, level, pos, rand);
		}
	}

	private void flow(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
		boolean changed = false;
		int quantaRemaining = this.quanta(state);
		int prevRemaining = quantaRemaining;
		quantaRemaining = this.tryToFlowVerticallyInto(level, pos, quantaRemaining);
		if (quantaRemaining < 1) {
			return;
		}
		if (quantaRemaining != prevRemaining) {
			changed = true;
			if (quantaRemaining == 1) {
				level.setBlock(pos, this.withQuanta(level, pos, quantaRemaining), Block.UPDATE_CLIENTS);
				return;
			}
		} else if (quantaRemaining == 1) {
			return;
		}
		int lowerThan = quantaRemaining - 1;
		Direction[] dirs = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
		int[] side = new int[4];
		int count = 1;
		int total = quantaRemaining;
		for (int i = 0; i < 4; i++) {
			side[i] = this.getQuantaValueBelow(level, pos.relative(dirs[i]), lowerThan);
			if (side[i] >= 0) {
				count++;
				total += side[i];
			}
		}
		if (count == 1) {
			if (changed) {
				level.setBlock(pos, this.withQuanta(level, pos, quantaRemaining), Block.UPDATE_CLIENTS);
			}
			return;
		}
		int each = total / count;
		int rem = total % count;
		for (int i = 0; i < 4; i++) {
			if (side[i] < 0) {
				continue;
			}
			int newSide = each;
			if (rem == count || rem > 1 && rand.nextInt(count - rem) != 0) {
				newSide++;
				rem--;
			}
			if (newSide != side[i]) {
				BlockPos sp = pos.relative(dirs[i]);
				level.setBlock(sp, newSide == 0 ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState() : this.withQuanta(level, sp, newSide),
					Block.UPDATE_ALL);
				level.scheduleTick(sp, this, this.tickRate);
			}
			count--;
		}
		if (rem > 0) {
			each++;
		}
		level.setBlock(pos, this.withQuanta(level, pos, each), Block.UPDATE_CLIENTS);
	}

	/** Air (and thin goo's own displaceables) counts as 0; this fluid with fewer quanta than asked counts as its quanta; anything else -1. */
	private int getQuantaValueBelow(Level level, BlockPos pos, int belowThan) {
		BlockState s = level.getBlockState(pos);
		if (s.is(this)) {
			int q = this.quanta(s);
			return q >= belowThan ? -1 : q;
		}
		return s.isAir() || s.canBeReplaced() && s.getFluidState().isEmpty() && !(s.getBlock() instanceof FluxFluidBlock) ? 0 : -1;
	}

	private int tryToFlowVerticallyInto(ServerLevel level, BlockPos pos, int amtToInput) {
		BlockPos other = pos.above(this.densityDir);
		if (other.getY() < level.getMinY() || other.getY() > level.getMaxY()) {
			level.removeBlock(pos, false);
			return 0;
		}
		int amt = this.getQuantaValueBelow(level, other, QUANTA);
		if (amt >= 0) {
			amt += amtToInput;
			if (amt > QUANTA) {
				level.setBlock(other, this.withQuanta(level, other, QUANTA), Block.UPDATE_ALL);
				level.scheduleTick(other, this, this.tickRate);
				return amt - QUANTA;
			}
			if (amt > 0) {
				level.setBlock(other, this.withQuanta(level, other, amt), Block.UPDATE_ALL);
				level.scheduleTick(other, this, this.tickRate);
				level.removeBlock(pos, false);
				return 0;
			}
			return amtToInput;
		}
		return amtToInput;
	}

	/** BlockFluxGoo.updateTick: evaporating, curdling into slimes, or seeding taint. */
	private void gooTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
		int meta = state.getValue(LEVEL);
		boolean airAbove = level.isEmptyBlock(pos.above());
		if (meta >= 2 && meta < 6 && airAbove && rand.nextInt(25) == 0) {
			level.removeBlock(pos, false);
			this.spawnSlime(level, pos, 1);
		} else if (meta >= 6 && airAbove) {
			if (rand.nextInt(25) == 0) {
				level.removeBlock(pos, false);
				this.spawnSlime(level, pos, 2);
			} else if (rand.nextInt(50) == 0) {
				// Config.taintFromFlux
				TVBiomes.setBiomeAt(level, pos.getX(), pos.getZ(), TVBiomes.TAINTED_LAND);
				level.setBlock(pos, TVBlocks.TAINT_FIBRES.defaultBlockState(), Block.UPDATE_ALL);
				level.blockEvent(pos, TVBlocks.TAINT_FIBRES, 1, 0);
			}
		} else if (rand.nextInt(30) == 0) {
			if (meta == 0) {
				level.removeBlock(pos, false);
			} else {
				level.setBlock(pos, state.setValue(LEVEL, meta - 1), Block.UPDATE_ALL);
				if (rand.nextBoolean() && airAbove) {
					level.setBlock(pos.above(), ((FluxFluidBlock) TVBlocks.FLUX_GAS).withQuanta(level, pos.above(), 1), Block.UPDATE_ALL);
				}
			}
		}
		level.scheduleTick(pos, this, this.tickRate);
	}

	/** BlockFluxGoo: a thaumic slime curdles out of the goo. */
	private void spawnSlime(ServerLevel level, BlockPos pos, int size) {
		if (level.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) {
			return;
		}
		var slime = dev.thaumvibe.entity.TVEntities.THAUMIC_SLIME.create(level, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
		if (slime != null) {
			slime.setSlimeSize(size);
			slime.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
			level.addFreshEntity(slime);
			level.playSound(null, pos, TVSounds.GORE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
		}
	}

	// ------------------------------------------------------------------ entities

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean isPrecise) {
		int md = state.getValue(LEVEL);
		if (this.gas) {
			if (!level.isClientSide() && level.getRandom().nextInt(10) == 0 && entity instanceof LivingEntity le
				&& !(entity instanceof dev.thaumvibe.api.TaintedMob) && !le.isInvertedHealAndHarm() && !le.hasEffect(TVMobEffects.VIS_EXHAUST)
				&& !le.hasEffect(MobEffects.NAUSEA)) {
				if (level.getRandom().nextBoolean()) {
					le.addEffect(new MobEffectInstance(TVMobEffects.VIS_EXHAUST, 1200, md / 3, true, true));
				} else {
					le.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80 + md * 20, 0));
				}
				if (md > 0) {
					level.setBlock(pos, state.setValue(LEVEL, md - 1), Block.UPDATE_ALL);
				} else {
					level.removeBlock(pos, false);
				}
			}
			return;
		}
		// the deeper the goo, the more it drags
		float q = (md + 1) / (float) QUANTA;
		entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.0F - q, 1.0, 1.0F - q));
		if (!level.isClientSide() && entity instanceof LivingEntity le) {
			le.addEffect(new MobEffectInstance(TVMobEffects.VIS_EXHAUST, 600, md / 3, true, true));
		}
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
		if (!this.gas) {
			int meta = state.getValue(LEVEL);
			if (rand.nextInt(40) <= meta) {
				FX.crucibleBubble(level, pos.getX() + rand.nextFloat(), pos.getY() + 0.125F * meta, pos.getZ() + rand.nextFloat(), 0.6F, 0.2F, 0.8F);
			}
		}
	}
}
