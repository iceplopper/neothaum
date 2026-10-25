package dev.thaumvibe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thaumvibe.Thaumvibe;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

/**
 * Minimal Wavefront OBJ model (Forge's AdvancedModelLoader/WavefrontObject replacement): v/vt/vn, tri or quad faces, and named groups
 * (g/o) for renderPart.
 */
public final class ObjModel {
	private final List<float[]> faces = new ArrayList<>();
	private final List<String> groups = new ArrayList<>();

	private ObjModel() {
	}

	/** Each face is stored as 4 vertices of {x, y, z, u, v, nx, ny, nz}; triangles repeat their last vertex. */
	public static ObjModel load(Identifier location) {
		ObjModel model = new ObjModel();
		List<float[]> v = new ArrayList<>(), vt = new ArrayList<>(), vn = new ArrayList<>();
		String group = "";
		try (BufferedReader r = new BufferedReader(new InputStreamReader(Minecraft.getInstance().getResourceManager().open(location), StandardCharsets.UTF_8))) {
			String line;
			while ((line = r.readLine()) != null) {
				String[] p = line.trim().split("\\s+");
				switch (p[0]) {
					case "v" -> v.add(new float[]{Float.parseFloat(p[1]), Float.parseFloat(p[2]), Float.parseFloat(p[3])});
					case "vt" -> vt.add(new float[]{Float.parseFloat(p[1]), Float.parseFloat(p[2])});
					case "vn" -> vn.add(new float[]{Float.parseFloat(p[1]), Float.parseFloat(p[2]), Float.parseFloat(p[3])});
					case "g", "o" -> group = p.length > 1 ? p[1] : "";
					case "f" -> {
						float[] face = new float[32];
						for (int i = 0; i < 4; i++) {
							String[] idx = p[Math.min(i + 1, p.length - 1)].split("/");
							float[] pos = v.get(Integer.parseInt(idx[0]) - 1);
							float[] uv = idx.length > 1 && !idx[1].isEmpty() ? vt.get(Integer.parseInt(idx[1]) - 1) : new float[2];
							float[] n = idx.length > 2 ? vn.get(Integer.parseInt(idx[2]) - 1) : new float[]{0, 1, 0};
							// OBJ texture space has v pointing up; WavefrontObject flipped it the same way
							System.arraycopy(new float[]{pos[0], pos[1], pos[2], uv[0], 1.0F - uv[1], n[0], n[1], n[2]}, 0, face, i * 8, 8);
						}
						model.faces.add(face);
						model.groups.add(group);
					}
					default -> {
					}
				}
			}
		} catch (Exception e) {
			Thaumvibe.LOGGER.error("Failed to load OBJ model {}", location, e);
		}
		return model;
	}

	public void render(PoseStack.Pose pose, VertexConsumer vc, int light, int overlay, int argb) {
		this.renderPart(null, pose, vc, light, overlay, argb);
	}

	/** IModelCustom.renderPart: only the faces of the named group (null renders everything). */
	public void renderPart(String part, PoseStack.Pose pose, VertexConsumer vc, int light, int overlay, int argb) {
		for (int k = 0; k < this.faces.size(); k++) {
			if (part != null && !part.equalsIgnoreCase(this.groups.get(k))) {
				continue;
			}
			float[] f = this.faces.get(k);
			for (int i = 0; i < 4; i++) {
				int o = i * 8;
				vc.addVertex(pose, f[o], f[o + 1], f[o + 2]).setColor(argb).setUv(f[o + 3], f[o + 4]).setOverlay(overlay).setLight(light)
					.setNormal(pose, f[o + 5], f[o + 6], f[o + 7]);
			}
		}
	}
}
