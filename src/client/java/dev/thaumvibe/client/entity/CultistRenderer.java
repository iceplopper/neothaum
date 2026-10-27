package dev.thaumvibe.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.entity.monster.CrimsonClericEntity;
import dev.thaumvibe.entity.monster.CrimsonKnightEntity;
import dev.thaumvibe.entity.monster.CrimsonLeaderEntity;
import dev.thaumvibe.entity.monster.CultistEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

/**
 * CultistRenderer: renders Crimson Cultists (Knights, Clerics, and Praetor Leaders).
 */
public class CultistRenderer<T extends net.minecraft.world.entity.Mob> extends HumanoidMobRenderer<T, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {
	private static final Identifier TEX_KNIGHT = Thaumvibe.id("textures/entity/equipment/humanoid/cultist_plate.png");
	private static final Identifier TEX_CLERIC = Thaumvibe.id("textures/entity/equipment/humanoid/cultist_robe.png");
	private static final Identifier TEX_LEADER = Thaumvibe.id("textures/entity/equipment/humanoid/cultist_leader.png");

	private final Identifier texture;
	private final float scale;

	public CultistRenderer(EntityRendererProvider.Context context, Identifier texture, float scale) {
		super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F * scale);
		this.texture = texture;
		this.scale = scale;
		this.addLayer(new HumanoidArmorLayer<>(this,
			ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel::new),
			context.getEquipmentRenderer()));
	}

	@Override
	public HumanoidRenderState createRenderState() {
		return new HumanoidRenderState();
	}

	@Override
	public Identifier getTextureLocation(HumanoidRenderState state) {
		return this.texture;
	}

	@Override
	protected void scale(HumanoidRenderState state, PoseStack ps) {
		if (this.scale != 1.0F) {
			ps.scale(this.scale, this.scale, this.scale);
		}
	}

	public static CultistRenderer<CrimsonKnightEntity> knight(EntityRendererProvider.Context context) {
		return new CultistRenderer<>(context, TEX_KNIGHT, 1.0F);
	}

	public static CultistRenderer<CrimsonClericEntity> cleric(EntityRendererProvider.Context context) {
		return new CultistRenderer<>(context, TEX_CLERIC, 1.0F);
	}

	public static CultistRenderer<CrimsonLeaderEntity> leader(EntityRendererProvider.Context context) {
		return new CultistRenderer<>(context, TEX_LEADER, 1.25F);
	}
}
