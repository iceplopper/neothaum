package dev.thaumvibe.items.eldritch;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Port of TC4 ItemEldritchObject metadata 3 (Primordial Pearl).
 * Rare and powerful nodule of primal matter.
 */
public class PrimordialPearlItem extends Item {
	public PrimordialPearlItem(Item.Properties props) {
		super(props.stacksTo(1).rarity(Rarity.EPIC));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		builder.accept(Component.translatable("item.ItemEldritchObject.text.5").withStyle(ChatFormatting.DARK_PURPLE));
		builder.accept(Component.translatable("item.ItemEldritchObject.text.6").withStyle(ChatFormatting.DARK_PURPLE));
	}
}
