package dev.neothaum.addon.totems;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.neothaum.NeoThaum;
import dev.neothaum.addon.Additions;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVItems;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

/**
 * NeoThaum Addition: Traveller's Totems. Arcane stone totems, found rarely across the overworld and later raised by players, that remember one
 * another: touch one to learn it, and from any you know you can travel to any other you know, free, after standing still a moment. A Home Totem
 * is a lesser one that anyone can claim with a wand as their home: you can travel to it, never from it. Late on, the Wayfarer's Compass carries
 * the bearings of five totems and takes you to them from anywhere. Each totem is named in Latin for what is round it, afresh wherever it is
 * placed; only a thaumium pickaxe or better can take one up.
 */
public final class TravellersTotems {
	public static final TagKey<Item> BREAKS_TOTEMS = TagKey.create(Registries.ITEM, NeoThaum.id("breaks_totems"));
	/** The compass holds this many totems. */
	public static final int COMPASS_SLOTS = 5;

	public static final Block TOTEM = TVBlocks.register("travellers_totem", p -> new TotemBlock(false, p), props(), false);
	public static final Block HOME_TOTEM = TVBlocks.register("home_totem", p -> new TotemBlock(true, p), props(), false);
	public static final BlockEntityType<TotemBlockEntity> TOTEM_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
		NeoThaum.id("travellers_totem"), new BlockEntityType<>(TotemBlockEntity::new, Set.of(TOTEM, HOME_TOTEM)));
	/** The totems a Wayfarer's Compass carries. */
	public static final DataComponentType<List<GlobalPos>> COMPASS_TOTEMS = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
		NeoThaum.id("compass_totems"), DataComponentType.<List<GlobalPos>>builder().persistent(GlobalPos.CODEC.listOf())
			.networkSynchronized(GlobalPos.STREAM_CODEC.apply(ByteBufCodecs.list())).build());
	public static Item totemItem;
	public static Item homeTotemItem;
	public static Item compass;

	/** A player's totems: the ones they have learned, their Home Totem, and whether they have been told how to claim one. */
	public record Known(List<GlobalPos> learned, Optional<GlobalPos> home, boolean reminded) {
		public static final Codec<Known> CODEC = RecordCodecBuilder.create(i -> i.group(
			GlobalPos.CODEC.listOf().fieldOf("learned").forGetter(Known::learned), GlobalPos.CODEC.optionalFieldOf("home").forGetter(Known::home),
			Codec.BOOL.optionalFieldOf("reminded", false).forGetter(Known::reminded)).apply(i, Known::new));

		static Known empty() {
			return new Known(List.of(), Optional.empty(), false);
		}

		public Known learn(GlobalPos pos) {
			List<GlobalPos> l = new ArrayList<>(this.learned);
			if (!l.contains(pos)) {
				l.add(pos);
			}
			return new Known(l, this.home, this.reminded);
		}

		public Known forget(GlobalPos pos) {
			List<GlobalPos> l = new ArrayList<>(this.learned);
			l.remove(pos);
			return new Known(l, this.home.filter(h -> !h.equals(pos)), this.reminded);
		}

		public Known withHome(Optional<GlobalPos> home) {
			return new Known(this.learned, home, this.reminded);
		}

		public Known withReminded() {
			return new Known(this.learned, this.home, true);
		}
	}

	public static final AttachmentType<Known> KNOWN = AttachmentRegistry.<Known>builder().persistent(Known.CODEC).copyOnDeath()
		.initializer(Known::empty).buildAndRegister(NeoThaum.id("totems"));

	// ------------------------------------------------------------------ network

	/** One entry in the travel list. */
	public record Destination(GlobalPos pos, String name, boolean home) {
		static final StreamCodec<ByteBuf, Destination> CODEC = StreamCodec.composite(GlobalPos.STREAM_CODEC, Destination::pos,
			ByteBufCodecs.STRING_UTF8, Destination::name, ByteBufCodecs.BOOL, Destination::home, Destination::new);
	}

	/** Server to client: open the travel list, from a totem ({@code from} its position) or the compass (no position). */
	public record OpenTravel(Optional<GlobalPos> from, List<Destination> destinations) implements CustomPacketPayload {
		public static final Type<OpenTravel> TYPE = new Type<>(NeoThaum.id("totem_travel_list"));
		public static final StreamCodec<ByteBuf, OpenTravel> CODEC = StreamCodec.composite(ByteBufCodecs.optional(GlobalPos.STREAM_CODEC),
			OpenTravel::from, Destination.CODEC.apply(ByteBufCodecs.list()), OpenTravel::destinations, OpenTravel::new);

		@Override
		public Type<OpenTravel> type() {
			return TYPE;
		}
	}

	/** Client to server: travel to this totem (by the compass when {@code compass}). */
	public record TravelTo(GlobalPos target, boolean compass) implements CustomPacketPayload {
		public static final Type<TravelTo> TYPE = new Type<>(NeoThaum.id("totem_travel"));
		public static final StreamCodec<ByteBuf, TravelTo> CODEC = StreamCodec.composite(GlobalPos.STREAM_CODEC, TravelTo::target,
			ByteBufCodecs.BOOL, TravelTo::compass, TravelTo::new);

		@Override
		public Type<TravelTo> type() {
			return TYPE;
		}
	}

	public static boolean enabled() {
		return Additions.enabled("travellers_totems");
	}

	private static BlockBehaviour.Properties props() {
		// as hard as obsidian; only a thaumium pickaxe or better makes any headway (TotemBlock.getDestroyProgress)
		return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
			.strength(50.0F, 1200.0F).sound(SoundType.STONE).noOcclusion().lightLevel(s -> 6);
	}

	/** Loads the class (registering the blocks) from NeoThaum.onInitialize, right after TVBlocks. */
	public static void registerBlocks() {
		TOTEM.getClass();
	}

	/** After TVItems: the items, packets and travel. */
	public static void init() {
		totemItem = blockItem(TOTEM);
		homeTotemItem = blockItem(HOME_TOTEM);
		var ckey = ResourceKey.create(Registries.ITEM, NeoThaum.id("wayfarers_compass"));
		compass = Registry.register(BuiltInRegistries.ITEM, ckey, new WayfarersCompassItem(new Item.Properties().setId(ckey).stacksTo(1)
			.rarity(net.minecraft.world.item.Rarity.RARE).component(COMPASS_TOTEMS, List.of())));
		for (Item i : new Item[]{totemItem, homeTotemItem, compass}) {
			TVItems.TAB_ORDER.add(i);
			Additions.hideWith(i, "travellers_totems");
		}
		PayloadTypeRegistry.clientboundPlay().register(OpenTravel.TYPE, OpenTravel.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(TravelTo.TYPE, TravelTo.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(TravelTo.TYPE, (p, ctx) -> TotemTravel.request(ctx.player(), p.target(), p.compass()));
		TotemTravel.init();
	}

	private static Item blockItem(Block b) {
		var key = ResourceKey.create(Registries.ITEM, BuiltInRegistries.BLOCK.getKey(b));
		return Registry.register(BuiltInRegistries.ITEM, key, new BlockItem(b, new Item.Properties().setId(key).useBlockDescriptionPrefix()));
	}

	private TravellersTotems() {
	}
}
