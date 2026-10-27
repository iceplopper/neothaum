package dev.thaumvibe.items.equipment;

import dev.thaumvibe.api.WarpingGear;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Port of ItemCrimsonSword (Crimson Blade):
 * Jagged crimson steel blade wielded by Crimson Knights and Praetors.
 * Saps the target's vitality on hit (Weakness and Hunger), self-repairs, and harbors warp.
 */
public class CrimsonSwordItem extends Item implements WarpingGear {
	public CrimsonSwordItem(Properties props) {
		super(props);
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, EquipmentSlot slot) {
		VoidGear.selfRepair(stack, owner);
	}

	@Override
	public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.hurtEnemy(stack, target, attacker);
		if (target.level() instanceof ServerLevel sl && (!(target instanceof Player) || !(attacker instanceof Player) || sl.isPvpAllowed())) {
			target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60));
			target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120));
		}
	}

	@Override
	public int getWarp(ItemStack stack, Player player) {
		return 2;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		builder.accept(Component.translatable("enchantment.special.sapgreat").withStyle(ChatFormatting.GOLD));
		VoidGear.warpTooltip(2, builder);
	}
}
