package dev.neothaum.items;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.api.wands.WandCap;
import dev.neothaum.api.wands.WandRod;
import dev.neothaum.api.wands.FocusItem;
import dev.neothaum.api.wands.FocusUpgradeType;
import dev.neothaum.wands.WandFoci;
import dev.neothaum.registry.TVComponents;
import dev.neothaum.tiles.NodeBlockEntity;
import dev.neothaum.wands.WandManager;
import java.text.DecimalFormat;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Port of thaumcraft.common.items.wands.ItemWandCasting (foci arrive in a later phase). */
public class WandItem extends Item {
	private static final DecimalFormat FORMAT = new DecimalFormat("#######.##");

	public WandItem(Item.Properties props) {
		super(props);
	}

	// ---------------- static accessors (ItemWandCasting instance methods) ----------------

	public static WandRod getRod(ItemStack stack) {
		WandRod r = WandRod.rods.get(stack.getOrDefault(TVComponents.WAND_ROD, "wood"));
		return r != null ? r : WandRod.rods.get("wood");
	}

	public static WandCap getCap(ItemStack stack) {
		WandCap c = WandCap.caps.get(stack.getOrDefault(TVComponents.WAND_CAP, "iron"));
		return c != null ? c : WandCap.caps.get("iron");
	}

	public static boolean isStaff(ItemStack stack) {
		return getRod(stack).isStaff();
	}

	public static boolean isSceptre(ItemStack stack) {
		return stack.getOrDefault(TVComponents.SCEPTRE, false);
	}

	/** StaffRod.hasRunes: the primal staff, whose runes also add a level of potency. */
	public static boolean hasRunes(ItemStack stack) {
		return getRod(stack).hasRunes();
	}

	// ---------------- focus

	public static ItemStack getFocusStack(ItemStack wand) {
		ItemStack f = wand.get(TVComponents.WAND_FOCUS);
		return f == null ? ItemStack.EMPTY : f;
	}

	public static FocusItem getFocus(ItemStack wand) {
		return getFocusStack(wand).getItem() instanceof FocusItem f ? f : null;
	}

	public static void setFocus(ItemStack wand, ItemStack focus) {
		if (focus == null || focus.isEmpty()) {
			wand.remove(TVComponents.WAND_FOCUS);
		} else {
			wand.set(TVComponents.WAND_FOCUS, focus.copyWithCount(1));
		}
	}

	private static int upgrade(ItemStack wand, FocusUpgradeType type) {
		FocusItem f = getFocus(wand);
		return f == null ? 0 : f.getUpgradeLevel(getFocusStack(wand), type);
	}

	public static int getFocusPotency(ItemStack wand) {
		return getFocus(wand) == null ? 0 : upgrade(wand, FocusUpgradeType.potency) + (hasRunes(wand) ? 1 : 0);
	}

	public static int getFocusTreasure(ItemStack wand) {
		return upgrade(wand, FocusUpgradeType.treasure);
	}

	public static int getFocusFrugal(ItemStack wand) {
		return upgrade(wand, FocusUpgradeType.frugal);
	}

	public static int getFocusEnlarge(ItemStack wand) {
		return upgrade(wand, FocusUpgradeType.enlarge);
	}

	public static int getFocusExtend(ItemStack wand) {
		return upgrade(wand, FocusUpgradeType.extend);
	}

	public static int getMaxVis(ItemStack stack) {
		return getRod(stack).getCapacity() * (isSceptre(stack) ? 150 : 100);
	}

	public static AspectList getAllVis(ItemStack is) {
		AspectList stored = is.get(TVComponents.WAND_VIS);
		AspectList out = new AspectList();
		for (Aspect aspect : Aspect.getPrimalAspects()) {
			out.merge(aspect, stored == null ? 0 : stored.getAmount(aspect));
		}
		return out;
	}

	public static AspectList getAspectsWithRoom(ItemStack wandstack) {
		AspectList out = new AspectList();
		AspectList cur = getAllVis(wandstack);
		for (Aspect aspect : cur.getAspects()) {
			if (cur.getAmount(aspect) < getMaxVis(wandstack)) {
				out.add(aspect, 1);
			}
		}
		return out;
	}

	public static int getVis(ItemStack is, Aspect aspect) {
		AspectList stored = is.get(TVComponents.WAND_VIS);
		return stored == null ? 0 : stored.getAmount(aspect);
	}

	public static void storeVis(ItemStack is, Aspect aspect, int amount) {
		AspectList stored = getAllVis(is);
		stored.aspects.put(aspect, amount);
		is.set(TVComponents.WAND_VIS, stored);
	}

	public static float getConsumptionModifier(ItemStack is, Player player, Aspect aspect, boolean crafting) {
		WandCap cap = getCap(is);
		float mod;
		if (cap.getSpecialCostModifierAspects() != null && cap.getSpecialCostModifierAspects().contains(aspect)) {
			mod = cap.getSpecialCostModifier();
		} else {
			mod = cap.getBaseCostModifier();
		}
		if (player != null) {
			mod -= WandManager.getTotalVisDiscount(player, aspect);
		}
		if (getFocus(is) != null && !crafting) {
			mod -= getFocusFrugal(is) / 10.0F;
		}
		if (isSceptre(is)) {
			mod -= 0.1F;
		}
		return Math.max(mod, 0.1F);
	}

