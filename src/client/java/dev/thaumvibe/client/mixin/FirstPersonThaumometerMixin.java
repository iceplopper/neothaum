package dev.thaumvibe.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.thaumvibe.client.render.ThaumometerRenderer;
import dev.thaumvibe.items.ThaumometerItem;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ItemThaumometerRenderer EQUIPPED_FIRST_PERSON: the player holds the thaumometer in both hands in front of them, like a map,
 * with the scan readout on its screen.
 */
@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class FirstPersonThaumometerMixin {
	private static final float HELD_PITCH = 50.0F;

	@Shadow
	protected abstract void renderTwoHandedMap(PoseStack ps, SubmitNodeCollector collector, int light, float pitch, float equipProgress, float swingProgress,
		PlayerRenderState player, FirstPersonHandsAndItemsRenderState state);

	@Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
	private void thaumvibe$holdThaumometer(PlayerRenderState player, FirstPersonHandsAndItemsRenderState state, float partialTick, float pitch,
		InteractionHand hand, float swingProgress, ItemStack stack, float equipProgress, PoseStack ps, SubmitNodeCollector collector, int light,
		CallbackInfo ci) {
		if (hand == InteractionHand.MAIN_HAND && stack.getItem() instanceof ThaumometerItem && !state.isScoping && player.avatarRenderState != null) {
			ps.pushPose();
			// unlike a map, the thaumometer does not dip and tilt with the view pitch: it stays put in front of the player
			this.renderTwoHandedMap(ps, collector, light, HELD_PITCH, equipProgress, swingProgress, player, state);
			ps.popPose();
			ci.cancel();
		}
	}

	@Inject(method = "renderMap", at = @At("HEAD"), cancellable = true)
	private void thaumvibe$drawThaumometer(PoseStack ps, SubmitNodeCollector collector, int light, ItemStack stack, boolean twoHanded,
		FirstPersonHandsAndItemsRenderState state, CallbackInfo ci) {
		if (stack.getItem() instanceof ThaumometerItem) {
			// map space: x right, y up, z towards the viewer; stand the scanner's face towards the player
			ps.pushPose();
			ps.rotateDegrees(Axis.ZP, 90.0F);
			ps.rotateDegrees(Axis.XP, 90.0F);
			ps.scale(0.13F, 0.13F, 0.13F);
			ThaumometerRenderer.draw(ps, collector, light, true);
			ps.popPose();
			ci.cancel();
		}
	}
}
