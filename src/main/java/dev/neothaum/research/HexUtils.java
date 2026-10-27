package dev.neothaum.research;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.RandomSource;

/** Port of thaumcraft.common.lib.utils.HexUtils (axial hex grid used by research notes). */
public final class HexUtils {
	static final int[][] NEIGHBOURS = {{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {-1, 1}, {0, 1}};

	public static int getDistance(Hex a1, Hex a2) {
		return (Math.abs(a1.q - a2.q) + Math.abs(a1.r - a2.r) + Math.abs(a1.q + a1.r - a2.q - a2.r)) / 2;
	}

	public static Hex getRoundedHex(double qq, double rr) {
		double xx = qq, yy = rr, zz = -qq - rr;
		int rx = (int) Math.round(xx);
		int ry = (int) Math.round(yy);
		int rz = (int) Math.round(zz);
		double xd = Math.abs(rx - xx);
		double yd = Math.abs(ry - yy);
		double zd = Math.abs(rz - zz);
		if (xd > yd && xd > zd) {
			rx = -ry - rz;
		} else if (yd > zd) {
			ry = -rx - rz;
		} else {
			rz = -rx - ry;
		}
		return new Hex(rx, rz);
	}

	public static List<Hex> getRing(int radius) {
		Hex h = new Hex(0, 0);
		for (int k = 0; k < radius; k++) {
			h = h.getNeighbour(4);
		}
		List<Hex> ring = new ArrayList<>();
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < radius; j++) {
				ring.add(h);
				h = h.getNeighbour(i);
			}
		}
		return ring;
	}

	public static List<Hex> distributeRingRandomly(int radius, int entries, RandomSource random) {
		List<Hex> ring = getRing(radius);
		List<Hex> results = new ArrayList<>();
		float spacing = (float) ring.size() / entries;
		random.nextInt(ring.size()); // Thaumcraft rolls a start index it never uses
		float pos = 0.0F;
		for (int i = 0; i < entries; i++) {
			results.add(ring.get(Math.round(pos) % ring.size()));
			pos += spacing;
		}
		return results;
	}

	public static Map<String, Hex> generateHexes(int radius) {
		Map<String, Hex> results = new LinkedHashMap<>();
		Hex h = new Hex(0, 0);
		results.put(h.toString(), h);
		for (int k = 0; k < radius; k++) {
			h = h.getNeighbour(4);
			Hex hd = new Hex(h.q, h.r);
			for (int i = 0; i < 6; i++) {
				for (int j = 0; j <= k; j++) {
					results.put(hd.toString(), hd);
					hd = hd.getNeighbour(i);
				}
			}
		}
		return results;
	}

	public record Hex(int q, int r) {
		public Hex getNeighbour(int direction) {
			int[] d = NEIGHBOURS[direction];
			return new Hex(this.q + d[0], this.r + d[1]);
		}

		public double[] toPixel(int size) {
			return new double[]{size * 1.5 * this.q, size * Math.sqrt(3.0) * (this.r + this.q / 2.0)};
		}

		public static Hex fromPixel(double x, double y, int size) {
			double qq = 2.0 / 3.0 * x / size;
			double rr = (1.0 / 3.0 * Math.sqrt(3.0) * -y - 1.0 / 3.0 * x) / size;
			return getRoundedHex(qq, rr);
		}

		@Override
		public String toString() {
			return this.q + ":" + this.r;
		}
	}

	private HexUtils() {
	}
}
