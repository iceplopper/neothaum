package dev.thaumvibe.devtools;

import dev.thaumvibe.ClientBridge;
import dev.thaumvibe.tiles.NodeBlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/** Dev tool: right-click a node to open a screen for setting its type, modifier and aspects, or deleting it. */
public class NodeEditorItem extends Item {
	public NodeEditorItem(Item.Properties props) {
		super(props);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		if (ctx.getPlayer() == null || !ctx.getPlayer().isCreative() || !(ctx.getLevel().getBlockEntity(ctx.getClickedPos()) instanceof NodeBlockEntity)) {
			return InteractionResult.PASS;
		}
		if (ctx.getLevel().isClientSide()) {
			ClientBridge.openNodeEditor.accept(ctx.getClickedPos());
		}
		return InteractionResult.SUCCESS;
	}
}
