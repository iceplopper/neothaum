package dev.thaumvibe.items;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.api.nodes.NodeModifier;
import dev.thaumvibe.api.nodes.NodeType;
import dev.thaumvibe.registry.TVComponents;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

/** Port of ItemJarNode: carries a trapped node (its aspects, type and modifier) and places it back as a node in a jar. */
public class JarNodeItem extends BlockItem {
	/** Whether the local player has discovered an aspect; the client sets this (servers never build tooltips). */
	public static java.util.function.Predicate<Aspect> DISCOVERED = a -> true;

	public JarNodeItem(Block block, Item.Properties props) {
		super(block, props);
	}

	public static NodeType getNodeType(ItemStack stack) {
		return NodeType.values()[stack.getOrDefault(TVComponents.NODE_TYPE, 0)];
	}

	public static NodeModifier getNodeModifier(ItemStack stack) {
		int mod = stack.getOrDefault(TVComponents.NODE_MODIFIER, -1);
		return mod >= 0 ? NodeModifier.values()[mod] : null;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		AspectList aspects = stack.get(TVComponents.ASPECTS);
		if (aspects == null || aspects.size() == 0) {
			return;
		}
		MutableComponent desc = Component.translatable("nodetype." + getNodeType(stack) + ".name");
		if (getNodeModifier(stack) != null) {
			desc.append(", ").append(Component.translatable("nodemod." + getNodeModifier(stack) + ".name"));
		}
		builder.accept(desc.withStyle(ChatFormatting.BLUE));
		for (Aspect tag : aspects.getAspectsSorted()) {
			if (DISCOVERED.test(tag)) {
				builder.accept(Component.literal(tag.getName() + " x " + aspects.getAmount(tag)));
			} else {
				builder.accept(Component.translatable("tc.aspect.unknown"));
			}
		}
	}
}
