package dev.thaumvibe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.aspects.ObjectAspects;
import dev.thaumvibe.items.ThaumometerItem;
import dev.thaumvibe.research.ScanManager;
import dev.thaumvibe.tiles.NodeBlockEntity;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/** Port of ItemThaumometerRenderer: the scanner.obj body, its glowing screen and (in first person) the scan readout on that screen. */
public final class ThaumometerRenderer {
	private static final Identifier MODEL = Thaumvibe.id("textures/models/scanner.obj");
	private static final Identifier TEXTURE = Thaumvibe.id("textures/models/scanner.png");
	private static final Identifier SCREEN = Thaumvibe.id("textures/models/scanscreen.png");
	private static ObjModel model;

	private static ObjModel model() {
		if (model == null) {
			model = ObjModel.load(MODEL);
		}
		return model;
	}

	/** Called on resource reload so a changed OBJ is picked up. */
	public static void reset() {
		model = null;
	}

	private static int flicker(Player p, int offset) {
		int ticks = p == null ? 0 : p.tickCount;
		int jitter = p == null ? 0 : p.level().getRandom().nextInt(2);
		return (int) (190.0F + Mth.sin(offset + ticks - jitter) * 10.0F + 10.0F);
	}

	/** model.renderAll() followed by the scan screen; draws the readout when {@code readout} is set (first person). */
	public static void draw(PoseStack ps, SubmitNodeCollector collector, int light, boolean readout) {
		Player player = Minecraft.getInstance().player;
		collector.submitCustomGeometry(ps, RenderTypes.entityTranslucent(TEXTURE),
			(pose, vc) -> model().render(pose, vc, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
		ps.pushPose();
		ps.translate(0.0F, 0.11F, 0.0F);
		ps.rotateDegrees(Axis.XP, 90.0F);
		ps.rotateDegrees(Axis.ZP, 90.0F);
		// renderQuadCenteredFromTexture scales the matrix and leaves it scaled for the readout
		ps.scale(2.5F, 2.5F, 2.5F);
		int screenLight = flicker(player, 0);
		collector.submitCustomGeometry(ps, RenderTypes.entityTranslucent(SCREEN), (pose, vc) -> {
			float[][] v = {{-0.5F, 0.5F, 0, 1}, {0.5F, 0.5F, 1, 1}, {0.5F, -0.5F, 1, 0}, {-0.5F, -0.5F, 0, 0}};
			for (int side = 0; side < 1; side++) { // both render types draw back faces too; a second copy z-fights
				for (int i = 0; i < 4; i++) {
					float[] q = v[side == 0 ? i : 3 - i];
					vc.addVertex(pose, q[0], q[1], 0.0F).setColor(0xFFFFFFFF).setUv(q[2], q[3]).setOverlay(OverlayTexture.NO_OVERLAY)
						.setLight(screenLight).setNormal(pose, 0.0F, 0.0F, side == 0 ? -1.0F : 1.0F);
				}
			}
		});
		if (readout && player != null) {
			drawReadout(ps, collector, player);
		}
		ps.popPose();
	}

	private static void drawReadout(PoseStack ps, SubmitNodeCollector collector, Player player) {
		Minecraft mc = Minecraft.getInstance();
		Font font = mc.font;
		Component name = null;
		AspectList aspects = null;
		Entity target = ThaumometerItem.getPointedEntity(mc.level, player, 10.0);
		if (target != null) {
			ScanManager.ScanResult sr = ScanManager.ScanResult.entity(target);
			name = target instanceof ItemEntity ie ? ie.getItem().getHoverName() : target.getDisplayName();
			if (ScanManager.hasBeenScanned(player, sr)) {
				aspects = ScanManager.getScanAspects(sr, mc.level);
			}
		} else if (ThaumometerItem.pickBlock(mc.level, player) instanceof BlockHitResult bhr) {
			var pos = bhr.getBlockPos();
			if (mc.level.getBlockEntity(pos) instanceof NodeBlockEntity node) {
				ScanManager.ScanResult sr = ScanManager.ScanResult.node(pos);
				if (ScanManager.hasBeenScanned(player, sr)) {
					aspects = node.getAspects();
					String t = Component.translatable("nodetype." + node.getNodeType().name() + ".name").getString();
					if (node.getNodeModifier() != null) {
						t = t + ", " + Component.translatable("nodemod." + node.getNodeModifier().name() + ".name").getString();
					}
					ps.pushPose();
					ps.translate(0.0F, 0.0F, -0.01F);
					ps.scale(0.004F, 0.004F, 0.004F);
					text(ps, collector, font, Component.literal(t), -font.width(t) / 2.0F, -40.0F, 0xFFEEAE16);
					ps.popPose();
				}
				name = Component.translatable("block.thaumvibe.aura_node");
			} else if (mc.level.getBlockEntity(pos) instanceof dev.thaumvibe.tiles.JarNodeBlockEntity jarNode) {
				ScanManager.ScanResult sr = ScanManager.ScanResult.node(pos);
				if (ScanManager.hasBeenScanned(player, sr)) {
					aspects = jarNode.getAspects();
					String t = Component.translatable("nodetype." + jarNode.getNodeType().name() + ".name").getString();
					if (jarNode.getNodeModifier() != null) {
						t = t + ", " + Component.translatable("nodemod." + jarNode.getNodeModifier().name() + ".name").getString();
					}
					ps.pushPose();
					ps.translate(0.0F, 0.0F, -0.01F);
					ps.scale(0.004F, 0.004F, 0.004F);
					text(ps, collector, font, Component.literal(t), -font.width(t) / 2.0F, -40.0F, 0xFFEEAE16);
					ps.popPose();
				}
				name = Component.translatable("block.thaumvibe.node_in_a_jar");
			} else {
				var block = mc.level.getBlockState(pos).getBlock();
				var item = mc.level.getBlockState(pos).getCloneItemStack(mc.level, pos, false).getItem();
				if (item == Items.AIR && ObjectAspects.getBlockAspects(block) != null) {
					ScanManager.ScanResult sr = ScanManager.ScanResult.block(block);
					name = block.getName();
					if (ScanManager.hasBeenScanned(player, sr)) {
						aspects = ScanManager.getScanAspects(sr, mc.level);
					}
				} else if (item != Items.AIR) {
					ScanManager.ScanResult sr = ScanManager.ScanResult.item(item);
					name = item.getName(new ItemStack(item));
					if (ScanManager.hasBeenScanned(player, sr)) {
						aspects = ScanManager.getScanAspects(sr, mc.level);
					}
				}
			}
		}
		if (name == null) {
			return;
		}
		ps.translate(0.0F, 0.0F, -0.01F);
		if (aspects != null) {
			int posX = 0;
			int posY = 0;
			int aa = aspects.size();
			int baseX = Math.min(5, aa) * 8;
			for (Aspect aspect : aspects.getAspectsSorted()) {
				ps.pushPose();
				ps.scale(0.0075F, 0.0075F, 0.0075F);
				drawTag(ps, collector, font, -baseX + posX * 16, -8 + posY * 16, aspect, aspects.getAmount(aspect), flicker(player, posX));
				ps.popPose();
				if (++posX >= 5 - posY) {
					posX = 0;
					posY++;
					aa -= 5 - posY;
					baseX = Math.min(5 - posY, aa) * 8;
				}
			}
		}
		ps.pushPose();
		ps.translate(0.0F, -0.25F, 0.0F);
		int sw = font.width(name);
		float scale = 0.005F;
		if (sw > 90) {
			scale -= 2.5E-5F * (sw - 90);
		}
		ps.scale(scale, scale, scale);
		text(ps, collector, font, name, -sw / 2.0F, 0.0F, 0xFFFFFFFF);
		ps.popPose();
	}

	/** UtilsFX.drawTag with blend GL_ONE: the additive aspect icon plus its amount in half-size text. */
	private static void drawTag(PoseStack ps, SubmitNodeCollector collector, Font font, float x, float y, Aspect aspect, int amount, int light) {
		int rgb = aspect.getColor();
		collector.submitCustomGeometry(ps, TVRenderTypes.glow(aspect.getImage(), true, true), (pose, vc) -> {
			float[][] v = {{x, y + 16, 0, 1}, {x + 16, y + 16, 1, 1}, {x + 16, y, 1, 0}, {x, y, 0, 0}};
			for (int side = 0; side < 1; side++) { // both render types draw back faces too; a second copy z-fights
				for (int i = 0; i < 4; i++) {
					float[] q = v[side == 0 ? i : 3 - i];
					vc.addVertex(pose, q[0], q[1], 0.0F).setColor(0xFF000000 | rgb).setUv(q[2], q[3]).setOverlay(OverlayTexture.NO_OVERLAY)
						.setLight(light).setNormal(pose, 0.0F, 0.0F, -1.0F);
				}
			}
		});
		if (amount > 0) {
			ps.pushPose();
			ps.translate(0.0F, 0.0F, -0.01F);
			ps.scale(0.5F, 0.5F, 0.5F);
			String am = Integer.toString(amount);
			text(ps, collector, font, Component.literal(am), 32 - font.width(am) + x * 2, 32 - font.lineHeight + y * 2, 0xFFFFFFFF);
			ps.popPose();
		}
	}

	private static void text(PoseStack ps, SubmitNodeCollector collector, Font font, Component text, float x, float y, int argb) {
		collector.submitText(ps, x, y, text.getVisualOrderText(), false, Font.DisplayMode.POLYGON_OFFSET, 0xF000F0, argb, 0, 0);
	}

	/** The inventory, third person, dropped and item-frame views (IItemRenderer types other than first person). */
	public record ItemRenderer() implements NoDataSpecialModelRenderer {
		@Override
		public void submit(PoseStack ps, SubmitNodeCollector collector, int light, int overlay, boolean glint, int outline) {
			ps.pushPose();
			ps.translate(0.5F, 0.5F, 0.5F);
			ps.scale(0.5F, 0.5F, 0.5F);
			draw(ps, collector, light, false);
			ps.popPose();
		}

		@Override
		public void getExtents(Consumer<Vector3fc> out) {
			for (int i = 0; i < 8; i++) {
				out.accept(new Vector3f(-0.2F + (i & 1) * 1.4F, 0.45F + (i >> 1 & 1) * 0.1F, -0.2F + (i >> 2 & 1) * 1.4F));
			}
		}
	}

	public record Unbaked() implements SpecialModelRenderer.Unbaked<Void> {
		public static final MapCodec<Unbaked> CODEC = MapCodec.unit(new Unbaked());

		@Override
		public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext ctx) {
			reset();
			return new ItemRenderer();
		}

		@Override
		public MapCodec<Unbaked> type() {
			return CODEC;
		}
	}

	private ThaumometerRenderer() {
	}
}
