package dev.thaumvibe.devtools;

import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.api.nodes.NodeModifier;
import dev.thaumvibe.api.nodes.NodeType;
import dev.thaumvibe.tiles.NodeBlockEntity;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Blocks;

/**
 * Developer tools for testing Thaumvibe (not part of Thaumcraft, and left out of release builds): node placers for every node type, a
 * node editor, and shortcuts for research, aspects, wand vis and scanning. They live in their own creative tab, "TC: Dev Tools", and
 * only work for players in creative mode.
 * Their tab sits on the bottom row of the creative inventory, after the five Thaumcraft tabs.
 */
public final class DevTools {
	public static final List<Item> ITEMS = new ArrayList<>();
	public static final Item[] NODE_PLACERS = new Item[NodeType.values().length];

	static {
		for (NodeType t : NodeType.values()) {
			NODE_PLACERS[t.ordinal()] = register("dev_node_placer_" + t.name().toLowerCase(), p -> new NodePlacerItem(t, p));
		}
	}

	public static final Item NODE_EDITOR = register("dev_node_editor", NodeEditorItem::new);
	public static final Item RESEARCH_TOOL = register("dev_research_tool", ResearchToolItem::new);
	public static final Item ASPECT_TOOL = register("dev_aspect_tool", AspectToolItem::new);
	public static final Item WAND_CHARGER = register("dev_wand_charger", WandChargerItem::new);
	public static final Item SCAN_TOOL = register("dev_scan_tool", ScanToolItem::new);
	public static final Item ESSENTIA_FILLER = register("dev_essentia_filler", EssentiaFillerItem::new);
	public static final Item WARP_TOOL = register("dev_warp_tool", WarpToolItem::new);
	public static final Item BIOME_PAINTER = register("dev_biome_painter", BiomePainterItem::new);
	public static final Item TICK_ACCELERATOR = register("dev_tick_accelerator", TickAcceleratorItem::new);

	public static final CreativeModeTab TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Thaumvibe.id("f_dev_tools"),
		FabricCreativeModeTab.builder()
			.title(Component.translatable("itemGroup.thaumvibe.f_dev_tools"))
			.icon(() -> new ItemStack(NODE_EDITOR))
			.displayItems((params, out) -> ITEMS.forEach(out::accept))
			.build());

	private static Item register(String name, Function<Item.Properties, Item> factory) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Thaumvibe.id(name));
		Item item = Registry.register(BuiltInRegistries.ITEM, key, factory.apply(new Item.Properties().setId(key).stacksTo(1).rarity(Rarity.EPIC)));
		ITEMS.add(item);
		return item;
	}

	// ------------------------------------------------------------------ node editor packet

	/** Client to server: set a node's type, modifier and aspects, or remove it. modifier -1 is none. */
	public record NodeEdit(BlockPos pos, int nodeType, int modifier, AspectList aspects, boolean delete) implements CustomPacketPayload {
		public static final Type<NodeEdit> TYPE = new Type<>(Thaumvibe.id("dev_node_edit"));
		public static final StreamCodec<ByteBuf, NodeEdit> CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, NodeEdit::pos, ByteBufCodecs.VAR_INT,
			NodeEdit::nodeType, ByteBufCodecs.INT, NodeEdit::modifier, AspectList.STREAM_CODEC, NodeEdit::aspects, ByteBufCodecs.BOOL, NodeEdit::delete,
			NodeEdit::new);

		@Override
		public Type<NodeEdit> type() {
			return TYPE;
		}
	}

	private static void handleNodeEdit(NodeEdit p, ServerPlayer player) {
		if (!player.isCreative() || player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(p.pos())) > 64 * 64) {
			return;
		}
		var level = player.level();
		if (!(level.getBlockEntity(p.pos()) instanceof NodeBlockEntity node)) {
			return;
		}
		if (p.delete()) {
			level.setBlock(p.pos(), Blocks.AIR.defaultBlockState(), 3);
			return;
		}
		node.setNodeType(NodeType.values()[Math.floorMod(p.nodeType(), NodeType.values().length)]);
		node.setNodeModifier(p.modifier() < 0 ? null : NodeModifier.values()[p.modifier() % NodeModifier.values().length]);
		AspectList clean = new AspectList();
		for (Aspect a : p.aspects().getAspects()) {
			if (p.aspects().getAmount(a) > 0) {
				clean.add(a, Math.min(1000, p.aspects().getAmount(a)));
			}
		}
		node.setAspects(clean);
		node.markForUpdate();
	}

	public static void init() {
		PayloadTypeRegistry.serverboundPlay().register(NodeEdit.TYPE, NodeEdit.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(NodeEdit.TYPE, (p, ctx) -> handleNodeEdit(p, ctx.player()));
	}

	private DevTools() {
	}
}
