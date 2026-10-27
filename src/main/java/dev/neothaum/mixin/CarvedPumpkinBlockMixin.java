package dev.neothaum.mixin;

import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Disables vanilla block-assembly crafting of the Copper Golem, requiring it to be
 * created through Thaumcraft's Golemancy crucible alchemy instead.
 */
@Mixin(CarvedPumpkinBlock.class)
public abstract class CarvedPumpkinBlockMixin {
	@Inject(method = "getOrCreateCopperGolemBase", at = @At("HEAD"), cancellable = true)
	private void neothaum$disableVanillaCopperGolemBase(CallbackInfoReturnable<BlockPattern> cir) {
		cir.setReturnValue(null);
	}

	@Inject(method = "getOrCreateCopperGolemFull", at = @At("HEAD"), cancellable = true)
	private void neothaum$disableVanillaCopperGolemFull(CallbackInfoReturnable<BlockPattern> cir) {
		cir.setReturnValue(null);
	}
}
