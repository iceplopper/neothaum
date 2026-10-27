package dev.neothaum.items;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.registry.TVComponents;
import dev.neothaum.registry.TVItems;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Port of ItemCrystalEssence: a solid crystal of crystallized essentia holding 1 point of a single aspect.
 */
public class CrystalEssenceItem extends Item {
	public CrystalEssenceItem(Properties props) {
		super(props);
	}

	public static ItemStack of(Aspect aspect) {
		ItemStack stack = new ItemStack(TVItems.CRYSTAL_ESSENCE);
		stack.set(TVComponents.ASPECTS, new AspectList().add(aspect, 1));
		return stack;
	}

	public static Aspect aspectOf(ItemStack stack) {
		AspectList al = stack.get(TVComponents.ASPECTS);
		return al == null || al.size() == 0 ? null : al.getAspects()[0];
	}

	@Override
	public Component getName(ItemStack stack) {
		Aspect a = aspectOf(stack);
		return a == null ? super.getName(stack) : Component.literal(a.getName() + " ").append(Component.translatable("item.neothaum.crystal_essence"));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		AspectList al = stack.get(TVComponents.ASPECTS);
		if (al != null && al.size() > 0) {
			for (Aspect a : al.getAspectsSorted()) {
				builder.accept(Component.literal(a.getName() + " x" + al.getAmount(a)));
			}
		}
	}
}
