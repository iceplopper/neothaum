package dev.neothaum.devtools;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.items.WandItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Dev tool: fills every wand in the inventory with as much of each primal vis as it can hold. */
public class WandChargerItem extends Item {
	public WandChargerItem(Item.Properties props) {
		super(props);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!player.isCreative()) {
			return InteractionResult.FAIL;
		}
		if (!level.isClientSide()) {
			int wands = 0;
			for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
				ItemStack s = player.getInventory().getItem(i);
				if (s.getItem() instanceof WandItem) {
					for (Aspect a : Aspect.getPrimalAspects()) {
						WandItem.storeVis(s, a, WandItem.getMaxVis(s));
					}
					wands++;
				}
			}
			player.sendSystemMessage(Component.literal("[Dev] Filled " + wands + " wand(s)").withStyle(ChatFormatting.LIGHT_PURPLE));
		}
		return InteractionResult.SUCCESS;
	}
}
