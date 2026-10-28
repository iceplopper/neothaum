package dev.neothaum.world.outer;

/**
 * Cell: one chunk of an Outer Lands maze, packed into a short. Bits 0-5 are the open sides (north, south, east, west, above, below); the
 * high byte is the feature: 1 portal, 2-5 the four quarters of the boss room, 6 key room, 7 nest, 8 library, 9 plain, 10-14 passage
 * variants (10 plain, 11 trapped, 12 crusted, 13 tainted, 14 spider hall), 99 a temporary mark while the boss room is connected.
 */
public final class Cell {
	public boolean north;
	public boolean south;
	public boolean east;
	public boolean west;
	public boolean above;
	public boolean below;
	public byte feature;

	public Cell() {
	}

	public Cell(short data) {
		this.north = (data & 1) != 0;
		this.south = (data & 2) != 0;
		this.east = (data & 4) != 0;
		this.west = (data & 8) != 0;
		this.above = (data & 16) != 0;
		this.below = (data & 32) != 0;
		this.feature = (byte) (data >> 8);
	}

	public short pack() {
		int out = 0;
		if (this.north) {
			out |= 1;
		}
		if (this.south) {
			out |= 2;
		}
		if (this.east) {
			out |= 4;
		}
		if (this.west) {
			out |= 8;
		}
		if (this.above) {
			out |= 16;
		}
		if (this.below) {
			out |= 32;
		}
		out |= this.feature << 8;
		return (short) out;
	}
}
