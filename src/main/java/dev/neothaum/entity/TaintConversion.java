package dev.neothaum.entity;

import dev.neothaum.api.TaintedMob;
import dev.neothaum.entity.monster.ThaumicSlime;
import dev.neothaum.registry.TVMobEffects;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;

/**
 * EventHandlerEntity.livingTick (LivingDeathEvent): whatever dies with flux taint in it rises again as taint. Creepers, sheep, cows, pigs,
 * chickens and villagers come back as their tainted kin; anything else curdles into a thaumic slime sized to how big it was.
 */
public final class TaintConversion {
	public static void init() {
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
			if (!(entity.level() instanceof ServerLevel level) || entity instanceof TaintedMob || entity instanceof Player
				|| !entity.hasEffect(TVMobEffects.FLUX_TAINT)) {
				return true;
			}
			EntityType<? extends Mob> into = entity instanceof Creeper ? TVEntities.TAINTED_CREEPER
				: entity instanceof Sheep ? TVEntities.TAINTED_SHEEP
				: entity instanceof Cow ? TVEntities.TAINTED_COW
				: entity instanceof Pig ? TVEntities.TAINTED_PIG
				: entity instanceof Chicken ? TVEntities.TAINTED_CHICKEN
				: entity instanceof Villager ? TVEntities.TAINTED_VILLAGER
				: TVEntities.THAUMIC_SLIME;
			Mob mob = into.create(level, EntitySpawnReason.CONVERSION);
			if (mob == null) {
				return true;
			}
			if (mob instanceof ThaumicSlime slime) {
				slime.setSlimeSize((int) (1.0F + Math.min(entity.getMaxHealth() / 10.0F, 6.0F)));
			}
			mob.snapTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), 0.0F);
			level.addFreshEntity(mob);
			entity.discard();
			return false;
		});
	}

	private TaintConversion() {
	}
}
