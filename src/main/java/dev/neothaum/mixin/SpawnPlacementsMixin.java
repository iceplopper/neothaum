package dev.neothaum.mixin;

import dev.neothaum.config.NeoThaumConfig;
import dev.neothaum.entity.TVEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * TC4's Monster_Spawning switches took its mobs out of the biomes' spawn lists, so they stopped spawning naturally but spawners and eggs still
 * worked. Biome spawn lists are data now, so instead a switched-off mob fails its spawn rules for natural spawns.
 */
@Mixin(SpawnPlacements.class)
public abstract class SpawnPlacementsMixin {
	@Inject(method = "checkSpawnRules", at = @At("HEAD"), cancellable = true)
	private static <T extends Entity> void neothaum$configSpawns(EntityType<T> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
		RandomSource random, CallbackInfoReturnable<Boolean> cir) {
		if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION) {
			return;
		}
		var cfg = NeoThaumConfig.current;
		boolean off = type == TVEntities.BRAINY_ZOMBIE || type == TVEntities.GIANT_BRAINY_ZOMBIE ? !cfg.spawnAngryZombies
			: type == TVEntities.FIREBAT ? !cfg.spawnFireBats
			: type == TVEntities.WISP ? !cfg.spawnWisps
			: type == TVEntities.TAINTACLE ? !cfg.spawnTaintacles
			: type == TVEntities.PECH ? !cfg.spawnPechs
			: type == TVEntities.ELDRITCH_GUARDIAN && !cfg.spawnElderCreatures;
		if (off) {
			cir.setReturnValue(false);
		}
	}
}
