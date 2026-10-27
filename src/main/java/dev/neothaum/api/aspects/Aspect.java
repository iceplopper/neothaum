package dev.neothaum.api.aspects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/** Port of thaumcraft.api.aspects.Aspect. Tags, colours, components and blend modes are identical to Thaumcraft 4. */
public class Aspect {
	public static final LinkedHashMap<String, Aspect> aspects = new LinkedHashMap<>();

	public static final Aspect AIR = new Aspect("aer", 0xFFFF7E, "e", 1);
	public static final Aspect EARTH = new Aspect("terra", 0x56C000, "2", 1);
	public static final Aspect FIRE = new Aspect("ignis", 0xFF5A01, "c", 1);
	public static final Aspect WATER = new Aspect("aqua", 0x3CD4FC, "3", 1);
	public static final Aspect ORDER = new Aspect("ordo", 0xD5D4EC, "7", 1);
	public static final Aspect ENTROPY = new Aspect("perditio", 0x404040, "8", 771);
	public static final Aspect VOID = new Aspect("vacuos", 0x888888, new Aspect[]{AIR, ENTROPY}, 771);
	public static final Aspect LIGHT = new Aspect("lux", 0xFFF663, new Aspect[]{AIR, FIRE});
	public static final Aspect WEATHER = new Aspect("tempestas", 0xFFFFFF, new Aspect[]{AIR, WATER});
	public static final Aspect MOTION = new Aspect("motus", 0xCDCCF4, new Aspect[]{AIR, ORDER});
	public static final Aspect COLD = new Aspect("gelum", 0xE1FFFF, new Aspect[]{FIRE, ENTROPY});
	public static final Aspect CRYSTAL = new Aspect("vitreus", 0x80FFFF, new Aspect[]{EARTH, ORDER});
	public static final Aspect LIFE = new Aspect("victus", 0xDE0005, new Aspect[]{WATER, EARTH});
	public static final Aspect POISON = new Aspect("venenum", 0x89F000, new Aspect[]{WATER, ENTROPY});
	public static final Aspect ENERGY = new Aspect("potentia", 0xC0FFFF, new Aspect[]{ORDER, FIRE});
	public static final Aspect EXCHANGE = new Aspect("permutatio", 0x578357, new Aspect[]{ENTROPY, ORDER});
	public static final Aspect METAL = new Aspect("metallum", 0xB5B5CD, new Aspect[]{EARTH, CRYSTAL});
	public static final Aspect DEATH = new Aspect("mortuus", 0x887788, new Aspect[]{LIFE, ENTROPY});
	public static final Aspect FLIGHT = new Aspect("volatus", 0xE7E7D7, new Aspect[]{AIR, MOTION});
	public static final Aspect DARKNESS = new Aspect("tenebrae", 0x222222, new Aspect[]{VOID, LIGHT});
	public static final Aspect SOUL = new Aspect("spiritus", 0xEBEBFB, new Aspect[]{LIFE, DEATH});
	public static final Aspect HEAL = new Aspect("sano", 0xFF2F34, new Aspect[]{LIFE, ORDER});
	public static final Aspect TRAVEL = new Aspect("iter", 0xE0585B, new Aspect[]{MOTION, EARTH});
	public static final Aspect ELDRITCH = new Aspect("alienis", 0x805080, new Aspect[]{VOID, DARKNESS});
	public static final Aspect MAGIC = new Aspect("praecantatio", 0x9700C0, new Aspect[]{VOID, ENERGY});
	public static final Aspect AURA = new Aspect("auram", 0xFFC0FF, new Aspect[]{MAGIC, AIR});
	public static final Aspect TAINT = new Aspect("vitium", 0x800080, new Aspect[]{MAGIC, ENTROPY});
	public static final Aspect SLIME = new Aspect("limus", 0x01F800, new Aspect[]{LIFE, WATER});
	public static final Aspect PLANT = new Aspect("herba", 0x01AC00, new Aspect[]{LIFE, EARTH});
	public static final Aspect TREE = new Aspect("arbor", 0x876531, new Aspect[]{AIR, PLANT});
	public static final Aspect BEAST = new Aspect("bestia", 0x9F6409, new Aspect[]{MOTION, LIFE});
	public static final Aspect FLESH = new Aspect("corpus", 0xEE478D, new Aspect[]{DEATH, BEAST});
	public static final Aspect UNDEAD = new Aspect("exanimis", 0x3A4000, new Aspect[]{MOTION, DEATH});
	public static final Aspect MIND = new Aspect("cognitio", 0xFFC2B3, new Aspect[]{FIRE, SOUL});
	public static final Aspect SENSES = new Aspect("sensus", 0x0FD9FF, new Aspect[]{AIR, SOUL});
	public static final Aspect MAN = new Aspect("humanus", 0xFFD7C0, new Aspect[]{BEAST, MIND});
	public static final Aspect CROP = new Aspect("messis", 0xE1B371, new Aspect[]{PLANT, MAN});
	public static final Aspect MINE = new Aspect("perfodio", 0xDCD2D8, new Aspect[]{MAN, EARTH});
	public static final Aspect TOOL = new Aspect("instrumentum", 0x4040EE, new Aspect[]{MAN, ORDER});
	public static final Aspect HARVEST = new Aspect("meto", 0xEEAD82, new Aspect[]{CROP, TOOL});
	public static final Aspect WEAPON = new Aspect("telum", 0xC05050, new Aspect[]{TOOL, FIRE});
	public static final Aspect ARMOR = new Aspect("tutamen", 0x00C0C0, new Aspect[]{TOOL, EARTH});
	public static final Aspect HUNGER = new Aspect("fames", 0x9A0305, new Aspect[]{LIFE, VOID});
	public static final Aspect GREED = new Aspect("lucrum", 0xE6BE44, new Aspect[]{MAN, HUNGER});
	public static final Aspect CRAFT = new Aspect("fabrico", 0x809D80, new Aspect[]{MAN, TOOL});
	public static final Aspect CLOTH = new Aspect("pannus", 0xEAEAC2, new Aspect[]{TOOL, BEAST});
	public static final Aspect MECHANISM = new Aspect("machina", 0x8080A0, new Aspect[]{MOTION, TOOL});
	public static final Aspect TRAP = new Aspect("vinculum", 0x9A8080, new Aspect[]{MOTION, ENTROPY});

