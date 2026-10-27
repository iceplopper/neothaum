package dev.neothaum.items.equipment;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * ItemBootsTraveller: walk up full blocks, run faster on the ground, steer better in the air, jump higher and fall more softly. The step
 * height and jump come from the boots' attribute modifiers (TVItems); the push is applied on the client, which moves the player.
 */
public class TravellerBootsItem extends Item implements dev.neothaum.api.RunicArmor {
	public TravellerBootsItem(Item.Properties props) {
		super(props);
	}

	/** onArmorTick (fallDistance -= 0.25 a tick). */
	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, EquipmentSlot slot) {
		if (slot == EquipmentSlot.FEET && owner.fallDistance > 0.0) {
			owner.fallDistance = Math.max(0.0, owner.fallDistance - 0.25);
		}
	}

	/** onArmorTick's movement, run each client tick for the local player. */
	public static void clientTick(Player player) {
		if (!(player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof TravellerBootsItem) || player.getAbilities().flying || player.zza <= 0.0F) {
			return;
		}
		if (player.onGround()) {
			float bonus = 0.055F;
			if (player.isInWater()) {
				bonus /= 4.0F;
			}
			player.moveRelative(bonus, new Vec3(0.0, 0.0, 1.0));
		} else {
			// jumpMovementFactor 0.05 instead of the usual 0.02
			player.moveRelative(0.03F, new Vec3(0.0, 0.0, 1.0));
		}
	}

	@Override
	public int getRunicCharge(ItemStack stack) {
		return 0;
	}
}
