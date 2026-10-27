package dev.thaumvibe.items.eldritch;

import dev.thaumvibe.registry.TVSounds;
import dev.thaumvibe.research.ResearchManager;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/**
 * Port of TC4 ItemEldritchObject metadata 1 (Crimson Rites).
 * Unlocks the Crimson Cult research branch upon reading.
 */
public class CrimsonRitesItem extends Item {
	public CrimsonRitesItem(Item.Properties props) {
		super(props.stacksTo(1).rarity(Rarity.UNCOMMON));
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!level.isClientSide() && !ResearchManager.isResearchComplete(player, "CRIMSON")) {
			ResearchManager.completeResearch(player, "CRIMSON");
			level.playSound(null, player.getX(), player.getY(), player.getZ(), TVSounds.LEARN, SoundSource.PLAYERS, 0.75F, 1.0F);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		builder.accept(Component.translatable("item.ItemEldritchObject.text.2").withStyle(ChatFormatting.DARK_PURPLE));
		builder.accept(Component.translatable("item.ItemEldritchObject.text.3").withStyle(ChatFormatting.DARK_BLUE));
	}
}
