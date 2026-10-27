package dev.neothaum.mixin;

import dev.neothaum.runic.RunicShielding;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** LivingHurtEvent: Thaumcraft's damage hooks run on the raw damage, before armour. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityHurtMixin {
	@ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true)
	private float neothaum$livingHurt(float dmg, ServerLevel level, DamageSource source) {
		return RunicShielding.onHurt((LivingEntity) (Object) this, source, dmg);
	}
}
