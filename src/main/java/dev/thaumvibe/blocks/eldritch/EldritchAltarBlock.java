package dev.thaumvibe.blocks.eldritch;

import dev.thaumvibe.items.WandItem;
import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.registry.TVItems;
import dev.thaumvibe.tiles.eldritch.EldritchAltarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Eldritch Altar pedestal holding 4 eye sockets beneath an obelisk.
 * Accepting 4 Eldritch Eyes and a wand strike activates the portal to the Outer Lands.
 */
public class EldritchAltarBlock extends BaseEntityBlock {

	public EldritchAltarBlock(Properties props) {
		super(props);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new EldritchAltarBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof EldritchAltarBlockEntity altar) {
			if (stack.is(TVItems.ELDRITCH_EYE)) {
				return altar.insertEye(player, stack);
			}
			if (stack.getItem() instanceof WandItem) {
				return altar.activateWand(player, stack);
			}
		}
		return InteractionResult.PASS;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, TVBlockEntities.ELDRITCH_ALTAR, EldritchAltarBlockEntity::tick);
	}
}
