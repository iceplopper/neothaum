package dev.neothaum.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import io.netty.buffer.ByteBuf;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Port of thaumcraft.common.lib.research.PlayerKnowledge for one player: completed research, discovered aspects
 * (with their research-point pools), scanned things and warp. Treated as immutable; mutate a {@link #copy()}.
 */
public final class PlayerKnowledge {
	public static final Codec<PlayerKnowledge> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.STRING.listOf().fieldOf("research").forGetter(k -> List.copyOf(k.research)),
		AspectList.CODEC.fieldOf("aspects").forGetter(k -> k.aspects),
		Codec.STRING.listOf().fieldOf("objects").forGetter(k -> List.copyOf(k.objects)),
		Codec.STRING.listOf().fieldOf("entities").forGetter(k -> List.copyOf(k.entities)),
		Codec.STRING.listOf().fieldOf("phenomena").forGetter(k -> List.copyOf(k.phenomena)),
		Codec.INT.fieldOf("warp").forGetter(k -> k.warp), Codec.INT.fieldOf("warpSticky").forGetter(k -> k.warpSticky),
		Codec.INT.fieldOf("warpTemp").forGetter(k -> k.warpTemp), Codec.INT.fieldOf("warpCounter").forGetter(k -> k.warpCounter)
	).apply(i, PlayerKnowledge::new));
	public static final StreamCodec<ByteBuf, PlayerKnowledge> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), k -> List.copyOf(k.research),
		AspectList.STREAM_CODEC, k -> k.aspects,
		ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), k -> List.copyOf(k.objects),
		ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), k -> List.copyOf(k.entities),
		ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), k -> List.copyOf(k.phenomena),
		ByteBufCodecs.VAR_INT, k -> k.warp, ByteBufCodecs.VAR_INT, k -> k.warpSticky, ByteBufCodecs.VAR_INT, k -> k.warpTemp,
		ByteBufCodecs.VAR_INT, k -> k.warpCounter, PlayerKnowledge::new);

	public final Set<String> research;
	/** Discovered aspects mapped to their current research-point pool. */
	public final AspectList aspects;
	public final Set<String> objects;
	public final Set<String> entities;
	public final Set<String> phenomena;
	public int warp;
	public int warpSticky;
	public int warpTemp;
	public int warpCounter;

	public PlayerKnowledge(List<String> research, AspectList aspects, List<String> objects, List<String> entities, List<String> phenomena, int warp,
		int warpSticky, int warpTemp, int warpCounter) {
		this.research = new LinkedHashSet<>(research);
		this.aspects = aspects.copy();
		this.objects = new LinkedHashSet<>(objects);
		this.entities = new LinkedHashSet<>(entities);
		this.phenomena = new LinkedHashSet<>(phenomena);
		this.warp = warp;
		this.warpSticky = warpSticky;
		this.warpTemp = warpTemp;
		this.warpCounter = warpCounter;
	}

	public static PlayerKnowledge empty() {
		return new PlayerKnowledge(List.of(), new AspectList(), List.of(), List.of(), List.of(), 0, 0, 0, 0);
	}

	public PlayerKnowledge copy() {
		return new PlayerKnowledge(List.copyOf(this.research), this.aspects, List.copyOf(this.objects), List.copyOf(this.entities),
			List.copyOf(this.phenomena), this.warp, this.warpSticky, this.warpTemp, this.warpCounter);
	}

	public boolean hasDiscoveredAspect(Aspect aspect) {
		return this.aspects.aspects.containsKey(aspect);
	}

	public boolean hasDiscoveredParentAspects(Aspect aspect) {
		Aspect[] c = aspect.getComponents();
		if (c == null) {
			return true;
		}
		for (Aspect a : c) {
			if (!hasDiscoveredAspect(a)) {
				return false;
			}
		}
		return true;
	}

	public void addDiscoveredPrimalAspects() {
		for (Aspect a : new Aspect[]{Aspect.AIR, Aspect.FIRE, Aspect.EARTH, Aspect.WATER, Aspect.ORDER, Aspect.ENTROPY}) {
			if (!this.aspects.aspects.containsKey(a)) {
				this.aspects.add(a, 0);
			}
		}
	}

	public boolean addDiscoveredAspect(Aspect aspect) {
		if (!hasDiscoveredAspect(aspect)) {
			this.aspects.add(aspect, 0);
			return true;
		}
		return false;
	}

	public int getAspectPool(Aspect aspect) {
		return this.aspects.getAmount(aspect);
	}

	public boolean addAspectPool(Aspect aspect, int amount) {
		if (aspect == null) {
			return false;
		}
		this.aspects.aspects.put(aspect, Math.max(0, getAspectPool(aspect) + amount));
		return true;
	}

	public void setAspectPool(Aspect aspect, int amount) {
		this.aspects.aspects.put(aspect, amount);
	}

	public int getWarpTotal() {
		return this.warp + this.warpSticky + this.warpTemp;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof PlayerKnowledge k && k.research.equals(this.research) && k.aspects.equals(this.aspects) && k.objects.equals(this.objects)
			&& k.entities.equals(this.entities) && k.phenomena.equals(this.phenomena) && k.warp == this.warp && k.warpSticky == this.warpSticky
			&& k.warpTemp == this.warpTemp && k.warpCounter == this.warpCounter;
	}

	@Override
	public int hashCode() {
		return this.research.hashCode() ^ this.aspects.hashCode();
	}
}
