package dev.neothaum.entity;

import dev.neothaum.NeoThaum;
import dev.neothaum.entity.monster.BrainyZombie;
import dev.neothaum.entity.monster.CrimsonClericEntity;
import dev.neothaum.entity.monster.CrimsonKnightEntity;
import dev.neothaum.entity.monster.CrimsonLeaderEntity;
import dev.neothaum.entity.monster.EldritchCrabEntity;
import dev.neothaum.entity.monster.EldritchGolemEntity;
import dev.neothaum.entity.monster.EldritchGuardianEntity;
import dev.neothaum.entity.monster.EldritchWardenEntity;
import dev.neothaum.entity.monster.FireBat;
import dev.neothaum.entity.monster.GiantBrainyZombie;
import dev.neothaum.entity.monster.InhabitedZombieEntity;
import dev.neothaum.entity.monster.MindSpiderEntity;
import dev.neothaum.entity.monster.Pech;
import dev.neothaum.entity.projectile.PechBlast;
import dev.neothaum.entity.monster.TaintChicken;
import dev.neothaum.entity.monster.TaintCow;
import dev.neothaum.entity.monster.TaintCreeper;
import dev.neothaum.entity.monster.TaintPig;
import dev.neothaum.entity.monster.TaintSheep;
import dev.neothaum.entity.monster.TaintSpider;
import dev.neothaum.entity.monster.TaintVillager;
import dev.neothaum.entity.monster.Taintacle;
import dev.neothaum.entity.monster.TaintacleGiant;
import dev.neothaum.entity.monster.TaintacleSmall;
import dev.neothaum.entity.monster.TaintSpore;
import dev.neothaum.entity.monster.TaintSporeSwarmer;
import dev.neothaum.entity.monster.TaintSwarm;
import dev.neothaum.entity.monster.ThaumicSlime;
import dev.neothaum.entity.monster.Wisp;
import dev.neothaum.registry.TVItems;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.levelgen.Heightmap;

/** ConfigEntities: Thaumcraft's creatures, and ItemSpawnerEgg's eggs for them (in TC4's two colours). */
public final class TVEntities {
	/** name, base colour, spot colour: one spawn egg each, in registration order. */
	public record Egg(String name, int base, int spots, Item item) {
	}

	public static final List<Egg> EGGS = new ArrayList<>();

