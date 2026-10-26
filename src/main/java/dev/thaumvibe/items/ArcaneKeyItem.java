package dev.thaumvibe.items;

import dev.thaumvibe.blocks.ArcaneDoorBlock;
import dev.thaumvibe.blocks.ArcanePressurePlateBlock;
import dev.thaumvibe.registry.TVComponents;
import dev.thaumvibe.tiles.OwnedBlockEntity;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
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
			// Blank key: link to this block if permitted
			boolean canLink = this.admin ? be.isOwner(player) : be.hasAdminAccess(player);
			if (canLink) {
				if (!level.isClientSide()) {
					stack.set(TVComponents.KEY_DATA, new TVComponents.KeyData(targetPos, targetType));
					if (player != null) {
						player.sendOverlayMessage(
							Component.translatable(targetType == 0 ? "tc.key1" : "tc.key2")
								.withStyle(ChatFormatting.DARK_PURPLE));
					}
					level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.0F);
				}
				return InteractionResult.SUCCESS;
			} else {
				if (!level.isClientSide() && player != null) {
					player.sendOverlayMessage(Component.translatable("tc.doorfail").withStyle(ChatFormatting.DARK_PURPLE));
				}
				return InteractionResult.CONSUME;
			}
		}

		// Imprinted key: check target match
		TVComponents.KeyData keyData = stack.get(TVComponents.KEY_DATA);
		if (keyData == null) {
			return InteractionResult.PASS;
		}

		if (!keyData.pos().equals(targetPos) || keyData.type() != targetType) {
			if (!level.isClientSide() && player != null) {
				player.sendOverlayMessage(Component.translatable("tc.key7").withStyle(ChatFormatting.RED));
			}
			return InteractionResult.SUCCESS;
		}

		// Imprinted key matches: check if player already has access
		boolean alreadyHas = this.admin ? be.hasAdminAccess(player) : be.hasAccess(player);
		if (alreadyHas) {
			if (!level.isClientSide() && player != null) {
				player.sendOverlayMessage(Component.translatable("tc.key8").withStyle(ChatFormatting.YELLOW));
			}
			return InteractionResult.SUCCESS;
		}

		// Grant access
		if (!level.isClientSide() && player != null) {
			be.addAccess(player.getGameProfile().name(), this.admin);
			if (targetType == 0 && level.getBlockEntity(targetPos.above()) instanceof OwnedBlockEntity beTop) {
				beTop.addAccess(player.getGameProfile().name(), this.admin);
			}

			Component msg;
			if (targetType == 0) {
				msg = this.admin
					? Component.translatable("tc.key3").append(Component.translatable("tc.key4"))
					: Component.translatable("tc.key3");
			} else {
				msg = this.admin
					? Component.translatable("tc.key5").append(Component.translatable("tc.key6"))
					: Component.translatable("tc.key5");
			}
			player.sendOverlayMessage(msg.copy().withStyle(ChatFormatting.GREEN));
			level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5F, 1.2F);

			if (!player.getAbilities().instabuild) {
				stack.shrink(1);
			}
		}

		return InteractionResult.SUCCESS;
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
