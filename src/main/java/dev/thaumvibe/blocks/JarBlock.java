package dev.thaumvibe.blocks;

import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.registry.TVComponents;
import dev.thaumvibe.registry.TVItems;
import dev.thaumvibe.registry.TVSounds;
import dev.thaumvibe.tiles.JarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** BlockJar metadata 0 (warded jar) and 3 (void jar). */
public class JarBlock extends BaseEntityBlock {
	private static final VoxelShape SHAPE = Shapes.or(box(3, 0, 3, 13, 12, 13), box(5, 12, 5, 11, 14, 11));
	public final boolean isVoid;

	public JarBlock(boolean isVoid, BlockBehaviour.Properties props) {
		super(props);
		this.isVoid = isVoid;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new JarBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, TVBlockEntities.JAR, JarBlockEntity::tick);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		// the label faces the player who placed the jar
		if (placer != null && level.getBlockEntity(pos) instanceof JarBlockEntity jar) {
			jar.facing = placer.getDirection().getOpposite();
			jar.setChanged();
		}
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof JarBlockEntity jar)) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (player.isShiftKeyDown() && jar.aspectFilter != null && hit.getDirection() == jar.facing) {
			return this.peelLabel(level, pos, jar, hit);
		}
		if (stack.is(TVItems.JAR_LABEL) && jar.aspectFilter == null) {
			var labelled = stack.get(TVComponents.ASPECT_FILTER);
			if (jar.amount == 0 && labelled == null) {
				return InteractionResult.SUCCESS;
			}
			if (jar.amount == 0) {
				jar.aspect = labelled;
			}
			stack.consume(1, player);
			jar.facing = player.getDirection().getOpposite();
			jar.aspectFilter = jar.aspect;
			jar.markForUpdate();
			level.playSound(player, pos, TVSounds.JAR, SoundSource.BLOCKS, 0.4F, 1.0F);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof JarBlockEntity jar) || !player.isShiftKeyDown()) {
			return InteractionResult.PASS;
		}
		if (jar.aspectFilter != null && hit.getDirection() == jar.facing) {
			return this.peelLabel(level, pos, jar, hit);
		}
		// sneak with an empty hand: pour the jar out (a label keeps its aspect)
		jar.amount = 0;
		if (jar.aspectFilter == null) {
			jar.aspect = null;
		}
		jar.markForUpdate();
		level.playSound(player, pos, TVSounds.JAR, SoundSource.BLOCKS, 0.4F, 1.0F);
		level.playSound(player, pos, SoundEvents.GENERIC_SWIM, SoundSource.BLOCKS, 0.5F, 1.0F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.3F);
		return InteractionResult.SUCCESS;
	}

	private InteractionResult peelLabel(Level level, BlockPos pos, JarBlockEntity jar, BlockHitResult hit) {
		jar.aspectFilter = null;
		jar.markForUpdate();
		if (level.isClientSide()) {
			level.playLocalSound(pos, TVSounds.PAGE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
		} else {
			var d = hit.getDirection();
			level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5 + d.getStepX() / 3.0, pos.getY() + 0.5, pos.getZ() + 0.5 + d.getStepZ() / 3.0,
				new ItemStack(TVItems.JAR_LABEL)));
		}
		return InteractionResult.SUCCESS;
	}
}
