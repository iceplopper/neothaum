package dev.thaumvibe.items.foci;

import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.api.wands.FocusItem;
import dev.thaumvibe.api.wands.FocusUpgradeType;
import dev.thaumvibe.entity.projectile.PechBlast;
import dev.thaumvibe.items.WandItem;
import dev.thaumvibe.registry.TVSounds;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/** ItemFocusPech, the Pech's focus: the pech mages' sickening blast, with the nightshade upgrade adding every ill at once. */
public class PechFocusItem extends FocusItem {
	private static final AspectList COST = new AspectList().add(Aspect.EARTH, 10).add(Aspect.ENTROPY, 10).add(Aspect.WATER, 10);
	private static final AspectList COST_ALL = new AspectList().add(Aspect.AIR, 10).add(Aspect.FIRE, 10).add(Aspect.EARTH, 10).add(Aspect.ORDER, 10)
		.add(Aspect.ENTROPY, 10).add(Aspect.WATER, 10);
	public static final FocusUpgradeType NIGHTSHADE = new FocusUpgradeType(15, "nightshade",
		new AspectList().add(Aspect.LIFE, 1).add(Aspect.POISON, 1).add(Aspect.MAGIC, 1));
	private static final Identifier DEPTH = Thaumvibe.id("items/focus_pech_depth");

	public PechFocusItem(Item.Properties props) {
		super(props);
	}

	@Override
	public String getSortingHelper(ItemStack focus) {
		return "PP" + super.getSortingHelper(focus);
	}

	@Override
	public Identifier getDepthLayer(ItemStack focus) {
		return DEPTH;
	}

	@Override
	public int getActivationCooldown(ItemStack focus) {
		return 250;
	}

	@Override
	public boolean onFocusRightClick(ItemStack wand, Level level, Player p, HitResult hit) {
		ItemStack fs = WandItem.getFocusStack(wand);
		if (!level.isClientSide() && WandItem.consumeAllVis(wand, p, this.getVisCost(fs), true, false)) {
			PechBlast blast = new PechBlast(level, p, WandItem.getFocusPotency(wand), WandItem.getFocusExtend(wand), this.isUpgradedWith(fs, NIGHTSHADE));
			blast.shootFromRotation(p, p.getXRot(), p.getYRot(), 0.0F, 1.5F, 1.0F);
			level.addFreshEntity(blast);
			level.playSound(null, blast.getX(), blast.getY(), blast.getZ(), TVSounds.ICE, SoundSource.PLAYERS, 0.4F,
				1.0F + level.getRandom().nextFloat() * 0.1F);
		}
		p.swing(FireFocusItem.handOf(p, wand), net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
		return true;
	}

	@Override
	public int getFocusColor(ItemStack focus) {
		return 2267460;
	}

	@Override
	public AspectList getVisCost(ItemStack focus) {
		return this.isUpgradedWith(focus, NIGHTSHADE) ? COST_ALL : COST;
	}

	@Override
	public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
		return switch (rank) {
			case 1, 3 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
			case 2, 4 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.extend};
			case 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, NIGHTSHADE};
			default -> null;
		};
	}
}
