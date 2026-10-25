package dev.thaumvibe.client.fx;

import dev.thaumvibe.client.render.TVRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Base for Thaumcraft's particles (FXWisp, FXSparkle, FXBubble ...): a camera-facing quad taken straight from
 * textures/misc/particles.png by UV rectangle, full-bright, additive unless blend 771 is requested.
 */
public abstract class TCParticle extends SingleQuadParticle {
	protected float u0, v0, u1, v1;
	protected boolean additive = true;
	/** Draw from particles2.png (ParticleEngine layers 2 and 3) instead of particles.png. */
	protected boolean secondSheet;

	protected TCParticle(ClientLevel level, double x, double y, double z) {
		super(level, x, y, z, missing());
		this.hasPhysics = false;
		this.gravity = 0.0F;
		this.friction = 1.0F;
	}

	private static TextureAtlasSprite missing() {
		return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(net.minecraft.data.AtlasIds.PARTICLES).missingSprite();
	}

	/** UV rectangle in 0..1 of particles.png. */
	protected void setUV(float u0, float v0, float u1, float v1) {
		this.u0 = u0;
		this.v0 = v0;
		this.u1 = u1;
		this.v1 = v1;
	}

	@Override
	protected float getU0() {
		return this.u0;
	}

	@Override
	protected float getU1() {
		return this.u1;
	}

	@Override
	protected float getV0() {
		return this.v0;
	}

	@Override
	protected float getV1() {
		return this.v1;
	}

	@Override
	protected Layer getLayer() {
		if (this.secondSheet) {
			return this.additive ? TVRenderTypes.PARTICLE2_LAYER_ADDITIVE : TVRenderTypes.PARTICLE2_LAYER_TRANSLUCENT;
		}
		return this.additive ? TVRenderTypes.PARTICLE_LAYER_ADDITIVE : TVRenderTypes.PARTICLE_LAYER_TRANSLUCENT;
	}

	@Override
	public ParticleRenderType getGroup() {
		return ParticleRenderType.SINGLE_QUADS;
	}

	@Override
	protected int getLightCoords(float a) {
		return 0xF000F0;
	}

	protected static boolean tooFar(double x, double y, double z) {
		var cam = Minecraft.getInstance().player == null ? Vec3.ZERO : Minecraft.getInstance().player.getEyePosition();
		int visibleDistance = 50;
		return cam.distanceTo(new Vec3(x, y, z)) > visibleDistance;
	}

	protected static float clamp(double v, float lo, float hi) {
		return Mth.clamp((float) v, lo, hi);
	}
}
