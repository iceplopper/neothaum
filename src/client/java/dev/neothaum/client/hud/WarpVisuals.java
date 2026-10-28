package dev.neothaum.client.hud;

import dev.neothaum.NeoThaum;
import dev.neothaum.registry.TVMobEffects;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * ClientTickEventsFML.checkShaders and RenderEventHandler.renderVignette: the warp effects' screen shaders (Death Gaze desaturates, Blurred
 * Vision blurs, Unnatural Hunger reddens, Sun Scorned over-exposes) and the dark vignette that closes in during a warp heartbeat or while
 * the Death Gaze lasts.
 */
public final class WarpVisuals {
	private static final Identifier VIGNETTE = NeoThaum.id("textures/misc/vignette.png");
	private static final Identifier DESATURATE = NeoThaum.id("desaturate");
	private static final Identifier BLUR = NeoThaum.id("blur");
	private static final Identifier HUNGER = NeoThaum.id("hunger");
	private static final Identifier SUNSCORNED = NeoThaum.id("sunscorned");
	public static int warpVignette;
	private static float targetBrightness = 1.0F;
	private static float prevVignetteBrightness;

	private WarpVisuals() {
	}

	/** PacketMiscEvent 0: the heartbeat darkens the edges of the screen for five seconds. */
	public static void heartbeat() {
		warpVignette = 100;
	}

	public static void tick(Minecraft mc) {
		Player player = mc.player;
		if (player == null || mc.isPaused()) {
			return;
		}
		if (player.hasEffect(TVMobEffects.DEATH_GAZE)) {
			warpVignette = 10;
		}
		if (warpVignette > 0) {
			warpVignette--;
			targetBrightness = 0.0F;
		} else {
			targetBrightness = 1.0F;
		}
	}

	/** The shaders for the effects the player has, in TC4's order. */
	public static void addPostEffects(List<Identifier> out) {
		Player player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		if (player.hasEffect(TVMobEffects.DEATH_GAZE)) {
			out.add(DESATURATE);
		}
		if (player.hasEffect(TVMobEffects.BLURRED_VISION)) {
			out.add(BLUR);
		}
		if (player.hasEffect(TVMobEffects.UNNATURAL_HUNGER)) {
			out.add(HUNGER);
		}
		if (player.hasEffect(TVMobEffects.SUN_SCORNED)) {
			out.add(SUNSCORNED);
		}
	}

	public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
		Player player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		float brightness = 1.0F - targetBrightness;
		prevVignetteBrightness = (float) (prevVignetteBrightness + (brightness - prevVignetteBrightness) * 0.01);
		if (prevVignetteBrightness > 0.0F) {
			float b = Mth.clamp(prevVignetteBrightness * (1.0F + Mth.sin(player.tickCount / 2.0F) * 0.1F), 0.0F, 1.0F);
			g.blit(RenderPipelines.VIGNETTE, VIGNETTE, 0, 0, 0.0F, 0.0F, g.guiWidth(), g.guiHeight(), g.guiWidth(), g.guiHeight(),
				ARGB.colorFromFloat(1.0F, b, b, b));
		}
	}
}
