package dev.neothaum.addon.totems;

import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * The Wayfarer's Compass: it carries the bearings of up to five Traveller's Totems (sneak and use it on one you know) and, used in the air,
 * offers those and your Home Totem to travel to from wherever you stand. It keeps no memory of where you set out from. Its needle points to
 * the nearest Traveller's Totem you have not yet attuned to; knowing of none, to the nearest village (each has a totem) that isn't beside
 * one you know; with nothing to find, it spins.
 */
public class WayfarersCompassItem extends Item {
	public WayfarersCompassItem(Item.Properties props) {
		super(props);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!TravellersTotems.enabled()) {
			return InteractionResult.PASS;
		}
		ItemStack stack = player.getItemInHand(hand);
		if (player instanceof ServerPlayer sp) {
			if (sp.getCooldowns().isOnCooldown(stack)) {
				sp.sendSystemMessage(Component.translatable("neothaum.compass.settling").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
				return InteractionResult.FAIL;
			}
			List<net.minecraft.core.GlobalPos> saved = stack.getOrDefault(TravellersTotems.COMPASS_TOTEMS, List.of());
			ServerPlayNetworking.send(sp, new TravellersTotems.OpenTravel(Optional.empty(), "", TotemTravel.destinations(sp, Optional.empty(), saved)));
		}
		return InteractionResult.SUCCESS;
	}

	/** How often (ticks) the needle is set again, and how often the village search is repeated. */
	private static final int AIM_EVERY = 40;
	private static final int VILLAGE_EVERY = 600;
	private static final java.util.Map<java.util.UUID, net.minecraft.core.BlockPos> VILLAGES = new java.util.HashMap<>();
	private static final java.util.Map<java.util.UUID, Long> VILLAGE_TIMES = new java.util.HashMap<>();

	@Override
	public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.Entity owner,
		net.minecraft.world.entity.EquipmentSlot slot) {
		if (!(owner instanceof ServerPlayer sp) || level.getGameTime() % AIM_EVERY != 0 || !TravellersTotems.enabled()) {
			return;
		}
		var target = aim(sp, level);
		var tracker = target == null ? null : new net.minecraft.world.item.component.LodestoneTracker(
			Optional.of(net.minecraft.core.GlobalPos.of(level.dimension(), target)), false);
		if (!java.util.Objects.equals(tracker, stack.get(net.minecraft.core.component.DataComponents.LODESTONE_TRACKER))) {
			if (tracker == null) {
				stack.remove(net.minecraft.core.component.DataComponents.LODESTONE_TRACKER);
			} else {
				stack.set(net.minecraft.core.component.DataComponents.LODESTONE_TRACKER, tracker);
			}
		}
	}

	/** Where the needle points for this player: the nearest totem they haven't attuned, else the nearest village not beside one they know. */
	public static net.minecraft.core.BlockPos aim(ServerPlayer sp, net.minecraft.server.level.ServerLevel level) {
		var known = sp.getAttachedOrCreate(TravellersTotems.KNOWN);
		net.minecraft.core.BlockPos best = null;
		double bestD = Double.MAX_VALUE;
		for (TotemData.Totem t : TotemData.get(level.getServer()).all()) {
			if (t.home() || t.pos().dimension() != level.dimension() || known.learned().contains(t.pos())) {
				continue;
			}
			double d = t.pos().pos().distToCenterSqr(sp.position());
			if (d < bestD) {
				bestD = d;
				best = t.pos().pos();
			}
		}
		if (best != null) {
			return best;
		}
		// none known: the nearest village, searched now and then (a village's totem may not have been found yet)
		long now = level.getGameTime();
		var id = sp.getUUID();
		Long last = VILLAGE_TIMES.get(id);
		if (last == null || now - last >= VILLAGE_EVERY) {
			VILLAGE_TIMES.put(id, now);
			var found = level.dimension() == net.minecraft.world.level.Level.OVERWORLD
				? level.findNearestMapStructure(net.minecraft.tags.StructureTags.VILLAGE, sp.blockPosition(), 64, false) : null;
			var village = found != null && known.learned().stream().anyMatch(p -> p.dimension() == level.dimension() && p.pos().closerThan(found, 96))
				? null : found;
			if (village == null) {
				VILLAGES.remove(id);
			} else {
				VILLAGES.put(id, village);
			}
		}
		return VILLAGES.get(id);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display,
		java.util.function.Consumer<Component> lines, TooltipFlag flag) {
		int n = stack.getOrDefault(TravellersTotems.COMPASS_TOTEMS, List.of()).size();
		lines.accept(Component.translatable("neothaum.compass.count", n, TravellersTotems.COMPASS_SLOTS).withStyle(ChatFormatting.DARK_PURPLE));
	}
}
