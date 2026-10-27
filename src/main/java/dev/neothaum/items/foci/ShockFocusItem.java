package dev.neothaum.items.foci;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.api.wands.FocusItem;
import dev.neothaum.api.wands.FocusUpgradeType;
import dev.neothaum.entity.projectile.ShockOrb;
import dev.neothaum.fx.FX;
import dev.neothaum.items.WandItem;
import dev.neothaum.network.TVNetwork;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.wands.WandFoci;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** ItemFocusShock, the Wand Focus: Shock. Held, it arcs lightning into what it points at; upgraded, chain lightning or an earth shock orb. */
public class ShockFocusItem extends FocusItem {
	private static final AspectList COST_BASE = new AspectList().add(Aspect.AIR, 25);
	private static final AspectList COST_CHAIN = new AspectList().add(Aspect.AIR, 40).add(Aspect.WATER, 10);
	private static final AspectList COST_GROUND = new AspectList().add(Aspect.AIR, 75).add(Aspect.EARTH, 25);
	public static final FocusUpgradeType CHAINLIGHTNING = new FocusUpgradeType(17, "chainlightning", new AspectList().add(Aspect.WEATHER, 1));
	public static final FocusUpgradeType EARTHSHOCK = new FocusUpgradeType(18, "earthshock", new AspectList().add(Aspect.WEATHER, 1));

	public ShockFocusItem(Item.Properties props) {
		super(props);
	}

	@Override
	public String getSortingHelper(ItemStack focus) {
		return "BL" + super.getSortingHelper(focus);
	}

	@Override
	public int getFocusColor(ItemStack focus) {
		return 10466239;
	}

	@Override
	public AspectList getVisCost(ItemStack focus) {
		return this.isUpgradedWith(focus, CHAINLIGHTNING) ? COST_CHAIN : this.isUpgradedWith(focus, EARTHSHOCK) ? COST_GROUND : COST_BASE;
	}

	@Override
	public int getActivationCooldown(ItemStack focus) {
		return this.isUpgradedWith(focus, CHAINLIGHTNING) ? 500 : this.isUpgradedWith(focus, EARTHSHOCK) ? 1000 : 250;
	}

	@Override
	public Animation getAnimation(ItemStack focus) {
		return this.isUpgradedWith(focus, EARTHSHOCK) ? Animation.WAVE : Animation.CHARGE;
	}

	@Override
	public boolean onFocusRightClick(ItemStack wand, Level level, Player p, HitResult hit) {
		ItemStack fs = WandItem.getFocusStack(wand);
		if (this.isUpgradedWith(fs, EARTHSHOCK)) {
			if (WandItem.consumeAllVis(wand, p, this.getVisCost(fs), !level.isClientSide(), false)) {
				if (!level.isClientSide()) {
					ShockOrb orb = new ShockOrb(level, p);
					orb.area += this.getUpgradeLevel(fs, FocusUpgradeType.enlarge) * 2;
					orb.damage = (int) (orb.damage + WandItem.getFocusPotency(wand) * 1.33);
					level.addFreshEntity(orb);
					level.playSound(null, orb.getX(), orb.getY(), orb.getZ(), TVSounds.ZAP, SoundSource.PLAYERS, 1.0F,
						1.0F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F);
				}
				p.swing(FireFocusItem.handOf(p, wand), net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
			}
		} else {
			p.startUsingItem(FireFocusItem.handOf(p, wand));
			WandFoci.setCooldown(p, -1);
		}
		return true;
	}

	@Override
	public void onUsingFocusTick(ItemStack wand, Player p, int count) {
		ItemStack fs = WandItem.getFocusStack(wand);
		Level level = p.level();
		if (!WandItem.consumeAllVis(wand, p, this.getVisCost(fs), !level.isClientSide(), false)) {
			p.stopUsingItem();
			return;
		}
		int potency = WandItem.getFocusPotency(wand);
		Entity pointed = FocusUtil.pointedEntity(level, p, 0.0, 20.0, 1.1F, null);
		var r = level.getRandom();
		if (level.isClientSide()) {
			BlockHitResult mop = FocusUtil.targetBlock(level, p, 10.0);
			Vec3 v = p.getViewVector(2.0F);
			double px = p.getX() + v.x * 10.0;
			double py = p.getY() + v.y * 10.0;
			double pz = p.getZ() + v.z * 10.0;
			if (mop != null) {
				px = mop.getLocation().x;
				py = mop.getLocation().y;
				pz = mop.getLocation().z;
				for (int a = 0; a < 5; a++) {
					FX.sparkle(level, px + (r.nextFloat() - r.nextFloat()) * 0.3F, py + (r.nextFloat() - r.nextFloat()) * 0.3F,
						pz + (r.nextFloat() - r.nextFloat()) * 0.3F, 2.0F + r.nextFloat(), 2, 0.05F + r.nextFloat() * 0.05F);
				}
			}
			if (pointed != null) {
				px = pointed.getX();
				py = pointed.getBoundingBox().minY + pointed.getBbHeight() / 2.0F;
				pz = pointed.getZ();
				for (int a = 0; a < 5; a++) {
					FX.sparkle(level, px + (r.nextFloat() - r.nextFloat()) * 0.6F, py + (r.nextFloat() - r.nextFloat()) * 0.6F,
						pz + (r.nextFloat() - r.nextFloat()) * 0.6F, 2.0F + r.nextFloat(), 2, 0.05F + r.nextFloat() * 0.05F);
				}
			}
			FX.shockBolt(p, px, py, pz);
			return;
		}
		ServerLevel sl = (ServerLevel) level;
		sl.playSound(null, p.getX(), p.getY(), p.getZ(), TVSounds.SHOCK, SoundSource.PLAYERS, 0.25F, 1.0F);
		if (pointed instanceof LivingEntity && (!(pointed instanceof Player) || sl.isPvpAllowed())) {
			int cl = this.getUpgradeLevel(fs, CHAINLIGHTNING) * 2;
			pointed.hurtServer(sl, sl.damageSources().playerAttack(p), (cl > 0 ? 6 : 4) + potency);
			if (cl > 0) {
				cl += this.getUpgradeLevel(fs, FocusUpgradeType.enlarge) * 2;
				Entity center = pointed;
				List<Integer> targets = new ArrayList<>();
				targets.add(pointed.getId());
				while (cl > 0) {
					cl--;
					double d = Double.MAX_VALUE;
					Entity closest = null;
					for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, center.getBoundingBox().inflate(8.0))) {
						if (e == p || targets.contains(e.getId()) || e.distanceTo(center) > 8.0 || e instanceof Player && !sl.isPvpAllowed()) {
							continue;
						}
						double dd = e.distanceToSqr(center);
						if (dd < d) {
							closest = e;
							d = dd;
						}
					}
					if (closest != null) {
						TVNetwork.sendEntityFx(center, 4, closest.getId(), 0);
						targets.add(closest.getId());
						closest.hurtServer(sl, sl.damageSources().playerAttack(p), 4 + potency);
						center = closest;
					}
				}
			}
		}
	}

	@Override
	public boolean canApplyUpgrade(ItemStack focus, Player player, FocusUpgradeType type, int rank) {
		return !type.equals(FocusUpgradeType.enlarge) || this.isUpgradedWith(focus, CHAINLIGHTNING) || this.isUpgradedWith(focus, EARTHSHOCK);
	}

	@Override
	public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
		return switch (rank) {
			case 1, 2 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
			case 3 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, CHAINLIGHTNING, EARTHSHOCK};
			case 4, 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.enlarge};
			default -> null;
		};
	}
}
