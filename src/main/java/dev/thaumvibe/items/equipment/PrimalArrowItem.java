package dev.thaumvibe.items.equipment;

import dev.thaumvibe.entity.projectile.PrimalArrowEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Port of ItemPrimalArrow:
 * Arrows infused with elemental shards that impart primal effects upon impact.
 */
public class PrimalArrowItem extends ArrowItem {
	private final int primalType; // 0=Air, 1=Fire, 2=Water, 3=Earth, 4=Order, 5=Entropy

	public PrimalArrowItem(int primalType, Properties props) {
		super(props);
		this.primalType = primalType;
	}

	public int getPrimalType() {
		return this.primalType;
	}

	@Override
	public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter, @Nullable ItemStack weapon) {
		return new PrimalArrowEntity(level, shooter, ammo.copyWithCount(1), weapon, this.primalType);
	}

	@Override
	public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction dir) {
		return new PrimalArrowEntity(level, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1), null, this.primalType);
	}
}
