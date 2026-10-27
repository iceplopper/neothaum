package dev.thaumvibe.golem;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Port of ItemGolemDecoration:
 * Aesthetic and functional accessories (hats, spectacles, bowties, armor, etc.) worn by golems.
 */
public class GolemDecorationItem extends Item {
	private final GolemDecorationType decorationType;

	public GolemDecorationItem(GolemDecorationType type, Properties props) {
		super(props);
		this.decorationType = type;
	}

	public GolemDecorationType getDecorationType() {
		return this.decorationType;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		builder.accept(Component.translatable("item.ItemGolemDecoration." + this.decorationType.id + ".desc").withStyle(ChatFormatting.GRAY));
	}
}
