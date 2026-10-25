package dev.thaumvibe.blocks;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.registry.TVComponents;
import dev.thaumvibe.registry.TVItems;
import dev.thaumvibe.registry.TVSounds;
import dev.thaumvibe.tiles.AlembicBlockEntity;
import dev.thaumvibe.tiles.JarBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * BlockMetalDevice metadata 1: the arcane alembic. Knock on it to hear how full it is; sneak with an empty hand to pour it out; label it
 * like a jar; click it with a warded jar to decant it.
 */
public class AlembicBlock extends BaseEntityBlock {
	public AlembicBlock(BlockBehaviour.Properties props) {
		super(props);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new AlembicBlockEntity(pos, state);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if (placer != null && level.getBlockEntity(pos) instanceof AlembicBlockEntity a) {
			a.facing = placer.getDirection().getOpposite();
			a.setChanged();
		}
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
		if (level.getBlockEntity(pos) instanceof AlembicBlockEntity a) {
			float r = (float) a.amount / a.maxAmount;
			return Mth.floor(r * 14.0F) + (a.amount > 0 ? 1 : 0);
		}
		return 0;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof AlembicBlockEntity a)) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (player.isShiftKeyDown() && a.aspectFilter != null) {
			return this.peelLabel(level, pos, a, hit.getDirection());
		}
		if (stack.is(TVItems.JAR_LABEL) && a.aspectFilter == null) {
			Aspect labelled = stack.get(TVComponents.ASPECT_FILTER);
			if (a.amount == 0 && labelled == null) {
				return InteractionResult.SUCCESS;
			}
			if (a.amount == 0) {
				a.aspect = labelled;
			}
			stack.consume(1, player);
			a.aspectFilter = a.aspect;
			a.facing = hit.getDirection().getAxis().isHorizontal() ? hit.getDirection() : a.facing;
			a.markForUpdate();
			level.playSound(player, pos, TVSounds.JAR, SoundSource.BLOCKS, 0.4F, 1.0F);
			return InteractionResult.SUCCESS;
		}
		if (stack.is(TVBlocks.WARDED_JAR.asItem()) || stack.is(TVBlocks.VOID_JAR.asItem())) {
			return this.decant(stack, level, pos, player, a);
		}
		return InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	/** BlockJarItem.onItemUseFirst: a jar clicked on an alembic takes as much of its essentia as it can hold. */
	private InteractionResult decant(ItemStack stack, Level level, BlockPos pos, Player player, AlembicBlockEntity a) {
		if (a.aspect == null || a.amount <= 0 || stack.getCount() != 1) {
			return InteractionResult.PASS;
		}
		AspectList held = stack.get(TVComponents.ASPECTS);
		Aspect filter = stack.get(TVComponents.ASPECT_FILTER);
		Aspect jarAspect = held != null && held.size() > 0 ? held.getAspects()[0] : null;
		int jarAmount = jarAspect == null ? 0 : held.getAmount(jarAspect);
		if (jarAspect != null && jarAspect != a.aspect || filter != null && filter != a.aspect) {
			return InteractionResult.PASS;
		}
		int move = Math.min(JarBlockEntity.MAX - jarAmount, a.amount);
		if (move <= 0) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			Aspect as = a.aspect;
			a.takeFromContainer(as, move);
			stack.set(TVComponents.ASPECTS, new AspectList().add(as, jarAmount + move));
		}
		level.playSound(player, pos, SoundEvents.GENERIC_SWIM, SoundSource.BLOCKS, 0.25F, 1.0F);
		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof AlembicBlockEntity a)) {
			return InteractionResult.PASS;
		}
		if (player.isShiftKeyDown()) {
			if (a.aspectFilter != null) {
				return this.peelLabel(level, pos, a, hit.getDirection());
			}
			a.amount = 0;
			a.aspect = null;
			a.markForUpdate();
			level.playSound(player, pos, TVSounds.ALEMBICKNOCK, SoundSource.BLOCKS, 0.2F, 1.0F);
			level.playSound(player, pos, SoundEvents.GENERIC_SWIM, SoundSource.BLOCKS, 0.5F,
				1.0F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.3F);
			return InteractionResult.SUCCESS;
		}
		if (!level.isClientSide()) {
			String msg;
			if (a.aspect != null && a.amount != 0) {
				msg = a.amount < a.maxAmount * 0.4 ? "tile.alembic.msg.2"
					: a.amount < a.maxAmount * 0.8 ? "tile.alembic.msg.3" : a.amount < a.maxAmount ? "tile.alembic.msg.4" : "tile.alembic.msg.5";
			} else {
				msg = "tile.alembic.msg.1";
			}
			player.sendSystemMessage(Component.translatable(msg).withStyle(ChatFormatting.DARK_AQUA));
			level.playSound(null, pos, TVSounds.ALEMBICKNOCK, SoundSource.BLOCKS, 0.2F, 1.0F);
		}
		return InteractionResult.SUCCESS;
	}

	private InteractionResult peelLabel(Level level, BlockPos pos, AlembicBlockEntity a, Direction side) {
		a.aspectFilter = null;
		a.markForUpdate();
		if (level.isClientSide()) {
			level.playLocalSound(pos, TVSounds.PAGE, SoundSource.BLOCKS, 1.0F, 1.1F, false);
		} else {
			level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5 + side.getStepX() / 3.0, pos.getY() + 0.5, pos.getZ() + 0.5 + side.getStepZ() / 3.0,
				new ItemStack(TVItems.JAR_LABEL)));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		// breakBlock: the label comes off with it
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof AlembicBlockEntity a && a.aspectFilter != null) {
			level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(TVItems.JAR_LABEL)));
		}
		return super.playerWillDestroy(level, pos, state, player);
	}
}
