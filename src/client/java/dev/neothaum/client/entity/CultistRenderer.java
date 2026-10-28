package dev.neothaum.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.neothaum.NeoThaum;
import dev.neothaum.entity.monster.CrimsonClericEntity;
import dev.neothaum.entity.monster.CrimsonKnightEntity;
import dev.neothaum.entity.monster.CrimsonLeaderEntity;
import java.util.Random;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/**
 * Port of RenderCultist: every crimson cultist is the same robed figure (cultist.png on the 1.7 biped) under its armour, which draws its own
 * TC4 models; the praetor is a quarter larger, and a cleric performing a ritual hovers and bobs.
 */
public class CultistRenderer<T extends Mob> extends HumanoidMobRenderer<T, CultistRenderer.State, HumanoidModel<CultistRenderer.State>> {
	private static final Identifier SKIN = NeoThaum.id("textures/models/cultist.png");
	private final float scale;

	public static class State extends HumanoidRenderState {
		float bob;
	}

	public CultistRenderer(EntityRendererProvider.Context context, float scale) {
		super(context, new HumanoidModel<>(LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 32).bakeRoot()), 0.5F);
		this.scale = scale;
		this.addLayer(new HumanoidArmorLayer<>(this, ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel::new),
			context.getEquipmentRenderer()));
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(T entity, State s, float partialTicks) {
		super.extractRenderState(entity, s, partialTicks);
		s.bob = 0.0F;
		if (entity instanceof CrimsonClericEntity cleric && cleric.isRitualist()) {
			int val = new Random(entity.getId()).nextInt(1000);
			float c = entity.tickCount + partialTicks + val;
			s.bob = Mth.sin(c / 9.0F) * 0.1F + 0.21F;
		}
	}

	@Override
	public Vec3 getRenderOffset(State s) {
		return s.bob != 0.0F ? new Vec3(0.0, s.bob, 0.0) : super.getRenderOffset(s);
	}

	@Override
	public Identifier getTextureLocation(State s) {
		return SKIN;
	}

	@Override
	protected void scale(State s, PoseStack ps) {
		if (this.scale != 1.0F) {
			ps.scale(this.scale, this.scale, this.scale);
		}
	}

	public static CultistRenderer<CrimsonKnightEntity> knight(EntityRendererProvider.Context context) {
		return new CultistRenderer<>(context, 1.0F);
	}

	public static CultistRenderer<CrimsonClericEntity> cleric(EntityRendererProvider.Context context) {
		return new CultistRenderer<>(context, 1.0F);
	}

	public static CultistRenderer<CrimsonLeaderEntity> leader(EntityRendererProvider.Context context) {
		return new CultistRenderer<>(context, 1.25F);
	}
}
