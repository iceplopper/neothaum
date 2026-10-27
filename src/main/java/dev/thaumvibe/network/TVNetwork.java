package dev.thaumvibe.network;

import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.aspects.ObjectAspects;
import dev.thaumvibe.menu.BaublesMenu;
import dev.thaumvibe.research.ResearchItem;
import dev.thaumvibe.research.ResearchManager;
import dev.thaumvibe.research.ScanManager;
import dev.thaumvibe.tiles.ResearchTableBlockEntity;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

/** Port of thaumcraft.common.lib.network.PacketHandler and its packets. */
public final class TVNetwork {
	// ------------------------------------------------------------------ clientbound

	public record ObjectAspectsSync(Map<Identifier, AspectList> table) implements CustomPacketPayload {
		public static final Type<ObjectAspectsSync> TYPE = new Type<>(Thaumvibe.id("object_aspects"));
		public static final StreamCodec<ByteBuf, ObjectAspectsSync> CODEC = ByteBufCodecs.map(LinkedHashMap::new, Identifier.STREAM_CODEC,
			AspectList.STREAM_CODEC).map(m -> new ObjectAspectsSync(m), s -> new LinkedHashMap<>(s.table()));

		@Override
		public Type<ObjectAspectsSync> type() {
			return TYPE;
		}
	}

	public record RecipeSync(List<Identifier> ids, List<Recipe<?>> recipes) implements CustomPacketPayload {
		public static final Type<RecipeSync> TYPE = new Type<>(Thaumvibe.id("recipes"));
		public static final StreamCodec<RegistryFriendlyByteBuf, RecipeSync> CODEC = StreamCodec.composite(
			Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), RecipeSync::ids, Recipe.STREAM_CODEC.apply(ByteBufCodecs.list()), RecipeSync::recipes,
			RecipeSync::new);

