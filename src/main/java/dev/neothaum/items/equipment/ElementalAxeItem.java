package dev.neothaum.items.equipment;

import dev.neothaum.fx.FX;
import java.util.List;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;

/**
 * ItemElementalAxe, the Axe of the Stream: held up, it draws loose items within ten blocks to you in a stream of bubbles; chopping a log fells
 * the tree from its furthest log inwards (see {@link ToolEvents}).
 */
public class ElementalAxeItem extends Item {
	public ElementalAxeItem(Properties props) {
		super(props);
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.BOW;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 72000;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		player.startUsingItem(hand);
		return InteractionResult.CONSUME;
	}

	@Override
	public void onUseTick(Level level, LivingEntity player, ItemStack stack, int remaining) {
		List<ItemEntity> stuff = level.getEntitiesOfClass(ItemEntity.class, new AABB(player.blockPosition()).inflate(10.0),
			e -> e.isAlive() && e.distanceTo(player) <= 10.0);
		var rand = level.getRandom();
		for (ItemEntity e : stuff) {
			double dx = e.getX() - player.getX();
			double dy = e.getY() - player.getY() + player.getBbHeight() / 2.0F;
			double dz = e.getZ() - player.getZ();
			double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
			var m = e.getDeltaMovement();
			e.setDeltaMovement(Mth.clamp(m.x - dx / d * 0.3, -0.35, 0.35), Mth.clamp(m.y - dy / d * 0.3, -0.35, 0.35),
				Mth.clamp(m.z - dz / d * 0.3, -0.35, 0.35));
			FX.crucibleBubble(level, e.getX() + (rand.nextFloat() - rand.nextFloat()) * 0.125F, e.getY() + (rand.nextFloat() - rand.nextFloat()) * 0.125F,
				e.getZ() + (rand.nextFloat() - rand.nextFloat()) * 0.125F, 0.33F, 0.33F, 1.0F);
		}
	}
}
