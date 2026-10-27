package dev.neothaum.entity.champion;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

/**
 * 1:1 Port of TC4's 13 Champion Modifiers.
 */
public enum ChampionModifier {
	BOLD(0, "bold", ChatFormatting.BLUE),
	SPINED(1, "spine", ChatFormatting.YELLOW),
	ARMORED(2, "armor", ChatFormatting.GRAY),
	MIGHTY(3, "mighty", ChatFormatting.RED),
	GRIM(4, "grim", ChatFormatting.DARK_GRAY),
	WARDED(5, "warded", ChatFormatting.LIGHT_PURPLE),
	WARP(6, "warp", ChatFormatting.DARK_PURPLE),
	UNDYING(7, "undying", ChatFormatting.GREEN),
	FIERY(8, "fiery", ChatFormatting.GOLD),
	SICKLY(9, "sickly", ChatFormatting.DARK_GREEN),
	VENOMOUS(10, "venomous", ChatFormatting.DARK_GREEN),
	VAMPIRIC(11, "vampiric", ChatFormatting.DARK_RED),
	INFESTED(12, "infested", ChatFormatting.WHITE);

	private final int id;
	private final String key;
	private final ChatFormatting color;

	ChampionModifier(int id, String key, ChatFormatting color) {
		this.id = id;
		this.key = key;
		this.color = color;
	}

	public int getId() {
		return this.id;
	}

	public String getKey() {
		return this.key;
	}

	public ChatFormatting getColor() {
		return this.color;
	}

	public Component getFormattedPrefix() {
		return Component.translatable("champion.mod." + this.key).withStyle(this.color);
	}

	public static ChampionModifier byId(int id) {
		for (ChampionModifier mod : values()) {
			if (mod.id == id) {
				return mod;
			}
		}
		return BOLD;
	}

	public static ChampionModifier fromEntity(LivingEntity entity) {
		for (ChampionModifier mod : values()) {
			if (entity.entityTags().contains(ChampionEvents.TAG_MOD_PREFIX + mod.key)) {
				return mod;
			}
		}
		return null;
	}
}
