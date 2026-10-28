package dev.neothaum.client;

import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.aspects.ObjectAspects;
import dev.neothaum.client.hud.Notifications;
import dev.neothaum.client.hud.ResearchPopup;
import dev.neothaum.crafting.ArcaneCraftingManager;
import dev.neothaum.crafting.ArcaneRecipe;
import dev.neothaum.fx.FX;
import dev.neothaum.network.TVNetwork;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.research.ResearchItem;
import dev.neothaum.research.ResearchManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;

/** Client halves of Thaumcraft's packets. */
public final class TVClientNetwork {
	/** Thaumcraft recipes synced from the server, keyed by id (for the Thaumonomicon and JEI). */
	public static final Map<Identifier, Recipe<?>> RECIPES = new LinkedHashMap<>();
	public static final java.util.Set<String> HIGHLIGHTED = new java.util.HashSet<>();
	private static long lastSound;

	public static void init() {
		TVNetwork.clientSender = ClientPlayNetworking::send;
		TVNetwork.clientNotifier = c -> Notifications.add(c.getString());

		ClientPlayNetworking.registerGlobalReceiver(TVNetwork.ObjectAspectsSync.TYPE, (p, ctx) -> {
			Map<Item, AspectList> t = new HashMap<>();
			p.table().forEach((id, al) -> BuiltInRegistries.ITEM.getOptional(id).ifPresent(i -> t.put(i, al)));
			ObjectAspects.setClientTable(t);
		});
		ClientPlayNetworking.registerGlobalReceiver(TVNetwork.RecipeSync.TYPE, (p, ctx) -> {
			RECIPES.clear();
			List<ArcaneRecipe> arcane = new ArrayList<>();
			for (int i = 0; i < p.ids().size(); i++) {
				RECIPES.put(p.ids().get(i), p.recipes().get(i));
				if (p.recipes().get(i) instanceof ArcaneRecipe ar) {
					arcane.add(ar);
				}
			}
			ArcaneCraftingManager.clientArcane = arcane;
			if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("jei")) {
				dev.neothaum.client.jei.NeoThaumJeiPlugin.onRecipesUpdated();
			}
		});
		ClientPlayNetworking.registerGlobalReceiver(TVNetwork.ResearchComplete.TYPE, (p, ctx) -> {
			Minecraft mc = ctx.client();
			if (p.key().startsWith("@")) {
				Notifications.add("§a" + Component.translatable("tc.addclue").getString());
				mc.player.playSound(TVSounds.LEARN, 0.2F, 1.0F + mc.level.getRandom().nextFloat() * 0.1F);
			} else {
				ResearchItem ri = ResearchManager.getResearch(p.key());
				if (ri != null && !ri.isVirtual()) {
					ResearchPopup.queue(ri);
					HIGHLIGHTED.add(p.key());
					HIGHLIGHTED.add(ri.category);
				}
			}
		});
		ClientPlayNetworking.registerGlobalReceiver(TVNetwork.AspectDiscovery.TYPE, (p, ctx) -> {
			String text = Component.translatable("tc.addaspectdiscovery").getString().replaceAll("%n", p.aspect().getName());
			Notifications.add("§6" + text, p.aspect());
			ctx.client().player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.2F, 0.5F + ctx.client().level.getRandom().nextFloat() * 0.2F);
			HIGHLIGHTED.add("ASPECTS");
			HIGHLIGHTED.add("BASICS");
		});
		ClientPlayNetworking.registerGlobalReceiver(TVNetwork.AspectPool.TYPE, (p, ctx) -> {
			if (p.amount() > 0) {
				String text = Component.translatable("tc.addaspectpool").getString().replaceAll("%s", p.amount() + "").replaceAll("%n", p.aspect().getName());
				Notifications.add(text, p.aspect());
				for (int a = 0; a < p.amount(); a++) {
					Notifications.addAspect(p.aspect());
				}
				if (System.currentTimeMillis() > lastSound) {
					ctx.client().player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.1F, 0.9F + ctx.client().level.getRandom().nextFloat() * 0.2F);
					lastSound = System.currentTimeMillis() + 100L;
				}
			}
		});
		ClientPlayNetworking.registerGlobalReceiver(TVNetwork.RunicCharge.TYPE, (p, ctx) -> dev.neothaum.client.hud.RunicShieldHud.set(p.charge(), p.max()));
		ClientPlayNetworking.registerGlobalReceiver(TVNetwork.Shield.TYPE, (p, ctx) -> dev.neothaum.client.render.ShieldFX.onPacket(p.source(), p.target()));
		ClientPlayNetworking.registerGlobalReceiver(TVNetwork.WarpMessage.TYPE, (p, ctx) -> {
			// PacketWarpMessage: a notification, and whispers when warp is gained
			if (p.amount() == 0 || p.warpType() == 2 && p.amount() < 0) {
				return;
			}
			String key = switch (p.warpType()) {
				case 0 -> p.amount() > 0 ? "tc.addwarp" : null;
				case 1 -> p.amount() > 0 ? "tc.addwarpsticky" : "tc.removewarpsticky";
				default -> "tc.addwarptemp";
			};
			if (key == null) {
				return;
			}
			if (p.amount() > 0 && p.warpType() != 2) {
				ctx.player().playSound(TVSounds.WHISPERS, 0.5F, 1.0F);
			}
			dev.neothaum.client.hud.Notifications.add(net.minecraft.network.chat.Component.translatable(key).getString());
		});
		ClientPlayNetworking.registerGlobalReceiver(TVNetwork.MiscEvent.TYPE, (p, ctx) -> {
			var player = ctx.client().player;
			if (player == null) return;
			switch (p.eventType()) {
				case 0 -> {
					player.playSound(TVSounds.HEARTBEAT, 1.0F, 1.0F);
					dev.neothaum.client.hud.WarpVisuals.heartbeat();
				}
				case 1 -> dev.neothaum.client.hud.FogFiddle.warpFog();
				case 2 -> dev.neothaum.client.hud.FogFiddle.gaze();
			}
		});
		ClientPlayNetworking.registerGlobalReceiver(TVNetwork.EntityFx.TYPE, (p, ctx) -> {
			var level = ctx.client().level;
			var src = level.getEntity(p.source());
			if (src == null) {
				return;
			}
			switch (p.kind()) {
				case 0 -> {
					var tgt = level.getEntity(p.target());
					if (tgt != null) {
						dev.neothaum.client.fx.BoltFX.bolt(src, tgt);
					}
				}
				case 4 -> {
					// PacketFXZap: chain lightning from one victim to the next
					var tgt = level.getEntity(p.target());
					if (tgt != null) {
						dev.neothaum.client.fx.BoltFX.bolt(src.getX(), src.getBoundingBox().minY + src.getBbHeight() / 2.0F, src.getZ(), tgt.getX(),
							tgt.getBoundingBox().minY + tgt.getBbHeight() / 2.0F, tgt.getZ(), 6, 0.5F, 8, 2, 0.125F);
					}
				}
				case 1 -> ctx.client().particleEngine.add(new dev.neothaum.client.fx.TCParticles.Burst(level, src.getX(), src.getY() + 0.45F, src.getZ(),
					p.value() / 100.0F));
				case 5 -> dev.neothaum.client.fx.ArcSonicFX.sonic(src);
				case 6 -> {
					// PacketFXBlockArc: from the entity to the top of a block (packed in target and value)
					var bp = net.minecraft.core.BlockPos.of((long) p.target() << 32 | p.value() & 0xFFFFFFFFL);
					var rand = level.getRandom();
					boolean portal = src instanceof dev.neothaum.entity.monster.boss.CultistPortalEntity;
					float r = portal ? 0.5F + rand.nextFloat() * 0.2F : 0.3F - rand.nextFloat() * 0.1F;
					float b = portal ? 0.0F : 0.5F + rand.nextFloat() * 0.2F;
					dev.neothaum.client.fx.ArcSonicFX.arcLightning(src.getX(), src.getBoundingBox().minY + src.getBbHeight() / 2.0F, src.getZ(),
						bp.getX() + 0.5, bp.getY() + 1, bp.getZ() + 0.5, r, 0.0F, b, 0.5F);
				}
				default -> {
				}
			}
		});
		ClientPlayNetworking.registerGlobalReceiver(TVNetwork.BlockFx.TYPE, (p, ctx) -> {
			var level = ctx.client().level;
			switch (p.kind()) {
				case 0 -> FX.blockSparkle(level, p.pos(), p.value());
				case 2 -> {
					var a = net.minecraft.world.phys.Vec3.atCenterOf(p.pos());
					var b = net.minecraft.world.phys.Vec3.atCenterOf(p.target());
					FX.wispTo(level, a.x, a.y, a.z, b.x, b.y, b.z, 0.5F, 0, true, 0.0F);
				}
				case 3 -> {
					FX.crucibleBoil(level, p.pos(), p.value());
					level.playLocalSound(p.pos(), TVSounds.SPILL, net.minecraft.sounds.SoundSource.BLOCKS, 0.2F, 1.0F, false);
					if (p.value() == 5) {
						FX.blockSparkle(level, p.pos(), -9999);
					}
				}
				case 4 -> dev.neothaum.client.fx.SourceFX.essentia(p.pos(), p.target(), p.value());
				case 6 -> {
					// PacketFXBlockBubble: a bubble rising up each side of the block
					float r = (p.value() >> 16 & 255) / 255.0F, g = (p.value() >> 8 & 255) / 255.0F, b = (p.value() & 255) / 255.0F;
					var rand = level.getRandom();
					int x = p.pos().getX(), y = p.pos().getY(), z = p.pos().getZ();
					FX.crucibleBubble(level, x, y + rand.nextFloat(), z + rand.nextFloat(), r, g, b);
					FX.crucibleBubble(level, x + 1, y + rand.nextFloat(), z + rand.nextFloat(), r, g, b);
					FX.crucibleBubble(level, x + rand.nextFloat(), y + rand.nextFloat(), z, r, g, b);
					FX.crucibleBubble(level, x + rand.nextFloat(), y + rand.nextFloat(), z + 1, r, g, b);
				}
				case 20 -> {
					// PacketFXVisDrain: a green-gold mote floats from the vis node to the machine drawing on it
					var r = level.getRandom();
					var fb = new dev.neothaum.client.fx.TCParticles.VisSparkle(level, p.target().getX() + 0.4 + r.nextFloat() * 0.2F,
						p.target().getY() + 0.4 + r.nextFloat() * 0.2F, p.target().getZ() + 0.4 + r.nextFloat() * 0.2F, p.pos().getX() + r.nextFloat(),
						p.pos().getY() + r.nextFloat(), p.pos().getZ() + r.nextFloat());
					int c = dev.neothaum.api.aspects.Aspect.getPrimalAspects().get(Math.max(0, Math.min(5, p.value()))).getColor();
					fb.tint((c >> 16 & 255) / 255.0F, (c >> 8 & 255) / 255.0F, (c & 255) / 255.0F);
					ctx.client().particleEngine.add(fb);
				}
				case 5 -> {
					if (level.getBlockEntity(p.pos()) instanceof dev.neothaum.tiles.InfusionMatrixBlockEntity m) {
						// PacketFXInfusionSource: pedestals keep flowing for 3 seconds, a player's experience for 15 ticks
						int count = level.getBlockEntity(p.target()) instanceof dev.neothaum.tiles.PedestalBlockEntity ? 60 : 15;
						m.addSourceFX(p.target(), count, p.value());
					}
				}
				default -> {
				}
			}
		});
	}

	private TVClientNetwork() {
	}
}
