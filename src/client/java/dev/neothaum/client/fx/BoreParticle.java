package dev.neothaum.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** FXBoreParticles: a fleck of an item's texture that drifts, then homes in on a target block (the infusion matrix, a bore). */
public class BoreParticle extends SingleQuadParticle {
	private static final ItemStackRenderState SCRATCH = new ItemStackRenderState();
	private final double targetX, targetY, targetZ;
	private final float uo, vo;
	private float scale;

	public BoreParticle(ClientLevel level, double x, double y, double z, double tx, double ty, double tz, ItemStack stack) {
		super(level, x, y, z, sprite(level, stack));
		this.rCol = this.gCol = this.bCol = 0.6F;
		this.scale = this.random.nextFloat() * 0.3F + 0.4F;
		this.targetX = tx;
		this.targetY = ty;
		this.targetZ = tz;
		double dx = tx - x, dy = ty - y, dz = tz - z;
		int base = Math.max(1, (int) (Math.sqrt(dx * dx + dy * dy + dz * dz) * 3.0));
		this.lifetime = base / 2 + this.random.nextInt(base);
		this.xd = this.random.nextGaussian() * 0.01;
		this.yd = this.random.nextGaussian() * 0.01;
		this.zd = this.random.nextGaussian() * 0.01;
		this.gravity = 0.0F;
		this.hasPhysics = true;
		this.uo = this.random.nextFloat() * 3.0F;
		this.vo = this.random.nextFloat() * 3.0F;
		this.quadSize = 0.1F * this.scale;
	}

	private static TextureAtlasSprite sprite(ClientLevel level, ItemStack stack) {
		Minecraft.getInstance().getItemModelResolver().updateForTopItem(SCRATCH, stack, ItemDisplayContext.GROUND, level, null, 0);
		var m = SCRATCH.pickParticleMaterial(level.getRandom());
		return m != null ? m.sprite() : Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS).missingSprite();
	}

	public BoreParticle alpha(float a) {
		this.alpha = a;
		return this;
	}

	public BoreParticle motion(double x, double y, double z) {
		this.xd = x;
		this.yd = y;
		this.zd = z;
		return this;
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age++ >= this.lifetime || Mth.floor(this.x) == Mth.floor(this.targetX) && Mth.floor(this.y) == Mth.floor(this.targetY)
			&& Mth.floor(this.z) == Mth.floor(this.targetZ)) {
			this.remove();
			return;
		}
		this.move(this.xd, this.yd, this.zd);
		this.xd *= 0.985;
		this.yd *= 0.985;
		this.zd *= 0.985;
		double dx = this.targetX - this.x, dy = this.targetY - this.y, dz = this.targetZ - this.z;
		double pull = 0.3;
		double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (d < 4.0) {
			this.scale *= 0.9F;
			this.quadSize = 0.1F * this.scale;
			pull = 0.6;
		}
		this.xd = Mth.clamp(this.xd + dx / d * pull, -0.35, 0.35);
		this.yd = Mth.clamp(this.yd + dy / d * pull, -0.35, 0.35);
		this.zd = Mth.clamp(this.zd + dz / d * pull, -0.35, 0.35);
	}

	// a quarter of the sprite, as EntityDiggingFX did
	@Override
	protected float getU0() {
		return this.sprite.getU((this.uo + 1.0F) / 4.0F);
	}

	@Override
	protected float getU1() {
		return this.sprite.getU(this.uo / 4.0F);
	}

	@Override
	protected float getV0() {
		return this.sprite.getV(this.vo / 4.0F);
	}

	@Override
	protected float getV1() {
		return this.sprite.getV((this.vo + 1.0F) / 4.0F);
	}

	@Override
	protected Layer getLayer() {
		return Layer.bySprite(this.sprite);
	}

	@Override
	public ParticleRenderType getGroup() {
		return ParticleRenderType.SINGLE_QUADS;
	}
}
