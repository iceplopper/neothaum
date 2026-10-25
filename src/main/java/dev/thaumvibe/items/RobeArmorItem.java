package dev.thaumvibe.items;

import dev.thaumvibe.api.aspects.Aspect;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** Port of ItemRobeArmor: dyeable thaumaturge's robes (purple by default) that lower wand vis costs a little. */
public class RobeArmorItem extends Item implements VisDiscountGear, dev.thaumvibe.api.RunicArmor {
	public static final int DEFAULT_COLOR = 6961280;
	private final EquipmentSlot slot;

	public RobeArmorItem(EquipmentSlot slot, Item.Properties props) {
		super(props);
		this.slot = slot;
	}

	@Override
	public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) {
		return this.slot == EquipmentSlot.FEET ? 1 : 2;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		builder.accept(Component.translatable("tc.visdiscount").withStyle(ChatFormatting.DARK_PURPLE)
			.append(": " + this.getVisDiscount(stack, null, null) + "%"));
	}

	@Override
	public int getRunicCharge(ItemStack stack) {
		return 0;
	}
}
