package dev.neothaum.client.addon;

import dev.neothaum.NeoThaum;
import dev.neothaum.client.render.TVRenderTypes;
import net.fabricmc.loader.api.FabricLoader;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.api.v0.IrisProgram;
import net.irisshaders.iris.api.v0.IrisShadowProgram;

/**
 * Iris shader packs draw each render pipeline with one of the pack's programs, and only know vanilla's. NeoThaum's own world pipelines
 * (TVRenderTypes) are vanilla ones with another blend, so each is given the program its vanilla original uses: the glows (eyes shader) are
 * emissive entities, the additive particles translucent particles. Without this a shader pack can lose them (the node in a jar is invisible
 * under Complementary). In the shadow pass they are drawn as lightning is: light, not something that casts a shadow.
 */
public final class IrisCompat {
	public static void init() {
		// NEOTHAUM_IRIS_UNMAPPED=1 leaves the pipelines unassigned, for comparing in the gametests
		if (!FabricLoader.getInstance().isModLoaded("iris") || System.getenv("NEOTHAUM_IRIS_UNMAPPED") != null) {
			return;
		}
		Hooks.assign();
		NeoThaum.LOGGER.info("Iris found: NeoThaum's glows and particles are given to the shader pack's programs");
	}

	/** Kept apart so that nothing loads an Iris class unless Iris is installed. */
	private static final class Hooks {
		static void assign() {
			IrisApi iris = IrisApi.getInstance();
			iris.assignPipeline(TVRenderTypes.GLOW_ADDITIVE, IrisProgram.EMISSIVE_ENTITIES);
			iris.assignPipeline(TVRenderTypes.GLOW_ADDITIVE_NODEPTH, IrisProgram.EMISSIVE_ENTITIES);
			iris.assignPipeline(TVRenderTypes.GLOW_TRANSLUCENT, IrisProgram.EMISSIVE_ENTITIES);
			iris.assignPipeline(TVRenderTypes.GLOW_TRANSLUCENT_NODEPTH, IrisProgram.EMISSIVE_ENTITIES);
			iris.assignPipeline(TVRenderTypes.PARTICLE_ADDITIVE, IrisProgram.PARTICLES_TRANSLUCENT);
			for (var p : new com.mojang.renderpearl.api.pipeline.RenderPipeline[]{TVRenderTypes.GLOW_ADDITIVE, TVRenderTypes.GLOW_ADDITIVE_NODEPTH,
				TVRenderTypes.GLOW_TRANSLUCENT, TVRenderTypes.GLOW_TRANSLUCENT_NODEPTH, TVRenderTypes.PARTICLE_ADDITIVE}) {
				iris.assignPipelineShadow(p, IrisShadowProgram.SHADOW_LIGHTNING);
			}
		}
	}

	private IrisCompat() {
	}
}
