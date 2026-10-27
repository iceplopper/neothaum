package dev.neothaum.golem;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Port of ItemGolemCore:
 * Golem animation cores that impart specialized behaviors and goals.
 */
public class GolemCoreItem extends Item {
	private final GolemCoreType coreType;

	public GolemCoreItem(GolemCoreType type, Properties props) {
		super(props);
		this.coreType = type;
	}

	public GolemCoreType getCoreType() {
		return this.coreType;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		builder.accept(Component.translatable("item.ItemGolemCore." + this.coreType.id + ".desc").withStyle(ChatFormatting.GRAY));
	}
}
