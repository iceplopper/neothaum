package dev.thaumvibe.api.aspects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * IEssentiaTransport: anything essentia flows through (tubes, jars, alembics). Essentia moves one point at a time towards whichever
 * neighbour has the higher suction. A null face is ForgeDirection.UNKNOWN in TC4.
 */
public interface EssentiaTransport {
	boolean isConnectable(Direction face);

	boolean canInputFrom(Direction face);

	boolean canOutputTo(Direction face);

	void setSuction(Aspect aspect, int amount);

	Aspect getSuctionType(Direction face);

	int getSuctionAmount(Direction face);

	/**
	 * How hard this side pulls for one particular aspect: its suction if that is untyped or of this aspect, else nothing. Tubes override it
	 * to answer from their per-aspect suction.
	 */
	default int getSuctionFor(Aspect aspect, Direction face) {
		Aspect t = this.getSuctionType(face);
		return t == null || t == aspect ? this.getSuctionAmount(face) : 0;
	}

	/** Removes up to {@code amount} of {@code aspect}; returns how much was taken. */
	int takeEssentia(Aspect aspect, int amount, Direction face);

	/** Adds up to {@code amount} of {@code aspect}; returns how much was accepted. */
	int addEssentia(Aspect aspect, int amount, Direction face);

	Aspect getEssentiaType(Direction face);

	int getEssentiaAmount(Direction face);

	int getMinimumSuction();

	/** Whether a tube joining this face draws its long connector (jars). */
	boolean renderExtendedTube();

	/** ThaumcraftApiHelper.getConnectableTile: the transport on {@code face} of {@code pos}, if it accepts a connection from this side. */
	static EssentiaTransport connectable(BlockGetter level, BlockPos pos, Direction face) {
		BlockEntity be = level.getBlockEntity(pos.relative(face));
		return be instanceof EssentiaTransport t && t.isConnectable(face.getOpposite()) ? t : null;
	}
}
