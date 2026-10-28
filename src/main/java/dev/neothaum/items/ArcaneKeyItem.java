package dev.neothaum.items;

import dev.neothaum.blocks.ArcaneDoorBlock;
import dev.neothaum.blocks.ArcanePressurePlateBlock;
import dev.neothaum.registry.TVComponents;
import dev.neothaum.tiles.OwnedBlockEntity;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import dev.neothaum.registry.TVSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of ItemArcaneDoorKey: grants access to warded doors and arcane pressure plates.
 * Iron Arcane Key grants standard access; Gold Arcane Key grants admin access.
 */
public class ArcaneKeyItem extends Item {
	private final boolean admin;

	public ArcaneKeyItem(boolean admin, Properties props) {
		super(props);
		this.admin = admin;
	}

	public boolean isAdmin() {
		return this.admin;
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return stack.has(TVComponents.KEY_DATA) || super.isFoil(stack);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockState state = level.getBlockState(pos);
		Player player = ctx.getPlayer();
		ItemStack stack = ctx.getItemInHand();

		BlockPos targetPos;
		int targetType;

		if (state.getBlock() instanceof ArcaneDoorBlock door) {
			targetPos = door.getLowerPos(pos, state);
			targetType = 0;
		} else if (state.getBlock() instanceof ArcanePressurePlateBlock) {
			targetPos = pos;
			targetType = 1;
		} else {
			return InteractionResult.PASS;
		}

		BlockEntity rawBe = level.getBlockEntity(targetPos);
		if (!(rawBe instanceof OwnedBlockEntity be)) {
			return InteractionResult.PASS;
		}

		if (!stack.has(TVComponents.KEY_DATA)) {
			// ItemKey: a blank key is imprinted by the owner (or, for an iron key, anyone with admin access); one blank is used up and one
			// imprinted key handed back. Anyone else gets nothing.
			boolean canLink = this.admin ? be.isOwner(player) : be.hasAdminAccess(player);
			if (canLink && player != null) {
				if (!level.isClientSide()) {
					ItemStack key = new ItemStack(this);
					key.set(TVComponents.KEY_DATA, new TVComponents.KeyData(targetPos, targetType));
					if (!player.getInventory().add(key)) {
						player.drop(key, false, net.minecraft.util.Prediction.SERVER_ONLY);
					}
					if (!player.getAbilities().instabuild) {
						stack.shrink(1);
					}
					chat(player, Component.translatable(targetType == 0 ? "tc.key1" : "tc.key2"));
					level.playSound(null, pos, TVSounds.KEY, SoundSource.BLOCKS, 1.0F, 0.9F);
				}
			}
			return InteractionResult.SUCCESS;
		}

		TVComponents.KeyData keyData = stack.get(TVComponents.KEY_DATA);
		if (keyData == null) {
			return InteractionResult.PASS;
		}
		boolean alreadyHas = this.admin ? be.hasAdminAccess(player) : be.hasAccess(player);
		if (!alreadyHas && keyData.pos().equals(targetPos) && player != null) {
			// an imprinted key gives its holder access and is used up
			if (!level.isClientSide()) {
				be.addAccess(player.getGameProfile().name(), this.admin);
				if (targetType == 0 && level.getBlockEntity(targetPos.above()) instanceof OwnedBlockEntity beTop) {
					beTop.addAccess(player.getGameProfile().name(), this.admin);
				}
				Component msg = Component.translatable(targetType == 0 ? "tc.key3" : "tc.key5");
				if (this.admin) {
					msg = msg.copy().append(Component.translatable(targetType == 0 ? "tc.key4" : "tc.key6"));
				}
				chat(player, msg);
				level.playSound(null, pos, TVSounds.KEY, SoundSource.BLOCKS, 1.0F, 1.1F);
				if (!player.getAbilities().instabuild) {
					stack.shrink(1);
				}
			}
		} else if (!level.isClientSide() && player != null) {
			chat(player, Component.translatable(keyData.pos().equals(targetPos) ? "tc.key8" : "tc.key7"));
		}
		return InteractionResult.SUCCESS;
	}

	/** TC4 sends key messages to chat in purple italics. */
	private static void chat(Player player, Component msg) {
		player.sendSystemMessage(msg.copy().withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		TVComponents.KeyData keyData = stack.get(TVComponents.KEY_DATA);
		if (keyData != null) {
			builder.accept(Component.translatable("tc.key9").withStyle(ChatFormatting.DARK_PURPLE));
			Component loc = Component.translatable(keyData.type() == 0 ? "tc.key10" : "tc.key11")
				.append(" " + keyData.pos().getX() + ", " + keyData.pos().getY() + ", " + keyData.pos().getZ())
				.withStyle(ChatFormatting.GRAY);
			builder.accept(loc);
		}
	}
}
