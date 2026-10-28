package dev.neothaum.entity.champion;

import dev.neothaum.NeoThaum;
import dev.neothaum.config.NeoThaumConfig;
import dev.neothaum.entity.TVEntities;
import dev.neothaum.entity.monster.CultistEntity;
import dev.neothaum.entity.monster.EldritchCrabEntity;
import dev.neothaum.entity.monster.InhabitedZombieEntity;
import dev.neothaum.entity.monster.Pech;
import dev.neothaum.entity.monster.Taintacle;
import dev.neothaum.entity.monster.Wisp;
import dev.neothaum.registry.TVItems;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.warp.Warp;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biomes;

/**
 * TC4 champion mobs (EventHandlerEntity, EntityUtils.makeChampion, ChampionModifier). A whitelisted monster with at least 10 base health
 * rolls d100 as it joins the world: +2 on Easy, -2 on Hard, -3 in the Outer Lands (-10 more in its key rooms and libraries), -2 in
 * spooky, Nether or End biomes, minus its whitelist weight - 1. At 0 or
 * below it becomes a champion: a random modifier (creepers are always Bold), +30 health, triple damage and a name like "Bold Zombie". Bosses
 * are always champions, keep their health and damage, and take the modifier into their name. Champions killed by a player give 5-7 XP and
 * a loot bag. Modifiers act each tick (Warded, Undying), when the champion is hurt (Spined, Armored, Infested), or when it hits (the rest).
 */
public final class ChampionEvents {
	public static final String TAG_CHAMPION = "neothaum:champion";
	public static final String TAG_CHECKED = "neothaum:checked_champion";
	public static final String TAG_MOD_PREFIX = "neothaum:champion_mod_";

	/** ConfigEntities.championModWhitelist, matched by class like Class.isAssignableFrom. Wisps are listed but, not being EntityMob, never roll. */
	private static final List<Map.Entry<Class<?>, Integer>> WHITELIST = List.of(Map.entry(Zombie.class, 0), Map.entry(Spider.class, 0),
		Map.entry(Blaze.class, 0), Map.entry(Enderman.class, 0), Map.entry(AbstractSkeleton.class, 0), Map.entry(Witch.class, 1),
		Map.entry(EldritchCrabEntity.class, 0), Map.entry(Taintacle.class, 2), Map.entry(Wisp.class, 1), Map.entry(InhabitedZombieEntity.class, 3),
		Map.entry(CultistEntity.class, 1), Map.entry(Pech.class, 1), Map.entry(ChampionBoss.class, 200));

