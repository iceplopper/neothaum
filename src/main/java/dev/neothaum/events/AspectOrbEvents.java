package dev.neothaum.events;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.aspects.ObjectAspects;
import dev.neothaum.blocks.LiquidDeathBlock;
import dev.neothaum.config.NeoThaumConfig;
import dev.neothaum.entity.AspectOrbEntity;
import dev.neothaum.items.CrystalEssenceItem;
import dev.neothaum.research.ResearchManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

/**
 * EventHandlerEntity's death drops. Something killed by liquid death's "dissolve" damage drops, for each of its aspects, one time in two,
 * crystallized essence of that aspect (half of 1 to the amount). Anything that dies having lately been hit by a player looses aspect orbs:
 * for each primal its aspects reduce to, one time in two, an orb of 1 to that amount.
 */
public final class AspectOrbEvents {
	public static void init() {
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity.level().isClientSide()) {
				return;
			}
			var rand = entity.getRandom();
			AspectList aspects = ObjectAspects.getEntityAspects(entity);
			if (aspects == null || aspects.size() == 0) {
				return;
			}
			if (source.is(LiquidDeathBlock.DISSOLVE)) {
				for (Aspect aspect : aspects.getAspects()) {
					if (!rand.nextBoolean()) {
						int size = Math.max(1, (1 + rand.nextInt(aspects.getAmount(aspect))) / 2);
						ItemStack crystal = CrystalEssenceItem.of(aspect);
						crystal.setCount(size);
						entity.level().addFreshEntity(new ItemEntity(entity.level(), entity.getX(), entity.getEyeY(), entity.getZ(), crystal));
					}
				}
			}
			if (entity.getLastHurtByPlayer() != null) {
				float chance = NeoThaumConfig.ASPECT_ORB_CHANCE * NeoThaumConfig.current.aspectOrbMultiplier;
				AspectList primals = ResearchManager.reduceToPrimals(aspects);
				for (Aspect aspect : primals.getAspects()) {
					if (rand.nextFloat() < chance) {
						entity.level().addFreshEntity(new AspectOrbEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), aspect,
							1 + rand.nextInt(primals.getAmount(aspect))));
					}
				}
			}
		});
	}

	private AspectOrbEvents() {
	}
}
