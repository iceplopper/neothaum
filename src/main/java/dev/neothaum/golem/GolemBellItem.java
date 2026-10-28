package dev.neothaum.golem;

import dev.neothaum.registry.TVComponents;
import dev.neothaum.registry.TVItems;
import dev.neothaum.registry.TVSounds;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * ItemGolemBell. Right-click a golem to select it: the bell takes a copy of its markers and its home, which are drawn while the bell is held.
 * Right-clicking a block face (or liquid) then marks or unmarks it for that golem; a golem with an Order upgrade can tell marker colours apart,
 * so clicking a marked face again steps it through the colours instead (sneak always clears it). The focus key clears every marker. Hitting
 * a golem packs it into its placer with everything intact; sneak-hitting strips its core and upgrades, which drop separately, each upgrade
 * only half the time.
 */
public class GolemBellItem extends Item {
	public static final String[] COLOR_NAMES = {"White", "Orange", "Magenta", "Light Blue", "Yellow", "Lime", "Pink", "Gray", "Light Gray", "Cyan",
		"Purple", "Blue", "Brown", "Green", "Red", "Black"};

	public GolemBellItem(Properties props) {
		super(props);
	}

	public static int getGolemId(ItemStack stack) {
		return stack.getOrDefault(TVComponents.GOLEM_BELL_ID, -1);
	}

	public static List<GolemMarker> getMarkers(ItemStack stack) {
		return stack.getOrDefault(TVComponents.GOLEM_MARKERS, List.of());
	}

	private static void clearLink(ItemStack stack) {
		stack.remove(TVComponents.GOLEM_BELL_ID);
		stack.remove(TVComponents.GOLEM_MARKERS);
		stack.remove(TVComponents.GOLEM_BELL_POS);
		stack.remove(TVComponents.GOLEM_BELL_FACE);
	}

