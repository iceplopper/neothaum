package dev.neothaum.baubles;

/** Port of baubles.api.BaubleType. Slot layout: 0 amulet, 1-2 rings, 3 belt. */
public enum BaubleType {
	RING, AMULET, BELT;

	public boolean fitsSlot(int slot) {
		return switch (this) {
			case AMULET -> slot == 0;
			case RING -> slot == 1 || slot == 2;
			case BELT -> slot == 3;
		};
	}
}
