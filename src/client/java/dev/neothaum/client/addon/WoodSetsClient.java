package dev.neothaum.client.addon;

import dev.neothaum.NeoThaum;
import dev.neothaum.addon.WoodSets;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;

/**
 * The client half of the wood sets: each boat's model layer (named as vanilla's are, so the boat renderer finds
 * textures/entity/boat/&lt;wood&gt;.png and textures/entity/chest_boat/&lt;wood&gt;.png) and its renderer. Shelves use vanilla's shelf
 * renderer through the shelf block entity, and the doors, trapdoors and the rest pick their render type in their models.
 */
public final class WoodSetsClient {
	public static void init() {
		for (WoodSets.Set1 s : new WoodSets.Set1[]{WoodSets.GREATWOOD, WoodSets.SILVERWOOD}) {
			ModelLayerLocation boat = new ModelLayerLocation(NeoThaum.id("boat/" + s.name), "main");
			ModelLayerLocation chestBoat = new ModelLayerLocation(NeoThaum.id("chest_boat/" + s.name), "main");
			ModelLayerRegistry.registerModelLayer(boat, BoatModel::createBoatModel);
			ModelLayerRegistry.registerModelLayer(chestBoat, BoatModel::createChestBoatModel);
			EntityRenderers.register(s.boat, context -> new BoatRenderer(context, boat));
			EntityRenderers.register(s.chestBoat, context -> new BoatRenderer(context, chestBoat));
		}
	}

	private WoodSetsClient() {
	}
}
