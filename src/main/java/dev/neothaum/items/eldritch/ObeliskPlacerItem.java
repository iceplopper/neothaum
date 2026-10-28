package dev.neothaum.items.eldritch;

import dev.neothaum.blocks.eldritch.EldritchObeliskBlock;
import dev.neothaum.registry.TVBlocks;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** ItemEldritchObject meta 4, the Eldritch Obelisk Placer: on top of a block with six clear blocks above, raise an altar and obelisk. */
public class ObeliskPlacerItem extends Item {
	public ObeliskPlacerItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		if (ctx.getClickedFace() != Direction.UP) {
			return InteractionResult.PASS;
		}
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		for (int a = 1; a <= 6; a++) {
			if (!level.isEmptyBlock(pos.above(a))) {
				return InteractionResult.FAIL;
			}
		}
		if (!level.isClientSide()) {
			level.setBlock(pos.above(1), TVBlocks.ELDRITCH_ALTAR.defaultBlockState(), 3);
			level.setBlock(pos.above(3), TVBlocks.ELDRITCH_OBELISK.defaultBlockState(), 3);
			BlockState shaft = TVBlocks.ELDRITCH_OBELISK.defaultBlockState().setValue(EldritchObeliskBlock.BASE, false);
			for (int y = 4; y <= 7; y++) {
				level.setBlock(pos.above(y), shaft, 3);
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		builder.accept(Component.literal("§oCreative Mode Only"));
	}
}
