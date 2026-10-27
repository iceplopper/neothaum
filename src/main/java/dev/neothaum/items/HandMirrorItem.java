package dev.neothaum.items;

import dev.neothaum.blocks.MagicMirrorBlock;
import dev.neothaum.menu.HandMirrorMenu;
import dev.neothaum.registry.TVComponents;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.tiles.MagicMirrorBlockEntity;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * ItemHandMirror: Portable pocket mirror linked to a placed Magic Mirror.
 * Transport items across dimensions anywhere in the world!
 */
public class HandMirrorItem extends Item {
	public HandMirrorItem(Properties props) {
		super(props);
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return stack.has(TVComponents.MIRROR_LINK);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		Player player = ctx.getPlayer();
		ItemStack stack = ctx.getItemInHand();

		if (level.getBlockState(ctx.getClickedPos()).getBlock() instanceof MagicMirrorBlock) {
			if (!level.isClientSide() && player != null) {
				TVComponents.MirrorLink link = new TVComponents.MirrorLink(level.dimension().identifier(), ctx.getClickedPos());
				stack.set(TVComponents.MIRROR_LINK, link);
				level.playSound(null, ctx.getClickedPos(), TVSounds.JAR, SoundSource.PLAYERS, 1.0F, 2.0F);
				player.sendSystemMessage(
					Component.translatable("tc.handmirrorlinked").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)
				);
			}
			return InteractionResult.SUCCESS;
		}

		return super.useOn(ctx);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide()) {
			TVComponents.MirrorLink link = stack.get(TVComponents.MIRROR_LINK);
			if (link == null) {
				player.sendSystemMessage(
					Component.translatable("tc.handmirrorerror").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)
				);
				return InteractionResult.FAIL;
			}

			if (level instanceof ServerLevel serverLevel) {
				ServerLevel targetLevel = serverLevel.getServer().getLevel(link.levelKey());
				if (targetLevel != null && targetLevel.isLoaded(link.pos()) && targetLevel.getBlockEntity(link.pos()) instanceof MagicMirrorBlockEntity mirror) {
					if (mirror.isLinkValid()) {
						int slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().getSelectedSlot() : -1;
						player.openMenu(new SimpleMenuProvider(
							(id, inv, p) -> new HandMirrorMenu(id, inv, slot),
							stack.getHoverName()
						));
						return InteractionResult.SUCCESS;
					}
				}
			}

			player.sendSystemMessage(
				Component.translatable("tc.handmirrorerror").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)
			);
			return InteractionResult.FAIL;
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		TVComponents.MirrorLink link = stack.get(TVComponents.MIRROR_LINK);
		if (link != null) {
			builder.accept(Component.literal("§5Linked to " + link.pos().getX() + ", " + link.pos().getY() + ", " + link.pos().getZ() + " in " + link.dimension().getPath()));
		}
	}
}
