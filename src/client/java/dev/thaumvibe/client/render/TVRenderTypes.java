package dev.thaumvibe.client.render;

import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import dev.thaumvibe.Thaumvibe;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

/**
 * Render pipelines reproducing Thaumcraft's GL states: GL_SRC_ALPHA/GL_ONE additive glows and GL_SRC_ALPHA/GL_ONE_MINUS_SRC_ALPHA,
 * with depth writes off and optionally depth testing off (nodes seen through goggles).
 */
public final class TVRenderTypes {
	public static final Identifier PARTICLES = Thaumvibe.id("textures/misc/particles.png");
	public static final Identifier NODES = Thaumvibe.id("textures/misc/nodes.png");
	public static final Identifier WISPY = Thaumvibe.id("textures/misc/wispy.png");

	private static RenderPipeline glow(String name, BlendFunction blend, boolean depthTest) {
		return RenderPipelines.register(RenderPipeline.builder(RenderPipelines.ENTITY_NO_LIGHTMAP_SNIPPET, RenderPipelines.EYES_SNIPPET)
			.withLocation(Thaumvibe.id("pipeline/" + name))
			.withColorTargetState(new ColorTargetState(blend))
			.withDepthStencilState(new DepthStencilState(depthTest ? CompareOp.GREATER_THAN_OR_EQUAL : CompareOp.ALWAYS_PASS, false))
			.withCull(false)
			.build());
	}

	public static final RenderPipeline GLOW_ADDITIVE = glow("glow_additive", BlendFunction.LIGHTNING, true);
	public static final RenderPipeline GLOW_ADDITIVE_NODEPTH = glow("glow_additive_nodepth", BlendFunction.LIGHTNING, false);
	public static final RenderPipeline GLOW_TRANSLUCENT = glow("glow_translucent", BlendFunction.TRANSLUCENT, true);
	public static final RenderPipeline GLOW_TRANSLUCENT_NODEPTH = glow("glow_translucent_nodepth", BlendFunction.TRANSLUCENT, false);

	public static final RenderPipeline PARTICLE_ADDITIVE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
		.withLocation(Thaumvibe.id("pipeline/particle_additive"))
		.withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
		.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
		.build());

	/** ParticleEngine layer 0/1: particles.png drawn additively, and normally for blend 771 particles. */
	public static final SingleQuadParticle.Layer PARTICLE_LAYER_ADDITIVE = new SingleQuadParticle.Layer(true, PARTICLES, PARTICLE_ADDITIVE);
	public static final SingleQuadParticle.Layer PARTICLE_LAYER_TRANSLUCENT = new SingleQuadParticle.Layer(true, PARTICLES,
		RenderPipelines.TRANSLUCENT_PARTICLE);

	/** ParticleEngine layers 2 and 3: particles2.png (sparks), additive and normal. */
	/** A plain white texture, for untextured glows (the primal orb's rays). */
	public static final Identifier WHITE = Identifier.withDefaultNamespace("textures/misc/white.png");
	public static final Identifier PARTICLES2 = Thaumvibe.id("textures/misc/particles2.png");
	public static final SingleQuadParticle.Layer PARTICLE2_LAYER_ADDITIVE = new SingleQuadParticle.Layer(true, PARTICLES2, PARTICLE_ADDITIVE);
	public static final SingleQuadParticle.Layer PARTICLE2_LAYER_TRANSLUCENT = new SingleQuadParticle.Layer(true, PARTICLES2,
		RenderPipelines.TRANSLUCENT_PARTICLE);

	private static final Map<String, RenderType> CACHE = new HashMap<>();

	/** A textured, full-bright, depth-write-free render type for TC glows. */
	public static RenderType glow(Identifier texture, boolean additive, boolean depthTest) {
		String key = texture + "|" + additive + "|" + depthTest;
		return CACHE.computeIfAbsent(key, k -> {
			RenderPipeline p = additive ? (depthTest ? GLOW_ADDITIVE : GLOW_ADDITIVE_NODEPTH) : (depthTest ? GLOW_TRANSLUCENT : GLOW_TRANSLUCENT_NODEPTH);
			return RenderType.create("thaumvibe_glow", RenderSetup.builder(p).withTexture("Sampler0", texture).sortOnUpload().createRenderSetup());
		});
	}

	public static void init() {
	}

	private TVRenderTypes() {
	}
}
