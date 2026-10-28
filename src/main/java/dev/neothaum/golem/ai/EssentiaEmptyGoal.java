package dev.neothaum.golem.ai;

import dev.neothaum.api.aspects.EssentiaTransport;
import dev.neothaum.golem.GolemEntity;
import dev.neothaum.golem.GolemHelper;
import dev.neothaum.tiles.JarBlockEntity;
import dev.neothaum.tiles.ReservoirBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.entity.BlockEntity;

/** AIEssentiaEmpty: beside the jar, reservoir or tube its essentia belongs in, pour it in. */
public class EssentiaEmptyGoal extends GolemGoal {
	private BlockPos jar;

	public EssentiaEmptyGoal(GolemEntity golem) {
		super(golem);
	}

	@Override
	public boolean canUse() {
		if (!this.navDone() || this.golem.essentia == null || this.golem.essentiaAmount == 0) {
			return false;
		}
		this.jar = GolemHelper.findJarWithRoom(this.golem);
		return this.jar != null && GolemHelper.distSqr(this.golem, this.jar) <= 4.0;
	}

	@Override
	public boolean canContinueToUse() {
		return false;
	}

	private void poured(int added) {
		this.golem.essentiaAmount -= added;
		if (this.golem.essentiaAmount <= 0) {
			this.golem.essentiaAmount = 0;
			this.golem.essentia = null;
		}
		this.golem.playSound(SoundEvents.GENERIC_SWIM, 0.2F, 1.0F + (this.golem.getRandom().nextFloat() - this.golem.getRandom().nextFloat()) * 0.3F);
		this.golem.updateCarried();
	}

	@Override
	public void start() {
		BlockEntity tile = this.golem.level().getBlockEntity(this.jar);
		if (tile instanceof JarBlockEntity j) {
			int left = j.addToContainer(this.golem.essentia, this.golem.essentiaAmount);
			this.poured(this.golem.essentiaAmount - left);
		} else if (tile instanceof ReservoirBlockEntity r) {
			if (r.getSuctionAmount(r.facing) > 0 && (r.getSuctionType(r.facing) == null || r.getSuctionType(r.facing) == this.golem.essentia)) {
				int added = r.addEssentia(this.golem.essentia, this.golem.essentiaAmount, r.facing);
				if (added > 0) {
					this.poured(added);
				}
			}
		} else if (tile instanceof EssentiaTransport trans) {
			for (int s : GolemHelper.getMarkedSides(this.golem, this.jar, (byte) -1)) {
				Direction d = Direction.from3DDataValue(s);
				if (trans.canInputFrom(d) && trans.getSuctionAmount(d) > 0 && (trans.getSuctionType(d) == null || trans.getSuctionType(d) == this.golem.essentia)) {
					int added = trans.addEssentia(this.golem.essentia, this.golem.essentiaAmount, d);
					if (added > 0) {
						this.poured(added);
						break;
					}
				}
			}
		}
	}
}
