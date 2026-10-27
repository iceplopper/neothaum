package dev.neothaum.items.equipment;

import dev.neothaum.NeoThaum;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.aspects.ObjectAspects;
import dev.neothaum.research.ResearchManager;
import dev.neothaum.wands.WandManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;

/**
 * Config.enchHaste and Config.enchRepair, TC4's two working enchantments: Haste speeds the wearer's boots along, and Repair mends Thaumcraft
 * gear in the hotbar and armour slots every two seconds, paying vis from a wand or amulet (EventHandlerEntity.doRepair and updateSpeed).
 */
public final class TCEnchantments {
	public static final ResourceKey<Enchantment> HASTE = ResourceKey.create(Registries.ENCHANTMENT, NeoThaum.id("haste"));
	public static final ResourceKey<Enchantment> REPAIR = ResourceKey.create(Registries.ENCHANTMENT, NeoThaum.id("repair"));
	/** IRepairable. */
	public static final TagKey<Item> REPAIRABLE = TagKey.create(Registries.ITEM, NeoThaum.id("repairable"));

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer p : server.getPlayerList().getPlayers()) {
				if (p.tickCount % 40 == 0 && !p.isCreative()) {
					for (int a = 0; a < 9; a++) {
						doRepair(p.getInventory().getItem(a), p);
					}
					for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
						doRepair(p.getItemBySlot(slot), p);
					}
				}
			}
		});
	}

	public static int level(Player p, ItemStack stack, ResourceKey<Enchantment> key) {
		Holder<Enchantment> h = p.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key).orElse(null);
		return h == null ? 0 : EnchantmentHelper.getItemEnchantmentLevel(h, stack);
	}

	/** EventHandlerEntity.doRepair: one point of durability per level, for vis worth the root of twice each primal in the item's aspects. */
	public static void doRepair(ItemStack is, Player player) {
		if (is.isEmpty() || !is.isDamaged() || !is.is(REPAIRABLE)) {
			return;
		}
		int level = Math.min(2, level(player, is, REPAIR));
		if (level <= 0) {
			return;
		}
		AspectList cost = ObjectAspects.getAspects(is);
		if (cost == null || cost.size() == 0) {
			return;
		}
		cost = ResearchManager.reduceToPrimals(cost);
		AspectList finalCost = new AspectList();
		for (Aspect a : cost.getAspects()) {
			finalCost.merge(a, (int) Math.sqrt(cost.getAmount(a) * 2) * level);
		}
		if (WandManager.consumeVisFromInventory(player, finalCost)) {
			is.setDamageValue(Math.max(0, is.getDamageValue() - level));
		}
	}

	/** EventHandlerEntity.updateSpeed: Haste boots push a walking player forward (half as much in the air or water). */
	public static void updateSpeed(Player player) {
		if (player.getAbilities().flying || player.zza <= 0.0F) {
			return;
		}
		int haste = level(player, player.getItemBySlot(EquipmentSlot.FEET), HASTE);
		if (haste <= 0) {
			return;
		}
		float bonus = haste * 0.015F;
		if (!player.onGround()) {
			bonus /= 2.0F;
		}
		if (player.isInWater()) {
			bonus /= 2.0F;
		}
		player.moveRelative(bonus, new Vec3(0.0, 0.0, 1.0));
	}

	private TCEnchantments() {
	}
}
