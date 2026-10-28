package dev.neothaum.world;

import dev.neothaum.NeoThaum;
import dev.neothaum.mixin.StructureTemplatePoolAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

/**
 * VillageWizardManager and VillageBankerManager: the wizard's tower and the banker's house as village buildings, and only that (TC4 never
 * built them in the wild). Each is added to every village type's house pool at weight 1, which gives about half of all villages one of each,
 * as TC4's limit of none or one per village did. The banker's house comes in two of its six forms, a low one with its table by the door and
 * a tall one with its table further in. Each village type gets its own versions (TC4 re-skinned them only for deserts, in sandstone; here
 * savannas also build them in acacia, and taiga and snowy villages in spruce), made by agentic/tools/make_village_variants.py.
 */
public final class TCVillages {
	private static final String[] TYPES = {"plains", "desert", "savanna", "snowy", "taiga"};

	private TCVillages() {
	}

	public static void init() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			if (!dev.neothaum.config.NeoThaumConfig.current.villageBuildings) {
				return;
			}
			var pools = server.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);
			for (String type : TYPES) {
				pools.getOptional(Identifier.withDefaultNamespace("village/" + type + "/houses")).ifPresent(pool -> {
					String dir = type.equals("plains") ? "village/" : "village/" + type + "/";
					add(pool, dir + "wizard_tower", 1);
					add(pool, dir + "banker_home_1", 1);
					add(pool, dir + "banker_home_tall_2", 1);
				});
			}
		});
	}

	private static void add(StructureTemplatePool pool, String template, int weight) {
		StructurePoolElement element = StructurePoolElement.legacy(NeoThaum.id(template).toString()).apply(StructureTemplatePool.Projection.RIGID);
		var list = ((StructureTemplatePoolAccessor) pool).neothaum$templates();
		for (int i = 0; i < weight; i++) {
			list.add(element);
		}
	}
}
