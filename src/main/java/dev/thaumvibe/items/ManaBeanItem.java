package dev.thaumvibe.items;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.network.TVNetwork;
import dev.thaumvibe.registry.TVComponents;
import dev.thaumvibe.research.PlayerKnowledge;
import dev.thaumvibe.research.ResearchManager;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/**
 * ItemManaBean: a bean of pure aspect, tinted its colour. Eating one gives a random potion effect, and one time in four teaches a point of its
 * aspect. Pechs prize them. (Planting them on magical trees to grow mana pods comes with the pods.)
 */
public class ManaBeanItem extends Item {
	public ManaBeanItem(Properties props) {
		super(props);
	}

	public static ItemStack of(Aspect aspect) {
		ItemStack stack = new ItemStack(dev.thaumvibe.registry.TVItems.MANA_BEAN);
		stack.set(TVComponents.ASPECTS, new AspectList().add(aspect, 1));
		return stack;
	}

	private static Aspect randomAspect(net.minecraft.util.RandomSource r) {
		List<Aspect> all = new ArrayList<>(Aspect.aspects.values());
		return all.get(r.nextInt(all.size()));
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
		if (!stack.has(TVComponents.ASPECTS)) {
			stack.set(TVComponents.ASPECTS, new AspectList().add(randomAspect(level.getRandom()), 1));
		}
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		if (level instanceof ServerLevel sl) {
			List<Holder.Reference<MobEffect>> effects = BuiltInRegistries.MOB_EFFECT.listElements().toList();
			Holder<MobEffect> p = effects.get(level.getRandom().nextInt(effects.size()));
			if (p.value().isInstantaneous()) {
				p.value().applyInstantaneousEffect(sl, entity, entity, entity, 2, 3.0);
			} else {
				entity.addEffect(new MobEffectInstance(p, 160 + level.getRandom().nextInt(80), 0));
			}
			AspectList al = stack.get(TVComponents.ASPECTS);
			if (entity instanceof ServerPlayer player && level.getRandom().nextFloat() < 0.25F && al != null && al.size() > 0) {
				Aspect a = al.getAspects()[0];
				PlayerKnowledge k = ResearchManager.knowledge(player).copy();
				if (k.addAspectPool(a, 1)) {
					ResearchManager.setKnowledge(player, k);
					TVNetwork.sendAspectPool(player, a, 1, k.getAspectPool(a));
				}
			}
		}
		return super.finishUsingItem(stack, level, entity);
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
