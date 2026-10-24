package dev.thaumvibe.items;

import dev.thaumvibe.registry.TVBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** ItemResource meta 1 (Nitor): places a floating nitor light against the clicked face. */
public class NitorItem extends Item {
	public NitorItem(Item.Properties props) {
		super(props);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		if (!level.getBlockState(pos).canBeReplaced()) {
			pos = pos.relative(ctx.getClickedFace());
		}
		if (ctx.getPlayer() != null && !ctx.getPlayer().mayUseItemAt(pos, ctx.getClickedFace(), ctx.getItemInHand())) {
			return InteractionResult.FAIL;
		}
		if (!level.getBlockState(pos).canBeReplaced()) {
			return InteractionResult.FAIL;
		}
		if (!level.isClientSide()) {
			level.setBlock(pos, TVBlocks.NITOR_LIGHT.defaultBlockState(), 3);
			level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
			if (ctx.getPlayer() == null || !ctx.getPlayer().isCreative()) {
				ctx.getItemInHand().shrink(1);
			}
		}
		return InteractionResult.SUCCESS;
	}
}
