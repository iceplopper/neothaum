package dev.neothaum.tiles.eldritch;

import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.tiles.TVBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1:1 Port of TileEldritchPortal.
 * Swirling black void tear leading to the Outer Lands.
 */
public class EldritchPortalBlockEntity extends TVBlockEntity {
	private int count = 0;
	private int openCount = -1;

	public EldritchPortalBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.ELDRITCH_PORTAL, pos, state);
	}

	public int getOpenCount() {
		return this.openCount;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, EldritchPortalBlockEntity portal) {
		portal.count++;
		if (level.isClientSide()) {
			if (portal.count % 250 == 0 || portal.count == 1) {
				level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, TVSounds.EVILPORTAL, SoundSource.BLOCKS, 1.0F, 1.0F, false);
			}
			if (portal.openCount < 30) {
				portal.openCount++;
			}
		} else if (portal.count % 5 == 0) {
			// every quarter second, anyone standing in the portal goes through it
			var box = new net.minecraft.world.phys.AABB(pos).inflate(0.5, 1.0, 0.5);
			for (ServerPlayer p : level.getEntitiesOfClass(ServerPlayer.class, box)) {
				dev.neothaum.world.outer.OuterLands.travel(p, pos);
			}
		}
	}

	/** Travel is handled by the portal's own tick (TileEldritchPortal.updateEntity). */
	public void onEntityInside(Entity entity) {
	}
}
