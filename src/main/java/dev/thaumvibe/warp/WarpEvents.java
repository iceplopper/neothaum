package dev.thaumvibe.warp;

import dev.thaumvibe.api.WarpingGear;
import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.baubles.Baubles;
import dev.thaumvibe.entity.TVEntities;
import dev.thaumvibe.entity.monster.EldritchGuardianEntity;
import dev.thaumvibe.entity.monster.MindSpiderEntity;
import dev.thaumvibe.items.equipment.FortressArmorItem;
import dev.thaumvibe.network.TVNetwork;
import dev.thaumvibe.registry.TVComponents;
import dev.thaumvibe.registry.TVMobEffects;
import dev.thaumvibe.registry.TVSounds;
import dev.thaumvibe.research.PlayerKnowledge;
import dev.thaumvibe.research.ResearchManager;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 1:1 Port of TC4 WarpEvents.
 * Handles periodic warp event rolls, hallucinations, status effects, Eldritch milestones,
 * and temporary warp decay.
 */
public final class WarpEvents {

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (player.isAlive()) {
					if (player.tickCount > 0 && player.tickCount % 2000 == 0 && !player.hasEffect(TVMobEffects.WARP_WARD)) {
						if (dev.thaumvibe.config.ThaumvibeConfig.current.allowWarp) {
							checkWarpEvent(player);
						}
					}
					if (player.tickCount % 10 == 0 && player.hasEffect(TVMobEffects.DEATH_GAZE)) {
						checkDeathGaze(player);
					}
				}
			}
		});
	}

	public static int getWarpFromGear(Player player) {
		int w = 0;
		if (player.getMainHandItem().getItem() instanceof WarpingGear wg) {
			w += wg.getWarp(player.getMainHandItem(), player);
		}
		if (player.getOffhandItem().getItem() instanceof WarpingGear wg) {
			w += wg.getWarp(player.getOffhandItem(), player);
		}
		for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
			ItemStack armor = player.getItemBySlot(slot);
			if (!armor.isEmpty() && armor.getItem() instanceof WarpingGear wg) {
				w += wg.getWarp(armor, player);
			}
		}
		for (ItemStack bauble : Baubles.get(player).items()) {
			if (!bauble.isEmpty() && bauble.getItem() instanceof WarpingGear wg) {
				w += wg.getWarp(bauble, player);
			}
		}
		return w;
	}

	public static void checkWarpEvent(ServerPlayer player) {
		PlayerKnowledge k = ResearchManager.knowledge(player);
		int warp = k.warp + k.warpSticky + k.warpTemp;
		int actualwarp = k.warp + k.warpSticky;
		warp += getWarpFromGear(player);
		int warpCounter = k.warpCounter;

		int r = player.level().getRandom().nextInt(100);
		if (warpCounter > 0 && warp > 0 && r <= Math.sqrt(warpCounter)) {
			warp = Math.min(100, (warp + warp + warpCounter) / 3);
			warpCounter = (int) (warpCounter - Math.max(5.0, Math.sqrt(warpCounter) * 2.0));

			PlayerKnowledge newK = k.copy();
			newK.warpCounter = warpCounter;
			ResearchManager.setKnowledge(player, newK);

			int eff = player.level().getRandom().nextInt(warp);

			// Check Fortress Helm mask (Grinning Devil mask reduces warp effect severity)
			ItemStack helm = player.getItemBySlot(EquipmentSlot.HEAD);
			if (helm != null && helm.getItem() instanceof FortressArmorItem && helm.has(TVComponents.MASK)) {
				Integer mask = helm.get(TVComponents.MASK);
				if (mask != null && mask == 0) {
					eff -= 2 + player.level().getRandom().nextInt(4);
				}
			}

			// Send heartbeat packet
			ServerPlayNetworking.send(player, new TVNetwork.MiscEvent((short) 0));

			if (eff > 0) {
				if (eff <= 4) {
					grantResearch(player, 1);
					player.sendSystemMessage(Component.literal("§5§o" + Component.translatable("warp.text.3").getString()));
				} else if (eff <= 8) {
					// Harmless tension noise
				} else if (eff <= 12) {
					player.sendSystemMessage(Component.literal("§5§o" + Component.translatable("warp.text.11").getString()));
					player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
						TVSounds.CAMERACLACK, SoundSource.AMBIENT, 0.8F, 0.6F);
				} else if (eff <= 16) {
					player.addEffect(new MobEffectInstance(TVMobEffects.VIS_EXHAUST, 5000, Math.min(3, warp / 15)));
					player.sendSystemMessage(Component.literal("§5§o" + Component.translatable("warp.text.1").getString()));
				} else if (eff <= 20) {
					player.addEffect(new MobEffectInstance(TVMobEffects.THAUMARHIA, Math.min(32000, 10 * warp), 0));
					player.sendSystemMessage(Component.literal("§5§o" + Component.translatable("warp.text.15").getString()));
				} else if (eff <= 24) {
					player.addEffect(new MobEffectInstance(TVMobEffects.UNNATURAL_HUNGER, 5000, Math.min(3, warp / 15)));
					player.sendSystemMessage(Component.literal("§5§o" + Component.translatable("warp.text.2").getString()));
				} else if (eff <= 28) {
					player.sendSystemMessage(Component.literal("§5§o" + Component.translatable("warp.text.12").getString()));
					player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
						TVSounds.WIND, SoundSource.AMBIENT, 0.8F, 0.5F);
				} else if (eff <= 32) {
					spawnMist(player, warp, 1);
				} else if (eff <= 36) {
					player.addEffect(new MobEffectInstance(TVMobEffects.BLURRED_VISION, Math.min(32000, 10 * warp), 0));
				} else if (eff <= 40) {
					player.addEffect(new MobEffectInstance(TVMobEffects.SUN_SCORNED, 5000, Math.min(3, warp / 15)));
					player.sendSystemMessage(Component.literal("§5§o" + Component.translatable("warp.text.5").getString()));
				} else if (eff <= 44) {
					player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 1200, Math.min(3, warp / 15)));
					player.sendSystemMessage(Component.literal("§5§o" + Component.translatable("warp.text.9").getString()));
				} else if (eff <= 48) {
					player.addEffect(new MobEffectInstance(TVMobEffects.INFECTIOUS_VIS_EXHAUST, 6000, Math.min(3, warp / 15)));
					player.sendSystemMessage(Component.literal("§5§o" + Component.translatable("warp.text.1").getString()));
				} else if (eff <= 52) {
					player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, Math.min(40 * warp, 6000), 0));
					player.sendSystemMessage(Component.literal("§5§o" + Component.translatable("warp.text.10").getString()));
				} else if (eff <= 56) {
					player.addEffect(new MobEffectInstance(TVMobEffects.DEATH_GAZE, 6000, Math.min(3, warp / 15)));
					player.sendSystemMessage(Component.literal("§5§o" + Component.translatable("warp.text.4").getString()));
				} else if (eff <= 60) {
					suddenlySpiders(player, warp, false);
				} else if (eff <= 64) {
					player.sendSystemMessage(Component.literal("§5§o" + Component.translatable("warp.text.13").getString()));
					player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
						TVSounds.WHISPERS, SoundSource.AMBIENT, 0.7F, 0.8F);
				} else if (eff <= 68) {
					spawnMist(player, warp, Math.max(1, warp / 30));
				} else if (eff <= 72) {
					player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, Math.min(32000, 5 * warp), 0));
				} else if (eff == 76) {
					if (k.warpSticky > 0) {
						Warp.addStickyWarpToPlayer(player, -1);
					}
					player.sendSystemMessage(Component.literal("§5§o" + Component.translatable("warp.text.14").getString()));
				} else if (eff <= 80) {
					player.addEffect(new MobEffectInstance(TVMobEffects.UNNATURAL_HUNGER, 6000, Math.min(3, warp / 15)));
					player.sendSystemMessage(Component.literal("§5§o" + Component.translatable("warp.text.2").getString()));
				} else if (eff <= 84) {
					grantResearch(player, Math.max(1, warp / 10));
					player.sendSystemMessage(Component.literal("§5§o" + Component.translatable("warp.text.3").getString()));
				} else if (eff <= 92) {
					suddenlySpiders(player, warp, true);
				} else {
					spawnMist(player, warp, Math.max(1, warp / 15));
				}
			}

			// Eldritch Research Milestones
			if (actualwarp > 10 && !k.research.contains("ELDRITCHMINOR")) {
				grantResearch(player, 5);
				ResearchManager.completeResearch(player, "ELDRITCHMINOR");
				ServerPlayNetworking.send(player, new TVNetwork.ResearchComplete("ELDRITCHMINOR"));
			}
			if (actualwarp > 25 && !k.research.contains("ELDRITCHWARPED")) {
				grantResearch(player, 10);
				ResearchManager.completeResearch(player, "ELDRITCHWARPED");
				ServerPlayNetworking.send(player, new TVNetwork.ResearchComplete("ELDRITCHWARPED"));
			}
			if (actualwarp > 50 && !k.research.contains("ELDRITCHMAJOR")) {
				grantResearch(player, 20);
				ResearchManager.completeResearch(player, "ELDRITCHMAJOR");
				ServerPlayNetworking.send(player, new TVNetwork.ResearchComplete("ELDRITCHMAJOR"));
			}
		}

		// Passive temporary warp decay: -1 temporary warp every 2000 ticks
		Warp.addWarpToPlayer(player, -1, true);
	}

	public static void checkDeathGaze(ServerPlayer player) {
		MobEffectInstance pe = player.getEffect(TVMobEffects.DEATH_GAZE);
		if (pe == null) return;
		int level = pe.getAmplifier();
		int range = Math.min(8 + level * 3, 24);
		AABB box = player.getBoundingBox().inflate(range);
		List<LivingEntity> list = player.level().getEntitiesOfClass(LivingEntity.class, box,
			e -> e != player && e.isAlive() && !e.hasEffect(MobEffects.WITHER));

		Vec3 look = player.getViewVector(1.0F).normalize();
		for (LivingEntity target : list) {
			Vec3 toTarget = target.position().subtract(player.position()).normalize();
			double dot = look.dot(toTarget);
			if (dot > 0.75 && player.hasLineOfSight(target)) {
				target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0));
			}
		}
	}

	private static void grantResearch(Player player, int times) {
		int amt = 1 + player.level().getRandom().nextInt(Math.max(1, times));
		for (int a = 0; a < amt; a++) {
			Aspect aspect = Aspect.getPrimalAspects().get(player.level().getRandom().nextInt(6));
			PlayerKnowledge k = ResearchManager.knowledge(player).copy();
			if (k.addAspectPool(aspect, 1)) {
				ResearchManager.setKnowledge(player, k);
				if (player instanceof ServerPlayer sp) {
					TVNetwork.sendAspectPool(sp, aspect, 1, k.getAspectPool(aspect));
				}
			}
		}
	}

	private static void suddenlySpiders(ServerPlayer player, int warp, boolean real) {
		int spawns = Math.min(50, Math.max(2, warp / 5));
		ServerLevel level = (ServerLevel) player.level();
		BlockPos origin = player.blockPosition();

		for (int a = 0; a < spawns; a++) {
			MindSpiderEntity spider = new MindSpiderEntity(TVEntities.MIND_SPIDER, level);
			boolean success = false;
			for (int l = 0; l < 50; l++) {
				int dx = Mth.randomBetweenInclusive(player.getRandom(), 7, 24) * (player.getRandom().nextBoolean() ? 1 : -1);
				int dy = Mth.randomBetweenInclusive(player.getRandom(), -3, 3);
				int dz = Mth.randomBetweenInclusive(player.getRandom(), 7, 24) * (player.getRandom().nextBoolean() ? 1 : -1);
				BlockPos pos = origin.offset(dx, dy, dz);
				if (level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isSolidRender()) {
					spider.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getRandom().nextFloat() * 360.0F, 0.0F);
					if (level.noCollision(spider)) {
						success = true;
						break;
					}
				}
			}

			if (success) {
				spider.setTarget(player);
				if (!real) {
					spider.setViewer(player.getName().getString());
					spider.setHarmless(true);
				}
				level.addFreshEntity(spider);
			}
		}

		player.sendSystemMessage(Component.literal("§5§o" + Component.translatable("warp.text.7").getString()));
	}

	private static void spawnMist(ServerPlayer player, int warp, int guardians) {
		ServerPlayNetworking.send(player, new TVNetwork.MiscEvent((short) 1));
		if (guardians > 0) {
			guardians = Math.min(4, guardians);
			ServerLevel level = (ServerLevel) player.level();
			BlockPos origin = player.blockPosition();

			for (int a = 0; a < guardians; a++) {
				EldritchGuardianEntity eg = new EldritchGuardianEntity(TVEntities.ELDRITCH_GUARDIAN, level);
				for (int l = 0; l < 50; l++) {
					int dx = Mth.randomBetweenInclusive(player.getRandom(), 7, 24) * (player.getRandom().nextBoolean() ? 1 : -1);
					int dy = Mth.randomBetweenInclusive(player.getRandom(), -2, 2);
					int dz = Mth.randomBetweenInclusive(player.getRandom(), 7, 24) * (player.getRandom().nextBoolean() ? 1 : -1);
					BlockPos pos = origin.offset(dx, dy, dz);
					if (level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isSolidRender()) {
						eg.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getRandom().nextFloat() * 360.0F, 0.0F);
						if (level.noCollision(eg)) {
							eg.setTarget(player);
							level.addFreshEntity(eg);
							break;
						}
					}
				}
			}
		}

		player.sendSystemMessage(Component.literal("§5§o" + Component.translatable("warp.text.6").getString()));
	}

	private WarpEvents() {
	}
}
