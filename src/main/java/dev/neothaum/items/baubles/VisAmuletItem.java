package dev.neothaum.items.baubles;

import dev.neothaum.api.RunicArmor;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.baubles.BaubleItem;
import dev.neothaum.baubles.BaubleType;
import dev.neothaum.items.WandItem;
import dev.neothaum.registry.TVComponents;
import dev.neothaum.research.ResearchManager;
import java.text.DecimalFormat;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * ItemAmuletVis: the vis stone (25 of each primal) and the amulet of vis storage (250 of each). Worn, it tops up the wand in your hand five
 * centivis at a time; vis relays refill it. Vis is held in centivis, as wands do.
 */
public class VisAmuletItem extends Item implements BaubleItem, RunicArmor {
	private static final DecimalFormat FORMAT = new DecimalFormat("#######.##");
	private final boolean storage;

	public VisAmuletItem(boolean storage, Properties props) {
		super(props);
		this.storage = storage;
	}

	@Override
	public BaubleType getBaubleType(ItemStack stack) {
		return BaubleType.AMULET;
	}

	public int getMaxVis(ItemStack stack) {
		return this.storage ? 25000 : 2500;
	}

	public int getVis(ItemStack is, Aspect aspect) {
		AspectList al = is.get(TVComponents.WAND_VIS);
		return al == null ? 0 : al.getAmount(aspect);
	}

	public void storeVis(ItemStack is, Aspect aspect, int amount) {
		AspectList al = is.getOrDefault(TVComponents.WAND_VIS, new AspectList()).copy();
		al.aspects.put(aspect, amount);
		is.set(TVComponents.WAND_VIS, al);
	}

	public AspectList getAspectsWithRoom(ItemStack stack) {
		AspectList out = new AspectList();
		for (Aspect aspect : Aspect.getPrimalAspects()) {
			if (this.getVis(stack, aspect) < this.getMaxVis(stack)) {
				out.add(aspect, 1);
			}
		}
		return out;
	}

	public boolean consumeAllVis(ItemStack is, Player player, AspectList aspects, boolean doit, boolean crafting) {
		if (aspects == null || aspects.size() == 0) {
			return false;
		}
		for (Aspect aspect : aspects.getAspects()) {
			if (this.getVis(is, aspect) < aspects.getAmount(aspect)) {
				return false;
			}
		}
		if (doit) {
			for (Aspect aspect : aspects.getAspects()) {
				this.storeVis(is, aspect, this.getVis(is, aspect) - aspects.getAmount(aspect));
			}
		}
		return true;
	}

	/** addRealVis: in centivis; returns what did not fit. */
	public int addRealVis(ItemStack is, Aspect aspect, int amount, boolean doit) {
		if (!aspect.isPrimal()) {
			return 0;
		}
		int storeAmount = this.getVis(is, aspect) + amount;
		if (doit) {
			this.storeVis(is, aspect, Math.min(storeAmount, this.getMaxVis(is)));
		}
		return Math.max(storeAmount - this.getMaxVis(is), 0);
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity wearer) {
		if (wearer.level().isClientSide() || wearer.tickCount % 5 != 0) {
			return;
		}
		ItemStack held = wearer.getMainHandItem();
		if (held.getItem() instanceof WandItem) {
			for (Aspect aspect : WandItem.getAspectsWithRoom(held).getAspects()) {
				if (aspect != null && this.getVis(stack, aspect) > 0) {
					int amt = Math.min(5, WandItem.getMaxVis(held) - WandItem.getVis(held, aspect));
					amt = Math.min(amt, this.getVis(stack, aspect));
					this.storeVis(stack, aspect, this.getVis(stack, aspect) - amt);
					WandItem.storeVis(held, aspect, WandItem.getVis(held, aspect) + amt);
				}
			}
		}
		// TileVisRelay.nearbyPlayers: the nearest vis relay refills the amulet
		var ref = dev.neothaum.tiles.VisRelayBlockEntity.NEARBY_PLAYERS.get(wearer.getId());
		if (ref != null) {
			var relay = ref.get();
			if (relay != null && !relay.isRemoved() && relay.getBlockPos().distToCenterSqr(wearer.position()) < 26.0) {
				for (Aspect aspect : this.getAspectsWithRoom(stack).getAspects()) {
					int amt = relay.consumeVis(aspect, Math.min(5, this.getMaxVis(stack) - this.getVis(stack, aspect)));
					if (amt > 0) {
						this.addRealVis(stack, aspect, amt, true);
						relay.triggerConsumeEffect(aspect);
					}
				}
			} else {
				dev.neothaum.tiles.VisRelayBlockEntity.NEARBY_PLAYERS.remove(wearer.getId());
			}
		}
	}

	@Override
	public boolean canEquip(ItemStack stack, LivingEntity wearer) {
		return !this.storage || wearer instanceof Player p && ResearchManager.isResearchComplete(p, "VISAMULET");
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
		if (!this.storage) {
			builder.accept(Component.translatable("item.ItemAmuletVis.text").withStyle(ChatFormatting.AQUA));
		}
		builder.accept(Component.translatable("item.capacity.text").append(" " + this.getMaxVis(stack) / 100).withStyle(ChatFormatting.GOLD));
		AspectList al = stack.get(TVComponents.WAND_VIS);
		if (al != null) {
			for (Aspect aspect : Aspect.getPrimalAspects()) {
				if (al.aspects.containsKey(aspect)) {
					builder.accept(Component.literal(" " + aspect.getName() + " x " + FORMAT.format(al.getAmount(aspect) / 100.0F)));
				}
			}
		}
	}

	@Override
	public int getRunicCharge(ItemStack stack) {
		return 0;
	}
}
