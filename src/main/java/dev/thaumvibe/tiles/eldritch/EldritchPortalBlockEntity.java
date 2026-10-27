package dev.thaumvibe.tiles.eldritch;

import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.registry.TVSounds;
import dev.thaumvibe.research.ResearchManager;
import dev.thaumvibe.tiles.TVBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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
			if (level.getRandom().nextInt(3) == 0) {
				double px = pos.getX() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 1.5;
				double py = pos.getY() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 1.5;
				double pz = pos.getZ() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 1.5;
				level.addParticle(ParticleTypes.REVERSE_PORTAL, px, py, pz, (pos.getX() + 0.5 - px) * 0.1, (pos.getY() + 0.5 - py) * 0.1, (pos.getZ() + 0.5 - pz) * 0.1);
			}
		}
	}

	public void onEntityInside(Entity entity) {
		if (entity instanceof Player player) {
			if (!this.hasLevel() || this.level.isClientSide()) return;
			ResearchManager.completeResearch(player, "ENTEROUTER");
			if (player instanceof ServerPlayer sp) {
				// Outer lands dimension teleport hook (Feature 11)
				// For now, disorient with warp audio and particles if inside the gateway
				if (this.count % 20 == 0) {
					this.level.playSound(null, this.worldPosition, TVSounds.ZAP, SoundSource.BLOCKS, 0.8F, 0.8F);
				}
			}
		}
	}
}
