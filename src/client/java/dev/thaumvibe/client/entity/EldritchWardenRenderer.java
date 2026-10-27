package dev.thaumvibe.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.entity.monster.EldritchWardenEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/**
 * EldritchWardenRenderer: colossal boss version of the Eldritch Guardian.
 */
public class EldritchWardenRenderer extends MobRenderer<EldritchWardenEntity, EldritchGuardianRenderer.State, EldritchGuardianRenderer.GuardianModel> {
	private static final Identifier TEXTURE = Thaumvibe.id("textures/entity/eldritch_guardian.png");

	public EldritchWardenRenderer(EntityRendererProvider.Context context) {
		super(context, new EldritchGuardianRenderer.GuardianModel(EldritchGuardianRenderer.GuardianModel.createMesh().bakeRoot()), 0.75F);
	}

	@Override
	public EldritchGuardianRenderer.State createRenderState() {
		return new EldritchGuardianRenderer.State();
	}

	@Override
	public Identifier getTextureLocation(EldritchGuardianRenderer.State state) {
		return TEXTURE;
	}

	@Override
	protected void scale(EldritchGuardianRenderer.State state, PoseStack ps) {
		ps.scale(1.5F, 1.5F, 1.5F);
	}
}
