package dev.thaumvibe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.client.TVClientNetwork;
import dev.thaumvibe.crafting.CrucibleRecipe;
import dev.thaumvibe.tiles.ThaumatoriumBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Port of TileThaumatoriumRenderer:
 * Renders the 2-block tall Thaumatorium OBJ model oriented to facing, and displays
 * the rotating/cycling output item of the active recipes on the front plate.
 */
public class ThaumatoriumRenderer implements BlockEntityRenderer<ThaumatoriumBlockEntity, ThaumatoriumRenderer.State> {
	private static final Identifier MODEL = Thaumvibe.id("textures/models/thaumatorium.obj");
	private static final Identifier TEXTURE = Thaumvibe.id("textures/models/thaumatorium.png");

	private static ObjModel model;
	private final ItemModelResolver itemModelResolver;

	public static class State extends BlockEntityRenderState {
		Direction facing = Direction.NORTH;
		final ItemStackRenderState item = new ItemStackRenderState();
		boolean hasItem;
	}

	public ThaumatoriumRenderer(BlockEntityRendererProvider.Context ctx) {
		this.itemModelResolver = ctx.itemModelResolver();
	}

	private static ObjModel model() {
		if (model == null) {
			model = ObjModel.load(MODEL);
		}
		return model;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(ThaumatoriumBlockEntity be, State s, float pt, Vec3 cam, ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, s, pt, cam, crumbling);
		s.facing = be.getFacing();

		if (!be.selectedRecipes.isEmpty() && be.getLevel() != null) {
			int idx = (int) (be.getLevel().getGameTime() / 40) % be.selectedRecipes.size();
			Identifier id = be.selectedRecipes.get(idx);
			if (TVClientNetwork.RECIPES.get(id) instanceof CrucibleRecipe recipe) {
				ItemStack out = recipe.result().create();
				this.itemModelResolver.updateForTopItem(s.item, out, ItemDisplayContext.FIXED, be.getLevel(), null, 0);
				s.hasItem = true;
				return;
			}
		}
		s.item.clear();
		s.hasItem = false;
	}

	/** Model rotation around Z in OBJ space (TC4: NORTH -> 270, SOUTH -> 90, WEST -> 0, EAST -> 180). */
	private static float modelRotation(Direction facing) {
		return switch (facing) {
			case NORTH -> 270.0F;
			case SOUTH -> 90.0F;
			case EAST -> 180.0F;
			default -> 0.0F;
		};
	}

	/** Item rotation around Y on the front face. */
	private static float itemRotation(Direction facing) {
		return switch (facing) {
			case NORTH -> 180.0F;
			case WEST -> 270.0F;
			case EAST -> 90.0F;
			default -> 0.0F;
		};
	}

	@Override
	public void submit(State s, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
		// Render 3D OBJ Machine Model
		ps.pushPose();
		ps.translate(0.5F, 0.0F, 0.5F);
		ps.rotateDegrees(Axis.XP, -90.0F);
		ps.rotateDegrees(Axis.ZP, modelRotation(s.facing));

		collector.submitCustomGeometry(ps, RenderTypes.entityCutout(TEXTURE), (pose, vc) -> {
			model().render(pose, vc, s.lightCoords, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
		});
		ps.popPose();

		// Render Front Plate Display Item
		if (s.hasItem && !s.item.isEmpty()) {
			ps.pushPose();
			ps.translate(0.5F + s.facing.getStepX() / 1.99F, 1.325F, 0.5F + s.facing.getStepZ() / 1.99F);
			ps.rotateDegrees(Axis.YP, itemRotation(s.facing));
			ps.scale(0.75F, 0.75F, 0.75F);
			s.item.submit(ps, collector, s.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			ps.popPose();
		}
	}
}
