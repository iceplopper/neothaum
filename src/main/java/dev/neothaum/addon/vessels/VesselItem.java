package dev.neothaum.addon.vessels;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

/** A Vessel of Binding as an item: empty, or holding what it bound (its kind, name and health shown). Placed, it sets down as a block. */
public class VesselItem extends BlockItem {
	public final Vessels.Tier tier;

	public VesselItem(Block block, Vessels.Tier tier, Properties props) {
		super(block, props);
		this.tier = tier;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
		List<CompoundTag> held = stack.getOrDefault(Vessels.BOUND, List.of());
		if (held.isEmpty()) {
			lines.accept(Component.translatable("neothaum.vessel.tip." + this.tier.name().toLowerCase(java.util.Locale.ROOT))
				.withStyle(ChatFormatting.GRAY));
			lines.accept(Component.translatable("neothaum.vessel.tip.how").withStyle(ChatFormatting.DARK_GRAY));
			return;
		}
		CompoundTag first = held.get(0);
		Component kind = Vessels.kindOf(first);
		lines.accept((this.tier.holds > 1 ? Component.translatable("neothaum.vessel.holds_many", held.size(), this.tier.holds, kind)
			: Component.translatable("neothaum.vessel.holds", kind)).withStyle(ChatFormatting.DARK_PURPLE));
		for (CompoundTag t : held.subList(0, Math.min(held.size(), 4))) {
			var tag = t.get("CustomName");
			String name = tag == null ? null : net.minecraft.network.chat.ComponentSerialization.CODEC
				.parse(net.minecraft.nbt.NbtOps.INSTANCE, tag).result().map(Component::getString).orElse(null);
			float health = t.getFloatOr("Health", -1);
			String line = (name != null ? name + ", " : "") + (health >= 0 ? Math.round(health) + " health" : "");
			if (!line.isEmpty()) {
				lines.accept(Component.literal("  " + line).withStyle(ChatFormatting.GRAY));
			}
		}
		lines.accept(Component.translatable("neothaum.vessel.tip.release").withStyle(ChatFormatting.DARK_GRAY));
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return !stack.getOrDefault(Vessels.BOUND, List.of()).isEmpty();
	}
}
