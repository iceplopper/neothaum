package dev.neothaum.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Port of thaumcraft.common.entities.EntitySpecialItem: an item drop that drifts gently upward instead of falling and is not destroyed by
 * explosions. Bosses drop their primordial pearl this way, the crucible spits out its products, and a wand turns a bookshelf into one.
 */
public class SpecialItemEntity extends ItemEntity {
	public SpecialItemEntity(EntityType<? extends SpecialItemEntity> type, Level level) {
		super(type, level);
	}

	/** The EntitySpecialItem(world, x, y, z, stack) constructor: a random spin and a small random push upward. */
	public static SpecialItemEntity create(Level level, double x, double y, double z, ItemStack stack) {
		SpecialItemEntity e = new SpecialItemEntity(TVEntities.SPECIAL_ITEM, level);
		e.setPos(x, y, z);
		e.setItem(stack);
		e.setYRot((float) (Math.random() * 360.0));
		e.setDeltaMovement(Math.random() * 0.2F - 0.1F, 0.2F, Math.random() * 0.2F - 0.1F);
		return e;
	}

	/** EntityUtils.entityDropSpecialItem: dropped from an entity at the given height, rising slowly with a short pickup delay. */
	public static SpecialItemEntity dropFrom(Entity entity, ItemStack stack, float dropHeight) {
		if (stack.isEmpty()) {
			return null;
		}
		SpecialItemEntity e = create(entity.level(), entity.getX(), entity.getY() + dropHeight, entity.getZ(), stack);
		e.setPickUpDelay(10);
		e.setDeltaMovement(0.0, 0.1F, 0.0);
		entity.level().addFreshEntity(e);
		return e;
	}

	@Override
	public void tick() {
		Vec3 m = this.getDeltaMovement();
		double y = m.y > 0.0 ? m.y * 0.9F : m.y;
		this.setDeltaMovement(m.x, y + 0.04F, m.z);
		super.tick();
	}

	@Override
	public boolean ignoreExplosion(Explosion explosion) {
		return true;
	}
}
