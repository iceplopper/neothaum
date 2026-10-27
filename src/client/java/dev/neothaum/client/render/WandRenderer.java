package dev.neothaum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.neothaum.NeoThaum;
import dev.neothaum.api.wands.FocusItem;
import dev.neothaum.api.wands.WandCap;
import dev.neothaum.api.wands.WandRod;
import dev.neothaum.items.WandItem;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Port of ModelWand and ItemWandRenderer. Rod, caps and the focus orb are the 1.7 boxes; staffs stretch the rod and add cap rings, sceptres a
 * third cap and a ring of spinning runes, the primal staff its rune columns. The view (inventory, held, first person, dropped) picks the
 * ItemWandRenderer transform; a held wand in use tilts forward and waves (or quivers, for charged foci).
 */
public final class WandRenderer {
	private static final Identifier ORB = NeoThaum.id("textures/models/wand.png");
	private static final Identifier SCRIPT = NeoThaum.id("textures/misc/script.png");
	private static ModelPart rod;
	private static ModelPart cap;
	private static ModelPart capBottom;
	private static ModelPart focus;

	public enum Mode { INVENTORY, EQUIPPED, FIRST_PERSON, ENTITY }

	/** Everything the renderer needs from the stack, taken when the frame is extracted. */
	public record Data(Identifier rodTex, Identifier capTex, boolean staff, boolean sceptre, boolean runes, boolean glowing, int focusColor,
		Identifier ornament, Identifier depth, float useTime, FocusItem.Animation animation) {
	}

