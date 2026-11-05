package dev.neothaum.addon.totems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * Travelling by totem. The list holds every totem the player knows that still stands, and their Home Totem; picking one starts a short
 * channel (a second at a totem, three with the compass) during which the player must stand still, then takes them there. It is free; the
 * compass then needs half a minute to settle.
 */
public final class TotemTravel {
	public static final int TOTEM_CHANNEL = 20;
	public static final int COMPASS_CHANNEL = 60;
	public static final int COMPASS_COOLDOWN = 600;

	private record Channel(GlobalPos target, Vec3 start, int ticks, int length, boolean compass) {
	}

	private static final Map<UUID, Channel> CHANNELS = new HashMap<>();

	static void init() {
		ServerTickEvents.END_SERVER_TICK.register(TotemTravel::tick);
	}

	/** What the player can travel to: the totems they know that still stand, then their home; a Home Totem gone is forgotten, with a word. */
	public static List<TravellersTotems.Destination> destinations(ServerPlayer sp, Optional<GlobalPos> from, List<GlobalPos> only) {
		TotemData data = TotemData.get(sp.level().getServer());
		var known = sp.getAttachedOrCreate(TravellersTotems.KNOWN);
		List<TravellersTotems.Destination> out = new ArrayList<>();
		var changed = known;
		for (GlobalPos pos : known.learned()) {
			TotemData.Totem t = data.get(pos);
			if (t == null) {
				changed = changed.forget(pos);
				continue;
			}
			if (from.filter(pos::equals).isEmpty() && (only == null || only.contains(pos))) {
				out.add(new TravellersTotems.Destination(pos, t.name(), false));
			}
		}
		if (known.home().isPresent()) {
			TotemData.Totem h = data.get(known.home().get());
			if (h == null) {
				changed = changed.withHome(Optional.empty());
				sp.sendSystemMessage(Component.translatable("neothaum.totem.home.gone").withStyle(ChatFormatting.DARK_PURPLE));
			} else if (from.filter(known.home().get()::equals).isEmpty()) {
				out.add(new TravellersTotems.Destination(h.pos(), h.name(), true));
			}
		}
		if (changed != known) {
			sp.setAttached(TravellersTotems.KNOWN, changed);
		}
		// nearest first, the home last
		Vec3 here = sp.position();
		out.sort((a, b) -> a.home() != b.home() ? Boolean.compare(a.home(), b.home())
			: Double.compare(distance(sp, a.pos(), here), distance(sp, b.pos(), here)));
		return out;
	}

	private static double distance(ServerPlayer sp, GlobalPos pos, Vec3 here) {
		return pos.dimension() == sp.level().dimension() ? Vec3.atCenterOf(pos.pos()).distanceToSqr(here) : Double.MAX_VALUE;
	}

	public static void openList(ServerPlayer sp, Optional<GlobalPos> from) {
		String name = from.map(f -> TotemData.get(sp.level().getServer()).get(f)).map(TotemData.Totem::name).orElse("");
		ServerPlayNetworking.send(sp, new TravellersTotems.OpenTravel(from, name, destinations(sp, from, null)));
	}

	/** The player picked a destination. From a totem: one they know, near them. With the compass: one it carries, or home. */
	public static void request(ServerPlayer sp, GlobalPos target, boolean compass) {
		if (!TravellersTotems.enabled() || CHANNELS.containsKey(sp.getUUID())) {
			return;
		}
		var known = sp.getAttachedOrCreate(TravellersTotems.KNOWN);
		boolean home = known.home().filter(target::equals).isPresent();
		if (compass) {
			var held = sp.getMainHandItem().is(TravellersTotems.compass) ? sp.getMainHandItem() : sp.getOffhandItem();
			if (!held.is(TravellersTotems.compass) || sp.getCooldowns().isOnCooldown(held)
				|| !home && !held.getOrDefault(TravellersTotems.COMPASS_TOTEMS, List.of()).contains(target)) {
				return;
			}
		} else {
			// standing by a Traveller's Totem they know
			boolean near = known.learned().stream().anyMatch(p -> p.dimension() == sp.level().dimension()
				&& p.pos().distToCenterSqr(sp.position()) < 36 && sp.level().getBlockState(p.pos()).is(TravellersTotems.TOTEM));
			if (!near || !home && !known.learned().contains(target)) {
				return;
			}
		}
		if (TotemData.get(sp.level().getServer()).get(target) == null) {
			return;
		}
		CHANNELS.put(sp.getUUID(), new Channel(target, sp.position(), 0, compass ? COMPASS_CHANNEL : TOTEM_CHANNEL, compass));
		sp.level().playSound(null, sp.blockPosition(), SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 0.25F, 1.6F);
	}

	private static void tick(MinecraftServer server) {
		if (CHANNELS.isEmpty()) {
			return;
		}
		for (var it = CHANNELS.entrySet().iterator(); it.hasNext(); ) {
			var e = it.next();
			ServerPlayer sp = server.getPlayerList().getPlayer(e.getKey());
			Channel c = e.getValue();
			if (sp == null || sp.isRemoved()) {
				it.remove();
				continue;
			}
			// moving breaks the stone's hold
			if (sp.position().distanceToSqr(c.start()) > 0.04) {
				sp.sendSystemMessage(Component.translatable("neothaum.totem.moved").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
				it.remove();
				continue;
			}
			ServerLevel here = (ServerLevel) sp.level();
			here.sendParticles(ParticleTypes.PORTAL, sp.getX(), sp.getY() + 1, sp.getZ(), 6, 0.4, 0.8, 0.4, 0.4);
			if (c.ticks() + 1 < c.length()) {
				e.setValue(new Channel(c.target(), c.start(), c.ticks() + 1, c.length(), c.compass()));
				continue;
			}
			it.remove();
			arrive(sp, c);
		}
	}

	private static void arrive(ServerPlayer sp, Channel c) {
		ServerLevel to = sp.level().getServer().getLevel(c.target().dimension());
		if (to == null || TotemData.get(sp.level().getServer()).get(c.target()) == null) {
			return;
		}
		BlockPos at = c.target().pos();
		// on top of the totem, which every totem leaves room for
		sp.teleportTo(to, at.getX() + 0.5, at.getY() + 1.0, at.getZ() + 0.5, java.util.Set.of(), sp.getYRot(), sp.getXRot(), false);
		to.playSound(null, at, SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS, 0.6F, 1.0F);
		to.sendParticles(ParticleTypes.REVERSE_PORTAL, at.getX() + 0.5, at.getY() + 1.5, at.getZ() + 0.5, 30, 0.4, 0.8, 0.4, 0.05);
		if (c.compass()) {
			var held = sp.getMainHandItem().is(TravellersTotems.compass) ? sp.getMainHandItem() : sp.getOffhandItem();
			if (held.is(TravellersTotems.compass)) {
				sp.getCooldowns().addCooldown(held, COMPASS_COOLDOWN);
			}
		}
	}

	/** For tests: whether the player is standing still for a journey. */
	public static boolean channelling(ServerPlayer sp) {
		return CHANNELS.containsKey(sp.getUUID());
	}

	private TotemTravel() {
	}
}
