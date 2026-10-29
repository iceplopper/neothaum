package dev.neothaum.items;

import dev.neothaum.warp.Warp;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** ItemZombieBrain: wolf food that may bring on Hunger (see its food component), and eating it gives warp, sticky one time in ten. */
public class ZombieBrainItem extends Item {
	public ZombieBrainItem(Properties props) {
		super(props);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		if (!level.isClientSide() && entity instanceof ServerPlayer player) {
			if (level.getRandom().nextFloat() < 0.1F) {
				Warp.addStickyWarpToPlayer(player, 1);
			} else {
				Warp.addWarpToPlayer(player, 1 + level.getRandom().nextInt(3), true);
			}
		}
		return super.finishUsingItem(stack, level, entity);
	}
}
