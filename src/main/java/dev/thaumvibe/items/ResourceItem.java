package dev.thaumvibe.items;

import dev.thaumvibe.registry.TVMobEffects;
import dev.thaumvibe.research.ResearchManager;
import java.util.Random;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * ItemResource's restless members: tainted goo and taint tendrils poison whoever carries them (and rot away); the primal charm hums with the
 * primal aspects and now and then whispers about a focus.
 */
public class ResourceItem extends Item {
	public enum Kind { TAINT, PRIMAL_CHARM }

	private final Kind kind;

	public ResourceItem(Kind kind, Properties props) {
		super(props);
		this.kind = kind;
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
		if (this.kind == Kind.TAINT) {
			if (entity instanceof LivingEntity le && !le.isInvertedHealAndHarm() && !le.hasEffect(TVMobEffects.FLUX_TAINT)
				&& level.getRandom().nextInt(4321) <= stack.getCount()) {
				le.addEffect(new MobEffectInstance(TVMobEffects.FLUX_TAINT, 120, 0));
				if (entity instanceof Player player) {
					player.sendSystemMessage(Component.translatable("tc.taint_item_poison",
						Component.literal("§5§o" + stack.getHoverName().getString() + "§r")));
					stack.shrink(1);
				}
			}
		} else if (this.kind == Kind.PRIMAL_CHARM) {
			int r = level.getRandom().nextInt(20000);
			// r < 20 drops an aspect orb of a random primal, once aspect orbs are in
			if (r == 42 && entity instanceof ServerPlayer player && !ResearchManager.isResearchComplete(player, "FOCUSPRIMAL")
				&& !ResearchManager.isResearchComplete(player, "@FOCUSPRIMAL")) {
				player.sendSystemMessage(Component.literal("§5§o" + Component.translatable("tc.primalcharm.trigger").getString()));
				ResearchManager.completeResearch(player, "@FOCUSPRIMAL");
			}
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		if (this.kind == Kind.PRIMAL_CHARM) {
			Random rand = new Random(stack.hashCode() + System.currentTimeMillis() / 6000L);
			if (rand.nextInt(200) < 25) {
				builder.accept(Component.literal("§6" + Component.translatable("tc.primalcharm." + rand.nextInt(5)).getString()));
			}
		}
	}
}
