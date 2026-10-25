package dev.thaumvibe.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** IInfusionStabiliser: blocks placed symmetrically around an infusion altar that calm it (candles, crystal clusters; skulls count too). */
public interface InfusionStabiliser {
	boolean canStabaliseInfusion(Level level, BlockPos pos);
}
