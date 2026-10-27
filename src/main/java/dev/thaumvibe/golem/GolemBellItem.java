package dev.thaumvibe.golem;

import dev.thaumvibe.registry.TVComponents;
import dev.thaumvibe.registry.TVSounds;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Port of ItemGolemBell (Golemancer's Bell):
 * Primary configuration tool for golems.
 * - Shift-right click golem: picks up golem back into item form.
 * - Right click golem: links bell to this golem / inspects configuration.
 * - Right click block / inventory: sets home position and side face.
 */
public class GolemBellItem extends Item {
	public GolemBellItem(Properties props) {
		super(props);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
		if (interactionTarget instanceof GolemEntity golem) {
			Level world = player.level();
			if (player.isShiftKeyDown()) {
				// Pick up golem back into placer item
				if (!world.isClientSide()) {
					ItemStack placer = golem.toPlacerItem();
					golem.discard();
					world.playSound(null, player.getX(), player.getY(), player.getZ(), TVSounds.ZAP, SoundSource.PLAYERS, 0.5F, 1.0F);
					if (!player.getInventory().add(placer)) {
						player.spawnAtLocation((ServerLevel) world, placer);
					}
				}
				return InteractionResult.SUCCESS;
			} else {
				// Select golem to configure
				if (!world.isClientSide()) {
					stack.set(TVComponents.GOLEM_BELL_ID, golem.getId());
					player.sendSystemMessage(Component.translatable("tc.bell.linked", golem.getDisplayName()).withStyle(ChatFormatting.GOLD));
					world.playSound(null, player.getX(), player.getY(), player.getZ(), TVSounds.CAMERACLACK, SoundSource.PLAYERS, 0.5F, 1.2F);
				}
				return InteractionResult.SUCCESS;
			}
		}
		return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level world = ctx.getLevel();
		Player player = ctx.getPlayer();
		ItemStack stack = ctx.getItemInHand();
		BlockPos clickedPos = ctx.getClickedPos();
		Direction face = ctx.getClickedFace();

		if (!world.isClientSide() && player != null) {
			if (stack.has(TVComponents.GOLEM_BELL_ID)) {
				int golemId = stack.get(TVComponents.GOLEM_BELL_ID);
				if (world.getEntity(golemId) instanceof GolemEntity golem) {
					golem.setHomeArea(clickedPos, face);
					player.sendSystemMessage(Component.translatable("tc.bell.home_set", clickedPos.getX(), clickedPos.getY(), clickedPos.getZ()).withStyle(ChatFormatting.GREEN));
					world.playSound(null, clickedPos.getX(), clickedPos.getY(), clickedPos.getZ(), TVSounds.HHON, SoundSource.BLOCKS, 0.6F, 1.0F);
					return InteractionResult.SUCCESS;
				}
			}
			stack.set(TVComponents.GOLEM_BELL_POS, clickedPos);
			stack.set(TVComponents.GOLEM_BELL_FACE, face.get3DDataValue());
			world.playSound(null, clickedPos.getX(), clickedPos.getY(), clickedPos.getZ(), TVSounds.HHOFF, SoundSource.BLOCKS, 0.5F, 1.0F);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		builder.accept(Component.translatable("item.GolemBell.desc").withStyle(ChatFormatting.GRAY));
		if (stack.has(TVComponents.GOLEM_BELL_ID)) {
			builder.accept(Component.translatable("tc.bell.has_linked").withStyle(ChatFormatting.YELLOW));
		}
	}
}
