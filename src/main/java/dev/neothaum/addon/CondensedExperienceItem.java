package dev.neothaum.addon;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Condensed Experience: eaten (it can always be eaten, and fills no hunger), it gives back a jar's worth: ten levels from zero, 160 points. */
public class CondensedExperienceItem extends Item {
	public CondensedExperienceItem(Item.Properties props) {
		super(props);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		if (entity instanceof ServerPlayer player) {
			player.giveExperiencePoints(ExperienceSiphon.JAR);
		}
		return super.finishUsingItem(stack, level, entity);
	}
}
