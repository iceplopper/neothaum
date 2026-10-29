package dev.neothaum.mixin;

import dev.neothaum.runic.RunicShielding;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * LivingHurtEvent for players: Player overrides actuallyHurt without calling LivingEntity's, so {@link LivingEntityHurtMixin} never sees a
 * player being hurt. Runic shielding, fortress armour and masks, and champions' on-hit effects all hang on this.
 */
@Mixin(Player.class)
public abstract class PlayerHurtMixin {
	@ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true)
	private float neothaum$playerHurt(float dmg, ServerLevel level, DamageSource source) {
		LivingEntity self = (LivingEntity) (Object) this;
		return dev.neothaum.entity.champion.ChampionEvents.onHurt(self, source, RunicShielding.onHurt(self, source, dmg));
	}
}
