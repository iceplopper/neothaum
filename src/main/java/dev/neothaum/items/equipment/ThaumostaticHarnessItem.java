package dev.neothaum.items.equipment;

import dev.neothaum.api.RunicArmor;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.baubles.Baubles;
import dev.neothaum.items.VisDiscountGear;
import dev.neothaum.items.baubles.HoverGirdleItem;
import dev.neothaum.menu.HoverHarnessMenu;
import dev.neothaum.registry.TVComponents;
import dev.neothaum.registry.TVSounds;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/**
 * ItemHoverHarness: Thaumostatic Harness chestplate allowing creative-style flight.
 * Consumes Potentia essentia from an inserted warded jar.
 * Hovering drags on horizontal movement; the Haste enchantment and the Thaumostatic Girdle lessen the drag.
 */
public class ThaumostaticHarnessItem extends Item implements VisDiscountGear, RunicArmor {
	private static long lastJacobsSound = 0;

	public ThaumostaticHarnessItem(Properties props) {
		super(props);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!level.isClientSide()) {
			ItemStack held = player.getItemInHand(hand);
			int slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().getSelectedSlot() : -1;
			player.openMenu(new SimpleMenuProvider(
				(id, inv, p) -> new HoverHarnessMenu(id, inv, slot),
				held.getHoverName()
			));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, EquipmentSlot slot) {
		if (slot != EquipmentSlot.CHEST || !(owner instanceof ServerPlayer player)) {
			return;
		}

		if (player.isCreative() || player.isSpectator()) {
			return;
		}

		ItemStack jar = stack.get(TVComponents.HARNESS_JAR);
		int fuel = 0;
		if (jar != null && !jar.isEmpty()) {
			AspectList al = jar.get(TVComponents.ASPECTS);
			if (al != null) {
				fuel = al.getAmount(Aspect.ENERGY);
			}
		}

		boolean active = Boolean.TRUE.equals(stack.get(TVComponents.HARNESS_ACTIVE));
		if (active && fuel > 0) {
			if (!player.getAbilities().mayfly) {
				player.getAbilities().mayfly = true;
				player.onUpdateAbilities();
			}

			if (player.getAbilities().flying) {
				player.fallDistance = 0.0F;

				boolean hasGirdle = Baubles.get(player).get(3).getItem() instanceof HoverGirdleItem;
				float mod = hasGirdle ? 0.8F : 1.0F;
				int limit = (int) (360.0F * mod);

				int charge = stack.getOrDefault(TVComponents.HARNESS_CHARGE, 0);
				charge++;

				if (charge < limit) {
					stack.set(TVComponents.HARNESS_CHARGE, charge);
				} else {
					stack.set(TVComponents.HARNESS_CHARGE, 0);
					ItemStack jarCopy = jar.copy();
					AspectList al = jarCopy.get(TVComponents.ASPECTS);
					if (al != null) {
						AspectList copy = al.copy();
						copy.remove(Aspect.ENERGY, 1);
						fuel = copy.getAmount(Aspect.ENERGY);
						if (copy.size() > 0) {
							jarCopy.set(TVComponents.ASPECTS, copy);
						} else {
							jarCopy.remove(TVComponents.ASPECTS);
						}
					}
					stack.set(TVComponents.HARNESS_JAR, jarCopy);

					if (fuel <= 0) {
						stack.set(TVComponents.HARNESS_ACTIVE, false);
						player.getAbilities().flying = false;
						player.getAbilities().mayfly = false;
						player.onUpdateAbilities();
						level.playSound(null, player.getX(), player.getY(), player.getZ(), TVSounds.HHOFF, SoundSource.PLAYERS, 0.33F, 1.0F);
					}
				}
			}
		} else {
			if (player.getAbilities().mayfly) {
				player.getAbilities().flying = false;
				player.getAbilities().mayfly = false;
				player.onUpdateAbilities();
			}
			if (player.fallDistance > 0.0F) {
				player.fallDistance *= 0.75F;
			}
		}
	}

