package dev.neothaum.tiles.eldritch;

import dev.neothaum.entity.monster.CultistEntity;
import dev.neothaum.entity.monster.EldritchGuardianEntity;
import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.tiles.TVBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * 1:1 Port of TileEldritchObelisk.
 * Monolith pillar buffing Eldritch creatures with Strength and Resistance.
 */
public class EldritchObeliskBlockEntity extends TVBlockEntity {
	private int counter = 0;

	public EldritchObeliskBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.ELDRITCH_OBELISK, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, EldritchObeliskBlockEntity obelisk) {
		obelisk.counter++;
		if (level.isClientSide()) {
			if (obelisk.counter % 20 == 0) {
				double x = pos.getX() + 0.5;
				double y = pos.getY() + 1.0 + level.getRandom().nextFloat() * 3.0F;
				double z = pos.getZ() + 0.5;
				level.addParticle(ParticleTypes.PORTAL, x, y, z, (level.getRandom().nextDouble() - 0.5) * 0.2, (level.getRandom().nextDouble() - 0.5) * 0.2, (level.getRandom().nextDouble() - 0.5) * 0.2);
			}
			return;
		}

		if (obelisk.counter % 20 == 0) {
			AABB box = new AABB(pos).inflate(6.0, 6.0, 6.0);
			List<LivingEntity> living = level.getEntitiesOfClass(LivingEntity.class, box,
				e -> e instanceof EldritchGuardianEntity || e instanceof CultistEntity);
			for (LivingEntity entity : living) {
				if (!entity.hasEffect(MobEffects.STRENGTH)) {
					entity.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 60, 0, true, true));
					entity.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 60, 0, true, true));
				}
			}
		}
	}
}
