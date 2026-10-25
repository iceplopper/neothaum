package dev.thaumvibe.client.fx;

import dev.thaumvibe.fx.FX;
import dev.thaumvibe.tiles.InfusionMatrixBlockEntity;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

/** EssentiaHandler.sourceFX on the client: a trail from each drained jar to the device drinking from it, kept up for 15 ticks per point. */
public final class SourceFX {
	private record Key(BlockPos start, BlockPos end, int color) {
	}

	private static final Map<Key, int[]> TRAILS = new LinkedHashMap<>();
	private static int tickCount;

	public static void essentia(BlockPos device, BlockPos source, int color) {
		Key k = new Key(device, source, color);
		TRAILS.remove(k);
		TRAILS.put(k, new int[]{15});
	}

	/** ClientTickEventsFML: draws each live trail from the source up to the device (an infusion matrix one block lower, to its centre). */
	public static void tick(Minecraft mc) {
		tickCount++;
		var level = mc.level;
		if (level == null) {
			TRAILS.clear();
			return;
		}
		for (Iterator<Map.Entry<Key, int[]>> it = TRAILS.entrySet().iterator(); it.hasNext(); ) {
			var e = it.next();
			Key k = e.getKey();
			int[] ticks = e.getValue();
			if (ticks[0] <= 0) {
				it.remove();
				continue;
			}
			int mod = level.getBlockEntity(k.start()) instanceof InfusionMatrixBlockEntity ? -1 : 0;
			float scale = ticks[0] > 5 ? 1.0F : ticks[0] * ticks[0] / 25.0F;
			FX.essentiaTrail(level, k.end().getX() + 0.5, k.end().getY() + 0.5, k.end().getZ() + 0.5, k.start().getX() + 0.5,
				k.start().getY() + 0.5 + mod, k.start().getZ() + 0.5, 1, k.color(), scale);
			ticks[0]--;
		}
	}

	private SourceFX() {
	}
}
