package dev.thaumvibe.golem;

import net.minecraft.util.StringRepresentable;

/**
 * Port of ItemGolemDecoration types:
 * Visual and functional accessories worn by golems.
 */
public enum GolemDecorationType implements StringRepresentable {
	TOP_HAT(0, "tophat", "golemdecotophat", 'H'),
	GLASSES(1, "glasses", "golemdecoglasses", 'G'),
	BOWTIE(2, "bowtie", "golemdecobowtie", 'B'),
	FEZ(3, "fez", "golemdecofez", 'F'),
	DARTGUN(4, "dart", "golemdecodart", 'R'),
	VISOR(5, "visor", "golemdecovisor", 'V'),
	ARMOR(6, "armor", "golemdecoarmor", 'P'),
	MACE(7, "mace", "golemdecomace", 'M');

	public final int id;
	private final String name;
	public final String texture;
	public final char code;

	GolemDecorationType(int id, String name, String texture, char code) {
		this.id = id;
		this.name = name;
		this.texture = texture;
		this.code = code;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

	public static GolemDecorationType byId(int id) {
		GolemDecorationType[] values = values();
		if (id < 0 || id >= values.length) {
			return TOP_HAT;
		}
		return values[id];
	}

	public static GolemDecorationType byCode(char code) {
		for (GolemDecorationType d : values()) {
			if (d.code == code) {
				return d;
			}
		}
		return null;
	}
}
