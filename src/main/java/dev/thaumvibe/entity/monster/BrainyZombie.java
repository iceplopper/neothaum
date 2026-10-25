package dev.thaumvibe.entity.monster;

import dev.thaumvibe.entity.TVEntities;
import dev.thaumvibe.registry.TVItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/** EntityBrainyZombie: an angry zombie with a little more armour, which drops a brain more often than not. */
public class BrainyZombie extends Zombie {
	public BrainyZombie(EntityType<? extends BrainyZombie> type, Level level) {
		super(type, level);
	}

	@Override
	public int getArmorValue() {
		return Math.min(20, super.getArmorValue() + 3);
	}

	@Override
	protected boolean convertsInWater() {
		return false;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		int looting = TVEntities.looting(level, source);
		for (int a = 0; a < 3; a++) {
			if (this.random.nextBoolean()) {
				this.spawnAtLocation(level, Items.ROTTEN_FLESH);
			}
		}
		if (this.random.nextInt(10) - looting <= 4) {
			this.spawnAtLocation(level, new ItemStack(TVItems.ZOMBIE_BRAIN), 1.5F);
		}
	}
}
