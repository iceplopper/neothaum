package dev.neothaum.addon.totems;

import dev.neothaum.NeoThaum;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * The totems a player has attuned to (and their Home Totem), with their names, for the client's maps: vanilla map items, and Xaero's World
 * Map and Minimap. The client only knows where its totems are (TravellersTotems.KNOWN); this adds what they're called. Sent when the list
 * changes (checked every five seconds), and empty while the addition is off.
 */
public final class TotemMarks {
	/** Server to client: every totem this player is attuned to, and their Home Totem. */
	public record Marks(List<TravellersTotems.Destination> totems) implements CustomPacketPayload {
		public static final Type<Marks> TYPE = new Type<>(NeoThaum.id("totem_marks"));
		public static final StreamCodec<ByteBuf, Marks> CODEC = TravellersTotems.Destination.CODEC.apply(
			net.minecraft.network.codec.ByteBufCodecs.list()).map(Marks::new, Marks::totems);

		@Override
		public Type<Marks> type() {
			return TYPE;
		}
	}

	/** What each player was last sent, so the list goes again only when it changes. */
	private static final Map<UUID, List<TravellersTotems.Destination>> SENT = new HashMap<>();

	static void init() {
		PayloadTypeRegistry.clientboundPlay().register(Marks.TYPE, Marks.CODEC);
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % 100 == 0) {
				for (ServerPlayer p : server.getPlayerList().getPlayers()) {
					update(server, p);
				}
			}
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			SENT.remove(handler.player.getUUID());
			server.execute(() -> update(server, handler.player));
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> SENT.remove(handler.player.getUUID()));
	}

	private static void update(MinecraftServer server, ServerPlayer p) {
		List<TravellersTotems.Destination> now = TravellersTotems.enabled() ? of(server, p) : List.of();
		if (!now.equals(SENT.get(p.getUUID())) && ServerPlayNetworking.canSend(p, Marks.TYPE)) {
			SENT.put(p.getUUID(), now);
			ServerPlayNetworking.send(p, new Marks(now));
		}
	}

	/** This player's attuned totems that still stand, by name, and their Home Totem. */
	private static List<TravellersTotems.Destination> of(MinecraftServer server, ServerPlayer p) {
		TravellersTotems.Known known = p.getAttachedOrCreate(TravellersTotems.KNOWN);
		TotemData data = TotemData.get(server);
		List<TravellersTotems.Destination> out = new ArrayList<>();
		for (GlobalPos pos : known.learned()) {
			TotemData.Totem t = data.get(pos);
			if (t != null && !t.home()) {
				out.add(new TravellersTotems.Destination(pos, t.name(), false));
			}
		}
		known.home().ifPresent(pos -> {
			TotemData.Totem t = data.get(pos);
			if (t != null) {
				out.add(new TravellersTotems.Destination(pos, t.name(), true));
			}
		});
		return out;
	}

	private TotemMarks() {
	}
}
