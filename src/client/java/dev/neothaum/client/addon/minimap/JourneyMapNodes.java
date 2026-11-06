package dev.neothaum.client.addon.minimap;

import dev.neothaum.NeoThaum;
import dev.neothaum.config.NeoThaumConfig;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.display.MarkerOverlay;
import journeymap.api.v2.client.model.MapImage;
import journeymap.api.v2.common.Context;
import journeymap.api.v2.common.JourneyMapPlugin;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

/**
 * Addon: aura nodes on JourneyMap's fullscreen map, as on Xaero's World Map (WorldMapNodes keeps what has been seen): only nodes the player
 * has scanned and then seen through Goggles of Revealing, never on the minimap. Each is a marker in its main aspect's colour, named on hover.
 * The same setting as Xaero's map (worldMapNodes) shows or hides them. Loaded by JourneyMap through the "journeymap" entrypoint.
 */
@JourneyMapPlugin(apiVersion = "2.0.0")
public final class JourneyMapNodes implements IClientPlugin {
	private static final Identifier ICON = NeoThaum.id("textures/misc/journeymap_node.png");
	private IClientAPI api;
	/** "dimension|x,y,z" -> the marker shown for it */
	private final Map<String, MarkerOverlay> shown = new HashMap<>();
	private int tick;

	@Override
	public String getModId() {
		return NeoThaum.MOD_ID;
	}

	@Override
	public void initialize(IClientAPI api) {
		this.api = api;
		NeoThaum.LOGGER.info("JourneyMap detected - aura nodes you have seen will be shown on its fullscreen map.");
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			if (++this.tick % 40 == 0) {
				this.sync(mc);
			}
		});
	}

	/** Shows a marker for each node WorldMapNodes has for this dimension, and takes away any no longer wanted. */
	private void sync(Minecraft mc) {
		Set<String> wanted = new HashSet<>();
		if (mc.level != null && mc.player != null && NeoThaumConfig.current.worldMapNodes) {
			var dim = mc.level.dimension();
			for (WorldMapNodes.Seen s : WorldMapNodes.seenIn(dim)) {
				String key = dim.identifier() + "|" + s.x() + "," + s.y() + "," + s.z();
				wanted.add(key);
				if (this.shown.containsKey(key)) {
					continue;
				}
				MapImage icon = new MapImage(ICON, 16, 16).setColor(WorldMapNodes.color(s)).setDisplayWidth(12).setDisplayHeight(12).centerAnchors();
				MarkerOverlay marker = new MarkerOverlay(NeoThaum.MOD_ID, new BlockPos(s.x(), s.y(), s.z()), icon);
				marker.setDimension(dim).setTitle(WorldMapNodes.name(s)).setActiveUIs(Context.UI.Fullscreen);
				try {
					this.api.show(marker);
					this.shown.put(key, marker);
				} catch (Exception e) {
					NeoThaum.LOGGER.warn("JourneyMap would not show a node marker", e);
				}
			}
		}
		this.shown.entrySet().removeIf(e -> {
			if (wanted.contains(e.getKey())) {
				return false;
			}
			this.api.remove(e.getValue());
			return true;
		});
	}
}
