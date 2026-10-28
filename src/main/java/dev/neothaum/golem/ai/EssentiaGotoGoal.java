package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import dev.neothaum.golem.GolemHelper;
import net.minecraft.core.BlockPos;

/** AIEssentiaGoto: carrying essentia, walk to the jar, reservoir or tube it should go into. */
public class EssentiaGotoGoal extends GotoGoal {
	public EssentiaGotoGoal(GolemEntity golem) {
		super(golem);
	}

	@Override
	public boolean canUse() {
		if (!this.delayTick() || this.golem.essentia == null || this.golem.essentiaAmount == 0) {
			return false;
		}
		BlockPos jar = GolemHelper.findJarWithRoom(this.golem);
		if (jar == null) {
			return false;
		}
		this.setTarget(jar.getX(), jar.getY(), jar.getZ());
		return true;
	}
}
