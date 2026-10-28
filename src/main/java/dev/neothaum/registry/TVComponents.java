package dev.neothaum.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import dev.neothaum.NeoThaum;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.research.ResearchNoteData;
import java.util.function.UnaryOperator;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

/** Item data that Thaumcraft 4 kept in NBT. */
public final class TVComponents {
	/** ItemWandCasting "cap" tag. */
	public static final DataComponentType<String> WAND_CAP = register("wand_cap", b -> b.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));
	/** ItemWandCasting "rod" tag. */
	public static final DataComponentType<String> WAND_ROD = register("wand_rod", b -> b.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));
	/** Stored centi-vis per primal aspect (ItemWandCasting stored one int per aspect tag). */
	public static final DataComponentType<AspectList> WAND_VIS = register("wand_vis",
		b -> b.persistent(AspectList.CODEC).networkSynchronized(AspectList.STREAM_CODEC));
	/** IEssentiaContainerItem aspects (phials, crystals, wisps). */
	public static final DataComponentType<AspectList> ASPECTS = register("aspects",
		b -> b.persistent(AspectList.CODEC).networkSynchronized(AspectList.STREAM_CODEC));
	/** ItemResearchNotes data (research key, hex grid, completion). */
	public static final DataComponentType<ResearchNoteData> RESEARCH_NOTE = register("research_note",
		b -> b.persistent(ResearchNoteData.CODEC).networkSynchronized(ResearchNoteData.STREAM_CODEC));
	/** Scribing tools ink use (damage-like counter). */
	/** A jar's or alembic's label (TC4 "AspectFilter"), also carried by labelled jar labels. */
	public static final DataComponentType<dev.neothaum.api.aspects.Aspect> ASPECT_FILTER = register("aspect_filter",
		b -> b.persistent(dev.neothaum.api.aspects.Aspect.CODEC).networkSynchronized(dev.neothaum.api.aspects.Aspect.STREAM_CODEC));
	/** ItemElementalShovel "or": which way its 3x3 of placed blocks is laid (0 flat to the face, 1 upright, 2 the other upright). */
	public static final DataComponentType<Integer> ORIENTATION = register("orientation", b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
	/** "RS.HARDEN": levels of runic augment infused into runic gear. */
	public static final DataComponentType<Integer> RUNIC_HARDENING = register("runic_hardening",
		b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
	public static final DataComponentType<Boolean> SCEPTRE = register("sceptre", b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

	/** ItemWandCasting "focus": the focus fitted to a wand. */
	public static final DataComponentType<net.minecraft.world.item.ItemStack> WAND_FOCUS = register("wand_focus",
		b -> b.persistent(net.minecraft.world.item.ItemStack.CODEC).networkSynchronized(net.minecraft.world.item.ItemStack.STREAM_CODEC));
	/** ItemFocusBasic "upgrade": the five upgrade slots (-1 when empty). */
	public static final DataComponentType<java.util.List<Integer>> FOCUS_UPGRADES = register("focus_upgrades",
		b -> b.persistent(Codec.INT.listOf()).networkSynchronized(ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list())));
	/** ItemFocusTrade "picked": the block the equal trade focus swaps in. */
	public static final DataComponentType<net.minecraft.world.item.ItemStack> PICKED_BLOCK = register("picked_block",
		b -> b.persistent(net.minecraft.world.item.ItemStack.CODEC).networkSynchronized(net.minecraft.world.item.ItemStack.STREAM_CODEC));
	/** WandManager area "areax/areay/areaz/areadim": the architect area of a wand. */
	public static final DataComponentType<java.util.List<Integer>> WAND_AREA = register("wand_area",
		b -> b.persistent(Codec.INT.listOf()).networkSynchronized(ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list())));

	/** ItemJarNode "nodetype": the trapped node's type (NodeType ordinal). */
	public static final DataComponentType<Integer> NODE_TYPE = register("node_type", b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
	/** ItemJarNode "nodemod": the trapped node's modifier (NodeModifier ordinal; absent for none). */
	public static final DataComponentType<Integer> NODE_MODIFIER = register("node_modifier",
		b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

	/** ItemFortressArmor goggles upgrade. */
	public static final DataComponentType<Boolean> GOGGLES = register("goggles", b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
	/** ItemFortressArmor mask (0: Grinning Devil, 1: Angry Ghost, 2: Sipping Fiend). */
	public static final DataComponentType<Integer> MASK = register("mask", b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
	/** TileBanner color (-1: Cultist, 0..15: Thaumcraft colors). */
	public static final DataComponentType<Integer> BANNER_COLOR = register("banner_color", b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

	/** ItemHoverHarness fuel jar. */
	public static final DataComponentType<net.minecraft.world.item.ItemStack> HARNESS_JAR = register("harness_jar",
		b -> b.persistent(net.minecraft.world.item.ItemStack.CODEC).networkSynchronized(net.minecraft.world.item.ItemStack.STREAM_CODEC));
	/** ItemHoverHarness charge counter (0..360). */
	public static final DataComponentType<Integer> HARNESS_CHARGE = register("harness_charge",
		b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
	/** ItemHoverHarness hover mode active. */
	public static final DataComponentType<Boolean> HARNESS_ACTIVE = register("harness_active",
		b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
	/** Generic active state (e.g. Sinister Lodestone pointing to a sinister node). */
	public static final DataComponentType<Boolean> ACTIVE = register("active",
		b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

	/** Golem placer & entity data components. */
	public static final DataComponentType<Integer> GOLEM_TYPE = register("golem_type",
		b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
	public static final DataComponentType<Boolean> GOLEM_ADVANCED = register("golem_advanced",
		b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
	public static final DataComponentType<Integer> GOLEM_CORE = register("golem_core",
		b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
	public static final DataComponentType<java.util.List<Integer>> GOLEM_UPGRADES = register("golem_upgrades",
		b -> b.persistent(Codec.INT.listOf()).networkSynchronized(ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list())));
	public static final DataComponentType<String> GOLEM_DECORATION = register("golem_decoration",
		b -> b.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));
	/** ItemGolemBell / ItemGolemPlacer "markers". */
	public static final DataComponentType<java.util.List<dev.neothaum.golem.GolemMarker>> GOLEM_MARKERS = register("golem_markers",
		b -> b.persistent(dev.neothaum.golem.GolemMarker.LIST_CODEC).networkSynchronized(dev.neothaum.golem.GolemMarker.LIST_STREAM_CODEC));
	/** ItemGolemPlacer "Inventory": a picked-up golem's filter. */
	public static final DataComponentType<java.util.List<net.minecraft.world.item.ItemStack>> GOLEM_INVENTORY = register("golem_inventory",
		b -> b.persistent(net.minecraft.world.item.ItemStack.OPTIONAL_CODEC.listOf())
			.networkSynchronized(net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list())));
	public static final DataComponentType<Integer> GOLEM_BELL_ID = register("golem_bell_id",
		b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
	public static final DataComponentType<Integer> GOLEM_BELL_FACE = register("golem_bell_face",
		b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
	public static final DataComponentType<BlockPos> GOLEM_BELL_POS = register("golem_bell_pos",
		b -> b.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC));

	public record KeyData(BlockPos pos, int type) {
		public static final Codec<KeyData> CODEC = RecordCodecBuilder.create(i -> i.group(
			BlockPos.CODEC.fieldOf("pos").forGetter(KeyData::pos),
			Codec.INT.fieldOf("type").forGetter(KeyData::type)
		).apply(i, KeyData::new));
		public static final StreamCodec<ByteBuf, KeyData> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, KeyData::pos,
			ByteBufCodecs.VAR_INT, KeyData::type,
			KeyData::new);
	}
	/** Imprinted arcane key destination and type (0: door, 1: pressure plate). */
	public static final DataComponentType<KeyData> KEY_DATA = register("key_data",
		b -> b.persistent(KeyData.CODEC).networkSynchronized(KeyData.STREAM_CODEC));

	public record MirrorLink(Identifier dimension, BlockPos pos) {
		public static final Codec<MirrorLink> CODEC = RecordCodecBuilder.create(i -> i.group(
			Identifier.CODEC.fieldOf("dim").forGetter(MirrorLink::dimension),
			BlockPos.CODEC.fieldOf("pos").forGetter(MirrorLink::pos)
		).apply(i, MirrorLink::new));
		public static final StreamCodec<ByteBuf, MirrorLink> STREAM_CODEC = StreamCodec.composite(
			Identifier.STREAM_CODEC, MirrorLink::dimension,
			BlockPos.STREAM_CODEC, MirrorLink::pos,
			MirrorLink::new);
		public net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> levelKey() {
			return net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dimension);
		}
	}
	/** Magic mirror and hand mirror link destination. */
	public static final DataComponentType<MirrorLink> MIRROR_LINK = register("mirror_link",
		b -> b.persistent(MirrorLink.CODEC).networkSynchronized(MirrorLink.STREAM_CODEC));

	private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
		return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, NeoThaum.id(name), builder.apply(DataComponentType.builder()).build());
	}

	public static void init() {
	}
}
