package dev.thaumvibe.events;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.entity.AspectOrbEntity;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

/**
 * AspectOrbEvents: listens for mob deaths and spawns aspect orbs.
 */
public final class AspectOrbEvents {
	public static void init() {
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity.level().isClientSide()) return;
			if (source != null && source.getEntity() instanceof Player) {
				float chance = dev.thaumvibe.config.ThaumvibeConfig.current.aspectOrbDropChance;
				if (chance > 0 && entity.level().getRandom().nextFloat() < chance) {
					Aspect[] primals = Aspect.getPrimalAspects().toArray(new Aspect[0]);
					Aspect a = primals[entity.level().getRandom().nextInt(primals.length)];
					int value = entity instanceof Monster ? (entity.level().getRandom().nextInt(2) + 1) : 1;
					AspectOrbEntity orb = new AspectOrbEntity(entity.level(), entity.getX(), entity.getY() + 0.5, entity.getZ(), a, value);
					entity.level().addFreshEntity(orb);
				}
			}
		});
	}

	private AspectOrbEvents() {
	}
}