		@Override
		public Type<RecipeSync> type() {
			return TYPE;
		}
	}

	public record ResearchComplete(String key) implements CustomPacketPayload {
		public static final Type<ResearchComplete> TYPE = new Type<>(Thaumvibe.id("research_complete"));
		public static final StreamCodec<ByteBuf, ResearchComplete> CODEC = ByteBufCodecs.STRING_UTF8.map(ResearchComplete::new, ResearchComplete::key);

		@Override
		public Type<ResearchComplete> type() {
			return TYPE;
		}
	}

	public record AspectDiscovery(Aspect aspect) implements CustomPacketPayload {
		public static final Type<AspectDiscovery> TYPE = new Type<>(Thaumvibe.id("aspect_discovery"));
		public static final StreamCodec<ByteBuf, AspectDiscovery> CODEC = Aspect.STREAM_CODEC.map(AspectDiscovery::new, AspectDiscovery::aspect);

		@Override
		public Type<AspectDiscovery> type() {
			return TYPE;
		}
	}

	public record AspectPool(Aspect aspect, int amount, int total) implements CustomPacketPayload {
		public static final Type<AspectPool> TYPE = new Type<>(Thaumvibe.id("aspect_pool"));
		public static final StreamCodec<ByteBuf, AspectPool> CODEC = StreamCodec.composite(Aspect.STREAM_CODEC, AspectPool::aspect, ByteBufCodecs.VAR_INT,
			AspectPool::amount, ByteBufCodecs.VAR_INT, AspectPool::total, AspectPool::new);

		@Override
		public Type<AspectPool> type() {
			return TYPE;
		}
	}

	/**
	 * Block FX: kind 0 = sparkle (PacketFXBlockSparkle), 1 = crucible bubble burst, 2 = zap (PacketFXBlockZap), 3 = crucible boil, 4 = essentia
	 * drawn from a source at target (PacketFXEssentiaSource, value = colour), 5 = infusion source at target (PacketFXInfusionSource, value =
	 * the entity whose experience is drained, or 0), 6 = bubbles round a block (PacketFXBlockBubble, value = colour).
	 */
	public record BlockFx(int kind, BlockPos pos, BlockPos target, int value) implements CustomPacketPayload {
		public static final Type<BlockFx> TYPE = new Type<>(Thaumvibe.id("block_fx"));
		public static final StreamCodec<ByteBuf, BlockFx> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, BlockFx::kind, BlockPos.STREAM_CODEC,
			BlockFx::pos, BlockPos.STREAM_CODEC, BlockFx::target, ByteBufCodecs.INT, BlockFx::value, BlockFx::new);

		@Override
		public Type<BlockFx> type() {
			return TYPE;
		}
	}

	// ------------------------------------------------------------------ serverbound

	/** PacketScannedToServer. kind: 1 item (id), 2 entity (network id), 3 node (pos). */
	public record Scan(int kind, Optional<Identifier> item, int entity, BlockPos pos) implements CustomPacketPayload {
		public static final Type<Scan> TYPE = new Type<>(Thaumvibe.id("scan"));
		public static final StreamCodec<ByteBuf, Scan> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Scan::kind,
			ByteBufCodecs.optional(Identifier.STREAM_CODEC), Scan::item, ByteBufCodecs.VAR_INT, Scan::entity, BlockPos.STREAM_CODEC, Scan::pos, Scan::new);

		@Override
		public Type<Scan> type() {
			return TYPE;
		}
	}

	/** PacketPlayerCompleteToServer. type 0 = spend aspects to complete, 1 = create a research note. */
	public record StartResearch(String key, int kind) implements CustomPacketPayload {
		public static final Type<StartResearch> TYPE = new Type<>(Thaumvibe.id("start_research"));
		public static final StreamCodec<ByteBuf, StartResearch> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, StartResearch::key,
			ByteBufCodecs.VAR_INT, StartResearch::kind, StartResearch::new);

		@Override
		public Type<StartResearch> type() {
			return TYPE;
		}
	}

	/** PacketAspectPlaceToServer (aspect empty = erase). */
	public record PlaceAspect(BlockPos pos, int q, int r, Optional<Aspect> aspect) implements CustomPacketPayload {
		public static final Type<PlaceAspect> TYPE = new Type<>(Thaumvibe.id("place_aspect"));
		public static final StreamCodec<ByteBuf, PlaceAspect> CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, PlaceAspect::pos, ByteBufCodecs.VAR_INT,
			PlaceAspect::q, ByteBufCodecs.VAR_INT, PlaceAspect::r, ByteBufCodecs.optional(Aspect.STREAM_CODEC), PlaceAspect::aspect, PlaceAspect::new);

		@Override
		public Type<PlaceAspect> type() {
			return TYPE;
		}
	}

	/** PacketAspectCombinationToServer. */
	public record CombineAspects(BlockPos pos, Aspect a1, Aspect a2, boolean ab1, boolean ab2) implements CustomPacketPayload {
		public static final Type<CombineAspects> TYPE = new Type<>(Thaumvibe.id("combine_aspects"));
		public static final StreamCodec<ByteBuf, CombineAspects> CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, CombineAspects::pos, Aspect.STREAM_CODEC,
			CombineAspects::a1, Aspect.STREAM_CODEC, CombineAspects::a2, ByteBufCodecs.BOOL, CombineAspects::ab1, ByteBufCodecs.BOOL, CombineAspects::ab2,
			CombineAspects::new);

		@Override
		public Type<CombineAspects> type() {
			return TYPE;
		}
	}

	/** PacketWarpMessage: type 0 permanent, 1 sticky, 2 temporary; the amount gained (or lost, if negative). */
	public record WarpMessage(int warpType, int amount) implements CustomPacketPayload {
		public static final Type<WarpMessage> TYPE = new Type<>(Thaumvibe.id("warp_message"));
		public static final StreamCodec<ByteBuf, WarpMessage> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, WarpMessage::warpType,
			ByteBufCodecs.INT, WarpMessage::amount, WarpMessage::new);

		@Override
		public Type<WarpMessage> type() {
			return TYPE;
		}
	}

	/** PacketRunicCharge: the player's runic shield, for the bar over the hotbar. */
	public record RunicCharge(int charge, int max) implements CustomPacketPayload {
		public static final Type<RunicCharge> TYPE = new Type<>(Thaumvibe.id("runic_charge"));
		public static final StreamCodec<ByteBuf, RunicCharge> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, RunicCharge::charge,
			ByteBufCodecs.VAR_INT, RunicCharge::max, RunicCharge::new);

		@Override
		public Type<RunicCharge> type() {
			return TYPE;
		}
	}

	/** PacketFXShield: the runic shield flaring round an entity, facing what hit it (-1 all round, -2 below, -3 above). */
	public record Shield(int source, int target) implements CustomPacketPayload {
		public static final Type<Shield> TYPE = new Type<>(Thaumvibe.id("shield"));
		public static final StreamCodec<ByteBuf, Shield> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Shield::source, ByteBufCodecs.INT,
			Shield::target, Shield::new);

		@Override
		public Type<Shield> type() {
			return TYPE;
		}
	}

	/**
	 * Entity FX: kind 0 = lightning bolt from source to target (PacketFXWispZap, value = bolt type), 1 = burst at the source (value = size x 100),
	 * 2 = pech blast trail, 3 = taint splash at the source, 4 = chain lightning zap from source to target.
	 */
	public record EntityFx(int kind, int source, int target, int value) implements CustomPacketPayload {
		public static final Type<EntityFx> TYPE = new Type<>(Thaumvibe.id("entity_fx"));
		public static final StreamCodec<ByteBuf, EntityFx> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, EntityFx::kind, ByteBufCodecs.VAR_INT,
			EntityFx::source, ByteBufCodecs.INT, EntityFx::target, ByteBufCodecs.INT, EntityFx::value, EntityFx::new);

		@Override
		public Type<EntityFx> type() {
			return TYPE;
		}
	}

	/** PacketMiscEvent: 0 = warp heartbeat, 1 = long fog/mist, 2 = short fog/mist. */
	public record MiscEvent(short eventType) implements CustomPacketPayload {
		public static final Type<MiscEvent> TYPE = new Type<>(Thaumvibe.id("misc_event"));
		public static final StreamCodec<ByteBuf, MiscEvent> CODEC = ByteBufCodecs.SHORT.map(MiscEvent::new, MiscEvent::eventType);

		@Override
		public Type<MiscEvent> type() {
			return TYPE;
		}
	}

	/** PacketFocusChangeToServer: fit the focus with this sorting key ("REMOVE" stows the current one). */
	public record FocusChange(String key) implements CustomPacketPayload {
		public static final Type<FocusChange> TYPE = new Type<>(Thaumvibe.id("focus_change"));
		public static final StreamCodec<ByteBuf, FocusChange> CODEC = ByteBufCodecs.STRING_UTF8.map(FocusChange::new, FocusChange::key);

		@Override
		public Type<FocusChange> type() {
			return TYPE;
		}
	}

	/** PacketItemKeyToServer (key 1): the misc key, which sizes an architect focus's area. */
	public record WandMisc() implements CustomPacketPayload {
		public static final Type<WandMisc> TYPE = new Type<>(Thaumvibe.id("wand_misc"));
		public static final StreamCodec<ByteBuf, WandMisc> CODEC = StreamCodec.unit(new WandMisc());

		@Override
		public Type<WandMisc> type() {
			return TYPE;
		}
	}

	public record OpenBaubles() implements CustomPacketPayload {
		public static final Type<OpenBaubles> TYPE = new Type<>(Thaumvibe.id("open_baubles"));
		public static final StreamCodec<ByteBuf, OpenBaubles> CODEC = StreamCodec.unit(new OpenBaubles());

		@Override
		public Type<OpenBaubles> type() {
			return TYPE;
		}
	}

	public record ToggleHarness() implements CustomPacketPayload {
		public static final Type<ToggleHarness> TYPE = new Type<>(Thaumvibe.id("toggle_harness"));
		public static final StreamCodec<ByteBuf, ToggleHarness> CODEC = StreamCodec.unit(new ToggleHarness());

		@Override
		public Type<ToggleHarness> type() {
			return TYPE;
		}
	}

	public record GolemButton(int golemId, int buttonId, int value) implements CustomPacketPayload {
		public static final Type<GolemButton> TYPE = new Type<>(Thaumvibe.id("golem_button"));
		public static final StreamCodec<ByteBuf, GolemButton> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, GolemButton::golemId,
			ByteBufCodecs.VAR_INT, GolemButton::buttonId,
			ByteBufCodecs.VAR_INT, GolemButton::value,
			GolemButton::new);

		@Override
		public Type<GolemButton> type() {
			return TYPE;
		}
	}

	// ------------------------------------------------------------------ client hooks (installed by the client entrypoint)

	public static Consumer<CustomPacketPayload> clientSender = p -> {
	};
	public static Consumer<Component> clientNotifier = c -> {
	};

	public static void init() {
		var s2c = PayloadTypeRegistry.clientboundPlay();
		s2c.registerLarge(ObjectAspectsSync.TYPE, ObjectAspectsSync.CODEC, 8 * 1024 * 1024);
		s2c.registerLarge(RecipeSync.TYPE, RecipeSync.CODEC, 8 * 1024 * 1024);
		s2c.register(ResearchComplete.TYPE, ResearchComplete.CODEC);
		s2c.register(AspectDiscovery.TYPE, AspectDiscovery.CODEC);
		s2c.register(AspectPool.TYPE, AspectPool.CODEC);
		s2c.register(BlockFx.TYPE, BlockFx.CODEC);
		s2c.register(WarpMessage.TYPE, WarpMessage.CODEC);
		s2c.register(RunicCharge.TYPE, RunicCharge.CODEC);
		s2c.register(Shield.TYPE, Shield.CODEC);
		s2c.register(EntityFx.TYPE, EntityFx.CODEC);
		s2c.register(MiscEvent.TYPE, MiscEvent.CODEC);
		var c2s = PayloadTypeRegistry.serverboundPlay();
		c2s.register(Scan.TYPE, Scan.CODEC);
		c2s.register(StartResearch.TYPE, StartResearch.CODEC);
		c2s.register(PlaceAspect.TYPE, PlaceAspect.CODEC);
		c2s.register(CombineAspects.TYPE, CombineAspects.CODEC);
		c2s.register(OpenBaubles.TYPE, OpenBaubles.CODEC);
		c2s.register(ToggleHarness.TYPE, ToggleHarness.CODEC);
		c2s.register(FocusChange.TYPE, FocusChange.CODEC);
		c2s.register(WandMisc.TYPE, WandMisc.CODEC);
		c2s.register(GolemButton.TYPE, GolemButton.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(GolemButton.TYPE, (p, ctx) -> {
			if (ctx.player().level().getEntity(p.golemId()) instanceof dev.thaumvibe.golem.GolemEntity golem
				&& ctx.player().distanceToSqr(golem) < 64) {
				golem.handleButton(p.buttonId(), p.value());
			}
		});
		ServerPlayNetworking.registerGlobalReceiver(FocusChange.TYPE, (p, ctx) -> {
			var held = ctx.player().getMainHandItem();
			// KeyHandler: a sceptre takes no focus
			if (held.getItem() instanceof dev.thaumvibe.items.WandItem && !dev.thaumvibe.items.WandItem.isSceptre(held)) {
				dev.thaumvibe.wands.WandFoci.changeFocus(held, ctx.player(), p.key());
			}
		});
		ServerPlayNetworking.registerGlobalReceiver(WandMisc.TYPE, (p, ctx) -> {
			var held = ctx.player().getMainHandItem();
			if (held.getItem() instanceof dev.thaumvibe.items.WandItem) {
				dev.thaumvibe.wands.WandFoci.toggleMisc(held, ctx.player());
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(Scan.TYPE, (p, ctx) -> handleScan(p, ctx.player()));
		ServerPlayNetworking.registerGlobalReceiver(StartResearch.TYPE, (p, ctx) -> handleStartResearch(p, ctx.player()));
		ServerPlayNetworking.registerGlobalReceiver(PlaceAspect.TYPE, (p, ctx) -> {
			if (ctx.player().level().getBlockEntity(p.pos()) instanceof ResearchTableBlockEntity rt && ctx.player().distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(p.pos())) < 64) {
				rt.placeAspect(p.q(), p.r(), p.aspect().orElse(null), ctx.player());
			}
		});
		ServerPlayNetworking.registerGlobalReceiver(CombineAspects.TYPE, (p, ctx) -> {
			if (ctx.player().level().getBlockEntity(p.pos()) instanceof ResearchTableBlockEntity rt && ctx.player().distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(p.pos())) < 64) {
				rt.combine(ctx.player(), p.a1(), p.a2(), p.ab1(), p.ab2());
			}
		});
		ServerPlayNetworking.registerGlobalReceiver(OpenBaubles.TYPE, (p, ctx) -> ctx.player().openMenu(
			new SimpleMenuProvider((id, inv, pl) -> new BaublesMenu(id, inv), Component.translatable("container.inventory"))));
		ServerPlayNetworking.registerGlobalReceiver(ToggleHarness.TYPE, (p, ctx) -> {
			var player = ctx.player();
			var chest = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
			if (chest.getItem() instanceof dev.thaumvibe.items.equipment.ThaumostaticHarnessItem harness) {
				harness.toggleHover(player, chest);
			}
		});
	}

	private static void handleScan(Scan p, ServerPlayer player) {
		ScanManager.ScanResult scan = switch (p.kind()) {
			case 1 -> p.item().flatMap(BuiltInRegistries.ITEM::getOptional).map(ScanManager.ScanResult::item).orElse(null);
			case 2 -> {
				Entity e = player.level().getEntity(p.entity());
				yield e != null && e.distanceToSqr(player) < 256 ? ScanManager.ScanResult.entity(e) : null;
			}
			case 3 -> player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(p.pos())) < 64 ? ScanManager.ScanResult.node(p.pos()) : null;
			case 4 -> p.item().flatMap(BuiltInRegistries.BLOCK::getOptional).map(ScanManager.ScanResult::block).orElse(null);
			default -> null;
		};
		if (scan != null && ScanManager.isValidScanTarget(player, scan, "@")) {
			ScanManager.completeScan(player, scan, "@");
		}
	}

	private static void handleStartResearch(StartResearch p, ServerPlayer player) {
		ResearchItem ri = ResearchManager.getResearch(p.key());
		if (ri == null || ResearchManager.isResearchComplete(player, p.key())) {
			return;
		}
		if (!ResearchManager.doesPlayerHaveRequisites(player, p.key())) {
			player.sendSystemMessage(Component.translatable("tc.researcherror"));
			return;
		}
		if (p.kind() == 1) {
			ResearchManager.createResearchNoteForPlayer(player.level(), player, p.key());
		} else {
			var k = ResearchManager.knowledge(player).copy();
			for (Aspect a : ri.tags.getAspects()) {
				if (k.getAspectPool(a) < ri.tags.getAmount(a)) {
					return;
				}
			}
			for (Aspect a : ri.tags.getAspects()) {
				k.addAspectPool(a, -ri.tags.getAmount(a));
			}
			ResearchManager.setKnowledge(player, k);
			ResearchManager.completeResearch(player, p.key());
			sendResearchComplete(player, p.key());
			for (String sibling : ri.siblings) {
				if (!ResearchManager.isResearchComplete(player, sibling) && ResearchManager.doesPlayerHaveRequisites(player, sibling)) {
					ResearchManager.completeResearch(player, sibling);
					sendResearchComplete(player, sibling);
				}
			}
		}
		player.level().playSound(null, player.getX(), player.getY(), player.getZ(), dev.thaumvibe.registry.TVSounds.LEARN,
			net.minecraft.sounds.SoundSource.PLAYERS, 0.75F, 1.0F);
	}

	// ------------------------------------------------------------------ senders

	public static void sendObjectAspects(ServerPlayer player) {
		Map<Identifier, AspectList> m = new LinkedHashMap<>();
		for (Map.Entry<Item, AspectList> e : ObjectAspects.table().entrySet()) {
			m.put(BuiltInRegistries.ITEM.getKey(e.getKey()), e.getValue());
		}
		ServerPlayNetworking.send(player, new ObjectAspectsSync(m));
		sendRecipes(player);
	}

	/** Recipes outside our namespace that Thaumonomicon pages show. */
	public static final java.util.Set<Identifier> EXTRA_SYNCED_RECIPES = new java.util.HashSet<>();

	/** Thaumcraft recipes the client needs for the Thaumonomicon and JEI (26.x no longer sends recipes to clients). */
	public static void sendRecipes(ServerPlayer player) {
		List<Identifier> ids = new ArrayList<>();
		List<Recipe<?>> recipes = new ArrayList<>();
		for (RecipeHolder<?> h : player.level().getServer().getRecipeManager().getRecipes()) {
			Identifier id = h.id().identifier();
			if (id.getNamespace().equals(Thaumvibe.MOD_ID) || EXTRA_SYNCED_RECIPES.contains(id)) {
				ids.add(id);
				recipes.add(h.value());
			}
		}
		ServerPlayNetworking.send(player, new RecipeSync(ids, recipes));
	}

	public static void sendResearchComplete(ServerPlayer player, String key) {
		ServerPlayNetworking.send(player, new ResearchComplete(key));
	}

	public static void sendAspectDiscovery(ServerPlayer player, Aspect aspect) {
		ServerPlayNetworking.send(player, new AspectDiscovery(aspect));
	}

	public static void sendAspectPool(ServerPlayer player, Aspect aspect, int amount, int total) {
		ServerPlayNetworking.send(player, new AspectPool(aspect, amount, total));
	}

	private static void around(Level level, BlockPos pos, CustomPacketPayload payload) {
		if (level instanceof ServerLevel sl) {
			for (ServerPlayer p : PlayerLookup.around(sl, pos, 32.0)) {
				ServerPlayNetworking.send(p, payload);
			}
		}
	}

	private static final java.util.Map<BlockPos, Long> VIS_EFFECT_BUFFER = new java.util.concurrent.ConcurrentHashMap<>();

	/** Utils.generateVisEffect: at most one vis drain mote every half second per machine. */
	public static void sendVisEffect(Level level, BlockPos machine, BlockPos node, int primal) {
		long time = System.currentTimeMillis();
		Long until = VIS_EFFECT_BUFFER.get(machine);
		if (until != null) {
			if (until < time) {
				VIS_EFFECT_BUFFER.remove(machine);
			}
			return;
		}
		VIS_EFFECT_BUFFER.put(machine.immutable(), time + 500L + level.getRandom().nextInt(100));
		around(level, machine, new BlockFx(20, machine, node, primal));
	}

	public static void sendBlockSparkle(Level level, BlockPos pos, int color) {
		around(level, pos, new BlockFx(0, pos, pos, color));
	}

	public static void sendZap(Level level, BlockPos from, BlockPos to) {
		around(level, to, new BlockFx(2, from, to, 0));
	}

	public static void sendEssentiaSource(Level level, BlockPos pos, BlockPos source, int color) {
		around(level, pos, new BlockFx(4, pos, source, color));
	}

	public static void sendInfusionSource(Level level, BlockPos matrix, BlockPos source, int entity) {
		around(level, matrix, new BlockFx(5, matrix, source, entity));
	}

	public static void sendBubbles(Level level, BlockPos pos, int color) {
		around(level, pos, new BlockFx(6, pos, pos, color));
	}

	public static void sendShield(Level level, net.minecraft.world.entity.Entity entity, int target) {
		if (level instanceof ServerLevel sl) {
			for (ServerPlayer p : PlayerLookup.around(sl, entity.position(), 64.0)) {
				ServerPlayNetworking.send(p, new Shield(entity.getId(), target));
			}
		}
	}

	public static void sendEntityFx(net.minecraft.world.entity.Entity entity, int kind, int target, int value) {
		if (entity.level() instanceof ServerLevel sl) {
			for (ServerPlayer p : PlayerLookup.around(sl, entity.position(), 32.0)) {
				ServerPlayNetworking.send(p, new EntityFx(kind, entity.getId(), target, value));
			}
		}
	}

	public static void sendCrucibleBoil(Level level, BlockPos pos, int amount) {
		around(level, pos, new BlockFx(3, pos, pos, amount));
	}

	/** Client side: send the confirmed thaumometer scan. */
	public static void sendScanToServer(ScanManager.ScanResult scan) {
		Scan p = switch (scan.type()) {
			case 1 -> new Scan(1, Optional.of(BuiltInRegistries.ITEM.getKey(scan.item())), 0, BlockPos.ZERO);
			case 2 -> new Scan(2, Optional.empty(), scan.entity().getId(), BlockPos.ZERO);
			case 4 -> new Scan(4, Optional.of(Identifier.parse(scan.phenomena().substring(6))), 0, BlockPos.ZERO);
			default -> {
				String[] s = scan.phenomena().substring(4).split(":");
				yield new Scan(3, Optional.empty(), 0, new BlockPos(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2])));
			}
		};
		clientSender.accept(p);
	}

	public static void clientNotify(Component c) {
		clientNotifier.accept(c);
	}

	private TVNetwork() {
	}
}
