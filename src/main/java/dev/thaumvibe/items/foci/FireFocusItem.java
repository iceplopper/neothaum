package dev.thaumvibe.items.foci;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.api.wands.FocusItem;
import dev.thaumvibe.api.wands.FocusUpgradeType;
import dev.thaumvibe.entity.projectile.Ember;
import dev.thaumvibe.entity.projectile.ExplosiveOrb;
import dev.thaumvibe.items.WandItem;
import dev.thaumvibe.registry.TVSounds;
import dev.thaumvibe.wands.WandFoci;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/** ItemFocusFire, the Wand Focus: Fire. Held, it sprays embers; upgraded, a fireball or a tight firebeam. */
public class FireFocusItem extends FocusItem {
	private static final AspectList COST_BASE = new AspectList().add(Aspect.FIRE, 10);
	private static final AspectList COST_BEAM = new AspectList().add(Aspect.FIRE, 10).add(Aspect.ORDER, 3);
	private static final AspectList COST_BALL = new AspectList().add(Aspect.FIRE, 66).add(Aspect.ENTROPY, 33);
	public static final FocusUpgradeType FIREBALL = new FocusUpgradeType(9, "fireball", new AspectList().add(Aspect.DARKNESS, 1));
	public static final FocusUpgradeType FIREBEAM = new FocusUpgradeType(10, "firebeam", new AspectList().add(Aspect.ENERGY, 1).add(Aspect.AIR, 1));
	private static final Map<Integer, Long> SOUND_DELAY = new ConcurrentHashMap<>();

	public FireFocusItem(Item.Properties props) {
		super(props);
	}

	static InteractionHand handOf(Player p, ItemStack wand) {
		return p.getOffhandItem() == wand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
	}

	@Override
	public String getSortingHelper(ItemStack focus) {
		return "AF" + super.getSortingHelper(focus);
	}

	@Override
	public int getFocusColor(ItemStack focus) {
		return 15028484;
	}

	@Override
	public AspectList getVisCost(ItemStack focus) {
		return this.isUpgradedWith(focus, FIREBEAM) ? COST_BEAM : this.isUpgradedWith(focus, FIREBALL) ? COST_BALL : COST_BASE;
	}

	@Override
	public int getActivationCooldown(ItemStack focus) {
		return this.isUpgradedWith(focus, FIREBALL) ? 1000 : 0;
	}

	@Override
	public boolean isVisCostPerTick(ItemStack focus) {
		return true;
	}

	@Override
	public Animation getAnimation(ItemStack focus) {
		return this.isUpgradedWith(focus, FIREBALL) ? Animation.WAVE : Animation.CHARGE;
	}

	@Override
	public boolean onFocusRightClick(ItemStack wand, Level level, Player p, HitResult hit) {
		ItemStack fs = WandItem.getFocusStack(wand);
		if (this.isUpgradedWith(fs, FIREBALL)) {
			if (WandItem.consumeAllVis(wand, p, this.getVisCost(fs), !level.isClientSide(), false)) {
				if (!level.isClientSide()) {
					ExplosiveOrb orb = new ExplosiveOrb(level, p);
					orb.strength += WandItem.getFocusPotency(wand) * 0.4F;
					orb.onFire = this.isUpgradedWith(fs, FocusUpgradeType.alchemistsfire);
					level.addFreshEntity(orb);
					level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.GHAST_SHOOT, SoundSource.PLAYERS, 1.0F,
						(level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F + 1.0F);
				}
				p.swing(handOf(p, wand), net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
			}
		} else {
			p.startUsingItem(handOf(p, wand));
			WandFoci.setCooldown(p, -1);
		}
		return true;
	}

	@Override
	public void onUsingFocusTick(ItemStack wand, Player p, int count) {
		ItemStack fs = WandItem.getFocusStack(wand);
		if (!WandItem.consumeAllVis(wand, p, this.getVisCost(fs), false, false)) {
			p.stopUsingItem();
			return;
		}
		Level level = p.level();
		if (!level.isClientSide() && SOUND_DELAY.getOrDefault(p.getId(), 0L) < System.currentTimeMillis()) {
			level.playSound(null, p.getX(), p.getY(), p.getZ(), TVSounds.FIRELOOP, SoundSource.PLAYERS, 0.33F, 2.0F);
			SOUND_DELAY.put(p.getId(), System.currentTimeMillis() + 500L);
		}
		if (!level.isClientSide() && WandItem.consumeAllVis(wand, p, this.getVisCost(fs), true, false)) {
			boolean beam = this.isUpgradedWith(fs, FIREBEAM);
			float scatter = beam ? 0.25F : 15.0F;
			int potency = WandItem.getFocusPotency(wand);
			for (int a = 0; a < 2 + potency; a++) {
				Ember orb = new Ember(level, p, scatter);
				orb.damage = 2 + potency;
				if (beam) {
					orb.damage += 0.5F;
					orb.damage *= 1.5F;
					orb.setDuration(30);
				}
				orb.firey = this.getUpgradeLevel(fs, FocusUpgradeType.alchemistsfire);
				orb.setPos(orb.position().add(orb.getDeltaMovement()));
				level.addFreshEntity(orb);
			}
		}
	}

	@Override
	public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
		return switch (rank) {
			case 1, 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
			case 2, 4 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.alchemistsfire};
			case 3 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FIREBALL, FIREBEAM};
			default -> null;
		};
	}

	@Override
	public boolean canApplyUpgrade(ItemStack focus, Player player, FocusUpgradeType type, int rank) {
		return !type.equals(FocusUpgradeType.alchemistsfire) || !this.isUpgradedWith(focus, FIREBALL)
			|| !this.isUpgradedWith(focus, FocusUpgradeType.alchemistsfire);
	}
}
