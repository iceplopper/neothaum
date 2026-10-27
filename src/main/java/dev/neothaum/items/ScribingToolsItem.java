package dev.neothaum.items;

import dev.neothaum.registry.TVBlocks;
import dev.neothaum.tiles.ResearchTableBlockEntity;
import dev.neothaum.blocks.ResearchTableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Port of ItemInkwell: using it on one of two adjacent tables turns them into a research table. */
public class ScribingToolsItem extends Item {
	public ScribingToolsItem(Item.Properties props) {
		super(props);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		Player player = ctx.getPlayer();
		if (!level.getBlockState(pos).is(TVBlocks.TABLE)) {
			return InteractionResult.PASS;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		for (Direction dir : Direction.Plane.HORIZONTAL) {
			BlockPos other = pos.relative(dir);
			if (level.getBlockState(other).is(TVBlocks.TABLE)) {
				ResearchTableBlock.place(level, pos, dir);
				if (level.getBlockEntity(pos) instanceof ResearchTableBlockEntity rt) {
					ItemStack ink = ctx.getItemInHand().copyWithCount(1);
					rt.setItem(0, ink);
					if (player == null || !player.isCreative()) {
						ctx.getItemInHand().shrink(1);
					}
					rt.markForUpdate();
				}
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}
}
