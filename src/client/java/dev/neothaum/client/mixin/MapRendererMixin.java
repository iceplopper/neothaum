package dev.neothaum.client.mixin;

import dev.neothaum.addon.totems.TravellersTotems;
import dev.neothaum.client.addon.minimap.TotemMapMarks;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Addon: the totems the viewer has attuned to (and their Home Totem) on map items, held or framed, as small icons among the map's own
 * markers. Drawn for the one looking only: nothing is added to the map's saved data, so other players see their own totems, not these.
 */
@Mixin(MapRenderer.class)
public abstract class MapRendererMixin {
	@Shadow
	@Final
	private TextureAtlas decorationSprites;

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void neothaum$addTotems(MapId mapId, MapItemSavedData data, MapRenderState state, CallbackInfo ci) {
		int size = 1 << data.scale;
		for (TravellersTotems.Destination m : TotemMapMarks.in(data.dimension)) {
			var p = m.pos().pos();
			// as MapItemSavedData places a banner's marker: half-pixel steps from the map's centre, and only if it is on the map
			float fx = (float) (p.getX() - data.centerX) / size;
			float fz = (float) (p.getZ() - data.centerZ) / size;
			if (fx < -63.0F || fx > 63.0F || fz < -63.0F || fz > 63.0F) {
				continue;
			}
			MapRenderState.MapDecorationRenderState d = new MapRenderState.MapDecorationRenderState();
			d.atlasSprite = this.decorationSprites.getSprite(m.home() ? TotemMapMarks.HOME_SPRITE : TotemMapMarks.TOTEM_SPRITE);
			d.x = (byte) (int) (fx * 2.0F + 0.5F);
			d.y = (byte) (int) (fz * 2.0F + 0.5F);
			// upright, as banners are
			d.rot = 8;
			d.renderOnFrame = true;
			state.decorations.add(d);
		}
	}
}
