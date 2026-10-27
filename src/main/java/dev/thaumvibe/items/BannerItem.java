package dev.thaumvibe.items;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.registry.TVComponents;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Port of TC4 BlockWoodenDeviceItem metadata 8:
 * Item representation for Thaumcraft dyed banners and Cultist banners.
 */
public class BannerItem extends BlockItem {
	private final boolean cultist;

	public BannerItem(Item.Properties props, boolean cultist) {
		super(TVBlocks.BANNER, props);
		this.cultist = cultist;
	}

	public static ItemStack ofColor(int color) {
		ItemStack stack = new ItemStack(dev.thaumvibe.registry.TVItems.BANNER);
		stack.set(TVComponents.BANNER_COLOR, color);
		return stack;
	}

	@Override
	public Component getName(ItemStack stack) {
		if (this.cultist || stack.getOrDefault(TVComponents.BANNER_COLOR, -1) == -1) {
			return Component.translatable("item.thaumvibe.cultist_banner");
		}
		int color = stack.getOrDefault(TVComponents.BANNER_COLOR, 0);
		if (color >= 0 && color < 16) {
			DyeColor dye = DyeColor.byId(color);
			return Component.translatable("item.thaumvibe.banner." + dye.getName());
		}
		return Component.translatable("item.thaumvibe.banner");
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		Aspect a = stack.get(TVComponents.ASPECT_FILTER);
		if (a != null) {
			builder.accept(Component.literal("§5" + a.getName()));
		}
		if (this.cultist) {
			builder.accept(Component.translatable("item.thaumvibe.cultist_banner.desc").withStyle(ChatFormatting.DARK_PURPLE));
		}
	}
}
