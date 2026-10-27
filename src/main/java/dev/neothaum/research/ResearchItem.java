package dev.neothaum.research;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/** Port of thaumcraft.api.research.ResearchItem (data comes from research.json converted from ConfigResearch). */
public class ResearchItem {
	public final String key;
	public final String category;
	public final AspectList tags;
	public final int displayColumn;
	public final int displayRow;
	public final int complexity;
	public final ItemStack iconItem;
	public final Identifier iconResource;
	public final List<String> parents;
	public final List<String> parentsHidden;
	public final List<String> siblings;
	public final List<String> entityTriggers;
	public final List<ItemStack> itemTriggers;
	public final List<Aspect> aspectTriggers;
	public final List<ResearchPage> pages;
	public final int warp;
	/** An item tag that must have something in it for this research to exist (TC4 registered silver caps only when some mod had silver). */
	public String requiresTag;
	private final List<String> flags;

	public ResearchItem(String key, String category, AspectList tags, int col, int row, int complexity, ItemStack iconItem, Identifier iconResource,
		List<String> parents, List<String> parentsHidden, List<String> siblings, List<String> entityTriggers, List<ItemStack> itemTriggers,
		List<Aspect> aspectTriggers, List<ResearchPage> pages, List<String> flags, int warp) {
		this.key = key;
		this.category = category;
		this.tags = tags;
		this.displayColumn = col;
		this.displayRow = row;
		this.complexity = Math.max(1, Math.min(3, complexity));
		this.iconItem = iconItem;
		this.iconResource = iconResource;
		this.parents = parents;
		this.parentsHidden = parentsHidden;
		this.siblings = siblings;
		this.entityTriggers = entityTriggers;
		this.itemTriggers = itemTriggers;
		this.aspectTriggers = aspectTriggers;
		this.pages = pages;
		this.flags = flags;
		this.warp = warp;
	}

	private boolean flag(String f) {
		return this.flags.contains(f);
	}

	public boolean isSpecial() {
		return flag("special");
	}

	public boolean isStub() {
		return flag("stub");
	}

	public boolean isLost() {
		return flag("lost");
	}

	public boolean isConcealed() {
		return flag("concealed");
	}

	public boolean isHidden() {
		return flag("hidden");
	}

	public boolean isVirtual() {
		return flag("virtual");
	}

	public boolean isAutoUnlock() {
		return flag("autounlock");
	}

	public boolean isRound() {
		return flag("round");
	}

	public boolean isSecondary() {
		return flag("secondary");
	}

	public int getComplexity() {
		return this.complexity;
	}

	public Component getName() {
		return Component.translatable("tc.research_name." + this.key);
	}

	public Component getText() {
		return Component.translatable("tc.research_text." + this.key);
	}

	public Aspect getResearchPrimaryTag() {
		Aspect aspect = null;
		int highest = 0;
		for (Aspect tag : this.tags.getAspects()) {
			if (this.tags.getAmount(tag) > highest) {
				aspect = tag;
				highest = this.tags.getAmount(tag);
			}
		}
		return aspect;
	}
}
