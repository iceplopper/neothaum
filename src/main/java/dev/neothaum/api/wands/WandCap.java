package dev.neothaum.api.wands;

import dev.neothaum.api.aspects.Aspect;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/** Port of thaumcraft.api.wands.WandCap. */
public class WandCap {
	public static final LinkedHashMap<String, WandCap> caps = new LinkedHashMap<>();

	private final String tag;
	private final int craftCost;
	private final float baseCostModifier;
	private final List<Aspect> specialCostModifierAspects;
	private final float specialCostModifier;
	private final Identifier texture;
	private final Supplier<ItemStack> item;

	public WandCap(String tag, float discount, Supplier<ItemStack> item, int craftCost) {
		this(tag, discount, null, discount, item, craftCost);
	}

	public WandCap(String tag, float discount, List<Aspect> specialAspects, float discountSpecial, Supplier<ItemStack> item, int craftCost) {
		this.tag = tag;
		this.baseCostModifier = discount;
		this.specialCostModifierAspects = specialAspects;
		this.specialCostModifier = discountSpecial;
		this.texture = Identifier.fromNamespaceAndPath("neothaum", "textures/models/wand_cap_" + tag + ".png");
		this.item = item;
		this.craftCost = craftCost;
		caps.put(tag, this);
	}

	public float getBaseCostModifier() {
		return this.baseCostModifier;
	}

	public List<Aspect> getSpecialCostModifierAspects() {
		return this.specialCostModifierAspects;
	}

	public float getSpecialCostModifier() {
		return this.specialCostModifier;
	}

	public String getTag() {
		return this.tag;
	}

	public Identifier getTexture() {
		return this.texture;
	}

	public ItemStack getItem() {
		return this.item.get();
	}

	public int getCraftCost() {
		return this.craftCost;
	}
}
