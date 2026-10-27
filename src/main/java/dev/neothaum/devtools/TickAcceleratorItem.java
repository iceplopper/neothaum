package dev.neothaum.devtools;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Dev tool: use on a machine to run 200 ticks of it at once (sneak: 1200); on anything without a ticker, give it 50 random ticks instead (plants,
 * taint, flux).
 */
public class TickAcceleratorItem extends Item {
	public TickAcceleratorItem(Item.Properties props) {
		super(props);
	}

	@Override
	@SuppressWarnings("unchecked")
	public InteractionResult useOn(UseOnContext ctx) {
		Player player = ctx.getPlayer();
		if (player == null || !player.isCreative()) {
			return InteractionResult.FAIL;
		}
		if (!(ctx.getLevel() instanceof ServerLevel level)) {
			return InteractionResult.SUCCESS;
		}
		BlockPos pos = ctx.getClickedPos();
		BlockState state = level.getBlockState(pos);
		BlockEntity be = level.getBlockEntity(pos);
		int ticks = player.isShiftKeyDown() ? 1200 : 200;
		BlockEntityTicker<BlockEntity> ticker = be == null ? null
			: (BlockEntityTicker<BlockEntity>) state.getTicker(level, (net.minecraft.world.level.block.entity.BlockEntityType<BlockEntity>) be.getType());
		if (ticker != null) {
			for (int i = 0; i < ticks && !be.isRemoved(); i++) {
				ticker.tick(level, pos, level.getBlockState(pos), be);
			}
			player.sendOverlayMessage(Component.literal("[Dev] Ran " + ticks + " ticks").withStyle(ChatFormatting.LIGHT_PURPLE));
		} else {
			for (int i = 0; i < 50; i++) {
				BlockState s = level.getBlockState(pos);
				if (s.isRandomlyTicking()) {
					s.randomTick(level, pos, level.getRandom());
				}
			}
			player.sendOverlayMessage(Component.literal("[Dev] 50 random ticks").withStyle(ChatFormatting.LIGHT_PURPLE));
		}
		return InteractionResult.SUCCESS;
	}
}
