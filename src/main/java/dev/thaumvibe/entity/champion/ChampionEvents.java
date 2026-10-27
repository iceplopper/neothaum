package dev.thaumvibe.entity.champion;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.config.ThaumvibeConfig;
import dev.thaumvibe.entity.AspectOrbEntity;
import dev.thaumvibe.registry.TVItems;
import dev.thaumvibe.registry.TVSounds;
import dev.thaumvibe.warp.Warp;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

/**
 * Champion Mob events: spawning with TC4 modifiers, modifier combat triggers, undying revival, and death rewards.
 */
public final class ChampionEvents {
	public static final String TAG_CHAMPION = "thaumvibe:champion";
	public static final String TAG_CHECKED = "thaumvibe:checked_champion";
	public static final String TAG_MOD_PREFIX = "thaumvibe:champion_mod_";
	public static final String TAG_REVIVED = "thaumvibe:champion_revived";

	public static boolean isChampion(LivingEntity entity) {
		return entity != null && entity.entityTags().contains(TAG_CHAMPION);
	}

	public static ChampionModifier getModifier(LivingEntity entity) {
		return ChampionModifier.fromEntity(entity);
	}

	public static void makeChampion(Monster monster) {
		ChampionModifier mod = ChampionModifier.values()[monster.getRandom().nextInt(ChampionModifier.values().length)];
		if (monster instanceof Creeper) {
			mod = ChampionModifier.BOLD;
		}

		monster.addTag(TAG_CHAMPION);
		monster.addTag(TAG_MOD_PREFIX + mod.getKey());

		// +30 Max Health
		var maxHealth = monster.getAttribute(Attributes.MAX_HEALTH);
		if (maxHealth != null) {
			maxHealth.setBaseValue(maxHealth.getBaseValue() + 30.0);
			monster.setHealth((float) maxHealth.getValue());
		}

		// Stat buffs according to modifier
		switch (mod) {
			case BOLD -> {
				var speed = monster.getAttribute(Attributes.MOVEMENT_SPEED);
				if (speed != null) speed.setBaseValue(speed.getBaseValue() * 1.2);
				var kb = monster.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
				if (kb != null) kb.setBaseValue(kb.getBaseValue() + 0.5);
			}
			case ARMORED -> {
				var armor = monster.getAttribute(Attributes.ARMOR);
				if (armor != null) armor.setBaseValue(armor.getBaseValue() + 8.0);
				var toughness = monster.getAttribute(Attributes.ARMOR_TOUGHNESS);
				if (toughness != null) toughness.setBaseValue(toughness.getBaseValue() + 4.0);
			}
			case MIGHTY -> {
				var attack = monster.getAttribute(Attributes.ATTACK_DAMAGE);
				if (attack != null) attack.setBaseValue(attack.getBaseValue() * 1.5);
			}
			case WARDED -> monster.setAbsorptionAmount(20.0F);
			default -> {}
		}

		// Colored Custom Nameplate
		Component origName = monster.getType().getDescription();
		Component champName = Component.empty().append(mod.getFormattedPrefix()).append(" ").append(origName);
		monster.setCustomName(champName);
		monster.setCustomNameVisible(true);
		monster.setPersistenceRequired();
	}

