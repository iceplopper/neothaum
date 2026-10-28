package dev.neothaum.tiles.eldritch;

import dev.neothaum.network.TVNetwork;
import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.warp.Warp;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** TileEldritchTrap: every half to two seconds, zap the nearest player within 3 blocks for 2 magic damage and, half the time, 1-2 warp. */
public class RunedStoneBlockEntity extends BlockEntity {
	private int count = 20;

	public RunedStoneBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.RUNED_STONE, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, RunedStoneBlockEntity trap) {
		if (!(level instanceof ServerLevel sl) || trap.count-- > 0) {
			return;
		}
		trap.count = 10 + level.getRandom().nextInt(25);
		Player p = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0, false);
		if (p != null && !p.isCreative() && !p.isSpectator()) {
			p.hurtServer(sl, sl.damageSources().magic(), 2.0F);
			if (level.getRandom().nextBoolean()) {
				Warp.addWarpToPlayer(p, 1 + level.getRandom().nextInt(2), true);
			}
			TVNetwork.sendZap(sl, pos, p.blockPosition().above());
		}
	}
}
