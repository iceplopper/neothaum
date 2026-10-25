package dev.thaumvibe.runic;

import dev.thaumvibe.api.RunicArmor;
import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.baubles.Baubles;
import dev.thaumvibe.items.baubles.RunicBaubleItem;
import dev.thaumvibe.network.TVNetwork;
import dev.thaumvibe.registry.TVSounds;
import dev.thaumvibe.wands.WandManager;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * EventHandlerRunic: a player's runic shield charge is the sum of their worn runic gear (armour and baubles). It refills one point at a time,
 * paying half a vis of air and earth from a vis amulet or wand each time, and soaks up damage before armour does. When it breaks, a kinetic
 * girdle throws out a blast, a revitalizing ring heals and an emergency amulet refills it, each on its own cooldown.
 */
public final class RunicShielding {
	public static final int SHIELD_RECHARGE = 2000;
	public static final int SHIELD_WAIT = 80;
	public static final int SHIELD_COST = 50;

	/** max, charged, kinetic, healing, emergency */
	private record Info(int max, int charged, int kinetic, int healing, int emergency) {
	}

	private static final Map<Integer, Integer> RUNIC_CHARGE = new HashMap<>();
	private static final Map<Integer, Long> NEXT_CYCLE = new HashMap<>();
	private static final Map<Integer, Integer> LAST_CHARGE = new HashMap<>();
	private static final Map<Integer, Info> RUNIC_INFO = new HashMap<>();
	private static final Map<String, Long> UPGRADE_COOLDOWN = new HashMap<>();
	private static boolean isDirty = true;
	private static int rechargeDelay;

	public static void markDirty() {
		isDirty = true;
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(RunicShielding::tick);
	}

	private static void tick(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			livingTick(player);
		}
	}

	private static void send(ServerPlayer player, int charge, int max) {
		ServerPlayNetworking.send(player, new TVNetwork.RunicCharge(charge, max));
	}

	private static void livingTick(ServerPlayer player) {
		int id = player.getId();
		if (isDirty || player.tickCount % 40 == 0) {
			int max = 0, charged = 0, kinetic = 0, healing = 0, emergency = 0;
			isDirty = false;
			for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
				max += RunicArmor.getFinalCharge(player.getItemBySlot(slot));
			}
			for (ItemStack b : Baubles.get(player).items()) {
				if (b.getItem() instanceof RunicArmor) {
					if (b.getItem() instanceof RunicBaubleItem rb) {
						switch (rb.kind) {
							case CHARGED -> charged++;
							case HEALING -> healing++;
							case EMERGENCY -> emergency++;
							case KINETIC -> kinetic++;
							default -> {
							}
						}
					}
					max += RunicArmor.getFinalCharge(b);
				}
			}
			if (max > 0) {
				RUNIC_INFO.put(id, new Info(max, charged, kinetic, healing, emergency));
				Integer charge = RUNIC_CHARGE.get(id);
				if (charge != null && charge > max) {
					RUNIC_CHARGE.put(id, max);
					send(player, max, max);
				}
			} else {
				RUNIC_INFO.remove(id);
				RUNIC_CHARGE.put(id, 0);
				send(player, 0, 0);
			}
		}
		if (rechargeDelay > 0) {
			rechargeDelay--;
			return;
		}
		Info info = RUNIC_INFO.get(id);
		if (info == null) {
			return;
		}
		LAST_CHARGE.putIfAbsent(id, -1);
		RUNIC_CHARGE.putIfAbsent(id, 0);
		NEXT_CYCLE.putIfAbsent(id, 0L);
		long time = System.currentTimeMillis();
		int charge = RUNIC_CHARGE.get(id);
		if (charge > info.max()) {
			charge = info.max();
		} else if (charge < info.max() && NEXT_CYCLE.get(id) < time
			&& WandManager.consumeVisFromInventory(player, new AspectList().add(Aspect.AIR, SHIELD_COST).add(Aspect.EARTH, SHIELD_COST))) {
			long interval = SHIELD_RECHARGE - info.charged() * 500L;
			NEXT_CYCLE.put(id, time + interval);
			RUNIC_CHARGE.put(id, ++charge);
		}
		if (LAST_CHARGE.get(id) != charge) {
			send(player, charge, info.max());
			LAST_CHARGE.put(id, charge);
		}
	}

	/** LivingHurtEvent: the shield takes what it can of the blow, before armour. */
	public static float onHurt(LivingEntity entity, DamageSource source, float amount) {
		if (!(entity instanceof ServerPlayer player) || !(entity.level() instanceof ServerLevel level)) {
			return amount;
		}
		if (source.is(DamageTypes.DROWN) || source.is(DamageTypes.WITHER) || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
			|| source.is(DamageTypes.STARVE)) {
			return amount;
		}
		int id = player.getId();
		Info info = RUNIC_INFO.get(id);
		Integer stored = RUNIC_CHARGE.get(id);
		if (info == null || stored == null || stored <= 0) {
			return amount;
		}
		long time = System.currentTimeMillis();
		int target = source.getEntity() != null ? source.getEntity().getId() : -1;
		if (source.is(DamageTypes.FALL)) {
			target = -2;
		}
		if (source.is(DamageTypes.FALLING_BLOCK) || source.is(DamageTypes.FALLING_ANVIL)) {
			target = -3;
		}
		TVNetwork.sendShield(level, player, target);
		int charge = stored;
		if (charge > amount) {
			charge = (int) (charge - amount);
			amount = 0.0F;
		} else {
			amount -= charge;
			charge = 0;
		}
		String key = id + ":2";
		if (charge <= 0 && info.kinetic() > 0 && cooled(key, time)) {
			UPGRADE_COOLDOWN.put(key, time + 20000L);
			level.explode(player, player.getX(), player.getY() + player.getBbHeight() / 2.0F, player.getZ(), 1.5F + info.kinetic() * 0.5F,
				Level.ExplosionInteraction.NONE);
		}
		key = id + ":3";
		if (charge <= 0 && info.healing() > 0 && cooled(key, time)) {
			UPGRADE_COOLDOWN.put(key, time + 20000L);
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 240, info.healing()));
			level.playSound(null, player.blockPosition(), TVSounds.RUNICSHIELDEFFECT, SoundSource.PLAYERS, 1.0F, 1.0F);
		}
		key = id + ":4";
		if (charge <= 0 && info.emergency() > 0 && cooled(key, time)) {
			UPGRADE_COOLDOWN.put(key, time + 60000L);
			charge = Math.min(info.max(), 8 * info.emergency());
			isDirty = true;
			level.playSound(null, player.blockPosition(), TVSounds.RUNICSHIELDCHARGE, SoundSource.PLAYERS, 1.0F, 1.0F);
		}
		if (charge <= 0) {
			rechargeDelay = SHIELD_WAIT;
		}
		RUNIC_CHARGE.put(id, charge);
		send(player, charge, info.max());
		return amount;
	}

	private static boolean cooled(String key, long time) {
		Long until = UPGRADE_COOLDOWN.get(key);
		return until == null || until < time;
	}

	/** Players leaving take their shield with them. */
	public static void forget(Player player) {
		RUNIC_CHARGE.remove(player.getId());
		RUNIC_INFO.remove(player.getId());
		LAST_CHARGE.remove(player.getId());
		NEXT_CYCLE.remove(player.getId());
	}

	private RunicShielding() {
	}
}
