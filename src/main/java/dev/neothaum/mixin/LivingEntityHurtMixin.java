package dev.neothaum.mixin;

import dev.neothaum.runic.RunicShielding;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** LivingHurtEvent: Thaumcraft's damage hooks run on the raw damage, before armour. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityHurtMixin {
	@ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true)
	private float neothaum$livingHurt(float dmg, ServerLevel level, DamageSource source) {
		LivingEntity self = (LivingEntity) (Object) this;
		return dev.neothaum.entity.champion.ChampionEvents.onHurt(self, source, RunicShielding.onHurt(self, source, dmg));
	}

	/** EventHandlerEntity.livingTick: champions' per-tick modifiers. */
	@Inject(method = "tick", at = @At("TAIL"))
	private void neothaum$championTick(CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!self.level().isClientSide() && dev.neothaum.entity.champion.ChampionEvents.isChampion(self)) {
			dev.neothaum.entity.champion.ChampionEvents.tick(self);
		}
	}
}