	public static void init() {
		// Spawn chance on entity load into server level
		ServerEntityEvents.ENTITY_LOAD.register((Entity entity, ServerLevel level) -> {
			if (!ThaumvibeConfig.current.championMobs) return;
			if (entity instanceof Monster monster) {
				if (!monster.entityTags().contains(TAG_CHECKED)) {
					monster.addTag(TAG_CHECKED);
					// 2.5% chance to spawn as a Champion mob
					if (monster.getRandom().nextFloat() < 0.025F) {
						makeChampion(monster);
					}
				}
			}
		});

		// Combat Effects
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
			if (entity.level().isClientSide()) return;

			// Attacker was a champion
			if (source.getEntity() instanceof LivingEntity attacker && isChampion(attacker)) {
				ChampionModifier mod = getModifier(attacker);
				if (mod != null) {
					switch (mod) {
						case FIERY -> entity.igniteForSeconds(4.0F);
						case GRIM -> entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
						case SICKLY -> {
							entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120, 1));
							entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
						}
						case VENOMOUS -> entity.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
						case VAMPIRIC -> attacker.heal(damageTaken * 0.4F);
						case WARP -> {
							entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0));
							entity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0));
							if (entity instanceof Player player) {
								Warp.addWarpToPlayer(player, 1 + attacker.getRandom().nextInt(3), true);
							}
						}
						default -> {}
					}
				}
			}

			// Victim was a champion
			if (isChampion(entity)) {
				ChampionModifier mod = getModifier(entity);
				if (mod != null) {
					switch (mod) {
						case SPINED -> {
							if (source.getEntity() instanceof LivingEntity attacker && attacker != entity) {
								attacker.hurt(entity.damageSources().thorns(entity), 3.0F);
							}
						}
						case INFESTED -> {
							if (entity.getRandom().nextFloat() < 0.35F && entity.level() instanceof ServerLevel sl) {
								EntityType<?> minionType = entity.getRandom().nextBoolean() ? EntityTypes.SILVERFISH : EntityTypes.CAVE_SPIDER;
								Mob minion = (Mob) minionType.create(sl, EntitySpawnReason.TRIGGERED);
								if (minion != null) {
									minion.snapTo(entity.getX() + (entity.getRandom().nextDouble() - 0.5) * 2, entity.getY(),
										entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * 2, entity.getYRot(), 0.0F);
									if (source.getEntity() instanceof LivingEntity attacker) {
										minion.setTarget(attacker);
									}
									sl.addFreshEntity(minion);
								}
							}
						}
						default -> {}
					}
				}
			}
		});

		// Undying Modifier (prevents death once and heals to 50%)
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, damageAmount) -> {
			if (entity.level().isClientSide()) return true;
			if (isChampion(entity)) {
				ChampionModifier mod = getModifier(entity);
				if (mod == ChampionModifier.UNDYING && !entity.entityTags().contains(TAG_REVIVED)) {
					entity.addTag(TAG_REVIVED);
					entity.setHealth(entity.getMaxHealth() * 0.5F);
					entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1));
					entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), TVSounds.ZAP, SoundSource.HOSTILE, 1.0F, 1.0F);
					return false;
				}
			}
			return true;
		});

		// Drops on Death (Loot Bag, Aspect Orbs, Extra XP)
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity.level().isClientSide() || !(entity.level() instanceof ServerLevel sl)) return;
			if (isChampion(entity)) {
				int looting = 0;
				if (source.getEntity() instanceof LivingEntity) {
					looting = 1;
				}
				int lb = Math.min(2, (entity.getRandom().nextInt(9) + looting) / 5);
				Item bag = switch (lb) {
					case 0 -> TVItems.LOOT_BAG_COMMON;
					case 1 -> TVItems.LOOT_BAG_UNCOMMON;
					default -> TVItems.LOOT_BAG_RARE;
				};
				entity.spawnAtLocation(sl, bag);

				// Drop 2-4 Aspect Orbs
				Aspect[] primals = Aspect.getPrimalAspects().toArray(new Aspect[0]);
				int count = 2 + entity.getRandom().nextInt(3);
				for (int i = 0; i < count; i++) {
					Aspect a = primals[entity.getRandom().nextInt(primals.length)];
					AspectOrbEntity orb = new AspectOrbEntity(entity.level(), entity.getX(), entity.getY() + 0.5, entity.getZ(), a, 1 + entity.getRandom().nextInt(2));
					entity.level().addFreshEntity(orb);
				}

				// Award extra XP
				ExperienceOrb.award(sl, entity.position(), 6 + entity.getRandom().nextInt(5));
			}
		});
	}

	private ChampionEvents() {
	}
}
