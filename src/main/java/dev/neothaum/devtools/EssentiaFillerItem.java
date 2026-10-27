package dev.neothaum.devtools;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectContainer;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.registry.TVComponents;
import dev.neothaum.tiles.AlchemyFurnaceBlockEntity;
import dev.neothaum.tiles.CrucibleBlockEntity;
import dev.neothaum.tiles.TVBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Dev tool: use in the air to pick an aspect (sneak to go backwards); use on a jar, alembic, buffer, crucible or alchemical furnace to pour
 * in 16 of it, or sneak-use to empty it.
 */
public class EssentiaFillerItem extends Item {
	public EssentiaFillerItem(Item.Properties props) {
		super(props);
	}

	private static Aspect selected(ItemStack stack) {
		Aspect a = stack.get(TVComponents.ASPECT_FILTER);
		return a != null ? a : Aspect.AIR;
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable("item.neothaum.dev_essentia_filler.of", selected(stack).getName());
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!player.isCreative()) {
			return InteractionResult.FAIL;
		}
		ItemStack stack = player.getItemInHand(hand);
		List<Aspect> all = new ArrayList<>(Aspect.aspects.values());
		int i = Math.floorMod(all.indexOf(selected(stack)) + (player.isShiftKeyDown() ? -1 : 1), all.size());
		stack.set(TVComponents.ASPECT_FILTER, all.get(i));
		if (!level.isClientSide()) {
			player.sendOverlayMessage(Component.literal("[Dev] Essentia: " + all.get(i).getName()).withStyle(ChatFormatting.LIGHT_PURPLE));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Player player = ctx.getPlayer();
		if (player == null || !player.isCreative()) {
			return InteractionResult.FAIL;
		}
		Level level = ctx.getLevel();
		BlockEntity be = level.getBlockEntity(ctx.getClickedPos());
		if (level.isClientSide()) {
			return be == null ? InteractionResult.PASS : InteractionResult.SUCCESS;
		}
		Aspect a = selected(ctx.getItemInHand());
		boolean empty = player.isShiftKeyDown();
		boolean done = true;
		if (be instanceof AspectContainer c) {
			if (empty) {
				c.setAspects(new AspectList());
			} else {
				c.addToContainer(a, 16);
			}
		} else if (be instanceof CrucibleBlockEntity c) {
			c.aspects = empty ? new AspectList() : c.aspects.add(a, 16);
		} else if (be instanceof AlchemyFurnaceBlockEntity f) {
			if (empty) {
				f.aspects = new AspectList();
			} else if (f.vis + 16 <= AlchemyFurnaceBlockEntity.MAX_VIS) {
				f.aspects.add(a, 16);
			}
			f.vis = f.aspects.visSize();
		} else {
			done = false;
		}
		if (!done) {
			return InteractionResult.PASS;
		}
		if (be instanceof TVBlockEntity t) {
			t.markForUpdate();
		}
		player.sendOverlayMessage(Component.literal(empty ? "[Dev] Emptied" : "[Dev] Added 16 " + a.getName()).withStyle(ChatFormatting.LIGHT_PURPLE));
		return InteractionResult.SUCCESS;
	}
}
