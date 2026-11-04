package dev.neothaum.addon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.neothaum.NeoThaum;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.network.TVNetwork;
import dev.neothaum.research.ResearchManager;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * NeoThaum Addition: "Shortcuts of the Impatient Mind", a hidden research that hints at what to scan when a scan fails with "To understand
 * this you need to study X". It comes to light after a player's scans fail that way on 4 different things, or 8 times in all, with the
 * usual new-research message and sound; it is then bought with aspect points (16 of each primal) and gives 1 warp. Once known, each such
 * failure also names the missing aspect, with its symbol, and gives a hint at where it is found, never naming the thing itself.
 */
public final class HintResearch {
	public static final String KEY = "HINTRESEARCH";
	/** Different blocked things, or blocked scans in all, that bring the research to light. */
	public static final int DIFFERENT = 4;
	public static final int TOTAL = 8;

	/** What a player's blocked scans have been: the scan keys of different things, and how many in all. */
	public record Blocked(List<String> keys, int total) {
		public static final Codec<Blocked> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.listOf().fieldOf("keys").forGetter(Blocked::keys), Codec.INT.fieldOf("total").forGetter(Blocked::total)).apply(i, Blocked::new));

		static Blocked empty() {
			return new Blocked(List.of(), 0);
		}
	}

	public static final AttachmentType<Blocked> BLOCKED = AttachmentRegistry.<Blocked>builder()
		.persistent(Blocked.CODEC)
		.copyOnDeath()
		.initializer(Blocked::empty)
		.buildAndRegister(NeoThaum.id("blocked_scans"));

	public static boolean enabled() {
		return Additions.enabled("hint_research");
	}

	/** Whether the player has bought the research (so their blocked scans get hints). */
	public static boolean known(Player player) {
		return enabled() && ResearchManager.isResearchComplete(player, KEY);
	}

	/** Whether the research has come to light for the player (or is already theirs). */
	public static boolean found(Player player) {
		return ResearchManager.isResearchComplete(player, "@" + KEY) || ResearchManager.isResearchComplete(player, KEY);
	}

	/** Server: a scan of the thing with this scan key failed for want of an aspect. Counts it, and brings the research to light in time. */
	public static void blocked(ServerPlayer player, String scanKey) {
		if (!enabled() || found(player)) {
			return;
		}
		Blocked b = player.getAttachedOrCreate(BLOCKED);
		List<String> keys = new ArrayList<>(b.keys());
		if (!keys.contains(scanKey)) {
			keys.add(scanKey);
		}
		b = new Blocked(keys, b.total() + 1);
		player.setAttached(BLOCKED, b);
		if (b.keys().size() >= DIFFERENT || b.total() >= TOTAL) {
			if (ResearchManager.completeResearch(player, "@" + KEY)) {
				TVNetwork.sendResearchComplete(player, "@" + KEY);
			}
		}
	}

	/** The hint line for a missing aspect: its name, then a hint at where it is found (a general one for aspects with none written). */
	public static Component hint(Aspect aspect) {
		String key = "neothaum.hint." + aspect.getTag();
		Component where = Language.getInstance().has(key) ? Component.translatable(key) : Component.translatable("neothaum.hint.generic");
		return Component.translatable("neothaum.hint.line", aspect.getName(), where);
	}

	public static void init() {
		BLOCKED.getClass();
	}

	private HintResearch() {
	}
}
