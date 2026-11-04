package dev.neothaum.client.fx;

import dev.neothaum.client.render.SoftBeam;
import dev.neothaum.tiles.InfusionMatrixBlockEntity;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * EssentiaHandler.sourceFX on the client: while a device (an infusion matrix, most of all) drinks from a jar, essentia streams from the jar to
 * it, kept up for 15 ticks after each point. TC4 sent wisp particles; these are three thin soft strands per jar, each wavering in its own way,
 * dimmer than a node's drain, fading as the drinking stops.
 */
public final class SourceFX {
	private record Key(BlockPos start, BlockPos end, int color) {
	}

	private static final Map<Key, int[]> TRAILS = new LinkedHashMap<>();
	private static final int STRANDS = 3;

	public static void essentia(BlockPos device, BlockPos source, int color) {
		Key k = new Key(device, source, color);
		TRAILS.remove(k);
		TRAILS.put(k, new int[]{15});
	}

	public static void tick(Minecraft mc) {
		if (mc.level == null) {
			TRAILS.clear();
			return;
		}
		for (Iterator<Map.Entry<Key, int[]>> it = TRAILS.entrySet().iterator(); it.hasNext(); ) {
			int[] ticks = it.next().getValue();
			if (--ticks[0] <= 0) {
				it.remove();
			}
		}
	}

	/** Each live stream, from the jar to the device (an infusion matrix one block lower, to its centre). */
	public static void render(LevelRenderContext ctx) {
		Minecraft mc = Minecraft.getInstance();
		if (TRAILS.isEmpty() || mc.level == null) {
			return;
		}
		Vec3 cam = mc.gameRenderer.mainCamera().position();
		var ps = ctx.poseStack();
		ps.pushPose();
		ps.translate(-cam.x, -cam.y, -cam.z);
		for (var e : TRAILS.entrySet()) {
			Key k = e.getKey();
			int mod = mc.level.getBlockEntity(k.start()) instanceof InfusionMatrixBlockEntity ? -1 : 0;
			Vec3 from = Vec3.atCenterOf(k.end());
			Vec3 to = Vec3.atCenterOf(k.start()).add(0, mod, 0);
			float fade = Math.min(1.0F, e.getValue()[0] / 5.0F);
			// each pair of points wavers in its own way, and each of its strands differently again
			float seed = (k.end().hashCode() & 1023) / 97.0F;
			for (int s = 0; s < STRANDS; s++) {
				SoftBeam.draw(ps, ctx.submitNodeCollector(), cam, from, to, k.color(), 0.45F * fade, 0.12F + 0.04F * s, 0.35F + 0.2F * s,
					seed + s * 2.1F, 1.2F + 0.5F * s, 1.0F);
			}
		}
		ps.popPose();
	}

	private SourceFX() {
	}
}
