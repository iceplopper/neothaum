package dev.thaumvibe.blocks;

import com.mojang.serialization.MapCodec;
import dev.thaumvibe.items.WandItem;
import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.tiles.CrucibleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** BlockMetalDevice meta 0: the Crucible. */
public class CrucibleBlock extends BaseEntityBlock {
	private static final VoxelShape INSIDE = box(2.0, 5.0, 2.0, 14.0, 16.0, 14.0);
	private static final VoxelShape SHAPE = Shapes.join(Shapes.block(), INSIDE, BooleanOp.ONLY_FIRST);
	private int delay = 0;

	public CrucibleBlock(BlockBehaviour.Properties props) {
		super(props);
	}


	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
		return INSIDE;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CrucibleBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, TVBlockEntities.CRUCIBLE, CrucibleBlockEntity::tick);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof CrucibleBlockEntity tile)) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (stack.is(Items.WATER_BUCKET)) {
			if (!level.isClientSide() && tile.water < CrucibleBlockEntity.CAPACITY) {
				tile.water = CrucibleBlockEntity.CAPACITY;
				if (!player.isCreative()) {
					player.setItemInHand(hand, new ItemStack(Items.BUCKET));
				}
				tile.markForUpdate();
				level.playSound(null, pos, SoundEvents.PLAYER_SWIM, SoundSource.BLOCKS, 0.33F,
					1.0F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.3F);
			}
			return InteractionResult.SUCCESS;
		}
		if (stack.getItem() instanceof WandItem && player.isShiftKeyDown()) {
			if (!level.isClientSide()) {
				tile.spillRemnants();
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
		if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof CrucibleBlockEntity tile)) {
			return;
		}
		if (entity instanceof ItemEntity item && !CrucibleBlockEntity.isCrucibleOutput(item) && tile.heat > 150 && tile.water > 0) {
			tile.attemptSmelt(item);
		} else {
			this.delay++;
			if (this.delay < 10) {
				return;
			}
			this.delay = 0;
			if (entity instanceof LivingEntity && tile.heat > 150 && tile.water > 0 && level instanceof net.minecraft.server.level.ServerLevel sl) {
				entity.hurtServer(sl, sl.damageSources().inFire(), 1.0F);
				level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.4F, 2.0F + level.getRandom().nextFloat() * 0.4F);
			}
		}
	}
}
