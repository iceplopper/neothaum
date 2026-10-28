package dev.neothaum.client.addon.minimap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.neothaum.NeoThaum;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.nodes.NodeModifier;
import dev.neothaum.api.nodes.NodeType;
import dev.neothaum.config.NeoThaumConfig;
import dev.neothaum.items.GogglesItem;
import dev.neothaum.research.ScanManager;
import dev.neothaum.tiles.NodeBlockEntity;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

/**
 * Addon: aura nodes on Xaero's fullscreen World Map, and only there (never on the minimap or as in-world waypoints). A node goes on the map
 * once the player has scanned it and then seen it through Goggles of Revealing; what has been seen is kept per world in config/neothaum/nodemap. A button on
 * the map shows or hides them.
 */
public final class WorldMapNodes {
	private static final Gson GSON = new GsonBuilder().create();
	private static final Identifier NODES = NeoThaum.id("textures/misc/nodes.png");
	/** dimension id -> "x,y,z" -> node */
	private static Map<String, Map<String, Seen>> seen = new HashMap<>();
	private static @Nullable String worldKey;
	private static boolean dirty;
	private static int tick;

	/** What the map remembers of a node: where it is, its type and modifier, and its strongest aspect for the colour. */
	record Seen(int x, int y, int z, String type, String modifier, String aspect) {
	}

	/** Every second, while wearing goggles, note the nodes in the chunks the client has loaded. */
	public static void tick(Minecraft mc) {
		if (mc.player == null || mc.level == null) {
			if (worldKey != null) {
				save();
				worldKey = null;
			}
			return;
		}
		String key = currentWorldKey(mc);
		if (!key.equals(worldKey)) {
			save();
			worldKey = key;
			load();
		}
		if (++tick % 20 != 0 || !GogglesItem.isWearingGoggles(mc.player)) {
			if (tick % 1200 == 0 && dirty) {
				save();
			}
			return;
		}
		var nodes = seen.computeIfAbsent(mc.level.dimension().identifier().toString(), d -> new HashMap<>());
		int cx = mc.player.blockPosition().getX() >> 4, cz = mc.player.blockPosition().getZ() >> 4;
		int r = mc.options.getEffectiveRenderDistance();
		for (int x = cx - r; x <= cx + r; x++) {
			for (int z = cz - r; z <= cz + r; z++) {
				LevelChunk chunk = mc.level.getChunkSource().getChunk(x, z, false);
				if (chunk == null) {
					continue;
				}
				for (BlockEntity be : chunk.getBlockEntities().values()) {
					if (be instanceof NodeBlockEntity node && node.getAspects() != null
						&& ScanManager.hasBeenScanned(mc.player, ScanManager.ScanResult.node(node.getBlockPos()))) {
						var p = node.getBlockPos();
						Aspect top = null;
						for (Aspect a : node.getAspects().getAspects()) {
							if (top == null || node.getAspects().getAmount(a) > node.getAspects().getAmount(top)) {
								top = a;
							}
						}
						NodeModifier mod = node.getNodeModifier();
						Seen s = new Seen(p.getX(), p.getY(), p.getZ(), node.getNodeType().name(), mod == null ? "" : mod.name(), top == null ? "" : top.getTag());
						if (!s.equals(nodes.put(p.getX() + "," + p.getY() + "," + p.getZ(), s))) {
							dirty = true;
						}
					}
				}
			}
		}
	}

	/** Adds the show/hide button to the map screen, below Xaero's own buttons on the right. */
	public static void addToggle(Screen screen, java.util.function.Consumer<Button> add) {
		Button b = Button.builder(label(), btn -> {
			NeoThaumConfig.current.worldMapNodes = !NeoThaumConfig.current.worldMapNodes;
			NeoThaumConfig.save();
			btn.setMessage(label());
		}).bounds(screen.width - 22, screen.height - 22, 20, 20).tooltip(Tooltip.create(Component.translatable("neothaum.worldmap.nodes.tooltip"))).build();
		add.accept(b);
	}

	private static Component label() {
		return Component.literal(NeoThaumConfig.current.worldMapNodes ? "✦" : "✧");
	}

