package dev.thaumvibe.devtools;

import dev.thaumvibe.api.nodes.NodeType;
import dev.thaumvibe.world.NodeGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/** Dev tool: places an aura node of one type on the clicked face, with random aspects generated the way world generation does. */
public class NodePlacerItem extends Item {
	private final NodeType type;

	public NodePlacerItem(NodeType type, Item.Properties props) {
		super(props);
		this.type = type;
	}

	@Override
	public Component getName(net.minecraft.world.item.ItemStack stack) {
		return Component.translatable("item.thaumvibe.dev_node_placer", Component.translatable("nodetype." + this.type.name() + ".name"));
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		if (ctx.getPlayer() == null || !ctx.getPlayer().isCreative()) {
			return InteractionResult.FAIL;
		}
		BlockPos pos = ctx.getLevel().getBlockState(ctx.getClickedPos()).canBeReplaced() ? ctx.getClickedPos() : ctx.getClickedPos().relative(ctx.getClickedFace());
		if (ctx.getLevel() instanceof ServerLevel level) {
			NodeGenerator.createRandomNodeAt(level, pos, level.getRandom(), false, false, false, true, this.type);
		}
		return InteractionResult.SUCCESS;
	}
}