	public static boolean consumeVis(ItemStack is, Player player, Aspect aspect, int amount, boolean crafting) {
		amount = (int) (amount * getConsumptionModifier(is, player, aspect, crafting));
		if (getVis(is, aspect) >= amount) {
			storeVis(is, aspect, getVis(is, aspect) - amount);
			return true;
		}
		return false;
	}

	public static boolean consumeAllVisCrafting(ItemStack is, Player player, AspectList aspects, boolean doit) {
		if (aspects == null || aspects.size() == 0) {
			return false;
		}
		AspectList nl = new AspectList();
		for (Aspect aspect : aspects.getAspects()) {
			nl.add(aspect, aspects.getAmount(aspect) * 100);
		}
		return consumeAllVis(is, player, nl, doit, true);
	}

	public static boolean consumeAllVis(ItemStack is, Player player, AspectList aspects, boolean doit, boolean crafting) {
		if (aspects == null || aspects.size() == 0) {
			return false;
		}
		AspectList nl = new AspectList();
		for (Aspect aspect : aspects.getAspects()) {
			int cost = (int) (aspects.getAmount(aspect) * getConsumptionModifier(is, player, aspect, crafting));
			nl.add(aspect, cost);
		}
		for (Aspect aspect : nl.getAspects()) {
			if (getVis(is, aspect) < nl.getAmount(aspect)) {
				return false;
			}
		}
		if (doit && (player == null || !player.level().isClientSide())) {
			for (Aspect aspect : nl.getAspects()) {
				storeVis(is, aspect, getVis(is, aspect) - nl.getAmount(aspect));
			}
		}
		return true;
	}

	/** Adds whole vis (x100 centivis); returns leftover whole vis. */
	public static int addVis(ItemStack is, Aspect aspect, int amount, boolean doit) {
		if (!aspect.isPrimal()) {
			return 0;
		}
		int storeAmount = getVis(is, aspect) + amount * 100;
		int leftover = Math.max(storeAmount - getMaxVis(is), 0);
		if (doit) {
			storeVis(is, aspect, Math.min(storeAmount, getMaxVis(is)));
		}
		return leftover / 100;
	}

	public static int addRealVis(ItemStack is, Aspect aspect, int amount, boolean doit) {
		if (!aspect.isPrimal()) {
			return 0;
		}
		int storeAmount = getVis(is, aspect) + amount;
		int leftover = Math.max(storeAmount - getMaxVis(is), 0);
		if (doit) {
			storeVis(is, aspect, Math.min(storeAmount, getMaxVis(is)));
		}
		return leftover;
	}

	public static ItemStack create(Item wandItem, String cap, String rod) {
		ItemStack s = new ItemStack(wandItem);
		s.set(TVComponents.WAND_CAP, cap);
		s.set(TVComponents.WAND_ROD, rod);
		WandRod r = WandRod.rods.get(rod);
		if (r != null && r.isStaff()) {
			// ItemWandCasting.setRod: a staff hits for 6 more
			s.set(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS, net.minecraft.world.item.component.ItemAttributeModifiers.builder()
				.add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, new net.minecraft.world.entity.ai.attributes.AttributeModifier(
					BASE_ATTACK_DAMAGE_ID, 6.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),
					net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
				.build());
		}
		return s;
	}

	public static ItemStack createFull(Item wandItem, String cap, String rod) {
		ItemStack s = create(wandItem, cap, rod);
		for (Aspect a : Aspect.getPrimalAspects()) {
			addVis(s, a, getMaxVis(s), true);
		}
		return s;
	}

	// ---------------- item behaviour ----------------

