package dev.thaumvibe.wands;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.wands.WandRod;
import dev.thaumvibe.items.WandItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Port of WandRodPrimalOnUpdate: slowly recharges a rod's primal up to 10% capacity. */
public class PrimalRodUpdate implements WandRod.OnUpdate {
	private final Aspect aspect;

	public PrimalRodUpdate(Aspect aspect) {
		this.aspect = aspect;
	}

	public PrimalRodUpdate() {
		this(null);
	}

	@Override
	public void onUpdate(ItemStack itemstack, Player player) {
		if (this.aspect != null) {
			if (player.tickCount % 200 == 0 && WandItem.getVis(itemstack, this.aspect) < WandItem.getMaxVis(itemstack) / 10) {
				WandItem.addVis(itemstack, this.aspect, 1, true);
			}
		} else if (player.tickCount % 50 == 0) {
			List<Aspect> q = new ArrayList<>();
			for (Aspect as : Aspect.getPrimalAspects()) {
				if (WandItem.getVis(itemstack, as) < WandItem.getMaxVis(itemstack) / 10) {
					q.add(as);
				}
			}
			if (!q.isEmpty()) {
				WandItem.addVis(itemstack, q.get(player.getRandom().nextInt(q.size())), 1, true);
			}
		}
	}
}
