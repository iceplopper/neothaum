package dev.thaumvibe.items.equipment;

import dev.thaumvibe.api.RunicArmor;
import dev.thaumvibe.items.RevealerGear;
import dev.thaumvibe.registry.TVComponents;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Port of ItemFortressArmor:
 * Exceptional thaumium plate offering superior defense, fire/magic resistance,
 * set bonuses, and helmet upgrades (Goggles of Revealing, Grinning Devil, Angry Ghost, Sipping Fiend masks).
 */
public class FortressArmorItem extends Item implements RevealerGear, RunicArmor {
	private final EquipmentSlot slot;

	public FortressArmorItem(EquipmentSlot slot, Properties props) {
		super(props);
		this.slot = slot;
	}

	public EquipmentSlot getSlot() {
		return this.slot;
	}

	@Override
	public boolean showNodes(ItemStack stack) {
		return Boolean.TRUE.equals(stack.get(TVComponents.GOGGLES));
	}

	@Override
	public int getRunicCharge(ItemStack stack) {
		return 0;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		if (Boolean.TRUE.equals(stack.get(TVComponents.GOGGLES))) {
			builder.accept(Component.translatable("item.ItemGoggles.name").withStyle(ChatFormatting.DARK_PURPLE));
		}
		Integer mask = stack.get(TVComponents.MASK);
		if (mask != null) {
			builder.accept(Component.translatable("item.HelmetFortress.mask." + mask).withStyle(ChatFormatting.GOLD));
		}
	}
}
