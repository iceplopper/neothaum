package dev.thaumvibe.items.equipment;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** ItemBowBone: draws fully in half the time, lets fly by itself after 18 ticks, and hits a little harder and faster than a bow. */
public class BoneBowItem extends BowItem {
	public BoneBowItem(Properties props) {
		super(props);
	}

	/** onUsingTick: released automatically once drawn for 18 ticks. */
	@Override
	public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
		if (this.getUseDuration(stack, entity) - remaining > 18) {
			entity.releaseUsingItem();
		}
	}

	/** onPlayerStoppedUsing: power builds over 10 ticks, not 20, and the arrow leaves at 2.5 rather than 2 (x1.5 in today's units). */
	@Override
	public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
		if (!(entity instanceof Player player)) {
			return false;
		}
		ItemStack projectile = player.getProjectile(stack);
		if (projectile.isEmpty()) {
			return false;
		}
		float f = (this.getUseDuration(stack, entity) - remaining) / 10.0F;
		f = (f * f + f * 2.0F) / 3.0F;
		if (f < 0.1) {
			return false;
		}
		f = Math.min(f, 1.0F);
		List<ItemStack> fired = draw(stack, projectile, player);
		if (level instanceof ServerLevel sl && !fired.isEmpty()) {
			this.shoot(sl, player, player.getUsedItemHand(), stack, fired, f * 3.75F, 1.0F, f == 1.0F, null);
		}
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F,
			1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + f * 0.5F);
		return true;
	}

	/** setDamage(getDamage() + 0.5). */
	@Override
	protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float power, float uncertainty, float angle,
		@Nullable LivingEntity target) {
		if (projectile instanceof AbstractArrow arrow) {
			arrow.setBaseDamage(arrow.getBaseDamage() + 0.5);
		}
		super.shootProjectile(shooter, projectile, index, power, uncertainty, angle, target);
	}
}
