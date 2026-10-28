package dev.neothaum.tiles.eldritch;

import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.tiles.TVBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
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
	public EldritchObeliskBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.ELDRITCH_OBELISK, pos, state);
	}

	/** TileEldritchObelisk.updateEntity: every eldritch mob within six blocks is given strength and regeneration, and wisps drift to it. */
	public static void tick(Level level, BlockPos pos, BlockState state, EldritchObeliskBlockEntity obelisk) {
		AABB box = new AABB(pos).inflate(6.0);
		List<LivingEntity> living = level.getEntitiesOfClass(LivingEntity.class, box,
			e -> e instanceof dev.neothaum.api.EldritchMob && e.distanceToSqr(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5) <= 36.0);
		if (level.isClientSide()) {
			for (LivingEntity e : living) {
				dev.neothaum.fx.FX.wispTo(level, pos.getX() + 0.5, pos.getY() + 1 + level.getRandom().nextFloat() * 3.0F, pos.getZ() + 0.5, e.getX(),
					e.getY() + e.getBbHeight() / 2.0, e.getZ(), 0.4F, 5, true, 1.0F);
			}
			return;
		}
		// TC4 never advanced its counter, so this ran every tick
		for (LivingEntity e : living) {
			if (!e.hasEffect(MobEffects.REGENERATION)) {
				e.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 0, true, true));
				e.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, true));
			}
		}
	}
}
