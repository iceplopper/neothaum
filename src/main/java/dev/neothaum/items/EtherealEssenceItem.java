package dev.neothaum.items;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.registry.TVComponents;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** ItemWispEssence: what is left of a wisp, two points of its aspect, tinted its colour. */
public class EtherealEssenceItem extends Item {
	public EtherealEssenceItem(Properties props) {
		super(props);
	}

	public static ItemStack of(Aspect aspect) {
		ItemStack stack = new ItemStack(dev.neothaum.registry.TVItems.ETHEREAL_ESSENCE);
		stack.set(TVComponents.ASPECTS, new AspectList().add(aspect, 2));
		return stack;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		AspectList al = stack.get(TVComponents.ASPECTS);
		if (al != null) {
			for (Aspect a : al.getAspectsSorted()) {
				builder.accept(Component.literal(a.getName() + " x" + al.getAmount(a)));
			}
		}
	}
}
