package dev.thaumvibe.client.entity;

import dev.thaumvibe.entity.TVEntities;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

/** ClientProxy.registerDisplayInformation: the creatures' renderers. */
public final class TVEntityRenderers {
	public static void init() {
		EntityRendererRegistry.register(TVEntities.BRAINY_ZOMBIE, BrainyZombieRenderer::new);
		EntityRendererRegistry.register(TVEntities.GIANT_BRAINY_ZOMBIE, BrainyZombieRenderer::new);
		EntityRendererRegistry.register(TVEntities.WISP, WispRenderer::new);
		EntityRendererRegistry.register(TVEntities.FIREBAT, FireBatRenderer::new);
		EntityRendererRegistry.register(TVEntities.THAUMIC_SLIME, ThaumicSlimeRenderer::new);
		EntityRendererRegistry.register(TVEntities.PECH, PechRenderer::new);
		// RenderPechBlast draws nothing but its wisps
		EntityRendererRegistry.register(TVEntities.PECH_BLAST, NoopRenderer::new);
		EntityRendererRegistry.register(TVEntities.EMBER, FocusProjectileRenderers.EmberRenderer::new);
		EntityRendererRegistry.register(TVEntities.EXPLOSIVE_ORB, FocusProjectileRenderers.ExplosiveOrbRenderer::new);
		EntityRendererRegistry.register(TVEntities.FROST_SHARD, FocusProjectileRenderers.FrostShardRenderer::new);
		EntityRendererRegistry.register(TVEntities.SHOCK_ORB, FocusProjectileRenderers.ShockOrbRenderer::new);
		EntityRendererRegistry.register(TVEntities.PRIMAL_ORB, FocusProjectileRenderers.PrimalOrbRenderer::new);
		EntityRendererRegistry.register(TVEntities.PRIMAL_ARROW, dev.thaumvibe.client.render.PrimalArrowRenderer::new);
		EntityRendererRegistry.register(TVEntities.BOTTLE_TAINT, ThrownItemRenderer::new);
		EntityRendererRegistry.register(TVEntities.ALUMENTUM, ThrownItemRenderer::new);
		EntityRendererRegistry.register(TVEntities.TAINT_SPIDER, TaintSpiderRenderer::new);
		EntityRendererRegistry.register(TVEntities.TAINT_SPORE, TaintSporeRenderer::new);
		EntityRendererRegistry.register(TVEntities.TAINT_SPORE_SWARMER, TaintSporeRenderer::new);
		// RenderTaintSwarm draws nothing: the swarm is all particles
		EntityRendererRegistry.register(TVEntities.TAINT_SWARM, NoopRenderer::new);
		EntityRendererRegistry.register(TVEntities.TAINTACLE, TaintacleRenderer::normal);
		EntityRendererRegistry.register(TVEntities.TAINTACLE_SMALL, TaintacleRenderer::small);
		EntityRendererRegistry.register(TVEntities.TAINTACLE_GIANT, TaintacleRenderer::giant);
		EntityRendererRegistry.register(TVEntities.TAINTED_CHICKEN, TaintedAnimalRenderers.ChickenRenderer::new);
		EntityRendererRegistry.register(TVEntities.TAINTED_COW, TaintedAnimalRenderers.Cow::new);
		EntityRendererRegistry.register(TVEntities.TAINTED_CREEPER, TaintedAnimalRenderers.Creeper::new);
		EntityRendererRegistry.register(TVEntities.TAINTED_PIG, TaintedAnimalRenderers.Pig::new);
		EntityRendererRegistry.register(TVEntities.TAINTED_SHEEP, TaintedAnimalRenderers.Sheep::new);
		EntityRendererRegistry.register(TVEntities.TAINTED_VILLAGER, TaintedAnimalRenderers.VillagerRenderer::new);
		EntityRendererRegistry.register(TVEntities.GOLEM, GolemRenderer::new);
		EntityRendererRegistry.register(TVEntities.TRAVELING_TRUNK, TravelingTrunkRenderer::new);
		EntityRendererRegistry.register(TVEntities.MIND_SPIDER, MindSpiderRenderer::new);
		EntityRendererRegistry.register(TVEntities.ELDRITCH_GUARDIAN, EldritchGuardianRenderer::new);
		EntityRendererRegistry.register(TVEntities.GOLEM_ORB, FocusProjectileRenderers.GolemOrbRenderer::new);
		EntityRendererRegistry.register(TVEntities.CULTIST_KNIGHT, CultistRenderer::knight);
		EntityRendererRegistry.register(TVEntities.CULTIST_CLERIC, CultistRenderer::cleric);
		EntityRendererRegistry.register(TVEntities.CULTIST_LEADER, CultistRenderer::leader);
		EntityRendererRegistry.register(TVEntities.ELDRITCH_CRAB, EldritchCrabRenderer::new);
		EntityRendererRegistry.register(TVEntities.INHABITED_ZOMBIE, net.minecraft.client.renderer.entity.ZombieRenderer::new);
		EntityRendererRegistry.register(TVEntities.ELDRITCH_WARDEN, EldritchWardenRenderer::new);
		EntityRendererRegistry.register(TVEntities.ELDRITCH_GOLEM, EldritchGolemRenderer::new);
		EntityRendererRegistry.register(TVEntities.ASPECT_ORB, AspectOrbRenderer::new);
		EntityRendererRegistry.register(TVEntities.CULTIST_PORTAL, CultistPortalRenderer::new);
	}

	private TVEntityRenderers() {
	}
}
