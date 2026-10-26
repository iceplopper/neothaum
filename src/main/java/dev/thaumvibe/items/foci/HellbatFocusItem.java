package dev.thaumvibe.items.foci;

import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.api.wands.FocusItem;
import dev.thaumvibe.api.wands.FocusUpgradeType;
import dev.thaumvibe.entity.TVEntities;
import dev.thaumvibe.entity.monster.FireBat;
import dev.thaumvibe.items.WandItem;
import dev.thaumvibe.registry.TVSounds;
import dev.thaumvibe.research.ResearchManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** ItemFocusHellbat, the Wand Focus: Hellbat. Looses a summoned firebat at whatever the caster points at, as a devil, bomb or vampire bat. */
public class HellbatFocusItem extends FocusItem {
	private static final AspectList COST_BASE = new AspectList().add(Aspect.FIRE, 200).add(Aspect.ENTROPY, 100).add(Aspect.AIR, 100);
	private static final AspectList COST_BOMB = new AspectList().add(Aspect.FIRE, 100).add(Aspect.ENTROPY, 200).add(Aspect.AIR, 100);
	private static final AspectList COST_DEVIL = new AspectList().add(Aspect.FIRE, 100).add(Aspect.ENTROPY, 100).add(Aspect.AIR, 100)
		.add(Aspect.EARTH, 100);
	public static final FocusUpgradeType BATBOMBS = new FocusUpgradeType(13, "batbombs", new AspectList().add(Aspect.ENERGY, 1).add(Aspect.TRAP, 1));
	public static final FocusUpgradeType DEVILBATS = new FocusUpgradeType(14, "devilbats", new AspectList().add(Aspect.ARMOR, 1));
	public static final FocusUpgradeType VAMPIREBATS = new FocusUpgradeType(19, "vampirebats",
		new AspectList().add(Aspect.HUNGER, 1).add(Aspect.LIFE, 1));
	private static final Identifier ORNAMENT = Thaumvibe.id("items/focus_hellbat_orn");

	public HellbatFocusItem(Item.Properties props) {
		super(props);
	}

	@Override
	public String getSortingHelper(ItemStack focus) {
		return "HH" + super.getSortingHelper(focus);
	}

	@Override
	public Identifier getOrnament(ItemStack focus) {
		return ORNAMENT;
	}

	@Override
	public boolean onFocusRightClick(ItemStack wand, Level level, Player player, HitResult hit) {
		ItemStack fs = WandItem.getFocusStack(wand);
		Entity pointed = FocusUtil.pointedEntity(level, player, 0.0, 32.0, 0.0F, FireBat.class);
		double py = player.getBoundingBox().minY + player.getBbHeight() / 2.0F + 0.25 - 0.05000000014901161;
		double px = player.getX() - Mth.cos(player.getYRot() / 180.0F * (float) Math.PI) * 0.16F;
		double pz = player.getZ() - Mth.sin(player.getYRot() / 180.0F * (float) Math.PI) * 0.16F;
		Vec3 look = player.getViewVector(1.0F);
		px += look.x * 0.5;
		py += look.y * 0.5;
		pz += look.z * 0.5;
		if (pointed instanceof LivingEntity target) {
			if (level instanceof ServerLevel sl) {
				if (target instanceof Player && !sl.isPvpAllowed()) {
					return true;
				}
				FireBat bat = TVEntities.FIREBAT.create(sl, EntitySpawnReason.MOB_SUMMONED);
				if (bat == null) {
					return true;
				}
				bat.snapTo(px, py + bat.getBbHeight(), pz, player.getYRot(), 0.0F);
				bat.setTarget(target);
				bat.damBonus = WandItem.getFocusPotency(wand);
				bat.setIsSummoned(true);
				bat.setIsBatHanging(false);
				if (this.isUpgradedWith(fs, DEVILBATS)) {
					bat.setIsDevil(true);
				}
				if (this.isUpgradedWith(fs, BATBOMBS)) {
					bat.setIsExplosive(true);
				}
				if (this.isUpgradedWith(fs, VAMPIREBATS)) {
					bat.owner = player;
					bat.setIsVampire(true);
				}
				if (WandItem.consumeAllVis(wand, player, this.getVisCost(fs), true, false) && sl.addFreshEntity(bat)) {
					sl.levelEvent(2004, bat.blockPosition(), 0);
					sl.playSound(null, bat.getX(), bat.getY(), bat.getZ(), TVSounds.ICE, SoundSource.PLAYERS, 0.2F,
						0.95F + level.getRandom().nextFloat() * 0.1F);
				} else {
					sl.playSound(null, player.getX(), player.getY(), player.getZ(), TVSounds.WANDFAIL, SoundSource.PLAYERS, 0.1F,
						0.8F + level.getRandom().nextFloat() * 0.1F);
				}
			}
			player.swing(FireFocusItem.handOf(player, wand), net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
		}
		return true;
	}

	@Override
	public int getFocusColor(ItemStack focus) {
		return 14431746;
	}

	@Override
	public AspectList getVisCost(ItemStack focus) {
		return this.isUpgradedWith(focus, BATBOMBS) ? COST_BOMB : this.isUpgradedWith(focus, DEVILBATS) ? COST_DEVIL : COST_BASE;
	}

	@Override
	public int getActivationCooldown(ItemStack focus) {
		return 1000;
	}

	@Override
	public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
		return switch (rank) {
			case 1, 2, 4 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
			case 3 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, BATBOMBS, DEVILBATS};
			case 5 -> new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, VAMPIREBATS};
			default -> null;
		};
	}

	@Override
	public boolean canApplyUpgrade(ItemStack focus, Player player, FocusUpgradeType type, int rank) {
		return !type.equals(VAMPIREBATS) || ResearchManager.isResearchComplete(player, "VAMPBAT");
	}
}
