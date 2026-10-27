package dev.thaumvibe.items.equipment;

import dev.thaumvibe.api.RunicArmor;
import dev.thaumvibe.api.WarpingGear;
import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.items.VisDiscountGear;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Port of ItemCultistBoots:
 * Heavy iron boots worn by Crimson Cultists.
 * Grants 1% vis discount and 1 warp.
 */
public class CultistBootsItem extends Item implements WarpingGear, VisDiscountGear, RunicArmor {
	public CultistBootsItem(Properties props) {
		super(props);
	}

	@Override
	public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) {
		return 1;
	}

	@Override
	public int getWarp(ItemStack stack, Player player) {
		return 1;
	}

	@Override
	public int getRunicCharge(ItemStack stack) {
		return 0;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		builder.accept(Component.translatable("tc.visdiscount").withStyle(ChatFormatting.DARK_PURPLE)
			.append(": " + this.getVisDiscount(stack, null, null) + "%"));
		VoidGear.warpTooltip(1, builder);
	}
}
