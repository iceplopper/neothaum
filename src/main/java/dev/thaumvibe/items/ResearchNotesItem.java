package dev.thaumvibe.items;

import dev.thaumvibe.network.TVNetwork;
import dev.thaumvibe.registry.TVComponents;
import dev.thaumvibe.registry.TVItems;
import dev.thaumvibe.registry.TVSounds;
import dev.thaumvibe.research.ResearchItem;
import dev.thaumvibe.research.ResearchManager;
import dev.thaumvibe.research.ResearchNoteData;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/** Port of ItemResearchNotes. A note without data is a "discovery" (meta 42) that rolls a hidden research. */
public class ResearchNotesItem extends Item {
	public ResearchNotesItem(Item.Properties props) {
		super(props);
	}

	@Override
	public Component getName(ItemStack stack) {
		ResearchNoteData d = stack.get(TVComponents.RESEARCH_NOTE);
		return d == null ? Component.translatable("item.discovery.name") : Component.translatable("item.researchnotes.name");
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		ResearchNoteData d = stack.get(TVComponents.RESEARCH_NOTE);
		return d == null || d.complete;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		ResearchNoteData data = stack.get(TVComponents.RESEARCH_NOTE);
		if (data != null && data.complete && !ResearchManager.isResearchComplete(player, data.key)) {
			if (ResearchManager.doesPlayerHaveRequisites(player, data.key)) {
				ResearchManager.completeResearch(player, data.key);
				TVNetwork.sendResearchComplete((ServerPlayer) player, data.key);
				ResearchItem ri = ResearchManager.getResearch(data.key);
				if (ri != null) {
					for (String sibling : ri.siblings) {
						if (!ResearchManager.isResearchComplete(player, sibling) && ResearchManager.doesPlayerHaveRequisites(player, sibling)) {
							ResearchManager.completeResearch(player, sibling);
							TVNetwork.sendResearchComplete((ServerPlayer) player, sibling);
						}
					}
				}
				stack.shrink(1);
				level.playSound(null, player.getX(), player.getY(), player.getZ(), TVSounds.LEARN, SoundSource.PLAYERS, 0.75F, 1.0F);
			} else {
				player.sendSystemMessage(Component.translatable("tc.researcherror"));
			}
		} else if (data == null) {
			String key = ResearchManager.findHiddenResearch(player);
			if (key.equals("FAIL")) {
				stack.shrink(1);
				ItemEntity e = new ItemEntity(level, player.getX(), player.getY() + player.getEyeHeight() / 2.0F, player.getZ(),
					new ItemStack(TVItems.KNOWLEDGE_FRAGMENT, 7 + level.getRandom().nextInt(3)));
				level.addFreshEntity(e);
				level.playSound(null, player.getX(), player.getY(), player.getZ(), TVSounds.ERASE, SoundSource.PLAYERS, 0.75F, 1.0F);
			} else {
				ResearchManager.createNote(stack, key, level.getRandom());
				level.playSound(null, player.getX(), player.getY(), player.getZ(), TVSounds.WRITE, SoundSource.PLAYERS, 0.75F, 1.0F);
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		ResearchNoteData d = stack.get(TVComponents.RESEARCH_NOTE);
		if (d != null) {
			ResearchItem ri = ResearchManager.getResearch(d.key);
			if (ri != null) {
				builder.accept(ri.getName().copy().withColor(d.color));
				builder.accept(ri.getText().copy().withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
			}
		}
	}
}
