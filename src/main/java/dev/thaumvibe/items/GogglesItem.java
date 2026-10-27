package dev.thaumvibe.items;

import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.api.aspects.Aspect;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;

/** Port of ItemGoggles: Goggles of Revealing. Shows aspects/nodes and gives a 5% vis discount. */
public class GogglesItem extends Item implements VisDiscountGear, RevealerGear, dev.thaumvibe.api.RunicArmor {
	public static final ResourceKey<EquipmentAsset> ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, Thaumvibe.id("goggles"));

	public GogglesItem(Item.Properties props) {
		super(props);
	}

	public static Item.Properties properties() {
		return new Item.Properties().stacksTo(1).durability(275).rarity(Rarity.RARE).enchantable(25)
			.component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).setAsset(ASSET).build())
			.attributes(ItemAttributeModifiers.builder()
				.add(Attributes.ARMOR, new AttributeModifier(Thaumvibe.id("goggles_armor"), 1.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD)
				.build());
	}

	public static boolean isWearingGoggles(Player player) {
		if (player == null) return false;
		ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
		return head.getItem() instanceof RevealerGear r && r.showNodes(head);
	}

	@Override
	public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) {
		return 5;
	}

	@Override
	public boolean showNodes(ItemStack stack) {
		return true;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		builder.accept(Component.translatable("tc.visdiscount").withStyle(ChatFormatting.DARK_PURPLE).append(": 5%"));
	}

	@Override
	public int getRunicCharge(ItemStack stack) {
		return 0;
	}
}
