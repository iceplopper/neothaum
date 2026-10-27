package dev.neothaum.blocks;

import dev.neothaum.api.aspects.EssentiaTransport;
import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVComponents;
import dev.neothaum.registry.TVItems;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.tiles.BufferBlockEntity;
import dev.neothaum.tiles.TubeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import java.util.Arrays;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * BlockTube: essentia tubes. Each side's arm shows when the neighbour carries essentia and both sides are open; the FACING property drives
 * the valve handle and the one-way tube's arrow.
 */
public class TubeBlock extends BaseEntityBlock {
	/**
	 * An arm reaches the block edge for a tube, and on into the neighbour for jars and alembics (renderExtendedTube). A side sealed with the
	 * wand draws nothing but keeps its arm's hitbox (as BlockTube.collisionRayTrace did), so the wand can open it again.
	 */
	public enum Arm implements StringRepresentable {
		NONE, TUBE, EXTENDED, CLOSED;

		@Override
		public String getSerializedName() {
			return this.name().toLowerCase(java.util.Locale.ROOT);
		}
	}

	public static final List<EnumProperty<Arm>> ARMS = Arrays.stream(Direction.values())
		.map(d -> EnumProperty.create(d.getSerializedName(), Arm.class)).toList();
	/** The fat "node" core instead of the knot, when the tube meets anything but other tubes (or nothing at all). */
	public static final BooleanProperty NODE = BooleanProperty.create("node");
	public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
	private static final VoxelShape CORE = box(5.5, 5.5, 5.5, 10.5, 10.5, 10.5);
	private static final VoxelShape[] ARM_SHAPES = {box(6, 0, 6, 10, 6, 10), box(6, 10, 6, 10, 16, 10), box(6, 6, 0, 10, 10, 6),
		box(6, 6, 10, 10, 10, 16), box(0, 6, 6, 6, 10, 10), box(10, 6, 6, 16, 10, 10)};
	/** The core plus each combination of arms, built once: every tube block has thousands of states sharing these 64 shapes. */
	private static final VoxelShape[] SHAPES = new VoxelShape[64];

	static {
		for (int mask = 0; mask < 64; mask++) {
			VoxelShape s = CORE;
			for (int i = 0; i < 6; i++) {
				if ((mask & 1 << i) != 0) {
					s = Shapes.or(s, ARM_SHAPES[i]);
				}
			}
			SHAPES[mask] = s;
		}
	}

	public final TubeBlockEntity.Kind kind;

	public TubeBlock(TubeBlockEntity.Kind kind, BlockBehaviour.Properties props) {
		super(props);
		this.kind = kind;
		BlockState s = this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(NODE, true);
		for (EnumProperty<Arm> p : ARMS) {
			s = s.setValue(p, Arm.NONE);
		}
		this.registerDefaultState(s);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(ARMS.toArray(new EnumProperty[0])).add(NODE, FACING);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		int mask = 0;
		for (Direction d : Direction.values()) {
			if (state.getValue(ARMS.get(d.ordinal())) != Arm.NONE) {
				mask |= 1 << d.ordinal();
			}
		}
		return SHAPES[mask];
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return this.kind == TubeBlockEntity.Kind.BUFFER ? new BufferBlockEntity(pos, state) : new TubeBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (this.kind == TubeBlockEntity.Kind.BUFFER) {
			return createTickerHelper(type, TVBlockEntities.BUFFER, BufferBlockEntity::tick);
		}
		return createTickerHelper(type, TVBlockEntities.TUBE, TubeBlockEntity::tick);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return connections(ctx.getLevel(), ctx.getClickedPos(), this.defaultBlockState().setValue(FACING, ctx.getClickedFace().getOpposite()));
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
		if (level.getBlockEntity(pos) instanceof TubeBlockEntity t) {
			t.facing = state.getValue(FACING);
			t.setChanged();
		}
		refresh(level, pos);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction dir, BlockPos neighborPos,
		BlockState neighbor, RandomSource random) {
		return connections(level, pos, state);
	}

	/** Recomputes the arms (a neighbour's tile may appear a tick after its block, so placement and wanding call this too). */
	public static void refresh(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (state.getBlock() instanceof TubeBlock) {
			BlockState updated = connections(level, pos, state);
			if (updated != state) {
				level.setBlock(pos, updated, Block.UPDATE_CLIENTS);
			}
		}
	}

	private static BlockState connections(BlockGetter level, BlockPos pos, BlockState state) {
		EssentiaTransport self = level.getBlockEntity(pos) instanceof EssentiaTransport t ? t : null;
		TubeBlockEntity tube = self instanceof TubeBlockEntity t ? t : null;
		if (tube != null) {
			state = state.setValue(FACING, tube.facing);
		}
		boolean node = true;
		boolean onlyTubes = true;
		for (Direction d : Direction.values()) {
			boolean open = self == null || self.isConnectable(d) || tube != null && tube.kind() == TubeBlockEntity.Kind.VALVE && d == tube.facing;
			EssentiaTransport t = open ? EssentiaTransport.connectable(level, pos, d) : null;
			Arm arm = t == null ? Arm.NONE : t.renderExtendedTube() ? Arm.EXTENDED : Arm.TUBE;
			if (!open && level.getBlockEntity(pos.relative(d)) instanceof EssentiaTransport) {
				arm = Arm.CLOSED;
			}
			if (t != null) {
				node = false;
				onlyTubes &= t instanceof TubeBlockEntity;
			}
			state = state.setValue(ARMS.get(d.ordinal()), arm);
		}
		// BlockTubeRenderer: the knot only joins tubes to tubes; valves always show their node
		boolean valve = state.getBlock() instanceof TubeBlock tb && tb.kind == TubeBlockEntity.Kind.VALVE;
		return state.setValue(NODE, node || !onlyTubes || valve);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult hit) {
		// a filtered tube takes its aspect from a label, like a jar does
		if (this.kind == TubeBlockEntity.Kind.FILTER && stack.is(TVItems.JAR_LABEL) && level.getBlockEntity(pos) instanceof TubeBlockEntity t
			&& t.aspectFilter == null && stack.get(TVComponents.ASPECT_FILTER) != null) {
			t.aspectFilter = stack.get(TVComponents.ASPECT_FILTER);
			stack.consume(1, player);
			t.markForUpdate();
			level.playSound(player, pos, TVSounds.JAR, SoundSource.BLOCKS, 0.4F, 1.0F);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (this.kind == TubeBlockEntity.Kind.FILTER && player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof TubeBlockEntity t
			&& t.aspectFilter != null) {
			t.aspectFilter = null;
			t.markForUpdate();
			if (!level.isClientSide()) {
				Block.popResource(level, pos, new ItemStack(TVItems.JAR_LABEL));
			} else {
				level.playLocalSound(pos, TVSounds.PAGE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
			}
			return InteractionResult.SUCCESS;
		}
		if (this.kind == TubeBlockEntity.Kind.VALVE && level.getBlockEntity(pos) instanceof TubeBlockEntity t) {
			// BlockTube: a hand on the valve turns it
			t.allowFlow = !t.allowFlow;
			t.markForUpdate();
			level.playSound(player, pos, TVSounds.SQUEEK, SoundSource.BLOCKS, 0.7F, 0.9F + level.getRandom().nextFloat() * 0.2F);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
		BlockEntity be = level.getBlockEntity(pos);
		return be != null && be.triggerEvent(id, param);
	}
}
