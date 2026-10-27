package dev.neothaum.research;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Port of ResearchCategoryList. */
public class ResearchCategory {
	public final String key;
	public final Identifier icon;
	public final Identifier background;
	public final Map<String, ResearchItem> research = new LinkedHashMap<>();
	public int minDisplayColumn;
	public int minDisplayRow;
	public int maxDisplayColumn;
	public int maxDisplayRow;

	public ResearchCategory(String key, Identifier icon, Identifier background) {
		this.key = key;
		this.icon = icon;
		this.background = background;
	}

	public Component getName() {
		return Component.translatable("tc.research_category." + this.key);
	}
}
