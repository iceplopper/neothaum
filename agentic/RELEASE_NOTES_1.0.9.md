# NeoThaum 1.0.9 (alpha)

Includes everything from [1.0.8](https://github.com/iceplopper/neothaum/releases/tag/v1.0.8-alpha), plus the changes below. This release is about working alongside other mods, and shaders.

Two downloads:
- **`neothaum-1.0.9.jar`:** the mod alone.
- **`NeoThaum-1.0.9.mrpack`:** a Modrinth modpack. It contains the mod with Fabric API, ModMenu, JEI, Jade, Xaero's Minimap and World Map, and now **LambDynamicLights**, set up for Minecraft 26.3 with Fabric Loader 0.20.1.

## Major features

- **Shader packs (Iris).** NeoThaum's glowing effects now work with shader packs. Before, the node inside a node jar was invisible under Complementary. Checked with Complementary Reimagined and BSL: nodes, essentia, infusion, the alchemy devices, eldritch structures, the harness and the warp screen effects.
- **Bright nodes shine.** Bright nodes now give full light (15; every other node keeps TC4's 8). Under a shader pack, looking toward one in clear sight shows a soft lens flare in the colour of its strongest aspect. Both are NeoThaum additions, each with its own switch in the config screen.
- **Glowing wisps.** With LambDynamicLights (now in the modpack), wisps light up their surroundings as they fly, more softly than a bright node.
- **Trinkets.** With Trinkets Updated installed, amulets, rings and belts go in its necklace, ring and belt slots, and work exactly as in NeoThaum's own. Anything in NeoThaum's own slots moves across when you join.
- **REI.** With Roughly Enough Items installed, NeoThaum has the same recipe pages as in JEI: arcane workbench, crucible, infusion, infusion enchanting and aspect combinations.

## Minor features

- **Also checked:** Sodium (no changes needed) and AppleSkin (shows NeoThaum's food values).
- **The zombie brain can be eaten**, as in TC4: 4 hunger, wolves eat it, it often brings on Hunger, and it gives warp.
- **Arcane keys stack to 64**, as in TC4. Their recipes make two keys, which stacks of one couldn't hold.
- **Smaller fixes:** the aspect orb, golem dart, eldritch orb, golem bobber, pech blast and floating item have names (they showed as raw text in Jade).
