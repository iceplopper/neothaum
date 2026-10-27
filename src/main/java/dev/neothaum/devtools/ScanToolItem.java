package dev.neothaum.devtools;

import dev.neothaum.research.ScanManager;
import dev.neothaum.tiles.NodeBlockEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/** Dev tool: an instant thaumometer. Click a block, node or mob to scan it at once (with the normal research rewards). */
public class ScanToolItem extends Item {
	public ScanToolItem(Item.Properties props) {
		super(props);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		if (!(ctx.getPlayer() instanceof ServerPlayer sp) || !sp.isCreative()) {
			return ctx.getPlayer() != null && ctx.getPlayer().isCreative() ? InteractionResult.SUCCESS : InteractionResult.PASS;
		}
		var level = ctx.getLevel();
		var pos = ctx.getClickedPos();
		ScanManager.ScanResult scan;
		if (level.getBlockEntity(pos) instanceof NodeBlockEntity) {
			scan = ScanManager.ScanResult.node(pos);
		} else {
			ItemStack is = level.getBlockState(pos).getCloneItemStack(level, pos, false);
			if (is.isEmpty()) {
				return InteractionResult.PASS;
			}
			scan = ScanManager.ScanResult.item(is.getItem());
		}
		ScanManager.completeScan(sp, scan, "@");
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
		if (player instanceof ServerPlayer sp && sp.isCreative()) {
			ScanManager.completeScan(sp, ScanManager.ScanResult.entity(target), "@");
		}
		return InteractionResult.SUCCESS;
	}
}
