package dev.neothaum.client.entity;

import dev.neothaum.NeoThaum;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;

/** RenderInhabitedZombie: a zombie with an eldritch crab nested in its skull; its cultist plate is drawn in the zombie_plate colours. */
public class InhabitedZombieRenderer extends ZombieRenderer {
	private static final Identifier TEXTURE = NeoThaum.id("textures/models/czombie.png");

	/** Marks the wearer for the cultist plate renderer, which swaps in zombie_plate_armor.png. */
	public static class State extends ZombieRenderState {
	}

	public InhabitedZombieRenderer(EntityRendererProvider.Context context) {
		super(context, ModelLayers.ZOMBIE, ModelLayers.ZOMBIE, ModelLayers.ZOMBIE_ARMOR, ModelLayers.ZOMBIE_ARMOR);
	}

	@Override
	public ZombieRenderState createRenderState() {
		return new State();
	}

	@Override
	public Identifier getTextureLocation(ZombieRenderState state) {
		return TEXTURE;
	}
}
