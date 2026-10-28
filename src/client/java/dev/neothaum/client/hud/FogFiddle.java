package dev.neothaum.client.hud;

/**
 * RenderEventHandler.fogFiddled: a thick fog that closes in around the player (warp's creeping mist for two minutes, an eldritch guardian's
 * gaze for ten seconds). Its exponential density climbs to 0.1 and thins out again over the last five seconds.
 */
public final class FogFiddle {
	public static boolean fiddled;
	public static float target;
	public static int duration;

	private FogFiddle() {
	}

	/** PacketMiscEvent 1: the warp fog. */
	public static void warpFog() {
		fiddled = true;
		duration = 2400;
	}

	/** PacketMiscEvent 2: a guardian's gaze. */
	public static void gaze() {
		fiddled = true;
		if (duration < 200) {
			duration = 200;
		}
	}

	/** ClientTickEventsFML.clientTick. */
	public static void tick() {
		if (!fiddled) {
			return;
		}
		if (duration < 100) {
			target = 0.1F * (duration / 100.0F);
		} else if (target < 0.1F) {
			target += 0.001F;
		}
		duration--;
		if (duration < 0) {
			fiddled = false;
			target = 0.0F;
		}
	}

	/** How far the fog lets you see: exp2 fog of this density is all but opaque at 1.73 / density blocks. */
	public static float visibleDistance() {
		return fiddled && target > 0.0F ? 1.73F / target : Float.MAX_VALUE;
	}
}
