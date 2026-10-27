package dev.neothaum.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.neothaum.NeoThaum;
import dev.neothaum.entity.monster.GiantBrainyZombie;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.zombie.Zombie;

/** RenderBrainyZombie: a zombie with its brain showing; the furious kind is drawn at the size of its anger. */
public class BrainyZombieRenderer extends ZombieRenderer {
	private static final Identifier TEXTURE = NeoThaum.id("textures/models/bzombie.png");

	public static class State extends ZombieRenderState {
		float anger = 1.0F;
		boolean giant;
	}

	public BrainyZombieRenderer(EntityRendererProvider.Context context) {
		// 26.3 draws baby zombies with a redesigned model that the 1.7 texture does not fit; a baby here is the adult model at half size
		super(context, ModelLayers.ZOMBIE, ModelLayers.ZOMBIE, ModelLayers.ZOMBIE_ARMOR, ModelLayers.ZOMBIE_ARMOR);
	}

	@Override
	public ZombieRenderState createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(Zombie entity, ZombieRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		if (state instanceof State s) {
			s.giant = entity instanceof GiantBrainyZombie;
			s.anger = entity instanceof GiantBrainyZombie g ? g.getAnger() : 1.0F;
		}
	}

	@Override
	protected void scale(ZombieRenderState state, PoseStack ps) {
		if (state instanceof State s && s.giant) {
			ps.scale(s.anger, s.anger, s.anger);
		}
		if (state.isBaby) {
			ps.scale(0.5F, 0.5F, 0.5F);
		}
	}

	@Override
	public Identifier getTextureLocation(ZombieRenderState state) {
		return TEXTURE;
	}
}
