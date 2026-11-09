package dev.neothaum.addon.vessels;

import com.mojang.serialization.Codec;
import dev.neothaum.NeoThaum;
import dev.neothaum.addon.Additions;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.items.WandItem;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVItems;
import dev.neothaum.registry.TVSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.TagValueOutput;

/**
 * NeoThaum Addition: Vessels of Binding, warded jars re-cut to hold a creature as Node in a Jar holds a node. With a wand in the main hand
 * and an empty vessel in the other, use a creature: the wand pays the vis and the creature is drawn into the vessel, kept exactly as it was
 * (name, health, owner, trades, saddle, what it carries). Set the vessel down and use it with a wand to let the creature out, for nothing.
 * <ul>
 * <li>Lesser Vessel: animals only.</li>
 * <li>Vessel of Binding: any creature but the great ones (bosses) and the tainted; a hostile one only once worn below half its health.</li>
 * <li>Greater Vessel: as the Vessel of Binding, up to eight of one kind.</li>
 * </ul>
 */
public final class Vessels {
	public enum Tier {
		LESSER(1), BINDING(1), GREATER(8);

		public final int holds;

		Tier(int holds) {
			this.holds = holds;
		}
	}

	/** The creatures in a vessel, each as the world saves it. */
	public static final DataComponentType<List<CompoundTag>> BOUND = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
		NeoThaum.id("bound_creatures"), DataComponentType.<List<CompoundTag>>builder().persistent(CompoundTag.CODEC.listOf())
			.networkSynchronized(ByteBufCodecs.COMPOUND_TAG.apply(ByteBufCodecs.list())).build());
	/** Creatures no vessel holds: the great ones. (The tainted are refused by name.) */
	public static final TagKey<EntityType<?>> UNBINDABLE = TagKey.create(Registries.ENTITY_TYPE, NeoThaum.id("unbindable"));

	public static final Block LESSER = TVBlocks.register("lesser_vessel", p -> new VesselBlock(Tier.LESSER, p), props(), false);
	public static final Block BINDING = TVBlocks.register("vessel_of_binding", p -> new VesselBlock(Tier.BINDING, p), props(), false);
	public static final Block GREATER = TVBlocks.register("greater_vessel", p -> new VesselBlock(Tier.GREATER, p), props(), false);
	public static final BlockEntityType<VesselBlockEntity> VESSEL_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
		NeoThaum.id("vessel"), new BlockEntityType<>(VesselBlockEntity::new, Set.of(LESSER, BINDING, GREATER)));
	public static Item lesserItem;
	public static Item bindingItem;
	public static Item greaterItem;

	public static boolean enabled() {
		return Additions.enabled("creature_vessels");
	}

	private static BlockBehaviour.Properties props() {
		SoundType jar = new SoundType(1.0F, 1.0F, TVSounds.JAR, TVSounds.JAR, TVSounds.JAR, TVSounds.JAR, TVSounds.JAR);
		return BlockBehaviour.Properties.of().strength(0.3F).sound(jar).lightLevel(s -> 6).noOcclusion();
	}

	/** Loads the class (registering the blocks) from NeoThaum.onInitialize, right after TVBlocks. */
	public static void registerBlocks() {
		LESSER.getClass();
	}

	/** After TVItems: the items, and binding by wand. */
	public static void init() {
		lesserItem = item(LESSER, Tier.LESSER);
		bindingItem = item(BINDING, Tier.BINDING);
		greaterItem = item(GREATER, Tier.GREATER);
		int at = TVItems.TAB_ORDER.indexOf(TVBlocks.NODE_IN_A_JAR.asItem()) + 1;
		for (Item i : new Item[]{lesserItem, bindingItem, greaterItem}) {
			Additions.hideWith(i, "creature_vessels");
			TVItems.TAB_ORDER.add(at > 0 ? at++ : TVItems.TAB_ORDER.size(), i);
		}
		UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
			if (hand != InteractionHand.MAIN_HAND || !(player.getMainHandItem().getItem() instanceof WandItem)
				|| !(player.getOffhandItem().getItem() instanceof VesselItem) || !enabled()) {
				return InteractionResult.PASS;
			}
			if (player instanceof ServerPlayer sp && level instanceof ServerLevel sl) {
				bind(sp, sl, player.getMainHandItem(), player.getOffhandItem(), entity);
			}
			return InteractionResult.SUCCESS;
		});
	}

	private static Item item(Block block, Tier tier) {
		var key = ResourceKey.create(Registries.ITEM, BuiltInRegistries.BLOCK.getKey(block));
		Item.Properties p = new Item.Properties().setId(key).useBlockDescriptionPrefix().stacksTo(tier == Tier.LESSER ? 16 : 1);
		return Registry.register(BuiltInRegistries.ITEM, key, new VesselItem(block, tier, p));
	}

	/** Draws the creature into the vessel in the off hand, if the vessel may hold it and the wand can pay. */
	public static void bind(ServerPlayer sp, ServerLevel level, ItemStack wand, ItemStack vessel, Entity entity) {
		Tier tier = ((VesselItem) vessel.getItem()).tier;
		List<CompoundTag> held = vessel.getOrDefault(BOUND, List.of());
		String refusal = refusal(sp, tier, held, entity);
		if (refusal != null) {
			sp.sendOverlayMessage(Component.translatable("neothaum.vessel." + refusal).withStyle(ChatFormatting.DARK_PURPLE));
			level.playSound(null, sp.blockPosition(), TVSounds.WANDFAIL, SoundSource.PLAYERS, 0.5F, 1.0F);
			return;
		}
		AspectList cost = cost(entity);
		if (!WandItem.consumeAllVis(wand, sp, cost, true, false)) {
			sp.sendOverlayMessage(Component.translatable("neothaum.vessel.novis").withStyle(ChatFormatting.DARK_PURPLE));
			level.playSound(null, sp.blockPosition(), TVSounds.WANDFAIL, SoundSource.PLAYERS, 0.5F, 1.0F);
			return;
		}
		// as it is: off its mount and its lead, its riders set down
		if (entity instanceof Mob mob && mob.isLeashed()) {
			mob.dropLeash();
		}
		entity.stopRiding();
		entity.ejectPassengers();
		TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		if (!entity.save(out)) {
			return;
		}
		List<CompoundTag> now = new ArrayList<>(held);
		now.add(out.buildResult());
		ItemStack filled = vessel.getCount() > 1 ? vessel.split(1) : vessel;
		filled.set(BOUND, List.copyOf(now));
		if (filled != vessel) {
			// one of a stack of empty vessels: the filled one goes to the inventory
			if (!sp.getInventory().add(filled)) {
				sp.drop(filled, false, net.minecraft.util.Prediction.PREDICTED);
			}
		}
		level.sendParticles(ParticleTypes.WITCH, entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(), 20, 0.3,
			entity.getBbHeight() / 3, 0.3, 0.02);
		level.playSound(null, entity.blockPosition(), TVSounds.WAND, SoundSource.PLAYERS, 0.6F, 0.8F);
		entity.discard();
	}

	/** Why the vessel won't take this creature (a lang key's end), or null if it will. */
	private static String refusal(ServerPlayer sp, Tier tier, List<CompoundTag> held, Entity entity) {
		if (!(entity instanceof Mob mob) || BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity.getType()).is(UNBINDABLE)) {
			return "unbindable";
		}
		if (BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath().contains("taint")) {
			return "tainted";
		}
		if (held.size() >= tier.holds) {
			return "full";
		}
		if (!held.isEmpty() && !EntityType.by(net.minecraft.world.level.storage.TagValueInput.create(ProblemReporter.DISCARDING,
			sp.level().registryAccess(), held.get(0))).map(t -> t == entity.getType()).orElse(false)) {
			return "other_kind";
		}
		if (tier == Tier.LESSER && (!(entity instanceof Animal) || entity instanceof Enemy)) {
			return "lesser";
		}
		if (entity instanceof OwnableEntity pet && pet.getOwnerReference() != null && !pet.getOwnerReference().getUUID().equals(sp.getUUID())) {
			return "not_yours";
		}
		if (entity instanceof Enemy && mob.getHealth() > mob.getMaxHealth() / 2) {
			return "struggles";
		}
		return null;
	}

	/** The vis a binding costs: a little for an animal, more for a person or a pet, most for something hostile. */
	static AspectList cost(Entity entity) {
		int each = entity instanceof Enemy ? 25
			: entity instanceof net.minecraft.world.entity.npc.villager.AbstractVillager || entity instanceof OwnableEntity
				|| entity instanceof net.minecraft.world.entity.animal.equine.AbstractHorse ? 12 : 5;
		AspectList cost = new AspectList().add(Aspect.ORDER, each * 100).add(Aspect.AIR, each * 100);
		if (entity instanceof Enemy) {
			cost.add(Aspect.ENTROPY, 300);
		}
		return cost;
	}

	/** A creature's kind, from its saved form, for tooltips: "Cow". */
	public static Component kindOf(CompoundTag tag) {
		return EntityType.by(net.minecraft.world.level.storage.TagValueInput.create(ProblemReporter.DISCARDING,
				net.minecraft.core.RegistryAccess.EMPTY, tag)).map(EntityType::getDescription)
			.orElse(Component.literal("?"));
	}

	private Vessels() {
	}
}
