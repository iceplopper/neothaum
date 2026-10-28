package dev.neothaum.entity.champion;

import net.minecraft.network.chat.Component;

/** EntityThaumcraftBoss: always spawns a champion, without the usual health and damage buffs, and names itself after its modifier. */
public interface ChampionBoss {
	/** EntityThaumcraftBoss.generateName. */
	void nameAsChampion(Component modifier);
}