	public static final Codec<Aspect> CODEC = Codec.STRING.comapFlatMap(
		tag -> {
			Aspect a = aspects.get(tag);
			return a != null ? DataResult.success(a) : DataResult.error(() -> "Unknown aspect " + tag);
		},
		Aspect::getTag
	);
	public static final StreamCodec<ByteBuf, Aspect> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(Aspect::getAspect, Aspect::getTag);

	private final String tag;
	private final Aspect[] components;
	private final int color;
	private String chatcolor;
	private final Identifier image;
	private final int blend;

	public Aspect(String tag, int color, Aspect[] components, Identifier image, int blend) {
		if (aspects.containsKey(tag)) {
			throw new IllegalArgumentException(tag + " already registered!");
		}
		this.tag = tag;
		this.components = components;
		this.color = color;
		this.image = image;
		this.blend = blend;
		aspects.put(tag, this);
	}

	public Aspect(String tag, int color, Aspect[] components) {
		this(tag, color, components, Identifier.fromNamespaceAndPath("neothaum", "textures/aspects/" + tag.toLowerCase() + ".png"), 1);
	}

	public Aspect(String tag, int color, Aspect[] components, int blend) {
		this(tag, color, components, Identifier.fromNamespaceAndPath("neothaum", "textures/aspects/" + tag.toLowerCase() + ".png"), blend);
	}

	public Aspect(String tag, int color, String chatcolor, int blend) {
		this(tag, color, (Aspect[]) null, blend);
		this.chatcolor = chatcolor;
	}

	public int getColor() {
		return this.color;
	}

	/** Display name, e.g. "Aer" — Thaumcraft capitalises the Latin tag. */
	public String getName() {
		return Character.toUpperCase(this.tag.charAt(0)) + this.tag.substring(1).toLowerCase();
	}

	public Component getDisplayName() {
		return Component.literal(getName());
	}

	public Component getLocalizedDescription() {
		return Component.translatable("tc.aspect." + this.tag);
	}

	public String getTag() {
		return this.tag;
	}

	public Aspect[] getComponents() {
		return this.components;
	}

	public Identifier getImage() {
		return this.image;
	}

	public static Aspect getAspect(String tag) {
		return aspects.get(tag);
	}

	public int getBlend() {
		return this.blend;
	}

	public boolean isPrimal() {
		return this.components == null || this.components.length != 2;
	}

	public static List<Aspect> getPrimalAspects() {
		List<Aspect> primals = new ArrayList<>();
		for (Aspect aspect : aspects.values()) {
			if (aspect.isPrimal()) {
				primals.add(aspect);
			}
		}
		return primals;
	}

	public static List<Aspect> getCompoundAspects() {
		List<Aspect> compounds = new ArrayList<>();
		for (Aspect aspect : aspects.values()) {
			if (!aspect.isPrimal()) {
				compounds.add(aspect);
			}
		}
		return compounds;
	}

	public static Aspect getCombination(Aspect a, Aspect b) {
		if (a == null || b == null) {
			return null;
		}
		for (Aspect candidate : aspects.values()) {
			Aspect[] comps = candidate.getComponents();
			if (comps != null && comps.length == 2) {
				if ((comps[0] == a && comps[1] == b) || (comps[0] == b && comps[1] == a)) {
					return candidate;
				}
			}
		}
		return null;
	}

	public String getChatcolor() {
		return this.chatcolor;
	}

	@Override
	public String toString() {
		return this.tag;
	}

	/** Forces class init so every aspect is registered. */
	public static void bootstrap() {
	}
}