	private static final AttributeModifier HEALTH = new AttributeModifier(NeoThaum.id("champion_health"), 30.0, AttributeModifier.Operation.ADD_VALUE);
	private static final AttributeModifier DAMAGE = new AttributeModifier(NeoThaum.id("champion_damage"), 2.0,
		AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
	private static final AttributeModifier BOLD = new AttributeModifier(NeoThaum.id("champion_bold"), 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
	private static final AttributeModifier MIGHTY = new AttributeModifier(NeoThaum.id("champion_mighty"), 3.0,
		AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
	private static final net.minecraft.resources.Identifier WARDED = NeoThaum.id("champion_warded");

	private ChampionEvents() {
	}

	public static boolean isChampion(LivingEntity entity) {
		return entity != null && entity.entityTags().contains(TAG_CHAMPION);
	}

	public static ChampionModifier getModifier(LivingEntity entity) {
		return ChampionModifier.fromEntity(entity);
	}

	/** EntityUtils.makeChampion. */
	public static void makeChampion(Monster mob, boolean persist) {
		ChampionModifier mod = ChampionModifier.values()[mob.getRandom().nextInt(ChampionModifier.values().length)];
		if (mob instanceof Creeper) {
			mod = ChampionModifier.BOLD;
		}
		mob.addTag(TAG_CHAMPION);
		mob.addTag(TAG_MOD_PREFIX + mod.getKey());
		if (mob instanceof ChampionBoss boss) {
			boss.nameAsChampion(mod.getFormattedPrefix());
		} else {
			addModifier(mob.getAttribute(Attributes.MAX_HEALTH), HEALTH);
			addModifier(mob.getAttribute(Attributes.ATTACK_DAMAGE), DAMAGE);
			mob.heal(25.0F);
			mob.setCustomName(mod.getFormattedPrefix().copy().append(" ").append(mob.getType().getDescription()));
		}
		if (persist) {
			mob.setPersistenceRequired();
		}
		switch (mod) {
			case BOLD -> addModifier(mob.getAttribute(Attributes.MOVEMENT_SPEED), BOLD);
			case MIGHTY -> addModifier(mob.getAttribute(Attributes.ATTACK_DAMAGE), MIGHTY);
			case WARDED -> {
				// a ward of half its base health to start with (MAX_ABSORPTION lets it hold that much)
				float ward = (float) mob.getAttributeBaseValue(Attributes.MAX_HEALTH) / 2.0F;
				addModifier(mob.getAttribute(Attributes.MAX_ABSORPTION), new AttributeModifier(WARDED, ward, AttributeModifier.Operation.ADD_VALUE));
				mob.setAbsorptionAmount(mob.getAbsorptionAmount() + ward);
			}
			default -> {
			}
		}
	}

	private static void addModifier(AttributeInstance attribute, AttributeModifier modifier) {
		if (attribute != null) {
			attribute.removeModifier(modifier.id());
			attribute.addPermanentModifier(modifier);
		}
	}

	/** EventHandlerEntity.entitySpawns: the champion roll. */
	private static void roll(Monster mob, ServerLevel level) {
		int c = mob.getRandom().nextInt(100);
		if (level.getDifficulty() == Difficulty.EASY) {
			c += 2;
		}
		if (level.getDifficulty() == Difficulty.HARD) {
			c -= 2;
		}
		// the Outer Lands are deadlier, and their key rooms and libraries most of all (isDangerousLocation)
		if (level.dimension().equals(dev.neothaum.world.outer.OuterLands.KEY)) {
			c -= 3;
			var cell = dev.neothaum.world.outer.MazeHandler.getCell(mob.getBlockX() >> 4, mob.getBlockZ() >> 4);
			if (cell != null && (cell.feature == 6 || cell.feature == 8)) {
				c -= 10;
			}
		}
		var biome = level.getBiome(mob.blockPosition());
		if (biome.is(BiomeTags.IS_NETHER) || biome.is(BiomeTags.IS_END) || biome.is(Biomes.DARK_FOREST) || biome.is(Biomes.PALE_GARDEN)
			|| biome.is(dev.neothaum.world.TVBiomes.EERIE)) {
			c -= 2;
		}
		int cc = 0;
		boolean whitelisted = false;
		for (var e : WHITELIST) {
			if (e.getKey().isInstance(mob)) {
				whitelisted = true;
				cc = Math.max(cc, e.getValue() - 1);
			}
		}
		c -= cc;
		if (whitelisted && c <= 0 && mob.getAttributeBaseValue(Attributes.MAX_HEALTH) >= 10.0) {
			makeChampion(mob, false);
		}
	}

	/** EventHandlerEntity.livingTick: the per-tick modifiers. */
	public static void tick(LivingEntity mob) {
		ChampionModifier mod = getModifier(mob);
		if (mod == ChampionModifier.WARDED) {
			// ChampionModWarded: while not reeling from a hit, the ward regrows a point every 25 ticks up to half its base health
			if (mob.hurtTime <= 0 && mob.tickCount % 25 == 0) {
				int bh = (int) mob.getAttributeBaseValue(Attributes.MAX_HEALTH) / 2;
				if (mob.getAbsorptionAmount() < bh) {
					mob.setAbsorptionAmount(mob.getAbsorptionAmount() + 1.0F);
				}
			}
		} else if (mod == ChampionModifier.UNDYING && mob.tickCount % 20 == 0) {
			mob.heal(1.0F);
		}
	}

	/** EventHandlerRunic.entityHurt: a champion's hurt modifiers on what hits it, then the attacking champion's hit modifiers. */
	public static float onHurt(LivingEntity victim, DamageSource source, float amount) {
		if (!(victim.level() instanceof ServerLevel sl)) {
			return amount;
		}
		var rand = victim.getRandom();
		if (isChampion(victim) && source.getEntity() instanceof LivingEntity attacker) {
			switch (getModifier(victim)) {
				case SPINED -> {
					if (!source.is(DamageTypes.THORNS)) {
						attacker.hurtServer(sl, victim.damageSources().thorns(victim), 1 + rand.nextInt(3));
						attacker.playSound(SoundEvents.THORNS_HIT, 0.5F, 1.0F);
					}
				}
				case ARMORED -> {
					if (!source.is(DamageTypeTags.BYPASSES_ARMOR)) {
						amount = amount * 19.0F / 25.0F;
					}
				}
				case INFESTED -> {
					if (rand.nextFloat() < 0.4F) {
						var spiderling = TVEntities.TAINT_SPIDER.create(sl, EntitySpawnReason.MOB_SUMMONED);
						if (spiderling != null) {
							spiderling.snapTo(victim.getX(), victim.getY() + victim.getBbHeight() / 2.0F, victim.getZ(), rand.nextFloat() * 360.0F, 0.0F);
							sl.addFreshEntity(spiderling);
							victim.playSound(TVSounds.GORE, 0.5F, 1.0F);
						}
					}
				}
				case null, default -> {
				}
			}
		}
		if (amount > 0.0F && source.getEntity() instanceof Monster mob && isChampion(mob)) {
			switch (getModifier(mob)) {
				case GRIM -> {
					if (rand.nextFloat() < 0.4F) {
						victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 200));
					}
				}
				case SICKLY -> {
					if (rand.nextFloat() < 0.4F) {
						victim.addEffect(new MobEffectInstance(MobEffects.HUNGER, 500));
					}
				}
				case VENOMOUS -> {
					if (rand.nextFloat() < 0.4F) {
						victim.addEffect(new MobEffectInstance(MobEffects.POISON, 100));
					}
				}
				case FIERY -> {
					if (rand.nextFloat() < 0.4F) {
						victim.igniteForSeconds(4.0F);
					}
				}
				case VAMPIRIC -> mob.heal(Math.max(2.0F, amount / 2.0F));
				case WARP -> {
					if (rand.nextFloat() < 0.33F && victim instanceof Player player) {
						Warp.addWarpToPlayer(player, 1 + rand.nextInt(3), true);
					}
				}
				case null, default -> {
				}
			}
		}
		return amount;
	}

	public static void init() {
		ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
			if (NeoThaumConfig.current.championMobs && entity instanceof Monster mob && !mob.entityTags().contains(TAG_CHECKED)) {
				mob.addTag(TAG_CHECKED);
				roll(mob, level);
			}
		});

		// EventHandlerEntity.livingDrops: a champion (not a boss) that a player killed gives 5-7 XP and a loot bag
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (!(entity.level() instanceof ServerLevel sl) || !(entity instanceof Monster) || entity instanceof ChampionBoss || !isChampion(entity)
				|| entity.getLastHurtByPlayer() == null) {
				return;
			}
			ExperienceOrb.award(sl, entity.position(), 5 + entity.getRandom().nextInt(3));
			int lb = Math.min(2, (entity.getRandom().nextInt(9) + TVEntities.looting(sl, source)) / 5);
			var bag = switch (lb) {
				case 0 -> TVItems.LOOT_BAG_COMMON;
				case 1 -> TVItems.LOOT_BAG_UNCOMMON;
				default -> TVItems.LOOT_BAG_RARE;
			};
			entity.spawnAtLocation(sl, new net.minecraft.world.item.ItemStack(bag), entity.getEyeHeight());
		});
	}
}
