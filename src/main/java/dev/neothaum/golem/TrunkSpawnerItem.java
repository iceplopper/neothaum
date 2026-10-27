package dev.neothaum.golem;

import dev.neothaum.entity.TVEntities;
import dev.neothaum.registry.TVComponents;
import dev.neothaum.registry.TVSounds;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class TrunkSpawnerItem extends Item {
	public TrunkSpawnerItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level world = ctx.getLevel();
		if (world.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		Player player = ctx.getPlayer();
		ItemStack stack = ctx.getItemInHand();
		BlockPos clickedPos = ctx.getClickedPos();
		Direction side = ctx.getClickedFace();
		BlockPos spawnPos = clickedPos.relative(side);

		TravelingTrunkEntity trunk = new TravelingTrunkEntity(TVEntities.TRAVELING_TRUNK, world);
		trunk.snapTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
			world.getRandom().nextFloat() * 360.0F, 0.0F);

		if (player != null) {
			trunk.setOwner(player);
		}

		if (stack.has(TVComponents.GOLEM_CORE)) {
			trunk.setUpgrade(stack.get(TVComponents.GOLEM_CORE));
		}

		ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
		if (contents != null) {
			contents.copyInto(trunk.getItemsList());
		}

		world.addFreshEntity(trunk);
		world.playSound(null, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(),
			TVSounds.ZAP, SoundSource.PLAYERS, 0.5F, 1.0F);

		if (player != null && !player.getAbilities().instabuild) {
			stack.shrink(1);
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		if (stack.has(TVComponents.GOLEM_CORE)) {
			int up = stack.get(TVComponents.GOLEM_CORE);
			if (up >= 0 && up < GolemUpgradeType.values().length) {
				GolemUpgradeType type = GolemUpgradeType.values()[up];
				builder.accept(Component.translatable("item.neothaum.golem_upgrade_" + type.name().toLowerCase())
					.withStyle(ChatFormatting.BLUE));
			}
		}

		ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
		if (contents != null && contents.nonEmptyItemCopyStream().findAny().isPresent()) {
			builder.accept(Component.translatable("item.TrunkSpawner.text.1").withStyle(ChatFormatting.GRAY));
		}
	}
}
