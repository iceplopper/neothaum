package dev.neothaum.items;

import dev.neothaum.blocks.MirrorBlock;
import dev.neothaum.registry.TVComponents;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.tiles.MagicMirrorBlockEntity;
import dev.neothaum.tiles.EssentiaMirrorBlockEntity;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BlockMirrorItem: Handles linking pairs of mirrors by right-clicking on a placed mirror,
 * and linking them upon block placement.
 */
public class MirrorItem extends BlockItem {
	private final boolean essentia;

	public MirrorItem(Block block, boolean essentia, Properties props) {
		super(block, props);
		this.essentia = essentia;
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return stack.has(TVComponents.MIRROR_LINK);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		Player player = ctx.getPlayer();
		ItemStack held = ctx.getItemInHand();
		BlockState targetState = level.getBlockState(ctx.getClickedPos());

		if (targetState.getBlock() instanceof MirrorBlock mirrorBlock) {
			if (mirrorBlock.isEssentia() == this.essentia) {
				if (!level.isClientSide() && player != null) {
					boolean alreadyLinked = false;
					if (!this.essentia) {
						if (level.getBlockEntity(ctx.getClickedPos()) instanceof MagicMirrorBlockEntity mm) {
							alreadyLinked = mm.isLinkValid();
						}
					} else {
						if (level.getBlockEntity(ctx.getClickedPos()) instanceof EssentiaMirrorBlockEntity em) {
							alreadyLinked = em.isLinkValid();
						}
					}

					if (alreadyLinked) {
						player.sendSystemMessage(
							Component.translatable("tc.mirrorlinkedalready").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)
						);
						return InteractionResult.SUCCESS;
					}

					ItemStack linkedStack = held.copy();
					linkedStack.setCount(1);
					TVComponents.MirrorLink link = new TVComponents.MirrorLink(level.dimension().identifier(), ctx.getClickedPos());
					linkedStack.set(TVComponents.MIRROR_LINK, link);

					level.playSound(null, ctx.getClickedPos(), TVSounds.JAR, SoundSource.BLOCKS, 1.0F, 2.0F);

					if (!player.isCreative()) {
						held.shrink(1);
					}
					if (!player.getInventory().add(linkedStack)) {
						player.drop(linkedStack, false, net.minecraft.util.Prediction.SERVER_ONLY);
					}
				}
				return InteractionResult.SUCCESS;
			}
		}

		return super.useOn(ctx);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		TVComponents.MirrorLink link = stack.get(TVComponents.MIRROR_LINK);
		if (link != null) {
			builder.accept(Component.literal("§5Linked to " + link.pos().getX() + ", " + link.pos().getY() + ", " + link.pos().getZ() + " in " + link.dimension().getPath()));
		}
	}
}
