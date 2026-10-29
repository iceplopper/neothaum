package dev.neothaum.baubles;

import com.mojang.serialization.Codec;
import dev.neothaum.registry.TVAttachments;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** The four Baubles slots, stored on the player as a persistent, synced attachment (replaces the Baubles mod). */
public record Baubles(List<ItemStack> items) {
	public static final int SIZE = 4;
	public static final Codec<Baubles> CODEC = ItemStack.OPTIONAL_CODEC.listOf().xmap(Baubles::new, Baubles::items);
	public static final StreamCodec<RegistryFriendlyByteBuf, Baubles> STREAM_CODEC = ItemStack.OPTIONAL_STREAM_CODEC
		.apply(ByteBufCodecs.list()).map(Baubles::new, Baubles::items);

	public Baubles {
		List<ItemStack> l = new ArrayList<>(items);
		while (l.size() < SIZE) {
			l.add(ItemStack.EMPTY);
		}
		items = l;
	}

	/** One worn bauble, and how to put a changed stack back in its slot. */
	public record Worn(ItemStack stack, Consumer<ItemStack> set) {
	}

	/** Bauble slots another mod adds (the Trinkets addon): each NeoThaum bauble worn there. Empty unless an addon fills it in. */
	public static Function<Player, List<Worn>> extraSlots = p -> List.of();

	/** Every bauble slot the player has: NeoThaum's own four first, then any an addon adds. */
	public static List<Worn> wornSlots(Player player) {
		List<Worn> out = new ArrayList<>();
		Baubles b = get(player);
		for (int i = 0; i < SIZE; i++) {
			int slot = i;
			out.add(new Worn(b.get(i), s -> set(player, get(player).with(slot, s))));
		}
		out.addAll(extraSlots.apply(player));
		return out;
	}

	/** What the player is wearing in every bauble slot (some stacks may be empty). */
	public static List<ItemStack> worn(Player player) {
		return wornSlots(player).stream().map(Worn::stack).toList();
	}

	public static Baubles empty() {
		return new Baubles(List.of());
	}

	public static Baubles get(Player player) {
		Baubles b = player.getAttached(TVAttachments.BAUBLES);
		return b != null ? b : empty();
	}

	public static void set(Player player, Baubles b) {
		player.setAttached(TVAttachments.BAUBLES, b);
	}

	public ItemStack get(int slot) {
		return this.items.get(slot);
	}

	public Baubles with(int slot, ItemStack stack) {
		List<ItemStack> l = new ArrayList<>(this.items);
		l.set(slot, stack);
		return new Baubles(l);
	}

	/** Per player, what was in each slot last tick: Baubles called onEquipped and onUnequipped as the slots changed. */
	private static final java.util.Map<java.util.UUID, List<ItemStack>> LAST = new java.util.HashMap<>();

	/** The Baubles mod ticked every worn bauble each tick (IBauble.onWornTick). */
	public static void initTicking() {
		net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (var player : server.getPlayerList().getPlayers()) {
				tick(player);
			}
		});
		net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((h, server) -> LAST.remove(h.player.getUUID()));
	}

	private static void tick(Player player) {
		Baubles b = get(player);
		List<ItemStack> last = LAST.computeIfAbsent(player.getUUID(), u -> new ArrayList<>(List.of(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
			ItemStack.EMPTY)));
		boolean changed = false;
		for (int i = 0; i < SIZE; i++) {
			ItemStack now = b.get(i);
			ItemStack before = last.get(i);
			if (!ItemStack.isSameItem(now, before)) {
				if (before.getItem() instanceof BaubleItem bi) {
					bi.onUnequipped(before, player);
				}
				if (now.getItem() instanceof BaubleItem bi) {
					bi.onEquipped(now, player);
				}
			}
			if (now.getItem() instanceof BaubleItem bi) {
				ItemStack copy = now.copy();
				bi.onWornTick(now, player);
				changed |= !ItemStack.matches(copy, now);
			}
			last.set(i, now.copy());
		}
		if (changed) {
			// worn stacks changed in place (a vis amulet filling a wand): push the new state to the client
			set(player, new Baubles(b.items()));
		}
	}
}