	@Override
	public Component getName(ItemStack is) {
		String rod = getRod(is).getTextureTag();
		String obj = isStaff(is) ? "item.Wand.staff.obj" : (isSceptre(is) ? "item.Wand.sceptre.obj" : "item.Wand.wand.obj");
		return Component.translatable("item.Wand.name", Component.translatable("item.Wand." + getCap(is).getTag() + ".cap"),
			Component.translatable("item.Wand." + rod + ".rod"), Component.translatable(obj));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		boolean shift = WandManager.isShiftDown();
		Player player = WandManager.clientPlayer();
		StringBuilder tt = new StringBuilder();
		int tot = 0;
		int num = 0;
		java.util.List<Component> lines = new java.util.ArrayList<>();
		AspectList vis = getAllVis(stack);
		for (Aspect aspect : Aspect.getPrimalAspects()) {
			String amount = FORMAT.format(vis.getAmount(aspect) / 100.0F);
			float mod = getConsumptionModifier(stack, player, aspect, false);
			String consumption = FORMAT.format(mod * 100.0F);
			num++;
			tot = (int) (tot + mod * 100.0F);
			Component focusCost = Component.empty();
			ItemStack fs = getFocusStack(stack);
			if (fs.getItem() instanceof FocusItem fi) {
				AspectList fc = fi.getVisCost(fs);
				int amt = fc == null ? 0 : fc.getAmount(aspect);
				if (amt > 0) {
					focusCost = Component.literal("§r, " + FORMAT.format(amt * mod / 100.0F) + " ")
						.append(Component.translatable(fi.isVisCostPerTick(fs) ? "item.Focus.cost2" : "item.Focus.cost1"));
				}
			}
			if (shift) {
				lines.add(Component.literal(" §" + aspect.getChatcolor() + aspect.getName() + "§r x " + amount + ", §o(" + consumption + "% ")
					.append(Component.translatable("tc.vis.cost")).append(")").append(focusCost));
			} else {
				if (!tt.isEmpty()) {
					tt.append(" | ");
				}
				tt.append("§").append(aspect.getChatcolor()).append(amount).append("§r");
			}
		}
		Component cap = Component.translatable("item.capacity.text").withStyle(ChatFormatting.GOLD).append(" " + getMaxVis(stack) / 100);
		if (!shift && num > 0) {
			tot /= num;
			cap = cap.copy().append(Component.literal("§r (" + tot + "% ").append(Component.translatable("tc.vis.costavg")).append(")"));
		}
		builder.accept(cap);
		if (!shift) {
			builder.accept(Component.literal(tt.toString()));
		} else {
			lines.forEach(builder);
		}
		ItemStack fs = getFocusStack(stack);
		if (fs.getItem() instanceof FocusItem fi) {
			builder.accept(fs.getHoverName().copy().withStyle(ChatFormatting.BOLD, ChatFormatting.ITALIC, ChatFormatting.GREEN));
			if (shift) {
				fi.addFocusInformation(fs, builder);
			}
		}
	}

	@Override
	public void inventoryTick(ItemStack is, ServerLevel level, Entity owner, EquipmentSlot slot) {
		if (owner instanceof Player player && getRod(is).getOnUpdate() != null) {
			getRod(is).getOnUpdate().onUpdate(is, player);
		}
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		Player player = ctx.getPlayer();
		ItemStack stack = ctx.getItemInHand();
		if (player == null) {
			return InteractionResult.PASS;
		}
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof NodeBlockEntity) {
			player.startUsingItem(ctx.getHand());
			WandManager.setObjectInUse(player, pos);
			return InteractionResult.CONSUME;
		}
		if (be instanceof dev.neothaum.api.Wandable w) {
			var hit = new net.minecraft.world.phys.BlockHitResult(ctx.getClickLocation(), ctx.getClickedFace(), pos, ctx.isInside());
			return w.onWandRightClick(level, stack, player, pos, ctx.getClickedFace(), hit) ? InteractionResult.SUCCESS : InteractionResult.PASS;
		}
		return WandManager.performTrigger(level, stack, player, pos, ctx.getClickedFace(), ctx.getHand()) ? InteractionResult.SUCCESS : InteractionResult.PASS;
	}

	/** ItemWandCasting.onItemRightClick: past any wandable block, the focus casts (once its cooldown is up). */
	@Override
	public InteractionResult use(Level level, Player player, net.minecraft.world.InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		FocusItem focus = getFocus(stack);
		if (focus != null && !WandFoci.isOnCooldown(player)) {
			WandFoci.setCooldown(player, focus.getActivationCooldown(getFocusStack(stack)));
			net.minecraft.world.phys.HitResult hit = getPlayerPOVHitResult(level, player, net.minecraft.world.level.ClipContext.Fluid.ANY);
			if (focus.onFocusRightClick(stack, level, player, hit)) {
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}

	@Override
	public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int ticksRemaining) {
		if (!(entity instanceof Player player)) {
			return;
		}
		int count = this.getUseDuration(stack, entity) - ticksRemaining;
		BlockPos pos = WandManager.getObjectInUse(player);
		if (pos != null && level.getBlockEntity(pos) instanceof NodeBlockEntity node) {
			node.onUsingWandTick(stack, player, count);
		} else if (pos == null && getFocus(stack) != null) {
			FocusItem focus = getFocus(stack);
			if (!WandFoci.isOnCooldown(player)) {
				WandFoci.setCooldown(player, focus.getActivationCooldown(getFocusStack(stack)));
				focus.onUsingFocusTick(stack, player, count);
			}
		} else {
			player.stopUsingItem();
		}
	}

	@Override
	public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
		if (entity instanceof Player player) {
			BlockPos pos = WandManager.getObjectInUse(player);
			if (pos != null && level.getBlockEntity(pos) instanceof NodeBlockEntity node) {
				node.onWandStoppedUsing(stack, level, player, remainingTime);
			} else if (pos == null && getFocus(stack) != null) {
				getFocus(stack).onPlayerStoppedUsingFocus(stack, level, player, remainingTime);
			}
			WandManager.clearObjectInUse(player);
		}
		return true;
	}

	/** ItemWandCasting.getItemUseAction: the bow pose while channelling. */
	public static FocusItem.Animation animation(ItemStack stack) {
		FocusItem f = getFocus(stack);
		return f == null ? FocusItem.Animation.WAVE : f.getAnimation(getFocusStack(stack));
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.BOW;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity user) {
		return 72000;
	}
}
