package dev.thaumvibe.api.aspects;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Port of thaumcraft.api.aspects.AspectList with identical add/merge/reduce/sort semantics. */
public class AspectList {
	public static final Codec<AspectList> CODEC = Codec.unboundedMap(Aspect.CODEC, Codec.INT)
		.xmap(m -> {
			AspectList l = new AspectList();
			m.forEach(l::add);
			return l;
		}, l -> l.aspects);
	public static final StreamCodec<ByteBuf, AspectList> STREAM_CODEC = ByteBufCodecs.map(LinkedHashMap::new, Aspect.STREAM_CODEC, ByteBufCodecs.VAR_INT)
		.map(m -> {
			AspectList l = new AspectList();
			m.forEach(l::add);
			return l;
		}, l -> l.aspects);

	public final LinkedHashMap<Aspect, Integer> aspects = new LinkedHashMap<>();

	public AspectList() {
	}

	public AspectList copy() {
		AspectList out = new AspectList();
		out.aspects.putAll(this.aspects);
		return out;
	}

	public int size() {
		return this.aspects.size();
	}

	public int visSize() {
		int q = 0;
		for (int v : this.aspects.values()) {
			q += v;
		}
		return q;
	}

	public Aspect[] getAspects() {
		return this.aspects.keySet().toArray(new Aspect[0]);
	}

	public Aspect[] getPrimalAspects() {
		return this.aspects.keySet().stream().filter(Aspect::isPrimal).toArray(Aspect[]::new);
	}

	public Aspect[] getAspectsSorted() {
		Aspect[] out = getAspects();
		Arrays.sort(out, (a, b) -> a.getTag().compareTo(b.getTag()));
		return out;
	}

	public Aspect[] getAspectsSortedAmount() {
		Aspect[] out = getAspects();
		// stable sort, highest first — matches Thaumcraft's bubble sort
		Arrays.sort(out, (a, b) -> Integer.compare(getAmount(b), getAmount(a)));
		return out;
	}

	public int getAmount(Aspect key) {
		Integer v = this.aspects.get(key);
		return v == null ? 0 : v;
	}

	public boolean reduce(Aspect key, int amount) {
		if (this.getAmount(key) >= amount) {
			int am = this.getAmount(key) - amount;
			this.aspects.put(key, am);
			return true;
		}
		return false;
	}

	public AspectList remove(Aspect key, int amount) {
		int am = this.getAmount(key) - amount;
		if (am <= 0) {
			this.aspects.remove(key);
		} else {
			this.aspects.put(key, am);
		}
		return this;
	}

	public AspectList remove(Aspect key) {
		this.aspects.remove(key);
		return this;
	}

	public AspectList add(Aspect aspect, int amount) {
		if (this.aspects.containsKey(aspect)) {
			amount += this.aspects.get(aspect);
		}
		this.aspects.put(aspect, amount);
		return this;
	}

	public AspectList merge(Aspect aspect, int amount) {
		if (this.aspects.containsKey(aspect)) {
			int old = this.aspects.get(aspect);
			if (amount < old) {
				amount = old;
			}
		}
		this.aspects.put(aspect, amount);
		return this;
	}

	public AspectList add(AspectList in) {
		for (Map.Entry<Aspect, Integer> e : in.aspects.entrySet()) {
			this.add(e.getKey(), e.getValue());
		}
		return this;
	}

	public AspectList merge(AspectList in) {
		for (Map.Entry<Aspect, Integer> e : in.aspects.entrySet()) {
			this.merge(e.getKey(), e.getValue());
		}
		return this;
	}

	/** Removes zero/negative entries, like ThaumcraftApiHelper.cullTags. */
	public AspectList cull() {
		this.aspects.values().removeIf(v -> v <= 0);
		return this;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof AspectList l && l.aspects.equals(this.aspects);
	}

	@Override
	public int hashCode() {
		return this.aspects.hashCode();
	}

	@Override
	public String toString() {
		return this.aspects.toString();
	}
}
