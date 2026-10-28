package dev.neothaum.items;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.baubles.BaubleType;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** ItemBaubleBlanks 3-8: an Apprentice's Ring of one primal aspect, tinted its colour, taking 1% off that aspect's vis costs. */
public class ApprenticeRingItem extends BaubleBlankItem implements VisDiscountGear {
	private final Aspect aspect;

	public ApprenticeRingItem(Aspect aspect, Item.Properties props) {
		super(BaubleType.RING, props);
		this.aspect = aspect;
	}

	public Aspect aspect() {
		return this.aspect;
	}

	@Override
	public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) {
		return aspect == this.aspect ? 1 : 0;
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable("item.neothaum.apprentice_ring", this.aspect.getDisplayName());
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		builder.accept(Component.literal(this.aspect.getName() + " ").append(Component.translatable("tc.discount")).append(": 1%")
			.withStyle(ChatFormatting.DARK_PURPLE));
	}
}
