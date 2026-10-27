package dev.neothaum.items.foci;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.api.wands.FocusItem;
import dev.neothaum.api.wands.FocusUpgradeType;
import dev.neothaum.entity.projectile.FrostShard;
import dev.neothaum.items.WandItem;
import dev.neothaum.registry.TVSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/** ItemFocusFrost, the Wand Focus: Frost. Throws a shard of ice; upgraded, a scattering of splinters or a bouncing ice boulder. */
public class FrostFocusItem extends FocusItem {
	private static final AspectList COST_BASE = new AspectList().add(Aspect.WATER, 5).add(Aspect.FIRE, 2).add(Aspect.ENTROPY, 2);
	private static final AspectList COST_SCATTER = new AspectList().add(Aspect.WATER, 20).add(Aspect.FIRE, 2).add(Aspect.ENTROPY, 2).add(Aspect.AIR, 5);
	private static final AspectList COST_BOULDER = new AspectList().add(Aspect.WATER, 20).add(Aspect.FIRE, 2).add(Aspect.ENTROPY, 2).add(Aspect.EARTH, 5);
	public static final FocusUpgradeType SCATTERSHOT = new FocusUpgradeType(11, "scattershot", new AspectList().add(Aspect.COLD, 1).add(Aspect.WEAPON, 1));
	public static final FocusUpgradeType ICEBOULDER = new FocusUpgradeType(12, "iceboulder", new AspectList().add(Aspect.COLD, 1).add(Aspect.CRYSTAL, 1));

	public FrostFocusItem(Item.Properties props) {
		super(props);
	}

	@Override
	public String getSortingHelper(ItemStack focus) {
		return "BF" + super.getSortingHelper(focus);
	}

	@Override
	public int getFocusColor(ItemStack focus) {
		return 5204428;
	}

	@Override
	public AspectList getVisCost(ItemStack focus) {
		return this.isUpgradedWith(focus, SCATTERSHOT) ? COST_SCATTER : this.isUpgradedWith(focus, ICEBOULDER) ? COST_BOULDER : COST_BASE;
	}

	@Override
	public int getActivationCooldown(ItemStack focus) {
		return this.getUpgradeLevel(focus, SCATTERSHOT) <= 0 && this.getUpgradeLevel(focus, ICEBOULDER) <= 0 ? 200 : 500;
	}

	@Override
	public boolean onFocusRightClick(ItemStack wand, Level level, Player p, HitResult hit) {
		ItemStack fs = WandItem.getFocusStack(wand);
		if (!level.isClientSide() && WandItem.consumeAllVis(wand, p, this.getVisCost(fs), true, false)) {
			int frosty = this.getUpgradeLevel(fs, FocusUpgradeType.alchemistsfrost);
			int potency = WandItem.getFocusPotency(wand);
			FrostShard shard = null;
			if (this.isUpgradedWith(fs, SCATTERSHOT)) {
				for (int a = 0; a < 5 + potency * 2; a++) {
					shard = new FrostShard(level, p, 8.0F);
					shard.setDamage(1.0F);
					shard.setFragile(true);
					shard.setFrosty(frosty);
					level.addFreshEntity(shard);
				}
			} else if (this.isUpgradedWith(fs, ICEBOULDER)) {
				shard = new FrostShard(level, p, 1.0F);
				shard.setDamage(4 + potency * 2);
				shard.bounce = 0.8;
				shard.bounceLimit = 6;
				shard.setFrosty(frosty);
				level.addFreshEntity(shard);
			} else {
				shard = new FrostShard(level, p, 1.0F);
				shard.setDamage((float) (3.0 + potency * 1.5));
				shard.setFrosty(frosty);
				level.addFreshEntity(shard);
			}
			level.playSound(null, shard.getX(), shard.getY(), shard.getZ(), TVSounds.ICE, SoundSource.PLAYERS, 0.4F,
				1.0F + level.getRandom().nextFloat() * 0.1F);
		}
		p.swing(FireFocusItem.handOf(p, wand), net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
		return true;
	}

	@Override
	public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
		return switch (rank) {
			case 1, 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.alchemistsfrost};
			case 2, 4 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
			case 3 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, SCATTERSHOT, ICEBOULDER, FocusUpgradeType.alchemistsfrost};
			default -> null;
		};
	}
}
