package dev.neothaum.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Forge's ItemCraftedEvent, which TC4 gave crafting warp on: vanilla calls onCraftedBy when a crafted stack is taken. */
@Mixin(ItemStack.class)
public abstract class ItemStackCraftedMixin {
	@Inject(method = "onCraftedBy", at = @At("HEAD"))
	private void neothaum$craftingWarp(Player player, int craftCount, CallbackInfo ci) {
		dev.neothaum.warp.CraftingWarp.onCrafted((ItemStack) (Object) this, player);
	}
}
