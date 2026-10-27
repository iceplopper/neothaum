package dev.neothaum.items.equipment;

import dev.neothaum.api.RunicArmor;
import dev.neothaum.api.WarpingGear;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.items.RevealerGear;
import dev.neothaum.items.VisDiscountGear;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Port of ItemVoidRobeArmor:
 * High-tier eldritch robes forged from void metal and enchanted fabric.
 * Provides self-repair, 5% vis discount per piece, 2 warp per piece,
 * runic shielding capacity, and the helmet possesses built-in Revealer goggles.
 */
public class VoidRobeArmorItem extends Item implements WarpingGear, VisDiscountGear, RevealerGear, RunicArmor {
	public static final int DEFAULT_COLOR = 6961280;
	private final EquipmentSlot slot;

	public VoidRobeArmorItem(EquipmentSlot slot, Properties props) {
		super(props);
		this.slot = slot;
	}

	public EquipmentSlot getSlot() {
		return this.slot;
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, EquipmentSlot slot) {
		VoidGear.selfRepair(stack, owner);
	}

	@Override
	public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) {
		return 5;
	}

	@Override
	public int getWarp(ItemStack stack, Player player) {
		return 2;
	}

	@Override
	public boolean showNodes(ItemStack stack) {
		return this.slot == EquipmentSlot.HEAD;
	}

	@Override
	public int getRunicCharge(ItemStack stack) {
		return 0;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		if (this.slot == EquipmentSlot.HEAD) {
			builder.accept(Component.translatable("item.ItemGoggles.name").withStyle(ChatFormatting.DARK_PURPLE));
		}
		builder.accept(Component.translatable("tc.visdiscount").withStyle(ChatFormatting.DARK_PURPLE)
			.append(": " + this.getVisDiscount(stack, null, null) + "%"));
		VoidGear.warpTooltip(2, builder);
	}
}
