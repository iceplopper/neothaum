package dev.neothaum.world.outer;

import dev.neothaum.NeoThaum;
import dev.neothaum.network.TVNetwork;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.research.ResearchManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Outer Lands dimension (WorldProviderOuter) and the trip through an Eldritch Portal (TileEldritchPortal, TeleporterThaumcraft). A maze's
 * portal cell sits on the same chunk as the altar that made it, so each side's portal is looked for in the matching chunk; the traveller
 * steps out one block to the side of it.
 */
public final class OuterLands {
	public static final ResourceKey<Level> KEY = ResourceKey.create(Registries.DIMENSION, NeoThaum.id("outer_lands"));

	private OuterLands() {
	}

	/** Through the portal: to the maze if in the overworld (or anywhere else), back to the overworld if in the maze. */
	public static void travel(ServerPlayer player, BlockPos portal) {
		if (player.isPassenger() || player.isVehicle()) {
			return;
		}
		if (player.isOnPortalCooldown()) {
			player.setPortalCooldown(100);
			return;
		}
		player.setPortalCooldown(100);
		var server = player.level().getServer();
		boolean leaving = player.level().dimension().equals(KEY);
		ServerLevel target = server.getLevel(leaving ? Level.OVERWORLD : KEY);
		if (target == null) {
			return;
		}
		int cx = portal.getX() >> 4;
		int cz = portal.getZ() >> 4;
		Cell cell = MazeHandler.getCell(cx, cz);
		if (!leaving && (cell == null || cell.feature != 1)) {
			// no finished maze behind this portal yet (or one opened before mazes existed): start one, and wait for it
			MazeHandler.checkForMaze(cx, cz, player.getRandom());
			return;
		}
		// load (and so build) the destination chunk, then find its portal
		target.getChunk(cx, cz);
		BlockPos found = findPortal(target, cx, cz);
		Vec3 dest;
		if (found != null) {
			var rand = target.getRandom();
			dest = new Vec3(found.getX() + 0.5 + (rand.nextBoolean() ? 1 : -1), found.getY(), found.getZ() + 0.5 + (rand.nextBoolean() ? 1 : -1));
		} else {
			int top = target.getHeight(Heightmap.Types.MOTION_BLOCKING, player.getBlockX(), player.getBlockZ());
			dest = new Vec3(player.getX(), Math.max(top, target.getMinY() + 1), player.getZ());
		}
		player.teleport(new TeleportTransition(target, dest, Vec3.ZERO, player.getYRot(), player.getXRot(), TeleportTransition.DO_NOTHING));
		if (!leaving) {
			player.sendSystemMessage(Component.literal("Entering The Outer Lands"), true);
			if (ResearchManager.completeResearch(player, "ENTEROUTER")) {
				TVNetwork.sendResearchComplete(player, "ENTEROUTER");
			}
		} else {
			player.sendSystemMessage(Component.literal("Leaving The Outer Lands"), true);
		}
	}

	private static @Nullable BlockPos findPortal(ServerLevel level, int cx, int cz) {
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = level.getMaxY(); y >= level.getMinY(); y--) {
					p.set(cx * 16 + x, y, cz * 16 + z);
					if (level.getBlockState(p).is(TVBlocks.ELDRITCH_PORTAL)) {
						return p.immutable();
					}
				}
			}
		}
		return null;
	}
}
