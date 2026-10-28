package dev.neothaum.golem.ai;

import dev.neothaum.golem.GolemEntity;
import dev.neothaum.golem.GolemHelper;
import dev.neothaum.golem.GolemInventory;
import dev.neothaum.golem.GolemMarker;
import java.util.List;
import net.minecraft.core.BlockPos;

/** AIEmptyGoto: carrying something, walk to the nearest marked container with room for it, or else to a marked spot to drop it. */
public class EmptyGotoGoal extends GotoGoal {
	public EmptyGotoGoal(GolemEntity golem) {
		super(golem);
	}

	@Override
	public boolean canUse() {
		if (this.golem.itemCarried.isEmpty() || !this.delayTick()) {
			return false;
		}
		List<Byte> colors = this.golem.getColorsMatching(this.golem.itemCarried);
		float dmod = this.golem.getRange();
		for (byte color : colors) {
			BlockPos dest = null;
			double range = Double.MAX_VALUE;
			for (BlockPos p : GolemHelper.getContainersWithRoom(this.golem, color, this.golem.itemCarried)) {
				double distance = GolemHelper.distSqr(this.golem, p);
				if (distance < range && distance <= dmod * dmod && !GolemHelper.isHomeContainer(this.golem, p)) {
					range = distance;
					dest = p;
				}
			}
			if (dest != null) {
				this.setTarget(dest.getX(), dest.getY(), dest.getZ());
				return true;
			}
		}
		for (byte color : colors) {
			for (GolemMarker m : this.golem.getMarkers()) {
				if ((m.color() == color || color == -1) && GolemInventory.getContainer(this.golem.level(), m.pos()) == null) {
					this.setTarget(m.pos().getX(), m.pos().getY(), m.pos().getZ());
					return true;
				}
			}
		}
		return false;
	}
}
