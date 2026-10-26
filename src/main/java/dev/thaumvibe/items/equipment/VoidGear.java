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
 * The void metal family: ItemVoidSword/Axe/Pickaxe/Shovel/Hoe, ItemVoidArmor and the primal crusher. They mend themselves a point every
 * second while carried, weaken what they strike, and carry warp.
 */
public final class VoidGear {
	/** onUpdate / onArmorTick: one point of durability back every 20 ticks. */
	public static void selfRepair(ItemStack stack, Entity owner) {
		if (stack.isDamaged() && owner instanceof LivingEntity && owner.tickCount % 20 == 0) {
			stack.setDamageValue(stack.getDamageValue() - 1);
		}
	}

	/** hitEntity / onLeftClickEntity: weakness on the target (players only where PvP is on). */
	public static void weaken(LivingEntity target, LivingEntity attacker, int ticks) {
		if (target.level() instanceof ServerLevel sl && (!(target instanceof Player) || !(attacker instanceof Player) || sl.isPvpAllowed())) {
			target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, ticks));
		}
	}

	/** EventHandlerRunic.getWarpFromGear: what the held item, the armour and the baubles add. */
	public static int warpFromGear(Player player) {
		int w = 0;
		ItemStack held = player.getMainHandItem();
		if (held.getItem() instanceof WarpingGear g) {
			w += g.getWarp(held, player);
		}
		for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
			ItemStack s = player.getItemBySlot(slot);
			if (s.getItem() instanceof WarpingGear g) {
				w += g.getWarp(s, player);
			}
		}
		return w;
	}

	public static void warpTooltip(int warp, Consumer<Component> builder) {
		builder.accept(Component.translatable("item.warping").append(" " + warp).withStyle(ChatFormatting.DARK_PURPLE));
	}

	/** A void tool: any tool shape, weakening for 80 ticks (the sword 60), warp 1. */
	public static class Tool extends Item implements WarpingGear {
		private final int weakness;

		public Tool(int weakness, Item.Properties props) {
			super(props);
			this.weakness = weakness;
		}

		@Override
		public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, EquipmentSlot slot) {
			selfRepair(stack, owner);
		}

		@Override
		public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
			super.hurtEnemy(stack, target, attacker);
			weaken(target, attacker, this.weakness);
		}

		@Override
		public int getWarp(ItemStack stack, Player player) {
			return 1;
		}

		@Override
		public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
			warpTooltip(1, builder);
		}
	}

	/** ItemVoidArmor: runic augments take to it. */
	public static class Armor extends Item implements WarpingGear, dev.thaumvibe.api.RunicArmor {
		public Armor(Item.Properties props) {
			super(props);
		}

		@Override
		public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, EquipmentSlot slot) {
			selfRepair(stack, owner);
		}

		@Override
		public int getWarp(ItemStack stack, Player player) {
			return 1;
		}

		@Override
		public int getRunicCharge(ItemStack stack) {
			return 0;
		}

		@Override
		public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
			warpTooltip(1, builder);
		}
	}

	private VoidGear() {
	}
}
