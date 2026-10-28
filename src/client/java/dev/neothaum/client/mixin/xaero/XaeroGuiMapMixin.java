package dev.neothaum.client.mixin.xaero;

import dev.neothaum.client.addon.minimap.WorldMapNodes;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Xaero's World Map screen: adds the aura node button and draws the nodes over the map. Skipped when the World Map is not installed. */
@Pseudo
@Mixin(targets = "xaero.map.gui.GuiMap", remap = false)
public abstract class XaeroGuiMapMixin {
	@Shadow
	private double cameraX;
	@Shadow
	private double cameraZ;
	@Shadow
	private double scale;
	@Shadow
	private double screenScale;
	@Shadow
	private ResourceKey<Level> lastViewedDimensionId;

	@Inject(method = "init", at = @At("TAIL"), require = 0)
	private void neothaum$addNodeButton(CallbackInfo ci) {
		Screen screen = (Screen) (Object) this;
		WorldMapNodes.addToggle(screen, b -> ((ScreenWidgetAccessor) screen).neothaum$addRenderableWidget(b));
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"), require = 0)
	private void neothaum$drawNodes(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		WorldMapNodes.render(g, (Screen) (Object) this, mouseX, mouseY, this.cameraX, this.cameraZ, this.scale, this.screenScale,
			this.lastViewedDimensionId);
	}
}
