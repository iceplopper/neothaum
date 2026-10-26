package dev.thaumvibe.items.foci;

import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.api.wands.FocusItem;
import dev.thaumvibe.api.wands.FocusUpgradeType;
import dev.thaumvibe.entity.projectile.PrimalOrb;
import dev.thaumvibe.items.WandItem;
import dev.thaumvibe.registry.TVSounds;
import java.util.Random;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/** ItemFocusPrimal, the Primal Focus: a wandering orb of all six primals whose price shifts every fifth of a second. */
public class PrimalFocusItem extends FocusItem {
	public static final FocusUpgradeType SEEKER = new FocusUpgradeType(16, "seeker", new AspectList().add(Aspect.SENSES, 1).add(Aspect.MIND, 1));
	private static final Identifier DEPTH = Thaumvibe.id("items/focus_primal_depth");

	public PrimalFocusItem(Item.Properties props) {
		super(props);
	}

	@Override
	public String getSortingHelper(ItemStack focus) {
		return "FP" + super.getSortingHelper(focus);
	}

	@Override
	public Identifier getDepthLayer(ItemStack focus) {
		return DEPTH;
	}

	@Override
	public int getActivationCooldown(ItemStack focus) {
		return 500;
	}

	@Override
	public boolean onFocusRightClick(ItemStack wand, Level level, Player p, HitResult hit) {
		ItemStack fs = WandItem.getFocusStack(wand);
		if (!level.isClientSide() && WandItem.consumeAllVis(wand, p, this.getVisCost(fs), true, false)) {
			PrimalOrb orb = new PrimalOrb(level, p, this.isUpgradedWith(fs, SEEKER));
			level.addFreshEntity(orb);
			level.playSound(null, orb.getX(), orb.getY(), orb.getZ(), TVSounds.ICE, SoundSource.PLAYERS, 0.3F,
				0.8F + level.getRandom().nextFloat() * 0.1F);
		}
		p.swing(FireFocusItem.handOf(p, wand), net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
		return true;
	}

	@Override
	public int getFocusColor(ItemStack focus) {
		return 10854849;
	}

	@Override
	public AspectList getVisCost(ItemStack focus) {
		Random rand = new Random(System.currentTimeMillis() / 200L);
		return new AspectList().add(Aspect.WATER, 50 + rand.nextInt(5) * 50).add(Aspect.AIR, 50 + rand.nextInt(5) * 50)
			.add(Aspect.EARTH, 50 + rand.nextInt(5) * 50).add(Aspect.FIRE, 50 + rand.nextInt(5) * 50).add(Aspect.ORDER, 50 + rand.nextInt(5) * 50)
			.add(Aspect.ENTROPY, 50 + rand.nextInt(5) * 50);
	}

	@Override
	public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
		return switch (rank) {
			case 1, 2, 4, 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal};
			case 3 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, SEEKER};
			default -> null;
		};
	}
}
