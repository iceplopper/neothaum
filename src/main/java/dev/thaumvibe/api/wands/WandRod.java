package dev.thaumvibe.api.wands;

import java.util.LinkedHashMap;
import java.util.function.Supplier;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Port of thaumcraft.api.wands.WandRod (and StaffRod via {@link #isStaff()}). */
public class WandRod {
	public static final LinkedHashMap<String, WandRod> rods = new LinkedHashMap<>();

	public interface OnUpdate {
		void onUpdate(ItemStack wand, Player player);
	}

	private final String tag;
	private final int craftCost;
	private final int capacity;
	private final Identifier texture;
	private final Supplier<ItemStack> item;
	private final OnUpdate onUpdate;
	private final boolean staff;
	private boolean glowing;
	private boolean runes;

	public WandRod(String tag, int capacity, Supplier<ItemStack> item, int craftCost, OnUpdate onUpdate, boolean staff) {
		this.tag = staff ? tag + "_staff" : tag;
		this.capacity = capacity;
		this.texture = Identifier.fromNamespaceAndPath("thaumvibe", "textures/models/wand_rod_" + tag + ".png");
		this.item = item;
		this.craftCost = craftCost;
		this.onUpdate = onUpdate;
		this.staff = staff;
		rods.put(this.tag, this);
	}

	public WandRod(String tag, int capacity, Supplier<ItemStack> item, int craftCost) {
		this(tag, capacity, item, craftCost, null, false);
	}

	public String getTag() {
		return this.tag;
	}

	/** Texture name without the staff suffix. */
	public String getTextureTag() {
		return this.staff ? this.tag.substring(0, this.tag.length() - 6) : this.tag;
	}

	public int getCapacity() {
		return this.capacity;
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

	public OnUpdate getOnUpdate() {
		return this.onUpdate;
	}

	public boolean isStaff() {
		return this.staff;
	}

	public boolean isGlowing() {
		return this.glowing;
	}

	public WandRod setGlowing(boolean glowing) {
		this.glowing = glowing;
		return this;
	}

	/** StaffRod.hasRunes: the primal staff's spinning runes. */
	public boolean hasRunes() {
		return this.runes;
	}

	public WandRod setRunes(boolean runes) {
		this.runes = runes;
		return this;
	}
}
