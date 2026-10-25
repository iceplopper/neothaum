package dev.thaumvibe.api.wands;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.registry.TVComponents;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * Port of thaumcraft.api.wands.ItemFocusBasic. A focus sits on a wand; the wand passes its right clicks, use ticks and releases here. Upgrades
 * live in the {@link TVComponents#FOCUS_UPGRADES} component, five slots filled in rank order by the focal manipulator.
 */
public abstract class FocusItem extends Item {
	private static final DecimalFormat FORMAT = new DecimalFormat("#####.##");

	public enum Animation { WAVE, CHARGE }

	protected FocusItem(Item.Properties props) {
		super(props.stacksTo(1).rarity(Rarity.RARE));
	}

	// ---------------------------------------------------------------- appearance

	/** RGB of the orb on the wand's tip. */
	public abstract int getFocusColor(ItemStack focus);

	/** The flat ornament crossed over the orb (the hellbat's wings, the trade focus's scales), or null. */
	public Identifier getOrnament(ItemStack focus) {
		return null;
	}

	/** The animated inner cube some foci show through a fainter orb, or null. */
	public Identifier getDepthLayer(ItemStack focus) {
		return null;
	}

	public Animation getAnimation(ItemStack focus) {
		return Animation.WAVE;
	}

	// ---------------------------------------------------------------- costs and timing

	/** Centivis per cast (or per tick when {@link #isVisCostPerTick}). */
	public abstract AspectList getVisCost(ItemStack focus);

	public boolean isVisCostPerTick(ItemStack focus) {
		return false;
	}

	/** Milliseconds before the wand may cast again; -1 clears the cooldown so a held focus can tick. */
	public int getActivationCooldown(ItemStack focus) {
		return 0;
	}

	public int getMaxAreaSize(ItemStack focus) {
		return 1;
	}

	/** Groups foci of a kind together in the radial menu; upgrades are appended so differently upgraded copies stay distinct. */
	public String getSortingHelper(ItemStack focus) {
		StringBuilder out = new StringBuilder();
		for (int id : getAppliedUpgrades(focus)) {
			out.append(id);
		}
		return out.toString();
	}

	// ---------------------------------------------------------------- casting

	/** Right click with the focused wand; return true when the click was used. */
	public boolean onFocusRightClick(ItemStack wand, Level level, Player player, HitResult hit) {
		return false;
	}

	public void onUsingFocusTick(ItemStack wand, Player player, int count) {
	}

	public void onPlayerStoppedUsingFocus(ItemStack wand, Level level, Player player, int count) {
	}

	/** Left click (arm swing) with the focused wand; only the equal trade focus uses it. */
	public void onEntitySwing(Player player, ItemStack wand) {
	}

	// ---------------------------------------------------------------- upgrades

	public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
		return null;
	}

	public boolean canApplyUpgrade(ItemStack focus, Player player, FocusUpgradeType type, int rank) {
		return true;
	}

	public static int[] getAppliedUpgrades(ItemStack focus) {
		int[] out = {-1, -1, -1, -1, -1};
		List<Integer> l = focus.get(TVComponents.FOCUS_UPGRADES);
		if (l != null) {
			for (int j = 0; j < l.size() && j < 5; j++) {
				out[j] = l.get(j);
			}
		}
		return out;
	}

	public boolean applyUpgrade(ItemStack focus, FocusUpgradeType type, int rank) {
		int[] upgrades = getAppliedUpgrades(focus);
		if (rank < 1 || rank > 5 || upgrades[rank - 1] != -1) {
			return false;
		}
		upgrades[rank - 1] = type.id;
		List<Integer> l = new ArrayList<>();
		for (int u : upgrades) {
			l.add(u);
		}
		focus.set(TVComponents.FOCUS_UPGRADES, List.copyOf(l));
		return true;
	}

	public boolean isUpgradedWith(ItemStack focus, FocusUpgradeType type) {
		return getUpgradeLevel(focus, type) > 0;
	}

	public int getUpgradeLevel(ItemStack focus, FocusUpgradeType type) {
		int level = 0;
		for (int id : getAppliedUpgrades(focus)) {
			if (id == type.id) {
				level++;
			}
		}
		return level;
	}

	// ---------------------------------------------------------------- tooltip

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		AspectList al = this.getVisCost(stack);
		if (al != null && al.size() > 0) {
			builder.accept(Component.translatable(this.isVisCostPerTick(stack) ? "item.Focus.cost2" : "item.Focus.cost1"));
			for (Aspect aspect : al.getAspectsSorted()) {
				builder.accept(Component.literal(" §" + aspect.getChatcolor() + aspect.getName() + "§r x " + FORMAT.format(al.getAmount(aspect) / 100.0F)));
			}
		}
		addFocusInformation(stack, builder);
	}

	public void addFocusInformation(ItemStack focus, Consumer<Component> builder) {
		Map<Integer, Integer> map = new LinkedHashMap<>();
		for (int id : getAppliedUpgrades(focus)) {
			if (id >= 0) {
				map.merge(id, 1, Integer::sum);
			}
		}
		for (var e : map.entrySet()) {
			FocusUpgradeType t = FocusUpgradeType.types[e.getKey()];
			if (t == null) {
				continue;
			}
			Component name = t.getLocalizedName().copy();
			if (e.getValue() > 1) {
				name = name.copy().append(" ").append(Component.translatable("enchantment.level." + e.getValue()));
			}
			builder.accept(name.copy().withStyle(ChatFormatting.DARK_PURPLE));
		}
	}
}