	/** itemInteractionForEntity: select the golem. */
	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
		if (!(target instanceof GolemEntity golem)) {
			return InteractionResult.PASS;
		}
		if (target.level() instanceof ServerLevel sl) {
			// the interaction hands a copy of the held stack; write into the one in hand
			ItemStack held = player.getItemInHand(hand);
			clearLink(held);
			held.set(TVComponents.GOLEM_MARKERS, new ArrayList<>(golem.getMarkers()));
			held.set(TVComponents.GOLEM_BELL_ID, golem.getId());
			held.set(TVComponents.GOLEM_BELL_POS, golem.getHomePos());
			held.set(TVComponents.GOLEM_BELL_FACE, golem.getHomeFacing().get3DDataValue());
			sl.playSound(null, golem, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 0.7F, 1.0F + sl.getRandom().nextFloat() * 0.1F);
		}
		return InteractionResult.SUCCESS;
	}

	/** onItemUseFirst: mark the clicked face. */
	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		if (ctx.getPlayer() != null && !ctx.getLevel().isClientSide()) {
			changeMarkers(ctx.getItemInHand(), ctx.getPlayer(), ctx.getLevel(), ctx.getClickedPos(), ctx.getClickedFace());
		}
		return InteractionResult.SUCCESS;
	}

	/** The bell's ray also stops at liquid, so pools can be marked. */
	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
		if (hit.getType() != HitResult.Type.BLOCK) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			changeMarkers(player.getItemInHand(hand), player, level, hit.getBlockPos(), hit.getDirection());
		}
		return InteractionResult.SUCCESS;
	}

	/** ItemGolemBell.changeMarkers. */
	public static void changeMarkers(ItemStack stack, Player player, Level level, BlockPos pos, Direction side) {
		int gid = getGolemId(stack);
		GolemEntity golem = gid > -1 && level.getEntity(gid) instanceof GolemEntity g ? g : null;
		boolean multipleColors = golem != null && golem.getUpgradeCount(GolemUpgradeType.ORDER) > 0;
		List<GolemMarker> markers = new ArrayList<>(getMarkers(stack));
		int count = markers.size();
		GolemMarker any = new GolemMarker(pos.immutable(), level.dimension(), (byte) side.get3DDataValue(), (byte) -1);
		int index = -1;
		int color = -1;
		if (!multipleColors) {
			index = markers.indexOf(any);
		} else {
			for (int c = -1; c < 16 && index < 0; c++) {
				index = markers.indexOf(any.withColor(c));
				color = c;
			}
		}
		if (index >= 0) {
			markers.remove(index);
			if (multipleColors && !player.isShiftKeyDown() && ++color <= 15) {
				markers.add(any.withColor(color));
				count++;
				if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
					sp.sendSystemMessage(Component.translatable("tc.markerchange.to", COLOR_NAMES[color]), true);
				}
			}
		} else {
			markers.add(any);
		}
		if (count != markers.size()) {
			stack.set(TVComponents.GOLEM_MARKERS, markers);
			if (gid > -1) {
				if (golem != null) {
					golem.setMarkers(markers);
				} else {
					// the golem is gone: the bell forgets it
					clearLink(stack);
				}
			}
		}
		level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7F, 1.0F + level.getRandom().nextFloat() * 0.1F);
	}

	/** ItemGolemBell.resetMarkers: the focus key clears every marker. */
	public static void resetMarkers(ItemStack stack, Player player) {
		int gid = getGolemId(stack);
		if (gid > -1 && player.level().getEntity(gid) instanceof GolemEntity golem) {
			stack.set(TVComponents.GOLEM_MARKERS, new ArrayList<>());
			golem.setMarkers(List.of());
			player.level().playSound(null, player, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7F,
				1.0F + player.getRandom().nextFloat() * 0.1F);
		}
	}

	private static ItemStack upgradeStack(GolemUpgradeType type) {
		return new ItemStack(switch (type) {
			case AIR -> TVItems.GOLEM_UPGRADE_AIR;
			case EARTH -> TVItems.GOLEM_UPGRADE_EARTH;
			case FIRE -> TVItems.GOLEM_UPGRADE_FIRE;
			case WATER -> TVItems.GOLEM_UPGRADE_WATER;
			case ORDER -> TVItems.GOLEM_UPGRADE_ORDER;
			case ENTROPY -> TVItems.GOLEM_UPGRADE_ENTROPY;
		});
	}

	/** onLeftClickEntity: pick the golem up. */
	public static void init() {
		AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
			if (!player.getMainHandItem().is(TVItems.GOLEM_BELL) || !(entity instanceof GolemEntity golem) || !golem.isAlive()) {
				return InteractionResult.PASS;
			}
			if (level instanceof ServerLevel sl) {
				ItemStack placer = golem.toPlacerItem();
				if (player.isShiftKeyDown()) {
					if (golem.getCore() > -1) {
						golem.spawnAtLocation(sl, GolemPlacerItem.createCoreStack(GolemCoreType.byId(golem.getCore())), 0.5F);
					}
					for (int up : golem.getUpgrades()) {
						if (up > -1 && sl.getRandom().nextBoolean()) {
							golem.spawnAtLocation(sl, upgradeStack(GolemUpgradeType.byId(up)), 0.5F);
						}
					}
					placer.remove(TVComponents.GOLEM_CORE);
					placer.remove(TVComponents.GOLEM_UPGRADES);
					placer.remove(TVComponents.GOLEM_DECORATION);
					placer.remove(TVComponents.GOLEM_MARKERS);
					placer.remove(net.minecraft.core.component.DataComponents.CUSTOM_NAME);
				}
				golem.spawnAtLocation(sl, placer, 0.5F);
				if (!golem.getCarried().isEmpty()) {
					golem.spawnAtLocation(sl, golem.getCarried(), 0.5F);
					golem.setCarried(ItemStack.EMPTY);
				}
				sl.playSound(null, golem, TVSounds.ZAP, SoundSource.NEUTRAL, 0.5F, 1.0F);
				golem.discard();
			} else {
				golem.spawnAnim();
			}
			return InteractionResult.SUCCESS;
		});
	}
}
