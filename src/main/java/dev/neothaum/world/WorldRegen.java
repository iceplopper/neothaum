package dev.neothaum.world;

import com.mojang.serialization.Codec;
import dev.neothaum.NeoThaum;
import dev.neothaum.config.NeoThaumConfig;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * TC4's world regeneration (EventHandlerWorld chunkSave/chunkLoad and ServerTickEventsFML): every chunk carries the regen key it was last
 * generated under. A chunk loaded without the current key, while any World_Regeneration switch is on, is queued, and the queue runs a few
 * chunks a tick through ThaumcraftWorldGenerator with newGen false, which redoes only the switched-on parts. As in TC4, a chunk loaded while
 * every switch is off just takes the key, so turning a switch on later needs a new regen key to reach chunks already visited.
 */
public final class WorldRegen {
	public static final AttachmentType<String> KEY = AttachmentRegistry.<String>builder()
		.persistent(Codec.STRING)
		.buildAndRegister(NeoThaum.id("regen_key"));
	/** ServerTickEventsFML: at most this many chunks regenerated per tick in each world. */
	private static final int PER_TICK = 10;
	private static final Map<ResourceKey<Level>, ArrayDeque<ChunkPos>> QUEUE = new HashMap<>();

	public static void init() {
		ServerChunkEvents.CHUNK_LOAD.register(WorldRegen::chunkLoaded);
		ServerTickEvents.END_SERVER_TICK.register(WorldRegen::tick);
		ServerLifecycleEvents.SERVER_STOPPED.register(s -> QUEUE.clear());
	}

	/** Regeneration covers the worlds TC4's generator ran in: the overworld and the nether (not the end, nor the Outer Lands). */
	private static boolean regenerates(ServerLevel level) {
		return level.dimension() == Level.OVERWORLD || level.dimension() == Level.NETHER;
	}

	private static void chunkLoaded(ServerLevel level, LevelChunk chunk, boolean generated) {
		var cfg = NeoThaumConfig.current;
		if (cfg.regenKey.equals(chunk.getAttached(KEY))) {
			return;
		}
		if (!generated && cfg.anyRegen() && regenerates(level)) {
			QUEUE.computeIfAbsent(level.dimension(), k -> new ArrayDeque<>()).add(chunk.getPos());
		} else {
			chunk.setAttached(KEY, cfg.regenKey);
		}
	}

	private static void tick(MinecraftServer server) {
		if (QUEUE.isEmpty()) {
			return;
		}
		for (var it = QUEUE.entrySet().iterator(); it.hasNext(); ) {
			var entry = it.next();
			ServerLevel level = server.getLevel(entry.getKey());
			ArrayDeque<ChunkPos> queue = entry.getValue();
			for (int done = 0; level != null && done < PER_TICK && !queue.isEmpty(); ) {
				ChunkPos pos = queue.poll();
				// only while it is still loaded; an unloaded chunk without the key is queued again when it next loads
				LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x(), pos.z());
				if (chunk == null || NeoThaumConfig.current.regenKey.equals(chunk.getAttached(KEY))) {
					continue;
				}
				TCWorldFeature.regenerate(level, pos);
				chunk.setAttached(KEY, NeoThaumConfig.current.regenKey);
				chunk.markUnsaved();
				done++;
				NeoThaum.LOGGER.info("[NeoThaum] Regenerated chunk {} in {}", pos, entry.getKey().identifier());
			}
			if (level == null || queue.isEmpty()) {
				it.remove();
			}
		}
	}

	private WorldRegen() {
	}
}