	public static final EntityType<BrainyZombie> BRAINY_ZOMBIE = mob("brainy_zombie", BrainyZombie::new, MobCategory.MONSTER, 0.6F, 1.95F,
		BrainyZombie::createAttributes, b -> b.spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
			Monster::checkMonsterSpawnRules), 16761087, 32768);
	public static final EntityType<GiantBrainyZombie> GIANT_BRAINY_ZOMBIE = mob("giant_brainy_zombie", GiantBrainyZombie::new, MobCategory.MONSTER,
		0.6F, 1.95F, GiantBrainyZombie::createAttributes, b -> b.spawnPlacement(SpawnPlacementTypes.ON_GROUND,
			Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules), 16761087, 16384);
	public static final EntityType<Wisp> WISP = mob("wisp", Wisp::new, MobCategory.MONSTER, 0.9F, 0.9F, Wisp::createAttributes,
		b -> b.spawnPlacement(SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Wisp::checkWispSpawnRules),
		16761087, 16777215);
	public static final EntityType<FireBat> FIREBAT = mob("firebat", FireBat::new, MobCategory.MONSTER, 0.5F, 0.9F, FireBat::createAttributes,
		b -> b.spawnPlacement(SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, FireBat::checkFireBatSpawnRules),
		16761087, 12582912, true);
	public static final EntityType<Pech> PECH = mob("pech", Pech::new, MobCategory.MONSTER, 0.6F, 1.8F, Pech::createAttributes,
		b -> b.spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Pech::checkPechSpawnRules), 16761087, 4194368);
	public static final EntityType<PechBlast> PECH_BLAST = projectile("pech_blast", PechBlast::new);
	public static final EntityType<dev.neothaum.entity.projectile.Ember> EMBER = projectile("ember", dev.neothaum.entity.projectile.Ember::new);
	public static final EntityType<dev.neothaum.entity.projectile.ExplosiveOrb> EXPLOSIVE_ORB = projectile("explosive_orb",
		dev.neothaum.entity.projectile.ExplosiveOrb::new);
	public static final EntityType<dev.neothaum.entity.projectile.FrostShard> FROST_SHARD = projectile("frost_shard",
		dev.neothaum.entity.projectile.FrostShard::new);
	public static final EntityType<dev.neothaum.entity.projectile.ShockOrb> SHOCK_ORB = projectile("shock_orb",
		dev.neothaum.entity.projectile.ShockOrb::new);
	public static final EntityType<dev.neothaum.entity.projectile.PrimalOrb> PRIMAL_ORB = projectile("primal_orb",
		dev.neothaum.entity.projectile.PrimalOrb::new);
	public static final EntityType<dev.neothaum.entity.projectile.PrimalArrowEntity> PRIMAL_ARROW = projectile("primal_arrow",
		dev.neothaum.entity.projectile.PrimalArrowEntity::new);
	public static final EntityType<dev.neothaum.entity.projectile.BottleTaint> BOTTLE_TAINT = projectile("bottle_taint",
		dev.neothaum.entity.projectile.BottleTaint::new);
	public static final EntityType<dev.neothaum.entity.projectile.AlumentumEntity> ALUMENTUM = projectile("alumentum",
		dev.neothaum.entity.projectile.AlumentumEntity::new);
	/** EntityEldritchOrb: the warden's blast. */
	public static final EntityType<dev.neothaum.entity.projectile.EldritchOrbEntity> ELDRITCH_ORB = projectile("eldritch_orb",
		dev.neothaum.entity.projectile.EldritchOrbEntity::new);
	public static final EntityType<dev.neothaum.entity.projectile.GolemOrbEntity> GOLEM_ORB = projectile("golem_orb",
		dev.neothaum.entity.projectile.GolemOrbEntity::new);
	public static final EntityType<dev.neothaum.entity.AspectOrbEntity> ASPECT_ORB = projectile("aspect_orb",
		dev.neothaum.entity.AspectOrbEntity::new);
	public static final EntityType<ThaumicSlime> THAUMIC_SLIME = mob("thaumic_slime", ThaumicSlime::new, MobCategory.MONSTER, 0.5F, 0.5F,
		ThaumicSlime::createAttributes, b -> b, 16761087, 16744703);
	public static final EntityType<TaintSpider> TAINT_SPIDER = mob("taint_spider", TaintSpider::new, MobCategory.MONSTER, 0.4F, 0.3F,
		TaintSpider::createAttributes, b -> b, 16761087, 4210752);
	public static final EntityType<TaintSpore> TAINT_SPORE = mob("taint_spore", TaintSpore::new, MobCategory.MONSTER, 0.5F, 0.5F,
		TaintSpore::createAttributes, b -> b, 16761087, 8388720);
	public static final EntityType<TaintSporeSwarmer> TAINT_SPORE_SWARMER = mob("taint_spore_swarmer", TaintSporeSwarmer::new, MobCategory.MONSTER,
		1.0F, 1.0F, TaintSporeSwarmer::createAttributes, b -> b, 16761087, 8388704);
	public static final EntityType<TaintSwarm> TAINT_SWARM = mob("taint_swarm", TaintSwarm::new, MobCategory.MONSTER, 2.0F, 2.0F,
		TaintSwarm::createAttributes, b -> b.spawnPlacement(SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
			Monster::checkAnyLightMonsterSpawnRules), 16761087, 8388688);
	public static final EntityType<Taintacle> TAINTACLE = mob("taintacle", Taintacle::new, MobCategory.MONSTER, 0.66F, 3.0F, Taintacle::createAttributes,
		b -> b.spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Taintacle::checkTaintacleSpawnRules), 16761087,
		8388736);
	public static final EntityType<TaintacleSmall> TAINTACLE_SMALL = mob("taintacle_small", TaintacleSmall::new, MobCategory.MONSTER, 0.22F, 1.0F,
		TaintacleSmall::createAttributes, b -> b, 16761087, 8388752);
	public static final EntityType<TaintacleGiant> TAINTACLE_GIANT = mob("taintacle_giant", TaintacleGiant::new, MobCategory.MONSTER, 1.1F, 6.0F,
		TaintacleGiant::createAttributes, b -> b, 16761087, 8421504);
	public static final EntityType<TaintChicken> TAINTED_CHICKEN = mob("tainted_chicken", TaintChicken::new, MobCategory.MONSTER, 0.5F, 0.8F,
		TaintChicken::createAttributes, b -> b, 16761087, 12632256);
	public static final EntityType<TaintCow> TAINTED_COW = mob("tainted_cow", TaintCow::new, MobCategory.MONSTER, 0.9F, 1.3F, TaintCow::createAttributes,
		b -> b, 16761087, 8272443);
	public static final EntityType<TaintCreeper> TAINTED_CREEPER = mob("tainted_creeper", TaintCreeper::new, MobCategory.MONSTER, 0.6F, 1.7F,
		TaintCreeper::createAttributes, b -> b, 16761087, 65280);
	public static final EntityType<TaintPig> TAINTED_PIG = mob("tainted_pig", TaintPig::new, MobCategory.MONSTER, 0.9F, 0.9F, TaintPig::createAttributes,
		b -> b, 16761087, 15702511);
	public static final EntityType<TaintSheep> TAINTED_SHEEP = mob("tainted_sheep", TaintSheep::new, MobCategory.MONSTER, 0.9F, 1.3F,
		TaintSheep::createAttributes, b -> b, 16761087, 8421504);
	public static final EntityType<TaintVillager> TAINTED_VILLAGER = mob("tainted_villager", TaintVillager::new, MobCategory.MONSTER, 0.6F, 1.8F,
		TaintVillager::createAttributes, b -> b, 16761087, 65535);
	public static final EntityType<dev.neothaum.golem.GolemEntity> GOLEM = mob("golem",
		dev.neothaum.golem.GolemEntity::new, MobCategory.MISC, 0.4F, 0.95F,
		dev.neothaum.golem.GolemEntity::createAttributes, b -> b, 0, 0);
	/** EntityDart: a golem's dart. */
	public static final EntityType<dev.neothaum.golem.DartEntity> DART = projectile("dart", dev.neothaum.golem.DartEntity::new);
	/** EntityGolemBobber: a fishing golem's float. */
	public static final EntityType<dev.neothaum.golem.GolemBobberEntity> GOLEM_BOBBER = projectile("golem_bobber",
		dev.neothaum.golem.GolemBobberEntity::new);
	public static final EntityType<dev.neothaum.golem.TravelingTrunkEntity> TRAVELING_TRUNK = mob("traveling_trunk",
		dev.neothaum.golem.TravelingTrunkEntity::new, MobCategory.CREATURE, 0.8F, 0.8F,
		dev.neothaum.golem.TravelingTrunkEntity::createAttributes, b -> b, 0, 0, true);
	public static final EntityType<dev.neothaum.entity.monster.MindSpiderEntity> MIND_SPIDER = mob("mind_spider",
		dev.neothaum.entity.monster.MindSpiderEntity::new, MobCategory.MONSTER, 0.3F, 0.3F,
		dev.neothaum.entity.monster.MindSpiderEntity::createAttributes, b -> b, 11184810, 4210752);
	public static final EntityType<dev.neothaum.entity.monster.EldritchGuardianEntity> ELDRITCH_GUARDIAN = mob("eldritch_guardian",
		dev.neothaum.entity.monster.EldritchGuardianEntity::new, MobCategory.MONSTER, 0.8F, 2.25F,
		dev.neothaum.entity.monster.EldritchGuardianEntity::createAttributes, b -> b, 2236962, 4210752);
	public static final EntityType<CrimsonKnightEntity> CULTIST_KNIGHT = mob("cultist_knight",
		CrimsonKnightEntity::new, MobCategory.MONSTER, 0.6F, 1.95F,
		CrimsonKnightEntity::createAttributes, b -> b, 8060928, 12951641);
	public static final EntityType<CrimsonClericEntity> CULTIST_CLERIC = mob("cultist_cleric",
		CrimsonClericEntity::new, MobCategory.MONSTER, 0.6F, 1.95F,
		CrimsonClericEntity::createAttributes, b -> b, 8060928, 4849715);
	public static final EntityType<CrimsonLeaderEntity> CULTIST_LEADER = mob("cultist_leader",
		CrimsonLeaderEntity::new, MobCategory.MONSTER, 0.75F, 2.25F,
		CrimsonLeaderEntity::createAttributes, b -> b, 5898240, 16766720);
	public static final EntityType<dev.neothaum.entity.monster.boss.CultistPortalEntity> CULTIST_PORTAL = mob("cultist_portal",
		dev.neothaum.entity.monster.boss.CultistPortalEntity::new, MobCategory.MONSTER, 1.5F, 3.0F,
		dev.neothaum.entity.monster.boss.CultistPortalEntity::createAttributes, b -> b, 8060928, 16711680, true);
	public static final EntityType<EldritchCrabEntity> ELDRITCH_CRAB = mob("eldritch_crab",
		EldritchCrabEntity::new, MobCategory.MONSTER, 0.8F, 0.6F,
		EldritchCrabEntity::createAttributes, b -> b, 2763306, 9109504);
	public static final EntityType<InhabitedZombieEntity> INHABITED_ZOMBIE = mob("inhabited_zombie",
		InhabitedZombieEntity::new, MobCategory.MONSTER, 0.6F, 1.95F,
		InhabitedZombieEntity::createAttributes, b -> b, 4877128, 9109504);
	public static final EntityType<EldritchWardenEntity> ELDRITCH_WARDEN = mob("eldritch_warden",
		EldritchWardenEntity::new, MobCategory.MONSTER, 1.2F, 3.5F,
		EldritchWardenEntity::createAttributes, b -> b, 1381653, 10162462);
	public static final EntityType<EldritchGolemEntity> ELDRITCH_GOLEM = mob("eldritch_golem",
		EldritchGolemEntity::new, MobCategory.MONSTER, 1.4F, 2.8F,
		EldritchGolemEntity::createAttributes, b -> b, 2829110, 4251856);

	private static <T extends Mob> EntityType<T> mob(String name, EntityType.EntityFactory<T> factory, MobCategory category, float w, float h,
		Supplier<AttributeSupplier.Builder> attributes, UnaryOperator<FabricEntityType.Builder.Mob<T>> extra, int egg1, int egg2) {
		return mob(name, factory, category, w, h, attributes, extra, egg1, egg2, false);
	}

	private static <T extends Mob> EntityType<T> mob(String name, EntityType.EntityFactory<T> factory, MobCategory category, float w, float h,
		Supplier<AttributeSupplier.Builder> attributes, UnaryOperator<FabricEntityType.Builder.Mob<T>> extra, int egg1, int egg2, boolean fireImmune) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, NeoThaum.id(name));
		EntityType.Builder<T> builder = FabricEntityType.Builder.createMob(factory, category, b -> extra.apply(b.defaultAttributes(attributes)))
			.sized(w, h).clientTrackingRange(8);
		if (fireImmune) {
			builder = builder.fireImmune();
		}
		if (category == MobCategory.MONSTER && !name.startsWith("tainted_")) {
			// every TC4 monster is an IMob: removed when the world turns peaceful. Tainted animals are kept so that animals dying of flux
			// taint still turn in peaceful; monsters never spawn naturally in peaceful anyway.
			builder = builder.notInPeaceful();
		}
		EntityType<T> type = Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
		if (egg1 != 0 || egg2 != 0) {
			Item item = TVItems.register(name + "_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(type));
			TVItems.TAB_ORDER.remove(item);
			EGGS.add(new Egg(name, egg1, egg2, item));
		}
		return type;
	}

	/** EntitySpecialItem: a floating, explosion-proof item drop. */
	public static final EntityType<SpecialItemEntity> SPECIAL_ITEM = Registry.register(BuiltInRegistries.ENTITY_TYPE,
		ResourceKey.create(Registries.ENTITY_TYPE, NeoThaum.id("special_item")),
		EntityType.Builder.<SpecialItemEntity>of(SpecialItemEntity::new, MobCategory.MISC).noLootTable().sized(0.25F, 0.25F).eyeHeight(0.2125F)
			.clientTrackingRange(6).updateInterval(20).build(ResourceKey.create(Registries.ENTITY_TYPE, NeoThaum.id("special_item"))));

	private static <T extends net.minecraft.world.entity.Entity> EntityType<T> projectile(String name, EntityType.EntityFactory<T> factory) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, NeoThaum.id(name));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key,
			EntityType.Builder.of(factory, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10).build(key));
	}

	/** The looting level of whoever dealt the killing blow (EntityLivingBase.onDeath's lootingLevel). */
	public static int looting(ServerLevel level, DamageSource source) {
		if (source.getEntity() instanceof LivingEntity le) {
			return EnchantmentHelper.getEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING),
				le);
		}
		return 0;
	}

	public static void init() {
		TVItems.TAB_ORDER.addAll(EGGS.stream().map(Egg::item).toList());
		TaintConversion.init();
	}

	private TVEntities() {
	}
}