	private static void bake() {
		if (rod != null) {
			return;
		}
		// ModelWand: the boxes are added before setTextureSize(64, 32), so they map onto the 32x32 textures
		MeshDefinition mesh = new MeshDefinition();
		var root = mesh.getRoot();
		root.addOrReplaceChild("cap", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2), PartPose.ZERO);
		root.addOrReplaceChild("cap_bottom", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2), PartPose.offset(0.0F, 20.0F, 0.0F));
		root.addOrReplaceChild("rod", CubeListBuilder.create().texOffs(0, 8).addBox(-1.0F, -1.0F, -1.0F, 2, 18, 2), PartPose.offset(0.0F, 2.0F, 0.0F));
		root.addOrReplaceChild("focus", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -6.0F, -3.0F, 6, 6, 6), PartPose.ZERO);
		ModelPart baked = LayerDefinition.create(mesh, 32, 32).bakeRoot();
		cap = baked.getChild("cap");
		capBottom = baked.getChild("cap_bottom");
		rod = baked.getChild("rod");
		focus = baked.getChild("focus");
	}

	static Data extract(ItemStack stack) {
		WandRod r = WandItem.getRod(stack);
		WandCap c = WandItem.getCap(stack);
		ItemStack fs = WandItem.getFocusStack(stack);
		FocusItem f = fs.getItem() instanceof FocusItem fi ? fi : null;
		float use = -1.0F;
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null) {
			float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
			for (Player p : mc.level.players()) {
				if (p.isUsingItem() && p.getUseItem() == stack) {
					use = p.getTicksUsingItem() + pt;
					break;
				}
			}
		}
		return new Data(r.getTexture(), c.getTexture(), r.isStaff(), WandItem.isSceptre(stack), r.hasRunes(), r.isGlowing(),
			f == null ? -1 : f.getFocusColor(fs), f == null ? null : f.getOrnament(fs), f == null ? null : f.getDepthLayer(fs), use,
			WandItem.animation(stack));
	}

	private static float ticks() {
		Minecraft mc = Minecraft.getInstance();
		return mc.player == null ? 0.0F : mc.player.tickCount + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
	}

	/** ItemWandRenderer.renderItem: the per-view placement, the in-use tilt, then the model. */
	static void submit(Mode mode, Data d, PoseStack ps, SubmitNodeCollector collector, int light) {
		bake();
		ps.pushPose();
		boolean staff = d.staff();
		if (mode == Mode.INVENTORY || mode == Mode.ENTITY) {
			// these 1.7 views drew around the item's centre; the modern item frame starts at its corner
			ps.translate(0.5F, 0.5F, 0.5F);
		}
		if (staff) {
			ps.translate(0.0F, 0.5F, 0.0F);
		}
		switch (mode) {
			case ENTITY -> {
				if (staff) {
					ps.translate(0.0F, 1.5F, 0.0F);
					ps.scale(0.9F, 0.9F, 0.9F);
				} else {
					ps.translate(0.0F, 1.0F, 0.0F);
				}
			}
			case INVENTORY -> {
				if (staff) {
					ps.scale(0.8F, 0.8F, 0.8F);
				}
				ps.rotateDegrees(Axis.ZP, 66.0F);
				ps.translate(0.0F, 0.6F, 0.0F);
				if (staff) {
					ps.translate(-0.7F, 0.6F, 0.0F);
				}
			}
			default -> {
				ps.translate(0.5F, 1.5F, 0.5F);
				if (mode == Mode.FIRST_PERSON) {
					ps.scale(1.0F, 1.1F, 1.0F);
				}
			}
		}
		ps.rotateDegrees(Axis.XP, 180.0F);
		if ((mode == Mode.EQUIPPED || mode == Mode.FIRST_PERSON) && d.useTime() >= 0.0F) {
			float t = Math.min(d.useTime(), 3.0F);
			ps.translate(0.0F, 1.0F, 0.0F);
			if (mode != Mode.FIRST_PERSON) {
				ps.rotateDegrees(Axis.ZP, 33.0F);
			} else {
				ps.rotateDegrees(Axis.XP, 10.0F);
				ps.rotateDegrees(Axis.ZP, 10.0F);
			}
			ps.rotateDegrees(Axis.XN, 60.0F * (t / 3.0F));
			if (d.animation() == FocusItem.Animation.CHARGE && d.focusColor() >= 0) {
				ps.rotateDegrees(Axis.ZP, Mth.sin(d.useTime() / 0.8F));
				ps.rotateDegrees(Axis.XP, Mth.sin(d.useTime() / 0.7F));
			} else {
				ps.rotateDegrees(Axis.ZP, Mth.sin(d.useTime() / 10.0F) * 10.0F);
				ps.rotateDegrees(Axis.XP, Mth.sin(d.useTime() / 15.0F) * 10.0F);
			}
			ps.translate(0.0F, -1.0F, 0.0F);
		}
		model(d, ps, collector, light);
		ps.popPose();
	}

	/** ModelWand.render. Each piece gets its own pose, since the collector keeps a reference to the one it was handed. */
	private static void model(Data d, PoseStack ps, SubmitNodeCollector collector, int light) {
		boolean staff = d.staff();
		float ticks = ticks();
		ps.pushPose();
		if (staff) {
			ps.translate(0.0F, 0.2F, 0.0F);
		}
		// rod
		ps.pushPose();
		if (staff) {
			ps.translate(0.0F, -0.1F, 0.0F);
			ps.scale(1.2F, 2.0F, 1.2F);
		}
		int rodLight = d.glowing() ? (int) (200.0F + Mth.sin(ticks) * 5.0F + 5.0F) : light;
		collector.submitModelPart(rod, ps, RenderTypes.entityTranslucent(d.rodTex()), rodLight, OverlayTexture.NO_OVERLAY, null);
		ps.popPose();
		// caps
		ps.pushPose();
		if (staff) {
			ps.scale(1.3F, 1.1F, 1.3F);
		} else {
			ps.scale(1.2F, 1.0F, 1.2F);
		}
		var capType = RenderTypes.entityTranslucent(d.capTex());
		if (d.sceptre()) {
			ps.pushPose();
			ps.scale(1.3F, 1.3F, 1.3F);
			collector.submitModelPart(cap, ps, capType, light, OverlayTexture.NO_OVERLAY, null);
			ps.popPose();
			ps.pushPose();
			ps.translate(0.0F, 0.3F, 0.0F);
			ps.scale(1.0F, 0.66F, 1.0F);
			collector.submitModelPart(cap, ps, capType, light, OverlayTexture.NO_OVERLAY, null);
			ps.popPose();
		} else {
			ps.pushPose();
			collector.submitModelPart(cap, ps, capType, light, OverlayTexture.NO_OVERLAY, null);
			ps.popPose();
		}
		if (staff) {
			ps.translate(0.0F, 0.225F, 0.0F);
			ps.pushPose();
			ps.scale(1.0F, 0.66F, 1.0F);
			collector.submitModelPart(cap, ps, capType, light, OverlayTexture.NO_OVERLAY, null);
			ps.popPose();
			ps.translate(0.0F, 0.65F, 0.0F);
		}
		ps.pushPose();
		collector.submitModelPart(capBottom, ps, capType, light, OverlayTexture.NO_OVERLAY, null);
		ps.popPose();
		ps.popPose();
		// focus
		if (d.focusColor() >= 0) {
			if (d.ornament() != null) {
				ornament(d.ornament(), ps, collector, light);
			}
			float alpha = 0.95F;
			if (d.depth() != null) {
				ps.pushPose();
				if (staff) {
					ps.translate(0.0F, -0.15F, 0.0F);
					ps.scale(0.165F, 0.1765F, 0.165F);
				} else {
					ps.translate(0.0F, -0.09F, 0.0F);
					ps.scale(0.16F, 0.16F, 0.16F);
				}
				depthCube(d.depth(), ps, collector, light);
				ps.popPose();
				alpha = 0.6F;
			}
			ps.pushPose();
			if (staff) {
				ps.translate(0.0F, -0.0475F, 0.0F);
				ps.scale(0.525F, 0.5525F, 0.525F);
			} else {
				ps.scale(0.5F, 0.5F, 0.5F);
			}
			int orbLight = (int) (195.0F + Mth.sin(ticks / 3.0F) * 10.0F + 10.0F);
			int argb = (int) (alpha * 255.0F) << 24 | d.focusColor() & 0xFFFFFF;
			collector.submitModelPart(focus, ps, RenderTypes.entityTranslucent(ORB), orbLight, OverlayTexture.NO_OVERLAY, null, argb);
			ps.popPose();
		}
		if (d.sceptre()) {
			for (int rot = 0; rot < 10; rot++) {
				ps.pushPose();
				ps.rotateDegrees(Axis.YP, 36 * rot + ticks);
				rune(ps, collector, 0.16, -0.01F, -0.125, rot, ticks);
				ps.popPose();
			}
		}
		if (d.runes()) {
			for (int rot = 0; rot < 4; rot++) {
				ps.pushPose();
				ps.rotateDegrees(Axis.YP, 90.0F * (rot + 1));
				for (int a = 0; a < 14; a++) {
					int rune = (a + rot * 3) % 16;
					ps.pushPose();
					rune(ps, collector, 0.36 + a * 0.14, -0.01F, -0.08, rune, ticks);
					ps.popPose();
				}
				ps.popPose();
			}
		}
		ps.popPose();
	}

	/** ModelWand.drawRune: one glyph of script.png, flickering amber, drawn additively and full bright. */
	private static void rune(PoseStack ps, SubmitNodeCollector collector, double x, double y, double z, int rune, float ticks) {
		float r = Mth.sin((ticks + rune * 5) / 5.0F) * 0.1F + 0.88F;
		float g = Mth.sin((ticks + rune * 5) / 7.0F) * 0.1F + 0.63F;
		float alpha = Mth.sin((ticks + rune * 5) / 10.0F) * 0.2F;
		ps.rotateDegrees(Axis.ZP, 90.0F);
		ps.translate(x, y, z);
		float u0 = 0.0625F * rune;
		float u1 = u0 + 0.0625F;
		float s = 0.06F + alpha / 40.0F;
		float a = alpha + 0.6F;
		collector.submitCustomGeometry(ps, TVRenderTypes.glow(SCRIPT, true, true), (pose, vc) -> {
			vc.addVertex(pose, -s, s, 0.0F).setColor(r, g, 0.2F, a).setUv(u1, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(200)
				.setNormal(pose, 0.0F, 0.0F, 1.0F);
			vc.addVertex(pose, s, s, 0.0F).setColor(r, g, 0.2F, a).setUv(u1, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(200)
				.setNormal(pose, 0.0F, 0.0F, 1.0F);
			vc.addVertex(pose, s, -s, 0.0F).setColor(r, g, 0.2F, a).setUv(u0, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(200)
				.setNormal(pose, 0.0F, 0.0F, 1.0F);
			vc.addVertex(pose, -s, -s, 0.0F).setColor(r, g, 0.2F, a).setUv(u0, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(200)
				.setNormal(pose, 0.0F, 0.0F, 1.0F);
		});
	}

	private static TextureAtlasSprite sprite(Identifier id) {
		return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS).getSprite(id);
	}

	/** The focus's animated inner layer: a small cube of the depth sprite on every face (BlockRenderer.drawFaces). */
	private static void depthCube(Identifier id, PoseStack ps, SubmitNodeCollector collector, int light) {
		TextureAtlasSprite sp = sprite(id);
		collector.submitCustomGeometry(ps, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_ITEMS), (pose, vc) -> {
			float[][] faces = {
				{-0.5F, -0.5F, -0.5F, 0.5F, -0.5F, -0.5F, 0.5F, -0.5F, 0.5F, -0.5F, -0.5F, 0.5F, 0, -1, 0},
				{-0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, -0.5F, -0.5F, 0.5F, -0.5F, 0, 1, 0},
				{-0.5F, -0.5F, 0.5F, -0.5F, 0.5F, 0.5F, -0.5F, 0.5F, -0.5F, -0.5F, -0.5F, -0.5F, -1, 0, 0},
				{0.5F, -0.5F, -0.5F, 0.5F, 0.5F, -0.5F, 0.5F, 0.5F, 0.5F, 0.5F, -0.5F, 0.5F, 1, 0, 0},
				{-0.5F, -0.5F, -0.5F, -0.5F, 0.5F, -0.5F, 0.5F, 0.5F, -0.5F, 0.5F, -0.5F, -0.5F, 0, 0, -1},
				{0.5F, -0.5F, 0.5F, 0.5F, 0.5F, 0.5F, -0.5F, 0.5F, 0.5F, -0.5F, -0.5F, 0.5F, 0, 0, 1}};
			float[][] uv = {{sp.getU0(), sp.getV1()}, {sp.getU0(), sp.getV0()}, {sp.getU1(), sp.getV0()}, {sp.getU1(), sp.getV1()}};
			for (float[] f : faces) {
				for (int i = 0; i < 4; i++) {
					vc.addVertex(pose, f[i * 3], f[i * 3 + 1], f[i * 3 + 2]).setColor(0xFFFFFFFF).setUv(uv[i][0], uv[i][1])
						.setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, f[12], f[13], f[14]);
				}
			}
		});
	}

	/** The ornament crossed over the orb: two flat cards at right angles (ItemRenderer.renderItemIn2D in TC4). */
	private static void ornament(Identifier id, PoseStack ps, SubmitNodeCollector collector, int light) {
		TextureAtlasSprite sp = sprite(id);
		for (int card = 0; card < 2; card++) {
			ps.pushPose();
			ps.rotateDegrees(Axis.XP, 180.0F);
			if (card == 1) {
				ps.rotateDegrees(Axis.YP, 90.0F);
			}
			ps.translate(-0.25F, -0.1F, 0.0275F);
			ps.scale(0.5F, 0.5F, 0.5F);
			collector.submitCustomGeometry(ps, RenderTypes.entityCutout(TextureAtlas.LOCATION_ITEMS), (pose, vc) -> {
				for (int side = 0; side < 2; side++) {
					float z = side == 0 ? 0.0F : -0.1F;
					float nz = side == 0 ? 1.0F : -1.0F;
					float[][] q = {{0, 0, sp.getU1(), sp.getV1()}, {1, 0, sp.getU0(), sp.getV1()}, {1, 1, sp.getU0(), sp.getV0()},
						{0, 1, sp.getU1(), sp.getV0()}};
					for (int i = 0; i < 4; i++) {
						float[] v = q[side == 0 ? i : 3 - i];
						vc.addVertex(pose, v[0], v[1], z).setColor(0xFFFFFFFF).setUv(v[2], v[3]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
							.setNormal(pose, 0.0F, 0.0F, nz);
					}
				}
			});
			ps.popPose();
		}
	}

	/** One view of the wand, chosen in the item model by display context. */
	public record ItemRenderer(Mode mode) implements SpecialModelRenderer<Data> {
		@Override
		public Data extractArgument(ItemStack stack) {
			return extract(stack);
		}

		@Override
		public void submit(Data data, PoseStack ps, SubmitNodeCollector collector, int light, int overlay, boolean glint, int outline) {
			if (data != null) {
				WandRenderer.submit(this.mode, data, ps, collector, light);
			}
		}

		@Override
		public void getExtents(Consumer<Vector3fc> out) {
			for (int i = 0; i < 8; i++) {
				out.accept(new Vector3f((i & 1) * 1.0F, (i >> 1 & 1) * 1.6F, (i >> 2 & 1) * 1.0F));
			}
		}
	}

	public record Unbaked(Mode mode) implements SpecialModelRenderer.Unbaked<Data> {
		public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.STRING.fieldOf("view").xmap(s -> Mode.valueOf(s.toUpperCase(java.util.Locale.ROOT)), m -> m.name().toLowerCase(java.util.Locale.ROOT))
				.forGetter(Unbaked::mode)).apply(i, Unbaked::new));

		@Override
		public SpecialModelRenderer<Data> bake(SpecialModelRenderer.BakingContext ctx) {
			return new ItemRenderer(this.mode);
		}

		@Override
		public MapCodec<Unbaked> type() {
			return CODEC;
		}
	}

	private WandRenderer() {
	}
}
