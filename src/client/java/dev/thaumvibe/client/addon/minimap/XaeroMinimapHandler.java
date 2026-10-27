package dev.thaumvibe.client.addon.minimap;

import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.api.nodes.NodeModifier;
import dev.thaumvibe.api.nodes.NodeType;
import dev.thaumvibe.items.GogglesItem;
import dev.thaumvibe.tiles.NodeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import xaero.common.XaeroMinimapSession;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.WaypointColor;

public final class XaeroMinimapHandler {

	public static void update(Player player, Level level) {
		XaeroMinimapSession xSession = XaeroMinimapSession.getCurrentSession();
		if (xSession == null || xSession.getMinimapProcessor() == null) {
			return;
		}
		MinimapSession mSession = xSession.getMinimapProcessor().getSession();
		if (mSession == null || mSession.getWorldManager() == null) {
			return;
		}

		var customWaypoints = mSession.getWorldManager().getCustomWaypoints(Thaumvibe.id("nodes"));
		if (customWaypoints == null) {
			return;
		}
		customWaypoints.clear();

		// Strictly locked behind Goggles of Revealing, NO thaumometer
		if (!GogglesItem.isWearingGoggles(player)) {
			return;
		}

		BlockPos center = player.blockPosition();
		int pChunkX = center.getX() >> 4;
		int pChunkZ = center.getZ() >> 4;
		int chunkRadius = 3;
		int index = 0;

		for (int cx = pChunkX - chunkRadius; cx <= pChunkX + chunkRadius; cx++) {
			for (int cz = pChunkZ - chunkRadius; cz <= pChunkZ + chunkRadius; cz++) {
				LevelChunk chunk = level.getChunkSource().getChunk(cx, cz, false);
				if (chunk == null) {
					continue;
				}
				for (BlockEntity be : chunk.getBlockEntities().values()) {
					if (be instanceof NodeBlockEntity node) {
						BlockPos pos = node.getBlockPos();
						NodeType type = node.getNodeType();
						NodeModifier mod = node.getNodeModifier();
						String name = "Node: " + type.name() + (mod != null ? " (" + mod.name() + ")" : "");
						WaypointColor color = switch (type) {
							case TAINTED -> WaypointColor.MAGENTA;
							case HUNGRY -> WaypointColor.RED;
							case DARK -> WaypointColor.DARK_GRAY;
							case PURE -> WaypointColor.GREEN;
							case UNSTABLE -> WaypointColor.GOLD;
							default -> mod == NodeModifier.BRIGHT ? WaypointColor.WHITE : WaypointColor.AQUA;
						};
						Waypoint wp = new Waypoint(pos.getX(), pos.getY(), pos.getZ(), name, "✦", color);
						customWaypoints.put(index++, wp);
					}
				}
			}
		}
	}

	private XaeroMinimapHandler() {
	}
}