	public boolean toggleHover(ServerPlayer player, ItemStack harness) {
		ItemStack jar = harness.get(TVComponents.HARNESS_JAR);
		int fuel = 0;
		if (jar != null && !jar.isEmpty()) {
			AspectList al = jar.get(TVComponents.ASPECTS);
			if (al != null) {
				fuel = al.getAmount(Aspect.ENERGY);
			}
		}

		boolean active = Boolean.TRUE.equals(harness.get(TVComponents.HARNESS_ACTIVE));
		if (!active && fuel <= 0) {
			player.level().playSound(null, player.getX(), player.getY(), player.getZ(), TVSounds.HHOFF, SoundSource.PLAYERS, 0.33F, 1.0F);
			return false;
		}

		boolean newActive = !active;
		harness.set(TVComponents.HARNESS_ACTIVE, newActive);
		if (newActive) {
			player.getAbilities().mayfly = true;
			player.getAbilities().flying = true;
			player.onUpdateAbilities();
			player.level().playSound(null, player.getX(), player.getY(), player.getZ(), TVSounds.HHON, SoundSource.PLAYERS, 0.33F, 1.0F);
		} else {
			player.getAbilities().flying = false;
			player.getAbilities().mayfly = player.isCreative() || player.isSpectator();
			player.onUpdateAbilities();
			player.level().playSound(null, player.getX(), player.getY(), player.getZ(), TVSounds.HHOFF, SoundSource.PLAYERS, 0.33F, 1.0F);
		}
		return newActive;
	}

	/** Hover.handleHoverArmor's client half: the jacobs-ladder hum, and horizontal motion damped by 0.7 + 0.075 per Haste (+0.21 with the girdle). */
	public static void clientTick(Player player) {
		ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
		if (!(chest.getItem() instanceof ThaumostaticHarnessItem) || !Boolean.TRUE.equals(chest.get(TVComponents.HARNESS_ACTIVE))
			|| !player.getAbilities().flying) {
			return;
		}
		long now = System.currentTimeMillis();
		if (now - lastJacobsSound >= 1200) {
			lastJacobsSound = now;
			player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), TVSounds.JACOBS, SoundSource.PLAYERS, 0.05F,
				1.0F + player.level().getRandom().nextFloat() * 0.05F, false);
		}
		int haste = TCEnchantments.level(player, chest, TCEnchantments.HASTE);
		float mod = 0.7F + 0.075F * haste;
		if (Baubles.get(player).get(3).getItem() instanceof HoverGirdleItem) {
			mod += 0.21F;
		}
		mod = Math.min(1.0F, mod);
		var v = player.getDeltaMovement();
		player.setDeltaMovement(v.x * mod, v.y, v.z * mod);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		ItemStack jar = stack.get(TVComponents.HARNESS_JAR);
		if (jar != null && !jar.isEmpty()) {
			AspectList aspects = jar.get(TVComponents.ASPECTS);
			if (aspects != null && aspects.size() > 0) {
				for (Aspect tag : aspects.getAspectsSorted()) {
					builder.accept(Component.literal("§6" + tag.getName() + " x " + aspects.getAmount(tag)));
				}
			}
		}

		builder.accept(Component.translatable("tc.visdiscount").append(": " + this.getVisDiscount(stack, null, null) + "%").withStyle(ChatFormatting.DARK_PURPLE));
		builder.accept(Component.translatable("tc.visdiscount").append(" (Aer): " + this.getVisDiscount(stack, null, Aspect.AIR) + "%").withStyle(ChatFormatting.DARK_PURPLE));
	}

	@Override
	public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) {
		return aspect == Aspect.AIR ? 5 : 2;
	}

	@Override
	public int getRunicCharge(ItemStack stack) {
		return 0;
	}
}
