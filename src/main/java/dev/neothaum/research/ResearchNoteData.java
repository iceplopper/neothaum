package dev.neothaum.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.neothaum.api.aspects.Aspect;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Port of ResearchNoteData + ResearchManager.HexEntry: the contents of a research note's hex grid. */
public final class ResearchNoteData {
	public record HexEntry(Aspect aspect, int type) {
	}

	public record Cell(int q, int r, int type, Optional<Aspect> aspect) {
		static final Codec<Cell> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.INT.fieldOf("q").forGetter(Cell::q), Codec.INT.fieldOf("r").forGetter(Cell::r), Codec.INT.fieldOf("type").forGetter(Cell::type),
			Aspect.CODEC.optionalFieldOf("aspect").forGetter(Cell::aspect)).apply(i, Cell::new));
		static final StreamCodec<ByteBuf, Cell> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Cell::q, ByteBufCodecs.VAR_INT, Cell::r,
			ByteBufCodecs.VAR_INT, Cell::type, ByteBufCodecs.optional(Aspect.STREAM_CODEC), Cell::aspect, Cell::new);
	}

	public static final Codec<ResearchNoteData> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.STRING.fieldOf("key").forGetter(d -> d.key), Codec.INT.fieldOf("color").forGetter(d -> d.color),
		Codec.BOOL.fieldOf("complete").forGetter(d -> d.complete), Codec.INT.fieldOf("copies").forGetter(d -> d.copies),
		Cell.CODEC.listOf().fieldOf("hexgrid").forGetter(ResearchNoteData::cells)).apply(i, ResearchNoteData::new));
	public static final StreamCodec<ByteBuf, ResearchNoteData> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, d -> d.key,
		ByteBufCodecs.INT, d -> d.color, ByteBufCodecs.BOOL, d -> d.complete, ByteBufCodecs.VAR_INT, d -> d.copies,
		Cell.STREAM_CODEC.apply(ByteBufCodecs.list()), ResearchNoteData::cells, ResearchNoteData::new);

	public String key;
	public int color;
	public boolean complete;
	public int copies;
	public final Map<String, HexUtils.Hex> hexes = new LinkedHashMap<>();
	public final Map<String, HexEntry> hexEntries = new LinkedHashMap<>();

	public ResearchNoteData() {
	}

	private ResearchNoteData(String key, int color, boolean complete, int copies, List<Cell> cells) {
		this.key = key;
		this.color = color;
		this.complete = complete;
		this.copies = copies;
		for (Cell c : cells) {
			HexUtils.Hex h = new HexUtils.Hex(c.q(), c.r());
			this.hexes.put(h.toString(), h);
			this.hexEntries.put(h.toString(), new HexEntry(c.aspect().orElse(null), c.type()));
		}
	}

	public List<Cell> cells() {
		List<Cell> out = new ArrayList<>();
		for (HexUtils.Hex h : this.hexes.values()) {
			HexEntry e = this.hexEntries.get(h.toString());
			out.add(new Cell(h.q(), h.r(), e.type(), Optional.ofNullable(e.aspect())));
		}
		return out;
	}

	public ResearchNoteData copy() {
		return new ResearchNoteData(this.key, this.color, this.complete, this.copies, this.cells());
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof ResearchNoteData d && d.key.equals(this.key) && d.color == this.color && d.complete == this.complete && d.copies == this.copies
			&& d.cells().equals(this.cells());
	}

	@Override
	public int hashCode() {
		return this.key.hashCode() * 31 + this.cells().hashCode();
	}
}
