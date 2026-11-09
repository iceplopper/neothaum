package dev.neothaum.addon.vessels;

import dev.neothaum.items.WandItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A vessel set down: the jar is the block model, what it holds is drawn turning inside. Use it with a wand to let it out. */
public class VesselBlock extends BaseEntityBlock {
	private static final VoxelShape SHAPE = Shapes.or(box(3, 0, 3, 13, 12, 13), box(5, 12, 5, 11, 14, 11));
	public final Vessels.Tier tier;

	public VesselBlock(Vessels.Tier tier, BlockBehaviour.Properties props) {
		super(props);
		this.tier = tier;
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
		return new VesselBlockEntity(pos, state);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult hit) {
		if (!(stack.getItem() instanceof WandItem) || !(level.getBlockEntity(pos) instanceof VesselBlockEntity vessel)) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (vessel.isEmpty()) {
			return InteractionResult.PASS;
		}
		if (level instanceof ServerLevel sl) {
			if (!level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()) {
				player.sendOverlayMessage(Component.translatable("neothaum.vessel.no_room").withStyle(ChatFormatting.DARK_PURPLE));
				return InteractionResult.FAIL;
			}
			vessel.release(sl);
		}
		return InteractionResult.SUCCESS;
	}
}
