package dev.neothaum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.neothaum.items.BannerItem;
import dev.neothaum.registry.TVComponents;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Inventory renderers for the devices TC4 drew through their tile renderers in hand and in the inventory (renderInventoryBlock): the arcane
 * bore and its base, the flux scrubber and the banners. Which one is drawn comes from the item definition's "device" field.
 */
public final class DeviceItemRenderers {
	public record ItemRenderer(String device) implements SpecialModelRenderer<Integer> {
		@Override
		public Integer extractArgument(ItemStack stack) {
			if (stack.getItem() instanceof BannerItem) {
				return this.device.equals("cultist_banner") ? -1 : stack.getOrDefault(TVComponents.BANNER_COLOR, -1);
			}
			return 0;
		}

		@Override
		public void submit(Integer data, PoseStack ps, SubmitNodeCollector collector, int light, int overlay, boolean glint, int outline) {
			var p = Minecraft.getInstance().player;
			float time = p == null ? 0.0F : p.tickCount;
			switch (this.device) {
				case "arcane_bore_base" -> ArcaneBoreBaseRenderer.draw(ps, collector, Direction.EAST, light);
				case "arcane_bore" -> {
					ArcaneBoreRenderer.State s = new ArcaneBoreRenderer.State();
					s.orientation = Direction.EAST;
					ArcaneBoreRenderer.draw(ps, collector, s, light);
				}
				case "flux_scrubber" -> FluxScrubberRenderer.draw(ps, collector, Direction.UP, time, light);
				case "banner", "cultist_banner" -> {
					int color = data == null ? -1 : data;
					BannerRenderer.draw(ps, collector, color, color == -1, false, 0.0F, time, light);
				}
				case "eldritch_cap", "eldritch_altar" -> {
					ps.pushPose();
					ps.translate(0.5F, 0.0F, 0.5F);
					ps.rotateDegrees(com.mojang.math.Axis.XP, -90.0F);
					EldritchObeliskRenderer.cap(ps, collector, this.device.equals("eldritch_altar") ? EldritchObeliskRenderer.CAP_ALTAR
						: EldritchObeliskRenderer.CAP, light);
					ps.popPose();
				}
				case "eldritch_obelisk" -> {
					ps.pushPose();
					ps.translate(0.0F, -1.0F, 0.0F);
					EldritchObeliskRenderer.draw(ps, collector, 0.0F, light);
					ps.popPose();
				}
				default -> {
				}
			}
		}

		@Override
		public void getExtents(Consumer<Vector3fc> out) {
			for (int i = 0; i < 8; i++) {
				out.accept(new Vector3f(i & 1, (i >> 1 & 1) * 2.0F, i >> 2 & 1));
			}
		}
	}

	public record Unbaked(String device) implements SpecialModelRenderer.Unbaked<Integer> {
		public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(Codec.STRING.fieldOf("device").forGetter(Unbaked::device))
			.apply(i, Unbaked::new));

		@Override
		public SpecialModelRenderer<Integer> bake(SpecialModelRenderer.BakingContext ctx) {
			return new ItemRenderer(this.device);
		}

		@Override
		public MapCodec<Unbaked> type() {
			return CODEC;
		}
	}

	private DeviceItemRenderers() {
	}
}
