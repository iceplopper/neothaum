package dev.neothaum.addon.trinkets;

import dev.neothaum.NeoThaum;
import dev.neothaum.baubles.BaubleItem;
import dev.neothaum.baubles.Baubles;
import dev.neothaum.runic.RunicShielding;
import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import eu.pb4.trinkets.api.callback.TrinketCallback;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Trinkets Updated support: NeoThaum's amulets, rings and belts go in Trinkets' necklace, ring and belt slots (see the data tags under
 * data/trinkets), where they work exactly as in NeoThaum's own bauble slots. Those four slots then retire: anything already in them moves
 * across when the player joins, and their inventory button is hidden.
 */
final class TrinketsIntegration {
	/** IBauble's hooks, run by Trinkets for a NeoThaum bauble in one of its slots. Worn effects are server-side, as in Baubles.tick. */
	private static final TrinketCallback CALLBACK = new TrinketCallback() {
		@Override
		public void tick(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
			if (!entity.level().isClientSide() && slot.canApplyEffects() && stack.getItem() instanceof BaubleItem b) {
				b.onWornTick(stack, entity);
			}
		}

		@Override
		public void onEquip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
			if (stack.getItem() instanceof BaubleItem b) {
				b.onEquipped(stack, entity);
			}
			RunicShielding.markDirty();
		}

		@Override
		public void onUnequip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
			if (stack.getItem() instanceof BaubleItem b) {
				b.onUnequipped(stack, entity);
			}
			RunicShielding.markDirty();
		}

		@Override
		public boolean canEquip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
			return !(stack.getItem() instanceof BaubleItem b) || b.canEquip(stack, entity);
		}

		@Override
		public boolean canUnequip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
			return !(stack.getItem() instanceof BaubleItem b) || b.canUnequip(stack, entity);
		}
	};

	static void init() {
		for (Item item : BuiltInRegistries.ITEM) {
			if (item instanceof BaubleItem) {
				TrinketCallback.setCallback(item, CALLBACK);
			}
		}
		Baubles.extraSlots = TrinketsIntegration::wornInTrinkets;
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> moveBaublesIntoTrinkets(handler.player));
		NeoThaum.LOGGER.info("Trinkets Updated found: NeoThaum's baubles use its slots");
	}

	/** Each NeoThaum bauble worn in a Trinkets slot that applies its effects (not a cosmetic slot). */
	private static List<Baubles.Worn> wornInTrinkets(Player player) {
		TrinketAttachment att = TrinketsApi.getAttachment(player);
		if (att == null) {
			return List.of();
		}
		List<Baubles.Worn> out = new ArrayList<>();
		att.forEach((slot, stack) -> {
			if (stack.getItem() instanceof BaubleItem && !slot.cosmetic() && slot.canApplyEffects()) {
				out.add(new Baubles.Worn(stack, slot::set));
			}
		});
		return out;
	}

	/** NeoThaum's own slots retire under Trinkets: each bauble left there goes into a free Trinkets slot that takes it, else the pack. */
	static void moveBaublesIntoTrinkets(ServerPlayer player) {
		Baubles own = Baubles.get(player);
		TrinketAttachment att = TrinketsApi.getAttachment(player);
		if (att == null || own.items().stream().allMatch(ItemStack::isEmpty)) {
			return;
		}
		for (int i = 0; i < Baubles.SIZE; i++) {
			ItemStack stack = own.get(i);
			if (stack.isEmpty()) {
				continue;
			}
			boolean[] placed = {false};
			att.forEachWhileTrue(slot -> {
				if (!slot.cosmetic() && slot.get().isEmpty() && fits(stack, slot) && slot.set(stack.copy())) {
					placed[0] = true;
					return false;
				}
				return true;
			});
			if (!placed[0] && !player.getInventory().add(stack.copy())) {
				player.drop(stack.copy(), false, net.minecraft.util.Prediction.SERVER_ONLY);
			}
			own = own.with(i, ItemStack.EMPTY);
		}
		Baubles.set(player, own);
		RunicShielding.markDirty();
	}

	/** The slot's own item tag (trinkets:<group>/<slot>), as Trinkets' default validator reads it; quickMoveCheck alone lets anything in. */
	private static boolean fits(ItemStack stack, TrinketSlotAccess slot) {
		// the reference names the slot as "<group>/<slot>", e.g. chest/necklace
		var tag = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
			net.minecraft.resources.Identifier.fromNamespaceAndPath("trinkets", slot.reference().slot()));
		return stack.is(tag) || stack.is(eu.pb4.trinkets.api.DefaultTrinketSlotTags.ALL);
	}

	private TrinketsIntegration() {
	}
}
