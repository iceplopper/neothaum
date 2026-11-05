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
 * offers those and your Home Totem to travel to from wherever you stand. It keeps no memory of where you set out from.
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

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display,
		java.util.function.Consumer<Component> lines, TooltipFlag flag) {
		int n = stack.getOrDefault(TravellersTotems.COMPASS_TOTEMS, List.of()).size();
		lines.accept(Component.translatable("neothaum.compass.count", n, TravellersTotems.COMPASS_SLOTS).withStyle(ChatFormatting.DARK_PURPLE));
	}
}
