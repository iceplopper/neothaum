package dev.neothaum.addon.totems;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.neothaum.NeoThaum;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Every Traveller's Totem and Home Totem in the world: where it stands, its name and its kind. Kept with the overworld. */
public final class TotemData extends SavedData {
	public record Totem(GlobalPos pos, String name, boolean home) {
		static final Codec<Totem> CODEC = RecordCodecBuilder.create(i -> i.group(GlobalPos.CODEC.fieldOf("pos").forGetter(Totem::pos),
			Codec.STRING.fieldOf("name").forGetter(Totem::name), Codec.BOOL.fieldOf("home").forGetter(Totem::home)).apply(i, Totem::new));
	}

	static final Codec<TotemData> CODEC = RecordCodecBuilder.create(i -> i.group(Totem.CODEC.listOf().fieldOf("totems").forGetter(TotemData::list),
		Codec.LONG.listOf().optionalFieldOf("deferredVillages", List.of()).forGetter(d -> new ArrayList<>(d.deferred)))
		.apply(i, TotemData::new));
	static final SavedDataType<TotemData> TYPE = new SavedDataType<>(NeoThaum.id("travellers_totems"), TotemData::new, CODEC, null);

	private final Map<GlobalPos, Totem> totems = new LinkedHashMap<>();
	/** Villages (start chunks, x in the high half) that had no room for a totem while generating, to have one once built. */
	private final java.util.Set<Long> deferred = new java.util.LinkedHashSet<>();

	TotemData() {
	}

	private TotemData(List<Totem> list, List<Long> deferred) {
		list.forEach(t -> this.totems.put(t.pos(), t));
		this.deferred.addAll(deferred);
	}

	java.util.Set<Long> deferred() {
		return this.deferred;
	}

	void defer(long village) {
		if (this.deferred.add(village)) {
			this.setDirty();
		}
	}

	void undefer(long village) {
		if (this.deferred.remove(village)) {
			this.setDirty();
		}
	}

	private List<Totem> list() {
		return new ArrayList<>(this.totems.values());
	}

	public static TotemData get(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(TYPE);
	}

	public Totem get(GlobalPos pos) {
		return this.totems.get(pos);
	}

	public Collection<Totem> all() {
		return this.totems.values();
	}

	public boolean nameTaken(String name) {
		return this.totems.values().stream().anyMatch(t -> t.name().equals(name));
	}

	public void put(Totem t) {
		this.totems.put(t.pos(), t);
		this.setDirty();
	}

	public void remove(GlobalPos pos) {
		if (this.totems.remove(pos) != null) {
			this.setDirty();
		}
	}
}
