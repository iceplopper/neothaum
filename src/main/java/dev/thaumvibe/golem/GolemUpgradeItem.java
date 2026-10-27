package dev.thaumvibe.golem;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Port of ItemGolemUpgrade:
 * Elemental upgrades installed into golems to augment performance.
 */
public class GolemUpgradeItem extends Item {
	private final GolemUpgradeType upgradeType;

	public GolemUpgradeItem(GolemUpgradeType type, Properties props) {
		super(props);
		this.upgradeType = type;
	}

	public GolemUpgradeType getUpgradeType() {
		return this.upgradeType;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		builder.accept(Component.translatable("item.ItemGolemUpgrade." + this.upgradeType.id + ".desc").withStyle(ChatFormatting.GRAY));
	}
}
