package dev.thaumvibe.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.thaumvibe.Thaumvibe;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public class ThaumvibeConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("thaumvibe.json");

	public static ConfigData current = new ConfigData();

	public static class ConfigData {
		public boolean taintSpread = true;
		public boolean hardNodes = true;
		public int nodeRarity = 36;
		public float aspectOrbDropChance = 0.25f;
		public boolean allowWarp = true;
		public float wispSpawnRate = 1.0f;
		public boolean championMobs = true;
	}

	public static void load() {
		try {
			if (Files.exists(CONFIG_PATH)) {
				try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
					ConfigData loaded = GSON.fromJson(reader, ConfigData.class);
					if (loaded != null) {
						current = loaded;
					}
				}
			} else {
				save();
			}
		} catch (Exception e) {
			Thaumvibe.LOGGER.error("Failed to load thaumvibe config, using defaults", e);
		}
	}

	public static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(current, writer);
			}
		} catch (Exception e) {
			Thaumvibe.LOGGER.error("Failed to save thaumvibe config", e);
		}
	}
}
