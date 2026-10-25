package dev.thaumvibe.registry;

import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.api.TaintedMob;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** Thaumcraft's custom potions (Config.potion*ID), as mob effects with TC4's colours, icons and per-tick behaviour. */
public final class TVMobEffects {
	public static final ResourceKey<DamageType> TAINT_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, Thaumvibe.id("taint"));

	/** PotionFluxTaint: wears down everything but tainted creatures (which it heals) and the undead. */
	public static final Holder<MobEffect> FLUX_TAINT = register("flux_taint", new Effect(MobEffectCategory.HARMFUL, 6697847) {
		@Override
		public boolean applyEffectTick(ServerLevel level, LivingEntity target, int amp) {
			if (target instanceof TaintedMob) {
				target.heal(1.0F);
			} else if (!target.isInvertedHealAndHarm() && (!(target instanceof Player) || target.getMaxHealth() > 1.0F)) {
				target.hurtServer(level, taint(level), 1.0F);
			}
			return true;
		}

		@Override
		public boolean shouldApplyEffectTickThisTick(int duration, int amp) {
			int k = 40 >> amp;
			return k <= 0 || duration % k == 0;
		}
	});
	/** PotionVisExhaust: wands cost 10% more vis per level (WandManager.getTotalVisDiscount). */
	public static final Holder<MobEffect> VIS_EXHAUST = register("vis_exhaust", new Effect(MobEffectCategory.HARMFUL, 6702199));
	/** PotionInfectiousVisExhaust: spreads exhaustion to everything within 4 blocks, a level weaker each time. */
	public static final Holder<MobEffect> INFECTIOUS_VIS_EXHAUST = register("infectious_vis_exhaust", new Effect(MobEffectCategory.HARMFUL, 6706551) {
		@Override
		public boolean applyEffectTick(ServerLevel level, LivingEntity target, int amp) {
			for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(4.0))) {
				if (!e.hasEffect(INFECTIOUS_VIS_EXHAUST)) {
					e.addEffect(amp > 0 ? new MobEffectInstance(INFECTIOUS_VIS_EXHAUST, 6000, amp - 1) : new MobEffectInstance(VIS_EXHAUST, 6000, 0));
				}
			}
			return true;
		}

		@Override
		public boolean shouldApplyEffectTickThisTick(int duration, int amp) {
			return duration % 40 == 0;
		}
	});
	/** PotionUnnaturalHunger: steady exhaustion. */
	public static final Holder<MobEffect> UNNATURAL_HUNGER = register("unnatural_hunger", new Effect(MobEffectCategory.HARMFUL, 4482611) {
		@Override
		public boolean applyEffectTick(ServerLevel level, LivingEntity target, int amp) {
			if (target instanceof Player p) {
				p.causeFoodExhaustion(0.025F * (amp + 1));
			}
			return true;
		}

		@Override
		public boolean shouldApplyEffectTickThisTick(int duration, int amp) {
			return true;
		}
	});
	/** PotionWarpWard: holds warp events off. */
	public static final Holder<MobEffect> WARP_WARD = register("warp_ward", new Effect(MobEffectCategory.BENEFICIAL, 14742263));
	/** PotionDeathGaze: withers whatever the afflicted looks at (handled by the warp events). */
	public static final Holder<MobEffect> DEATH_GAZE = register("death_gaze", new Effect(MobEffectCategory.HARMFUL, 6702131));
	/** PotionBlurredVision: the client blurs the screen. */
	public static final Holder<MobEffect> BLURRED_VISION = register("blurred_vision", new Effect(MobEffectCategory.HARMFUL, 8421504));
	/** PotionSunScorned: burns in bright open sky, heals in the dark. */
	public static final Holder<MobEffect> SUN_SCORNED = register("sun_scorned", new Effect(MobEffectCategory.HARMFUL, 16308330) {
		@Override
		public boolean applyEffectTick(ServerLevel level, LivingEntity target, int amp) {
			float f = level.getMaxLocalRawBrightness(target.blockPosition()) / 15.0F;
			if (f > 0.5F && level.getRandom().nextFloat() * 30.0F < (f - 0.4F) * 2.0F && level.canSeeSky(target.blockPosition())) {
				target.igniteForSeconds(4.0F);
			} else if (f < 0.25F && level.getRandom().nextFloat() > f * 2.0F) {
				target.heal(1.0F);
			}
			return true;
		}

		@Override
		public boolean shouldApplyEffectTickThisTick(int duration, int amp) {
			return duration % 40 == 0;
		}
	});
	/** PotionThaumarhia: leaves puddles of flux goo behind. */
	public static final Holder<MobEffect> THAUMARHIA = register("thaumarhia", new Effect(MobEffectCategory.HARMFUL, 6702199) {
		@Override
		public boolean applyEffectTick(ServerLevel level, LivingEntity target, int amp) {
			if (level.getRandom().nextInt(15) == 0) {
				BlockPos p = target.blockPosition();
				if (level.isEmptyBlock(p)) {
					level.setBlockAndUpdate(p, TVBlocks.FLUX_GOO.defaultBlockState());
				}
			}
			return true;
		}

		@Override
		public boolean shouldApplyEffectTickThisTick(int duration, int amp) {
			return duration % 20 == 0;
		}
	});

	public static DamageSource taint(ServerLevel level) {
		return level.damageSources().source(TAINT_DAMAGE);
	}

	/** An effect with no per-tick behaviour of its own (MobEffect's constructor is protected). */
	private static class Effect extends MobEffect {
		Effect(MobEffectCategory category, int color) {
			super(category, color);
		}
	}

	private static Holder<MobEffect> register(String name, MobEffect effect) {
		return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Thaumvibe.id(name), effect);
	}

	public static void init() {
	}

	private TVMobEffects() {
	}
}
