package dev.neothaum.blocks;

import dev.neothaum.NeoThaum;
import dev.neothaum.fx.FX;
import dev.neothaum.registry.TVItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * BlockFluidDeath: a Forge finite fluid of 4 quanta (LEVEL holds quanta - 1, viscosity 1500) that sinks and spreads out until it is one
 * quantum deep. Anything living in it takes level + 1 "dissolve" damage, which ignores armour; what it kills drops crystallized essence of
 * its aspects (see AspectOrbEvents). FULL marks a block the renderer draws as a whole cube, under more of it.
 */
public class LiquidDeathBlock extends Block {
	public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 3);
	public static final BooleanProperty FULL = BooleanProperty.create("full");
	public static final int QUANTA = 4;
	public static final ResourceKey<DamageType> DISSOLVE = ResourceKey.create(Registries.DAMAGE_TYPE, NeoThaum.id("dissolve"));
	private static final int TICK_RATE = 1500 / 200;

	public LiquidDeathBlock(BlockBehaviour.Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, QUANTA - 1).setValue(FULL, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LEVEL, FULL);
	}

	public static DamageSource dissolve(Level level) {
		return new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DISSOLVE));
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
		level.scheduleTick(pos, this, TICK_RATE);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir, BlockPos neighborPos,
		BlockState neighbor, RandomSource random) {
		ticks.scheduleTick(pos, this, TICK_RATE);
		return state.setValue(FULL, level.getBlockState(pos.above()).is(this));
	}

	private BlockState withQuanta(BlockGetter level, BlockPos pos, int quanta) {
		return this.defaultBlockState().setValue(LEVEL, quanta - 1).setValue(FULL, level.getBlockState(pos.above()).is(this));
	}

	/** A full bucket's worth can be scooped up again. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult hit) {
		if (!stack.is(Items.BUCKET) || state.getValue(LEVEL) != QUANTA - 1) {
			return super.useItemOn(stack, state, level, pos, player, hand, hit);
		}
		level.removeBlock(pos, false);
		level.playSound(player, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
		if (!player.getAbilities().instabuild) {
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

	// ------------------------------------------------------------------ BlockFluidFinite.updateTick

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
		int quantaRemaining = state.getValue(LEVEL) + 1;
		int prevRemaining = quantaRemaining;
		quantaRemaining = this.tryToFlowDownInto(level, pos, quantaRemaining);
		if (quantaRemaining < 1) {
			return;
		}
		boolean changed = quantaRemaining != prevRemaining;
		if (quantaRemaining == 1) {
			if (changed) {
				level.setBlock(pos, this.withQuanta(level, pos, quantaRemaining), Block.UPDATE_CLIENTS);
			}
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
				level.setBlock(sp, newSide == 0 ? Blocks.AIR.defaultBlockState() : this.withQuanta(level, sp, newSide), Block.UPDATE_ALL);
				level.scheduleTick(sp, this, TICK_RATE);
			}
			count--;
		}
		if (rem > 0) {
			each++;
		}
		level.setBlock(pos, this.withQuanta(level, pos, each), Block.UPDATE_CLIENTS);
	}

	/** Air and other replaceable non-fluids count as 0; this fluid with fewer quanta than asked counts as its quanta; anything else -1. */
	private int getQuantaValueBelow(Level level, BlockPos pos, int belowThan) {
		BlockState s = level.getBlockState(pos);
		if (s.is(this)) {
			int q = s.getValue(LEVEL) + 1;
			return q >= belowThan ? -1 : q;
		}
		return s.isAir() || s.canBeReplaced() && s.getFluidState().isEmpty() && !(s.getBlock() instanceof FluxFluidBlock) ? 0 : -1;
	}

	private int tryToFlowDownInto(ServerLevel level, BlockPos pos, int amtToInput) {
		BlockPos other = pos.below();
		if (other.getY() < level.getMinY()) {
			level.removeBlock(pos, false);
			return 0;
		}
		int amt = this.getQuantaValueBelow(level, other, QUANTA);
		if (amt < 0) {
			return amtToInput;
		}
		amt += amtToInput;
		if (amt > QUANTA) {
			level.setBlock(other, this.withQuanta(level, other, QUANTA), Block.UPDATE_ALL);
			level.scheduleTick(other, this, TICK_RATE);
			return amt - QUANTA;
		}
		if (amt > 0) {
			level.setBlock(other, this.withQuanta(level, other, amt), Block.UPDATE_ALL);
			level.scheduleTick(other, this, TICK_RATE);
			level.removeBlock(pos, false);
			return 0;
		}
		return amtToInput;
	}

	// ------------------------------------------------------------------ entities

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean isPrecise) {
		entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.8, 0.8, 0.8));
		if (level instanceof ServerLevel sl && entity instanceof LivingEntity living) {
			living.hurtServer(sl, dissolve(level), state.getValue(LEVEL) + 1);
		}
	}

	/** FXSlimyBubble at the surface, purple, and now and then a lava pop. */
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
		int meta = state.getValue(LEVEL);
		FX.crucibleBubble(level, pos.getX() + rand.nextFloat(), pos.getY() + 0.1F + 0.225F * meta, pos.getZ() + rand.nextFloat(),
			0.3F - rand.nextFloat() * 0.1F, 0.0F, 0.4F + rand.nextFloat() * 0.1F);
		if (rand.nextInt(50) == 0) {
			level.playLocalSound(pos.getX() + rand.nextFloat(), pos.getY() + (meta + 1) / (float) QUANTA, pos.getZ() + rand.nextFloat(), SoundEvents.LAVA_POP,
				SoundSource.BLOCKS, 0.1F + rand.nextFloat() * 0.1F, 0.9F + rand.nextFloat() * 0.15F, false);
		}
	}
}
