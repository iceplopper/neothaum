package dev.neothaum.golem;

import dev.neothaum.registry.TVComponents;
import dev.neothaum.entity.TVEntities;
import dev.neothaum.registry.TVItems;
import dev.neothaum.registry.TVSounds;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceBlock;

/**
 * Port of ItemGolemPlacer:
 * Places a golem automaton of the specified material.
 * Preserves installed core, upgrades, decorations, and custom names.
 */
public class GolemPlacerItem extends Item {
	private final GolemType golemType;

	public GolemPlacerItem(GolemType type, Properties props) {
		super(props);
		this.golemType = type;
	}

	public GolemType getGolemType() {
		return this.golemType;
	}

	public static ItemStack createCoreStack(GolemCoreType type) {
		return switch (type) {
			case FILL -> new ItemStack(TVItems.GOLEM_CORE_FILL);
			case EMPTY -> new ItemStack(TVItems.GOLEM_CORE_EMPTY);
			case GATHER -> new ItemStack(TVItems.GOLEM_CORE_GATHER);
			case HARVEST -> new ItemStack(TVItems.GOLEM_CORE_HARVEST);
			case GUARD -> new ItemStack(TVItems.GOLEM_CORE_GUARD);
			case DECANT -> new ItemStack(TVItems.GOLEM_CORE_DECANT);
			case ALCHEMY -> new ItemStack(TVItems.GOLEM_CORE_ALCHEMY);
			case CHOP -> new ItemStack(TVItems.GOLEM_CORE_CHOP);
			case USE -> new ItemStack(TVItems.GOLEM_CORE_USE);
			case BUTCHER -> new ItemStack(TVItems.GOLEM_CORE_BUTCHER);
			case SORT -> new ItemStack(TVItems.GOLEM_CORE_SORT);
			case FISH -> new ItemStack(TVItems.GOLEM_CORE_FISH);
			default -> new ItemStack(TVItems.GOLEM_CORE_BLANK);
		};
	}

	public static ItemStack createDecorationStack(GolemDecorationType type) {
		return switch (type) {
			case TOP_HAT -> new ItemStack(TVItems.GOLEM_DECO_TOPHAT);
			case GLASSES -> new ItemStack(TVItems.GOLEM_DECO_GLASSES);
			case BOWTIE -> new ItemStack(TVItems.GOLEM_DECO_BOWTIE);
			case FEZ -> new ItemStack(TVItems.GOLEM_DECO_FEZ);
			case DARTGUN -> new ItemStack(TVItems.GOLEM_DECO_DART);
			case VISOR -> new ItemStack(TVItems.GOLEM_DECO_VISOR);
			case ARMOR -> new ItemStack(TVItems.GOLEM_DECO_ARMOR);
			case MACE -> new ItemStack(TVItems.GOLEM_DECO_MACE);
		};
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level world = ctx.getLevel();
		Player player = ctx.getPlayer();
		ItemStack stack = ctx.getItemInHand();
		if (world.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		BlockPos clickedPos = ctx.getClickedPos();
		Direction side = ctx.getClickedFace();
		BlockPos spawnPos = clickedPos.relative(side);

		double yOffset = 0.0;
		var clicked = world.getBlockState(clickedPos);
		if (side == Direction.UP && clicked.getBlock() instanceof FenceBlock || clicked.getBlock() instanceof net.minecraft.world.level.block.IronBarsBlock) {
			yOffset = 0.5;
		}

		GolemEntity golem = new GolemEntity(TVEntities.GOLEM, world);
		golem.setGolemType(this.golemType);
		golem.snapTo(spawnPos.getX() + 0.5, spawnPos.getY() + yOffset, spawnPos.getZ() + 0.5, world.getRandom().nextFloat() * 360.0F, 0.0F);
		golem.setAdvanced(Boolean.TRUE.equals(stack.get(TVComponents.GOLEM_ADVANCED)));
		// ItemGolemPlacer.spawnCreature: home is where the golem stands, facing back at the block it was placed on
		golem.setHomeArea(spawnPos, side);
		if (stack.has(TVComponents.GOLEM_DECORATION)) {
			golem.setDecoration(stack.get(TVComponents.GOLEM_DECORATION));
		}
		if (stack.has(TVComponents.GOLEM_UPGRADES)) {
			golem.setUpgrades(stack.get(TVComponents.GOLEM_UPGRADES));
		}
		golem.setCore(stack.has(TVComponents.GOLEM_CORE) ? stack.get(TVComponents.GOLEM_CORE) : -1);
		golem.setHealth(golem.getMaxHealth());
		if (stack.has(TVComponents.GOLEM_MARKERS)) {
			golem.setMarkers(stack.get(TVComponents.GOLEM_MARKERS));
		}
		if (player != null) {
			golem.setOwner(player);
		}
		if (stack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) {
			golem.setCustomName(stack.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME));
		}
		java.util.List<ItemStack> filter = stack.get(TVComponents.GOLEM_INVENTORY);
		if (filter != null) {
			for (int a = 0; a < filter.size(); a++) {
				golem.setFilterItem(a, filter.get(a));
			}
		}

		world.addFreshEntity(golem);
		world.playSound(null, golem.getX(), golem.getY(), golem.getZ(), TVSounds.CAMERACLACK, SoundSource.NEUTRAL, 1.0F, 1.0F);

		if (player == null || !player.getAbilities().instabuild) {
			stack.consume(1, player);
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		if (stack.has(TVComponents.GOLEM_CORE)) {
			int coreId = stack.get(TVComponents.GOLEM_CORE);
			GolemCoreType ct = GolemCoreType.byId(coreId);
			builder.accept(Component.translatable("item.ItemGolemCore.name").append(": ")
				.append(Component.translatable("item.ItemGolemCore." + ct.id + ".name").withStyle(ChatFormatting.GOLD)));
		}
		if (Boolean.TRUE.equals(stack.get(TVComponents.GOLEM_ADVANCED))) {
			builder.accept(Component.translatable("tc.adv").withStyle(ChatFormatting.AQUA));
		}
		if (stack.has(TVComponents.GOLEM_UPGRADES)) {
			List<Integer> ups = stack.get(TVComponents.GOLEM_UPGRADES);
			if (ups != null && !ups.isEmpty()) {
				var upComp = Component.empty().withStyle(ChatFormatting.BLUE);
				for (int u : ups) {
					GolemUpgradeType ut = GolemUpgradeType.byId(u);
					upComp.append(Component.translatable("item.ItemGolemUpgrade." + ut.id + ".name")).append(" ");
				}
				builder.accept(upComp);
			}
		}
		if (stack.has(TVComponents.GOLEM_DECORATION)) {
			String deco = stack.get(TVComponents.GOLEM_DECORATION);
			if (deco != null && !deco.isEmpty()) {
				var decoComp = Component.empty().withStyle(ChatFormatting.DARK_GREEN);
				for (int i = 0; i < deco.length(); i++) {
					GolemDecorationType dt = GolemDecorationType.byCode(deco.charAt(i));
					if (dt != null) {
						decoComp.append(Component.translatable("item.ItemGolemDecoration." + dt.id + ".name")).append(" ");
					}
				}
				builder.accept(decoComp);
			}
		}
	}
}