	/**
	 * Draws the remembered nodes of the dimension the map is showing. The map centre is block (cameraX, cameraZ), and a block is
	 * scale / screenScale GUI pixels across.
	 */
	public static void render(GuiGraphicsExtractor g, Screen screen, int mouseX, int mouseY, double cameraX, double cameraZ, double scale,
		double screenScale, @Nullable ResourceKey<Level> viewed) {
		Minecraft mc = Minecraft.getInstance();
		if (!NeoThaumConfig.current.worldMapNodes || mc.level == null) {
			return;
		}
		ResourceKey<Level> dim = viewed != null ? viewed : mc.level.dimension();
		var nodes = seen.get(dim.identifier().toString());
		if (nodes == null || nodes.isEmpty()) {
			return;
		}
		double ppb = scale / screenScale;
		float cx = screen.width / 2.0F, cy = screen.height / 2.0F;
		Seen hovered = null;
		int frame = (int) (mc.level.getGameTime() % 32);
		List<Seen> list = new ArrayList<>(nodes.values());
		for (Seen s : list) {
			// a node only goes on the map once scanned; this also hides any remembered before that rule
			if (!ScanManager.hasBeenScanned(mc.player, ScanManager.ScanResult.node(new net.minecraft.core.BlockPos(s.x, s.y, s.z)))) {
				continue;
			}
			int px = (int) Math.round(cx + (s.x + 0.5 - cameraX) * ppb);
			int py = (int) Math.round(cy + (s.z + 0.5 - cameraZ) * ppb);
			if (px < -8 || py < -8 || px > screen.width + 8 || py > screen.height + 8) {
				continue;
			}
			// as goggles show it in the world: the swirl of its main aspect's colour under its type's core, from nodes.png (32 frames a row,
			// each 64 pixels)
			g.blit(RenderPipelines.GUI_TEXTURED, NODES, px - 7, py - 7, frame * 64.0F, 0.0F, 14, 14, 64, 64, 2048, 2048, ARGB.opaque(color(s)));
			g.blit(RenderPipelines.GUI_TEXTURED, NODES, px - 5, py - 5, frame * 64.0F, strip(s) * 64.0F, 10, 10, 64, 64, 2048, 2048);
			if (Math.abs(mouseX - px) <= 6 && Math.abs(mouseY - py) <= 6) {
				hovered = s;
			}
		}
		if (hovered != null) {
			String name = Component.translatable("nodetype." + hovered.type + ".name").getString();
			if (!hovered.modifier.isEmpty()) {
				name += ", " + Component.translatable("nodemod." + hovered.modifier + ".name").getString();
			}
			g.setTooltipForNextFrame(mc.font, Component.literal(name + " (" + hovered.x + ", " + hovered.y + ", " + hovered.z + ")"), mouseX, mouseY);
		}
	}

	/** The row of nodes.png TileNodeRenderer used for the node's core. */
	private static int strip(Seen s) {
		return switch (NodeType.valueOf(s.type)) {
			case UNSTABLE -> 6;
			case DARK -> 2;
			case TAINTED -> 5;
			case PURE -> 4;
			case HUNGRY -> 3;
			default -> 1;
		};
	}

	private static int color(Seen s) {
		Aspect a = s.aspect.isEmpty() ? null : Aspect.getAspect(s.aspect);
		return a == null ? 0xFFFFFF : a.getColor();
	}

	private static String currentWorldKey(Minecraft mc) {
		if (mc.getSingleplayerServer() != null) {
			return "sp_" + mc.getSingleplayerServer().getWorldData().getLevelName();
		}
		return mc.getCurrentServer() != null ? "mp_" + mc.getCurrentServer().ip : "unknown";
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("neothaum").resolve("nodemap")
			.resolve(worldKey.replaceAll("[^A-Za-z0-9._-]", "_") + ".json");
	}

	private static void load() {
		seen = new HashMap<>();
		dirty = false;
		try {
			Path f = file();
			if (Files.exists(f)) {
				try (Reader r = Files.newBufferedReader(f)) {
					Map<String, Map<String, Seen>> m = GSON.fromJson(r, new TypeToken<Map<String, Map<String, Seen>>>() {
					}.getType());
					if (m != null) {
						seen = m;
					}
				}
			}
		} catch (Exception e) {
			NeoThaum.LOGGER.warn("Could not read the world map's nodes", e);
		}
	}

	private static void save() {
		if (worldKey == null || !dirty) {
			return;
		}
		try {
			Path f = file();
			Files.createDirectories(f.getParent());
			try (Writer w = Files.newBufferedWriter(f)) {
				GSON.toJson(seen, w);
			}
			dirty = false;
		} catch (Exception e) {
			NeoThaum.LOGGER.warn("Could not save the world map's nodes", e);
		}
	}

	private WorldMapNodes() {
	}
}
